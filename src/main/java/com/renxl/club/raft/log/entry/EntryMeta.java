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
public class EntryMeta  implements Entry{

    private int kind;
    private int index;
    private int term;



    @Override
    public int getKind() {
        return kind;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getTerm() {
        return term;
    }


    @Override
    public byte[] getCommandBytes() {
        return new byte[0];
    }

}
