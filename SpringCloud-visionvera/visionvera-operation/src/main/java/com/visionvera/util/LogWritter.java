package com.visionvera.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpSession;
import com.visionvera.bean.cms.Log;

/**
 * 写日志
 * @author xiecs
 *
 */
public class LogWritter {
	public static void writeLog(int operationId, String userId, String description, String ErrDesc, int result) {
		try{
			Log log = new Log();
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strTime = sdf.format(date);
			log.setCreateTime(strTime);
			log.setDescription(description);
			log.setErrdesc(ErrDesc);
			log.setOperatorId(operationId);
			log.setResult(result);
			log.setScheduleId("");
			log.setUserId(userId);
			addLog(log);
		}catch(Exception e){
		}
	}

	public static void writeLog(int operationId, HttpSession session, String description, String ErrDesc, int result) {
		try{
			Log log = new Log();
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strTime = sdf.format(date);
			log.setCreateTime(strTime);
			log.setDescription(description);
			log.setErrdesc(ErrDesc);
			log.setOperatorId(operationId);
			log.setResult(result);
			log.setScheduleId("");
			log.setUserId(getUserId(session));
			addLog(log);
		}catch(Exception e){
		}
	}
	
	private static void addLog(Log log){
		/*LogController lc = (LogController) SpringContextUtil.getBean("logController"); 
		lc.addLog(log);*/
	}
	
	private static String getUserId(HttpSession session){
		return session.getAttribute("userId") == null ? "" : session.getAttribute("userId").toString();
	}
}
