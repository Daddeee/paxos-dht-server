package it.polimi.distsys.paxos.protocol;

import it.polimi.distsys.paxos.protocol.actors.Acceptor;
import it.polimi.distsys.paxos.protocol.messages.Accept;
import it.polimi.distsys.paxos.utils.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Ledger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ledger.class);
    private static Ledger instance;

    private HashMap<String, Object> storage;
    private File savedFile;

    @SuppressWarnings("unchecked")
    public Ledger() {
        try {
            this.savedFile = new File(NodeRef.getSelf().getId() + ".data");
            if(savedFile.createNewFile()) {
                try (ObjectOutputStream writeToFile = new ObjectOutputStream(new FileOutputStream(savedFile))) {
                    this.storage = initEmptyStorage();
                    writeToFile.writeObject(this.storage);
                }
            }

            try (ObjectInputStream readFromFile = new ObjectInputStream(new FileInputStream(savedFile))) {
                this.storage = (HashMap<String, Object>) readFromFile.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        instance = this;
    }

    public HashMap<String, Object> initEmptyStorage() {
        HashMap<String, Object> s = new HashMap<>();
        s.put("va", Collections.synchronizedList(new ArrayList<>()));
        s.put("np", new ProposalNumber(0));
        s.put("na", new ProposalNumber(0));
        return s;
    }

    public void persist() {
        LOGGER.info("Started persisting state.");
        storage.put("va", Acceptor.getInstance().getAcceptedSequence());
        storage.put("np", Acceptor.getInstance().getPromised());
        storage.put("na", Acceptor.getInstance().getAccepted());

        try {
            try (ObjectOutputStream writeToFile = new ObjectOutputStream(new FileOutputStream(savedFile, false))) {
                writeToFile.writeObject(storage);
            }
            LOGGER.info("Completed persisting state.");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void retrieve() {
        LOGGER.info("Started reading state.");
        HashMap<String, Object> s;
        try {
            try (ObjectInputStream readFromFile = new ObjectInputStream(new FileInputStream(savedFile))) {
                s = (HashMap<String, Object>) readFromFile.readObject();
            }
            LOGGER.info("Completed reading state, passing to Acceptor.");
            Acceptor.getInstance().setAccepted((ProposalNumber) s.get("na"));
            Acceptor.getInstance().setPromised((ProposalNumber) s.get("np"));
            Acceptor.getInstance().setAcceptedSequence((List<ProposalValue>) s.get("va"));
            this.storage = s;
            LOGGER.info("Completed.");
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }

    }

    public static Ledger getInstance() {
        return instance;
    }
}
