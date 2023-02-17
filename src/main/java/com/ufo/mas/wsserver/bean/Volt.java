package com.ufo.mas.wsserver.bean;

import lombok.Data;

@Data
public class Volt {
    private Double maxVolt = 0d;
    private Double minVolt = 0d;
    private Double voltDiff = 0d;
    private Integer maxVoltNO = 0;
    private Integer minVoltNO = 0;
    private double [] vcells = new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//16个电压
}
