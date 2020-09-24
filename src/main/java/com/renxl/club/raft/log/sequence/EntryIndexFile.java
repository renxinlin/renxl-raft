package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.EntryIndex;
import com.renxl.club.raft.support.FileApi;

import java.io.File;
import java.util.Arrays;


/**
 * [minIndex] [maxIndex] [newEntryFileIndex]
 * [kind][term][data_filename][数据文件偏移量]
 *
 * @Author renxl
 * @Date 2020-09-09 02:23
 * @Version 1.0.0
 */
public class EntryIndexFile {

    private final int DEFAULT_INDEX_SIZE = (4 + 4 + 4 + 4);
    private final int DEFAULT_META_SIZE = (4 + 4 + 4);
    private FileApi fileApi;


    private boolean loaded = false;
    /**
     * 表示当前偏移量 用于entryfile恢复数据写位置
     * 应该叫做loadindex
     */
    private int loadOffset;

    /**
     * 为什么需要这两个参数
     * 文件体系为：索引mapping数据文件
     * 写文件的时候需要知道当前buffer的position位置
     * 知道maxIndex 就能知道当前索引文件的position
     * 而知道索引文件的position就能知道数据文件的position
     * <p>
     * 服务器会restart，所以在索引文件中新增这两个元信息
     */
    private int minIndex; // 全局index
    private int maxIndex; // 全局index

    private int newEntryFileIndex = -1; // 最新的数据文件变化索引点


    private String prefix = "./data/index";
    private int fileSize = 1024 * 1024 * 2;


    public EntryIndexFile() {

        fileApi = loadFromFileSystem(prefix);
        // 首次加载
        if (fileApi == null) {
            fileApi = new FileApi(prefix, fileSize, fileSize + DEFAULT_META_SIZE);
        }
    }

    private FileApi loadFromFileSystem(String prefix) {

        File file = new File(prefix);
        String[] fileName = file.list();
        if (fileName == null) return null;

        Arrays.sort(fileName);
        String lastFile = fileName[fileName.length - 1];
        // 需要从indexfile 恢复 currentOffset
        fileApi = new FileApi(prefix, Integer.parseInt(lastFile));
        loaded = true;
        minIndex = fileApi.readInt(0);
        maxIndex = fileApi.readInt(4);
        newEntryFileIndex = fileApi.readInt(8);
        int lastIndexOffset = DEFAULT_META_SIZE + (maxIndex - minIndex) * DEFAULT_INDEX_SIZE;
        loadOffset = fileApi.readInt(lastIndexOffset + 12);
        //  需要从索引文件恢复数据文件currentOffset

        return fileApi;
    }

    public boolean writeEntry(int index, EntryIndex entry, boolean newEntryFileIndexChanged) {
        boolean changed = false;
        int kind = entry.getKind();
        int term = entry.getTerm();
        int file = entry.getFile();

        // 文件已经写满
        if ((fileApi.getDataFileSize() - DEFAULT_META_SIZE) == (maxIndex - minIndex + 1) * DEFAULT_INDEX_SIZE) {
            fileApi = new FileApi(prefix, fileApi.getFileName() + fileSize, fileSize, index);
            changed = true;
        }
        // 内存
        if (minIndex == 0) {
            minIndex = index;
        }
        maxIndex = index;
        if (newEntryFileIndexChanged) {
            fileApi.writeInt(8, index);

        }

        // 索引文件
        int offset = entry.getOffset();// 数据文件偏移量
        // 元信息
        fileApi.writeInt(0, minIndex);
        fileApi.writeInt(4, maxIndex);

        // 索引信息
        int indexOffset = DEFAULT_META_SIZE + DEFAULT_INDEX_SIZE * (maxIndex - minIndex);
        fileApi.writeInt(indexOffset, kind);
        fileApi.writeInt(indexOffset + 4, term);
        fileApi.writeInt(indexOffset + 8, file);
        fileApi.writeInt(indexOffset + 12, offset);
        return changed;
    }
    //

    /**
     * 用于恢复entryFile的offset
     *
     * @return
     */
    public int getLoadOffset() {
        if (!loaded) return -1;
        return loadOffset;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public int getMinIndex() {
        return minIndex;
    }


    public int getNewEntryFileIndex() {
        return newEntryFileIndex;
    }

    /**
     * 数据文件的offset
     *
     * @param index
     * @return
     */
    public int getOffsetByIndex(int index) {
        int indexOffset = (index - minIndex) * DEFAULT_INDEX_SIZE + DEFAULT_META_SIZE;
        // 12 是长度offset的位置
        return fileApi.readInt(indexOffset + 12);

    }

    public void removeAfter(int lastMatchedIndex) {
        if (isEmpty() || lastMatchedIndex >= maxIndex) {
            return;
        }
        if (lastMatchedIndex < minIndex) {
            // 清空所有指针信息 恢复到刚刚load未使用的状态
            clear();
            return;
        }
        // 4-8  maxIndex 其中maxindex也表示了当前文件的使用位置
        fileApi.writeInt(4, lastMatchedIndex);
        // 索引文件字节缓冲区也不需要修改;只需要移动指针 由于快照存在由回退降级到快照恢复 所以lastMatchedIndex必然是大于newEntryFileIndex
        maxIndex = lastMatchedIndex;


    }

    public boolean isEmpty() {
        return maxIndex == minIndex;
    }

    public void clear() {
        maxIndex = minIndex;
        newEntryFileIndex = -1;

    }
}
