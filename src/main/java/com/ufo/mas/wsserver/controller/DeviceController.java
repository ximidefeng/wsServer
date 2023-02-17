package com.ufo.mas.wsserver.controller;

import com.ufo.mas.wsserver.datastation.Store;
import com.ufo.mas.wsserver.model.UpgradeInfo;
import com.ufo.mas.wsserver.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.parser.Upgrade;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 设备控制处理类
 */
@Slf4j
@Controller
@RequestMapping("/mas")
public class DeviceController {


    /**
     * 设备升级
     *
     * @return object
     */
    @RequestMapping("/upgrade")
    @ResponseBody
    public Object upgrade(@RequestBody UpgradeInfo upgradeInfo) {
        String devId = upgradeInfo.getDeviceId();
        String deviceVersion = upgradeInfo.getDeviceVersion();
        Integer cmd = upgradeInfo.getCmd();
        String url = upgradeInfo.getUrl();
        log.info("upgradeInfo. devNo = {}, deviceVersion = {}, cmd = {}, url = {}", devId, deviceVersion, cmd, url);
        /**
         * 这里需要将数据信息加入到队列中
         */
        Store.devUpgradeMap.put(devId , upgradeInfo);
        return ResponseUtil.ok();
    }
}
