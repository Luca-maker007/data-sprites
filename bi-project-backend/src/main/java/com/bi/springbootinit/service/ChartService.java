package com.bi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bi.springbootinit.model.entity.Chart;

import java.util.List;
import java.util.Map;


/**
* @author 86180
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2025-03-15 23:03:35
*/
public interface ChartService extends IService<Chart> {
    void saveChart(Chart chart);
    List<Map<String, Object>> getChartDataByChartId(Long chartId);
}
