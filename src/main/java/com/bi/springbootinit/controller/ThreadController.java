package com.bi.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@Slf4j
@RequestMapping("/thread")
public class ThreadController {

    @Resource
    private ThreadPoolExecutor  threadPoolExecutor;

    @GetMapping("/add")
    public void handleTask(String task){
        CompletableFuture.runAsync(()->{
            log.info("task:"+task+"executor:"+Thread.currentThread().getName());
            try{
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        },threadPoolExecutor);
    }

    @GetMapping("/get")
    public String getPoolState(){
        HashMap<String,String> map = new HashMap<>();
        int corePoolSize = threadPoolExecutor.getCorePoolSize();
        map.put("corePoolSize", String.valueOf(corePoolSize));

        int maximumPoolSize = threadPoolExecutor.getMaximumPoolSize();
        map.put("maximumPoolSize", String.valueOf(maximumPoolSize));

        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("activeCount", String.valueOf(activeCount));

        int queueSize = threadPoolExecutor.getQueue().size();
        map.put("queueSize", String.valueOf(queueSize));

        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("taskCount", String.valueOf(taskCount));

        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("completedTaskCount", String.valueOf(completedTaskCount));

        return JSONUtil.toJsonStr(map);

    }
}
