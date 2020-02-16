package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;

public interface PlaceStatus {

    public boolean isMergeStatus();

    public void setMergeStatus(boolean merge);

    public boolean isExternal();

    public void setExternal(boolean external);

    public boolean isInputOnlyArcConstraint();

    public void setInputOnlyArcConstraint(boolean inputOnly);

    public boolean isOutputOnlyArcConstraint();

    public void setOutputOnlyArcConstraint(boolean outputOnly);

    public MergeInterfaceStatus getMergeInterfaceStatus();

    public void setMergeInterfaceStatus(MergeInterfaceStatus interfaceStatus);

    public void setArcConstraint(ArcConstraint arcConstraint);

    public ArcConstraint getArcConstraint();

    public IncludeHierarchy getIncludeHierarchy();

    public void setIncludeHierarchy(IncludeHierarchy includeHierarchy);

    public PlaceStatus copyStatus(Place place);

    public Result<InterfacePlaceAction> update();

    public Place getPlace();

    /**
     * Used in XML marshalling
     * @param place to be marshalled
     */
    public void setPlace(Place place);

    public String getMergeXmlType();

    public void buildMergeStatus(String type);

    public void prefixIdWithQualifiedName(IncludeHierarchy currentIncludeHierarchy);

    public void updateHomePlace(Map<String, Place> pendingNewHomePlaces);

    //TODO implement paintComponent from PlaceView, or equivalent

}
