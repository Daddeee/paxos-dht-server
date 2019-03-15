package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.messages.Learn;
import it.polimi.distsys.paxos.protocol.messages.ProtocolMessage;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Learner extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Learner.class);
    private static Learner instance;

    private List<ProposalValue> vd;
    private Consumer<List<ProposalValue>> learnedConsumer;

    public Learner(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer, Consumer<List<ProposalValue>> learnedConsumer) {
        super(forwarder, consumer);
        this.learnedConsumer = learnedConsumer;
        this.vd = new ArrayList<>();

        instance = this;
    }

    public List<ProposalValue> getDecidedSequence() {
        return vd;
    }

    @Override
    public void handle(final ProtocolMessage m) {
        if(m instanceof Learn)
            this.onLearn((Learn) m);
        else throw new RuntimeException("Unrecognized message");
    }

    private void onLearn(Learn l) {
        LOGGER.info("Received sequence to learn, length: " + l.getSequence().size());
        List<ProposalValue> v = l.getSequence();
        if (v.size() > vd.size()) {
            this.vd = v;
            learnedConsumer.accept(v);
            LOGGER.info("Learned.");
        }
    }

    public static Learner getInstance() {
        return instance;
    }
}
