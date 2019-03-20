package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.messages.*;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class Proposer extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Proposer.class);
    private static Proposer instance;

    private boolean firstProposal;
    private ProposalNumber nc;
    private List<ProposalValue> vc;
    private ProposalNumber ns;
    private List<ProposalValue> vs;
    private int promises;
    private Map<Integer, Integer> a;
    private Map<Integer, Integer> s;
    private int lc;
    private int quorum;
    private ProposalValue c;

    public Proposer(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer) {
        super(forwarder, consumer);
        instance = this;
        this.firstProposal = true;
        this.nc = new ProposalNumber(0);
        this.vc = new ArrayList<>();
        this.a  = new HashMap<>();
        this.s = new HashMap<>();
        this.lc = 0;
        this.promises = 0;
        this.quorum = forwarder.getNumReceivers()/2 + 1;
        consumer.consume(this::handle);
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
            firstProposal = false;
            c = p.getValue();
            nc = nc.inc();
            vc = Acceptor.getInstance().getAcceptedSequence().subList(0, Acceptor.getInstance().getDecidedSequenceLength());
            reset();
            LOGGER.info("First proposal, broadcasting prepare " + nc.getProposalId() + ":" + nc.getProposerId() + " L: " + vc.size());
            forwarder.broadcast(new Prepare(nc, vc.size()));
        } else {
            LOGGER.info("Extending previous proposal, broadcasting accept.");
            c = p.getValue();
            if(!vc.contains(c)) vc.add(c);
            List<Integer> ids = s.keySet().stream().filter(k -> s.get(k) != null).collect(Collectors.toList());
            ids.forEach(id -> {
                int t = s.get(id);
                List<ProposalValue> suffix = new ArrayList<>(vc.subList(t, vc.size()));
                forwarder.send(new Accept(nc, suffix, t), id);
            });
        }
    }

    private void reset() {
        promises = 0;
        resetReceivedLengths();
        lc = 0;
        ns = new ProposalNumber(0);
        vs = new ArrayList<>();
    }

    private void resetReceivedLengths() {
        forwarder.getReceiversIds().forEach(id -> {
            a.put(id, 0);
            s.put(id, null);
        });
    }

    private void forwardToLeader(Propose p) {
        this.forwarder.send(p, Elector.getInstance().getLeaderId());
    }

    private void onPromise(Promise p) {
        LOGGER.info("Received promise from " + p.getFrom() + ".");
        ProposalNumber n = p.getPromisedNumber();
        ProposalNumber na = p.getLastAcceptedNumber();
        List<ProposalValue> va = p.getLastAcceptedSuffix();
        int l = p.getDecidedLength();

        if(n.compareTo(nc) != 0) {
            LOGGER.info("Wrong proposal number. Skipping.");
            return;
        }

        s.put(p.getFrom(), l);
        LOGGER.info("s[" + p.getFrom() + "] setted to " + vc.size());

        if(na.compareTo(ns) > 0 || (na.compareTo(ns) == 0 && va.size() > vs.size())) {
            ns = na;
            vs = va;
        }

        promises++;

        if(promises == quorum) {
            LOGGER.info("Reached quorum! Broadcasting Accept.");
            vc.addAll(vs);
            if(!vc.contains(c))
                vc.add(c);

            List<Integer> ids = s.keySet().stream().filter(k -> s.get(k) != null).collect(Collectors.toList());
            ids.forEach(id -> {
                int t = s.get(id);
                List<ProposalValue> suffix = new ArrayList<>(vc.subList(t, vc.size()));
                forwarder.send(new Accept(nc, suffix, t), id);
            });
        } else if(promises > quorum) {
            List<ProposalValue> suffix = new ArrayList<>(vc.subList(l, vc.size()));
            forwarder.send(new Accept(nc, suffix, l), p.getFrom());
            if(lc != 0)
                forwarder.send(new Decide(nc, lc), p.getFrom());
        }
    }

    private void onAccepted(Accepted acc) {
        LOGGER.info("Received accepted from " + acc.getFrom() + ".");
        ProposalNumber n = acc.getAcceptedProposalNumber();
        int l = acc.getAcceptedLength();

        s.put(acc.getFrom(), l);
        LOGGER.info("s[" + acc.getFrom() + "] setted to " + vc.size());

        if(n.compareTo(nc) != 0) {
            LOGGER.info("Old proposal. Skipping.");
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
        return greater >= quorum;
    }

    public static Proposer getInstance() {
        return instance;
    }
}