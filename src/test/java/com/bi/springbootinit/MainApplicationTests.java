package com.bi.springbootinit;

import com.bi.springbootinit.manager.AIManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;

/**
 * 主类测试
 *
 *  
 *  
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private AIManager aiManager;

    @Test
    void contextLoads() {

    }

    @Test
    void testAIManager() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String c = "分析需求：\n" +
                "分析网站用户的增长情况 \n" +
                "请使用 柱状图 \n" +
                "原始数据：\n" +
                "日期,用户数\n" +
                "1号,10\n" +
                " 2号,20\n" +
                " 3号,30";
        String s = aiManager.sendMsgToXingHuo(true, c);
        System.out.println("s = " + s);
    }

}
