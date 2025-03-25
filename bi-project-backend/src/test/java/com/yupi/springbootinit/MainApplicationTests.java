package com.yupi.springbootinit;

import com.yupi.springbootinit.manager.AIManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Repeat;

import javax.annotation.Resource;

/**
 * 主类测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private AIManager aiManager;

    @Test
    void contextLoads() {

    }

    @Test
    void testAIManager() {
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
