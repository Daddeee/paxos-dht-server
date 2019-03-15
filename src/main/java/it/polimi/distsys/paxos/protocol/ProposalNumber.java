package it.polimi.distsys.paxos.protocol;

import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;

public class ProposalNumber implements Serializable, Comparable<ProposalNumber> {
    private Integer proposalId;
    private Integer proposerId;

    public ProposalNumber(final int proposalId) {
        this.proposalId = proposalId;
        this.proposerId = NodeRef.getSelf().getId();
    }

    public int getProposalId() {
        return proposalId;
    }

    public int getProposerId() {
        return proposerId;
    }

    @Override
    public int compareTo(final ProposalNumber proposalNumber) {
        int first = this.proposalId.compareTo(proposalNumber.getProposalId());
        if(first != 0) return first;
        return this.proposerId.compareTo(proposalNumber.getProposerId());
    }

    public ProposalNumber inc() {
        return new ProposalNumber(proposalId + 1);
    }
}
