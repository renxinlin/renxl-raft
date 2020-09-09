package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.Entry;

import java.util.List;

/**
 * @Author renxl
 * @Date 2020-09-09 14:20
 * @Version 1.0.0
 */
public class FileSequence extends AbstractSequence {

    private EntryFile entryFile;

    private EntryIndexFile entryIndexFile;

    public FileSequence(){
        entryFile = new EntryFile();
    }


    @Override
    public int getFirstLogIndex() {
        return 0;
    }

    @Override
    public void append(Entry entry) {

    }

    @Override
    public void append(List<Entry> entries) {

    }

    @Override
    public void commit(int index) {

    }


}
