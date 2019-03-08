package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;

public class Learn extends ProtocolMessage implements Serializable {
    private ProposalValue value;

    public Learn(final ProposalValue value) {
        super(NodeRef.getSelf().getId());
        this.value = value;
    }

    public ProposalValue getValue() {
        return value;
    }
}
