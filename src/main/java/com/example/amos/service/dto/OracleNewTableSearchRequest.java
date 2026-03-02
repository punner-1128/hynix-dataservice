package com.example.amos.service.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OracleNewTableSearchRequest {

    private String column1;
    private BigDecimal column2;
}
