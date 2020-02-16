package uk.ac.imperial.pipe.models.petrinet;

public enum IncludeHierarchyCommandScopeEnum {
    PARENT {
        public IncludeHierarchyCommandScope buildScope(IncludeHierarchy includes) {
            return new ParentCommandScope(includes);
        }
    },
    PARENTS {
        public IncludeHierarchyCommandScope buildScope(IncludeHierarchy includes) {
            return new ParentsCommandScope(includes);
        }
    },
    PARENTS_AND_SIBLINGS {
        public IncludeHierarchyCommandScope buildScope(IncludeHierarchy includes) {
            return new ParentsSiblingsCommandScope(includes);
        }
    },
    ALL {
        public IncludeHierarchyCommandScope buildScope(IncludeHierarchy includes) {
            return new AllCommandScope(includes);
        }
    };

    public abstract IncludeHierarchyCommandScope buildScope(IncludeHierarchy includes);

}
