package com.qanairy.observableStructs;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class ObservableQueueTests {

    @Test
    public void testAddPollAndPeek() {
        ObservableQueue<String> queue = new ObservableQueue<>();

        assertTrue(queue.isEmpty());
        assertTrue(queue.add("first"));
        assertTrue(queue.offer("second"));

        assertEquals(2, queue.size());
        assertEquals("first", queue.peek());
        assertEquals("first", queue.poll());
        assertEquals("second", queue.element());
    }

    @Test
    public void testCollectionMutations() {
        ObservableQueue<Integer> queue = new ObservableQueue<>();

        assertTrue(queue.addAll(Arrays.asList(1, 2, 3, 4)));
        assertTrue(queue.containsAll(Arrays.asList(1, 2)));
        assertTrue(queue.remove(Integer.valueOf(3)));
        assertTrue(queue.removeAll(Arrays.asList(1, 4)));
        assertTrue(queue.retainAll(Arrays.asList(2)));

        assertEquals(1, queue.size());
        assertEquals(Integer.valueOf(2), queue.remove());
        assertTrue(queue.isEmpty());

        queue.add(99);
        queue.clear();
        assertTrue(queue.isEmpty());
    }
}
