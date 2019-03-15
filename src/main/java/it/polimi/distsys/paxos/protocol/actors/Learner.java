package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.messages.Decide;
import it.polimi.distsys.paxos.protocol.messages.ProtocolMessage;
import it.polimi.distsys.paxos.utils.QueueConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class Learner extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Learner.class);
    private static Learner instance;

    private int ld;
    private Consumer<ProposalValue> decisionConsumer;

    public Learner(Forwarder forwarder, QueueConsumer<ProtocolMessage> consumer, Consumer<ProposalValue> decisionConsumer) {
        super(forwarder, consumer);
        this.decisionConsumer = decisionConsumer;
        this.ld = 0;

        instance = this;
    }

    public int getDecidedSequenceLength() {
        return ld;
    }

    @Override
    public void handle(final ProtocolMessage m) {
        if(m instanceof Decide)
            this.onDecide((Decide) m);
        else throw new RuntimeException("Unrecognized message");
    }

    private void onDecide(Decide d) {
        LOGGER.info("Received sequence to learn, length: " + d.getLength());
        ProposalNumber n = d.getCurrent();
        ProposalNumber np = Acceptor.getInstance().getPromised();
        int l = d.getLength();
        List<ProposalValue> va = Acceptor.getInstance().getAcceptedSequence();


        if (np.compareTo(n) == 0 && ld <= l) {
            while(ld < l) {
                decisionConsumer.accept(va.get(ld));
                ld++;
            }
            LOGGER.info("Learned.");
        }
    }

    public static Learner getInstance() {
        return instance;
    }
}
