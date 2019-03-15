package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;
import java.util.List;

public class Accept extends ProtocolMessage implements Serializable {
    private ProposalNumber proposalNumber;
    private List<ProposalValue> proposalSuffix;
    private int offset;

    public Accept(final ProposalNumber proposalNumber, final List<ProposalValue> proposalSuffix, final int offset) {
        super(NodeRef.getSelf().getId());
        this.proposalNumber = proposalNumber;
        this.proposalSuffix = proposalSuffix;
        this.offset = offset;
    }

    public ProposalNumber getProposalNumber() {
        return proposalNumber;
    }

    public List<ProposalValue> getProposalSuffix() {
        return proposalSuffix;
    }

    public int getOffset() {
        return offset;
    }
}
