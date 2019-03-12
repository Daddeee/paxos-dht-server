package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;

public class HeartBeat extends ProtocolMessage implements Serializable {
    public HeartBeat() {
        super(NodeRef.getSelf().getId());
    }
}
