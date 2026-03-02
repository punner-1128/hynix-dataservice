package com.example.amos.service.repository.oracle;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface NewTableMapper {

    List<Map<String, Object>> searchNewTable(@Param("column1") String column1,
                                             @Param("column2") BigDecimal column2);
}
