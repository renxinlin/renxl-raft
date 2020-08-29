package com.renxl.club.raft.connector.message;

/**
 *
 *
 * rpc消息类型
 * @Author renxl
 * @Date 2020-08-29 14:49
 * @Version 1.0.0
 */
public class MessageType {

    public static final byte APPEND_ENTRY_REQUEST  =1;
    public static final byte APPEND_ENTRY_RESPONSE  =2;

    public static final byte ELECTION_REQUEST  =3;
    public static final byte ELECTION_RESPONSE  =4;
}
