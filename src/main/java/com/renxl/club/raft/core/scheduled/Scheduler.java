package com.renxl.club.raft.core.scheduled;

/**
 *
 * raft的选举任务以及日志任务
 * @Author renxl
 * @Date 2020-08-28 19:31
 * @Version 1.0.0
 */
public interface Scheduler{

    /**
     * 选举任务
     * @return
     */
    ElectionTaskFuture electionTask(Runnable task);

    /**
     * 日志复制
     * @return
     */
    LogReplicationFuture  logReplicate(Runnable task);




}
