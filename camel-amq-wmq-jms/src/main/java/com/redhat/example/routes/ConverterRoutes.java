package com.redhat.example.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("convert")
public class ConverterRoutes extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {

        // @formatter:off

        /************************************************************/
        /*** JMS Routes :: Routes demonstrate Cluster Consumption ***/
        /************************************************************/

        // Message Producer every 1 second


        from("timer://runOnce?repeatCount=1&delay=5000").routeId("Camel::AMQ::TestQueue")
            .process(new Processor() {
                @Override
                public void process(Exchange exchange)throws Exception {
                    exchange.getIn().setBody("TEST MESSAGE :: ");
                }})
            .log("BODY      = ${body}");


        // @formatter:on

    }

}
