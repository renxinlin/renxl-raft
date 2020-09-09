package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author renxl
 * @Date 2020-09-09 14:19
 * @Version 1.0.0
 */
public abstract class AbstractSequence implements Sequence {

    private Integer commitIndex;

    private List<Entry> entryBuffer = new ArrayList<>();
    @Override
    public int getCommitIndex(){
        return commitIndex;
    }


}
