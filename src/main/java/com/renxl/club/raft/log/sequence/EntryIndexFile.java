package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.EntryIndex;
import com.renxl.club.raft.support.FileApi;

import java.io.File;
import java.util.Arrays;

/**
 * [][][][数据文件偏移量]
 *
 * @Author renxl
 * @Date 2020-09-09 02:23
 * @Version 1.0.0
 */
public class EntryIndexFile {

    private final int DEFAULT_INDEX_SIZE = (4 + 4 + 4 + 4);
    private final int DEFAULT_META_SIZE = (4 + 4);
    private FileApi fileApi;


    private boolean loaded = false;
    /**
     * 表示当前偏移量 用于entryfile恢复
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


    private String prefix = "./data/index";
    private int fileSize = 1024 * 1024 * 2;


    public EntryIndexFile() {

        fileApi = loadFromFileSystem(prefix);
        // 首次加载
        if (fileApi == null) {
            fileApi = new FileApi(prefix, fileSize, fileSize + 4 + 4);
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
        int lastIndexOffset = DEFAULT_META_SIZE + (maxIndex - minIndex) * DEFAULT_INDEX_SIZE;
        loadOffset = fileApi.readInt(lastIndexOffset + 12);
        //  需要从索引文件恢复数据文件currentOffset
        return fileApi;
    }

    public void writeEntry(int index, EntryIndex entry) {
        int kind = entry.getKind();
        int term = entry.getTerm();
        int file = entry.getFile();

        // 文件已经写满
        if ((fileApi.getDataFileSize() - DEFAULT_META_SIZE) == (maxIndex - minIndex + 1) * DEFAULT_INDEX_SIZE) {
            fileApi = new FileApi(prefix, fileApi.getFileName() + fileSize, fileSize,index);
        }
        // 内存
        if (minIndex == 0) {
            minIndex = index;
        }
        maxIndex = index;

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
}
