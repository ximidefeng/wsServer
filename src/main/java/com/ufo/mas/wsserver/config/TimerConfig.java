package com.ufo.mas.wsserver.config;

import com.ufo.mas.wsserver.bean.DevData;
import com.ufo.mas.wsserver.bean.PackInfo;
import com.ufo.mas.wsserver.datastation.DataSender;
import com.ufo.mas.wsserver.datastation.Store;
import com.ufo.mas.wsserver.model.UpgradeInfo;
import com.ufo.mas.wsserver.util.VersionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yeauty.pojo.Session;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class TimerConfig {

    @Bean
    public ScheduledExecutorService upgradeScheduled() {
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
        scheduledThreadPool.scheduleAtFixedRate(new UpgradeTask("upgradeTask"), 5, 5, TimeUnit.SECONDS);
        return scheduledThreadPool;
    }

    class UpgradeTask implements Runnable {
        private String name;

        public UpgradeTask(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            log.info("upgradeTask start. devUpgradeMap size = {}.", Store.devUpgradeMap.size());
            try {
                upgradeWork();
            } catch (Exception e) {
                log.info("upgradeTask err.", e);
            }
            log.info("upgradeTask end.");
        }

        private void upgradeWork() {
            // 轮询升级列表
            Iterator<Map.Entry<String, UpgradeInfo>> iterator = Store.devUpgradeMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, UpgradeInfo> entry = iterator.next();
                String devNo = entry.getKey();
                UpgradeInfo upgradeInfo = entry.getValue();
                log.info("upgradeInfo.devNo = {}", devNo);
                // 先到设备表中查询如果查不到则说明没有此设备建立websocket连接
                Session session = Store.getSession4Dev(devNo);
                if (session != null) {
                    // 如果有这个设备则先比较此设备的版本是否需要升级
                    DevData devData = Store.getDevData(devNo);
                    if (devData != null) {
                        Optional<String> version = Optional.ofNullable(devData).map(DevData::getPackInfo).map(PackInfo::getBMS_SWVersion);
                        if (version.isPresent()) {
                            String deviceVersion = version.get();
                            int sendCount = upgradeInfo.getSendCount();
                            if (sendCount < 3 ){
//                                    && VersionUtil.compareVersion(upgradeInfo.getDeviceVersion(), deviceVersion) > 0) {
                                // 如果需要升级则发送websocket消息给此设备
                                session.sendText(upgradeInfo.buildMessage());
                                upgradeInfo.setSendCount(++sendCount);
                            } else {
                                // 如果版本号比当前版本号小或者發送次數已经达标 则说明无需升级 或者升级已经完成，那么移除此任务
                                iterator.remove();
                            }
                        }
                    } else {
                        log.info("DeviceInfo is blank.devNo = {}", devNo);
                    }

                }
            }
        }


    }


}
