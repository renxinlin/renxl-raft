package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.Entry;
import com.renxl.club.raft.log.entry.EntryIndex;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author renxl
 * @Date 2020-09-09 14:20
 * @Version 1.0.0
 */
@Slf4j
public class FileSequence extends AbstractSequence {

    private EntryFile entryFile;

    private EntryIndexFile entryIndexFile;


    public FileSequence() {
        entryIndexFile = new EntryIndexFile();
        commitIndex = entryIndexFile.getMaxIndex();
        entryFile = new EntryFile(entryIndexFile.getLoadOffset());
    }


    @Override
    public void append(Entry entry) {
        entryBuffer.add(entry);
        nextLogIndex++;


    }

    @Override
    public void append(List<Entry> entries) {
        entries.forEach(entry -> {
            entryBuffer.add(entry);
            nextLogIndex++;
        });
    }

    @Override
    public void commit(int commitIndexes) {
        if (commitIndexes <= commitIndex) {
            return;
        }
        if (entryBuffer.isEmpty() || entryBuffer.getLast().getIndex() < commitIndexes) {
            log.error("");
            return;
        }


        for (int index = commitIndexes + 1; index <= commitIndexes; index++) {
            Entry entry = entryBuffer.poll();
//            int entryIndex = entry.getIndex();

            entryFile.writeEntry(entry);
            entryIndexFile.writeEntry(index, (EntryIndex) entry);
            commitIndex = index;


            // todo 快照应用
        }

    }

    @Override
    public int getNextLogIndex() {
        return nextLogIndex;
    }

    @Override
    public Entry getEntry(int index) {
        return null;
    }


}
