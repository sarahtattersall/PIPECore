package uk.ac.imperial.pipe.parsers;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Listens for errors in a functional expressions syntax
 */
public final class RateGrammarErrorListener extends BaseErrorListener {
    /**
     * Errors in the functional expression
     */
    private List<String> errors = new LinkedList<>();

    /**
     * Registers a syntax error in this classes collection of errors
     * @param recognizer of syntax error
     * @param offendingSymbol of syntax error
     * @param line of syntax error
     * @param charPositionInLine of syntax error
     * @param msg of syntax error
     * @param e exception of syntax error
     */
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
            int line, int charPositionInLine, String msg,
            RecognitionException e) {
        List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
        Collections.reverse(stack);
        errors.add(String.format("line %d:%d %s", line, charPositionInLine, msg));
    }

    /**
     *
     * @return true if syntax errors were observed during parsing
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     *
     * @return any syntax errors observed during parsing a functional expression
     */
    public List<String> getErrors() {
        return errors;
    }
}
