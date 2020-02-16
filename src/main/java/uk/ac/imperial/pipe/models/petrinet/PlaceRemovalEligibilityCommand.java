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
        if (targetPlace != null) {
            buildListOfAffectedArcs(includeHierarchy);
            buildListOfAffectedFunctionalExpressions(includeHierarchy);
        } else {
            result.addEntry(buildMessage(place.getId(), place
                    .getId(), includeHierarchy, Reason.NOT_FOUND), new RemovePlaceFunctionalExpressionAction(
                            includeHierarchy, place, ""));
        }
        return result;

    }

    private void buildListOfAffectedArcs(IncludeHierarchy includeHierarchy) {
        Collection<OutboundArc> outboundArcs = includeHierarchy.getPetriNet().inboundArcs(targetPlace);
        if (outboundArcs.size() != 0) {
            for (OutboundArc arc : outboundArcs) {
                result.addEntry(buildMessage(targetPlace.getId(), arc
                        .getId(), includeHierarchy, Reason.ARC), new RemovePlaceFunctionalExpressionAction(
                                includeHierarchy, targetPlace, arc.getId()));
            }
        }
        Collection<InboundArc> inboundArcs = includeHierarchy.getPetriNet().outboundArcs(targetPlace);
        if (inboundArcs.size() != 0) {
            for (InboundArc arc : inboundArcs) {
                result.addEntry(buildMessage(targetPlace.getId(), arc
                        .getId(), includeHierarchy, Reason.ARC), new RemovePlaceFunctionalExpressionAction(
                                includeHierarchy, targetPlace, arc.getId()));
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
        Collection<String> references = includeHierarchy.getPetriNet().getComponentsReferencingId(targetPlace.getId());
        if (references.size() != 0) {
            for (String componentId : references) {
                result.addEntry(buildMessage(targetPlace
                        .getId(), componentId, includeHierarchy, Reason.EXPRESSION), new RemovePlaceFunctionalExpressionAction(
                                includeHierarchy, targetPlace, componentId));
            }
        }
    }

    private String buildMessage(String placeId, String componentId, IncludeHierarchy includeHierarchy, Reason reason) {
        StringBuffer sb = new StringBuffer();
        sb.append("Place ");
        sb.append(placeId);
        sb.append(" cannot be removed from IncludeHierarchy ");
        sb.append(includeHierarchy.getUniqueName());
        switch (reason) {
        case ARC:
            sb.append(reason);
            sb.append(componentId);
            break;
        case EXPRESSION:
            sb.append(reason);
            sb.append(componentId);
            break;
        case NOT_FOUND:
            sb.append(reason);
            break;
        }
        return sb.toString();
    }

    private enum Reason {
        ARC {
            @Override
            public String toString() {
                return " because it is referenced in arc ";
            }
        },
        EXPRESSION {
            @Override
            public String toString() {
                return " because it is referenced in a functional expression in component ";
            }
        },
        NOT_FOUND {
            @Override
            public String toString() {
                return " because no MergeInterfaceStatus was found for it in the IncludeHierarchy; probable logic error";
            }

        };
    }
}
