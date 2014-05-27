package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.state.State;

public interface Transition extends Connectable {
    /**
     * Message fired when the Transitions priority changes
     */
    String PRIORITY_CHANGE_MESSAGE = "priority";
    /**
     * Message fired when the rate changes
     */
    String RATE_CHANGE_MESSAGE = "rate";
    /**
     * Message fired when the angle changes
     */
    String ANGLE_CHANGE_MESSAGE = "angle";
    /**
     * Message fired when the transition becomes timed/becomes immediate
     */
    String TIMED_CHANGE_MESSAGE = "timed";
    /**
     * Message fired when the transition becomes an infinite/single server
     */
    String INFINITE_SEVER_CHANGE_MESSAGE = "infiniteServer";
    /**
     * Message fired when the transition is enabled
     */
    String ENABLED_CHANGE_MESSAGE = "enabled";
    /**
     * Message fired when the transition is enabled
     */
    String DISABLED_CHANGE_MESSAGE = "disabled";
    int TRANSITION_HEIGHT = 30;
    int TRANSITION_WIDTH = TRANSITION_HEIGHT / 3;

    int getPriority();

    void setPriority(int priority);

    Rate getRate();

    void setRate(Rate rate);

    /**
     * Evaluate the transitions rate against the given state
     * <p/>
     * If an infinite server the transition will return its rate * enabling degree
     *
     * @param state given state of a petri net to evaluate the functional rate of
     * @return actual evaluated rate of the Petri net
     */
    Double getActualRate(PetriNet petriNet, State state);

    String getRateExpr();

    boolean isInfiniteServer();

    void setInfiniteServer(boolean infiniteServer);

    int getAngle();

    void setAngle(int angle);

    boolean isTimed();

    void setTimed(boolean timed);

    void enable();

    void disable();

    boolean isEnabled();
}
