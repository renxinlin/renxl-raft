package com.renxl.club.raft.core.member;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;

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
    private Map<NodeId, Member> members;

    private NodeId self;


    public MemberGroup(List<Endpoint> endpoint, NodeId nodeId) {
        bulid(endpoint, nodeId);

    }

    private void bulid(List<Endpoint> endpoints, NodeId nodeId) {
        this.self = nodeId;
        members = new HashMap();
        endpoints.forEach(endpoint -> {
            Member member = new Member(endpoint, null);
            members.put(endpoint.getNodeId(), member);
        });


    }


    public int getMatchIndexOfMajor() {
        // 排出自己
        int size = members.size() - 1;

        // 过半算法很简单  获取所有member index 排序取中间就是当前过半位置
        List<Long> matchIndexes = members.values()
                .stream().filter(member -> !member.getEndpoint().getNodeId().equals(self))
                .collect(Collectors.toList())
                .stream().map(member -> member.getReplicatingState().getMatchIndex())
                .collect(Collectors.toList());

        Collections.sort(matchIndexes);

        // 过半 所以 5个 index  = 4/2 = 2 4个有两个   index = 3/2 = 1;
        return matchIndexes.get(size / 2).intValue();
    }
}
