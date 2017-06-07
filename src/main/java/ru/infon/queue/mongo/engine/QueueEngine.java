package ru.infon.queue.mongo.engine;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * 28.03.2017
 * @author KostaPC
 * 2017 Infon ZED
 */
public class QueueEngine<T> implements QueuePacketHolder<T> {

    private static final Log LOG = LogFactory.getLog(QueueEngine.class);

    private final QueueBehave<T> queueBehave;
    private final ExecutorService executor;
    private Map<String, QueueConsumerThread> listenerThreads = new ConcurrentHashMap<>();

    public QueueEngine(
            QueueBehave<T> queueBehave,
            ExecutorService executor
    ) {
        this.queueBehave = queueBehave;
        this.executor = executor;
    }

    public void queue(MessageContainer<T> event) {
        // trying to use sync save while all executions calling this is async
        queueBehave.put(event);
    }

    @Override
    public Collection<MessageContainer<T>> fetch(QueueConsumer<T> consumer) {
        return queueBehave.find(consumer);
    }

    @Override
    public void ack(MessageContainer<T> packet) {
        queueBehave.remove(packet);
    }

    @Override
    public void reset(MessageContainer<T> packet) {
        queueBehave.reset(packet);
    }

    public void registerConsumer(QueueConsumer<T> consumer) {
        if (listenerThreads.containsKey(consumer.getConsumerId())) {
            throw new IllegalStateException("consumer with id \""+consumer.getConsumerId()+"\" already registered");
        }
        QueueConsumerThread<T> consumerThread = new QueueConsumerThread<>(consumer, this, executor);
        listenerThreads.put(consumer.getConsumerId(), consumerThread);
        consumerThread.start();
    }

}