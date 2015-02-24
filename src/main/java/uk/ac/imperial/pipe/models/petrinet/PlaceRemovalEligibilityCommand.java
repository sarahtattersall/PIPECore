package uk.ac.imperial.pipe.models.petrinet;

import java.util.Collection;

public class PlaceRemovalEligibilityCommand extends AbstractIncludeHierarchyCommand<InterfacePlaceAction> {

	private Place place;
	private Place targetPlace;

	public PlaceRemovalEligibilityCommand(Place place) {
		this.place = place; 
	}

	@Override
	public Result<InterfacePlaceAction> execute(IncludeHierarchy includeHierarchy) {
		targetPlace = findCurrentPlaceForHomePlace(includeHierarchy); 
		buildListOfAffectedArcs(includeHierarchy);
		buildListOfAffectedFunctionalExpressions(includeHierarchy);
		return result; 

	}

	private void buildListOfAffectedArcs(IncludeHierarchy includeHierarchy) {
//		Collection<OutboundArc> outboundArcs = includeHierarchy.getPetriNet().inboundArcs(place);
		Collection<OutboundArc> outboundArcs = includeHierarchy.getPetriNet().inboundArcs(targetPlace);
		if (outboundArcs.size() != 0) { 
			for (OutboundArc arc : outboundArcs) {
				result.addEntry(buildMessage(arc.getId(), includeHierarchy, true), 
						new RemovePlaceFunctionalExpressionAction(includeHierarchy, targetPlace, arc.getId()));
//						new RemovePlaceFunctionalExpressionAction(includeHierarchy, place, arc.getId()));
			}
		}
//		Collection<InboundArc> inboundArcs = includeHierarchy.getPetriNet().outboundArcs(place);
		Collection<InboundArc> inboundArcs = includeHierarchy.getPetriNet().outboundArcs(targetPlace);
		if (inboundArcs.size() != 0) { 
			for (InboundArc arc : inboundArcs) {
				result.addEntry(buildMessage(arc.getId(), includeHierarchy, true), 
						new RemovePlaceFunctionalExpressionAction(includeHierarchy, targetPlace, arc.getId()));
			}
		}
		
	}

	private Place findCurrentPlaceForHomePlace(IncludeHierarchy includeHierarchy) {
		Place targetPlace = null; 
		for (Place place : includeHierarchy.getInterfacePlaces()) {
			if (place.getStatus().getMergeInterfaceStatus().getHomePlace().equals(this.place)) {
				targetPlace = place; 
			}
		}
		return targetPlace;
	}

	protected void buildListOfAffectedFunctionalExpressions(IncludeHierarchy includeHierarchy) {
//		Collection<String> references = includeHierarchy.getPetriNet().getComponentsReferencingId(place.getId());
		Collection<String> references = includeHierarchy.getPetriNet().getComponentsReferencingId(targetPlace.getId());
		if (references.size() != 0) { 
			for (String componentId : references) {
				result.addEntry(buildMessage(componentId, includeHierarchy, false), 
//						new RemovePlaceFunctionalExpressionAction(includeHierarchy, place, componentId));
						new RemovePlaceFunctionalExpressionAction(includeHierarchy, targetPlace, componentId));
			}
		}
	}
	private String buildMessage(String componentId, IncludeHierarchy includeHierarchy, boolean arc) {
		StringBuffer sb = new StringBuffer(); 
		sb.append("Place ");
//		sb.append(place.getId());
		sb.append(targetPlace.getId());
		sb.append(" cannot be removed from IncludeHierarchy ");
		sb.append(includeHierarchy.getUniqueName());
		if (arc) sb.append(" because it is referenced in arc ");
		else sb.append(" because it is referenced in a functional expression in component ");
		sb.append(componentId);
		return sb.toString();
	}

}
