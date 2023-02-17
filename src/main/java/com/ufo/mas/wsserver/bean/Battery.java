package com.ufo.mas.wsserver.bean;

import lombok.Data;

@Data
public class Battery {
    private Double totalVolt = 0d;
    private Double current = 0d;
    private Double socValue = 0d;
    private Double sohValue = 0d;
    private Double remainCapacity = 0d;
    private Double fullCapacity = 0d;
}
