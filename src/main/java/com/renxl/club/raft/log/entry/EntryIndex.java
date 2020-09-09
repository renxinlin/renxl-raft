package com.renxl.club.raft.log.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Author renxl
 * @Date 2020-09-09 14:38
 * @Version 1.0.0
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EntryIndex {


    /**
     * 索引中的条目在数据文件中的偏移量
     */
    protected int offset;
    /**
     * 当前leader信息
     */
    protected int term;
    /**
     * 当前消息种类
     */
    private int kind;

    /**
     * 当前文件信息
     */
    private int file;


}
