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


    void appendNoop(int term);

    /**
     * commitIndex + pendingEntries
     * @return
     */
    int getNextIndex();


    int getCommitIndex();

    List<Entry> getAppendEntrys(int nextIndex);


    AppendEntryRequest createAppendEntries(int term, NodeId leaderId,int nextIndex);

}
