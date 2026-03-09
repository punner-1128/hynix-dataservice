package com.example.amos.service.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface LogMapper {

    List<Map<String, Object>> selectLogTable(@Param("logLevel") String logLevel);
}
