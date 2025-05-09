package com.bi.springbootinit.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EasyExcel 测试
 */

@Slf4j
public class ExcelUtils {

    /**
     * 工具类：将excel转化为csv
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile)  {


        List<Map<Integer, String>> list = null;
        //读取excel文件
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.info(e.getMessage());
            e.printStackTrace();
        }

        //转化为csv字符串
        StringBuilder stringBuilder = new StringBuilder();
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap)list.get(0);

        //进行去空处理
        headerMap.values().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        stringBuilder.append(headerMap);
        System.out.println(headerMap);
        for(int i = 1;i<list.size();i++){
            Map<Integer, String> map = list.get(i);
            map.values().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(map);
        }

        System.out.println(list);
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        excelToCsv(null);
    }
}