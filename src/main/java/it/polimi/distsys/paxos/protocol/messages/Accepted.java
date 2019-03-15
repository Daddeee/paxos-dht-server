package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;
import java.util.List;

public class Accepted extends ProtocolMessage implements Serializable {
    private ProposalNumber acceptedProposalNumber;
    private List<ProposalValue> acceptedSequence;

    public Accepted(ProposalNumber acceptedProposalNumber, List<ProposalValue> acceptedSequence) {
        super(NodeRef.getSelf().getId());
        this.acceptedProposalNumber = acceptedProposalNumber;
        this.acceptedSequence = acceptedSequence;
    }

    public ProposalNumber getAcceptedProposalNumber() {
        return acceptedProposalNumber;
    }

    public List<ProposalValue> getAcceptedSequence() {
        return acceptedSequence;
    }
}
