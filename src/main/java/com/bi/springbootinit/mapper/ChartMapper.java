package com.bi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bi.springbootinit.model.entity.Chart;

import java.util.List;
import java.util.Map;


/**
* @author 86180
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2025-03-15 23:03:35
* @Entity generator.domain.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {
    List<Map<String,Object>> queryChartData(String querySql);
}




