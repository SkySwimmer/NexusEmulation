package org.asf.nexus.webservices.functions.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.asf.nexus.webservices.functions.FunctionInfo;
import org.asf.nexus.webservices.functions.FunctionResult;

/**
 * 
 * Marks methods as functions, if annotation has a value, it is used as function
 * name, else the method name is used.<br/>
 * <br/>
 * <b>Note</b>: the annotated method must return a {@link FunctionResult} object
 * and its parameters must contain a {@link FunctionInfo} parameter, no other
 * parameters are supported.
 * 
 * @author Sky Swimmer
 *
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Function {

	/**
	 * Defines if this function allows child paths
	 * 
	 * @return True if the function allows child paths, false otherwise
	 */
	public boolean allowSubPaths() default (false);

	/**
	 * Defines the parameter name
	 * 
	 * @return Parameter name or &lt;auto&gt; if automatic
	 */
	public String value() default ("<auto>");

	/**
	 * Defines the allowed methods of this function
	 * 
	 * @return Array of allowed method names
	 */
	public String[] allowedMethods() default { "GET", "POST" };

}
