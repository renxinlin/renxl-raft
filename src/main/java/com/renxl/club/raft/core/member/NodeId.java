package com.renxl.club.raft.core.member;

import com.renxl.club.raft.support.ProtoStuffUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author renxl
 * @Date 2020-08-27 02:02
 * @Version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NodeId  {
    private String id;

    public static void main(String[] args) {
        NodeId nodeId = new NodeId("12");
        byte[] serialize = ProtoStuffUtil.serialize(nodeId);
        NodeId deserialize = ProtoStuffUtil.deserialize(serialize, NodeId.class);
    }
}
