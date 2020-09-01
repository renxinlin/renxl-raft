package com.renxl.club.raft.core.scheduled;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author renxl
 * @Date 2020-08-28 20:48
 * @Version 1.0.0
 */
public class NodeWorker {


    private ExecutorService executorService = new ThreadPoolExecutor(2*4,2*10,2*60, TimeUnit.SECONDS,new LinkedBlockingDeque<>(10000),new DefaultThreadFactory("core-node-worker"), new ThreadPoolExecutor.CallerRunsPolicy());



    public void execute(Runnable task){
        executorService.execute(task);
    };
}
