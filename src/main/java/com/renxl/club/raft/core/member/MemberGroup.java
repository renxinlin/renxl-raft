package com.renxl.club.raft.core.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author renxl
 * @Date 2020-08-26 19:15
 * @Version 1.0.0
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)

public class MemberGroup {


    /**
     * 包含自己
     */
    private Map<NodeId,Member> members;

    private NodeId self;


    public MemberGroup(List<Endpoint> endpoint,NodeId nodeId) {
        bulid(endpoint,nodeId);

    }

    private void bulid(List<Endpoint> endpoints, NodeId nodeId) {
        this.self = nodeId;
        members = new HashMap();
        endpoints.forEach(endpoint->{
            Member member = new Member(endpoint,null);
            members.put(endpoint.getNodeId(),member);
        });


    }


}
