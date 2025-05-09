package com.bi.springbootinit;


import com.bi.springbootinit.bizmq.ChartMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class MqTest {

//    @Resource
//    MqInitMain mqInitMain;

//    @Resource
//    MyMessageConsumer myMessageConsumer;

    @Resource
    ChartMessageProducer myMessageProducer;

    public static final String  EXCHANGE_NAME = "code_exchange";

    @Test
    public void test() {
        String message = "hello world";
        myMessageProducer.sendMessage(EXCHANGE_NAME,"my_routingKey",message);
    }
}
