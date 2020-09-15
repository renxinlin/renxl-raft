package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.Entry;

import java.util.LinkedList;

/**
 * @Author renxl
 * @Date 2020-09-09 14:19
 * @Version 1.0.0
 */
public abstract class AbstractSequence implements Sequence {
    /**
     * 偏移量
     */
    protected Integer logIndexOffset;

    /**
     *   commitIndex + pendEntries =  commitIndex
     */
    protected Integer nextLogIndex;

    protected Integer commitIndex;

    protected LinkedList<Entry> entryBuffer = new LinkedList();

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

}
