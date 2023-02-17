package com.ufo.mas.wsserver.config;

import com.ufo.mas.wsserver.datastation.DataSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSenderConfig {

    @Value("${data-sender.post-url}")
    private String postUrl;

    @Bean
    public DataSender dataSender(){
        return new DataSender.Builder().setUrl(postUrl).build();
    }
}
