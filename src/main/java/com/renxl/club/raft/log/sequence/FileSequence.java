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

    // 索引 表示新建数据文件的位置
    int newEntryFileIndex = -1;

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

        newEntryFileIndex = entryIndexFile.getNewEntryFileIndex();
    }


    @Override
    public void appendNoop(Entry entry) {
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

    /**
     * commit的实质就是将缓冲区的内容持久化
     *
     *
     * 技术手段上
     * 从堆内存到达mmap的系统内存映射区域 在由操作系统进行刷盘
     * @param commitIndexes
     */
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
            // 有可能底层的文件以及发生改变
            boolean changed = entryFile.writeEntry(entry);
            // 说明这个日志条目写到了新的数据文件，需要刷新索引文件的变化点
            if (changed) {
                newEntryFileIndex = commitIndex;
            } // todo 验证下是为commitIndex 还是index
            changed = entryIndexFile.writeEntry(index, (EntryIndex) entry, changed);
            commitIndex = index;


            if (changed) {
                // todo 生成快照
            }
        }

    }

    @Override
    public int getNextLogIndex() {
        return nextLogIndex;
    }

    /**
     * todo 目前只是获取了metadata
     * 在日志复制的时候 获取prevIndex 的数据
     *
     * @param index
     * @return
     */
    @Override
    public Entry getEntry(int index) {
        if (!isEntryPresent(index)) {
            return null;
        }
        Entry entry = doGetEntry(index);
        log.info("entrySequence.getEntry result is []", entry);
        return entry;

    }

    /**
     * 先查缓冲区
     * 后取文件
     *
     * @param index
     * @return
     */
    private Entry doGetEntry(int index) {
        if (!entryBuffer.isEmpty()) {
            int firstPendingEntryIndex = entryBuffer.getFirst().getIndex();
            if (index >= firstPendingEntryIndex) {
                Entry entry = entryBuffer.get(index - firstPendingEntryIndex);
                return entry;
            }
        }
        // 如果在快照中 前置流程已经抛错，所以这个index肯定在当前indexfile中
        return getByIndex(index);


    }

    private Entry getByIndex(int index) {
        int dataFileOffset = entryIndexFile.getOffsetByIndex(index);
        Entry entry = entryFile.getEntry(dataFileOffset);
        return entry;
    }

    private boolean isEntryPresent(int index) {
        if (newEntryFileIndex == -1) {
            // 当前索引文件对应的数据文件没有分裂 todo 按照道理这里就是 index=nextLogIndex - 1
            return !isEmpty() && index >= logIndexOffset && index <= nextLogIndex - 1;
        } else {
            // 当前索引文件对应的数据文件已经分裂

            return !isEmpty() && index >= newEntryFileIndex && index <= nextLogIndex - 1;
        }
    }

    @Override
    public boolean isEmpty() {
        return logIndexOffset == nextLogIndex;
    }

    @Override
    public int getLastLogIndex() {
        if (isEmpty()) {
            throw new IllegalStateException("暂无数据");
        }
        return nextLogIndex - 1;
    }

    @Override
    public void removeAfter(int lastMatchedIndex) {
        if (isEmpty() || lastMatchedIndex >= getLastLogIndex()) {
            return;
        }
        doRemoveAfter(lastMatchedIndex);
    }

    private void doRemoveAfter(int lastMatchedIndex) {
        if (!entryBuffer.isEmpty() && lastMatchedIndex >= entryBuffer.getFirst().getIndex() - 1) {
            // remove last n entries in pending entries
            for (int i = lastMatchedIndex + 1; i <= getLastLogIndex(); i++) {
                entryBuffer.removeLast();
            }
            //相比原来的会减小 到lastMatchedIndex 的位置
            nextLogIndex = lastMatchedIndex + 1;
            return;
        }
        try {

            if (lastMatchedIndex >= getFirstLogIndex()) {
                entryBuffer.clear();
                // 由于我们是mmap 只需要处理指针就行 缓存区内容不处理
                entryFile.truncate(entryIndexFile.getOffsetByIndex(lastMatchedIndex + 1));

                entryIndexFile.removeAfter(lastMatchedIndex);
                nextLogIndex = lastMatchedIndex + 1;
                commitIndex = lastMatchedIndex;
            } else {
                // 如果lastMatchedIndex 小于持久化的初始值 直接清空
                entryBuffer.clear(); // entryBuffer的index是必然大于
                entryFile.clear();
                entryIndexFile.clear();
                nextLogIndex = logIndexOffset;
                commitIndex = logIndexOffset - 1;
            }
        } catch (Exception e) {
            throw new IllegalAccessError(e.getMessage());
        }

    }

    private int getFirstLogIndex() {
        if (isEmpty()) {
            throw new IllegalStateException(" log entry not add to this file ");
        }
        return logIndexOffset;

    }


}
