package redhat.example.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class JasyptRoutes extends RouteBuilder {

    // https://livebook.manning.com/book/camel-in-action-second-edition/chapter-14/37
    // https://access.redhat.com/documentation/en-us/red_hat_fuse/7.8/html/apache_camel_component_reference/jasypt_component

// Not required as Bean is loaded from camel-context.xml
//    @Override
//    protected CamelContext createCamelContext() throws Exception {
//        CamelContext context = super.createCamelContext();
//
//        JasyptPropertiesParser jasypt = new JasyptPropertiesParser();
//        jasypt.setPassword("supersecret");
//        PropertiesComponent prop = context.getComponent("properties", PropertiesComponent.class);
//        prop.setLocation("classpath:rider-test.properties");
//        prop.setPropertiesParser(jasypt);
//        return context;
//    }

    @Override
    public void configure() throws Exception {

        // @formatter:off

//        from("timer://foo?fixedRate=true&period=1000").routeId("Camel::DSL::Timer::Hello-World::1s")
//            .log(LoggingLevel.INFO, "Hello World :: {{another.encrypted.value}}");

    }
}