package com.qanairy.observableStructs;

import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

public class ObservableHashTests {

    @Test
    public void testPutSingleValuesAndRandomKey() {
        ObservableHash<String, Integer> hash = new ObservableHash<>();

        assertTrue(hash.isEmpty());

        hash.put("a", 1);
        hash.put("a", 2);
        hash.put("b", 3);

        assertEquals(2, hash.size());
        assertEquals(Integer.valueOf(1), hash.getQueueHash().get("a").poll());
        assertEquals(Integer.valueOf(2), hash.getQueueHash().get("a").poll());

        Object randomKey = hash.getRandomKey();
        assertTrue("a".equals(randomKey) || "b".equals(randomKey));
    }

    @Test
    public void testPutQueueValue() {
        ObservableHash<String, Integer> hash = new ObservableHash<>();
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        queue.add(10);
        queue.add(20);

        ConcurrentLinkedQueue<Integer> inserted = hash.put("queue", queue);

        assertSame(queue, inserted);
        assertEquals(2, hash.getQueueHash().get("queue").size());
    }
}
