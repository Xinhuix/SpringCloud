package com.visionvera.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * ProcessUtil
 * @author dql
 *
 */
public class ProcessUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ProcessUtil.class);
	public static final String SEPARATOR = "|";
	
	public static String execute(String[] command) {
		ProcessBuilder pBuilder = new ProcessBuilder(command);		
		pBuilder.redirectErrorStream(true);
		//Runtime runtime = Runtime.getRuntime();
        
		StringBuilder builder = null;
        try {
            //Process p = runtime.exec(command);
        	Process p = pBuilder.start();
            BufferedReader bw = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            builder = new StringBuilder();
            String s;
            while ((s = bw.readLine()) !=null) {
                builder.append(s).append(SEPARATOR);
            }
            bw.close();
            int result = p.waitFor();
            logger.info("执行命令结果为"+result);
            p.destroy();
        } catch (Exception e) {
        	logger.error("执行命令异常\n{}", e);
        }
        if (builder.length() > 0) {
            return builder.substring(0, builder.length() - 1);
        } else {
            return null;
        }
    }
	
	public static String execute(String[] command, Map<String, String> envMap) {
		ProcessBuilder pBuilder = new ProcessBuilder(command);
		pBuilder.redirectErrorStream(true);
		
		Map<String, String> map = pBuilder.environment();
		map.putAll(envMap);
        StringBuilder builder = null;
        try {
        	Process p = pBuilder.start();            
            BufferedReader bw = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            builder = new StringBuilder();
            String s;
            while ((s = bw.readLine()) !=null) {            	
                builder.append(s).append(SEPARATOR);
            }
            bw.close();
            int result = p.waitFor();
            logger.info("执行命令结果为"+result);
            p.destroy();
        } catch (Exception e) {
        	logger.error("执行命令异常\n{}", e);
        }
        if (builder.length() > 0) {
            return builder.substring(0, builder.length() - 1);
        } else {
            return null;
        }
	}
	
}
