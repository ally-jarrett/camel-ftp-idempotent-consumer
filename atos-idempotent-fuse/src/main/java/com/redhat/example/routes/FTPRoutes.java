package com.redhat.example.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.idempotent.FileIdempotentRepository;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
import org.apache.camel.spi.IdempotentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FTPRoutes extends RouteBuilder {

    @Bean
    private IdempotentRepository<String> idempotentRepo() {
        return FileIdempotentRepository.fileIdempotentRepository(new File("idempotent", "ftp-idempotent.dat"));
    }

    @Bean
    private IdempotentRepository<String> memoryIdempotentRepo() {
        return MemoryIdempotentRepository.memoryIdempotentRepository(200);
    }

    @Override
    public void configure() throws Exception {

        AtomicInteger i = new AtomicInteger(1);

        from("timer://foo?fixedRate=true&period=10000")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        //File file = new File(UUID.randomUUID().toString());
                        exchange.getIn().setHeader("fileName", String.format("%05d", i.getAndIncrement()));
                        exchange.getIn().setBody(UUID.randomUUID().toString());
                    }
                })
                .log(LoggingLevel.INFO, "Sending new file instance with name ${header.fileName} to FTP Server with Value: ${body}")
                .to("file://{{user.dir}}/ftp/?fileName=${header.fileName}.txt&charset=utf-8");

//        from("ftp://username@localhost:21"
//                + "?password=admin"
//                + "&readLock=changed" // ReadLock 'changed' will pick up META changes i.e. timestamps
//                + "&passiveMode=true").routeId("Camel::FTP::Consumer1")
//                .log(LoggingLevel.INFO, "FTP Server: File: ${header.CamelFileName} with payload : ${body}");
//
//        from("ftp://username@localhost:21"
//                + "?password=admin"
//                + "&readLock=changed" // ReadLock on File avoid competing consumers
//                + "&passiveMode=false").routeId("Camel::FTP::Consumer2")
//                .log(LoggingLevel.INFO, "FTP Server: File: ${header.CamelFileName} with payload : ${body}");
//
//
//        from("ftp://username@localhost:21"
//                + "?password=admin"
//                + "&readLock=idemopotent" // ReadLock on File avoid competing consumers
////                + "&move=processed" // Move successfully processed file to designated location
////                + "&moveFailed=failed" // Move failed processed files to designated location
//                + "&passiveMode=true").routeId("Camel::FTP::Consumer1::Idempotent")
//                .idempotentConsumer(header("camelFileAbsolutePath"),
//                        MemoryIdempotentRepository.memoryIdempotentRepository(200))
//                .log(LoggingLevel.INFO, "FTP Server: File: ${header.CamelFileName} with payload : ${body}");


        from("ftp://username@localhost:21"
                + "?password=admin"
                + "&idempotent=true"
                + "&idempotentRepository=#memoryIdempotentRepo"
                + "&readLock=idempotent"// ReadLock on File avoid competing consumers
                + "&noop=true"
                + "&passiveMode=true").routeId("Camel::FTP::Consumer1::FileIdempotent")
                .log(LoggingLevel.INFO, "FTP Server: File: ${header.CamelFileName} with payload : ${body}");

        from("ftp://username@localhost:21"
                + "?password=admin"
                + "&idempotent=true"
                + "&idempotentRepository=#memoryIdempotentRepo"
                + "&readLock=idempotent" // ReadLock on File avoid competing consumers
                + "&noop=true"
                + "&passiveMode=false").routeId("Camel::FTP::Consumer2::FileIdempotent")
                .log(LoggingLevel.INFO, "FTP Server: File: ${header.CamelFileName} with payload : ${body}");
    }

}
