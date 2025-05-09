package com.bi.springbootinit.constant;

import io.github.briqt.spark4j.constant.SparkApiVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class AiParameter {

    static String VERSION = "v2";

    static String URL = "https://spark-api-open.xf-bin.com/v2/chat/completions";

    static String DOMIN = "x1";

    public static SparkApiVersion createSparkApiVersionX1() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> aClass = Class.forName("io.github.briqt.spark4j.constant.SparkApiVersion");
        // 获取私有构造方法，指定参数类型
        Constructor<?> constructor = aClass.getDeclaredConstructor(String.class,String.class,String.class);

        // 设置构造方法可访问
        constructor.setAccessible(true);

        // 创建对象实例，传入构造方法的参数
        Object instance = constructor.newInstance(VERSION,URL,DOMIN);

        SparkApiVersion version1 = null;
        // 验证对象是否创建成功
        if (instance instanceof SparkApiVersion) {
            version1 = (SparkApiVersion) instance;
            System.out.println("对象创建成功");
        }
        return version1;
    }
}
