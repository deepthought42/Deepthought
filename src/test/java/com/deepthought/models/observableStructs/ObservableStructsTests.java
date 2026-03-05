package com.deepthought.models.observableStructs;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.testng.annotations.Test;

import com.qanairy.observableStructs.ConcurrentNode;
import com.qanairy.observableStructs.ObservableHash;
import com.qanairy.observableStructs.ObservableQueue;

@Test(groups = "Regression")
public class ObservableStructsTests {

    public void observableQueue_supportsQueueOperations() {
        ObservableQueue<String> queue = new ObservableQueue<>();
        assertTrue(queue.add("a"));
        assertTrue(queue.offer("b"));
        assertEquals(queue.size(), 2);
        assertEquals(queue.peek(), "a");
        assertEquals(queue.poll(), "a");
        assertTrue(queue.contains("b"));

        queue.addAll(Arrays.asList("c", "d"));
        assertTrue(queue.remove("c"));
        assertTrue(queue.retainAll(Arrays.asList("b", "d")));
        assertEquals(queue.size(), 2);
        queue.clear();
        assertTrue(queue.isEmpty());
    }

    public void observableHash_putAndGetRandomKey_work() {
        ObservableHash<String, Integer> hash = new ObservableHash<>();
        hash.put("one", 1);
        hash.put("one", 2);

        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        queue.add(3);
        hash.put("two", queue);

        assertEquals(hash.size(), 2);
        assertFalse(hash.isEmpty());
        assertEquals(hash.getQueueHash().get("one").size(), 2);

        Object randomKey = hash.getRandomKey();
        assertTrue("one".equals(randomKey) || "two".equals(randomKey));
    }

    public void concurrentNode_managesInputsOutputsAndData() {
        ConcurrentNode<String> node = new ConcurrentNode<>("root");
        ConcurrentNode<String> input = new ConcurrentNode<>("input");
        ConcurrentNode<String> output = new ConcurrentNode<>("output");
        UUID inputId = UUID.randomUUID();
        UUID outputId = UUID.randomUUID();

        node.addInput(inputId, input);
        node.addOutput(outputId, output);
        node.setData("updated");

        assertEquals(node.getInput(inputId), input);
        assertNotNull(node.getOutputs());
        assertEquals(node.getData(), "updated");
        assertEquals(node.getType(), String.class);
        assertNotNull(node.getUuid());
    }
}
