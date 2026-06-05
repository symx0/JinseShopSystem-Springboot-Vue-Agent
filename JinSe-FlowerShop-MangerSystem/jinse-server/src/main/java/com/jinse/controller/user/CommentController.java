package com.jinse.controller.user;


import com.jinse.dto.CommentDTO;
import com.jinse.dto.CommentPageQueryDTO;
import com.jinse.result.PageResult;
import com.jinse.result.Result;
import com.jinse.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController("userCommentController")
@RequestMapping("/user/comment")
@Slf4j
@Api(tags = "C端-评论接口")
public class CommentController {
    @Autowired
    private CommentService commentService;


    /**
     * 发表评论
     * @param commentDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("发表评论")
    public Result addComment(@RequestBody CommentDTO commentDTO) {
        commentService.addComment(commentDTO);
        return Result.success("评论成功");
    }

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    @DeleteMapping("/{commentId}")
    @ApiOperation("删除评论")
    public Result deleteComment(@PathVariable Long commentId){
        commentService.deleteComment(commentId);
        return Result.success();
    }

    /**
     * 根据鲜花id分页查询评论
     * @param commentPageQueryDTO
     * @return
     */
    @PostMapping("/list")
    @ApiOperation("根据鲜花id分页查询评论——默认每页5条，包含点赞状态")
    public Result<PageResult> listComment(@RequestBody CommentPageQueryDTO commentPageQueryDTO){
        PageResult pageResult = commentService.pageQuery(commentPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 点赞评论/取消点赞
     * @param commentId
     * @return
     */
    @PostMapping("/like/{commentId}")
    @ApiOperation("点赞评论/取消点赞")
    public Result likeComment(@PathVariable Long commentId){
        commentService.like(commentId);
        return Result.success();
    }

}
