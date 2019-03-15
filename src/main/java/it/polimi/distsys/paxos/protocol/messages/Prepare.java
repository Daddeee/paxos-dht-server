package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;

public class Prepare extends ProtocolMessage implements Serializable {
    private ProposalNumber proposalNumber;
    private int sequenceLength;

    public Prepare(final ProposalNumber proposalNumber, int sequenceLength) {
        super(NodeRef.getSelf().getId());
        this.proposalNumber = proposalNumber;
        this.sequenceLength = sequenceLength;
    }

    public ProposalNumber getProposalNumber() {
        return proposalNumber;
    }

    public int getSequenceLength() {
        return sequenceLength;
    }
}
