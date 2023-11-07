package org.asf.nexus.webservices.functions.processors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.asf.nexus.webservices.AbstractWebService;
import org.asf.nexus.webservices.ApiRequestParams;
import org.asf.nexus.webservices.exceptions.HttpException;
import org.asf.nexus.webservices.functions.FunctionInfo;

/**
 * 
 * Webservice Method Annotation Processors
 * 
 * @author Sky Swimmer
 * 
 * @param <T> Annotation type
 * 
 */
public interface IMethodAnnotationProcessor<T extends Annotation> {

	/**
	 * Defines the annotation class
	 * 
	 * @return Annotation class
	 */
	public Class<T> annotation();

	/**
	 * Called to process the annotation
	 * 
	 * @param annotation       Annotation object
	 * @param meth             Method that was annotated
	 * @param function         Function information object
	 * @param apiRequestParams Raw API request parameters
	 * @param webservice       Webservice instance
	 * @return MatchResult value
	 */
	public MatchResult process(T annotation, Method meth, FunctionInfo function, ApiRequestParams apiRequestParams,
			AbstractWebService<?> webservice) throws HttpException;

}
