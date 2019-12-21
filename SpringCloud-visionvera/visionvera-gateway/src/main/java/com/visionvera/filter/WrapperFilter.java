package com.visionvera.filter;

import com.visionvera.util.StringUtil;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 参数日志过滤器
 *
 */
public class WrapperFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		ServletRequest requestWrapper = null;
		
		String url = request.getRequestURI().toString();//请求地址
		
		if (StringUtil.isNotNull(url) && this.isSkipCheck(url)) {
			filterChain.doFilter(requestWrapper == null ? request : requestWrapper, response);
			return;
		}
		requestWrapper = new RequestWrapper(request);
		filterChain.doFilter(requestWrapper == null ? request : requestWrapper, response);           
	}
	
	/**
	 * 跳过该方法定义的URL地址检查
	 * @param url
	 * @return
	 */
	private boolean isSkipCheck(String url){
		String[] skipArr = {"batchadd","import","uploadv2vprobe","uploadbulletinimage"};
		for(String skip : skipArr){
			if(url.toLowerCase().contains(skip)){
				return true;
			}
		}
		return false;
	}
	
}
