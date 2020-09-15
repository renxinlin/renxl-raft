package com.renxl.club.raft.log.entry;

/**
 * @Author renxl
 * @Date 2020-08-26 12:23
 * @Version 1.0.0
 */
public class HeartEntry extends AbstractEntry{

    public HeartEntry(int term, int index) {
        super(KIND_NO_HEART, term, index);
    }

    @Override
    public byte[] getCommandBytes() {
        return new byte[0];
    }
}
