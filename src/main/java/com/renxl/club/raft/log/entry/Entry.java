package com.renxl.club.raft.log.entry;

public interface Entry {
    // 心跳
    int KIND_NO_HEART = 0;
    // k-v 操作
    int KIND_COMMAND = 1;
    // add node
    int KIND_ADD_NODE = 3;

    // remove node
    int KIND_REMOVE_NODE = 4;

    int getKind();

    int getIndex();

    int getTerm();

    EntryMeta getMeta();


    /**
     * k-v服务的命令[二进制]
     * @return
     */
    byte[] getCommandBytes();
}