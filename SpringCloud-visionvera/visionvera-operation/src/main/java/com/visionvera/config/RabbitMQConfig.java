package com.visionvera.config;

import com.visionvera.constrant.CommonConstrant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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
	 * 监听会管预约消息队列名称
	 * @return
	 */
	@Bean(name = CommonConstrant.RABBITMQ_HUIGUAN_2_MICRO_QUEUE_NAME)
	public Queue huiguanToMicroQueue () {
		/**
		 * 第一个参数为队列名称，第二个参数表示消息是否持久化
		 * 第三个参数表示是否独占队列: 即断开后是否自动删除
		 * 第四个参数表示当所有消费者客户端连接断开时是否自动删除队列
		 * 第五个参数表示Map类型的其他参数
		 */
		return new Queue(CommonConstrant.RABBITMQ_HUIGUAN_2_MICRO_QUEUE_NAME, true, false, true, null);
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
	 * 配置绑定: 会管预约队列和BusinessExchange进行绑定
	 * @param intelligentOperationQueue 使用的队列
	 * @param businessExchange 使用的交换器
	 * @return
	 */
	@Bean
	public Binding bindHuiguanToMicroQueueExchange(@Qualifier(CommonConstrant.RABBITMQ_HUIGUAN_2_MICRO_QUEUE_NAME)Queue huiguanToMicroQueue, 
			@Qualifier(CommonConstrant.RABBITMQ_BUSINESS_EXCHANGE_NAME) DirectExchange businessExchange) {
		/**
		 * 配置绑定，绑定会管预约队列到业务交换机上
		 */
		return BindingBuilder.bind(huiguanToMicroQueue).to(businessExchange).with(CommonConstrant.RABBITMQ_HUIGUAN_2_MICRO_QUEUE_NAME);
	}
	
//	@Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//		RabbitTemplate template = new RabbitTemplate(connectionFactory);
//        template.setMessageConverter(new Jackson2JsonMessageConverter());
//        return template;
//    }
//	 
//	@Bean
//	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
//		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//		factory.setConnectionFactory(connectionFactory);
//		factory.setMessageConverter(new Jackson2JsonMessageConverter());
//		return factory;
//	}
}
