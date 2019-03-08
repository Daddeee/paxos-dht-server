package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.network.messages.NetworkMessage;
import it.polimi.distsys.paxos.protocol.messages.ProtocolMessage;
import it.polimi.distsys.paxos.utils.QueueConsumer;

public abstract class AbstractActor {
    protected Forwarder forwarder;
    protected QueueConsumer<ProtocolMessage> consumer;

    public AbstractActor(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer) {
        this.forwarder = forwarder;
        this.consumer = consumer;
        consumer.consume(this::handle);
    }

    public abstract void handle(ProtocolMessage m);
}
