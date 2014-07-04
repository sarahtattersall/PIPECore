package uk.ac.imperial.pipe.models.petrinet;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import uk.ac.imperial.pipe.visitor.ClonePetriNet;

public class ExecutablePetriNet implements PropertyChangeListener {

	private PetriNet petriNet;
	private Collection<Annotation> annotations;
	private Collection<Arc<? extends Connectable, ? extends Connectable>> arcs;
	private Collection<InboundArc> inboundArcs;
	private Collection<OutboundArc> outboundArcs;
	private Collection<Token> tokens;
	private Collection<RateParameter> rateParameters;
	private Collection<Place> places;
	private Collection<Transition> transitions;
	private boolean refreshRequired;

	public ExecutablePetriNet(PetriNet petriNet) {
		this.petriNet = petriNet;
		refreshRequired = true; 
		refresh(); 
	}

	public Collection<Place> getPlaces() {
		refresh(); 
		return places;
	}

	public void refresh() {
		if (refreshRequired) {
		    PetriNet clonedPetriNet = ClonePetriNet.clone(petriNet);
			annotations = clonedPetriNet.getAnnotations(); 
			arcs = clonedPetriNet.getArcs(); 
			inboundArcs = clonedPetriNet.getInboundArcs();  
			outboundArcs = clonedPetriNet.getOutboundArcs();  
			tokens	= clonedPetriNet.getTokens();  
			rateParameters = clonedPetriNet.getRateParameters();  
			places = clonedPetriNet.getPlaces();  
			transitions = clonedPetriNet.getTransitions();  
			refreshRequired = false; 
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		refreshRequired = true; 
	}

}
