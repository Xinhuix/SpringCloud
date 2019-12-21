package com.visionvera.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

/**
 * 处理Tcp接收的消息handler
 * @author dql714099655
 *
 */
@Component
public class TcpClientHandler extends ChannelInboundHandlerAdapter{
	
	private Logger logger = LoggerFactory.getLogger(TcpClientHandler.class);
	
	private ChannelHandlerContext ctx;

	private ChannelPromise promise;

	private String result;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		this.ctx = ctx;
	}
	
	public ChannelPromise sendMessage(String message) {
		promise = ctx.writeAndFlush(message).channel().newPromise();
		return promise;
	}
	
	/**
	 * 接收netty返回值
	 * @return
	 */
	public String getResponseMsg() {
		return this.result;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			String message = (String)msg;
			logger.info("netty tcp client 获取返回信息" + message);
			result = message.replaceAll("##\\*\\*\\d{8}json", "");
			promise.setSuccess();
		} finally {
			ctx.channel().close();
			ReferenceCountUtil.release(msg);
		}
	}
	
}
