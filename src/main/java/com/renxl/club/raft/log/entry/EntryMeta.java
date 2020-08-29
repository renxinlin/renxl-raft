package com.renxl.club.raft.log.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 一条日志条目的元信息
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EntryMeta {

    private int kind;
    private int index;
    private int term;



    public int getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }

    public int getTerm() {
        return term;
    }

}
