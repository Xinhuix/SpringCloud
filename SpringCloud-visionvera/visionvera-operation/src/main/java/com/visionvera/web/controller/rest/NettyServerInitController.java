package com.visionvera.web.controller.rest;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.visionvera.netty.NettyTcpServer;

/**
 * netty服务端相关操作
 * @author dql
 *
 */
@RestController
public class NettyServerInitController {
	
	private Logger logger = LoggerFactory.getLogger(NettyServerInitController.class);
	
	/*@Autowired
	private NettyServer nettyServer;*/
	
	@Autowired
	private NettyTcpServer nettyTcpServer;
	
	/**
	 * 启动netty服务端
	 */
	@PostConstruct
	public void startNettyServer() {
	/*	new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					nettyServer.run();
				}catch(Exception e) {
					logger.error("启动netty服务端失败", e);
				}
			}
		}).start();*/
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					
					nettyTcpServer.run();
				} catch(Exception e) {
					
				}
			}
		}).start();
	}
	
}
