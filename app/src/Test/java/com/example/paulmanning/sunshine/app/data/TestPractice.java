package com.example.paulmanning.sunshine.app.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPractice {
        /*
        This gets run before every test.
     */
    @Before
    public void setItUp() throws Exception {
//        super.setUp();
    }

    @Test
    public void demonstratesThatAssertionsWork() throws Throwable {
        int a = 5;
        int b = 3;
        int c = 5;
        int d = 10;

        assertEquals("X should be equal", a, c);
        assertTrue("Y should be true", d > a);
        assertFalse("Z should be false", a == b);

        if (b > d) {
            fail("XX should never happen");
        }
    }

    @After
    public void tearDown() throws Exception {
//        super.tearDown();
    }
}
