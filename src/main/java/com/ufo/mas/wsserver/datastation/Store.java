package com.ufo.mas.wsserver.datastation;

import com.ufo.mas.wsserver.bean.DevData;
import com.ufo.mas.wsserver.model.UpgradeInfo;
import org.springframework.util.StringUtils;
import org.yeauty.pojo.Session;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Store {

    public static ConcurrentHashMap<String, String> sessionId2DevIdMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Session> dev2SessionMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, DevData> devDataMap = new ConcurrentHashMap<>();

    /**
     * 需要升级设备表
     */
    public static ConcurrentHashMap<String, UpgradeInfo> devUpgradeMap = new ConcurrentHashMap<>();

    public static void pushDevSession(String devNo, Session session){
        if(devNo == null || "".equals(devNo)) return;
        if(session == null) return;
        Session old = dev2SessionMap.get(devNo);
        if(old == null){
            dev2SessionMap.put(devNo, session);
            sessionId2DevIdMap.put(session.id().asShortText(), devNo);
        }else if(!session.id().asShortText().equals(old.id().asShortText())){
            old.close();
            dev2SessionMap.put(devNo, session);
            sessionId2DevIdMap.put(session.id().asShortText(), devNo);
        }
    }

    public static Session getSession4Dev(String devNo){
        return dev2SessionMap.get(devNo);
    }

    public static void pushDevData(DevData data){
        if(data != null && StringUtils.hasText(data.getDevName())){
            data.setCreateTime(System.currentTimeMillis());
            devDataMap.put(data.getDevName(), data);
        }
    }

    public static DevData getDevData(String devNo){
        return devDataMap.get(devNo);
    }

    public static String getDevNoBySessionId(String sid){
        return sessionId2DevIdMap.get(sid);
    }

    public static void clearSession(){
        sessionId2DevIdMap.clear();
        dev2SessionMap.clear();
    }

    public static void clearDevData(){
        devDataMap.clear();
    }

    public static void clearAll(){
        clearSession();
        clearDevData();
    }
}
