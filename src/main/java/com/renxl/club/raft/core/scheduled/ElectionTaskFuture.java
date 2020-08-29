package com.renxl.club.raft.core.scheduled;

import java.util.concurrent.ScheduledFuture;

/**
 * @Author renxl
 * @Date 2020-08-28 19:33
 * @Version 1.0.0
 */
public class ElectionTaskFuture {
    private ScheduledFuture<?> scheduledFuture ;
    public ElectionTaskFuture(ScheduledFuture<?> future) {
        scheduledFuture = future;

    }

    /**
     * 一般为false 执行中的就让他执行完毕
     * @param mayInterruptIfRunning
     */
    public boolean cancel(boolean mayInterruptIfRunning){
         return scheduledFuture.cancel(mayInterruptIfRunning);
    }
}
