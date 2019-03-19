package it.polimi.distsys.paxos.utils;

import java.util.concurrent.BlockingQueue;

public class SynchronousQueueProducer<T> extends QueueProducer<T> {
    public SynchronousQueueProducer(BlockingQueue<T> queue) {
        super(queue);
    }

    @Override
    public void produce(final T m) {
        super.produceObject(m);
    }
}
