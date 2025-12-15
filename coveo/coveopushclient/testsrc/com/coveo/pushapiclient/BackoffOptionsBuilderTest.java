package com.coveo.pushapiclient;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
class BackoffOptionsBuilderTest {
    private BackoffOptionsBuilder backoffOptionsBuilder;

    @BeforeEach
    public void setup() {
        backoffOptionsBuilder = new BackoffOptionsBuilder();
    }

    @Test
    public void testWithDefaultValues() {
        BackoffOptions backoffOptions = backoffOptionsBuilder.build();
        assertEquals(5000, backoffOptions.getRetryAfter());
        assertEquals(10, backoffOptions.getMaxRetries());
        assertEquals(2, backoffOptions.getTimeMultiple());
    }

    @Test
    public void testWithNonDefaultRetryAfter() {
        BackoffOptions backoffOptions = backoffOptionsBuilder.withRetryAfter(1000).build();
        assertEquals(1000, backoffOptions.getRetryAfter());
    }

    @Test
    public void testWithNonDefaultMaxRetries() {
        BackoffOptions backoffOptions = backoffOptionsBuilder.withMaxRetries(15).build();
        assertEquals(15, backoffOptions.getMaxRetries());
    }

    @Test
    public void testWithNonDefaultTimeMultiple() {
        BackoffOptions backoffOptions = backoffOptionsBuilder.withTimeMultiple(3).build();
        assertEquals(3, backoffOptions.getTimeMultiple());
    }
}
