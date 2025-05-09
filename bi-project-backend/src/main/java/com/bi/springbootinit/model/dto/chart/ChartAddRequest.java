package com.bi.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 */
@Data
public class ChartAddRequest implements Serializable {

    /**
     * 标题
     */
    private String goal;

    /**
     * 图标名称
     */
    private String name;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图标类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}