package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;

public class Election extends ProtocolMessage implements Serializable {
    public Election() {
        super(NodeRef.getSelf().getId());
    }
}
