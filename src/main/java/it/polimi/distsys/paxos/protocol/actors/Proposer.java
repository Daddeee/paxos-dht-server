package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.messages.*;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Proposer extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Proposer.class);
    private static Proposer instance;
    private int numAcceptors;
    private ProposalNumber currentProposalNumber;
    private ProposalValue currentProposalValue;
    private ProposalValue currentReceivedValue;
    private int receivedAccept;
    private boolean quorumReached;

    public Proposer(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer) {
        super(forwarder, consumer);
        this.currentProposalNumber = new ProposalNumber(0);
        this.currentProposalValue = null;
        this.currentReceivedValue = null;
        this.numAcceptors = this.forwarder.getNumReceivers();
        this.receivedAccept = 0;
        this.quorumReached = false;
        instance = this;
    }

    public ProposalNumber getCurrentProposalNumber() {
        return currentProposalNumber;
    }

    public void setCurrentProposalNumber(final ProposalNumber currentProposalNumber) {
        this.currentProposalNumber = currentProposalNumber;
    }

    @Override
    public void handle(final ProtocolMessage m) {
        if(m instanceof Propose)
            this.onPropose((Propose) m);
        else if(m instanceof Promise)
            this.onPromise((Promise) m);
        else throw new RuntimeException("Unrecognized message.");
    }

    private void onPropose(Propose p) {
        if(Elector.iAmTheLeader())
            handleProposal(p);
        else
            this.forwarder.send(p, Elector.getLeaderId());
    }

    private void handleProposal(Propose p) {
        this.currentProposalNumber = new ProposalNumber(this.currentProposalNumber.getProposalId() + 1);
        this.currentProposalValue = p.getValue();
        this.currentReceivedValue = null;
        this.receivedAccept = 0;
        this.quorumReached = false;
        LOGGER.info("Received proposal " + currentProposalNumber.getProposalId() + ":" + currentProposalNumber.getProposerId() + " .");
        LOGGER.info("Now broadcasting PREPARE.");
        this.forwarder.broadcast(new Prepare(this.currentProposalNumber));
        LOGGER.info("Done.");
    }

    private void onPromise(Promise p) {
        LOGGER.info("Received promise " + p.getPromisedNumber().getProposalId() + ":" + p.getPromisedNumber().getProposerId() +
                    " from " + p.getFrom());
        if(p.getPromisedNumber().compareTo(currentProposalNumber) < 0 || this.quorumReached) {
            LOGGER.info((quorumReached ? "Quorum already reached" : "Too old") + ", discarded.");
            return;
        }
        receivedAccept++;
        if(p.getLastAcceptedValue() != null)
            if(currentReceivedValue == null || currentReceivedValue.compareTo(p.getLastAcceptedValue()) < 0)
                currentReceivedValue = p.getLastAcceptedValue();
        LOGGER.info("Now received " + receivedAccept + " promises, max value: " + currentReceivedValue);
        if(receivedAccept > numAcceptors/2) {
            LOGGER.info("Quorum reached !! Now broadcasting accept.");
            this.quorumReached = true;
            if(currentReceivedValue != null)
                currentProposalValue = currentReceivedValue;
            this.forwarder.broadcast(new Accept(this.currentProposalNumber, this.currentProposalValue));
            LOGGER.info("Done.");
        }
    }

    public static Proposer getInstance() {
        return instance;
    }
}
