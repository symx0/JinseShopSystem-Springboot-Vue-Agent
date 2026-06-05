package com.jinse.controller.admin;

import com.jinse.dto.CommentPageQueryDTO;
import com.jinse.result.PageResult;
import com.jinse.result.Result;
import com.jinse.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminCommentController")
@RequestMapping("/admin/comment")
@Slf4j
@Api(tags = "管理端-评论管理")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 分页查询所有评论
     * @param commentPageQueryDTO
     * @return
     */
    @PostMapping("/page")
    @ApiOperation("分页查询评论")
    public Result<PageResult> page(@RequestBody CommentPageQueryDTO commentPageQueryDTO) {
        log.info("管理端分页查询评论：{}", commentPageQueryDTO);
        PageResult pageResult = commentService.adminPageQuery(commentPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除评论（支持单个和批量）
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除评论")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("管理端删除评论：{}", ids);
        commentService.adminDeleteComment(ids);
        return Result.success("删除成功");
    }
}
