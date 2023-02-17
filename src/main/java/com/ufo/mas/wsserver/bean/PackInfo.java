package com.ufo.mas.wsserver.bean;

import lombok.Data;

@Data
public class PackInfo {
    private String bMS_SWVersion = "";
    private String bMS_HWVersion = "";
    private String packSN = "";
    private String packAddr = "";
    private Integer packNum = 0;
}
