package com.redhat.example.config;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@ConditionalOnExpression("${fuse.wmq.jms.enabled:true}")
@Configuration
@EnableTransactionManagement
public class WMQConfig {

    @Bean
    public JmsComponent wmq() {
        JmsComponent jmsComponent = new JmsComponent();
        jmsComponent.setConnectionFactory(mqQueueConnectionFactory());
        return jmsComponent;
    }

    @Bean
    public MQQueueConnectionFactory mqQueueConnectionFactory() {
        MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
        mqQueueConnectionFactory.setHostName("localhost");
        try {
            mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
            mqQueueConnectionFactory.setChannel("DEV.APP.SVRCONN");
            mqQueueConnectionFactory.setPort(1414);
            mqQueueConnectionFactory.setQueueManager("QM1");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return mqQueueConnectionFactory;
    }

    @Bean
    public UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter(
            MQQueueConnectionFactory mqQueueConnectionFactory) {
        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
        userCredentialsConnectionFactoryAdapter.setUsername("app");
        userCredentialsConnectionFactoryAdapter.setPassword("");
        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqQueueConnectionFactory);
        return userCredentialsConnectionFactoryAdapter;
    }

    @Bean
    @Primary
    public CachingConnectionFactory cachingConnectionFactory(
            UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(userCredentialsConnectionFactoryAdapter);
        cachingConnectionFactory.setSessionCacheSize(500);
        cachingConnectionFactory.setReconnectOnException(true);
        return cachingConnectionFactory;
    }

    @Bean("wmqTransactionManager")
    @Primary
    public PlatformTransactionManager jmsTransactionManager(CachingConnectionFactory cachingConnectionFactory) {
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(cachingConnectionFactory);
        return jmsTransactionManager;
    }

    @Bean
    public JmsTemplate jmsTemplate(CachingConnectionFactory connectionFactory) {
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
