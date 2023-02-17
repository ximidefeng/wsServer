package com.ufo.mas.wsserver.datastation;

import com.alibaba.fastjson.JSONObject;
import com.ufo.mas.wsserver.bean.DevData;
import com.ufo.mas.wsserver.http.OkHttp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
public class DataSender {
    @Autowired
    private OkHttp okHttp;

    private final String url;

    //构造方法
    public DataSender() {
        this(new Builder());
    }

    //构造方法
    public DataSender(Builder builder) {
        this.url = builder.url;
    }

    public void send(DevData devData) {
        String resp = okHttp.doPostJson(url, JSONObject.toJSON(devData).toString());
        log.info("========================Send http to serverless========================\n{}", resp);
    }

    public static class Builder {
        private String url;

        public Builder() {

        }

        public Builder(DataSender dataSender) {
            this.url = dataSender.url;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public DataSender build() {
            return new DataSender(this);
        }
    }
}
