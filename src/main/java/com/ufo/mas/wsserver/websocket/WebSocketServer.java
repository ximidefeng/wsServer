package com.ufo.mas.wsserver.websocket;


import com.alibaba.fastjson.JSONObject;
import com.ufo.mas.wsserver.bean.DevData;
import com.ufo.mas.wsserver.datastation.DataSender;
import com.ufo.mas.wsserver.datastation.Store;
import com.ufo.mas.wsserver.datastation.Utils;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

import java.io.IOException;
import java.util.Map;

@Slf4j
@ServerEndpoint(path = "${ws.path}", port = "${ws.port}")
public class WebSocketServer {

    @Autowired
    private DataSender dataSender;

    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers,
                          @RequestParam String req, @RequestParam MultiValueMap reqMap,
                          @PathVariable String args, @PathVariable Map pathMap) {
        session.setSubprotocols("stomp");
        //log.info("New connection handshake. sessionid = {} ", session.id().asShortText());
    }

    @OnOpen
    public void onOpen(Session session, HttpHeaders headers,
                       @RequestParam String req, @RequestParam MultiValueMap reqMap,
                       @PathVariable String args, @PathVariable Map pathMap) {
        log.info("New connection success connected. sessionid = {} ", session.id().asShortText());
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        String sessionid = session.id().asShortText();
        log.info("Connection closed, sessionid = {} ", sessionid);
        String devNo = Store.getDevNoBySessionId(sessionid);
        if (StringUtils.hasText(devNo)) {
            Store.dev2SessionMap.remove(devNo);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String sessionid = session.id().asShortText();
        log.error("WebSocket onError! sessionid = {}, error: {} ", sessionid, throwable.getMessage());
        session.close();
        String devNo = Store.getDevNoBySessionId(sessionid);
        if (StringUtils.hasText(devNo)) {
            Store.dev2SessionMap.remove(devNo);
        }
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        log.info("Received message [length={}] from session - {} : {}", message.length(), session.id().asShortText(), message);
        log.info("Connections = {}, devCacheCount = {}", Store.dev2SessionMap.size(), Store.devDataMap.size());
        if (StringUtils.hasText(message)) {
            int length = message.length();
            if (message.startsWith("UFO") && length > 20) {
                String devNo = message.substring(0, 20);
                String dataStr = message.substring(20, length);
                DevData dev = Store.getDevData(devNo);
                if (dev == null) {
                    dev = new DevData();
                    dev.setDevName(devNo);
                    dev.setCreateTime(System.currentTimeMillis());
                }
                Store.pushDevSession(devNo, session); //????????????
                char lastChar = message.charAt(length - 1);
                if ((length==129||length==157||length==161) && lastChar=='A') { //?????????(?????????A)???8?????????=129???15?????????=157???16??????=161
                    dev.getPackInfo().setPackAddr(dataStr.substring(2, 4));
                    int packNum = Utils.hex2Int(dataStr.substring(14, 16)); // packNum
                    int voltCnt = Utils.hex2Int(dataStr.substring(16, 18)); //??????????????????
                    dev.getPackInfo().setPackNum(packNum);

                    double maxVolt = 0;
                    double minVolt = 0;
                    int maxIdx = 0;
                    int minIdx = 0;
                    int start = 18; //?????????????????????????????????
                    for (int i = 0; i < voltCnt; i++) { // voltCnt=8/15/16
                        String str = dataStr.substring(start, start + 4);
                        double val = Utils.hex2Int(str);
                        log.info("str = {}, val={}", str, val);
                        dev.getVolt().getVcells()[i] = val;
                        if (i == 0) {
                            maxVolt = val;
                            minVolt = val;
                        } else {
                            if (val > maxVolt) {
                                maxVolt = val;
                                maxIdx = i;
                            }
                            if (val < minVolt) {
                                minVolt = val;
                                minIdx = i;
                            }
                        }
                        start += 4;
                    }

                    //int start = 78; //15?????????????????????=78
                    dev.getVolt().setMaxVolt(maxVolt);
                    dev.getVolt().setMaxVoltNO(maxIdx + 1);
                    dev.getVolt().setMinVolt(minVolt);
                    dev.getVolt().setMinVoltNO(minIdx + 1);
                    dev.getVolt().setVoltDiff(maxVolt - minVolt);

                    start += 2; //??????????????????(??????2??????)
                    int t = Utils.hex2Int(dataStr.substring(start, start + 4));
                    dev.getTemperature().setTemp1(Utils.round((t - 2730) / 10.0d, 1));

                    start += 4;
                    t = Utils.hex2Int(dataStr.substring(start, start + 4));
                    dev.getTemperature().setTemp2(Utils.round((t - 2730) / 10.0d, 1));

                    start += 4;
                    t = Utils.hex2Int(dataStr.substring(start, start + 4));
                    dev.getTemperature().setTemp3(Utils.round((t - 2730) / 10.0d, 1));

                    start += 4;
                    t = Utils.hex2Int(dataStr.substring(start, start + 4));
                    dev.getTemperature().setTemp4(Utils.round((t - 2730) / 10.0d, 1));

                    start += 4;
                    t = Utils.hex2Int(dataStr.substring(start, start + 4));
                    dev.getTemperature().setMosTemp(Utils.round((t - 2730) / 10.0d, 1));

                    start += 4;
                    t = Utils.hex2Int(dataStr.substring(start, start + 4));
                    dev.getTemperature().setEvnTemp(Utils.round((t - 2730) / 10.0d, 1));

                    //PACK ?????? ??????Ah
                    start += 4;
                    t = Utils.hex2bmInt(dataStr.substring(start, start + 4)); //???????????????
                    dev.getBattery().setCurrent(Utils.round(t / 100.0d, 2));

                    //PACK ????????? ?????? V
                    start += 4;
                    t = Utils.hex2Int(dataStr.substring(start, start + 4));
                    dev.getBattery().setTotalVolt(Utils.round(t / 1000.0d, 2));

                    //PACK ???????????? ?????? Ah
                    start += 4;
                    t = Utils.hex2Int(dataStr.substring(start, start + 4));
                    int remain = t;
                    dev.getBattery().setRemainCapacity(Utils.round(t / 100.0d, 2));

                    start += 2;//?????????????????????????????????????????????+2
                    start += 4;//PACK ???????????? ?????? Ah
                    t = Utils.hex2Int(dataStr.substring(start, start + 4));
                    int full = t;
                    dev.getBattery().setFullCapacity(Utils.round(t / 100.0d, 2));

                    double soc = Utils.round((remain * 1.0d / full * 1.0d) * 100.0d, 2);
                    dev.getBattery().setSocValue(soc); //SOC
                }else if (length == 117 && lastChar == 'D') { //????????????(?????????D)???8?????????=117???15?????????=117???16??????=117
                    dev.getPackInfo().setBMS_HWVersion(Utils.hexToAscii(dataStr.substring(12, 52)));
                    dev.getPackInfo().setPackSN(Utils.hexToAscii(dataStr.substring(52, 92)));
                    dataSender.send(dev); //????????????
                }else if ((length==99||length==113||length==115) && lastChar == 'B') { //?????????(???????????????,?????????B)???8?????????=99???15?????????=113???16??????=115
                    int start = 16;//????????????
                    int vCnt = Utils.hex2Int(dataStr.substring(start, start + 2));//?????????????????? 16,18
                    start += 2; //??????vCnt?????? 18
                    start += (2 * vCnt); //??????vCnt??????????????? 48
                    int tCnt = Utils.hex2Int(dataStr.substring(start, start + 2));//?????????????????? 48,50
                    start += 2; //??????tCnt?????? 50
                    start += (2 * tCnt); //??????tCnt????????? 62
                    start += 6; //??????-??????????????????/???????????????/??????????????????

                    //????????????1???A19-????????????index=68,70
                    int vA19 = Utils.hex2Int(dataStr.substring(start, start + 2));//68,70
                    start += 2; //??????-????????????1

                    //????????????2???A20-????????????index=70,72
                    int vA20 = Utils.hex2Int(dataStr.substring(start, start + 2)); //70,72
                    start += 6; //??????-????????????2/????????????/????????????

                    //???????????? A23-????????????
                    int vA23 = Utils.hex2Int(dataStr.substring(start, start + 2)); //76,78
                    start += 6; //??????-????????????/????????????1/????????????2

                    //????????????1 ???A24-????????????
                    int vA24 = Utils.hex2Int(dataStr.substring(start, start + 2)); //82,84
                    start += 2; //??????-????????????1

                    //????????????2 ???A25-????????????
                    int vA25 = Utils.hex2Int(dataStr.substring(start, start + 2)); //84,86

                    dev.getProtect().setPSht(Utils.isBitOn(vA19, 6) ? 1 : 0);
                    dev.getProtect().setPDOC(Utils.isBitOn(vA19, 5) ? 1 : 0);
                    dev.getProtect().setPCOC(Utils.isBitOn(vA19, 4) ? 1 : 0);
                    dev.getProtect().setPTUV(Utils.isBitOn(vA19, 3) ? 1 : 0);
                    dev.getProtect().setPTOV(Utils.isBitOn(vA19, 2) ? 1 : 0);
                    dev.getProtect().setPUV(Utils.isBitOn(vA19, 1) ? 1 : 0);
                    dev.getProtect().setPOV(Utils.isBitOn(vA19, 0) ? 1 : 0);

                    dev.getProtect().setPFully(Utils.isBitOn(vA20, 7) ? 1 : 0);
                    dev.getProtect().setPEUT(Utils.isBitOn(vA20, 6) ? 1 : 0);
                    dev.getProtect().setPEOV(Utils.isBitOn(vA20, 5) ? 1 : 0);
                    dev.getProtect().setPMOV(Utils.isBitOn(vA20, 4) ? 1 : 0);
                    dev.getProtect().setPDCUT(Utils.isBitOn(vA20, 3) ? 1 : 0);
                    dev.getProtect().setPCUT(Utils.isBitOn(vA20, 2) ? 1 : 0);
                    dev.getProtect().setPDOT(Utils.isBitOn(vA20, 1) ? 1 : 0);
                    dev.getProtect().setPCOT(Utils.isBitOn(vA20, 0) ? 1 : 0);

                    dev.getProtect().setPSamp(Utils.isBitOn(vA23, 5) ? 1 : 0);
                    dev.getProtect().setPCells(Utils.isBitOn(vA23, 4) ? 1 : 0);
                    dev.getProtect().setPNTC(Utils.isBitOn(vA23, 2) ? 1 : 0);
                    dev.getProtect().setPDMOS(Utils.isBitOn(vA23, 1) ? 1 : 0);
                    dev.getProtect().setPCMOS(Utils.isBitOn(vA23, 0) ? 1 : 0);

                    dev.getAlarm().setADOC(Utils.isBitOn(vA24, 5) ? 1 : 0);
                    dev.getAlarm().setACOC(Utils.isBitOn(vA24, 4) ? 1 : 0);
                    dev.getAlarm().setATUV(Utils.isBitOn(vA24, 3) ? 1 : 0);
                    dev.getAlarm().setATOV(Utils.isBitOn(vA24, 2) ? 1 : 0);
                    dev.getAlarm().setAUV(Utils.isBitOn(vA24, 1) ? 1 : 0);
                    dev.getAlarm().setAOV(Utils.isBitOn(vA24, 0) ? 1 : 0);

                    dev.getAlarm().setAUCAP(Utils.isBitOn(vA25, 7) ? 1 : 0);
                    dev.getAlarm().setAMOT(Utils.isBitOn(vA25, 6) ? 1 : 0);
                    dev.getAlarm().setAEUT(Utils.isBitOn(vA25, 5) ? 1 : 0);
                    dev.getAlarm().setAEOT(Utils.isBitOn(vA25, 4) ? 1 : 0);
                    dev.getAlarm().setADUT(Utils.isBitOn(vA25, 3) ? 1 : 0);
                    dev.getAlarm().setACUT(Utils.isBitOn(vA25, 2) ? 1 : 0);
                    dev.getAlarm().setADOT(Utils.isBitOn(vA25, 1) ? 1 : 0);
                    dev.getAlarm().setACOT(Utils.isBitOn(vA25, 0) ? 1 : 0);
                }else if (length == 77 && lastChar == 'C') { //??????????????????(?????????C)???8?????????=77???15?????????=77???16??????=77 / 250146006028 50313553313030412D31323438302D312E3030 00F58BC
                    dev.getPackInfo().setBMS_SWVersion(Utils.hexToAscii(dataStr.substring(12, 51)));
                }
                Store.pushDevData(dev);
                log.info("<--: Save dev data to cache :--> \n {}", JSONObject.toJSON(dev));
            } else {
                log.info("Received invalid message [{}] from session - {} ", message, session.id().asShortText());
                session.close();
            }
        }
    }

    @OnBinary
    public void onBinary(Session session, byte[] bytes) {
        System.out.println("==================== onBinary start ======================");
        for (byte b : bytes) {
            System.out.print(b + " ");
        }
        //session.sendBinary(new byte[]{0x01});
        System.out.println("==================== onBinary end ======================");
    }

    @OnEvent
    public void onEvent(Session session, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    log.info("read idle");
                    break;
                case WRITER_IDLE:
                    log.info("write idle");
                    break;
                case ALL_IDLE:
                    log.info("all idle");
                    break;
                default:
                    break;
            }
        }
    }


}
