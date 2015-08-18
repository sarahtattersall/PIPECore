package uk.ac.imperial.pipe.models.petrinet;


public class DiscreteTimedTransition extends DiscreteTransition {

	private long currentTime;

	public DiscreteTimedTransition(String id) {
		super(id); 
		setTimed(true); 
	}

	protected void setCurrentTimeForTesting(long currentTime) {
		this.currentTime = currentTime; 
	}

	public long getCurrentTimeForTesting() {
		return currentTime;
	}
	
	@Override
	public void fire() {
		long time = getCurrentTime();
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		currentTime += delay; 
	}

	protected long getCurrentTime() {
		if (currentTime == 0) {
			currentTime = System.currentTimeMillis();
		}
		return currentTime; 
	}

}
