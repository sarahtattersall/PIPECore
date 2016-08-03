package uk.ac.imperial.pipe.parsers;

import com.google.common.primitives.Doubles;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Parses functional expressions related to the specified Petri net
 */
public class PetriNetWeightParser implements FunctionalWeightParser<Double> {


    /**
     * Petri net to parse results against
     */
    private final PetriNet petriNet;

    /**
     * Evaluator for the PetriNet and functional expression
     */
    private final RateGrammarBaseVisitor<Double> evalVisitor;

    /**
     * Parses Transition Rates to determine their value and
     * the components they reference.
     * @param evalVisitor visitor to perform parsing 
     * @param petriNet to be parsed 
     */
    public PetriNetWeightParser(RateGrammarBaseVisitor<Double> evalVisitor, PetriNet petriNet) {
        this.evalVisitor = evalVisitor;
        this.petriNet = petriNet;
    }


    /**
     *
     * @param parseTree
     * @return components referenced by the functional expression that is being parsed
     */
    //TODO: Use memoization
    private Set<String> getReferencedComponents(ParseTree parseTree) {
        ParseTreeWalker walker = new ParseTreeWalker();
        ComponentListener listener = new ComponentListener();
        walker.walk(listener, parseTree);
        return listener.getComponentIds();
    }



    /**
     *
     * @return true if all referenced components in expression
     * are valid in the Petri net
     */
    private boolean allComponentsInPetriNet(Set<String> components) {
        for (String id : components) {
            if (!petriNet.containsComponent(id)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Evaluate the expression against the given Petri net
     * @param expression to evaluate
     * @return evaluated results which contain error, component and the actual value if no errors were reported
     */
    @Override
    public FunctionalResults<Double> evaluateExpression(String expression) {
        Double maybeDouble = Doubles.tryParse(expression);
        if (maybeDouble != null) {
            return new FunctionalResults<>(maybeDouble, new HashSet<String>());
        }


        RateGrammarErrorListener errorListener = new RateGrammarErrorListener();
        ParseTree parseTree = GrammarUtils.parse(expression, errorListener);

        List<String> errors = new LinkedList<>();
        if (errorListener.hasErrors()) {
            errors.addAll(errorListener.getErrors());
        }

        Set<String> components = getReferencedComponents(parseTree);
        if (!allComponentsInPetriNet(components)) {
            errors.add("Not all referenced components exist in the Petri net!");
        }

        if (!errors.isEmpty()) {
            return new FunctionalResults<>(-1., errors, components);
        }

        Double result = evalVisitor.visit(parseTree);
        if (result < 0) {
            errors.add("Expression result cannot be less than zero!");
            return new FunctionalResults<>(-1., errors, components);
        }

        return new FunctionalResults<>(evalVisitor.visit(parseTree), components);
    }

    /**
     * Listener that registers the id's of Petri net components the parse tree
     * references whilst walking the tree
     */
    static class ComponentListener extends RateGrammarBaseListener {

        private Set<String> componentIds = new HashSet<>();


        @Override
        public void exitToken_number(
                @NotNull
                RateGrammarParser.Token_numberContext ctx) {
            componentIds.add(ctx.ID().getText());
        }

        @Override
        public void exitToken_color_number(RateGrammarParser.Token_color_numberContext ctx) {
            componentIds.add(ctx.ID().get(0).getText());
            componentIds.add(ctx.ID().get(1).getText());
        }

        public Set<String> getComponentIds() {
            return componentIds;
        }
    }
}
