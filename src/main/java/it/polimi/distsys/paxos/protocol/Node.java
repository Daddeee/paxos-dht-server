package it.polimi.distsys.paxos.protocol;

import it.polimi.distsys.dht.State;
import it.polimi.distsys.paxos.protocol.actors.Acceptor;
import it.polimi.distsys.paxos.protocol.actors.Elector;
import it.polimi.distsys.paxos.protocol.actors.Proposer;
import it.polimi.distsys.paxos.communication.Receiver;
import it.polimi.distsys.paxos.communication.Sender;
import it.polimi.distsys.paxos.network.Dispatcher;
import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.messages.Propose;
import it.polimi.distsys.paxos.utils.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Node {
    private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);
    //Protocol level
    //DHT
    private State state;
    //Actor
    private Proposer proposer;
    private Acceptor acceptor;
    private Elector elector;
    //Ledger
    private Ledger ledger;

    //Network level
    private Dispatcher dispatcher;
    private Forwarder forwarder;

    //Communication level
    private Receiver receiver;
    private Sender sender;

    public Node(int selfIndex, NodeRef[] all) throws IOException {
        NodeRef.setSelf(all[selfIndex]);
        this.receiver = new Receiver();
        this.sender = new Sender();

        this.dispatcher = new Dispatcher(this.receiver);
        this.forwarder = new Forwarder(this.sender, all);

        this.state = new State(this.forwarder);
        this.ledger = new Ledger();
        this.acceptor = new Acceptor(this.forwarder, this.dispatcher.getAcceptorConsumer(), state::handle);
        this.proposer = new Proposer(this.forwarder, this.dispatcher.getProposerConsumer());
        this.elector = new Elector(this.forwarder, this.dispatcher.getElectorConsumer());
        LOGGER.info("NODE " + NodeRef.getSelf().getId() + " UP AND RUNNING");
    }

    public void propose(ProposalValue value) {
        this.forwarder.send(new Propose(value), Elector.getInstance().getLeaderId());
    }

    public void debug() {
        this.state.debugStorage();
    }
}
