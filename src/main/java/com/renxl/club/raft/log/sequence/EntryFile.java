package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.Entry;
import com.renxl.club.raft.support.FileApi;

import java.io.File;
import java.util.Arrays;

/**
 *
 *  kind index term length bytes
 *
 * 当达到阈值的时候就开始新建新的内存映射文件
 *
 * @Author renxl
 * @Date 2020-09-09 02:23
 * @Version 1.0.0
 */
public class EntryFile {


    private FileApi fileApi;


    private FileApi secondFileApi;

    private int currentOffset;

    private String prefix = "./data/log";
    private int fileSize = 1024 * 1024 * 10;



    public EntryFile() {

        fileApi = loadFromFileSystem(prefix);
        // 首次加载
        if (fileApi == null) {
            fileApi = new FileApi(prefix ,fileSize, fileSize);
        }
    }

    private FileApi loadFromFileSystem(String prefix) {

        File file = new File(prefix);
        String[] fileName = file.list();
        if (fileName == null) return null;

        Arrays.sort(fileName);
        String lastFile = fileName[fileName.length - 1];
        // 需要从indexfile 恢复 currentOffset
        fileApi = new FileApi(prefix,Integer.parseInt(lastFile));
        // todo 需要从索引文件恢复currentOffset
        currentOffset = -1;
        return null;
    }

    public void writeEntry(Entry entry) {
        byte[] commandBytes = entry.getCommandBytes();
        if(currentOffset + commandBytes.length > fileSize){
            // todo 达到阈值就异步提前生成

            fileApi = new FileApi(prefix,fileApi.getFileName()+fileSize,fileSize);
            currentOffset = 0;
        }
        int index = entry.getIndex();
        int kind = entry.getKind();
        int term = entry.getTerm();
        fileApi.writeInt(currentOffset, kind);
        fileApi.writeInt(currentOffset, index);
        fileApi.writeInt(currentOffset, term);
        fileApi.write(currentOffset, commandBytes);
        currentOffset += 12;
        currentOffset += commandBytes.length;

    }
}
