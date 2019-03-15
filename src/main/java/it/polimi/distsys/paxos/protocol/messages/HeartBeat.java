package it.polimi.distsys.paxos.protocol.messages;

import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.Serializable;

public class HeartBeat extends ProtocolMessage implements Serializable {
    private ProposalNumber promisedNumber;

    public HeartBeat(ProposalNumber promisedNumber) {
        super(NodeRef.getSelf().getId());
        this.promisedNumber = promisedNumber;
    }

    public ProposalNumber getPromisedNumber() {
        return promisedNumber;
    }
}
