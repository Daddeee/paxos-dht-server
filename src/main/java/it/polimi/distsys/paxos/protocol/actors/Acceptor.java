package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.dht.State;
import it.polimi.distsys.dht.common.DHTMessage;
import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.Ledger;
import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.messages.*;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Acceptor extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Acceptor.class);
    private static Acceptor instance;

    private ProposalNumber np;
    private ProposalNumber na;
    private List<ProposalValue> va;
    private int ld;
    private Consumer<ProposalValue> decisionConsumer;

    public Acceptor(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer, Consumer<ProposalValue> decisionConsumer) {
        super(forwarder, consumer);
        instance = this;
        Ledger.getInstance().retrieve();
        this.decisionConsumer = decisionConsumer;
        this.ld = 0;
        consumer.consume(this::handle);
    }

    @Override
    public void handle(final ProtocolMessage m) {
        if(m instanceof Prepare)
            this.onPrepare((Prepare) m);
        else if(m instanceof Accept)
            this.onAccept((Accept) m);
        else if(m instanceof Decide)
            this.onDecide((Decide) m);
        else throw new RuntimeException("Unrecognized message.");
    }

    public int getDecidedSequenceLength() {
        return ld;
    }

    private void onPrepare(Prepare p) {
        LOGGER.info("Received prepare " + p.getProposalNumber().getProposalId() + ":" + p.getProposalNumber().getProposerId());
        ProposalNumber n = p.getProposalNumber();

        if(np.compareTo(n) < 0) {
            LOGGER.info("This is a new prepare. Promising.");
            np = n;
            Ledger.getInstance().persist();
            List<ProposalValue> suffix = new ArrayList<>(va.subList(p.getSequenceLength(), va.size()));
            forwarder.send(new Promise(np, na, suffix, ld), p.getFrom());
        }
    }

    private void onAccept(Accept a) {
        LOGGER.info("Received Accept " + a.getProposalNumber().getProposalId() + ":" + a.getProposalNumber().getProposerId() +
                " , OFFS: " + a.getOffset() + " , SUFFIX SIZE: " + a.getProposalSuffix().size());
        ProposalNumber n = a.getProposalNumber();
        List<ProposalValue> v = a.getProposalSuffix();
        int offs = a.getOffset();

        if(np.compareTo(n) == 0) {
            LOGGER.info("Accepting same proposal as what i promised.");

            LOGGER.info("Old va size: " + va.size());
            na = n;
            if(offs < va.size())
                va = Collections.synchronizedList(new ArrayList<>(va.subList(0, offs)));
            va.addAll(v);
            Ledger.getInstance().persist();
            LOGGER.info("New va size: " + va.size());

            LOGGER.info("Sending Accepted to Proposer.");
            forwarder.send(new Accepted(n, va.size()), a.getFrom());
        }
    }

    private void onDecide(Decide d) {
        LOGGER.info("Received sequence to learn, length: " + d.getLength());
        ProposalNumber n = d.getCurrent();
        int l = d.getLength();

        LOGGER.info("N   :" + n.getProposalId() + ":" + n.getProposerId());
        LOGGER.info("NP  :" + np.getProposalId() + ":" + n.getProposerId());
        LOGGER.info("LD  :" + ld);
        LOGGER.info("L   :" + l);
        LOGGER.info("|VA|:" + va.size());
        if (np.compareTo(n) == 0 && ld <= l && l <= va.size()) {
            while(ld < l) {
                LOGGER.info("Deciding: " + State.print((DHTMessage) va.get(ld)));
                decisionConsumer.accept(va.get(ld));
                ld++;
            }
        }
        LOGGER.info("Learned.");
    }

    public ProposalNumber getPromised() {
        return np;
    }

    public void setPromised(ProposalNumber np) {
        this.np = np;
    }

    public ProposalNumber getAccepted() {
        return na;
    }

    public void setAccepted(ProposalNumber na) {
        this.na = na;
    }

    public List<ProposalValue> getAcceptedSequence() {
        return va;
    }

    public void setAcceptedSequence(List<ProposalValue> va) {
        this.va = va;
    }

    public static Acceptor getInstance() {
        return instance;
    }
}
