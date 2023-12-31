package org.asf.nexus.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;

import org.asf.connective.RemoteClient;
import org.asf.connective.processors.HttpPushProcessor;
import org.asf.nexus.common.INexusBaseServer;
import org.asf.nexus.webservices.cookies.CookieContext;
import org.asf.nexus.webservices.cookies.CookieManager;
import org.asf.nexus.webservices.functions.FunctionInfo;
import org.asf.nexus.webservices.functions.FunctionResult;
import org.asf.nexus.webservices.functions.processors.IFunctionResultPostProcessor;
import org.asf.nexus.webservices.functions.processors.IMethodAnnotationProcessor;
import org.asf.nexus.webservices.functions.processors.IParameterAnnotationProcessor;
import org.asf.nexus.webservices.functions.processors.IParameterProcessor;
import org.asf.nexus.webservices.functions.processors.impl.ExperimentalFeatureAnnotationProcessor;

/**
 * 
 * Web Service Abstract
 * 
 * @author Sky Swimmer
 *
 * @param <T> Server type
 * 
 */
public abstract class AbstractWebService<T extends INexusBaseServer> extends HttpPushProcessor {

	private static ArrayList<IFunctionResultPostProcessor> functionResultPostProcessors = new ArrayList<IFunctionResultPostProcessor>();
	private static HashMap<String, ArrayList<IMethodAnnotationProcessor<Annotation>>> methodAnnotationProcessors = new HashMap<String, ArrayList<IMethodAnnotationProcessor<Annotation>>>();
	private static HashMap<String, ArrayList<IParameterAnnotationProcessor<Annotation>>> parameterAnnotationProcessors = new HashMap<String, ArrayList<IParameterAnnotationProcessor<Annotation>>>();
	private static ArrayList<IParameterProcessor> parameterProcessors = new ArrayList<IParameterProcessor>();

	protected WebServiceContext<T> context;
	private CookieContext cookies;

	public AbstractWebService(WebServiceContext<T> context) {
		this.context = context;
		context.initialize(this);
	}

	@Override
	public HttpPushProcessor createNewInstance() {
		return createNewInstance(context);
	}

	@Override
	public void process(String path, String method, RemoteClient client, String contentType) throws IOException {
		// Compute subpath
		path = sanitizePath(path.substring(this.path().length()));
		if (path.startsWith("/"))
			path = path.substring(1);
		if (path.isEmpty()) {
			setResponseStatus(404, "Not found");
			fallbackRequestProcessor(path, method, client, contentType);
			return;
		}

		// Make sure its not attempting to access a resource outside of the scope
		if (path.startsWith("..") || path.endsWith("..") || path.contains("/..") || path.contains("../")) {
			setResponseStatus(403, "Forbidden");
			return;
		}

		// Create info
		FunctionInfo function = new FunctionInfo(path, getRequest(), getResponse(), client.getServer(), method, client,
				getCookies());

		// Execute
		FunctionResult res = context.executeFunction(function, this);
		if (res == null)
			res = context.postProcess(fallbackRequestProcessor(path, method, client, contentType), function, this);

		// Set response
		setResponseStatus(res.getStatusCode(), res.getStatusMessage());
		if (res.hasResponseBody()) {
			// Check response modes
			if (res.getContentLength() != -1) {
				// With length
				if (res.getResponseMediaType() != null)
					setResponseContent(res.getResponseMediaType(), res.getResponseBodyStream(), res.getContentLength());
				else
					setResponseContent(res.getResponseBodyStream(), res.getContentLength());
			} else {
				// Without length
				if (res.getResponseMediaType() != null)
					setResponseContent(res.getResponseMediaType(), res.getResponseBodyStream());
				else
					setResponseContent(res.getResponseBodyStream());
			}
		}
	}

	@Override
	public boolean supportsNonPush() {
		return true;
	}

	@Override
	public boolean supportsChildPaths() {
		return true;
	}

	/**
	 * Creates a new instance of this webservice
	 * 
	 * @param context Webservice context
	 */
	public abstract AbstractWebService<T> createNewInstance(WebServiceContext<T> context);

	/**
	 * Called to process the request, called if no function applies
	 * 
	 * @param path        Path string
	 * @param method      Request method
	 * @param client      Remote client
	 * @param contentType Body content type
	 * @return FunctionResult instance
	 * @throws IOException If processing fails
	 */
	protected FunctionResult fallbackRequestProcessor(String path, String method, RemoteClient client,
			String contentType) throws IOException {
		return new FunctionResult(404, "Not found");
	}

	/**
	 * Cleans a given path
	 * 
	 * @param path Path to clean
	 * @return Cleaned path
	 */
	protected String sanitizePath(String path) {
		while (path.startsWith("/"))
			path = path.substring(1);
		while (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);
		while (path.contains("//"))
			path = path.replace("//", "/");
		if (path.contains("\\"))
			path = path.replace("\\", "/");
		if (!path.startsWith("/"))
			path = "/" + path;
		return path;
	}

	/**
	 * Retrieves the server instance
	 */
	public T getServerInstance() {
		return context.getServer();
	}

	/**
	 * Retrieves the cookie context of this request
	 * 
	 * @return CookieContext instance
	 */
	protected CookieContext getCookies() {
		if (cookies == null)
			cookies = CookieManager.getCookies(getRequest(), getResponse());
		return cookies;
	}

	/**
	 * Creates a function result object with no response body (errors will use
	 * default error page)
	 */
	protected FunctionResult ok() {
		return new FunctionResult(200, "OK");
	}

	/**
	 * Creates a function result object
	 * 
	 * @param mediaType    Response media type
	 * @param responseBody Response body
	 */
	protected FunctionResult ok(String mediaType, InputStream responseBody) {
		return new FunctionResult(200, "OK", mediaType, responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param responseBody Response body
	 */
	protected FunctionResult ok(InputStream responseBody) {
		return new FunctionResult(200, "OK", responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param mediaType     Response media type
	 * @param contentLength Response body length
	 * @param responseBody  Response body
	 */
	protected FunctionResult ok(String mediaType, long contentLength, InputStream responseBody) {
		return new FunctionResult(200, "OK", mediaType, contentLength, responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param contentLength Response body length
	 * @param responseBody  Response body
	 */
	protected FunctionResult ok(long contentLength, InputStream responseBody) {
		return new FunctionResult(200, "OK", contentLength, responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param mediaType    Response media type
	 * @param responseBody Response body
	 */
	protected FunctionResult ok(String mediaType, byte[] responseBody) {
		return new FunctionResult(200, "OK", mediaType, responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param responseBody Response body
	 */
	protected FunctionResult ok(byte[] responseBody) {
		return new FunctionResult(200, "OK", responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param mediaType    Response media type
	 * @param responseBody Response body
	 */
	protected FunctionResult ok(String mediaType, String responseBody) {
		return new FunctionResult(200, "OK", mediaType, responseBody);

	}

	/**
	 * Creates a function result object
	 * 
	 * @param responseBody Response body
	 */
	protected FunctionResult ok(String responseBody) {
		return new FunctionResult(200, "OK", responseBody);
	}

	/**
	 * Creates a function result object with no response body (errors will use
	 * default error page)
	 * 
	 * @param statusCode    Result status code
	 * @param statusMessage Result status message
	 */
	protected FunctionResult response(int statusCode, String statusMessage) {
		return new FunctionResult(statusCode, statusMessage);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param statusCode    Result status code
	 * @param statusMessage Result status message
	 * @param mediaType     Response media type
	 * @param responseBody  Response body
	 */
	protected FunctionResult response(int statusCode, String statusMessage, String mediaType,
			InputStream responseBody) {
		return new FunctionResult(statusCode, statusMessage, mediaType, responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param statusCode    Result status code
	 * @param statusMessage Result status message
	 * @param responseBody  Response body
	 */
	protected FunctionResult response(int statusCode, String statusMessage, InputStream responseBody) {
		return new FunctionResult(statusCode, statusMessage, responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param statusCode    Result status code
	 * @param statusMessage Result status message
	 * @param mediaType     Response media type
	 * @param contentLength Response body length
	 * @param responseBody  Response body
	 */
	protected FunctionResult response(int statusCode, String statusMessage, String mediaType, long contentLength,
			InputStream responseBody) {
		return new FunctionResult(statusCode, statusMessage, mediaType, contentLength, responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param statusCode    Result status code
	 * @param statusMessage Result status message
	 * @param contentLength Response body length
	 * @param responseBody  Response body
	 */
	protected FunctionResult response(int statusCode, String statusMessage, long contentLength,
			InputStream responseBody) {
		return new FunctionResult(statusCode, statusMessage, contentLength, responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param statusCode    Result status code
	 * @param statusMessage Result status message
	 * @param mediaType     Response media type
	 * @param responseBody  Response body
	 */
	protected FunctionResult response(int statusCode, String statusMessage, String mediaType, byte[] responseBody) {
		return new FunctionResult(statusCode, statusMessage, mediaType, responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param statusCode    Result status code
	 * @param statusMessage Result status message
	 * @param responseBody  Response body
	 */
	protected FunctionResult response(int statusCode, String statusMessage, byte[] responseBody) {
		return new FunctionResult(statusCode, statusMessage, responseBody);
	}

	/**
	 * Creates a function result object
	 * 
	 * @param statusCode    Result status code
	 * @param statusMessage Result status message
	 * @param mediaType     Response media type
	 * @param responseBody  Response body
	 */
	protected FunctionResult response(int statusCode, String statusMessage, String mediaType, String responseBody) {
		return new FunctionResult(statusCode, statusMessage, mediaType, responseBody);

	}

	/**
	 * Creates a function result object
	 * 
	 * @param statusCode    Result status code
	 * @param statusMessage Result status message
	 * @param responseBody  Response body
	 */
	protected FunctionResult response(int statusCode, String statusMessage, String responseBody) {
		return new FunctionResult(statusCode, statusMessage, responseBody);
	}

	/**
	 * Registers function result post-processors
	 * 
	 * @param processor Processor to register
	 */
	public static void registerFunctionResultPostProcessor(IFunctionResultPostProcessor processor) {
		functionResultPostProcessors.add(processor);
	}

	/**
	 * Retrieves all registered function result post-processors
	 * 
	 * @return Array of IFunctionResultPostProcessor instances
	 */
	public static IFunctionResultPostProcessor[] getFunctionResultPostProcessors() {
		return functionResultPostProcessors.toArray(t -> new IFunctionResultPostProcessor[t]);
	}

	/**
	 * Registers annotation processors
	 * 
	 * @param processor Processor to register
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void registerAnnotationProcessor(IMethodAnnotationProcessor<?> processor) {
		ArrayList<IMethodAnnotationProcessor<Annotation>> lst = methodAnnotationProcessors
				.get(processor.annotation().getTypeName());
		if (lst == null) {
			lst = new ArrayList<IMethodAnnotationProcessor<Annotation>>();
			methodAnnotationProcessors.put(processor.annotation().getTypeName(), lst);
		}
		lst.add((IMethodAnnotationProcessor) processor);
	}

	/**
	 * Retrieves method annotation processors
	 * 
	 * @return Array of IMethodAnnotationProcessor instances
	 */
	@SuppressWarnings("unchecked")
	public static IMethodAnnotationProcessor<Annotation>[] getMethodAnnotationProcessors() {
		ArrayList<IMethodAnnotationProcessor<Annotation>> lst = new ArrayList<IMethodAnnotationProcessor<Annotation>>();
		methodAnnotationProcessors.values().forEach(t -> lst.addAll(t));
		return lst.toArray(t -> new IMethodAnnotationProcessor[t]);
	}

	/**
	 * Retrieves method annotation processors
	 * 
	 * @param anno Annotation to retrieve the processors for
	 * @return Array of IMethodAnnotationProcessor instances
	 */
	@SuppressWarnings("unchecked")
	public static IMethodAnnotationProcessor<Annotation>[] getMethodAnnotationProcessors(
			Class<? extends Annotation> anno) {
		ArrayList<IMethodAnnotationProcessor<Annotation>> lst = new ArrayList<IMethodAnnotationProcessor<Annotation>>();
		if (methodAnnotationProcessors.containsKey(anno.getTypeName()))
			lst.addAll(methodAnnotationProcessors.get(anno.getTypeName()));
		return lst.toArray(t -> new IMethodAnnotationProcessor[t]);
	}

	/**
	 * Registers annotation processors
	 * 
	 * @param processor Processor to register
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void registerAnnotationProcessor(IParameterAnnotationProcessor<?> processor) {
		ArrayList<IParameterAnnotationProcessor<Annotation>> lst = parameterAnnotationProcessors
				.get(processor.annotation().getTypeName());
		if (lst == null) {
			lst = new ArrayList<IParameterAnnotationProcessor<Annotation>>();
			parameterAnnotationProcessors.put(processor.annotation().getTypeName(), lst);
		}
		lst.add((IParameterAnnotationProcessor) processor);
	}

	/**
	 * Retrieves parameter annotation processors
	 * 
	 * @return Array of IParameterAnnotationProcessor instances
	 */
	@SuppressWarnings("unchecked")
	public static IParameterAnnotationProcessor<Annotation>[] getParameterAnnotationProcessors() {
		ArrayList<IParameterAnnotationProcessor<Annotation>> lst = new ArrayList<IParameterAnnotationProcessor<Annotation>>();
		parameterAnnotationProcessors.values().forEach(t -> lst.addAll(t));
		return lst.toArray(t -> new IParameterAnnotationProcessor[t]);
	}

	/**
	 * Retrieves parameter annotation processors
	 * 
	 * @param anno Annotation to retrieve the processors for
	 * @return Array of IParameterAnnotationProcessor instances
	 */
	@SuppressWarnings("unchecked")
	public static IParameterAnnotationProcessor<Annotation>[] getParameterAnnotationProcessors(
			Class<? extends Annotation> anno) {
		ArrayList<IParameterAnnotationProcessor<Annotation>> lst = new ArrayList<IParameterAnnotationProcessor<Annotation>>();
		if (parameterAnnotationProcessors.containsKey(anno.getTypeName()))
			lst.addAll(parameterAnnotationProcessors.get(anno.getTypeName()));
		return lst.toArray(t -> new IParameterAnnotationProcessor[t]);
	}

	/**
	 * Registers parameter processors
	 * 
	 * @param processor Processor to register
	 */
	public static void registerParameterProcessor(IParameterProcessor processor) {
		parameterProcessors.add(processor);
	}

	/**
	 * Retrieves parameter processors
	 * 
	 * @return Array of IParameterProcessor instances
	 */
	public static IParameterProcessor[] getParameterProcessors() {
		return parameterProcessors.toArray(t -> new IParameterProcessor[t]);
	}

	static {
		// Register processors
		registerAnnotationProcessor(new ExperimentalFeatureAnnotationProcessor());
	}

}
