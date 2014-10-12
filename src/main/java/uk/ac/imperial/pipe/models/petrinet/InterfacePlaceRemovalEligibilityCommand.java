package uk.ac.imperial.pipe.models.petrinet;

import java.util.Collection;

public class InterfacePlaceRemovalEligibilityCommand extends AbstractIncludeHierarchyCommand<InterfacePlaceAction> {

	private InterfacePlace interfacePlace;

	public InterfacePlaceRemovalEligibilityCommand(InterfacePlace interfacePlace) {
		this.interfacePlace = interfacePlace; 
	}

	@Override
	public Result<InterfacePlaceAction> execute(IncludeHierarchy includeHierarchy) {
		Collection<String> references = includeHierarchy.getPetriNet().getComponentsReferencingId(interfacePlace.getId());
		if (references.size() != 0) { 
			for (String componentId : references) {
				result.addEntry(buildMessage(componentId, includeHierarchy), 
						new RemoveInterfacePlaceFunctionalExpressionAction(includeHierarchy, interfacePlace, componentId));
			}
		}
		return result; 

	}
	private String buildMessage(String componentId, IncludeHierarchy includeHierarchy) {
		StringBuffer sb = new StringBuffer(); 
		sb.append("InterfacePlace ");
		sb.append(interfacePlace.getId());
		sb.append(" cannot be removed from IncludeHierarchy ");
		sb.append(includeHierarchy.getUniqueName());
		sb.append(" because it is referenced in a functional expression in component ");
		sb.append(componentId);
		return sb.toString();
	}

}
