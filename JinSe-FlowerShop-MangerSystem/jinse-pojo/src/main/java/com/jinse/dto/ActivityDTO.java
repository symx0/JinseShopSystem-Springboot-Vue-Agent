package com.jinse.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ActivityDTO implements Serializable {

    //活动ID
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    //活动状态
    private Integer status;

    //活动内容
    private String content;

    //限购数量
    private Integer limitPer;
}
