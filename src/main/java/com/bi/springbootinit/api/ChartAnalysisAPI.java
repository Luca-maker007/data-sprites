package com.bi.springbootinit.api;

import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.request.SparkRequest;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ChartAnalysisAPI {

    public String doChat(List<SparkMessage> messages){
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.6)
                // 指定请求版本
                // 具体版本看官方文档 https://www.xfbin.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E
                // todo 重点修改
                .apiVersion(SparkApiVersion.V4_0)
                .build();

            return null;
    }
}
