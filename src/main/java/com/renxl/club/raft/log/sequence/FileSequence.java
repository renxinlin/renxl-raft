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

    //   entryIndexFile 的日志偏移量
    int logIndexOffset;
    //  entryIndexFile的 索引下标位置
    int nextLogIndex;
    private EntryFile entryFile;
    private EntryIndexFile entryIndexFile;


    public FileSequence() {
        // 会判断是来源于恢复 还是新建
        entryIndexFile = new EntryIndexFile();
        // 数据文件的偏移位置
        int loadOffset = entryIndexFile.getLoadOffset();

        entryFile = new EntryFile(loadOffset);

        commitIndex = entryIndexFile.getMaxIndex();
        // 由 entryIndexFile 和 entryFile 来加载出 logIndexOffset 和nextlogindex

        logIndexOffset = entryIndexFile.getMinIndex();
        //
        nextLogIndex = entryIndexFile.getMaxIndex() + 1;
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
