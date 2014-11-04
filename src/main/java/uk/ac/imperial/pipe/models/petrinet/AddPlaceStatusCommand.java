package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.IncludeException;

public class AddPlaceStatusCommand extends AbstractIncludeHierarchyCommand<InterfacePlaceAction> {

	private PlaceStatusInterface placeStatus;
	private IncludeHierarchy home;

	public AddPlaceStatusCommand(PlaceStatusInterface placeStatus, IncludeHierarchy home) {
		this.placeStatus = placeStatus; 
		this.home = home; 
	}

	@Override
	public Result<InterfacePlaceAction> execute(IncludeHierarchy includeHierarchy) {
		
//		InterfacePlace interfacePlace = null; 
		if (includeHierarchy.equals(home)) {
			try {
				result = includeHierarchy.getInterfacePlaceAccessScope().execute(this);
			} catch (IncludeException e) {
				e.printStackTrace();
			} 
		}
		else {
			// PlaceCloner
			// PlaceStatus -- force the MergeStatus ...builder? 	
//			interfacePlace = new DiscreteInterfacePlace((DiscretePlace) place, InterfacePlaceStatusEnum.AVAILABLE.buildStatus(includeHierarchy), home.getUniqueName(), includeHierarchy.getUniqueName()); 
		}
		//TODO replace with updateMapEntryCommand, then delete 
//		boolean added = includeHierarchy.addInterfacePlaceToMap(interfacePlace); 
//		if (!added) {
//			result.addEntry("Unable to add InterfacePlace "+interfacePlace.getId()+" to Include Hierarchy "+
//					includeHierarchy.getFullyQualifiedName()+" because it already exists.", (T) place); 
//		}
		return result;

	}

}
