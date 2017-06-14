package uk.ac.imperial.pipe.models.petrinet;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

/**
 * This class maps to the Place in PNML and has a discrete number of tokens
 */
public  class DiscretePlace extends AbstractConnectable implements Place {

    /**
     * Marking x offset relative to the place x coordinate
     */
    private double markingXOffset = 0;

    /**
     * Marking y offset relative to the place y coordinate
     */
    private double markingYOffset = 0;

    /**
     * Place capacity
     */
    private int capacity = 0;

    /**
     * Place tokens
     */
    private Map<String, Integer> tokenCounts = new HashMap<>();

	private boolean inInterface;

	private PlaceStatus status;

    /**
     * Constructor
     * @param id of the place
     * @param name of the place
     */
    public DiscretePlace(String id, String name) {
        super(id, name);
        status = new PlaceStatusNormal(this);
    }

    /**
     * Constructor that sets the Place's name to its id
     * @param id of the place
     */
    public DiscretePlace(String id) {
        this(id, id);
    }

    /**
     * Copy constructor
     * @param place to be copied
     */
    public DiscretePlace(DiscretePlace place) {
    	this(place, false);
    }
    /**
     * Copy constructor
     * @param place to be copied
     * @param linkClone whether the new place should be linked to the old place, e.g., when being cloned
     */
    public DiscretePlace(DiscretePlace place, boolean linkClone) {
    	super(place, linkClone);
    	this.capacity = place.capacity;
    	this.markingXOffset = place.markingXOffset;
    	this.markingYOffset = place.markingYOffset;
    	status = place.getStatus().copyStatus(this); 
    	inInterface = place.isInInterface();
    }
    
    

    /**
     * @return true - Place objects are always selectable
     */
    @Override
    public boolean isSelectable() {
        return true;
    }

    /**
     *
     * @return true - Place objects can be dragged on the canvas
     */
    @Override
    public boolean isDraggable() {
        return true;
    }

    /**
     * Accept the visitor if it is a {@link uk.ac.imperial.pipe.models.petrinet.PlaceVisitor}
     * or a {@link uk.ac.imperial.pipe.models.petrinet.DiscretePlaceVisitor}
     * @param visitor to be accepted
     * @throws PetriNetComponentException if the component is not found or other logic error 
     */
    @Override
    public void accept(PetriNetComponentVisitor visitor) throws PetriNetComponentException {
        if (visitor instanceof PlaceVisitor) {
            ((PlaceVisitor) visitor).visit(this);
        }
        if (visitor instanceof DiscretePlaceVisitor) {
            ((DiscretePlaceVisitor) visitor).visit(this);
        }
    }

    /**
     *
     * @return offset for token markings
     */
    @Override
    public double getMarkingXOffset() {
        return markingXOffset;
    }

    /**
     *
     * @param markingXOffset token x marking offset
     */
    @Override
    public void setMarkingXOffset(double markingXOffset) {
        this.markingXOffset = markingXOffset;
    }

    /**
     *
     * @return y location of token markings
     */
    @Override
    public double getMarkingYOffset() {
        return markingYOffset;
    }

    /**
     *
     * @param markingYOffset new y location for token markings
     */
    @Override
    public void setMarkingYOffset(double markingYOffset) {
        this.markingYOffset = markingYOffset;
    }

    /**
     *
     * @return the token capacity, which is the maximum number of tokens that can be stored in this place
     */
    @Override
    public int getCapacity() {
        return capacity;
    }

    /**
     *
     * @param capacity maximum number of tokens that can be stored in this place or 0 for no capacity restriction.
     */
    @Override
    public void setCapacity(int capacity) {
    	int oldValue = this.capacity; 
        this.capacity = capacity;
        changeSupport.firePropertyChange(CAPACITY_CHANGE_MESSAGE, oldValue, capacity);
    }

    /**
     *
     * @return token id's -&gt; count  i.e. the number of each type of token in this place
     */
    @Override
    public Map<String, Integer> getTokenCounts() {
        return tokenCounts;
    }
    /**
     * Modifies the token count of the specified token
     * @param token to be modified
     * @param count to be set 
     */
    @Override
    public void setTokenCount(String token, int count) {
    	Map<String, Integer> counts = new HashMap<>(getTokenCounts());
    	counts.put(token, count); 
    	setTokenCounts(counts); 
    }


    /**
     *
     * Sets the places token counts to those specified. Cannot exceed the capacity
     *
     * This overrides any previous token counts.
     * @param tokenCounts to be set
     */
    @Override
    public void setTokenCounts(Map<String, Integer> tokenCounts) {
        Map<String, Integer> old = setTokenCountsBare(tokenCounts);
        notifyTokenListeners(TOKEN_CHANGE_MESSAGE, old, tokenCounts);
    }

	protected Map<String, Integer> setTokenCountsBare(Map<String, Integer> tokenCounts) {
		if (hasCapacityRestriction()) {
            int count = getNumberOfTokensStored(tokenCounts);
            if (count > capacity) {
                throw new RuntimeException("Total token count ("+count+") exceeds capacity ("+capacity+")");
            }
        }
        Map<String, Integer> old = new HashMap<>(this.tokenCounts);
        this.tokenCounts = new HashMap<>(tokenCounts);
		return old;
	}

    /**
     *
     * @return true if the token has a capacity restriction
     */
    @Override
    public boolean hasCapacityRestriction() {
        return capacity > 0;
    }

    /**
     * @param tokens map of tokens to their counts
     * @return total number of tokens stored in the map
     */
    private int getNumberOfTokensStored(Map<String, Integer> tokens) {
        int sum = 0;
        for (Integer value : tokens.values()) {
            sum += value;
        }
        return sum;
    }

    /**
     * Increments the token count of the given token
     *
     * @param token to be incremented
     */
    @Override
    public void incrementTokenCount(String token) {
        Integer count;
        if (tokenCounts.containsKey(token)) {
            count = tokenCounts.get(token);
            count++;
        } else {
            count = 1;
        }
        Map<String, Integer> old = new HashMap<>(this.tokenCounts);
        setTokenCount(token, count);
        notifyTokenListeners(TOKEN_CHANGE_MESSAGE, old, tokenCounts);
    }

	protected void notifyTokenListeners(String propertyMessage, Map<String, Integer> old, Map<String, Integer> tokenCounts) {
		changeSupport.firePropertyChange(propertyMessage, old, tokenCounts);
	}

    /**
     * @return the number of tokens currently stored in this place
     */
    @Override
    public int getNumberOfTokensStored() {
        return getNumberOfTokensStored(tokenCounts);
    }

    /**
     *
     * @param token for which count is returned
     * @return number of tokens stored in this place for the token specified
     */
    @Override
    public int getTokenCount(String token) {
        if (tokenCounts.containsKey(token)) {
            return tokenCounts.get(token);
        }
        return 0;
    }

    /**
     * Decrements the count of the token by one in this place
     * @param token to be decremented
     */
    @Override
    public void decrementTokenCount(String token) {
        Map<String, Integer> old = new HashMap<>(this.tokenCounts);
        Integer count;
        if (tokenCounts.containsKey(token)) {
            count = tokenCounts.get(token);
            count--;
            tokenCounts.put(token, count);
        }
        notifyTokenListeners(TOKEN_CHANGE_MESSAGE, old, tokenCounts);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(markingXOffset);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(markingYOffset);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(capacity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + tokenCounts.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DiscretePlace place = (DiscretePlace) o;

        if (!super.equals(place)) {
            return false;
        }

        if (Double.compare(place.capacity, capacity) != 0) {
            return false;
        }
        if (Double.compare(place.markingXOffset, markingXOffset) != 0) {
            return false;
        }
        if (Double.compare(place.markingYOffset, markingYOffset) != 0) {
            return false;
        }

        if (!tokenCounts.equals(place.tokenCounts)) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return centre point of the place
     */
    @Override
    public Point2D.Double getCentre() {
        return new Point2D.Double(getX() + getWidth() / 2, getY() + getHeight() / 2);
    }

    /**
     *
     * @return height of the place
     */
    @Override
    public int getHeight() {
        return DIAMETER;
    }

    /**
     *
     * @return width of the place
     */
    @Override
    public int getWidth() {
        return DIAMETER;
    }

    /**
     * Since Place is a circle, performs basic trigonometry
     * based on the angle that the other object is from
     * <p>
     * Note (0,0) is top left corner of grid.  -------&gt; x
     * |
     * |
     * |
     * y V
     * </p>
     * @return point where arc attaches to this place
     */
    @Override
    public Point2D.Double getArcEdgePoint(double angle) {
        double radius = DIAMETER / 2;
        double centreX = x + radius;
        double opposite = Math.cos(angle);
        double attachX = centreX - radius * opposite;

        double centreY = y + radius;
        double adjacent = Math.sin(angle);
        double attachY = centreY - radius * adjacent;

        return new Point2D.Double(attachX, attachY);
    }

    /**
     *
     * @return true 
     */
    @Override
    public boolean isEndPoint() {
        return true;
    }


    /**
     *
     * Removes all tokens with the given id from this place
     *
     * @param token for which count is to be zero 
     */
    @Override
    public void removeAllTokens(String token) {
        tokenCounts.remove(token);
    }
    /**
     * 
     * @return whether this Place is in the interface for the Petri net.  
     */
    @Override
	public boolean isInInterface() {
    	return inInterface;
    }
	@Override
	public void setInInterface(boolean inInterface) {
    	this.inInterface = inInterface;
    	if (inInterface) {
    		status = new PlaceStatusInterface(this); //FIXME ?
    	}
    	else {
    		status = new PlaceStatusNormal(this); 
    	}
    }
    
    /**
     * Update token count to mirror another place 
     * (e.g., change for a place in ExecutablePetriNet will be mirrored to same place in source Petri net, or vice versa)
     *
     * Generates TOKEN_CHANGE_MIRROR_MESSAGE to inform non-Place listeners that tokens have been updated (e.g., PlaceView)
     * 
     * @param event for the property change 
     */
	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getPropertyName().equals(TOKEN_CHANGE_MESSAGE)) {
			Map<String, Integer> tokenCounts = (Map<String, Integer>) event.getNewValue();
			Map<String, Integer> old = setTokenCountsBare(tokenCounts); 
	        notifyTokenListeners(TOKEN_CHANGE_MIRROR_MESSAGE, old, tokenCounts);
		}
		else if (event.getPropertyName().equals(REMOVE_PLACE_MESSAGE)) {
			changeSupport.removePropertyChangeListener((PropertyChangeListener) event.getSource());
		}
	}


	@Override
	public PlaceStatus getStatus() {
		return status; 
	}

	//TODO perhaps these methods should be moved to a different interface...

	@Override
	public void addToInterface(IncludeHierarchy includeHierarchy)  {
		status = new PlaceStatusInterface(this, includeHierarchy) ;  
	}

	@Override
	public void setStatus(PlaceStatus status) {
		if ((status.getPlace() != null) && !(status.getPlace() == this)) {
			throw new IllegalArgumentException("PlaceStatus can only be assigned with same Place (if not null) as this Place\n"+
		"This place: "+getId()+".  Place from status: "+status.getPlace().getId()+"."); 
		}
		this.status = status; 
	}

	@Override
	public void removeSelfFromListeners() {
		notifyTokenListeners(Place.REMOVE_PLACE_MESSAGE, null, null);
	}

}
