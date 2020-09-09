package com.renxl.club.raft.log.sequence;

import com.renxl.club.raft.log.entry.EntryIndex;
import com.renxl.club.raft.support.FileApi;

import java.io.File;
import java.util.Arrays;

/**
 * @Author renxl
 * @Date 2020-09-09 02:23
 * @Version 1.0.0
 */
public class EntryIndexFile {

    private FileApi fileApi;


    private FileApi secondFileApi;

    /**
     * 为什么需要这两个参数
     * 文件体系为：索引mapping数据文件
     * 写文件的时候需要知道当前buffer的position位置
     * 知道maxIndex 就能知道当前索引文件的position
     * 而知道索引文件的position就能知道数据文件的position
     *
     * 服务器会restart，所以在索引文件中新增这两个元信息
     *
     *
     *
     */
    private int minIndex;
    private int maxIndex;


    private String prefix = "./data/index";
    private int fileSize = 1024 * 1024 * 2;



    public EntryIndexFile() {

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
        return null;
    }

    public void writeEntry(EntryIndex entry) {
        if (false) {
            fileApi = new FileApi(prefix, fileApi.getFileName() + fileSize, fileSize);
        }
        int index = entry.getOffset();
        int kind = entry.getKind();
        int term = entry.getTerm();
        int file = entry.getFile();
    }
        //


}
