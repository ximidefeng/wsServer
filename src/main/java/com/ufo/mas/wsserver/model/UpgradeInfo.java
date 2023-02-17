package com.ufo.mas.wsserver.model;

import lombok.Data;

/**
 * 设备更新信息
 */
@Data
public class UpgradeInfo {

    /**
     * 设备Id
     */
    private String deviceId;

    /**
     * 设备版本
     */
    private String deviceVersion;

    /**
     * 命令
     */
    private Integer cmd = 0;

    /**
     * rom文件地址
     */
    private String url;

    /**
     * 发送次数
     */
    private int sendCount;

    /**
     * 占位符
     */
    private String character = "|";


    /**
     * 组装数据
     * @return 数据Message
     */
    public String buildMessage() {
        StringBuilder sb = new StringBuilder(10);
        sb.append("UFO").append(character).append(deviceId).append(character).append(deviceVersion)
                .append(character).append(cmd).append(character).append(url);
        return sb.toString();
    }
}
