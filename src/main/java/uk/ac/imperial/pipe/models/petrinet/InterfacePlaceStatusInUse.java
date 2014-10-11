package uk.ac.imperial.pipe.models.petrinet;

import java.util.Collection;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;

public class InterfacePlaceStatusInUse implements InterfacePlaceStatus {
	
	private IncludeHierarchy includeHierarchy;
	private InterfacePlace interfacePlace;
	private InterfacePlaceStatus nextStatus;

	public InterfacePlaceStatusInUse(IncludeHierarchy includeHierarchy) {
		this(null, includeHierarchy); 
	}
	public InterfacePlaceStatusInUse(InterfacePlace interfacePlace, IncludeHierarchy includeHierarchy) {
		this.interfacePlace = interfacePlace; 
		this.includeHierarchy = includeHierarchy;
		nextStatus = this; 
	}

	@Override
	public boolean isInUse() {
		return true;
	}

	@Override
	public boolean canUse() {
		return false;
	}

	@Override
	public boolean isHome() {
		return false;
	}

	@Override
	public Result<InterfacePlaceAction> use() {
		return null;
	}

	@Override
	public Result<InterfacePlaceAction> remove() {
		Collection<String> references = includeHierarchy.getPetriNet().getComponentsReferencingId(interfacePlace.getId());
		Result<InterfacePlaceAction> result = new Result<>(); 
		if (references.size() == 0) {
			try {
				includeHierarchy.getPetriNet().removePlace(interfacePlace);
				nextStatus = new InterfacePlaceStatusAvailable(interfacePlace,includeHierarchy);
			} catch (PetriNetComponentException e) {
				e.printStackTrace(); // tested above, so logic error if we throw
			} 
		}
		else {
			// add references to result & test
		}
		return result; 
	}

	@Override
	public InterfacePlace getInterfacePlace() {
		return interfacePlace;
	}

	@Override
	public void setInterfacePlace(InterfacePlace interfacePlace) {
		this.interfacePlace = interfacePlace; 
	}

	@Override
	public IncludeHierarchy getIncludeHierarchy() {
		return includeHierarchy;
	}

	@Override
	public String buildId(String id, String homeName, String awayName) {
		if (awayName == null) awayName = ""; 
		return awayName+".."+homeName+"."+id;
	}


	@Override
	public InterfacePlaceStatus nextStatus() {
		return nextStatus;
	}

}
