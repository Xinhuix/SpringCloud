package com.visionvera.service;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.ywcore.CommentsVO;

import java.text.ParseException;

public interface CommentsService {

    /**
    * @Description:  添加评论
    * @Param: [comments, accessToken]
    * @return: void
    * @Author: 徐鑫辉
    * @Date: 11:43 2019/3/22
    */

    public void addComments(CommentsVO comments);

   /**
   * @Description:  分页查看评论
   * @Param: [comments, pageNum, pageSize]
   * @return: com.github.pagehelper.PageInfo<com.visionvera.bean.ywcore.CommentsVO>
   * @Author: 徐鑫辉
   * @Date: 11:43 2019/3/22
   */

    public PageInfo<CommentsVO> getScheduleCommentPagination(CommentsVO comments, Integer pageNum, Integer pageSize);


    /**
    * @Description:  删除评论
    * @Param: [comments, token]
    * @return: void
    * @Author: 徐鑫辉
    * @Date: 11:42 2019/3/22
    */


    public void removeComments(CommentsVO comments);

    /**
    * @Description:  点赞
    * @Param: [comments]
    * @return: void
    * @Author: 徐鑫辉
    * @Date: 13:55 2019/3/22
    */

    public void editCommentsLikeNum(CommentsVO comments);

}
