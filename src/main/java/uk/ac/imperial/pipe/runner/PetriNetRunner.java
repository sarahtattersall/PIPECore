package uk.ac.imperial.pipe.runner;

import java.util.Collection;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import uk.ac.imperial.pipe.animation.Animator;
import uk.ac.imperial.pipe.animation.PetriNetAnimator;
import uk.ac.imperial.pipe.models.petrinet.AbstractPetriNetPubSub;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.state.State;

public class PetriNetRunner extends AbstractPetriNetPubSub {


	public static final String EXECUTION_STARTED = "execution started";
	public static final String UPDATED_STATE = "state updated";
	public static final String EXECUTION_COMPLETED = "execution complete";
	private Random random;
	private int firingLimit;
	private ExecutablePetriNet executablePetriNet;
	private int round;
	private boolean transitionsToFire;
	private State previousState;
	private Animator animator;
	private Firing previousFiring;
	private Firing firing;

	public PetriNetRunner(PetriNet petriNet) {
		executablePetriNet = petriNet.getExecutablePetriNet(); 
		round = 0; 
		transitionsToFire = true; 
		previousState = executablePetriNet.getState(); 
		previousFiring = new Firing(round, "", previousState);; 
		animator = new PetriNetAnimator(executablePetriNet); 
	}

	public void setSeed(long seed) {
		this.random = new Random(seed); 
		animator.setRandom(random); 
	}

	public void setFiringLimit(int firingLimit) {
		this.firingLimit = firingLimit; 
	}

	public void run() {
		start(); 
		while ((round < firingLimit) && transitionsToFire()) {
			round++; 
			fireOneTransition();
		}
		end(); 
	}

	private void fireOneTransition() {
		Transition transition = null; 
		try {
			transition = animator.getRandomEnabledTransition(); 
			animator.fireTransition(transition); 
			firing = new Firing(round, transition.getId(), executablePetriNet.getState()); 
			changeSupport.firePropertyChange(UPDATED_STATE, previousFiring, firing); 
			previousFiring = firing; 
		} catch (RuntimeException e) {
			if (e.getMessage().equals(Animator.ERROR_NO_TRANSITIONS_TO_FIRE)) transitionsToFire = false;  
			else throw e; 
		}
	}
	
	private void end() {
		changeSupport.firePropertyChange(EXECUTION_COMPLETED, null, null); 
	}

	private boolean transitionsToFire() {
		return transitionsToFire;
	}

	private void start() {
		changeSupport.firePropertyChange(EXECUTION_STARTED, null, executablePetriNetPlaces()); 
	}

	private Collection<String> executablePetriNetPlaces() {	
		SortedSet<String> sortedPlaces = new TreeSet<>(); 
		for (Place place : executablePetriNet.getPlaces()) {
			sortedPlaces.add(place.getId());
		}
		return sortedPlaces;
	}

}
