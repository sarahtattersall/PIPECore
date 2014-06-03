package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public final class FunctionalRateParameter extends AbstractPetriNetPubSub implements RateParameter {

    private String expression;

    private String id;

    private String name;

    /**
     * Copy constructor
     *
     * @param rateParameter
     */
    public FunctionalRateParameter(FunctionalRateParameter rateParameter) {
        this(rateParameter.expression, rateParameter.id, rateParameter.name);
    }

    public FunctionalRateParameter(String expression, String id, String name) {
        this.expression = expression;
        this.id = id;
        this.name = name;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public RateType getRateType() {
        return RateType.RATE_PARAMETER;
    }

    @Override
    public void setExpression(String expression) {
        String old = this.expression;
        this.expression = expression;
        changeSupport.firePropertyChange(EXPRESSION_CHANGE_MESSAGE, old, expression);
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

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

    @Override
    public String getId() {
        return id;
    }

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

    @Override
    public String toString() {
        return id + ": " + expression;
    }
}
