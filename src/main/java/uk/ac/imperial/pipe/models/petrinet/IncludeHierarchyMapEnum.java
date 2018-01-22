package uk.ac.imperial.pipe.models.petrinet;

public enum IncludeHierarchyMapEnum {
    INCLUDE {
        public String getName() {
            return "IncludeMap";
        }
    },
    INCLUDE_ALL {
        public String getName() {
            return "IncludeMapAll";
        }
    },
    INTERFACE_PLACES {
        public String getName() {
            return "InterfacePlaces";
        }
    };

    public abstract String getName();

}
