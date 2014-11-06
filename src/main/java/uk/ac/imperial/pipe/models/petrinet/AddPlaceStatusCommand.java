package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.PlaceCloner;

public class AddPlaceStatusCommand extends AbstractIncludeHierarchyCommand<InterfacePlaceAction> {

	private PlaceStatus placeStatus;
	private IncludeHierarchy home;
	private Place homePlace;

	public AddPlaceStatusCommand(Place homePlace, PlaceStatus placeStatus, IncludeHierarchy home) {
		this.homePlace = homePlace; 
		this.placeStatus = placeStatus; 
		this.home = home; 
	}

	@Override
	public Result<InterfacePlaceAction> execute(IncludeHierarchy includeHierarchy) {
		if (this.home.equals(includeHierarchy)) {
			addPlaceToInterfacePlaces(homePlace, includeHierarchy);
		}
		else {
			Place place = buildPlaceWithMergeStatusAvailable(includeHierarchy); 
			addPlaceToInterfacePlaces(place, includeHierarchy);
			
		}
		return result;
	}

	private Place buildPlaceWithMergeStatusAvailable(IncludeHierarchy includeHierarchy) {
        PlaceCloner cloner = new PlaceCloner();
        try {
            homePlace.accept(cloner);
        } catch (PetriNetComponentException e) {
        	e.printStackTrace(); 
        }
        Place newPlace = cloner.cloned;
        MergeInterfaceStatus mergeStatus = 
        		new MergeInterfaceStatusAvailable(homePlace, newPlace.getStatus(),  newPlace.getStatus().getMergeInterfaceStatus().getAwayId());  
        newPlace.getStatus().setMergeInterfaceStatus(mergeStatus); 
        newPlace.setId(mergeStatus.getAwayId()); 
        listenForTokenCountChanges(newPlace);
		return newPlace;
	}
	private void listenForTokenCountChanges(Place place) {
		homePlace.addPropertyChangeListener(place); 
		place.addPropertyChangeListener(homePlace); 
	}

	private void addPlaceToInterfacePlaces(Place homePlace, IncludeHierarchy includeHierarchy) {
		IncludeHierarchyCommand<UpdateResultEnum> addCommand = 
				new UpdateMapEntryCommand<Place>(IncludeHierarchyMapEnum.INTERFACE_PLACES,homePlace.getId(), homePlace); 
		Result<UpdateResultEnum> tempResult = addCommand.execute(includeHierarchy); 
		if (tempResult.hasResult()) {
			for (String message : tempResult.getMessages()) {
				result.addMessage(message); 
			}
		}		
	}
}
