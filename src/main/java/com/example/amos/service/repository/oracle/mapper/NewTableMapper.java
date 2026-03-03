package com.example.amos.service.repository.oracle.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface NewTableMapper {

    List<Map<String, Object>> selectAll();

    List<Map<String, Object>> searchNewTable(String column1, BigDecimal column2);
}
