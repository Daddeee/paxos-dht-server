package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;

public class Accept extends ProtocolMessage implements Serializable {
    private ProposalNumber proposalNumber;
    private ProposalValue proposalValue;

    public Accept(final ProposalNumber proposalNumber, final ProposalValue proposalValue) {
        super(NodeRef.getSelf().getId());
        this.proposalNumber = proposalNumber;
        this.proposalValue = proposalValue;
    }

    public ProposalNumber getProposalNumber() {
        return proposalNumber;
    }

    public ProposalValue getProposalValue() {
        return proposalValue;
    }
}
