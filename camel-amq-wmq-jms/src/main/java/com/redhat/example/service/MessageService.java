package com.redhat.example.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.redhat.example.config.AMQConfig.PRODUCER_QUEUE;

@Slf4j
@Service
public class MessageService {

    public static final String brokerJMXURL = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";

    private JMXConnector connector;

//    @Autowired
//    private JmsTemplate jmsTemplate;

//    @JmsListener(destination = "DEV.QUEUE.1")
//    public void receiveMessage(final Message jsonMessage) throws JMSException, InterruptedException {
//        TimeUnit.SECONDS.sleep(5);
//        System.out.println("Received message " + jsonMessage);
//        throw new RuntimeException("Something went wrong...");
//    }

//    public void send(String myMessage) {
//        log.info("Sending JMS to queue <" + myMessage + ">");
//        jmsTemplate.send(PRODUCER_QUEUE, new MessageCreator() {
//            public Message createMessage(Session session) throws JMSException {
//                return session.createTextMessage(myMessage);
//            }
//        });
//    }

    public String getUUID() {
        return UUID.randomUUID().toString();
    }

    public Long getQueueDepth(String queueName) {

        try {
            if (connector == null) {
                connector = JMXConnectorFactory.connect(new JMXServiceURL(brokerJMXURL));
            }

            ObjectName nameConsumers = new ObjectName("org.apache.activemq:type=Broker,brokerName=embedded,destinationType=Queue,destinationName=" + queueName);
            DestinationViewMBean mbView = MBeanServerInvocationHandler.newProxyInstance(connector.getMBeanServerConnection(), nameConsumers, DestinationViewMBean.class, true);
            return mbView.getQueueSize();
        } catch (Exception e) {
            log.debug("Unable to connect to AMQ Queue : {}", e.getLocalizedMessage());
            return null;
        }
    }
}
