package it.polimi.distsys.paxos.protocol.actors;

import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.ProposalNumber;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.messages.Decide;
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


        if (np.compareTo(n) == 0 && vd.size() <= l) {
            vd = new ArrayList<>(va.subList(0, l));
            learnedConsumer.accept(vd);
            LOGGER.info("Learned.");
        }
    }

    public static Learner getInstance() {
        return instance;
    }
}
