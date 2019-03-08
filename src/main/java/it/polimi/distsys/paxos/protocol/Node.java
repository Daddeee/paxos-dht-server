package it.polimi.distsys.paxos.protocol;

import it.polimi.distsys.paxos.protocol.actors.Acceptor;
import it.polimi.distsys.paxos.protocol.actors.Learner;
import it.polimi.distsys.paxos.protocol.actors.Proposer;
import it.polimi.distsys.paxos.communication.Receiver;
import it.polimi.distsys.paxos.communication.Sender;
import it.polimi.distsys.paxos.network.Dispatcher;
import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.messages.Propose;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

public class Node {
    //Actor level
    private Proposer proposer;
    private Acceptor acceptor;
    private Learner learner;

    //Network level
    private Dispatcher dispatcher;
    private Forwarder forwarder;

    //Communication level
    private Receiver receiver;
    private Sender sender;

    public Node(int selfIndex, NodeRef[] all, Consumer<ProposalValue> learnedConsumer) throws IOException {
        NodeRef.setSelf(all[selfIndex]);
        this.receiver = new Receiver();
        this.sender = new Sender();

        this.dispatcher = new Dispatcher(this.receiver);
        this.forwarder = new Forwarder(this.sender);
        Arrays.stream(all).forEach(nodeRef -> this.forwarder.putNode(nodeRef));

        this.proposer = new Proposer(this.forwarder, this.dispatcher.getProposerConsumer());
        this.acceptor = new Acceptor(this.forwarder, this.dispatcher.getAcceptorConsumer());
        this.learner = new Learner(this.forwarder, this.dispatcher.getLearnerConsumer(), learnedConsumer);
    }

    public void propose(ProposalValue value) {
        this.forwarder.broadcast(new Propose(value));
    }
}
