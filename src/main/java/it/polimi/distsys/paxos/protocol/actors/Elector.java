package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.messages.HeartBeat;
import it.polimi.distsys.paxos.protocol.messages.ProtocolMessage;
import it.polimi.distsys.paxos.utils.NodeRef;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

// Using Hirschberg–Sinclair algorithm for leader election could be a possible better solution?
public class Elector extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Elector.class);
    private static Elector instance;
    private static final long HEARTBEAT_RATE_MS = 1000;
    private static final long ELECTION_TIMEOUT_MS = 2*HEARTBEAT_RATE_MS;

    private int leaderId;
    private boolean hasBeated;
    private Timer heartBeatTimer;
    private Timer leaderCheckTimer;

    public Elector(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer) {
        super(forwarder, consumer);
        this.heartBeatTimer = new Timer();
        this.leaderCheckTimer = new Timer();
        this.leaderId = NodeRef.getSelf().getId();
        this.hasBeated = false;
        instance = this;
        leaderCheckTimer.scheduleAtFixedRate(getTimerTask(this::checkLeader), 0, ELECTION_TIMEOUT_MS);
        heartBeatTimer.scheduleAtFixedRate(getTimerTask(this::beat), 0, HEARTBEAT_RATE_MS);

    }

    public int getLeaderId() {
        return this.leaderId;
    }

    public boolean iAmTheLeader() {
        return this.leaderId == NodeRef.getSelf().getId();
    }

    @Override
    public void handle(final ProtocolMessage m) {
        if(m instanceof HeartBeat)
            this.onHeartBeat((HeartBeat) m);
        else throw new RuntimeException("Unrecognized message.");
    }

    private void onHeartBeat(HeartBeat h) {
        if(h.getFrom() > NodeRef.getSelf().getId()) {
            leaderId = h.getFrom();
            this.hasBeated = true;
        }

        if(h.getPromisedNumber().compareTo(Proposer.getInstance().getCurrentProposalNumber()) > 0)
            Proposer.getInstance().setCurrentProposalNumber(h.getPromisedNumber());
    }

    private void beat() {
        this.forwarder.broadcast(new HeartBeat(Acceptor.getInstance().getPromised()));
    }

    private void checkLeader() {
        if(this.hasBeated) {
            LOGGER.info("Checking leader: all good there.");
            this.hasBeated = false;
        } else {
            LOGGER.info("Uh oh, the leader might have failed. I can be the next leader.");
            leaderId = NodeRef.getSelf().getId();
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
