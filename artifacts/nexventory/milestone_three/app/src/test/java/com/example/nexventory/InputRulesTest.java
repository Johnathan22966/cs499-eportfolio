package com.example.nexventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class InputRulesTest {
    @Test public void acceptsZeroAndPositiveWholeNumbers() {
        assertEquals(0, InputRules.quantity("0"));
        assertEquals(25, InputRules.quantity("25"));
    }

    @Test public void rejectsNegativeDecimalAndOverflow() {
        for (String value : new String[]{"-1", "2.5", "2147483648", ""}) {
            try { InputRules.quantity(value); fail("Accepted " + value); }
            catch (IllegalArgumentException expected) { /* Expected invalid input. */ }
        }
    }
}
