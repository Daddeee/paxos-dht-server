package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;

public class Promise extends ProtocolMessage implements Serializable {
    private ProposalNumber lastAcceptedNumber;
    private ProposalValue lastAcceptedValue;
    private ProposalNumber promisedNumber;

    public Promise(final ProposalNumber lastAcceptedNumber, final ProposalValue lastAcceptedValue, final ProposalNumber promisedNumber) {
        super(NodeRef.getSelf().getId());
        this.lastAcceptedNumber = lastAcceptedNumber;
        this.lastAcceptedValue = lastAcceptedValue;
        this.promisedNumber = promisedNumber;
    }

    public ProposalNumber getLastAcceptedNumber() {
        return lastAcceptedNumber;
    }

    public ProposalValue getLastAcceptedValue() {
        return lastAcceptedValue;
    }

    public ProposalNumber getPromisedNumber() {
        return promisedNumber;
    }
}
