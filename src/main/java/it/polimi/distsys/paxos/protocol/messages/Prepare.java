package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;

public class Prepare extends ProtocolMessage implements Serializable {
    private ProposalNumber proposalNumber;

    public Prepare(final ProposalNumber proposalNumber) {
        super(NodeRef.getSelf().getId());
        this.proposalNumber = proposalNumber;
    }

    public ProposalNumber getProposalNumber() {
        return proposalNumber;
    }
}
