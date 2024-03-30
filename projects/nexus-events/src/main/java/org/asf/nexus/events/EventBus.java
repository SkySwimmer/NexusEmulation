package org.asf.nexus.events;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.asf.nexus.common.ObjectStorageContainer;
import org.asf.nexus.events.impl.EventBusImpl;
import org.asf.nexus.tasks.async.AsyncTask;

/**
 * 
 * The EventBus system, used to register and dispatch events
 * 
 * @author Sky Swimmer
 *
 */
public abstract class EventBus extends ObjectStorageContainer {

	protected static EventBus instance = new EventBusImpl();
	private HashMap<Function<? extends SupplierEventObject<?>, ?>, Consumer<? extends EventObject>> handlers = new HashMap<Function<? extends SupplierEventObject<?>, ?>, Consumer<? extends EventObject>>();

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
	 * Adds event handlers
	 * 
	 * @param <T>          Event type
	 * @param <T2>         Event result type
	 * @param eventClass   Event class
	 * @param eventHandler Event handler to add
	 */
	public <T2, T extends SupplierEventObject<T2>> void addEventHandler(Class<T> eventClass,
			Function<T, T2> eventHandler) {
		Consumer<T> handler = ev -> {
			T2 ret = eventHandler.apply(ev);
			if (ret != null)
				ev.setResult(ret);
		};
		handlers.put(eventHandler, handler);
		addEventHandler(eventClass, handler);
	}

	/**
	 * Removes event handlers
	 * 
	 * @param <T>          Event type
	 * @param <T2>         Event result type
	 * @param eventClass   Event class
	 * @param eventHandler Event handler to add
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T2, T extends SupplierEventObject<T2>> void removeEventHandler(Class<T> eventClass,
			Function<T, T2> eventHandler) {
		if (handlers.containsKey(eventHandler)) {
			removeEventHandler(eventClass, (Consumer) handlers.get(eventHandler));
			handlers.remove(eventHandler);
		}
	}

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
	 * Dispatches an event
	 * 
	 * @param event Event to dispatch
	 */
	public abstract void dispatchEvent(EventObject event);

	/**
	 * Dispatches an event
	 * 
	 * @param event Event to dispatch
	 */
	public <T2, T extends SupplierEventObject<T2>> T2 dispatchEvent(T event) {
		dispatchEvent((EventObject) event);
		return event.getResult();
	}

	/**
	 * Dispatches an event
	 * 
	 * @param event    Event to dispatch
	 * @param callback Method to call once dispatching finishes
	 */
	public <T2, T extends SupplierEventObject<T2>> void dispatchEvent(T event, BiConsumer<T, T2> callback) {
		dispatchEvent((EventObject) event);
		callback.accept(event, event.getResult());
	}

	/**
	 * Dispatches an event asynchronously
	 * 
	 * @param event Event to dispatch
	 */
	public AsyncTask<Void> dispatchEventAsync(EventObject event) {
		return AsyncTask.runAsync(() -> {
			dispatchEvent(event);
		});
	}

	/**
	 * Dispatches an event asynchronously
	 * 
	 * @param event Event to dispatch
	 */
	public <T2, T extends SupplierEventObject<T2>> AsyncTask<T2> dispatchEventAsync(T event) {
		return AsyncTask.runAsync(() -> {
			dispatchEvent((EventObject) event);
			return event.getResult();
		});
	}

	/**
	 * Dispatches an event asynchronously
	 * 
	 * @param event    Event to dispatch
	 * @param callback Method to call once dispatching finishes
	 */
	public <T extends EventObject> AsyncTask<Void> dispatchEventAsync(T event, Consumer<T> callback) {
		return AsyncTask.runAsync(() -> {
			dispatchEvent((EventObject) event);
			callback.accept(event);
		});
	}

	/**
	 * Dispatches an event asynchronously
	 * 
	 * @param event    Event to dispatch
	 * @param callback Method to call once dispatching finishes
	 */
	public <T2, T extends SupplierEventObject<T2>> AsyncTask<T2> dispatchEventAsync(T event,
			BiConsumer<T, T2> callback) {
		return AsyncTask.runAsync(() -> {
			dispatchEvent((EventObject) event);
			callback.accept(event, event.getResult());
			return event.getResult();
		});
	}

	/**
	 * Creates a new event bus
	 * 
	 * @return New EventBus instance
	 */
	public abstract EventBus createBus();

}
