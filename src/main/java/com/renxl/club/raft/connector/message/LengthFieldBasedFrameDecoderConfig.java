package com.renxl.club.raft.connector.message;

/**
 * @Author renxl
 * @Date 2020-08-29 15:08
 * @Version 1.0.0
 */
public class LengthFieldBasedFrameDecoderConfig {


    public static final int MAX_FRAME_LENGTH = 4 * 1024 * 1024;  //4M
    public static final int LENGTH_FIELD_LENGTH = 4;  //长度字段所占的字节数
    public static final int LENGTH_FIELD_OFFSET = 1;  // 偏移一个单位
    public static final int LENGTH_ADJUSTMENT = 0;
    public static final int INITIAL_BYTES_TO_STRIP = 0;
}
