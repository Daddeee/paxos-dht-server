package it.polimi.distsys.paxos.network;

import it.polimi.distsys.paxos.network.messages.NetworkMessage;
import it.polimi.distsys.paxos.network.messages.NetworkMessageType;
import it.polimi.distsys.paxos.protocol.messages.*;
import it.polimi.distsys.paxos.utils.NodeRef;

public class ProtocolMessageConverter {
    public static NetworkMessage convert(ProtocolMessage p, int toId) {
        NetworkMessageType type = getNetworkMessageType(p);
        return new NetworkMessage(NodeRef.getSelf().getId(), toId, type, p);
    }

    private static NetworkMessageType getNetworkMessageType(final ProtocolMessage p) {
        if(p instanceof Propose) return NetworkMessageType.PROPOSE;
        else if(p instanceof Promise) return NetworkMessageType.PROMISE;
        else if(p instanceof Prepare) return NetworkMessageType.PREPARE;
        else if(p instanceof Learn) return NetworkMessageType.LEARN;
        else return NetworkMessageType.ACCEPT;
    }
}
