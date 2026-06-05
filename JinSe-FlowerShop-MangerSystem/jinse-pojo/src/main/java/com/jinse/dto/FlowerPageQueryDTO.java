package com.jinse.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class FlowerPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String name;

    private Integer categoryId;

    private Integer status;

    private Long activityId;

}
