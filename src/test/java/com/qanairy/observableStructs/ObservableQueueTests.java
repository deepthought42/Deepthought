package com.qanairy.observableStructs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ObservableQueueTests {

    @Test
    public void queueOperationsMutateStateAsExpected() {
        ObservableQueue<String> queue = new ObservableQueue<String>();

        assertTrue(queue.isEmpty());
        assertTrue(queue.add("first"));
        assertTrue(queue.offer("second"));
        assertEquals(2, queue.size());
        assertEquals("first", queue.peek());
        assertEquals("first", queue.element());
        assertEquals("first", queue.poll());
        assertEquals("second", queue.remove());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void mutatingOperationsNotifyObservers() {
        ObservableQueue<String> queue = new ObservableQueue<String>();
        AtomicInteger updates = new AtomicInteger(0);
        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                updates.incrementAndGet();
            }
        };
        queue.addObserver(observer);

        queue.add("a");
        queue.offer("b");
        queue.remove("a");
        queue.addAll(Arrays.asList("c", "d"));
        queue.removeAll(Arrays.asList("b"));
        queue.retainAll(Arrays.asList("c"));
        queue.clear();
        queue.poll();

        assertFalse(queue.contains("c"));
        assertEquals(7, updates.get());
    }
}
