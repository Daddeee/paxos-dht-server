package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;
import java.util.List;

public class Promise extends ProtocolMessage implements Serializable {
    private ProposalNumber promisedNumber;
    private ProposalNumber lastAcceptedNumber;
    private List<ProposalValue> lastAcceptedSuffix;
    private int decidedLength;

    public Promise(final ProposalNumber promisedNumber, final ProposalNumber lastAcceptedNumber, final List<ProposalValue> lastAcceptedSuffix, int decidedLength) {
        super(NodeRef.getSelf().getId());
        this.lastAcceptedNumber = lastAcceptedNumber;
        this.lastAcceptedSuffix = lastAcceptedSuffix;
        this.promisedNumber = promisedNumber;
        this.decidedLength = decidedLength;
    }

    public ProposalNumber getLastAcceptedNumber() {
        return lastAcceptedNumber;
    }

    public List<ProposalValue> getLastAcceptedSuffix() {
        return lastAcceptedSuffix;
    }

    public ProposalNumber getPromisedNumber() {
        return promisedNumber;
    }

    public int getDecidedLength() {
        return decidedLength;
    }
}
