package com.loohp.blockmodelrenderer.utils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskCompletion {

    private List<Future<?>> futures;

    public TaskCompletion(List<Future<?>> futures) {
        this.futures = futures;
    }

    public void join() {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Future<?>> getFutures() {
        return futures;
    }

}
