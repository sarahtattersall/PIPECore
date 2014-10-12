package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;

public class InterfacePlaceStatusInUse extends AbstractInterfacePlaceStatus implements InterfacePlaceStatus {
	
	public InterfacePlaceStatusInUse(IncludeHierarchy includeHierarchy) {
		super(includeHierarchy); 
	}
	public InterfacePlaceStatusInUse(InterfacePlace interfacePlace, IncludeHierarchy includeHierarchy) {
		super(interfacePlace, includeHierarchy); 
	}

	@Override
	public boolean isInUse() {
		return true;
	}

	@Override
	public Result<InterfacePlaceAction> use() {
		return null;
	}

	@Override
	public Result<InterfacePlaceAction> remove() {
		Result<InterfacePlaceAction> result = includeHierarchy.self(new InterfacePlaceRemovalEligibilityCommand(this.getInterfacePlace())); 
		if (!result.hasResult()) {
			try {
				includeHierarchy.getPetriNet().removePlace(interfacePlace);
				nextStatus = new InterfacePlaceStatusAvailable(interfacePlace,includeHierarchy);
			} catch (PetriNetComponentException e) {
				e.printStackTrace(); // tested in InterfacePlaceRemovalEligibilityCommand(...), so logic error if we throw
			} 
		}
		return result; 
	}

}
