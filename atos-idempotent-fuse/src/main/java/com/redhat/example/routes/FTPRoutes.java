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
import com.redhat.summit.exception.CustomException;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FTPRoutes extends RouteBuilder {

    // Camel IdempotentRepository : https://camel.apache.org/components/latest/eips/idempotentConsumer-eip.html

    /**
     * FileBased Idempotent Repo - {WORKING_DIR}/idempotent/dtp-idempotent.dat
     * @return
     */
    @Bean
    private IdempotentRepository<String> idempotentRepo() {
        return FileIdempotentRepository.fileIdempotentRepository(new File("idempotent", "ftp-idempotent.dat"));
    }

    /**
     * In-Memory Idempotent Repo (local only) use HazelCast or Infinispan for
     * distributed Idempotent Repo
     * @return
     */
    @Bean
    private IdempotentRepository<String> memoryIdempotentRepo() {
        return MemoryIdempotentRepository.memoryIdempotentRepository(200);
    }

    @Override
    public void configure() throws Exception {

        AtomicInteger i = new AtomicInteger(1);


        // Camel Exception Handling : https://camel.apache.org/manual/latest/exception-clause.html
        // errorHandler = handles 'uncaught' exceptions
        // onException = handles 'caught' exceptions
        onException(CustomException.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "Something went wrong, here's the Payload :: ${body}")
                .end();

        /****************************************************/
        /****** Simulate Files being placed into FTP FS *****/
        /****************************************************/

        from("timer://foo?fixedRate=true&period=10000")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setHeader("fileName", String.format("%05d", i.getAndIncrement()));
                        exchange.getIn().setBody(UUID.randomUUID().toString());
                    }
                })
                .log(LoggingLevel.INFO, "Sending new file instance with name ${header.fileName} to FTP Server with Value: ${body}")
                .to("file://{{user.dir}}/ftp/?fileName=${header.fileName}.txt&charset=utf-8");

        // Camel FTP : https://camel.apache.org/components/latest/ftp-component.html

        /****************************************************/
        /**** Concurrent Consumers :: Read Multiple Times ***/
        /****************************************************/

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

        /****************************************************/
        /**** Single Consumers :: Only Once ****/
        /****************************************************/

//        from("ftp://username@localhost:21"
//                + "?password=admin"
//                + "&readLock=idemopotent"
//                + "&idempotentRepository=#memoryIdempotentRepo"
//                + "&move=/processed" // Move successfully processed file to designated location
//                + "&moveFailed=/failed" // Move failed processed files to designated location
//                + "&passiveMode=true").routeId("Camel::FTP::Consumer1::Idempotent")
//                .log(LoggingLevel.INFO, "FTP Server: File: ${header.CamelFileName} with payload : ${body}");

        /****************************************************/
        /**** Concurrent Consumers :: Once and Only Once ****/
        /****************************************************/

        from("ftp://username@localhost:21"
                + "?password=admin"
                + "&idempotent=true"
                + "&idempotentRepository=#memoryIdempotentRepo"
                + "&inProgressRepository=#idempotentRepo"
                + "&readLock=idempotent"// ReadLock on File avoid competing consumers
                + "&passiveMode=true").routeId("Camel::FTP::Consumer1::Idempotent::Final")
                .log(LoggingLevel.INFO, "FTP Server: File: ${header.CamelFileName} with payload : ${body}");

        from("ftp://username@localhost:21"
                + "?password=admin"
                + "&idempotent=true"
                + "&idempotentRepository=#memoryIdempotentRepo"
                + "&inProgressRepository=#idempotentRepo" // ReadLock to avoid competing consumers
                + "&readLock=idempotent" // ReadLock to avoid competing consumers
                + "&noop=true"
                + "&passiveMode=true").routeId("Camel::FTP::Consumer2::Idempotent::Final")
                .log(LoggingLevel.INFO, "FTP Server: File: ${header.CamelFileName} with payload : ${body}");
    }

}
