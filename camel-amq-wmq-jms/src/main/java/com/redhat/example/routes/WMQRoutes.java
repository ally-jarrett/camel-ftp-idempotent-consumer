package com.redhat.example.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.redhat.example.config.AMQConfig.CONSUMER_QUEUE;

@Slf4j
@Component
@ConditionalOnExpression("${fuse.wmq.jms.enabled} == true && ${fuse.amq.jms.enabled} == true")
public class WMQRoutes extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {

        AtomicInteger counter = new AtomicInteger(1);

        // Camel Transaction Error Handler : https://camel.apache.org/manual/latest/transactionerrorhandler.html

        // Configure OOTB Transaction Error Handlers
        errorHandler(transactionErrorHandler()
                .rollbackLoggingLevel(LoggingLevel.INFO)); // << Logging Level of Rollback Logging Messages

        // onException example of Transaction Management
        onException(Exception.class) // catch certain exception
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

        /****************************************************/
        /*** JMS Routes :: Routes demonstrate TX Rollback ***/
        /****************************************************/

        // Producer Trusted Zone : Message Producer
        from("timer:producerTimer?period=10000").routeId("Camel::WMQ::MessageProducer")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String uuid = UUID.randomUUID().toString();
                        exchange.getIn().setHeader("messageCounter", counter.getAndIncrement());
                        exchange.getIn().setBody(uuid);
                    }
                })
                .log(LoggingLevel.INFO, "MESSAGE PRODUCER : ${header.messageCounter} - Sending message - ${body}")
                .to("wmq:queue:DEV.QUEUE.1");

        // Test WMQ RollBacks in isolation
//        from("wmq:queue:DEV.QUEUE.1?transacted=true")
//                .delay(5000)
//                .throwException(new RuntimeException("Issues..."))
//                .log(LoggingLevel.INFO, "Received IBM MQ Producers message - ${body}");

        // DMZ Example Route : Delay Consumer for demo purposefs
        from("wmq:queue:DEV.QUEUE.1?transacted=true")
                .delay(10000)
                .log(LoggingLevel.INFO, "Received IBM MQ Producer message - ${body}")
                .to("amq:queue:" + CONSUMER_QUEUE);

        // Consumer Trusted Zone - Dequeue Messages
        from("amq:queue:" + CONSUMER_QUEUE + "?transacted=true")
                .log("Consumer Trusted Zone processing message - ${body}");
    }
}
