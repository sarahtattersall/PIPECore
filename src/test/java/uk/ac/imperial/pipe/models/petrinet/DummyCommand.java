package uk.ac.imperial.pipe.models.petrinet;

public class DummyCommand<T> extends AbstractIncludeHierarchyCommand<T> {

    private int numberMsgs;

    public DummyCommand() {
        this(1);
    }

    public DummyCommand(int numberMsgs) {
        super();
        this.numberMsgs = numberMsgs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<T> execute(IncludeHierarchy includeHierarchy) {
        super.validate(includeHierarchy);
        for (int i = 0; i < numberMsgs; i++) {
            result.addEntry("dummy message for " + includeHierarchy.getPetriNet().getNameValue(), (T) new Integer(i));
        }
        return result;
    }
}
