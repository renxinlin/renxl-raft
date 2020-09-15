package com.renxl.club.raft.log.entry;

/**
 * K-V服务的操作日志
 */
public class CommandEntry extends AbstractEntry {
    // 操作的命令对应的二进制数据
    private final byte[] commandBytes;

    public CommandEntry( int term, int index,byte[] commandBytes) {
        super(KIND_COMMAND, index, term);
        this.commandBytes = commandBytes;
    }

    @Override
    public byte[] getCommandBytes() {
        return this.commandBytes;
    }



}
