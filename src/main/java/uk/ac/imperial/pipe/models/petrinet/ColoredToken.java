package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import java.awt.Color;

/**
 * Colored token has an id and a color and can be referenced via its id in
 * places and arc weights
 */
public class ColoredToken extends AbstractPetriNetPubSub implements Token {

    /**
     * Unique token id
     */
    private String id;

    /**
     * Unique token color
     */
    private Color color;

    /**
     * Constructor
     * @param id of the token
     * @param color of the token
     */
    public ColoredToken(String id, Color color) {
        this.id = id;
        this.color = color;
    }

    /**
     * Copy constructor
     * @param token to be copied
     */
    public ColoredToken(Token token) {
        this.id = token.getId();
        this.color = token.getColor();
    }

    /**
     *
     * @return tokens unique color
     */
    @Override
    public Color getColor() {
        return color;
    }

    /**
     *
     * Sets the token color, this should be unique to the other token colors
     * but is moderated by the user
     *
     * @param color of the token
     */
    @Override
    public void setColor(Color color) {
        Color old = this.color;
        this.color = color;
        changeSupport.firePropertyChange(COLOR_CHANGE_MESSAGE, old, color);
    }

    /**
     *
     * @return false, tokens do not appear on the canvas
     */
    @Override
    public boolean isSelectable() {
        return false;
    }

    /**
     *
     * @return false, tokens do not appear on the canvas
     */
    @Override
    public boolean isDraggable() {
        return false;
    }

    /**
     * accepts the visitor if it is a {@link uk.ac.imperial.pipe.models.petrinet.TokenVisitor}
     * @param visitor to be accepted
     * @throws PetriNetComponentException if the component does not exist or other logic error
     */
    @Override
    public void accept(PetriNetComponentVisitor visitor) throws PetriNetComponentException {
        if (visitor instanceof TokenVisitor) {
            ((TokenVisitor) visitor).visit(this);
        }
    }

    /**
     *
     * @return token id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     *
     * @param id unique token id
     */
    @Override
    public void setId(String id) {
        String old = this.id;
        this.id = id;
        changeSupport.firePropertyChange(ID_CHANGE_MESSAGE, old, id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + color.hashCode();
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

        ColoredToken token = (ColoredToken) o;

        if (!color.equals(token.color)) {
            return false;
        }
        if (!id.equals(token.id)) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return id of the token
     */
    @Override
    public String toString() {
        return getId();
    }
}
