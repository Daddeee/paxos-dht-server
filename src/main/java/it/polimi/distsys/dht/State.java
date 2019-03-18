package it.polimi.distsys.dht;

import it.polimi.distsys.dht.common.Get;
import it.polimi.distsys.dht.common.Put;
import it.polimi.distsys.dht.common.Remove;
import it.polimi.distsys.dht.common.Reply;
import it.polimi.distsys.paxos.network.Forwarder;
import it.polimi.distsys.paxos.protocol.ProposalValue;
import it.polimi.distsys.paxos.protocol.actors.Elector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class State {
    private static final Logger LOGGER = LoggerFactory.getLogger(State.class);
    private HashMap<String, String> storage;
    private Forwarder forwarder;

    public State(Forwarder forwarder) {
        this.storage = new HashMap<>();
        this.forwarder = forwarder;
    }

    public void debugStorage() {
        System.out.println("STORAGE:");
        storage.entrySet().forEach(e -> System.out.println(e.getKey() + "\t:\t" + e.getValue()));
    }

    public void handle(ProposalValue v) {
        if(v instanceof Get)
            handleGet((Get) v);
        else if(v instanceof Put)
            handlePut((Put) v);
        else if(v instanceof Remove)
            handleRemove((Remove) v);
        else LOGGER.error("Unrecognized command");
    }

    private void handleGet(Get g) {
        String s = storage.get(g.getKey());
        if(Elector.getInstance().iAmTheLeader()) {
            Reply r = g.getReply(s, true);
            this.forwarder.send(r, g.getReplyNode());
        }
    }

    private void handlePut(Put p) {
        storage.put(p.getKey(), p.getValue());
        if(Elector.getInstance().iAmTheLeader()) {
            Reply r = p.getReply("", true);
            this.forwarder.send(r, p.getReplyNode());
        }
    }

    private void handleRemove(Remove p) {
        storage.remove(p.getKey());
        if(Elector.getInstance().iAmTheLeader()) {
            Reply r = p.getReply("", true);
            this.forwarder.send(r, p.getReplyNode());
        }
    }
}
