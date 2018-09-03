package uk.ac.imperial.pipe.models.petrinet;

public class UpdateMergeInterfaceStatusCommand extends AbstractIncludeHierarchyCommand<InterfacePlaceAction> {

    @Override
    public Result<InterfacePlaceAction> execute(IncludeHierarchy includeHierarchy) {
        Result<InterfacePlaceAction> resultOne;
        for (Place place : includeHierarchy.getPetriNet().getPlaces()) {
            resultOne = place.getStatus().getMergeInterfaceStatus().add(includeHierarchy);
            if (resultOne.hasResult()) {
                result.addResult(resultOne);
            }
        }
        return result;
    }

}
