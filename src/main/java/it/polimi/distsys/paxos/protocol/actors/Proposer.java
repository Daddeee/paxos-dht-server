package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.messages.*;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class Proposer extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Proposer.class);
    private static Proposer instance;

    private boolean firstProposal;
    private ProposalNumber nc;
    private List<ProposalValue> vc;
    private ProposalNumber ns;
    private List<ProposalValue> vs;
    private int s;
    private Map<Integer, Integer> a;
    private int lc;
    private int quorum;
    private ProposalValue c;

    public Proposer(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer) {
        super(forwarder, consumer);
        this.firstProposal = true;
        this.nc = new ProposalNumber(0);
        this.vc = new ArrayList<>();
        this.a  = new HashMap<>();
        this.lc = 0;
        this.s = 0;
        this.quorum = forwarder.getNumReceivers() / 2;
        instance = this;
    }

    public void setCurrentProposalNumber(ProposalNumber nc) {
        this.nc = nc;
    }

    public ProposalNumber getCurrentProposalNumber() {
        return this.nc;
    }

    @Override
    public void handle(final ProtocolMessage m) {
        if(m instanceof Propose)
            this.onPropose((Propose) m);
        else if(m instanceof Promise)
            this.onPromise((Promise) m);
        else if(m instanceof Accepted)
            this.onAccepted((Accepted) m);
        else throw new RuntimeException("Unrecognized message.");
    }

    private void onPropose(Propose p) {
        LOGGER.info("Received propose : " + p.getValue());
        if(Elector.getInstance().iAmTheLeader()) {
            LOGGER.info("I'm the leader, handling it.");
            handlePropose(p);
        } else {
            LOGGER.info("I'm not the leader, delegating.");
            forwardToLeader(p);
        }
    }

    private void handlePropose(Propose p) {
        if(firstProposal) {
            LOGGER.info("First proposal, broadcasting prepare.");
            firstProposal = false;
            c = p.getValue();
            nc = nc.inc();
            vc = Learner.getInstance().getDecidedSequence();
            reset();
            forwarder.broadcast(new Prepare(nc, vc.size()));
        } else {
            LOGGER.info("Extending previous proposal, broadcasting accept.");
            c = p.getValue();
            if(!vc.contains(c)) vc.add(c);
            forwarder.broadcast(new Accept(nc, vc));
        }
    }

    private void reset() {
        s = 0;
        resetReceivedLengths();
        lc = 0;
        ns = new ProposalNumber(0);
        vs = new ArrayList<>();
    }

    private void resetReceivedLengths() {
        forwarder.getReceiversIds().forEach(id -> a.put(id, 0));
    }

    private void forwardToLeader(Propose p) {
        this.forwarder.send(p, Elector.getInstance().getLeaderId());
    }

    private void onPromise(Promise p) {
        LOGGER.info("Received promise from " + p.getFrom() + ".");
        ProposalNumber n = p.getPromisedNumber();
        ProposalNumber na = p.getLastAcceptedNumber();
        List<ProposalValue> va = p.getLastAcceptedSuffix();

        if(s > quorum || n.compareTo(nc) != 0) {
            LOGGER.info("Quorum reached or old proposal. Skipping.");
            return;
        }

        if(na.compareTo(ns) > 0 || (na.compareTo(ns) == 0 && va.size() > vs.size())) {
            ns = na;
            vs = va;
        }

        s++;

        if(s > quorum) {
            LOGGER.info("Reached quorum! Broadcasting Accept.");
            vc.addAll(vs);
            if(!vc.contains(c))
                vc.add(c);
            forwarder.broadcast(new Accept(nc, vc));
        }
    }

    private void onAccepted(Accepted acc) {
        LOGGER.info("Received accepted from " + acc.getFrom() + ".");
        ProposalNumber n = acc.getAcceptedProposalNumber();
        int l = acc.getAcceptedLength();

        if(n.compareTo(nc) != 0) {
            LOGGER.info("Old accepted. Skipping.");
            return;
        }

        if(a.get(acc.getFrom()) < l)
            a.put(acc.getFrom(), l);

        if(lc < l && isSupported(l)) {
            lc = l;
            LOGGER.info("Supported sequence, broadcasting Decide.");
            forwarder.broadcast(new Decide(nc, lc));
        }
    }

    private boolean isSupported(int l) {
        long greater = a.values().stream().filter(val -> val >= l).count();
        return greater > quorum;
    }

    public static Proposer getInstance() {
        return instance;
    }
}
