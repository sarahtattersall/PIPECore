package uk.ac.imperial.pipe.parsers;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class that contains the result of parsing a grammar
 *
 * @param <T> Numerical result type
 */
public final class FunctionalResults<T extends Number> {
    /**
     * Components referenced by the expression used to create this result
     */
    private final Set<String> components;

    /**
     * Errors that occurred whilst evaluating the expression
     */
    private List<String> errors;

    /**
     * Result of evaluating an expression
     * Not valid if contains errors
     */
    private T result;

    public FunctionalResults(T result, Set<String> components) {
        this(result, new LinkedList<String>(), components);
    }

    public FunctionalResults(T result, List<String> errors, Set<String> components) {
        this.result = result;
        this.errors = errors;
        this.components = components;
    }

    /**
     * @return true if any errors were observed during parsing
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * @return list of errors obtained whilst parsing if errors exist, else an empty list is returned
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     *
     * @return result of parsing the grammars. If hasErrors() is true, this result is not valid
     */
    public T getResult() {
        return result;
    }

    /**
     *
     * @return all listed components in the grammar. For example
     *          '#(P0, Default) * 2' returns {P0, Default} as these
     *          represent a place and a token respectively
     */
    public Set<String> getComponents() {
        return components;
    }
}
