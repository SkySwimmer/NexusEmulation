package org.asf.nexus.webservices.functions.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.asf.nexus.webservices.functions.FunctionInfo;

/**
 * 
 * Marks methods as API request handlers using standardized requests.<br/>
 * <br/>
 * Use a {@link FunctionInfo} parameter to receive the function info object.
 * <br/>
 * Use {@link RequestParam} on parameters to load from the request payload.
 * 
 * @author Sky Swimmer
 *
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface ApiHandler {
}
