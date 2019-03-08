package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.messages.*;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Acceptor extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Acceptor.class);
    private ProposalNumber promisedProposalNumber;
    private ProposalNumber lastAcceptedProposalNumber;
    private ProposalValue lastAcceptedProposalValue;

    public Acceptor(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer) {
        super(forwarder, consumer);
        this.promisedProposalNumber = new ProposalNumber(0);
        this.lastAcceptedProposalNumber = null;
        this.lastAcceptedProposalValue = null;
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
        LOGGER.info("Received PREPARE " + p.getProposalNumber().getProposalId() + ":" + p.getProposalNumber().getProposerId());
        if(p.getProposalNumber().compareTo(this.promisedProposalNumber) > 0) {
            this.promisedProposalNumber = p.getProposalNumber();
            Promise pr = new Promise(this.lastAcceptedProposalNumber, this.lastAcceptedProposalValue, this.promisedProposalNumber);
            LOGGER.info("PREPARE is for a new proposal, sending back Promise.");
            this.forwarder.send(pr, p.getFrom());
            LOGGER.info("Done.");
        }
        //TODO nack optimization (?)
    }

    private void onAccept(Accept a) {
        LOGGER.info("Received ACCEPT (" + a.getProposalNumber().getProposalId() + ":" + a.getProposalNumber().getProposerId() + ", " + a.getProposalValue() + ")");
        if(a.getProposalNumber().compareTo(this.promisedProposalNumber) >= 0) {
            LOGGER.info("Proposal is good, accepting it and broadcasting to learners.");
            this.promisedProposalNumber = a.getProposalNumber();
            this.lastAcceptedProposalNumber = a.getProposalNumber();
            this.lastAcceptedProposalValue = a.getProposalValue();
            this.forwarder.broadcast(new Learn(a.getProposalValue()));
            LOGGER.info("Done");
        }
    }
}
