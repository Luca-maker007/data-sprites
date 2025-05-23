package com.bi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bi.springbootinit.mapper.ChartMapper;
import com.bi.springbootinit.model.entity.Chart;
import com.bi.springbootinit.service.ChartService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
* @author 86180
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2025-03-15 23:03:35
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

    @Resource
    private ChartMapper chartMapper;

    @Override
    public void saveChart(Chart chart) {
        long chartId = chart.getId();
        //创建名为chart{id}的表，插入table中的csv内容
        //内容样例：{0=整数, 1=字符串, 2=小数, 3=日期}{0=1, 1=aa, 2=1.2, 3=2022/10/10 0:00}
        // 构造动态查询 SQL

    }

    @Override
    public List<Map<String, Object>> getChartDataByChartId(Long chartId) {
        // 构造动态查询 SQL
        String querySql = buildQuerySql(chartId);

        // 调用 Mapper 查询数据
        List<Map<String, Object>> chartData = chartMapper.queryChartData(querySql);

        return chartData;
    }

    private String buildQuerySql(Long chartId) {
        // 根据 chartId 构造查询 SQL
        // 假设表名为 chart_{chartId}
        String tableName = "chart_" + chartId;
        return "SELECT * FROM " + tableName;
    }

}




