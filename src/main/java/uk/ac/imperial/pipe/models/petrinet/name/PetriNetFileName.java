package uk.ac.imperial.pipe.models.petrinet.name;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Represents a saved file that a Petri net belongs to
 */
public final  class PetriNetFileName implements PetriNetName {
    private File file;

    public PetriNetFileName(File file) {
        this.file = file;
    }

    /**
     *
     * @return absolute path to the Petri net file
     */
    public String getPath() {
        return file.getAbsolutePath();
    }

    /**
     *
     * @return file name minus the path of the Petri net file
     */
    @Override
    public String getName() {
        return FilenameUtils.removeExtension(file.getName());
    }

    @Override
    public void visit(NameVisitor visitor) {
        if (visitor instanceof FileNameVisitor) {
            ((FileNameVisitor) visitor).visit(this);
        }
    }

    /**
     *
     * @return actual file that represents where this Petri net is saved
     */
    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PetriNetFileName)) {
            return false;
        }

        PetriNetFileName that = (PetriNetFileName) o;

        if (!file.equals(that.file)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
