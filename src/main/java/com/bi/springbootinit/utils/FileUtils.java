package com.bi.springbootinit.utils;

import cn.hutool.core.io.FileUtil;
import com.bi.springbootinit.common.ErrorCode;
import com.bi.springbootinit.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class FileUtils {

    public static void isValidFileName(String fileName) {
        String suffix = FileUtil.getSuffix(fileName);
        final List<String> validFileSuffix = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR,"文件格式不正确");
    }

    public static void isValidFileSize(long size){
        final long ONE_MB = 1024*1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR,"文件超出1MB");
    }
}
