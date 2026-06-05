package com.jinse.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ActivitySaleDTO implements Serializable {

    private Long activityId;

    private Long flowerId;

}
