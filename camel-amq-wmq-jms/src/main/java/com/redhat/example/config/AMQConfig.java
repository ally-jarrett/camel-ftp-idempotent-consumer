package com.redhat.example.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.jms.ConnectionFactory;

@Slf4j
@Configuration
@EnableTransactionManagement
@ConditionalOnExpression("'${fuse.amq.jms.enabled}' == 'true' || '${fuse.amq.cluster.jms.enabled}' == 'true'")
public class AMQConfig {

    @Value("${spring.activemq.broker-url}")
    String brokerUrl;

    @Value("${spring.activemq.user}")
    String username;

    @Value("${spring.activemq.password}")
    String pwd;

    public static final String PRODUCER_QUEUE = "producer-queue";
    public static final String CONSUMER_QUEUE = "consumer-queue";
    public static final String TEST_QUEUE = "cluster-queue";
    public static final String TEST_QUEUE_2 = "test-cluster-queue";
    public static final String ANOTHER_QUEUE = "another-cluster-queue";

//    @Bean
//    public JmsListenerContainerFactory<?> queueListenerFactory() {
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//        return factory;
//    }

    @Bean("amqJmsPoolConnectionFactory")
    public JmsPoolConnectionFactory jmsPoolConnectionFactory(ActiveMQConnectionFactory amQueueConnectionFactory) {
        JmsPoolConnectionFactory poolingFactory = new JmsPoolConnectionFactory();
        poolingFactory.setConnectionFactory(amQueueConnectionFactory);
        return poolingFactory;
    }

    @Bean("amqTransactionManager")
    public ActiveMQConnectionFactory amQueueConnectionFactory() {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
        cf.setBrokerURL(brokerUrl);
        cf.setUserName(username);
        cf.setPassword(pwd);
        return cf;
    }

    @Bean("amqTransactionManager")
    public PlatformTransactionManager amqTransactionManager(@Qualifier("amqJmsPoolConnectionFactory") final JmsPoolConnectionFactory jmsPoolConnectionFactory) {
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(jmsPoolConnectionFactory);
        return jmsTransactionManager;
    }

    @Bean
    public JmsComponent amq(@Qualifier("amqConnectionFactory") ConnectionFactory amQueueConnectionFactory,
                            @Qualifier("amqTransactionManager") PlatformTransactionManager jtaTansactionManager) {
        return JmsComponent.jmsComponentTransacted(amQueueConnectionFactory, jtaTansactionManager);
    }
}