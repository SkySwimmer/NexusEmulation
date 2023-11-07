package org.asf.nexus.webservices.functions.processors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.asf.nexus.webservices.AbstractWebService;
import org.asf.nexus.webservices.ApiRequestParams;
import org.asf.nexus.webservices.exceptions.HttpException;
import org.asf.nexus.webservices.functions.FunctionInfo;

/**
 * 
 * Webservice Parameter Annotation Processors
 * 
 * @author Sky Swimmer
 * 
 * @param <T> Annotation type
 * 
 */
public interface IParameterAnnotationProcessor<T extends Annotation> {

	/**
	 * Defines the annotation class
	 * 
	 * @return Annotation class
	 */
	public Class<T> annotation();

	/**
	 * Checks if the parameter can be processed by this processor
	 * 
	 * @param annotation       Annotation object
	 * @param meth             Function method
	 * @param param            Function parameter that was annotated
	 * @param function         Function information object
	 * @param apiRequestParams Raw API request parameters
	 * @param webservice       Webservice instance
	 * @return True if the parameter can be processed, false otherwise
	 */
	public boolean match(T annotation, Method meth, Parameter param, FunctionInfo function,
			ApiRequestParams apiRequestParams, AbstractWebService<?> webservice);

	/**
	 * Called to process the annotation
	 * 
	 * @param annotation       Annotation object
	 * @param meth             Function method
	 * @param param            Function parameter that was annotated
	 * @param function         Function information object
	 * @param apiRequestParams Raw API request parameters
	 * @param webservice       Webservice instance
	 * @return Assignment object for the parameter
	 * @throws HttpException If the method is invalid or if a HTTP error occurs
	 */
	public Object process(T annotation, Method meth, Parameter param, FunctionInfo function,
			ApiRequestParams apiRequestParams, AbstractWebService<?> webservice) throws HttpException;

}
