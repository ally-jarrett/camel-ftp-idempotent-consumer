package com.redhat.example.config;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.component.jms.JmsComponent;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.jms.JMSException;

@Slf4j
@ConditionalOnExpression("${fuse.wmq.jms.enabled:true}")
@Configuration
@EnableTransactionManagement
public class WMQConfig {

    @Value("${wmq.host:localhost}")
    String host;

    @Value("${wmq.port:1414}")
    Integer port;

    @Value("${wmq.username:app}")
    String username;

    @Value("${wmq.password:passw0rd}")
    String pwd;

    @Value("${wmq.channel:DEV.APP.SVRCONN}")
    String channel;

    @Value("${wmq.queue-manager:QM1}")
    String queueManager;

    /**
     * Using Springs CachingConnectionFactory
     * @param cachingConnectionFactory
     * @return
     * @throws JMSException
     */
    @Bean("wmq-consumer")
    public JmsComponent wmq_consumer(@Qualifier("cachingConnectionFactory") CachingConnectionFactory cachingConnectionFactory) throws JMSException {
        JmsComponent jmsComponent = new JmsComponent();
        jmsComponent.setConnectionFactory(cachingConnectionFactory);
        jmsComponent.setConcurrentConsumers(5);
        return jmsComponent;
    }

    /**
     * Using generic JmsPoolConnectionFactory
     * @param jmsPoolConnectionFactory
     * @return
     * @throws JMSException
     */
    @Bean("wmq-producer")
    public JmsComponent wmq(@Qualifier("wmqJmsPoolConnectionFactory") JmsPoolConnectionFactory jmsPoolConnectionFactory) throws JMSException {
        JmsComponent jmsComponent = new JmsComponent();
        jmsComponent.setConnectionFactory(jmsPoolConnectionFactory);
        return jmsComponent;
    }

    @Bean
    public MQQueueConnectionFactory mqQueueConnectionFactory() throws JMSException {
        MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
        mqQueueConnectionFactory.setHostName(host);
        mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        mqQueueConnectionFactory.setChannel(channel);
        mqQueueConnectionFactory.setPort(port);
        mqQueueConnectionFactory.setQueueManager(queueManager);
        return mqQueueConnectionFactory;
    }

    @Bean("wmqJmsPoolConnectionFactory")
    public JmsPoolConnectionFactory jmsPoolConnectionFactory(UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter) {
        JmsPoolConnectionFactory poolingFactory = new JmsPoolConnectionFactory();
        poolingFactory.setConnectionFactory(userCredentialsConnectionFactoryAdapter);
        poolingFactory.setMaxSessionsPerConnection(500);
        poolingFactory.setMaxConnections(1);
        return poolingFactory;
    }

    @Bean("userCredentialsConnectionFactoryAdapter")
    public UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter(
            MQQueueConnectionFactory mqQueueConnectionFactory) {
        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
        userCredentialsConnectionFactoryAdapter.setUsername(username);
        userCredentialsConnectionFactoryAdapter.setPassword(pwd);
        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqQueueConnectionFactory);
        return userCredentialsConnectionFactoryAdapter;
    }

    @Bean("cachingConnectionFactory")
    //@Primary
    public CachingConnectionFactory cachingConnectionFactory(
            UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(userCredentialsConnectionFactoryAdapter);
        cachingConnectionFactory.setSessionCacheSize(500);
        cachingConnectionFactory.setReconnectOnException(true);
        return cachingConnectionFactory;
    }

    @Bean("wmqTransactionManager")
    //@Primary
    public PlatformTransactionManager jmsTransactionManager(@Qualifier("cachingConnectionFactory") CachingConnectionFactory cachingConnectionFactory) {
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(cachingConnectionFactory);
        return jmsTransactionManager;
    }

    @Bean
    public JmsTemplate jmsTemplate(@Qualifier("cachingConnectionFactory") CachingConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

//    @Bean(name = "PROPAGATION_REQUIRED")
//    public SpringTransactionPolicy springTransactionPolicy( @Qualifier("wmqTranasactionManager") PlatformTransactionManager wmqTransactionManager){
//        SpringTransactionPolicy propagationRequired = new SpringTransactionPolicy();
//        propagationRequired.setTransactionManager(jmsTransactionManager);
//        propagationRequired.setPropagationBehaviorName("PROPAGATION_REQUIRED");
//        return propagationRequired;
//
//    }
}
