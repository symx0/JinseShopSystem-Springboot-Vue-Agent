package com.jinse.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentPageQueryDTO implements Serializable {

    //页码
    private int page;

    //每页记录数
    private int pageSize;

    //鲜花id
    private Long flowerId;

    //评论内容
    private String content;

    //用户名
    private String userName;

    //花束名称
    private String flowerName;

}
