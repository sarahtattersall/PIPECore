package uk.ac.imperial.pipe.models.petrinet.name;

/**
 * Represents a normal Petri net name that is just a String
 */
public final class NormalPetriNetName implements PetriNetName {
    /**
     * Petri net name
     */
    private String name;

    /**
     * Constructor
     * @param name Petri net name
     */
    public NormalPetriNetName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NormalPetriNetName)) {
            return false;
        }

        NormalPetriNetName that = (NormalPetriNetName) o;

        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return Petri net name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Visitor method
     * @param visitor to be visited  
     */
    @Override
    public void visit(NameVisitor visitor) {
        if (visitor instanceof FileNameVisitor) {
            ((NormalNameVisitor) visitor).visit(this);
        }
    }
}
