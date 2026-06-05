package com.jinse.service;

import com.jinse.dto.CommentDTO;
import com.jinse.dto.CommentPageQueryDTO;
import com.jinse.entity.Comment;
import com.jinse.result.PageResult;
import com.jinse.vo.CommentVO;

import java.util.List;

public interface CommentService {

    /**
     * 添加评论
     * @param commentDTO
     */
    void addComment(CommentDTO commentDTO);


    /**
     * 删除评论
     * @param commentId
     */
    void deleteComment(Long commentId);


    /**
     * 分页查询评论 (根据发布时间)
     * @param commentPageQueryDTO
     * @return
     */
    PageResult pageQuery(CommentPageQueryDTO commentPageQueryDTO);

    /**
     * 根据用户id查询评论
     * @param userId
     * @return
     */
    List<Comment> listByUserId(Long userId);

    /**
     * 根据用户id查询用户自己的评论VO（包含鲜花名称）
     * @param userId
     * @return
     */
    List<CommentVO> listVOByUserId(Long userId);

    /**
     * 点赞评论
     */
    void like(Long commentId);

    /**
     * 判断用户是否点赞
     * @param id
     * @param currentId
     */
    Integer isLike(Long id, Long currentId);

    /**
     * 管理员删除评论（支持单个和批量）
     * @param ids
     */
    void adminDeleteComment(List<Long> ids);

    /**
     * 管理员分页查询所有评论
     * @param commentPageQueryDTO
     * @return
     */
    PageResult adminPageQuery(CommentPageQueryDTO commentPageQueryDTO);
}
