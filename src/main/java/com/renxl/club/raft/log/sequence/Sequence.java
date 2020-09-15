package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.Entry;

import java.util.List;

/**
 * @Author renxl
 * @Date 2020-09-08 19:26
 * @Version 1.0.0
 */
public interface Sequence {




    void append(Entry entry);

    void append(List<Entry> entries);

    void commit(int index);

    int getCommitIndex();


    int getNextLogIndex();

    Entry getEntry(int i);
}
