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

    /**
     * Constructor for results which contain no errors
     * @param result without error
     * @param components without error 
     */
    public FunctionalResults(T result, Set<String> components) {
        this(result, new LinkedList<String>(), components);
    }

    /**
     * Constructor for results which contain errors
     * @param result with errors
     * @param errors found
     * @param components with errors 
     */
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
     * @return result of parsing the grammars. If hasErrors() is true, this result is not valid
     */
    public T getResult() {
        return result;
    }

    /**
     * @return all listed components in the grammar. For example
     * '#(P0, Default) * 2' returns {P0, Default} as these
     * represent a place and a token respectively
     */
    public Set<String> getComponents() {
        return components;
    }

    /**
     * @param seperator string to seperate error messages with. For example "," or "\n"
     * @return errors conjoined by seperator
     */
    public String getErrorString(String seperator) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (String error : errors) {
            builder.append(error);

            i++;
            if (i < errors.size()) {
                builder.append(seperator);
            }
        }
        return builder.toString();
    }
}
