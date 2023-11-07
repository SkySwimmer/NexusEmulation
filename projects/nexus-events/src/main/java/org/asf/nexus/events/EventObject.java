package org.asf.nexus.events;

/**
 * 
 * Abstract event object, needs to be annotated with EventPath for registration
 * to work
 * 
 * @author Sky Swimmer
 *
 */
public abstract class EventObject {

	private boolean handled = false;

	/**
	 * Defines the event path
	 */
	public abstract String eventPath();

	/**
	 * Checks if the event has been marked as handled
	 * 
	 * @return True if the event has been handled, false otherwise
	 */
	public boolean isHandled() {
		return handled;
	}

	/**
	 * Marks the event as handled
	 */
	public void setHandled() {
		handled = true;
	}

}
