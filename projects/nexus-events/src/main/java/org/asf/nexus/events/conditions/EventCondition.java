package org.asf.nexus.events.conditions;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.asf.nexus.events.conditions.impl.GenericConstructorImpl;
import org.asf.nexus.events.conditions.interfaces.IGenericEventCondition;

@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@Repeatable(EventConditions.class)
@EventConditionConstructor(GenericConstructorImpl.class)
public @interface EventCondition {

	/**
	 * Defines the condition type
	 * 
	 * @return IGenericEventCondition class value
	 */
	public Class<? extends IGenericEventCondition> value();

}
