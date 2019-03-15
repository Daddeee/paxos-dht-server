package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.messages.*;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Acceptor extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Acceptor.class);
    private static Acceptor instance;

    private ProposalNumber np;
    private ProposalNumber na;
    private List<ProposalValue> va;

    public Acceptor(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer) {
        super(forwarder, consumer);
        this.np = new ProposalNumber(0);
        this.na = new ProposalNumber(0);
        this.va = new ArrayList<>();
        instance = this;
    }

    @Override
    public void handle(final ProtocolMessage m) {
        if(m instanceof Prepare)
            this.onPrepare((Prepare) m);
        else if(m instanceof Accept)
            this.onAccept((Accept) m);
        else throw new RuntimeException("Unrecognized message.");
    }

    private void onPrepare(Prepare p) {
        LOGGER.info("Received prepare " + p.getProposalNumber().getProposalId() + ":" + p.getProposalNumber().getProposerId());
        ProposalNumber n = p.getProposalNumber();

        if(np.compareTo(n) < 0) {
            LOGGER.info("This is a new prepare. Promising.");
            np = n;
            forwarder.send(new Promise(np, na, va), p.getFrom());
        }
    }

    private void onAccept(Accept a) {
        LOGGER.info("Received Accept " + a.getProposalNumber().getProposalId() + ":" + a.getProposalNumber().getProposerId());
        ProposalNumber n = a.getProposalNumber();
        List<ProposalValue> v = a.getProposalSequence();

        if(np.compareTo(n) <= 0) {
            LOGGER.info("Accepting same proposal as what i promised.");
            np = n;
            if(n.compareTo(na) > 0 || (n.compareTo(na) == 0 && v.size() > va.size())) {
                na = n;
                va = v;
            }
            LOGGER.info("Sending Accepted to Proposer.");
            forwarder.send(new Accepted(n, va), a.getFrom());
        }
    }

    public ProposalNumber getPromised() {
        return np;
    }

    public static Acceptor getInstance() {
        return instance;
    }
}
