package uk.ac.imperial.pipe.io;

public enum XmlFileEnum {

    PETRI_NET {
        public String toString() {
            return "Petri net";
        }
    },
    INCLUDE_HIERARCHY {
        public String toString() {
            return "include hierarchy";
        }
    };

}
