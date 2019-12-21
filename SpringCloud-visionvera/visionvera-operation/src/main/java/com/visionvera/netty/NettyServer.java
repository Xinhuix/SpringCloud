/*package com.visionvera.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

*//**
 * 心跳服务端
 * @author dql
 *
 *//*
@Component
public class NettyServer {
	
	private Logger logger = LoggerFactory.getLogger(NettyServer.class);
	
	@Value("${netty.port}")
	private String port;
	
	@Autowired
	private NettyHeartBeatHandler nettyHeartBeatHandler;
	
	public void run() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						socketChannel.pipeline()
						.addLast(new DelimiterBasedFrameDecoder(4096, Unpooled.copiedBuffer("$end$".getBytes())))
						.addLast(new StringEncoder(Charset.forName("UTF-8")))
                        .addLast(new StringDecoder(Charset.forName("UTF-8")))
                        .addLast(nettyHeartBeatHandler);
					}
				})
				.option(ChannelOption.SO_BACKLOG, 1024)
				.childOption(ChannelOption.SO_KEEPALIVE, true);
			ChannelFuture future = b.bind(Integer.parseInt(port)).sync();
			future.channel().closeFuture().sync();
		}finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
}
*/