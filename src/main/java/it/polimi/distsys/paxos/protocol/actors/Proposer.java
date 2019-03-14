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

public class Proposer extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Proposer.class);
    private static Proposer instance;
    private int numAcceptors;
    private ProposalNumber currentProposalNumber;
    private ProposalValue currentProposalValue;
    private List<ProposalValue> currentReceivedSequence;
    private int receivedPromises;
    private int receivedAccepted;

    public Proposer(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer) {
        super(forwarder, consumer);
        this.currentProposalNumber = new ProposalNumber(0);
        this.currentProposalValue = null;
        this.currentReceivedSequence = new ArrayList<>();
        this.numAcceptors = this.forwarder.getNumReceivers();
        this.receivedPromises = 0;
        this.receivedAccepted = 0;
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
        else if(m instanceof Accepted)
            this.onAccepted((Accepted) m);
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
        this.currentReceivedSequence = new ArrayList<>();
        this.receivedPromises = 0;
        this.receivedAccepted = 0;
        LOGGER.info("Received proposal " + currentProposalNumber.getProposalId() + ":" + currentProposalNumber.getProposerId() + " .");
        LOGGER.info("Now broadcasting PREPARE.");
        this.forwarder.broadcast(new Prepare(this.currentProposalNumber));
        LOGGER.info("Done.");
    }

    private void onPromise(Promise p) {
        LOGGER.info("Received promise " + p.getPromisedNumber().getProposalId() + ":" + p.getPromisedNumber().getProposerId() +
                    " from " + p.getFrom());
        if(p.getPromisedNumber().compareTo(currentProposalNumber) != 0 || receivedPromises > numAcceptors/2) {
            LOGGER.info(((receivedPromises > numAcceptors/2) ? "Quorum already reached" : "Too old") + ", discarded.");
            return;
        }
        receivedPromises++;
        if(currentReceivedSequence.size() < p.getLastAcceptedSequence().size())
            currentReceivedSequence = p.getLastAcceptedSequence();
        LOGGER.info("Now received " + receivedPromises + " promises, max value: " + currentReceivedSequence);
        if(receivedPromises > numAcceptors/2) {
            LOGGER.info("Quorum reached !! Now broadcasting accept.");
            if(!currentReceivedSequence.contains(currentProposalValue))
                currentReceivedSequence.add(currentProposalValue);
            this.forwarder.broadcast(new Accept(this.currentProposalNumber, this.currentReceivedSequence));
            LOGGER.info("Done.");
        }
    }

    private void onAccepted(Accepted a) {
        LOGGER.info("Received accepted " + a.getAcceptedProposalNumber().getProposalId() + ":"
                + a.getAcceptedProposalNumber().getProposerId() + " from " + a.getFrom());
        if(a.getAcceptedProposalNumber().compareTo(currentProposalNumber) != 0 || receivedAccepted > numAcceptors/2) {
            LOGGER.info(((receivedAccepted > numAcceptors/2) ? "Quorum already reached" : "Too old") + ", discarded.");
            return;
        }

        receivedAccepted++;
        if(receivedAccepted > numAcceptors/2)
            this.forwarder.broadcast(new Learn(currentReceivedSequence));
    }

    public static Proposer getInstance() {
        return instance;
    }
}
