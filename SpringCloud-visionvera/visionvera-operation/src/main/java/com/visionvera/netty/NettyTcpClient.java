package com.visionvera.netty;

import com.visionvera.constrant.GlobalConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NettyTcpClient {
	
	private static Logger logger = LoggerFactory.getLogger(NettyTcpClient.class);
	
	private volatile static AtomicInteger packageSeq = new AtomicInteger(0);

	private static EventLoopGroup workerGroup;

	private static Integer tcpPort;
	@Value("${netty.tcp.client.port}")
	public void setTcpPort(Integer tcpPort) {
		NettyTcpClient.tcpPort = tcpPort;
	}
	
	/**
	 * 初试化Bootstrap
	 * @return
	 */
	public static Bootstrap getBootstrap(TcpClientHandler tcpClientHandler) {
		workerGroup = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(workerGroup)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline()
						.addLast(new DelimiterBasedFrameDecoder(8192, Unpooled.copiedBuffer(GlobalConstants.TCP_END_DELIMITER.getBytes())))
						.addLast(new StringDecoder(Charset.forName("UTF-8")))
						.addLast(new StringEncoder(Charset.forName("UTF-8")))
						.addLast(tcpClientHandler);
				}
			});
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
			.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535));
		return bootstrap;
	}
	
	/**
	 * 发送信息,并接收返回
	 * @param host
	 * @param message
	 * @throws Exception
	 */
	public static String sendMsg(String host,String message){
		try {
			TcpClientHandler tcpClientHandler = new TcpClientHandler();
			Bootstrap bootstrap = getBootstrap(tcpClientHandler);
			Channel channel = bootstrap.connect(host, tcpPort).sync().channel();
			byte[] bytes = message.getBytes("utf-8");
			int loadLen = bytes.length;
			message = GlobalConstants.TCP_START_DELIMITER + String.format("%04d", loadLen)
				+ String.format("%04d", packageSeq.get()) + "json" + message + GlobalConstants.TCP_END_DELIMITER;
			//包序列加1
			packageSeq.set(packageSeq.incrementAndGet() % 10000);
			logger.info("发送信息："+message);
			ChannelPromise promise = tcpClientHandler.sendMessage(message);
			promise.await();
			logger.info("接收信息："+tcpClientHandler.getResponseMsg());
			return tcpClientHandler.getResponseMsg();
		} catch (Exception e) {
			logger.error("Netty tcp client发送信息失败", e);
			e.printStackTrace();
			return "error";
		}finally {
			close();
		}
	}
	public static void close() {
		logger.info("关闭资源");
		workerGroup.shutdownGracefully();
	}
}
