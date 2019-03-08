package it.polimi.distsys.paxos.network;

import it.polimi.distsys.paxos.communication.messages.CommunicationMessage;
import it.polimi.distsys.paxos.communication.Receiver;
import it.polimi.distsys.paxos.network.messages.NetworkMessage;
import it.polimi.distsys.paxos.protocol.messages.*;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import it.polimi.distsys.paxos.utils.QueueProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Dispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);
    private QueueConsumer<CommunicationMessage> recvConsumer;
    private BlockingQueue<ProtocolMessage> proposerQueue;
    private BlockingQueue<ProtocolMessage> acceptorQueue;
    private BlockingQueue<ProtocolMessage> learnerQueue;
    private QueueProducer<ProtocolMessage> proposerQueueProducer;
    private QueueProducer<ProtocolMessage> acceptorQueueProducer;
    private QueueProducer<ProtocolMessage> learnerQueueProducer;

    public Dispatcher(Receiver receiver) {
        this.recvConsumer = receiver.getRecvConsumer();
        this.recvConsumer.consume(this::dispatch);

        this.proposerQueue = new LinkedBlockingQueue<>();
        this.acceptorQueue = new LinkedBlockingQueue<>();
        this.learnerQueue = new LinkedBlockingQueue<>();
        this.proposerQueueProducer = new QueueProducer<>(this.proposerQueue);
        this.acceptorQueueProducer = new QueueProducer<>(this.acceptorQueue);
        this.learnerQueueProducer = new QueueProducer<>(this.learnerQueue);
    }

    public QueueConsumer<ProtocolMessage> getProposerConsumer() {
        return new QueueConsumer<>(this.proposerQueue);
    }

    public QueueConsumer<ProtocolMessage> getAcceptorConsumer() {
        return new QueueConsumer<>(this.acceptorQueue);
    }

    public QueueConsumer<ProtocolMessage> getLearnerConsumer() {
        return new QueueConsumer<>(this.learnerQueue);
    }

    private void dispatch(CommunicationMessage m) {
        NetworkMessage message = (NetworkMessage) m.getBody();

        switch (message.getType()) {
            case PROPOSE:
                this.proposerQueueProducer.produce((Propose) message.getBody());
                break;
            case PREPARE:
                this.acceptorQueueProducer.produce((Prepare) message.getBody());
                break;
            case PROMISE:
                this.proposerQueueProducer.produce((Promise) message.getBody());
                break;
            case ACCEPT:
                this.acceptorQueueProducer.produce((Accept) message.getBody());
                break;
            case LEARN:
                this.learnerQueueProducer.produce((Learn) message.getBody());
                break;
            default:
                LOGGER.error("Unrecognized message");
        }
    }
}
