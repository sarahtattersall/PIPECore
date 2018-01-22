package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.petrinet.PetriNetComponent;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameter;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;

import java.util.Map;

/**
 * Interface for creating Petri net components via a nice DSL.
 * To create an object a general create method should be used.
 *
 * These Creators largely follow a sort of builder design pattern
 *
 * The {@link uk.ac.imperial.pipe.dsl.APetriNet} can then use this create method
 * to put all components into a {@link uk.ac.imperial.pipe.models.petrinet.PetriNet}
 */
public interface DSLCreator<T extends PetriNetComponent> {

    /**
     *
     * This method will create the relevant petri net component
     * It needs to take all maps of previously created items so that
     * the implementation of DSLCreator can turn component ids into the actual value.
     *
     * E.g. if a Place contains 5 "Red" tokens, the PlaceCreator needs to be able to
     * look up the Token "Red" in tokens.
     *
     * Also the creators should add the created component to the list to be used
     * by following items. For this reason if components depend on others e.g. a Place
     * needs a Token, these items should be created first.
     *
     * A suggested order would be
     * Tokens
     * Places
     * RateParameters
     * Transitions
     * Arcs
     *
     *
     * @param tokens map of created tokens with id of Token
     * @param places map of created places with id of Connectable
     * @param transitions map of created transitions with id of Transition
     * @param rateParameters map of created rateParameters with id of rateParameter
     * @return new {@link PetriNetComponent}
     */
    T create(Map<String, Token> tokens, Map<String, Place> places, Map<String, Transition> transitions,
            Map<String, FunctionalRateParameter> rateParameters);
}
