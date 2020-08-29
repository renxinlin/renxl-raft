package com.renxl.club.raft.log.entry;

public class EntryBuilder {
    /**
     * 负责解析网络io以及文件中的二进制数据为条目数据
     * @param kind
     * @param index
     * @param term
     * @param commandBytes
     * @return
     */
    public Entry create(int kind, int index, int term, byte[] commandBytes) {
        try {
            switch (kind) {
                case Entry.KIND_NO_HEART:
                    return new HeartEntry(index, term);
                case Entry.KIND_COMMAND:
                    return new CommandEntry(index, term, commandBytes);
                default:
                    throw new IllegalArgumentException("unexpected entry kind " + kind);
            }
        } catch (UnsupportedOperationException e) {
            throw new IllegalStateException("failed to parse command", e);
        }
    }


}
