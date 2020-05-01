package com.redhat.routes;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class APIRoute extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {

        // @formatter:off

        restConfiguration()
                .component("servlet")
                .dataFormatProperty("prettyPrint", "true")
                .port("8080")
                .contextPath("/rest")
                .bindingMode(RestBindingMode.json);

        // Define the component and hostname and port
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.auto);

        rest()
                .get("/ping").to("direct:ping");

        from("direct:ping")
                .transform().constant("Ping!");

        // @formatter:on
    }
}
