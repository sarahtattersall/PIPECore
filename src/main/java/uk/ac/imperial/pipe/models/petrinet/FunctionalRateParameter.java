package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * Rate parameter that represents a functional expression
 */
public final class FunctionalRateParameter extends AbstractPetriNetPubSub implements RateParameter {

    /**
     * Functional expression that follows the rate grammar
     */
    private String expression;

    /**
     * Rate id
     */
    private String id;

    /**
     * Rate name
     */
    private String name;

    /**
     * Copy constructor
     *
     * @param rateParameter to be copied
     */
    public FunctionalRateParameter(FunctionalRateParameter rateParameter) {
        this(rateParameter.expression, rateParameter.id, rateParameter.name);
    }

    /**
     * Constructor
     * @param expression of the rate parameter
     * @param id of the rate parameter
     * @param name of the rate parameter
     */
    public FunctionalRateParameter(String expression, String id, String name) {
        this.expression = expression;
        this.id = id;
        this.name = name;
    }

    /**
     *
     * @return functional expression
     */
    @Override
    public String getExpression() {
        return expression;
    }

    /**
     *
     * @return type of rate parameter
     */
    @Override
    public RateType getRateType() {
        return RateType.RATE_PARAMETER;
    }

    /**
     *
     * @param expression the new expression for the rate parameter, must conform to the rate grammar
     */
    @Override
    public void setExpression(String expression) {
        String old = this.expression;
        this.expression = expression;
        changeSupport.firePropertyChange(EXPRESSION_CHANGE_MESSAGE, old, expression);
    }

    /**
     *
     * @return false since rate parameters do not appear on the canvas
     */
    @Override
    public boolean isSelectable() {
        return false;
    }

    /**
     *
     * @return false since rate parameters do not appear on the canvas
     */
    @Override
    public boolean isDraggable() {
        return false;
    }

    /**
     * Accepts the visitor if it is a {@link uk.ac.imperial.pipe.models.petrinet.RateParameterVisitor} or
     * {@link uk.ac.imperial.pipe.models.petrinet.FunctionalRateParameterVisitor}
     * @param visitor to be accepted 
     * @throws PetriNetComponentException if component not found or other logic error 
     */
    @Override
    public void accept(PetriNetComponentVisitor visitor) throws PetriNetComponentException {
        if (visitor instanceof RateParameterVisitor) {
            try {
                ((RateParameterVisitor) visitor).visit(this);
            } catch (InvalidRateException e) {
                throw new PetriNetComponentException(e);
            }
        }

        if (visitor instanceof FunctionalRateParameterVisitor) {
            ((FunctionalRateParameterVisitor) visitor).visit(this);
        }
    }

    /**
     *
     * @return rate parameter id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     *
     * @param id new unique id for the rate parameter
     */
    @Override
    public void setId(String id) {
        String old = this.id;
        this.id = id;
        changeSupport.firePropertyChange(ID_CHANGE_MESSAGE, old, id);
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FunctionalRateParameter that = (FunctionalRateParameter) o;

        if (!expression.equals(that.expression)) {
            return false;
        }
        if (!id.equals(that.id)) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return string representation which is the paramters id followed by its functional expression
     */
    @Override
    public String toString() {
        return id + ": " + expression;
    }
}
