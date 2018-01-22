package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

public interface MergeInterfaceStatus extends InterfaceStatus {

    public static final String AWAY = "away";
    public static final String HOME = "home";

    public Place getHomePlace();

    public void setHomePlace(Place homePlace);

    public String getAwayId();

    public void setAwayId(String awayId);

    public void setArcConstraint(ArcConstraint arcConstraint);

    public ArcConstraint getArcConstraint();

    public Result<InterfacePlaceAction> add(PetriNet petriNet);

    public boolean canRemove();

    public String getXmlType();

    public void prefixIdWithQualifiedName(IncludeHierarchy currentIncludeHierarchy);

    public MergeInterfaceStatus copy(PlaceStatus placeStatus);

    public void updateHomePlace(Map<String, Place> pendingNewHomePlaces);

}
