package uk.ac.imperial.pipe.models.petrinet;

public class BuildUniqueNameCommand extends AbstractIncludeHierarchyCommand<IncludeHierarchy> {

    public static final String BUILD_UNIQUE_NAME = "BuildUniqueNameCommand: ";

    private String name;
    private IncludeHierarchy conflictingInclude;
    private String currentUniqueName;
    private IncludeHierarchy includeHierarchy;
    private int nameType;
    private boolean controller;
    private boolean finished;
    private static final int shortName = 1;
    private static final int minimalName = 2;
    private static final int fullyQualifiedName = 3;

    public BuildUniqueNameCommand() {
        this(true);
        finished = false;
    }

    private BuildUniqueNameCommand(boolean controller) {
        this.controller = controller;
    }

    @Override
    public Result<IncludeHierarchy> execute(IncludeHierarchy includeHierarchy) {
        if (controller) {
            BuildUniqueNameCommand command = null;
            while (!finished) {
                command = new BuildUniqueNameCommand(false);
                Result<IncludeHierarchy> buildResult = includeHierarchy.getRoot().all(command);
                if (!buildResult.hasResult()) {
                    finished = true;
                }
            }
        } else {
            init(includeHierarchy);
            String newUniqueName = buildUniqueName();
            if (newUniqueName != null) {
                updateUniqueNameInSelfAndParentsMaps(includeHierarchy, newUniqueName);
                result.addEntry(newUniqueName, includeHierarchy);
            }
        }
        return result;
    }

    private String buildUniqueName() {
        String uniqueName = null;
        String targetName = buildTargetName();
        uniqueName = (targetName.equals(currentUniqueName)) ? null : targetName;
        return uniqueName;
    }

    protected String buildTargetName() {
        conflictingInclude = includeHierarchy.getRoot().getIncludeMapAll().get(name);
        String targetName = null;
        boolean done = false;
        nameType = shortName;
        while (!done) {
            switch (nameType) {
            case shortName:
                targetName = includeHierarchy.getName();
                if ((noConflict() || self() || parent() || aunt())) {
                    done = true;
                } else {
                    nameType = minimalName;
                }
                break;
            case minimalName:
                if (child()) {
                    targetName = buildMinimalName();
                    done = true;
                } else {
                    nameType = fullyQualifiedName;
                }
                break;
            case fullyQualifiedName:
                if (niece()) {
                    targetName = includeHierarchy.getFullyQualifiedName();
                    done = true;
                } else {
                    throw new RuntimeException(
                            BUILD_UNIQUE_NAME + "logic error in building unique name; no relation found.");
                }
                break;
            }
        }
        return targetName;
    }

    private String buildMinimalName() {
        IncludeHierarchy parent = includeHierarchy.getParent();
        String minimalName = null;
        String tempName = name;
        while (parent != null) {
            tempName = parent.getName() + "." + tempName;
            if (parent.equals(conflictingInclude)) {
                minimalName = tempName;
            }
            parent = parent.getParent();
        }
        if (minimalName == null)
            throw new RuntimeException(BUILD_UNIQUE_NAME +
                    "buildMinimalName logic error; minimal name should not be null.  Temp name: " + tempName);
        return minimalName;
    }

    private boolean noConflict() {
        return (conflictingInclude == null);
    }

    private boolean self() {
        return (includeHierarchy.equals(conflictingInclude));
    }

    private boolean parent() {
        return (conflictingInclude.hasParent(includeHierarchy));
    }

    private boolean child() {
        return (includeHierarchy.hasParent(conflictingInclude));
    }

    private boolean aunt() {
        return (includeHierarchy.higherLevelInHierarchyThanOther(conflictingInclude));
    }

    private boolean niece() {
        return (conflictingInclude.higherLevelInHierarchyThanOther(includeHierarchy));
    }

    private void init(
            IncludeHierarchy includeHierarchy) {
        super.validate(includeHierarchy);
        this.includeHierarchy = includeHierarchy;
        this.name = includeHierarchy.getName();
        this.currentUniqueName = includeHierarchy.getUniqueName();
    }

    private void updateUniqueNameInSelfAndParentsMaps(IncludeHierarchy includeHierarchy, String uniqueName) {
        includeHierarchy.setUniqueName(uniqueName);
        includeHierarchy.buildUniqueNameAsPrefix();
        UpdateMapEntryCommand<IncludeHierarchy> updateCommand = new UpdateMapEntryCommand<IncludeHierarchy>(
                IncludeHierarchyMapEnum.INCLUDE_ALL, currentUniqueName, uniqueName, includeHierarchy, true);
        Result<UpdateResultEnum> updateResult = includeHierarchy.parents(updateCommand);
        updateResult = includeHierarchy.self(updateCommand);
        if (updateResult.hasResult())
            throw new RuntimeException(BUILD_UNIQUE_NAME +
                    "updateUniqueNameInSelfAndParentsMaps probable logic error: \n" + updateResult.getMessage());
    }
}
