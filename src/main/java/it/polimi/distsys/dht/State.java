package it.polimi.distsys.dht;

import it.polimi.distsys.dht.common.*;
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
        LOGGER.info("Handling command.");
        if(v instanceof Get)
            handleGet((Get) v);
        else if(v instanceof Put)
            handlePut((Put) v);
        else if(v instanceof Remove)
            handleRemove((Remove) v);
        else if(v instanceof Reply)
            LOGGER.info("NO-OP");
        else LOGGER.error("Unrecognized command");
    }

    private void handleGet(Get g) {
        LOGGER.info("Performing GET " + g.getKey());
        String s = storage.get(g.getKey());
        if(Elector.getInstance().iAmTheLeader()) {
            Reply r = g.getReply(s, true);
            this.forwarder.send(r, g.getReplyNode());
        }
    }

    private void handlePut(Put p) {
        LOGGER.info("Performing PUT " + p.getKey() + " : " + p.getValue());
        storage.put(p.getKey(), p.getValue());
        if(Elector.getInstance().iAmTheLeader()) {
            Reply r = p.getReply("", true);
            this.forwarder.send(r, p.getReplyNode());
        }
    }

    private void handleRemove(Remove p) {
        LOGGER.info("Performing REMOVE " + p.getKey());
        storage.remove(p.getKey());
        if(Elector.getInstance().iAmTheLeader()) {
            Reply r = p.getReply("", true);
            this.forwarder.send(r, p.getReplyNode());
        }
    }

    public static String print(DHTMessage m) {
        if(m instanceof Get)
            return "GET " + ((Get) m).getKey();
        else if(m instanceof Put)
            return "PUT " + ((Put) m).getKey() + " : " + ((Put) m).getValue();
        else if(m instanceof Remove)
            return "REMOVE " + ((Remove) m).getKey();
        else return "";
    }
}
