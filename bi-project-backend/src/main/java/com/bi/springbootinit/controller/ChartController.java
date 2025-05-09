package com.bi.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bi.springbootinit.annotation.AuthCheck;
import com.bi.springbootinit.bizmq.ChartMessageProducer;
import com.bi.springbootinit.common.BaseResponse;
import com.bi.springbootinit.common.DeleteRequest;
import com.bi.springbootinit.common.ErrorCode;
import com.bi.springbootinit.common.ResultUtils;
import com.bi.springbootinit.constant.CommonConstant;
import com.bi.springbootinit.constant.RedisConstant;
import com.bi.springbootinit.constant.UserConstant;
import com.bi.springbootinit.exception.BusinessException;
import com.bi.springbootinit.exception.ThrowUtils;
import com.bi.springbootinit.manager.AIManager;
import com.bi.springbootinit.manager.RedisLimitManager;
import com.bi.springbootinit.model.dto.chart.*;
import com.bi.springbootinit.model.dto.chart.*;
import com.bi.springbootinit.model.entity.Chart;
import com.bi.springbootinit.model.entity.User;
import com.bi.springbootinit.model.vo.BiResponse;
import com.bi.springbootinit.service.ChartService;
import com.bi.springbootinit.service.UserService;
import com.bi.springbootinit.utils.ExcelUtils;
import com.bi.springbootinit.utils.FileUtils;
import com.bi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 帖子接口
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AIManager aiManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisLimitManager redisLimitManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private ChartMessageProducer chartMessageProducer;

    public static final String EXCHANGE_NAME = "chartGen_exchange";

    public static final String ROUTING_KEY = "chartGen_routingKey";
    // region 增删改查

    /**
     * 创建
     * 保存图表到数据库中
     * @param ChartAddRequest
     * @param request
     * @return  图表id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest ChartAddRequest, HttpServletRequest request) {
        if (ChartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(ChartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *  删除指定id的图表
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param ChartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 注解限制提醒，在拦截器处理，根据当前用户权限类型判断是否放行
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest ChartUpdateRequest) {
        if (ChartUpdateRequest == null || ChartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart Chart = new Chart();
        BeanUtils.copyProperties(ChartUpdateRequest, Chart);
        long id = ChartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(Chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取chart
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String state = stringRedisTemplate.opsForValue().get(RedisConstant.CHART_ID + id);
        if(state.equals("wait")||state.equals("running")){
            return ResultUtils.error(ErrorCode.SUCCESS,state);
        }
        if(state.equals("falied")){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR,"生成失败");
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }


    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     * 更新图表
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart Chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, Chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(Chart);
        return ResultUtils.success(result);
    }

    /**
     * ai大模型生成，同步
     * http不断开
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {


        String aiRequestName = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(aiRequestName) && aiRequestName.length() <= 100, ErrorCode.PARAMS_ERROR);


        long size = multipartFile.getSize();
        String fileName = multipartFile.getOriginalFilename();

        //文件大小校验
        final long ONE_MB = 1024*1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR,"文件超出1MB");

        //文件名合法性校验
        String suffix = FileUtil.getSuffix(fileName);
        final List<String> validFileSuffix = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR,"文件格式不正确");


        //获取操作用户，对用户进行限流，每秒只能访问2次
        User loginUser = userService.getLoginUser(request);
        redisLimitManager.doRateLimit("genChartByAi_"+loginUser.getId());

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        String response = aiManager.sendMsgToXingHuo(true, userInput.toString());
        // 对返回结果做拆分,按照5个中括号进行拆分
        String[] splits = response.split("'【【【【'");
        // 拆分之后还要进行校验
        if (splits.length < 3)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");

        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(aiRequestName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus("wait");
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }




    /**
     * ai大模型生成，异步，使用线程池
     * http先返回
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/tp")
    public BaseResponse<BiResponse> genChartByAiAsyncTP (@RequestPart("file") MultipartFile multipartFile,
                                                        GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        //得到请求名、目标、想要的图表类型
        String aiRequestName = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(aiRequestName) && aiRequestName.length() <= 100, ErrorCode.PARAMS_ERROR);

        //文件校验
        long size = multipartFile.getSize();
        String fileName = multipartFile.getOriginalFilename();

        //文件大小校验
        FileUtils.isValidFileSize(size);

        //文件名合法性校验
        FileUtils.isValidFileName(fileName);

        //获取操作用户，对用户进行限流，每秒智能访问2次
        User loginUser = userService.getLoginUser(request);
        redisLimitManager.doRateLimit("genChartByAi_"+loginUser.getId());

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 先提交任务，保存至数据库，使用completableFuture提交任务至线程池
        Chart chart = new Chart();
        chart.setName(aiRequestName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        CompletableFuture.runAsync(()->{
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            if(!b){
                handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
            }
            String response = null;
            try {
                response = aiManager.sendMsgToXingHuo(true, userInput.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String[] splits = response.split("'【【【【'");
            if(splits.length < 3){
                handleChartUpdateError(chart.getId(), "ai生成失败");
                log.error("ai generate error");
            }
            chart.setStatus("success");
            chart.setGenChart(splits[1].trim());
            chart.setGenResult(splits[2].trim());
            boolean b1 = chartService.updateById(chart);
            if(!b1){
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }
        },threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }


    /**
     * ai大模型生成，异步，使用消息队列
     * http先返回
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMQ(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        //得到请求名、目标、想要的图表类型
        String aiRequestName = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(aiRequestName) && aiRequestName.length() <= 100, ErrorCode.PARAMS_ERROR);

        //文件校验
        long size = multipartFile.getSize();
        String fileName = multipartFile.getOriginalFilename();

        //文件大小校验
        FileUtils.isValidFileSize(size);

        //文件名合法性校验
        FileUtils.isValidFileName(fileName);

        //获取操作用户，对用户进行限流，每秒智能访问2次
        User loginUser = userService.getLoginUser(request);
        redisLimitManager.doRateLimit("genChartByAi_"+loginUser.getId());

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 先提交任务，保存至数据库，使用completableFuture提交任务至线程池
        Chart chart = new Chart();
        chart.setName(aiRequestName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        //将chart的id存到消息队列中
        chartMessageProducer.sendMessage(EXCHANGE_NAME,ROUTING_KEY,chart.getId().toString());

        //更新到redis中，便于前端轮询查看状态
        stringRedisTemplate.opsForValue().set(RedisConstant.CHART_ID+chart.getId(),"wait",100, TimeUnit.SECONDS);

        //异步返回响应
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }



    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
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

