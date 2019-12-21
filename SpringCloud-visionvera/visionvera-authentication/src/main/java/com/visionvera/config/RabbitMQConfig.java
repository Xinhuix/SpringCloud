package com.visionvera.config;

import com.visionvera.constrant.CommonConstrant;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置RabbitMQ
 *
 */
@Configuration
public class RabbitMQConfig {
	/**
	 * 配置队列名称为LogQueue
	 * @return
	 */
	@Bean(name = CommonConstrant.RABBITMQ_LOG_QUEUE_NAME)
	public Queue logQueue() {
		/**
		 * 第一个参数为队列名称，第二个参数表示消息是否持久化
		 * 第三个参数表示是否独占队列: 即断开后是否自动删除
		 * 第四个参数表示当所有消费者客户端连接断开时是否自动删除队列
		 * 第五个参数表示Map类型的其他参数
		 */
		return new Queue(CommonConstrant.RABBITMQ_LOG_QUEUE_NAME, true, false, false, null);
	}
	
	/**
	 * 向会管推送用户信息的队列名称
	 * @return
	 */
	@Bean(name = CommonConstrant.RABBIT_HUIGUAN_QUEUE_NAME)
	public Queue huiguanQueue() {
		/**
		 * 第一个参数为队列名称，第二个参数表示消息是否持久化
		 * 第三个参数表示是否独占队列: 即断开后是否自动删除
		 * 第四个参数表示当所有消费者客户端连接断开时是否自动删除队列
		 * 第五个参数表示Map类型的其他参数
		 */
		return new Queue(CommonConstrant.RABBIT_HUIGUAN_QUEUE_NAME, true, false, false, null);
	}
	
	/**
	 * 监听智能运维的队列名称
	 * @return
	 */
	@Bean(name = CommonConstrant.RABBITMQ_INTELLIGENT_OPERATION_QUEUE_NAME)
	public Queue intelligentOperationQueue () {
		/**
		 * 第一个参数为队列名称，第二个参数表示消息是否持久化
		 * 第三个参数表示是否独占队列: 即断开后是否自动删除
		 * 第四个参数表示当所有消费者客户端连接断开时是否自动删除队列
		 * 第五个参数表示Map类型的其他参数
		 */
		return new Queue(CommonConstrant.RABBITMQ_INTELLIGENT_OPERATION_QUEUE_NAME, true, false, false, null);
	}
	
	/**
	 * 配置交换机(路由器)及其类型。FanoutExchange表示广播式交换器
	 * @return
	 */
	@Bean(name = CommonConstrant.RABBITMQ_LOG_EXCHANGE_NAME)
	public FanoutExchange logExchange() {
		/**
		 * 第一个参数表示交换机名称，第二个参数表示是否持久化，第三个参数表示断开后是否自动删除
		 */
		return new FanoutExchange(CommonConstrant.RABBITMQ_LOG_EXCHANGE_NAME, true, false);
	}
	
	/**
	 * 配置交换机(路由器)及其类型。DirectExchange表示直连式交换机
	 * @return
	 */
	@Bean(name = CommonConstrant.RABBITMQ_BUSINESS_EXCHANGE_NAME)
	public DirectExchange businessExchange() {
		/**
		 * 第一个参数表示交换机名称，第二个参数表示是否持久化，第三个参数表示断开后是否自动删除
		 */
		return new DirectExchange(CommonConstrant.RABBITMQ_BUSINESS_EXCHANGE_NAME, true, false);
	}
	
	/**
	 * 配置绑定:logExchange与logQueue进行绑定
	 * @param logQueue 使用的队列
	 * @param logExchange 使用的交换器
	 * @return
	 */
	@Bean
	public Binding bindLogExchange(@Qualifier(CommonConstrant.RABBITMQ_LOG_QUEUE_NAME)Queue logQueue, 
			@Qualifier(CommonConstrant.RABBITMQ_LOG_EXCHANGE_NAME) FanoutExchange logExchange) {
		/**
		 * 配置绑定，将广播式日志交换器与日志队列绑定
		 */
		return BindingBuilder.bind(logQueue).to(logExchange);
	}
	
	/**
	 * 配置绑定: 会管队列和BusinessExchange交换机绑定
	 * @param huiguanQueue 使用的队列
	 * @param businessExchange 使用的交换器
	 * @return
	 */
	@Bean
	public Binding bindHuiguanUserExchange(@Qualifier(CommonConstrant.RABBIT_HUIGUAN_QUEUE_NAME)Queue huiguanQueue, 
			@Qualifier(CommonConstrant.RABBITMQ_BUSINESS_EXCHANGE_NAME) DirectExchange businessExchange) {
		/**
		 * 配置绑定，绑定会管队列到用户交换机上
		 */
		return BindingBuilder.bind(huiguanQueue).to(businessExchange).with(CommonConstrant.RABBIT_USER_EXCHANGE_HUIGUAN_QUEUE_BINDING_NAME);
	}
	
	/**
	 * 配置绑定: 智能运维队列和BusinessExchange进行绑定
	 * @param intelligentOperationQueue 使用的队列
	 * @param businessExchange 使用的交换器
	 * @return
	 */
	@Bean
	public Binding bindintelligentOperationQueueExchange(@Qualifier(CommonConstrant.RABBITMQ_INTELLIGENT_OPERATION_QUEUE_NAME)Queue intelligentOperationQueue, 
			@Qualifier(CommonConstrant.RABBITMQ_BUSINESS_EXCHANGE_NAME) DirectExchange businessExchange) {
		/**
		 * 配置绑定，绑定智能运维队列到业务交换机上
		 */
		return BindingBuilder.bind(intelligentOperationQueue).to(businessExchange).with(CommonConstrant.RABBIT_INTELLIGENT_OPERATION_QUEUE_BINDING_NAME);
	}
}
