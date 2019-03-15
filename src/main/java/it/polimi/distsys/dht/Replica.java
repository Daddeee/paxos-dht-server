package it.polimi.distsys.dht;

import com.google.gson.Gson;
import it.polimi.distsys.paxos.protocol.Node;
import it.polimi.distsys.paxos.utils.NodeRef;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Replica {
    public static void main(String [] args) throws IOException {
        NodeRef[] receivers = parseNodeRefs();
        String self = args.length > 0 ? args[0] : null;
        if(self == null) throw new RuntimeException("Need to specify the replica's number as an argument.");
        Node node = new Node(Integer.parseInt(self), receivers, System.out::println);
    }

    private static NodeRef[] parseNodeRefs() throws IOException {
        Gson gson = new Gson();
        String properties = readFile("nodes.json", Charset.defaultCharset());
        TmpNodeRef[] tmp = gson.fromJson(properties, TmpNodeRef[].class);
        NodeRef[] receivers = new NodeRef[tmp.length];
        for(int i = 0; i < tmp.length; i++)
            receivers[i] = new NodeRef(tmp[i].ip, tmp[i].port);
        return receivers;
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private static class TmpNodeRef {
        public String ip;
        public int port;
    }
}
