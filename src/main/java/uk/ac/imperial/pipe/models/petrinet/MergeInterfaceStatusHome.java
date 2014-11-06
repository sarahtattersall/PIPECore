package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.IncludeException;

public class MergeInterfaceStatusHome extends AbstractMergeInterfaceStatus implements MergeInterfaceStatus {


	public MergeInterfaceStatusHome(Place place, PlaceStatus placeStatus) {
		super(place, placeStatus); 
	}



	@Override
	public Result<InterfacePlaceAction> add(IncludeHierarchy includeHierarchy)  {
		buildAwayId(includeHierarchy.getUniqueNameAsPrefix()); 
		IncludeHierarchyCommand<InterfacePlaceAction> command = new AddPlaceStatusCommand(homePlace, placeStatus, includeHierarchy);
		Result<InterfacePlaceAction> result = includeHierarchy.self(command);  
		try {
			result = includeHierarchy.getInterfacePlaceAccessScope().execute(command);
		} catch (IncludeException e) {
			result.addMessage(e.getMessage()); 
			e.printStackTrace();
		}  
		return result;
	}

	@Override
	public Result<InterfacePlaceAction> remove(
			IncludeHierarchy includeHierarchy) {
		return null;
	}

}
