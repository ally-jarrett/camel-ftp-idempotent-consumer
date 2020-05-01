package com.redhat.example.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.jms.ConnectionFactory;

@Slf4j
@Configuration
@EnableTransactionManagement
@ConditionalOnExpression("${fuse.amq.jms.enabled:true}")
public class AMQConfig {

    @Value("${spring.activemq.broker-url}")
    String brokerUrl;

    @Value("${spring.activemq.user}")
    String username;

    @Value("${spring.activemq.password}")
    String pwd;

    public static final String PRODUCER_QUEUE = "producer-queue";
    public static final String CONSUMER_QUEUE = "consumer-queue";

//    @Bean
//    public JmsListenerContainerFactory<?> queueListenerFactory() {
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//        return factory;
//    }

    @Bean("amqConnectionFactory")
    public ActiveMQConnectionFactory amQueueConnectionFactory() {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
        cf.setBrokerURL(brokerUrl);
        cf.setUserName(username);
        cf.setPassword(pwd);
        return cf;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PooledConnectionFactory pooledConnectionFactory() {
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setMaxConnections(10);
        pooledConnectionFactory.setConnectionFactory(amQueueConnectionFactory());
        return pooledConnectionFactory;
    }

    @Bean("amqTransactionManager")
    public PlatformTransactionManager amqTransactionManager(final ActiveMQConnectionFactory activeMQConnectionFactory) {
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(pooledConnectionFactory());
        return jmsTransactionManager;
    }

//    @Bean
//    public JmsTransactionManager createJmsTransactionManager(final ConnectionFactory connectionFactory) {
//        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
//        jmsTransactionManager.setConnectionFactory(connectionFactory);
//        return jmsTransactionManager;
//    }

    @Bean
    public JmsComponent amq(@Qualifier("amqConnectionFactory") ConnectionFactory amQueueConnectionFactory,
                            @Qualifier("amqTransactionManager") PlatformTransactionManager jtaTansactionManager) {
        return JmsComponent.jmsComponentTransacted(amQueueConnectionFactory, jtaTansactionManager);
    }
}