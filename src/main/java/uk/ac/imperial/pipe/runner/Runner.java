package uk.ac.imperial.pipe.runner;

import java.beans.PropertyChangeListener;

public interface Runner {

	public void run();

	public void setFiringLimit(int firingLimit);

	public void setSeed(long seed);
	
	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void listenForTokenChanges(PropertyChangeListener listener, String placeId);

	public void markPlace(String placeId, String token, int count);

	public void setTransitionContext(String transitionId, Object object);

}
