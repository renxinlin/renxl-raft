package com.renxl.club.raft.core;

import com.renxl.club.raft.core.member.NodeId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author renxl
 * @Date 2020-08-29 16:59
 * @Version 1.0.0
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class NodeStoreImpl implements NodeStore {
    private int term = 0;
    private NodeId votedFor;


}
