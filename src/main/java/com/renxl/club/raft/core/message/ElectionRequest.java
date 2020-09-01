package com.renxl.club.raft.core.message;

import com.renxl.club.raft.connector.message.RpcMessage;
import com.renxl.club.raft.core.member.NodeId;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @Author renxl
 * @Date 2020-08-29 14:15
 * @Version 1.0.0
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class ElectionRequest extends RpcMessage {


    private int term;

    // 就是自己
    private NodeId candidateId;


    // 等到日志部分 需要比较日志

    /**
     *
     *
     *
     * 非常重要
     * 假设 集群网络很不稳定 或者节点不断宕机 出故障
     *
     * 以下最开始都是A为leader
     * 情况1：
     * index
     * A【term = 1】
     *
     * B【term =1】
     *
     * C 宕机 无index
     * 假设a重起并再一次率先发生选举 可以选举成功 此时c的 lastLog不存在
     * 情况2：
     * index
     * 1
     * A[term = 1]
     * B【term = 1】
     * C【】
     *
     *
     * A[term = 1]
     * B[term = 1][term = 2]
     * C[term = 1][term = 2]
     *
     *
     * 假设a选举成功 这时候C宕机 ，然后a宕机c启动
     * 这时候只能b选举成为leader c的日志追到index= 1 term=2
     * 此时b宕机 a率先发起选举
     * 比较以下两个字段 a为【0，1】c为【1，2】 c大于a，则a不可能选举成功
     *
     *
     *
     *
     */
    // 候选者最后一条
    private int lastLogIndex = 0;
    // 候选者最后一条日志的term
    private int lastLogTerm = 0;


    private Channel channel;

}
