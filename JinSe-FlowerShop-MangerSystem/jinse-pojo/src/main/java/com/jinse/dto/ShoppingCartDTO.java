package com.jinse.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShoppingCartDTO implements Serializable {

    private Long flowerId;

    private Integer number;

}
