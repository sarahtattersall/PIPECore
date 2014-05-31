package uk.ac.imperial.pipe.models.petrinet.name;

public final class NormalPetriNetName implements PetriNetName {
    String name;

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

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public NormalPetriNetName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void visit(NameVisitor visitor) {
        if (visitor instanceof FileNameVisitor) {
            ((NormalNameVisitor) visitor).visit(this);
        }
    }
}
