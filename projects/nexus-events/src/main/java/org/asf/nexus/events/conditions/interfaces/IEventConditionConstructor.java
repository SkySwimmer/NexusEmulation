package org.asf.nexus.events.conditions.interfaces;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.asf.nexus.events.EventBus;
import org.asf.nexus.events.EventObject;
import org.asf.nexus.events.IEventReceiver;

public interface IEventConditionConstructor {

	/**
	 * Called to construct event conditions
	 * 
	 * @param receiverType Receiver type
	 * @param listener     Event listener method
	 * @param event        Event object
	 * @param annotation   Annotation that was used to add the condition
	 * @param bus          The event bus that is registering the listener
	 * @return IGenericEventCondition instance
	 */
	public IGenericEventCondition construct(IEventReceiver receiverType, Method listener, EventObject event,
			Annotation annotation, EventBus bus);

}
