package com.renxl.club.raft.log;

import com.renxl.club.raft.core.member.NodeId;
import com.renxl.club.raft.core.message.AppendEntryRequest;
import com.renxl.club.raft.log.entry.Entry;

import java.util.List;

/**
 * 目前以及确定日志全部采用内存映射文件
 * @Author renxl
 * @Date 2020-08-26 12:20
 * @Version 1.0.0
 */
public interface Log {

    /**
     * leader节点解决幽灵复现
     * @param term
     */
    void appendNoop(int term);

    /**
     * commitIndex + pendingEntries
     * @return
     */
    int getNextIndex();


    /**
     * 获取已经提交的commitindex
     * @return
     */
    int getCommitIndex();



    /**
     *  leader 节点 根据 follower节点的 nextIndex 获取相应日志
     * @param term
     * @param leaderId
     * @param nextIndex
     * @return
     */
    AppendEntryRequest createAppendEntries(int term, NodeId leaderId, int nextIndex);

    /**
     * follower 节点接收leader节点日志
     * @param prevLogTerm
     * @param prevLogIndex  来自leader节点存储的nextindex-1
     * @param entries
     * @return
     */
    boolean appendFromLeader(int prevLogTerm, int prevLogIndex, List<Entry> entries);

    /**
     * follower节点提交日志
     * @param commitIndex
     * @param term
     */
    void commitIndex(int commitIndex, int term);

}
