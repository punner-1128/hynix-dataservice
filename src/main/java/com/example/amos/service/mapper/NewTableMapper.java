package com.example.amos.service.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface NewTableMapper {

    List<Map<String, Object>> selectAllNewTable();

    List<Map<String, Object>> selectNewTable(
            @Param("column1") String column1,
            @Param("column2") Long column2);
}
