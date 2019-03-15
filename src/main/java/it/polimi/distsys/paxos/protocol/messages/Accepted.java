package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;
import java.util.List;

public class Accepted extends ProtocolMessage implements Serializable {
    private ProposalNumber acceptedProposalNumber;
    private int acceptedLength;

    public Accepted(ProposalNumber acceptedProposalNumber, int acceptedLength) {
        super(NodeRef.getSelf().getId());
        this.acceptedProposalNumber = acceptedProposalNumber;
        this.acceptedLength = acceptedLength;
    }

    public ProposalNumber getAcceptedProposalNumber() {
        return acceptedProposalNumber;
    }

    public int getAcceptedLength() {
        return acceptedLength;
    }
}
