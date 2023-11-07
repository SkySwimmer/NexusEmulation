package org.asf.nexus.webservices.functions.processors;

import org.asf.nexus.webservices.AbstractWebService;
import org.asf.nexus.webservices.exceptions.HttpException;
import org.asf.nexus.webservices.functions.FunctionInfo;
import org.asf.nexus.webservices.functions.FunctionResult;

/**
 * 
 * Function result post-processor
 * 
 * @author Sky Swimmer
 * 
 */
public interface IFunctionResultPostProcessor {

	/**
	 * Post-processes function result objects
	 * 
	 * @param result  Input result object
	 * @param func    Function information instance
	 * @param service Webservice instance
	 * @return Post-processed FunctionResult instance
	 */
	public FunctionResult postProcess(FunctionResult result, FunctionInfo func, AbstractWebService<?> service)
			throws HttpException;

}
