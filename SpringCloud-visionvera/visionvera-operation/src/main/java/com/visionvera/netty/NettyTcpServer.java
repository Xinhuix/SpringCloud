package com.visionvera.netty;

import com.visionvera.constrant.GlobalConstants;
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

/**
 * TCP server
 * @author dql714099655
 *
 */
@Component
public class NettyTcpServer {
	
	private Logger logger = LoggerFactory.getLogger(NettyTcpServer.class);
	
	@Value("${netty.tcp.server.port}")
	private String port;

	@Value("${netty.tcp.server.threads}")
	private Integer nThreads;
	
	@Autowired
	private TcpServerHandler tcpServerHandler;
	
	public void run() throws Exception {
		logger.info("========启动netty tcp 服务端Start ======");
		logger.info("========nThreads========" + nThreads);
		EventLoopGroup bossGroup = new NioEventLoopGroup(nThreads);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						socketChannel.pipeline()
						.addLast(new DelimiterBasedFrameDecoder(8192, Unpooled.copiedBuffer(GlobalConstants.TCP_END_DELIMITER.getBytes())))
						.addLast(new StringEncoder(Charset.forName("UTF-8")))
                        .addLast(new StringDecoder(Charset.forName("UTF-8")))
                        .addLast(tcpServerHandler);
					}
				})
				.option(ChannelOption.SO_BACKLOG, 1024)
				.childOption(ChannelOption.SO_KEEPALIVE, true);
			ChannelFuture future = b.bind(Integer.parseInt(port)).sync();
			future.channel().closeFuture().sync();
			logger.info("=========启动netty tcp 服务端End ========");
		}finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
}
