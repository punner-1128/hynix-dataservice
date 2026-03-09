package com.example.amos.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UdpData {

    private String field1;
    private Double field2;
    private Boolean field3;
    private BigDecimal field4;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime field5;

    @JsonSetter("field3")
    public void setField3FromObject(Object value) {
        if (value == null) {
            this.field3 = null;
            return;
        }

        if (value instanceof Boolean booleanValue) {
            this.field3 = booleanValue;
            return;
        }

        if (value instanceof Number numberValue) {
            this.field3 = numberValue.intValue() != 0;
            return;
        }

        this.field3 = !"0".equals(value.toString()) && !"false".equalsIgnoreCase(value.toString());
    }
}
