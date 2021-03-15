package com.redhat.example.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.redhat.example.config.AMQConfig.*;

@Slf4j
@Component
@Profile("clustered")
public class AMQClusterRoutes extends SpringRouteBuilder {

    static int counter = 1;
    static int consumed = 1;

    @Override
    public void configure() throws Exception {

        // @formatter:off

        /************************************************************/
        /*** JMS Routes :: Routes demonstrate Cluster Consumption ***/
        /************************************************************/

        // Message Producer every 1 second
        from("timer:producerTimer?period=10000").routeId("Camel::AMQ::ClusteredMessageProducer")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange)throws Exception {
                        exchange.getIn().setBody("TEST MESSAGE :: " + counter++);
                    }})
                .to("amq:queue:" + TEST_QUEUE + "?includeSentJMSMessageID=true&preserveMessageQos=true");
//                .log(LoggingLevel.INFO, "net.atos", "route - [${routeId}]: PEGA msg received, headers: ${headers} - ${body}")
//                .to("amq:queue:" + ANOTHER_QUEUE + "?includeSentJMSMessageID=true&preserveMessageQos=true")
//                .log(LoggingLevel.INFO, "net.atos", "route - [${routeId}]: PEGA msg delivered to CAL, headers: ${headers} - ${body}");

//        from("direct:amq-test-queue").routeId("Camel::AMQ::TestQueue")
//                .to("amq:queue:" + TEST_QUEUE)
//                .to("log:like-to-see-all?level=INFO&showAll=true&multiline=true")
//                .log(LoggingLevel.INFO, "net.atos", "route - [${routeId}]: PEGA msg received, headers: ${headers} - ${body}")
//                .log("method      = ${header.JMSTimestamp}")
//                .to("direct:amq-another-queue");
//
//        from("direct:amq-another-queue").routeId("Camel::AMQ::AnotherQueue")
//                .to("amq:queue:" + ANOTHER_QUEUE + "?includeSentJMSMessageID=true&preserveMessageQos=true")
//                .log(LoggingLevel.INFO, "net.atos", "route - [${routeId}]: PEGA msg delivered to CAL, headers: ${headers} - ${body}");

        // Message Consumer every 1 seconds
        from("amq:queue:" + TEST_QUEUE + "?transacted=true&includeSentJMSMessageID=true&preserveMessageQos=true").routeId("Camel::AMQ::ClusteredMessageConsumer")
                //.to("log:like-to-see-all?level=INFO&showAll=true&multiline=true")
                .log(LoggingLevel.INFO, "net.atos", "route - [${routeId}]: PEGA msg received, headers: ${headers} - ${body}")
                .to("amq:queue:" + TEST_QUEUE_2 + "?includeSentJMSMessageID=true&preserveMessageQos=true")
                .log(LoggingLevel.INFO, "net.atos", "route - [${routeId}]: PEGA msg delivered to AMQ, headers: ${headers} - ${body}")
                .to("amq:queue:" + ANOTHER_QUEUE + "?includeSentJMSMessageID=true&preserveMessageQos=true")
                .log(LoggingLevel.INFO, "net.atos", "route - [${routeId}]: PEGA msg delivered to IBM, headers: ${headers} - ${body}");
     //           .log(LoggingLevel.INFO, "MESSAGE CONSUMER : Received message - ${body} :: TOTAL CONSUMED MESSAGES :: " + consumed++);

        // @formatter:on

    }

}
