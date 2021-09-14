package com.xebialabs.overcast.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RetryCommand<T> {

    public static Logger logger = LoggerFactory.getLogger(RetryCommand.class);

    private final int maxRetries;

    public RetryCommand(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public T run(Supplier<T> function) {
        try {
            return function.get();
        } catch (Exception e) {
            logger.info(e.getMessage());
            sleep();
            return retry(function);
        }
    }

    private T retry(Supplier<T> function) {
        int retryCounter = 0;
        while (retryCounter < maxRetries) {
            try {
                return function.get();
            } catch (Exception ex) {
                retryCounter++;
                sleep();
                logger.info(ex.getMessage());
                if (retryCounter >= maxRetries) {
                    break;
                }
            }
        }
        throw new RuntimeException("Command failed on all of " + maxRetries + " retries");
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
