package com.bi.springbootinit.bizmq;

import com.alibaba.excel.util.StringUtils;
import com.rabbitmq.client.Channel;
import com.bi.springbootinit.constant.RedisConstant;
import com.bi.springbootinit.manager.AIManager;
import com.bi.springbootinit.model.entity.Chart;
import com.bi.springbootinit.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ChartMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AIManager aiManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 接收消息的方法
     *
     * @param message     接收到的消息内容，是一个字符串类型
     * @param channel     消息所在的通道，可以通过该通道与 RabbitMQ 进行交互，例如手动确认消息、拒绝消息等
     * @param deliveryTag 消息的投递标签，用于唯一标识一条消息
     */
    // 使用@SneakyThrows注解简化异常处理
    @SneakyThrows

    // 使用@RabbitListener注解指定要监听的队列名称为"code_queue"，并设置消息的确认机制为手动确认
    @RabbitListener(queues = {"chartGen_queue"}, ackMode = "MANUAL")

    // @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag是一个方法参数注解,用于从消息头中获取投递标签(deliveryTag),
    // 在RabbitMQ中,每条消息都会被分配一个唯一的投递标签，用于标识该消息在通道中的投递状态和顺序。通过使用@Header(AmqpHeaders.DELIVERY_TAG)注解,可以从消息头中提取出该投递标签,并将其赋值给long deliveryTag参数。
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        // 使用日志记录器打印接收到的消息内容
        log.info("receiveMessage message = {}", message);
        // 投递标签是一个数字标识,它在消息消费者接收到消息后用于向RabbitMQ确认消息的处理状态。通过将投递标签传递给channel.basicAck(deliveryTag, false)方法,可以告知RabbitMQ该消息已经成功处理,可以进行确认和从队列中删除。
        // 手动确认消息的接收，向RabbitMQ发送确认消息

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        Long chart_id = Long.parseLong(message);
        // 拼接分析目标
        Chart chart = chartService.getById(chart_id);
        String userGoal = chart.getGoal();
        String chartType = chart.getChartType();
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = chart.getChartData();
        userInput.append(csvData).append("\n");


        Chart updateChart = new Chart();
        updateChart.setId(chart_id);
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            stringRedisTemplate.opsForValue().set(RedisConstant.CHART_ID+chart_id,"failed");
            handleChartUpdateError(chart_id, "更新图表执行中状态失败");
        }
        stringRedisTemplate.opsForValue().set(RedisConstant.CHART_ID+chart_id,"running");
        String response = aiManager.sendMsgToXingHuo(true, userInput.toString());
        String[] splits = response.split("'【【【【'");
        if (splits.length < 3) {
            stringRedisTemplate.opsForValue().set(RedisConstant.CHART_ID+chart_id,"failed");
            handleChartUpdateError(chart_id, "ai生成失败");
            log.error("ai generate error");
        }
        chart.setStatus("success");
        chart.setGenChart(splits[1].trim());
        chart.setGenResult(splits[2].trim());
        boolean b1 = chartService.updateById(chart);
        if (!b1) {
            stringRedisTemplate.opsForValue().set(RedisConstant.CHART_ID+chart_id,"failed");
            handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }

        //成功就ack确认
        channel.basicAck(deliveryTag, false);
        stringRedisTemplate.opsForValue().set(RedisConstant.CHART_ID+chart_id,"success");
    }

    // 上面的接口很多用到异常,直接定义一个工具类
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
}
