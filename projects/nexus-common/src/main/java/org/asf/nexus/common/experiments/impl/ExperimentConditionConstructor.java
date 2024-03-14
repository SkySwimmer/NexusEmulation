package org.asf.nexus.common.experiments.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.asf.nexus.common.experiments.ExperimentManager;
import org.asf.nexus.events.EventBus;
import org.asf.nexus.events.EventObject;
import org.asf.nexus.events.IEventReceiver;
import org.asf.nexus.events.conditions.ExperimentCondition;
import org.asf.nexus.events.conditions.interfaces.IEventConditionConstructor;
import org.asf.nexus.events.conditions.interfaces.IGenericEventCondition;

public class ExperimentConditionConstructor implements IEventConditionConstructor {

	@Override
	public IGenericEventCondition construct(IEventReceiver receiverType, Method listener, EventObject event,
			Annotation annotation, EventBus bus) {
		return new IGenericEventCondition() {

			@Override
			public boolean supportsStatic() {
				return true;
			}

			@Override
			public boolean match(IEventReceiver receiverType, Method listener, EventObject event) {
				// Check if experiment is enabled
				ExperimentCondition anno = (ExperimentCondition) annotation;
				String key = anno.value();
				if (anno.isReverse()) {
					// Check
					if (ExperimentManager.getInstance().isExperimentEnabled(key))
						return false;
				} else {
					// Check
					if (!ExperimentManager.getInstance().isExperimentEnabled(key))
						return false;
				}
				return true;
			}

		};
	}

}
