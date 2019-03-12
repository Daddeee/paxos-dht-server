package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.messages.Election;
import it.polimi.distsys.paxos.protocol.messages.HeartBeat;
import it.polimi.distsys.paxos.protocol.messages.ProtocolMessage;
import it.polimi.distsys.paxos.utils.NodeRef;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class Elector extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Elector.class);
    private static Elector instance;
    private static final long HEARTBEAT_RATE_MS = 10000;
    private static final long ELECTION_TIMEOUT_MS = 2*HEARTBEAT_RATE_MS;

    private static int leaderId = NodeRef.getSelf().getId();
    private static boolean isLeaderAlive = false;

    private boolean hasBeated = false;
    private Timer heartBeatTimer;
    private Timer electionTimer;

    public static int getLeaderId() {
        return leaderId;
    }

    public static boolean iAmTheLeader() {
        return leaderId == NodeRef.getSelf().getId();
    }

    public Elector(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer) {
        super(forwarder, consumer);
        this.heartBeatTimer = new Timer();
        this.electionTimer = new Timer();
        instance = this;

        electionTimer.scheduleAtFixedRate(getTimerTask(this::checkLeader), 0, ELECTION_TIMEOUT_MS);
        heartBeatTimer.scheduleAtFixedRate(getTimerTask(this::beat), 0, HEARTBEAT_RATE_MS);

    }

    @Override
    public void handle(final ProtocolMessage m) {
        if(m instanceof Election)
            this.onElection((Election) m);
        else if(m instanceof HeartBeat)
            this.onHeartBeat((HeartBeat) m);
        else throw new RuntimeException("Unrecognized message.");
    }

    private void onElection(Election e) {
        LOGGER.info("Received election message with id: " + e.getFrom());
        if(e.getFrom() >= leaderId) {
            if(e.getFrom() > leaderId) LOGGER.info("Higher than mine, he can be the leader, and sadly i can't no more.");
            leaderId = e.getFrom();
            isLeaderAlive = true;
        } else if(!isLeaderAlive){
            LOGGER.info("Lower than mine. I don't know an alive leader and i might be the one. Telling others.");
            isLeaderAlive = true;
            this.forwarder.broadcast(new Election());
        } else {
            LOGGER.info("Lower than mine. I know an alive leader. Discard.");
        }
    }

    private void onHeartBeat(HeartBeat h) {
        LOGGER.info("Ping by " + h.getFrom());
        this.hasBeated = true;
    }

    private void beat() {
        if(iAmTheLeader()) {
            LOGGER.info("Hey, i'm the leader. Pinging others.");
            this.forwarder.broadcast(new HeartBeat());
        }
    }

    private void checkLeader() {
        if(this.hasBeated) {
            LOGGER.info("Checking leader: all good there.");
            this.hasBeated = false;
        } else {
            LOGGER.info("Uh oh, the leader might have failed. I can be the next leader, better start an election.");
            leaderId = NodeRef.getSelf().getId();
            isLeaderAlive = false;
            this.forwarder.broadcast(new Election());
        }
    }

    private TimerTask getTimerTask(Runnable task) {
        return new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };
    }

    public static Elector getInstance() {
        return instance;
    }
}
