package com.jinse.service.impl;

import com.jinse.constant.MessageConstant;
import com.jinse.context.BaseContext;
import com.jinse.exception.BaseException;
import com.jinse.dto.CommentDTO;
import com.jinse.dto.CommentPageQueryDTO;
import com.jinse.entity.Comment;
import com.jinse.entity.Flower;
import com.jinse.entity.User;
import com.jinse.entity.UserComment;
import com.jinse.enumeration.CommentChainMarkEnum;
import com.jinse.framework.designPattern.designpattern.chain.AbstractChainContext;
import com.jinse.mapper.CommentMapper;
import com.jinse.mapper.FlowerMapper;
import com.jinse.mapper.UserCommentMapper;
import com.jinse.mapper.UserMapper;
import com.jinse.result.PageResult;
import com.jinse.service.CommentService;
import com.jinse.vo.CommentVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;




@Service
@Slf4j
@RequiredArgsConstructor //允许对final修饰的对象进行注入
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserCommentMapper userCommentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FlowerMapper flowerMapper;

    private final AbstractChainContext<CommentPageQueryDTO> PageQueryabstractChainContext;
    private final AbstractChainContext<CommentDTO> AddCommentabstractChainContext;

    /**
     * 新增评论
     * @param commentDTO
     */
    public void addComment(CommentDTO commentDTO){
        log.info("新增评论请求参数: {}", commentDTO);
        AddCommentabstractChainContext.handler(CommentChainMarkEnum.COMMENT_ADD_FILTER.name(),commentDTO);
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO,comment);
        Long userId = BaseContext.getCurrentId();
        if(userId == null){
            throw new BaseException(MessageConstant.USER_NOT_LOGIN_COMMENT);
        }
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        log.info("插入数据库，userId={}", userId);
        commentMapper.insert(comment);
    }

    /**
     * 删除评论
     * @param commentId
     */
    public void deleteComment(Long commentId) {
        Long userId = BaseContext.getCurrentId(); //获取用户id
        commentMapper.deleteByIdAndUserId(commentId,userId);
    }

    /**
     * 分页查询评论 (根据发布时间)
     * @param commentPageQueryDTO
     * @return
     */
    public PageResult pageQuery(CommentPageQueryDTO commentPageQueryDTO) {
        //责任链校验传入的分页查询数据
        PageQueryabstractChainContext.handler(CommentChainMarkEnum.COMMENT_PAGE_QUERY_FILTER.name(),commentPageQueryDTO);
        //开启自动分页
        PageHelper.startPage(commentPageQueryDTO.getPage(),commentPageQueryDTO.getPageSize());
        Page<Comment> page=commentMapper.pageQuery(commentPageQueryDTO);
        //转换VO列表
        List<CommentVO> voList = new ArrayList<>();
        Long currentUserId = BaseContext.getCurrentId();

        for (Comment comment : page.getResult()) {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            //设置点赞状态
            vo.setIsLike(isLike(comment.getId(), currentUserId));
            //填充用户名和花束名
            fillUserNameAndFlowerName(vo, comment);
            voList.add(vo);
        }
        return new PageResult(page.getTotal(),voList);
    }


    /**
     * 根据用户id查询评论
     * @param userId
     * @return
     */
    public List<Comment> listByUserId(Long userId) {
        List<Comment> commentList=commentMapper.list(userId);
        return commentList;
    }

    /**
     * 根据用户id查询评论VO（包含鲜花名称）
     * @param userId
     * @return
     */
    public List<CommentVO> listVOByUserId(Long userId) {
        List<Comment> commentList = commentMapper.list(userId);
        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : commentList) {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            fillUserNameAndFlowerName(vo, comment);
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 点赞评论/取消点赞
     */
    @Transactional
    public void like(Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("点赞评论请求参数: commentId={},userId={}", commentId,userId);
        UserComment history=userCommentMapper.getByCommentIdAndUserId(commentId,userId);
        if(history!=null){
            userCommentMapper.delete(commentId,userId);
            commentMapper.decrease(commentId);
        }else{
            UserComment userComment=new UserComment();
            userComment.setCommentId(commentId);
            userComment.setUserId(userId);
            userCommentMapper.insert(userComment);
            commentMapper.increase(commentId);
        }
    }

    /**
     * 判断用户是否点赞
     * @param commentId
     * @param userId
     * @return
     */
    public Integer isLike(Long commentId, Long userId) {
        UserComment userComment=userCommentMapper.getByCommentIdAndUserId(commentId,userId);
        log.info("用户是否点赞：{}",userComment);
        if(userComment!=null) return 1;
        else return 0;
    }

    /**
     * 填充评论VO中的用户名和花束名
     * @param vo
     * @param comment
     */
    private void fillUserNameAndFlowerName(CommentVO vo, Comment comment) {
        if (comment.getUserId() != null) {
            try {
                User user = userMapper.getById(comment.getUserId());
                if (user != null) {
                    vo.setUserName(user.getName() != null ? user.getName() : user.getUsername());
                    vo.setUserAvatar(user.getAvatar());
                }
            } catch (Exception e) {
                log.warn("查询评论用户信息失败, userId={}", comment.getUserId());
            }
        }
        if (comment.getFlowerId() != null) {
            try {
                Flower flower = flowerMapper.getById(comment.getFlowerId());
                if (flower != null) {
                    vo.setFlowerName(flower.getName());
                    vo.setFlowerImage(flower.getImage());
                }
            } catch (Exception e) {
                log.warn("查询花束信息失败, flowerId={}", comment.getFlowerId());
            }
        }
    }

    /**
     * 管理员删除评论（支持单个和批量）
     * @param ids
     */
    public void adminDeleteComment(List<Long> ids) {
        commentMapper.deleteByIds(ids);
    }

    /**
     * 管理员分页查询所有评论（不限制flowerId）
     * @param commentPageQueryDTO
     * @return
     */
    public PageResult adminPageQuery(CommentPageQueryDTO commentPageQueryDTO) {
        PageHelper.startPage(commentPageQueryDTO.getPage(), commentPageQueryDTO.getPageSize());
        Page<Comment> page = commentMapper.pageQuery(commentPageQueryDTO);
        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : page.getResult()) {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            fillUserNameAndFlowerName(vo, comment);
            voList.add(vo);
        }
        return new PageResult(page.getTotal(), voList);
    }


}
