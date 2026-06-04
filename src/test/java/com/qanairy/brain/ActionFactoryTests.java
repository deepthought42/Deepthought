package com.qanairy.brain;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ActionFactoryTests {

    @Test
    public void getActionsReturnsExpectedOrderedActions() {
        String[] actions = ActionFactory.getActions();

        assertEquals(5, actions.length);
        assertArrayEquals(new String[] {"buy", "sell", "hold", "long", "short"}, actions);
    }
}
