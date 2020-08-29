package com.renxl.club.raft.core.scheduled;

import java.util.concurrent.ScheduledFuture;

/**
 * @Author renxl
 * @Date 2020-08-28 19:34
 * @Version 1.0.0
 */

public class LogReplicationFuture {

    private ScheduledFuture<?> scheduledFuture ;
    public LogReplicationFuture(ScheduledFuture<?> future) {
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
