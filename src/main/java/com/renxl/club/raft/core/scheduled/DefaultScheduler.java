package com.renxl.club.raft.core.scheduled;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * raft的选举任务以及日志任务
 *
 * @Author renxl
 * @Date 2020-08-28 19:31
 * @Version 1.0.0
 */
public class DefaultScheduler implements Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultScheduler.class);
    private final int minElectionTimeout;
    private final int maxElectionTimeout;
    /**
     * 注意这是个延时任务不是个定时任务
     */
    private final ScheduledExecutorService scheduledExecutorService;
    // 这个参数要不要外配
    private Random electionTimeoutRandom;
     // 日志复制间隔时间
    private int logReplicationInterval;


    public DefaultScheduler(int minElectionTimeout, int maxElectionTimeout, int logReplicationInterval) {
        if (minElectionTimeout <= 0 || maxElectionTimeout <= 0 || minElectionTimeout > maxElectionTimeout) {
            throw new IllegalArgumentException("check election time config ");
        }
        if (logReplicationInterval < 0 || logReplicationInterval <= 0) {
            throw new IllegalArgumentException("check log replication time config ");
        }


        if (logReplicationInterval < 2 * minElectionTimeout || logReplicationInterval < 200) {
            throw new IllegalArgumentException("in order to make sure cluster stable ,please make sure minElectionTimeout more bigger than [>>] minElectionTimeout ");

        }

        this.minElectionTimeout = minElectionTimeout;
        this.maxElectionTimeout = maxElectionTimeout;
        electionTimeoutRandom = new Random();

        this.logReplicationInterval = logReplicationInterval;

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor( new DefaultThreadFactory("core-schedule"));
    }

    /**
     * 选举任务
     * 这是个延时任务
     *
     * @return
     */
    @Override
    public ElectionTaskFuture electionTask(Runnable task) {
        int startElectionAfterSpecifiedime = minElectionTimeout + electionTimeoutRandom.nextInt(maxElectionTimeout - minElectionTimeout);
        ScheduledFuture<?> future = scheduledExecutorService.schedule(task, startElectionAfterSpecifiedime, TimeUnit.MILLISECONDS);
        return new ElectionTaskFuture(future);
    }

    /**
     * 谁调用？ 肯定是node 调用 node 怎么调用？通过上下文调用
     * 日志复制
     * 这是个定时任务
     * @return
     */
    @Override
    public LogReplicationFuture logReplicate(Runnable task) {
        // 英文选举完成的时候 没有了选举任务 所以这个线程用来继续做日志复制的任务
        // 选择scheduleAtFixedRate 还是scheduleWithFixedDelay对于但线程来说都可以
        // TODO 由于是单线程 我倾向于scheduleWithFixedDelay 等到复制完成在回看这里
        ScheduledFuture<?> future = scheduledExecutorService.scheduleWithFixedDelay(task, 100, logReplicationInterval, TimeUnit.MILLISECONDS);
        return new LogReplicationFuture(future);


    }




}
