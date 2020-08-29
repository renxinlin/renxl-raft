package com.renxl.club.raft.connector.message;

import lombok.Data;

/**
 * @Author renxl
 * @Date 2020-08-29 14:39
 * @Version 1.0.0
 */
@Data
public abstract class RpcMessage {

    private byte type;
}
