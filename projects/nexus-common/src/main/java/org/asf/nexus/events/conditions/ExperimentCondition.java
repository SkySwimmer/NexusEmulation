package org.asf.nexus.events.conditions;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.asf.nexus.common.experiments.impl.ExperimentConditionConstructor;

@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@Repeatable(ExperimentConditions.class)
@EventConditionConstructor(ExperimentConditionConstructor.class)
public @interface ExperimentCondition {

	/**
	 * Defines the experimental feature thats required
	 * 
	 * @return Experimental feature key
	 */
	public String value();

	/**
	 * Marks this method as a reverse feature, meaning this method only is enabled
	 * should the feature be disabled.
	 * 
	 * @return True to enable reverse-mode, false otherwise
	 */
	public boolean isReverse() default (false);

}
