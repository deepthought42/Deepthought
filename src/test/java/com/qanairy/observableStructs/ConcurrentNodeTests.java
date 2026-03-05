package com.qanairy.observableStructs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ConcurrentNodeTests {

    @Test
    public void constructorsAndDataAccessorsBehaveAsExpected() {
        ConcurrentNode<String> node = new ConcurrentNode<String>("seed");

        assertNotNull(node.getUuid());
        assertEquals("seed", node.getData());
        assertEquals(String.class, node.getType());

        node.setData("updated");
        assertEquals("updated", node.getData());
    }

    @Test
    public void addInputAndOutputStoreNodesAndNotifyObservers() {
        ConcurrentNode<String> node = new ConcurrentNode<String>("center");
        ConcurrentNode<String> inputNode = new ConcurrentNode<String>("input");
        ConcurrentNode<String> outputNode = new ConcurrentNode<String>("output");
        AtomicInteger updates = new AtomicInteger(0);

        node.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                updates.incrementAndGet();
            }
        });

        UUID inputId = UUID.randomUUID();
        UUID outputId = UUID.randomUUID();

        node.addInput(inputId, inputNode);
        node.addOutput(outputId, outputNode);

        assertEquals(2, updates.get());
        assertEquals(inputNode, node.getInput(inputId));
        assertEquals(outputNode, node.getOutputs().get(outputId));
        assertNull(node.getOutput(outputNode));
    }

    @Test
    public void mapBasedConstructorUsesProvidedMaps() {
        ConcurrentHashMap<UUID, ConcurrentNode<?>> inputs = new ConcurrentHashMap<UUID, ConcurrentNode<?>>();
        ConcurrentHashMap<UUID, ConcurrentNode<?>> outputs = new ConcurrentHashMap<UUID, ConcurrentNode<?>>();
        ConcurrentNode<Integer> node = new ConcurrentNode<Integer>(inputs, outputs, 42);

        assertEquals(Integer.class, node.getType());
        assertEquals(Integer.valueOf(42), node.getData());
        assertEquals(outputs, node.getOutputs());
    }
}
