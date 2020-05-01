package com.redhat.example.routes;

import com.redhat.example.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.redhat.example.config.AMQConfig.CONSUMER_QUEUE;
import static com.redhat.example.config.AMQConfig.PRODUCER_QUEUE;

@Slf4j
@Component
@ConditionalOnExpression("${fuse.wmq.jms.enabled} == false && ${fuse.amq.jms.enabled} == true")
public class AMQRoutes extends SpringRouteBuilder {

    @Autowired
    CamelContext context;

    @Autowired
    MessageService messageService;

    @Override
    public void configure() throws Exception {

        // @formatter:off

        AtomicInteger counter = new AtomicInteger(1);

        // Camel Transaction Error Handler : https://camel.apache.org/manual/latest/transactionerrorhandler.html

        // Configure OOTB Transaction Error Handlers
        errorHandler(transactionErrorHandler()
                .rollbackLoggingLevel(LoggingLevel.INFO)); // << Logging Level of Rollback Logging Messages

        // onException example of Transaction Management
        onException(ConnectException.class) // catch certain exception
                .handled(true) // false == SpringTransactionManager would auto rollback transaction
                .log(LoggingLevel.ERROR, "Something went wrong... !!")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Access Handled Exception and log/audit etc..
                        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                        log.error("Cause Type: '{}', Exception Localised Message: '{}'", cause.getClass().getName(), cause.getLocalizedMessage());
                    }
                })
                //.rollback(); //<< Rolls back and Throws Exception on Exchange
                .markRollbackOnly(); // Rollsback, Logs error on exchange but doesnt throw exceptions

        /****************************************************/
        /*** Utility Routes : Routes to help log Scenario ***/
        /****************************************************/

        // First delivery attempt always fails
        interceptSendToEndpoint("amq:queue:" + CONSUMER_QUEUE)
                .choice()
                .when(header("JMSRedelivered").isEqualTo("false"))
                    .throwException(new ConnectException("Cannot connect to Consumer Network"))
                .end();

        // Poll and & Log Producer Queue Depth
        from("timer:producerQueue?period=1000").routeId("Camel::AMQ::ProducerQueueDepth")
                .setHeader("queueName", constant(PRODUCER_QUEUE))
                .bean(messageService, "getQueueDepth(${header.queueName})")
                .choice()
                    .when(body().isNotNull())
                        .log(LoggingLevel.INFO, "${header.queueName} Queue Size: ${body}")
                .endChoice();

        // Poll and & Log Consumer Queue Depth
        from("timer:consumerQueue?period=3000").routeId("Camel::AMQ::ConsumerQueueDepth")
                .setHeader("queueName", constant(CONSUMER_QUEUE))
                .bean(messageService, "getQueueDepth(${header.queueName})")
                .choice()
                    .when(body().isNotNull())
                        .log(LoggingLevel.INFO, "${header.queueName} Queue Size: ${body}")
                .endChoice();

        /****************************************************/
        /*** JMS Routes :: Routes demonstrate TX Rollback ***/
        /****************************************************/

        // Producer Trusted Zone : Message Producer
        from("timer:producerTimer?period=10000").routeId("Camel::AMQ::ProducerMessageProducer")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange)throws Exception {
                        String uuid = UUID.randomUUID().toString();
                        exchange.getIn().setHeader("messageCounter", counter.getAndIncrement());
                        exchange.getIn().setBody(uuid);
                    }})
                .log(LoggingLevel.INFO, "MESSAGE PRODUCER : ${header.messageCounter} - Sending message - ${body}")
                .to("amq:queue:" + PRODUCER_QUEUE);


        // DMZ Example Route : Delay Consumer for demo purposes
        from("amq:queue:" + PRODUCER_QUEUE + "?transacted=true")
//                .transacted()
                .delay(10000)
                .log(LoggingLevel.INFO, "Consumer Received Producer message - ${body}")
                .to("amq:queue:" + CONSUMER_QUEUE);


        // Consumer Trusted Zone - Dequeue Messages
        from("jms:queue:" + CONSUMER_QUEUE + "?transacted=true")
                .transacted()
                .delay(20000)
                .log("Consumer Trusted Zone processing message - ${body}");

        // @formatter:on

    }

}
