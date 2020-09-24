package com.renxl.club.raft.log.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 所有日志条目都包含的EntryMeta 元信息部分
 */
@AllArgsConstructor
@Getter
abstract class AbstractEntry implements Entry {

    protected final int index;
    protected final int term;
    private final int kind;




}
