package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.messages.Learn;
import it.polimi.distsys.paxos.protocol.messages.ProtocolMessage;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class Learner extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Learner.class);
    private Consumer<ProposalValue> learnedConsumer;

    public Learner(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer, Consumer<ProposalValue> learnedConsumer) {
        super(forwarder, consumer);
        this.learnedConsumer = learnedConsumer;
    }

    @Override
    public void handle(final ProtocolMessage m) {
        if(m instanceof Learn)
            this.onLearn((Learn) m);
        else throw new RuntimeException("Unrecognized message");
    }

    private void onLearn(Learn l) {
        LOGGER.info("Received value to learn: " + l.getValue());
        learnedConsumer.accept(l.getValue());
        LOGGER.info("Learned.");
    }
}
