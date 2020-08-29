package com.renxl.club.raft;

import com.renxl.club.raft.core.Node;
import com.renxl.club.raft.core.NodeBuilder;
import com.renxl.club.raft.core.member.Address;
import com.renxl.club.raft.core.member.Endpoint;
import com.renxl.club.raft.core.member.NodeId;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author renxl
 * @Date 2020-08-25 18:08
 * @Version 1.0.0
 */
public class Launcher {


    public static void main(String[] args) {
        Endpoint a = new Endpoint(new NodeId("A"), new Address("127.0,0.1", 20000));
        Endpoint b = new Endpoint(new NodeId("B"), new Address("127.0,0.1", 20001));
        Endpoint c = new Endpoint(new NodeId("C"), new Address("127.0,0.1", 20002));
       List<Endpoint>  endpoints = new ArrayList<>();
        NodeBuilder nodeBuilder = new NodeBuilder(endpoints,new NodeId("A"));
        Node node = nodeBuilder.build();
        node.start();
    }
}
