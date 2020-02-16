package uk.ac.imperial.pipe.parsers;

import org.antlr.v4.runtime.misc.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Used to determine components that are referenced in the functional expression
 */
public class ComponentListener extends RateGrammarBaseListener {

    /**
     * Component ids that are referenced
     */
    private Set<String> componentIds = new HashSet<>();

    /**
     * When exiting a token_number token we store the id of the token
     * @param ctx token number context 
     */
    @Override
    public void exitToken_number(@NotNull RateGrammarParser.Token_numberContext ctx) {
        componentIds.add(ctx.ID().getText());
    }

    /**
     *
     * @return referenced component ids
     */
    public Set<String> getComponentIds() {
        return componentIds;
    }
}
