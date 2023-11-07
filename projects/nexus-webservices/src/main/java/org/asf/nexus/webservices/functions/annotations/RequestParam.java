package org.asf.nexus.webservices.functions.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 *
 * Marks the parameter as a request parameter
 * 
 * @author Sky Swimmer
 *
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface RequestParam {

	/**
	 * Defines the request field name to retrieve, by default it will use the
	 * parameter name
	 */
	public String value() default "";

	/**
	 * Checks if the parameter is required
	 * 
	 * @return True if required, false otherwise
	 */
	public boolean required() default true;

}
