package org.asf.nexus.events.conditions;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@RepeatableTarget(ExperimentCondition.class)
public @interface ExperimentConditions {

	public ExperimentCondition[] value();

}
