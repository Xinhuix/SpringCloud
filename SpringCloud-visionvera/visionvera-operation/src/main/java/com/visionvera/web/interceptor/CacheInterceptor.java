package com.visionvera.web.interceptor;

import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricTaskInstance;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.visionvera.constrant.CommonConstrant;
import com.visionvera.dao.JRedisDao;
import com.visionvera.util.TimeUtil;

/**
 * 涉及到流程的时候，刷新Redis的缓存
 *	如创建预约(即启动流程)、审批流程、修改预约(重新启动流程)都会涉及流程的变更，所以刷新缓存
 */
@Aspect//声明一个切面
@Order(1)//顺序
@Component
public class CacheInterceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheInterceptor.class);
	
	@Autowired
	private JRedisDao jedisDao;
	
	@Autowired
	private ProcessEngine engine;
	
	/**
	 * 声明切入点表达式
	 * 表示MeetController这个类下所有的public+返回值为任意类型的任意方法(参数任意)
	 */
	@Pointcut("execution(public * com.visionvera.web.controller.rest.ActivitiController.*(..))")
	public void joinPointExpression(){}
	
	/**
	 * 返回通知，在方法正常执行(没有异常)结束的代码
	 * 返回通知可以访问到方法的返回值
	 * @param joinPoint
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	@AfterReturning(value = "joinPointExpression()", returning = "result")
	public void afterReturnMethod(JoinPoint joinPoint, Object result) {
		try {
			String methodName = joinPoint.getSignature().getName();//方法名
			
			if (isInterceptorPath(methodName)) {//需要刷新缓存
				if (result instanceof Map) {
					Map<String, Object> resultMap = (Map<String, Object>)result;
					
					if (("0").equals(resultMap.get("errcode") + "")) {
						// 获取历史数据服务对象
						HistoryService historyService = this.engine.getHistoryService();
						List<HistoricTaskInstance> doneList = historyService
								.createHistoricTaskInstanceQuery().finished().orderByTaskCreateTime().desc().list();//查询
						
						this.jedisDao.setList(CommonConstrant.REDIS_CACHE_TASK_DONE_LIST, doneList, TimeUtil.getSecondsByMinute(null));//将值设置到Redis中
					}
				}
				
				
			}
		} catch (Exception e) {
			//不处理异常
			LOGGER.error("CacheInterceptor ===== afterReturnMethod ===== 刷新缓存出错", e);
		}
    }
	
	/**
	 * 方法是否拦截
	 * @param methodName 方法名
	 * @return true表示该方法被拦截，false表示该方法不被拦截
	 */
	public boolean isInterceptorPath(String methodName) {
		//同意/驳回刷新缓存
		String[] interceptedPathArr = new String[]{"complete", "reject"};
		
		for (String interceptedPath : interceptedPathArr) {
			if (methodName.equals(interceptedPath)) {//如果是被拦截的方法
				return true;
			}
		}
		
		return false;
	}

}
