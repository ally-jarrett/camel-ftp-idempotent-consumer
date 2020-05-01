package com.redhat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(Application.class);

    /**
     * Main method to start the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        boolean run = true;

        while (run) {
            TimeUnit.SECONDS.sleep(30);

            logger.trace("A TRACE Message");
            logger.debug("A DEBUG Message");
            logger.info("An INFO Message");
            logger.warn("A WARN Message");
            logger.error("An ERROR Message");
        }
    }
}