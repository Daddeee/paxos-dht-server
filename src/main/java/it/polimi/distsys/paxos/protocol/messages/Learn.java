package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;
import java.util.List;

public class Learn extends ProtocolMessage implements Serializable {
    private List<ProposalValue> sequence;

    public Learn(final List<ProposalValue> sequence) {
        super(NodeRef.getSelf().getId());
        this.sequence = sequence;
    }

    public List<ProposalValue> getSequence() {
        return sequence;
    }
}
