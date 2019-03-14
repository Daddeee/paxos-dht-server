package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;

public class Accepted extends ProtocolMessage implements Serializable {
    private ProposalNumber acceptedProposalNumber;

    public Accepted(ProposalNumber acceptedProposalNumber) {
        super(NodeRef.getSelf().getId());
        this.acceptedProposalNumber = acceptedProposalNumber;
    }

    public ProposalNumber getAcceptedProposalNumber() {
        return acceptedProposalNumber;
    }
}
