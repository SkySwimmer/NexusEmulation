package org.asf.nexus.events;

import java.util.function.Consumer;

import org.asf.nexus.events.impl.EventBusImpl;

/**
 * 
 * The EventBus system, used to register and dispatch events
 * 
 * @author Sky Swimmer
 *
 */
public abstract class EventBus {

	protected static EventBus instance = new EventBusImpl();

	/**
	 * Retrieves the active event bus
	 * 
	 * @return EventBus instance
	 */
	public static EventBus getInstance() {
		return instance;
	}

	/**
	 * Adds event handlers
	 * 
	 * @param <T>          Event type
	 * @param eventClass   Event class
	 * @param eventHandler Event handler to add
	 */
	public abstract <T extends EventObject> void addEventHandler(Class<T> eventClass, Consumer<T> eventHandler);

	/**
	 * Removes event handlers
	 * 
	 * @param <T>          Event type
	 * @param eventClass   Event class
	 * @param eventHandler Event handler to add
	 */
	public abstract <T extends EventObject> void removeEventHandler(Class<T> eventClass, Consumer<T> eventHandler);

	/**
	 * Subscribes all events in a IEventReceiver object
	 * 
	 * @param receiver IEventReceiver to add
	 */
	public abstract void addAllEventsFromReceiver(IEventReceiver receiver);

	/**
	 * Removes all subscribed events from a IEventReceiver object
	 * 
	 * @param receiver IEventReceiver to add
	 */
	public abstract void removeAllEventsFromReceiver(IEventReceiver receiver);

	/**
	 * Dispatches a event
	 * 
	 * @param event Event to dispatch
	 */
	public abstract void dispatchEvent(EventObject event);

	/**
	 * Creates a new event bus
	 * 
	 * @return New EventBus instance
	 */
	public abstract EventBus createBus();

}
