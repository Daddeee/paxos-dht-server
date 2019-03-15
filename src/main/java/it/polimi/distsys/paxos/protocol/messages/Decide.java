package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;
import java.util.List;

public class Decide extends ProtocolMessage implements Serializable {
    private ProposalNumber current;
    private int length;

    public Decide(ProposalNumber current, int length) {
        super(NodeRef.getSelf().getId());
        this.current = current;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public ProposalNumber getCurrent() {
        return current;
    }
}
