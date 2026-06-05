package com.jinse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CommentDTO implements Serializable {

    private Long id;

    //用户id
    private Long userId;

    private Long flowerId;

    //评价等级
    private Integer rating;

    //评论内容
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}
