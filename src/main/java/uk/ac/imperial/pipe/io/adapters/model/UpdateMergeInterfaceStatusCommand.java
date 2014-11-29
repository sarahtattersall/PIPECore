package uk.ac.imperial.pipe.io.adapters.model;

import uk.ac.imperial.pipe.models.petrinet.AbstractIncludeHierarchyCommand;
import uk.ac.imperial.pipe.models.petrinet.IncludeHierarchy;
import uk.ac.imperial.pipe.models.petrinet.InterfacePlaceAction;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Result;

public class UpdateMergeInterfaceStatusCommand extends AbstractIncludeHierarchyCommand<InterfacePlaceAction>  {

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
