package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;
import java.util.List;

public class Promise extends ProtocolMessage implements Serializable {
    private ProposalNumber lastAcceptedNumber;
    private List<ProposalValue> lastAcceptedSequence;
    private ProposalNumber promisedNumber;

    public Promise(final ProposalNumber lastAcceptedNumber, final List<ProposalValue> lastAcceptedSequence, final ProposalNumber promisedNumber) {
        super(NodeRef.getSelf().getId());
        this.lastAcceptedNumber = lastAcceptedNumber;
        this.lastAcceptedSequence = lastAcceptedSequence;
        this.promisedNumber = promisedNumber;
    }

    public ProposalNumber getLastAcceptedNumber() {
        return lastAcceptedNumber;
    }

    public List<ProposalValue> getLastAcceptedSequence() {
        return lastAcceptedSequence;
    }

    public ProposalNumber getPromisedNumber() {
        return promisedNumber;
    }
}
