package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.Entry;
import com.renxl.club.raft.log.entry.EntryMeta;
import com.renxl.club.raft.support.FileApi;

import java.io.File;
import java.util.Arrays;

/**
 * kind index term length bytes
 * <p>
 * 当达到阈值的时候就开始新建新的内存映射文件
 *
 * @Author renxl
 * @Date 2020-09-09 02:23
 * @Version 1.0.0
 */
public class EntryFile {


    private FileApi fileApi;

    /**
     * 当前数据文件写到的索引位置
     */
    private int nextOffset;

    private String prefix = "./data/log";
    private int fileSize = 1024 * 1024 * 10;


    public EntryFile(int loadOffset) {

        fileApi = loadFromFileSystem(prefix, loadOffset);
        // 首次加载
        if (fileApi == null) {
            fileApi = new FileApi(prefix, fileSize, fileSize);
        }

    }

    private FileApi loadFromFileSystem(String prefix, int loadOffset) {

        File file = new File(prefix);
        String[] fileName = file.list();
        if (fileName == null) return null;

        Arrays.sort(fileName);
        String lastFile = fileName[fileName.length - 1];
        // 需要从indexfile 恢复 currentOffset
        fileApi = new FileApi(prefix, Integer.parseInt(lastFile));
        int length = fileApi.readInt(loadOffset + 12);
        nextOffset = loadOffset + 16 + length;
        return fileApi;
    }

    public boolean writeEntry(Entry entry) {
        boolean changed = false;
        byte[] commandBytes = entry.getCommandBytes();
        if (nextOffset + 16 + commandBytes.length > fileSize) {
            fileApi = new FileApi(prefix, fileApi.getFileName() + fileSize, fileSize);
            nextOffset = 0;
            changed = true;

        }
        int index = entry.getIndex();
        int kind = entry.getKind();
        int term = entry.getTerm();

        fileApi.writeInt(nextOffset, kind);
        fileApi.writeInt(nextOffset + 4, index);
        fileApi.writeInt(nextOffset + 8, term);
        fileApi.writeInt(nextOffset + 12, commandBytes.length);

        fileApi.write(nextOffset + 16, commandBytes);
        nextOffset += 16;
        nextOffset += commandBytes.length;
        return changed;

    }

    public Entry getEntry(int dataFileOffset) {
        int kind = fileApi.readInt(dataFileOffset);
        int index = fileApi.readInt(dataFileOffset + 4);
        int term = fileApi.readInt(dataFileOffset + 8);
        EntryMeta entryMeta = new EntryMeta(kind, index, term);
        return entryMeta;
    }


    public void truncate(int offsetByIndex) {
        // 只需要移动指针 缓冲区内的内容不管 将来会被覆盖
        nextOffset = offsetByIndex;

    }

    public void clear() {
        nextOffset = 0;
    }
}
