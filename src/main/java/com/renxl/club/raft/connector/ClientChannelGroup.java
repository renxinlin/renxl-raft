package com.renxl.club.raft.connector;

import com.renxl.club.raft.core.member.NodeId;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author renxl
 * @Date 2020-08-06 16:13
 * @Version 1.0.0
 */
public class ClientChannelGroup {

    private static  Map<NodeId, Channel> channels = new HashMap();
    public static void add(NodeId nodeId,Channel channel){
        channels.put(nodeId,channel);
    }
    public static Channel remove(NodeId nodeId){
        Channel channel = get(nodeId);
        if(channel!=null) {
            channels.remove(nodeId);
        }
        return channel;
    }

    public static Channel get(NodeId nodeId){
        Channel channel = channels.get(nodeId);
        return channel;
    }


}
