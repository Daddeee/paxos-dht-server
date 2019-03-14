package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;
import java.util.List;

public class Accept extends ProtocolMessage implements Serializable {
    private ProposalNumber proposalNumber;
    private List<ProposalValue> proposalSequence;

    public Accept(final ProposalNumber proposalNumber, final List<ProposalValue> proposalSequence) {
        super(NodeRef.getSelf().getId());
        this.proposalNumber = proposalNumber;
        this.proposalSequence = proposalSequence;
    }

    public ProposalNumber getProposalNumber() {
        return proposalNumber;
    }

    public List<ProposalValue> getProposalSequence() {
        return proposalSequence;
    }
}
