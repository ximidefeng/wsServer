package com.ufo.mas.wsserver.bean;

import lombok.Data;

@Data
public class DevData {
    private String devId = "";
    private String devType = "WIFI";
    private String devMac = "";
    private String devName = "";
    private String devAlias = "";
    private String areaFullCode = "";
    private String areaFullName = "";

    private Alarm alarm = new Alarm();
    private Battery battery = new Battery();
    private Temperature temperature = new Temperature();
    private Volt volt = new Volt();
    private PackInfo packInfo = new PackInfo();
    private Protect protect = new Protect();

    private long createTime; //数据创建时间,毫秒
}
