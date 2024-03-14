package org.asf.nexus.events.conditions.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.asf.nexus.events.EventBus;
import org.asf.nexus.events.EventObject;
import org.asf.nexus.events.IEventReceiver;
import org.asf.nexus.events.conditions.EventCondition;
import org.asf.nexus.events.conditions.interfaces.IEventConditionConstructor;
import org.asf.nexus.events.conditions.interfaces.IGenericEventCondition;

public class GenericConstructorImpl implements IEventConditionConstructor {

	private static HashMap<String, IGenericEventCondition> conditions = new HashMap<String, IGenericEventCondition>();

	@Override
	public IGenericEventCondition construct(IEventReceiver receiverType, Method listener, EventObject event,
			Annotation annotation, EventBus bus) {
		// Cast annotation type
		EventCondition anno = (EventCondition) annotation;
		synchronized (conditions) {
			Class<? extends IGenericEventCondition> condType = anno.value();
			if (conditions.containsKey(condType.getTypeName()))
				return conditions.get(condType.getTypeName());

			// Constructor
			Constructor<? extends IGenericEventCondition> ctor;
			try {
				ctor = condType.getConstructor();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException("Failed to create instance of condition type " + condType.getTypeName()
						+ ", no suitable constructor found.", e);
			}

			// Create instance
			try {
				IGenericEventCondition inst = ctor.newInstance();
				conditions.put(condType.getTypeName(), inst);
				return inst;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException("Failed to create instance of condition type " + condType.getTypeName(), e);
			}
		}
	}

}
