package redhat.example.strategy;

import org.apache.camel.PollingConsumerPollingStrategy;

public class CustomFtpPollingStrategy implements PollingConsumerPollingStrategy {


    @Override
    public void onInit() throws Exception {
        System.out.println("Init Poll!");
    }

    @Override
    public long beforePoll(long timeout) throws Exception {
        System.out.println("Before Poll!");
        return 0;
    }

    @Override
    public void afterPoll() throws Exception {
        System.out.println("After Poll!");
    }
}