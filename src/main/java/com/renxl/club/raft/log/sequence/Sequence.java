package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.Entry;

import java.util.List;

/**
 * @Author renxl
 * @Date 2020-09-08 19:26
 * @Version 1.0.0
 */
public interface Sequence {


    /**
     * appendNoop
     * @param entry
     */
    void appendNoop(Entry entry);

    void append(List<Entry> entries);

    void commit(int index);

    int getCommitIndex();


    int getNextLogIndex();

    Entry getEntry(int index);

    boolean isEmpty();

    int getLastLogIndex();

    void removeAfter(int lastMatchedIndex);

}
