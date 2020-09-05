package com.renxl.club.raft.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-28 20:36
 * @Version 1.0.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class DefaultConfig implements Config {


    // 选举最小时间
    private final int minElectionTimeout = 3000;
    // 选举最大时间
    private final int maxElectionTimeout = 4000;
    // 日志复制间隔时间
    private int logReplicationInterval = 1000;


    private int raftPort ;


}
