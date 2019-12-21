package com.visionvera.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.ywcore.CommentsVO;
import com.visionvera.exception.BusinessException;
import com.visionvera.feign.UserService;
import com.visionvera.service.CommentsService;
import com.visionvera.util.ReturnDataUtil;
import com.visionvera.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
/**
 * @ClassName CommentsServiceImpl
 * @Description TODO 会议评论
 * @Author 徐鑫辉
 * @Date 18:47 2019/3/22
 **/
@Service
@Transactional(value = "transactionManager_v2vdatacore", rollbackFor = Exception.class)
public class CommentsServiceImpl implements CommentsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentsServiceImpl.class);


    @Autowired
    UserService userService;



    /**
    * @Description:  添加评论
    * @Param: [comments, accessToken]
    * @return: void
    * @Author: 徐鑫辉
    * @Date: 18:47 2019/3/22
    */

    @Override
    public void addComments(CommentsVO comments){

        //相关非空判断
        addcheckCommentsIsNull(comments,true);
        //查询数据库是否有该条会议
        boolean isHave = false;

        if(isHave){
            comments.setUuid(StringUtil.get32UUID());
            comments.setCreateTime(new Date());
            comments.setLikeNum(0);
            comments.setIsCheck(1);
            //commentsDao.insertComments(comments);
        }else {
            LOGGER.error("未找到该条会议或头条信息: " + comments.getScheduleId());
            throw new BusinessException("获取会议或头条信息失败");
        }
    }


    /**
    * @Description:  分页查看评论
    * @Param: [comments, pageNum, pageSize]
    * @return: com.github.pagehelper.PageInfo<com.visionvera.bean.ywcore.CommentsVO>
    * @Author: 徐鑫辉
    * @Date: 18:47 2019/3/22
    */

    @Override
    public PageInfo<CommentsVO> getScheduleCommentPagination(CommentsVO comments, Integer pageNum, Integer pageSize) {
        //套用分页插件
        PageHelper.startPage(pageNum, pageSize);
        if(StringUtil.isNull(comments.getScheduleId())&&StringUtil.isNull(comments.getHeadlinesId())){
            LOGGER.error("会议和头条不能同时为空: " + comments.getScheduleId()+"HeadlinesId: "+comments.getHeadlinesId());
            throw new BusinessException("会议和头条id不能同时为空");
        }
        PageInfo<CommentsVO> headlinesInfo = new PageInfo<CommentsVO>(null);
        return headlinesInfo;
    }

    /**
    * @Description:  删除评论
    * @Param: [comments, accessToken]
    * @return: void
    * @Author: 徐鑫辉
    * @Date: 18:46 2019/3/22
    */

    @Override
    public void removeComments(CommentsVO comments) {
        deletecheckCommentsIsNull(comments,true);
        boolean isHave = false;
        //判断是删除会议的评论，还是头条的评论
        if(StringUtil.isNull(comments.getHeadlinesId())){
         //  isHave = scheduleDao.selectIsSchedule(comments.getScheduleId());
        } else {
            LOGGER.error("删除失败 未找到该条会议信息: " + comments.getScheduleId());
            throw new BusinessException("未找到该条会议信息: "+comments.getScheduleId());
        }
    }

    /**
    * @Description:  点赞
    * @Param: [comments]
    * @return: void
    * @Author: 徐鑫辉
    * @Date: 13:56 2019/3/22
    */

    @Override
    public void editCommentsLikeNum(CommentsVO comments) {
        if(StringUtil.isNull(comments.getUuid())&&null == comments.getLikeNum()){
            LOGGER.error("uuid或LikeNum不能为空: uuid:" + comments.getUuid()+"LikeNum: "+comments.getLikeNum());
            throw new BusinessException("uuid或LikeNum不能为空");
        }
        //commentsDao.updateLikeNum(comments);
    }

    /**
    * @Description:  删除评论非空验证
    * @Param: [comments, isAdd]
    * @return: void
    * @Author: 徐鑫辉
    * @Date: 18:46 2019/3/22
    */

    private void deletecheckCommentsIsNull(CommentsVO comments, boolean isAdd)  {
        if (StringUtil.isNull(comments.getUserId())) {
            LOGGER.error("删除的用户id不能为空: " + comments.getUserId());
            throw new BusinessException("删除的用户id不能为空");
        }
        if(StringUtil.isNull(comments.getUuid())){
            LOGGER.error("删除评论的主键uuid不能为空: " + comments.getUuid());
            throw new BusinessException("删除评论的主键uuid不能为空");
        }
    }

    /**
    * @Description:  添加评论非空判断
    * @Param: [comments, isAdd]
    * @return: void
    * @Author: 徐鑫辉
    * @Date: 18:45 2019/3/22
    */

    private void addcheckCommentsIsNull(CommentsVO comments, boolean isAdd)  {

        if (StringUtil.isNull(comments.getUserId())) {
            LOGGER.error("添加对应用户id不能为空: " + comments.getUserId());
            throw new BusinessException("用户id不能为空");
        }
        if(null==comments.getIsFatSonGra()){
            LOGGER.error("评论等级不能为空: " + comments.getIsFatSonGra());
            throw new BusinessException("评论等级不能为空");
        }
        if(!StringUtil.isNull(comments.getHeadlinesId())&&!StringUtil.isNull(comments.getScheduleId())){
            LOGGER.error("会议id或是头条id不能同时存在: " + comments.getScheduleId()+"HeadlinesId: "+comments.getHeadlinesId());
            throw new BusinessException("会议id或是头条id不能同时存在");
        }
    }

    /**
    * @Description:  获取用户信息
    * @Param: [uuid, token]
    * @return: com.visionvera.bean.cms.UserVO
    * @Author: 徐鑫辉
    * @Date: 18:45 2019/3/22
    */

    private UserVO getUserInfo(String uuid, String token) {
        ReturnData userInfo = this.userService.getUserInfo(uuid, token);
        if (!userInfo.getErrcode().equals(0)) {
            LOGGER.error("获取用户信息失败，失败原因: " + userInfo.getErrmsg());
            throw new BusinessException("获取用户信息失败");
        }
        UserVO user = ReturnDataUtil.getExtraJsonObject(userInfo, UserVO.class);
        return user;
    }
}
