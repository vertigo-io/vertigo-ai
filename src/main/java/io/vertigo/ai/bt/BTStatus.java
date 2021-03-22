package io.vertigo.ai.bt;

/**
 * The status returned after a node has been evaluated.
 * There are only 3 status 
 *  - succeeded
 *  - failed
 *  - running 
 *  
 *  the two first are obvious
 *  the third permits to have some task that have a duration
 *  
 *  for example : 
 *  - walk to the next wall
 *  - collect data 
 *  - ...
 *  
 *  The node triggers the task which may take some time
 *  
 * @author pchretien
 */
public enum BTStatus {
	Succeeded,
	Failed,
	Running;

	public boolean isFailed() {
		return this == Failed;
	}

	public boolean isSucceeded() {
		return this == Succeeded;
	}

	public boolean isRunning() {
		return this == Running;
	}
}
