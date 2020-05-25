package com.ech.kitchen.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

@Slf4j
public class BaseIT {

    public void waitToBeDone(BooleanSupplier endCondition, long maxWaitTime) {
        Semaphore watchProcess = new Semaphore(0);
        new Thread(() -> {
            while (!endCondition.getAsBoolean()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
            watchProcess.release();
        }).start();

        try {
            watchProcess.tryAcquire(maxWaitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.info(e.getMessage());
        }
    }

}
