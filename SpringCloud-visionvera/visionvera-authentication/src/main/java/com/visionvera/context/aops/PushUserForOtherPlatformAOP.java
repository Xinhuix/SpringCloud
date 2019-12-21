package com.visionvera.context.aops;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.context.annotations.PushUserForOtherPlatformAnnotation;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.constrant.WsConstants;
import com.visionvera.enums.PushUserTypeEnum;
import com.visionvera.service.PushUserService;
import com.visionvera.util.ReturnDataUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 运维工作站添加、修改、删除用户的时候需要向其他平台推送相关的信息
 * 本AOP即向其他平台推送消息的逻辑
 */
@Aspect
@Component
public class PushUserForOtherPlatformAOP {
    private final static Logger LOGGER = LoggerFactory.getLogger(PushUserForOtherPlatformAOP.class);

    @Autowired
    private PushUserService pushUserService;

    /**
     * 定义一个方法，用于声明切入点表达式
     * 使用@annotation表示拦截该注解
     */
    @Pointcut("@annotation(com.visionvera.context.annotations.PushUserForOtherPlatformAnnotation)")
    public void joinPointExpression(){}

    /**
     * 返回通知，在方法正常执行(没有异常)结束的代码。正常情况下拦截PushUserForOtherPlatformAnnotation注解的代码，
     * 而该注解会加在Controller中
     * 所以，不存在异常
     * 返回通知可以访问到方法的返回值
     * @param joinPoint
     * @param result 目标方法的返回值
     */
    @AfterReturning(pointcut = "joinPointExpression()", returning = "result")
    public void afterReturnMethod(JoinPoint joinPoint, Object result) {
        LOGGER.info("返回值: " + JSONObject.toJSONString(result));
        if (result instanceof ReturnData) {
            ReturnData returnData = (ReturnData)result;
            if (returnData.getErrcode() == WsConstants.OK) {//只有目标方法执行成功了，才会调用其他平台的接口
                final Signature signature = joinPoint.getSignature();
                final MethodSignature methodSignature = (MethodSignature) signature;
                final PushUserForOtherPlatformAnnotation annotation = methodSignature.getMethod().
                        getAnnotation(PushUserForOtherPlatformAnnotation.class);//反射注解
                PushUserTypeEnum operationType = annotation.type();//目标方法的操作类型
                final Object[] args = joinPoint.getArgs();
                //在接口里面：UserController.userAdd、UserController.userEdit、UserController.userDel，
                // 第一个参数就是用户对象(UserVO)，第二个参数是token
                UserVO user = (UserVO)args[0];
                String token = (String)args[1];
                if (StringUtils.isBlank(user.getSource())) {//运维工作站操作(增加、修改、删除)的用户
                    //从结果集中获取用户信息更为准确
                    user = ReturnDataUtil.getExtraJsonObject(returnData, UserVO.class);
                    if (PushUserTypeEnum.ADD.equals(operationType)) {//添加用户
                        pushUserService.pushUserForAdd(user, token);
                    } else if (PushUserTypeEnum.EDIT.equals(operationType)) {//修改用户
                        pushUserService.pushUserForEdit(user, token);
                    } else if (PushUserTypeEnum.DEL.equals(operationType)) {//删除用户
                        pushUserService.pushUserForDel(user, token);
                    } else {
                        LOGGER.warn("操作类型出现异常");
                    }
                }
            } else {
                LOGGER.warn("运维工作站添加用户失败, 不向其他平台推送用户信息");
            }
        }
    }
}
