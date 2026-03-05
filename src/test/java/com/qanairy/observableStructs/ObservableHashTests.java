package com.qanairy.observableStructs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ObservableHashTests {

    @Test
    public void putCreatesAndAppendsQueueValues() {
        ObservableHash<String, Integer> hash = new ObservableHash<String, Integer>();

        ConcurrentLinkedQueue<Integer> firstQueue = hash.put("k", 1);
        ConcurrentLinkedQueue<Integer> secondQueue = hash.put("k", 2);

        assertEquals(1, hash.size());
        assertEquals(2, secondQueue.size());
        assertTrue(firstQueue == secondQueue);
        assertTrue(secondQueue.contains(1));
        assertTrue(secondQueue.contains(2));
    }

    @Test
    public void putAndRandomKeyNotifyAndReturnExistingKey() {
        ObservableHash<String, Integer> hash = new ObservableHash<String, Integer>();
        AtomicInteger updates = new AtomicInteger(0);
        hash.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                updates.incrementAndGet();
            }
        });

        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<Integer>();
        queue.add(10);
        hash.put("alpha", queue);

        assertEquals(1, updates.get());
        Object randomKey = hash.getRandomKey();
        assertNotNull(randomKey);
        assertEquals("alpha", randomKey);
    }
}
