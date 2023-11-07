package org.asf.nexus.webservices.functions.processors;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.asf.nexus.webservices.AbstractWebService;
import org.asf.nexus.webservices.ApiRequestParams;
import org.asf.nexus.webservices.exceptions.HttpException;
import org.asf.nexus.webservices.functions.FunctionInfo;

/**
 * 
 * Webservice Parameter Processors
 * 
 * @author Sky Swimmer
 * 
 */
public interface IParameterProcessor {

	/**
	 * Checks if the parameter can be processed by this processor
	 * 
	 * @param meth             Function method
	 * @param paramType        Parameter type
	 * @param param            Function parameter that was annotated
	 * @param function         Function information object
	 * @param apiRequestParams Raw API request parameters
	 * @param webservice       Webservice instance
	 * @return True if the parameter can be processed, false otherwise
	 */
	public boolean match(Method meth, Class<?> paramType, Parameter param, FunctionInfo function,
			ApiRequestParams apiRequestParams, AbstractWebService<?> webservice);

	/**
	 * Called to process the parameter assignment
	 *
	 * @param meth             Function method
	 * @param paramType        Parameter type
	 * @param param            Function parameter that was annotated
	 * @param function         Function information object
	 * @param apiRequestParams Raw API request parameters
	 * @param webservice       Webservice instance
	 * @return Assignment object for the parameter
	 * @throws HttpException If the method is invalid or if a HTTP error occurs
	 */
	public Object process(Method meth, Class<?> paramType, Parameter param, FunctionInfo function,
			ApiRequestParams apiRequestParams, AbstractWebService<?> webservice) throws HttpException;

}
