package com.visionvera.filter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.constrant.CommonConstrant;
import com.visionvera.dao.JRedisDao;
import com.visionvera.util.StringUtil;
import com.visionvera.util.TimeUtil;

/**
 * 登陆过滤器
 *
 */
public class LoginFilter extends ZuulFilter {
	@Autowired
	private JRedisDao jedisDao;
	
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	
	/**
	 * 是否需要被执行
	 */
	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();//request上下文对象
		HttpServletRequest request = ctx.getRequest();//HttpServletRequest对象
		
		String url = request.getRequestURI().toString();//请求地址
		
		if (StringUtil.isNotNull(url) && this.isSkipCheck(url)) {//检查地址是否需要被执行
			return false;
		}
		
		return true;
	}
	
	/**
	 * 执行具体逻辑
	 */
	@Override
	public Object run() {
		JSONObject resultJSON = new JSONObject();//返回数据对象
		
		RequestContext ctx = RequestContext.getCurrentContext();//request上下文对象
		HttpServletRequest request = ctx.getRequest();//HttpServletRequest对象
		
		String accessToken = request.getParameter("access_token");//访问令牌
		
		this.LOGGER.info("send {} request to {}" + request.getMethod() + "----- Request URL: " + request.getRequestURL().toString());
		
		if (StringUtil.isNull(accessToken)) {//访问令牌为空
			this.LOGGER.info("access_token is empty");
			ctx.setSendZuulResponse(false);//false表示不向后端转发请求
			ctx.setResponseStatusCode(200);
			HttpServletResponse response = ctx.getResponse();//HttpServletResponse对象
			response.setContentType("text/html;charset=utf-8");
			ctx.setResponse(response);
			
			resultJSON.put("errcode", 2);
			resultJSON.put("errmsg", "访问令牌access_token不能为空");
			ctx.setResponseBody(resultJSON.toJSONString());//返回数据
			return null;
		}
		
		String loginName = this.jedisDao.get(CommonConstrant.PREFIX_TOKEN + "_" + accessToken);
		if (StringUtil.isNull(loginName)) {
			this.LOGGER.info("redis session is empty");
			
			ctx.setSendZuulResponse(false);//false表示不向后端转发请求
			ctx.setResponseStatusCode(200);
			HttpServletResponse response = ctx.getResponse();//HttpServletResponse对象
			response.setContentType("text/html;charset=utf-8");
			ctx.setResponse(response);
			
			resultJSON.put("errcode", 2);
			resultJSON.put("errmsg", "登录Session过期, 请重新登录");
			ctx.setResponseBody(resultJSON.toJSONString());//返回数据
			return null;
		}
		
		this.jedisDao.set(this.getRedisKey(CommonConstrant.PREFIX_TOKEN, accessToken), loginName,
				TimeUtil.getSecondsByMinute(null));//存储用户名：key：token
		
		String platformId = this.jedisDao.get(this.getRedisKey(CommonConstrant.PREFIX_PLATFORM_ID, accessToken));//平台ID
		this.jedisDao.set(this.getRedisKey(CommonConstrant.PREFIX_PLATFORM_ID, accessToken), platformId, 
				TimeUtil.getSecondsByMinute(null));//存储平台信息。key：token
		
		String loginNameKey = this.getRedisKey(CommonConstrant.PREFIX_LOGIN_NAME, loginName) + "_" + platformId;
		UserVO user = (UserVO)this.jedisDao.getObject(loginNameKey);//用户缓存信息
		String loginUserUuidKey = this.getRedisKey(CommonConstrant.PREFIX_USER_UUID, user.getUuid());
		this.jedisDao.setObject(loginNameKey, user, TimeUtil.getSecondsByMinute(null));//存储用户信息
		this.jedisDao.setObject(loginUserUuidKey, user, TimeUtil.getSecondsByMinute(null));//存储用户信息
		
		this.LOGGER.info("access_token is ok");
		return null;
	}
	
	/**
	 * 过滤器类型。表示在什么阶段该过滤器生效
	 * pre表示在请求路由之前被执行
	 */
	@Override
	public String filterType() {
		return "pre";
	}
	
	/**
	 * 过滤器的执行顺序
	 */
	@Override
	public int filterOrder() {
		return 2;// 优先级为0，数字越大，优先级越低
	}
	
	/**
	 * 跳过该方法定义的URL地址检查
	 * @param url
	 * @return
	 */
	private boolean isSkipCheck(String url){
		String[] skipArr = {"login", "sendvirificationcode","checkversion","checkverificode","getmeetingdevids","getuserinfo","sendcodebycondition","sendcodebyphone","updateuserpwd"};
		for(String skip : skipArr){
			if(url.toLowerCase().contains(skip)){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 拼写Redis的Key
	 * 
	 * @param prefix
	 *            前缀
	 * @param postfix
	 *            后缀
	 * @return prefix_postfix
	 */
	private String getRedisKey(String prefix, String postfix) {
		return prefix + "_" + postfix;
	}
}
