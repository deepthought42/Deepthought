package com.qanairy.brain;

import static org.junit.Assert.*;

import org.junit.Test;

public class ActionFactoryTests {

    @Test
    public void testGetActions() {
        String[] actions = ActionFactory.getActions();

        assertArrayEquals(new String[] {"buy", "sell", "hold", "long", "short"}, actions);
    }
}
