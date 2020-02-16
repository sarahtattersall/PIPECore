package uk.ac.imperial.pipe.parsers;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class FunctionalResultsTest {

    @Test
    public void concatenatesErrors() {
        FunctionalResults<Double> results = new FunctionalResults<>(-1., Arrays.asList("a", "b", "c"),
                new HashSet<String>());
        String actual = results.getErrorString(", ");
        String expected = "a, b, c";
        assertEquals(expected, actual);
    }

    @Test
    public void hasErrors() {
        FunctionalResults<Double> results = new FunctionalResults<>(-1., Arrays.asList("a", "b", "c"),
                new HashSet<String>());
        assertTrue(results.hasErrors());
    }

    @Test
    public void hasNoErrors() {
        FunctionalResults<Double> results = new FunctionalResults<>(-1., new HashSet<String>());
        assertFalse(results.hasErrors());
    }

    @Test
    public void hasNoErrorsEmptyErrors() {
        FunctionalResults<Double> results = new FunctionalResults<>(-1., new LinkedList<String>(),
                new HashSet<String>());
        assertFalse(results.hasErrors());
    }

    @Test
    public void getComponents() {

        FunctionalResults<Double> results = new FunctionalResults<>(-1., new HashSet<>(Arrays.asList("a", "b", "c")));
        Collection<String> actual = results.getComponents();
        assertEquals(3, actual.size());
        assertTrue(actual.contains("a"));
        assertTrue(actual.contains("b"));
        assertTrue(actual.contains("c"));
    }

}