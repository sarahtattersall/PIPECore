package uk.ac.imperial.pipe.models.petrinet;

public class RemoveInterfacePlaceFunctionalExpressionAction implements
		InterfacePlaceAction {

	private String componentId;
	private InterfacePlace interfacePlace;
	private IncludeHierarchy includeHierarchy;

	public RemoveInterfacePlaceFunctionalExpressionAction(
			IncludeHierarchy includeHierarchy, InterfacePlace interfacePlace,
			String componentId) {
		this.includeHierarchy = includeHierarchy; 
		this.interfacePlace = interfacePlace; 
		this.componentId = componentId; 
	}

	@Override
	public String getComponentId() {
		return componentId;
	}

	@Override
	public InterfacePlace getInterfacePlace() {
		return interfacePlace;
	}

	@Override
	public IncludeHierarchy getIncludeHierarchy() {
		return includeHierarchy;
	}

}
