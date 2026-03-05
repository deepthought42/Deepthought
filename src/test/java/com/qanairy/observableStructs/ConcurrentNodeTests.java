package com.qanairy.observableStructs;

import static org.junit.Assert.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

public class ConcurrentNodeTests {

    @Test
    public void testDefaultConstructorAndSetData() {
        ConcurrentNode<String> node = new ConcurrentNode<>();

        assertNotNull(node.getUuid());
        assertNull(node.getData());

        node.setData("payload");
        assertEquals("payload", node.getData());
    }

    @Test
    public void testAddInputAndOutput() {
        ConcurrentNode<String> node = new ConcurrentNode<>("root");
        ConcurrentNode<String> input = new ConcurrentNode<>("input");
        ConcurrentNode<String> output = new ConcurrentNode<>("output");

        UUID inputId = input.getUuid();
        UUID outputId = output.getUuid();

        node.addInput(inputId, input);
        node.addOutput(outputId, output);

        assertSame(input, node.getInput(inputId));
        assertEquals(1, node.getOutputs().size());
        assertEquals(String.class, node.getType());
    }

    @Test
    public void testConstructorWithMaps() {
        ConcurrentHashMap<UUID, ConcurrentNode<?>> inputs = new ConcurrentHashMap<>();
        ConcurrentHashMap<UUID, ConcurrentNode<?>> outputs = new ConcurrentHashMap<>();
        ConcurrentNode<String> existingInput = new ConcurrentNode<>("in");
        ConcurrentNode<String> existingOutput = new ConcurrentNode<>("out");

        inputs.put(existingInput.getUuid(), existingInput);
        outputs.put(existingOutput.getUuid(), existingOutput);

        ConcurrentNode<String> node = new ConcurrentNode<>(inputs, outputs, "seed");

        assertEquals("seed", node.getData());
        assertEquals(1, node.getOutputs().size());
        assertSame(existingInput, node.getInput(existingInput.getUuid()));
    }
}
