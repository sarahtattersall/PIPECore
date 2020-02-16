package uk.ac.imperial.pipe.models.petrinet;

public abstract class AbstractConnectable extends AbstractPetriNetPubSub implements Connectable {
    /**
     * Connectable position x
     */
    protected double x = 0;

    /**
     * Connectable position y
     */
    protected double y = 0;

    /**
     * Connectable id
     */
    protected String id;

    /**
     * Connectable name
     */
    protected String name;

    /**
     * Connectable name x offset relative to its x coordinate
     */
    protected double nameXOffset = -5;

    /**
     * Connectable name y offset relative to its y coordinate
     */
    protected double nameYOffset = 35;

    protected boolean original = true;

    protected AbstractConnectable(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Copy constructor, makes identical copy
     *
     * @param connectable component to copy
     */
    protected AbstractConnectable(AbstractConnectable connectable) {
        this.id = connectable.id;
        this.name = connectable.name;
        this.x = connectable.x;
        this.y = connectable.y;
        this.nameXOffset = connectable.nameXOffset;
        this.nameYOffset = connectable.nameYOffset;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + id.hashCode();
        result = 31 * result + name.hashCode();
        temp = Double.doubleToLongBits(nameXOffset);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(nameYOffset);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     *
     * @return id of the Petri net component
     */
    @Override
    public final String toString() {
        return id;
    }

    /**
     *
     * @return name offset x component for the name label
     */
    @Override
    public final double getNameXOffset() {
        return nameXOffset;
    }

    /**
     *
     * @param nameXOffset new name label x offset
     */
    @Override
    public final void setNameXOffset(double nameXOffset) {
        double oldValue = this.nameXOffset;
        this.nameXOffset = nameXOffset;
        changeSupport.firePropertyChange(NAME_X_OFFSET_CHANGE_MESSAGE, oldValue, nameXOffset);
    }

    /**
     *
     * @return name offset y component for the name label
     */
    @Override
    public final double getNameYOffset() {
        return nameYOffset;
    }

    /**
     *
     * @param nameYOffset name offset y component for the name label
     */
    @Override
    public final void setNameYOffset(double nameYOffset) {
        double oldValue = this.nameYOffset;
        this.nameYOffset = nameYOffset;
        changeSupport.firePropertyChange(NAME_Y_OFFSET_CHANGE_MESSAGE, oldValue, nameXOffset);
    }

    /**
     *
     * @return name of the Petri net component
     */
    public final String getName() {
        return name;
    }

    /**
     *
     * @param name name of Petri net component
     */
    @Override
    public final void setName(String name) {
        String old = this.name;
        this.name = name;
        changeSupport.firePropertyChange(NAME_CHANGE_MESSAGE, old, name);
    }

    /**
     *
     * @return id of Petri net component
     */
    @Override
    public final String getId() {
        return id;
    }

    /**
     *
     * @param id new unique id of Petri net component
     */
    @Override
    public final void setId(String id) {
        String old = this.id;
        this.id = id;
        changeSupport.firePropertyChange(ID_CHANGE_MESSAGE, old, id);
    }

    /**
     *
     * @return x coordinate of Petri net component
     */
    @Override
    public final int getX() {
        return (int) x;
    }

    /**
     *
     * @param x new x location of Petri net component
     */
    @Override
    public final void setX(int x) {
        double oldValue = this.x;
        this.x = x;
        changeSupport.firePropertyChange(X_CHANGE_MESSAGE, oldValue, x);

    }

    /**
     *
     * @return y coordinate of Petri net component
     */
    @Override
    public final int getY() {
        return (int) y;
    }

    /**
     *
     * @param y new y location of Petri net component
     */
    @Override
    public final void setY(int y) {
        double oldValue = this.y;
        this.y = y;
        changeSupport.firePropertyChange(Y_CHANGE_MESSAGE, oldValue, y);
    }

    @Override
    public boolean equals(Object o) {
        if (!equalsMinimal(o)) {
            return false;
        }

        Connectable connectable = (Connectable) o;

        return equalsStructure(connectable) && equalsPosition(connectable);
    }

    public boolean equalsMinimal(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public <C extends Connectable> boolean equalsStructure(C connectable) {
        if (!equalsMinimal(connectable)) {
            return false;
        }

        if (!id.equals(connectable.getId())) {
            return false;
        }
        if (!name.equals(connectable.getName())) {
            return false;
        }

        return true;
    }

    @Override
    public <C extends Connectable> boolean equalsPosition(C connectable) {
        if (!equalsMinimal(connectable)) {
            return false;
        }

        if (Double.compare(connectable.getX(), getX()) != 0) {
            return false;
        }
        if (Double.compare(connectable.getY(), getY()) != 0) {
            return false;
        }
        if (Double.compare(connectable.getNameXOffset(), nameXOffset) != 0) {
            return false;
        }
        if (Double.compare(connectable.getNameYOffset(), nameYOffset) != 0) {
            return false;
        }
        return true;
    }

}
