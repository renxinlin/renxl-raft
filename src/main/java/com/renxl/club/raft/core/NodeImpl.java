package com.renxl.club.raft.core;

import com.google.common.eventbus.Subscribe;
import com.renxl.club.raft.core.member.Endpoint;
import com.renxl.club.raft.core.member.Member;
import com.renxl.club.raft.core.member.NodeId;
import com.renxl.club.raft.core.message.AppendEntryRequest;
import com.renxl.club.raft.core.message.ElectionRequest;
import com.renxl.club.raft.core.message.ElectionResponse;
import com.renxl.club.raft.core.role.*;
import com.renxl.club.raft.core.scheduled.ElectionTaskFuture;
import com.renxl.club.raft.core.scheduled.LogReplicationFuture;
import com.renxl.club.raft.log.statemachine.StateMachine;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author renxl
 * @Date 2020-08-26 10:49
 * @Version 1.0.0
 */
@Slf4j
public class NodeImpl implements Node {
    private NodeContext nodeContext;

    private Role role;

    public NodeImpl(NodeContext buildContext) {
        this.nodeContext = buildContext;
        nodeContext.getEventBus().register(this);
    }

    public static void main(String[] args) {
        System.out.println(null == null);
    }

    @Override
    public void start() {
        nodeContext.getConnector().initialize();
        nodeContext.getEventBus().register(this);
        becomeFollower(
                new FollowerRole(
                        RoleEnum.FOLLOWER,
                        nodeContext.getNodeStore().getTerm(),
                        startElectionDelayTask(),
                        nodeContext.getNodeStore().getVotedFor(),
                        nodeContext.getNodeStore().getVotedFor()
                )
        );


    }

    private void becomeFollower(FollowerRole followerRole) {
        if (shouldRecord(role, followerRole)) {
            // 将必要的变更持久化
            nodeContext.getNodeStore().setTerm(followerRole.getTerm());
            nodeContext.getNodeStore().setVotedFor(followerRole.getVotedFor());
        }

        role = followerRole;


    }

    private boolean shouldRecord(Role role, Role newRole) {
        try {
            return role != null && newRole != null && role.getTerm() == newRole.getTerm();
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * 调度线程池只是负责时间周期 不负责具体的任务
     *
     * @return
     */
    private ElectionTaskFuture startElectionDelayTask() {
        return nodeContext.getDefaultScheduler().electionTask(this::electionTask);
    }

    /**
     * node线程作为核心线程不负责周期 但负责具体任务
     */
    private void electionTask() {
        /**
         * 选举核心: 选举异步
         */
        nodeContext.getNodeWorker().execute(this::startElectionDelayTaskInNodeWorker);


    }

    private void startElectionDelayTaskInNodeWorker() {
        // 核心线程负责选举

        // 前置校验
        log.info("start election and current role is  [{}]", role);

        role.cancelLogOrElection();

        // term 增加
        int term = role.getTerm();
        term++;

        // 成为候选者
        becomeCandidate(new CandidateRole(startElectionDelayTask(), RoleEnum.CANDIDATE, term, 0));

        // 发送选举消息
        ElectionRequest electionRequest = new ElectionRequest();
        electionRequest.setTerm(term);
        electionRequest.setCandidateId(nodeContext.getSelfId());
        // 日志部分在实现日志相关
        Collection<Member> members = nodeContext.getMemberGroup().getMembers().values();
        List<Endpoint> endpoints = members.stream().map(Member::getEndpoint).collect(Collectors.toList());
        nodeContext.getConnector().sendElectionRequest(electionRequest, endpoints);

    }

    private void becomeCandidate(CandidateRole candidateRole) {

        if (shouldRecord(role, candidateRole)) {
            // 将必要的变更持久化
            nodeContext.getNodeStore().setTerm(candidateRole.getTerm());
            nodeContext.getNodeStore().setVotedFor(nodeContext.getSelfId());
        }

        role = candidateRole;
    }


    @Subscribe
    public void receiveElectionRequest(ElectionRequest electionRequest) {

        this.nodeContext.getNodeWorker().execute(() -> replyElectionRequest(electionRequest));

    }

    private void replyElectionRequest(ElectionRequest electionRequest) {
        electionRequest.getChannel().writeAndFlush(doProcessElectionRequest(electionRequest));


    }

    private ElectionResponse doProcessElectionRequest(ElectionRequest electionRequest) {
        if (electionRequest.getTerm() < role.getTerm()) {
            electionRequest.getChannel().writeAndFlush(new ElectionResponse(false, role.getTerm()));
        }


        // 大于则同意选举
        NodeId votedFor = nodeContext.getNodeStore().getVotedFor();
        if (electionRequest.getTerm() > role.getTerm()) {
            role.cancelLogOrElection();
            becomeFollower(
                    new FollowerRole(
                            RoleEnum.FOLLOWER,
                            electionRequest.getTerm(),
                            startElectionDelayTask(),
                            null,
                            votedFor
                    )
            );

            electionRequest.getChannel().writeAndFlush(new ElectionResponse(true, electionRequest.getTerm()));

        }
        // 等于则需要特殊处理
        if (electionRequest.getTerm() == role.getTerm()) {
            // 无重复投票
            // 一节点一票制

            if (role instanceof FollowerRole) {
                if (votedFor == null || electionRequest.getCandidateId().equals(votedFor)) {
                    //
                    electionRequest.getChannel().writeAndFlush(new ElectionResponse(true, electionRequest.getTerm()));
                }
            }


            if (role instanceof LeaderRole) {
                return new ElectionResponse(false, role.getTerm());

            }


            if (role instanceof CandidateRole) {
                return new ElectionResponse(false, role.getTerm());

            }
            return new ElectionResponse(false, role.getTerm());


        }
        return null;
    }


    @Subscribe
    public void receiveElectionResponse(ElectionResponse response) {
        // 过半选举
        this.nodeContext.getNodeWorker().execute(() -> processResponse(response));
    }


    /**
     * 这里非常复杂:
     * <p>
     * 维度分析
     * 第一:  可能有1-n个候选者参加了选举
     * 虽然加上了随机数种子，降低这种概率，但理论上做不到绝对
     * <p>
     * 第二:  有些候选者的term可能比你大 有些可能比你小 有些可能和你相等
     * 即有些会同意你成为leader 有些不同意  有些不仅不同意 还要求你改你的term
     * <p>
     * 第三:  有时候还会存在节点不稳定 你无法ping通的情况 在生产中，节点中总会有故障概率
     * <p>
     * <p>
     * 所以这里是一个高度并发的环节:
     * 正常来说 发起节点之后 会马上收到并发的请求过来
     * <p>
     * 备注下: 网络层虽然没有顺序 但tcp层总是绝逼顺序 这种顺序不仅仅是tcp协议层面的，由于tcp的队列化以及报文的序列号，其顺带解决了应用层的顺序性
     * <p>
     * <p>
     * <p>
     * <p>
     * 所以最终我们按照raft算法的理论分析
     * 选举时候: 只要你的term大，你就强制别人取你的term
     * 不管你是谁  只有对方term大，你就是follower
     *
     * @param response
     */
    private void processResponse(ElectionResponse response) {
        // 6 7 9 10

        if (response.getTerm() > role.getTerm()) {
            // response role 会在后续发起选举
            role.cancelLogOrElection();
            becomeFollower(new FollowerRole(
                    RoleEnum.FOLLOWER,
                    response.getTerm(),
                    startElectionDelayTask(),
                    null,
                    null
            ));
        }

        if (!(role instanceof CandidateRole)) {
            //  [8] 9 7 7
            return;

        }
        if (!response.getAgreeElection()) {
            // [ 5] 44  8 候选是5 8不同意 依旧可以成为leader
            return;

        }

        CandidateRole candidateRole = (CandidateRole) this.role;
        //
        int size = nodeContext.getMemberGroup().getMembers().size();
        // 过半机制
        int votesCount = candidateRole.getVotesCount();

        // 同意选举
        // 偶数 4 取 3
        if (size / 2 < votesCount) {
            becomeLeader(new LeaderRole(
                    startLogTask(),
                    RoleEnum.LEADER,
                    response.getTerm()
            ));

        } else {
            role.cancelLogOrElection();
            becomeCandidate(new CandidateRole(startElectionDelayTask(), RoleEnum.CANDIDATE, role.getTerm(), votesCount++));
        }


        // check role


    }

    private void becomeLeader(LeaderRole leaderRole) {
        this.role.cancelLogOrElection();
        if (shouldRecord(role, leaderRole)) {
            // 将必要的变更持久化
            nodeContext.getNodeStore().setTerm(leaderRole.getTerm());
            nodeContext.getNodeStore().setVotedFor(nodeContext.getSelfId());
        }
        this.role = leaderRole;

    }

    private LogReplicationFuture startLogTask() {
        return nodeContext.getDefaultScheduler().logReplicate(this::logTask);
    }

    private void logTask() {
        this.nodeContext.getNodeWorker().execute(this::doLogTask);
    }

    private void doLogTask() {
        Collection<Member> members = nodeContext.getMemberGroup().getMembers().values();
        List<Endpoint> endpoints = members.stream().map(Member::getEndpoint).collect(Collectors.toList());
        this.nodeContext.getConnector().sendAppendEntryRequest(new AppendEntryRequest(this.role.getTerm(), null), endpoints);

    }


    @Subscribe
    public void receiveAppendEntryRequest(AppendEntryRequest appendEntryRequest) {

        this.nodeContext.getNodeWorker().execute(() -> replyAppendEntryRequest(appendEntryRequest));

    }

    private void replyAppendEntryRequest(AppendEntryRequest appendEntryRequest) {
        if (appendEntryRequest.getTerm() > role.getTerm()) {
            // response role 会在后续发起选举
            role.cancelLogOrElection();
            becomeFollower(new FollowerRole(
                    RoleEnum.FOLLOWER,
                    appendEntryRequest.getTerm(),
                    startElectionDelayTask(),
                    null,
                    null
            ));
        }


        if (appendEntryRequest.getTerm() < role.getTerm()) {

        }
    }


    @Override
    public void registerStateMachine(@Nonnull StateMachine stateMachine) {

    }

    @Override
    public void appendLog(@Nonnull byte[] commandBytes) {

    }

    @Override
    public void stop() throws InterruptedException {

    }
}
