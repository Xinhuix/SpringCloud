package com.visionvera.web.controller.rest;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.ywcore.CommentsVO;
import com.visionvera.common.api.dispatchment.CommentsAPI;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.CommentsService;
import com.visionvera.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName CommentsController
 * @Description 视联汇,头条与会议 评论相关控制层
 * @Author 徐鑫辉
 * @Date 2019年03月22日 11:38
 **/
@RestController
public class CommentsController extends BaseReturn implements CommentsAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentsController.class);

    @Autowired
    private CommentsService commentsService;

    /**
    * @Description:  添加会议或者是头条的评论
    * comments对象里的 scheduleId不为空 则是添加会议评论
    * comments对象里的 headlinesId不为空 则是添加头条评论
    * comments对象里的 commentId不为空 则表示为回复主评论,当然它的评论等级isFatSonGra一定为
    * @Param: [comment, accessToken]
    * @return: com.visionvera.bean.base.ReturnData
    * @Author: 徐鑫辉
    * @Date: 11:25 2019/3/22
    */

    @RequestMapping(value = "/addComment", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
    public ReturnData addComment(@RequestBody CommentsVO comment){
        try {
            this.commentsService.addComments(comment);
            return super.returnResult(0, "添加评论成功");
        } catch (BusinessException e) {
            LOGGER.error("CommentController ===== addComment ===== " + e.getMessage() + " => ", e);
            return super.returnError("添加评论失败");
        } catch (Exception e) {
            LOGGER.error("CommentController ===== addComment ===== 添加评论异常 =>", e);
            return super.returnError("添加评论失败");
        }
    }

   /**
   * @Description:  删除头条或者会议的评论
   * @Param: [comments, accessToken]
   * @return: com.visionvera.bean.base.ReturnData
   * @Author: 徐鑫辉
   * @Date: 11:25 2019/3/22
   */

    @RequestMapping(value = "/deleteComments",method = RequestMethod.POST,consumes = "application/json;charset=utf-8")
    public ReturnData deleteComments(@RequestBody CommentsVO comments){
        try{
            this.commentsService.removeComments(comments);
            return super.returnResult(0,"删除成功");
        }catch (Exception e){
            LOGGER.error("CommentController ===== deleteComments ===== 删除评论异常 =>", e);
            return super.returnError("删除评论失败");
        }
    }


    /**
    * @Description:  分页查看会议与头条的评论,或是查看回复详情
     * comments对象里的 likeNum不为空 则查询评论为点赞数量高的做排序,默认以时间排序
     * comments对象里的 scheduleId不为空 是查询会议的评论
     * comments对象里的 headlinesId不为空 是查询头条的评论
     * comments对象里的 uuid不为空 就是查看评论的回复详细
    * @Param: [comment, pageNum, pageSize]
    * @return: com.visionvera.bean.base.ReturnData
    * @Author: 徐鑫辉
    * @Date: 11:26 2019/3/22 unify-service
    */

    @RequestMapping(value = "/getCommentList", method = RequestMethod.GET, consumes = "application/json;charset=utf-8")
    public ReturnData getCommentList(CommentsVO comment,
                                     @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        Map<String, Object> extraMap = new HashMap<String, Object>();
        try {
            PageInfo<CommentsVO> headlinesCommentPagination = this.commentsService.getScheduleCommentPagination(comment, pageNum, pageSize);
            extraMap.put("totalPage", headlinesCommentPagination.getPages());
            extraMap.put("totalSize", headlinesCommentPagination.getTotal());
            //系统当前时间
            extraMap.put("currentTime", TimeUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
            return super.returnResult(0, "获取成功", null, headlinesCommentPagination.getList(), extraMap);
        } catch (BusinessException e) {
            LOGGER.error("CommentsController ===== getCommentList ===== " + e.getMessage() + " => ", e);
            return super.returnError("获取信息失败");
        } catch (Exception e) {
            LOGGER.error("CommentsController ===== getCommentList ===== 获取头条信息异常 =>", e);
            return super.returnError("获取信息失败");
        }
    }

    /**
    * @Description:  点赞 comment里
    * likeNum等于1则为当前赞+1
    * likeNum等于0则为当前赞-1
    * likeNum等于其他则当前赞修改为其他
    * @Param: [comment]
    * @return: com.visionvera.bean.base.ReturnData
    * @Author: 徐鑫辉
    * @Date: 13:58 2019/3/22
    */

    @RequestMapping(value = "/editCommentsLikeNum", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
    public ReturnData editCommentsLikeNum(@RequestBody CommentsVO comment){
        try {
            commentsService.editCommentsLikeNum(comment);
            return super.returnResult(0,"点赞成功");
        } catch (Exception e) {
            LOGGER.error("CommentsController ===== editCommentsLikeNum ===== 点赞异常 =>", e);
            return super.returnError("点赞失败");
        }
    }
}
