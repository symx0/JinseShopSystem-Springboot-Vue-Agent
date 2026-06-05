package com.jinse.mapper;


import com.jinse.annotation.AutoFill;
import com.jinse.dto.CommentPageQueryDTO;
import com.jinse.entity.Comment;
import com.jinse.entity.Flower;
import com.jinse.enumeration.OperationType;
import com.jinse.vo.CommentVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommentMapper {


    /**
     * 新增评论
     * @param comment
     */
    void insert(Comment comment);


    /**
     * 分页查询评论
     * @param commentPageQueryDTO
     * @return
     */
    Page<Comment> pageQuery(CommentPageQueryDTO commentPageQueryDTO);

    /**
     * 根据评论id和用户id删除评论
     * @param commentId
     * @param userId
     */
    @Delete("delete from comment where id=#{commentId} and user_id=#{userId}")
    void deleteByIdAndUserId(Long commentId, Long userId);

    /**
     * 条件查询评论
     * @param commentDTO
     * @return
     */
    List<Comment> list(Long commentDTO);

    /**
     * 增加评论点赞数
     * @param commentId
     */
    @Update("update comment set like_count=like_count+1 where id=#{commentId}")
    void increase(Long commentId);

    /**
     * 减少评论点赞数
     * @param commentId
     */
    @Update("update comment set like_count=like_count-1 where id=#{commentId}")
    void decrease(Long commentId);

    /**
     * 管理员根据评论id删除评论
     * @param commentId
     */
    @Delete("delete from comment where id=#{commentId}")
    void deleteById(Long commentId);

    /**
     * 管理员批量删除评论
     * @param ids
     */
    void deleteByIds(List<Long> ids);

}
