package org.asf.nexus.webservices;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.asf.connective.objects.HttpRequest;
import org.asf.nexus.common.INexusBaseServer;
import org.asf.nexus.webservices.exceptions.HttpException;
import org.asf.nexus.webservices.functions.FunctionInfo;
import org.asf.nexus.webservices.functions.FunctionResult;
import org.asf.nexus.webservices.functions.annotations.ApiHandler;
import org.asf.nexus.webservices.functions.annotations.Function;
import org.asf.nexus.webservices.functions.annotations.RequestParam;
import org.asf.nexus.webservices.functions.processors.IFunctionResultPostProcessor;
import org.asf.nexus.webservices.functions.processors.IMethodAnnotationProcessor;
import org.asf.nexus.webservices.functions.processors.IParameterAnnotationProcessor;
import org.asf.nexus.webservices.functions.processors.IParameterProcessor;
import org.asf.nexus.webservices.functions.processors.MatchResult;
import org.asf.nexus.webservices.requestparams.impl.JacksonObjectParams;
import org.asf.nexus.webservices.requestparams.impl.UrlEncodedParams;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

/**
 * 
 * Webservice Context Container
 * 
 * @author Sky Swimmer
 * 
 */
public class WebServiceContext<T extends INexusBaseServer> {

	private T server;
	private HashMap<String, ArrayList<Method>> functions = new HashMap<String, ArrayList<Method>>();

	private static XmlMapper xmlMapper = new XmlMapper();
	private static ObjectMapper jsonMapper = new ObjectMapper();
	static {
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
		xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		jsonMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}

	/**
	 * Creates and initializes a service context container
	 * 
	 * @param <T>    Server type
	 * @param server Server instance
	 * @return WebServiceContext instance
	 */
	public static <T extends INexusBaseServer> WebServiceContext<T> create(T server) {
		return new WebServiceContext<T>(server);
	}

	private WebServiceContext(T server) {
		this.server = server;
	}

	private boolean inited = false;

	synchronized void initialize(AbstractWebService<T> service) {
		if (inited)
			return;
		inited = true;

		// Find all functions
		for (Method meth : service.getClass().getMethods()) {
			if (!Modifier.isAbstract(meth.getModifiers()) && !Modifier.isStatic(meth.getModifiers())) {
				// Check parameters
				if ((meth.isAnnotationPresent(Function.class) || meth.isAnnotationPresent(ApiHandler.class))
						&& FunctionResult.class.isAssignableFrom(meth.getReturnType())) {
					// Check
					if (!meth.isAnnotationPresent(ApiHandler.class) && (meth.getParameterTypes().length != 1
							|| !meth.getParameterTypes()[0].isAssignableFrom(FunctionInfo.class)))
						continue;

					// Load name
					String name = meth.getName();
					if (meth.isAnnotationPresent(Function.class)) {
						Function funcAnno = meth.getAnnotation(Function.class);
						if (!funcAnno.value().equals("<auto>"))
							name = funcAnno.value();
					}

					// Make accessible
					meth.setAccessible(true);

					// Register
					if (!functions.containsKey(name.toLowerCase()))
						functions.put(name.toLowerCase(), new ArrayList<Method>());
					functions.get(name.toLowerCase()).add(meth);
				}
			}
		}
	}

	/**
	 * Retrieves the server instance
	 * 
	 * @return Server instance
	 */
	public T getServer() {
		return server;
	}

	/**
	 * Executes functions
	 * 
	 * @param function Function to execute
	 * @param service  Service instance
	 * @return FunctionResult instance or null if none were matching
	 * @throws IOException If an IO error occurs
	 */
	public FunctionResult executeFunction(FunctionInfo function, AbstractWebService<?> service) throws IOException {
		// Load request/response variables from function
		HttpRequest request = function.getRequest();
		String path = function.getRequestedPath();
		String method = request.getRequestMethod();

		// Process request
		ApiRequestParams req;
		if (method.equalsIgnoreCase("GET")) {
			// Query-based
			Map<String, String> query = request.getRequestQueryParameters();
			req = new UrlEncodedParams(query);
		} else {
			try {
				// Body-based
				// Check content type header
				if (!request.hasHeader("content-type"))
					return postProcess(new FunctionResult(400, "Bad Request"), function, service);

				// Check type
				switch (request.getHeader("content-type").getValue().toLowerCase()) {

				case "text/json":
				case "application/json": {
					// JSON
					req = new JacksonObjectParams(
							jsonMapper.readValue(request.getRequestBodyAsString(), ObjectNode.class));
					break;
				}

				case "text/xml":
				case "application/xml": {
					// XML
					req = new JacksonObjectParams(
							xmlMapper.readValue(request.getRequestBodyAsString(), ObjectNode.class));
					break;
				}

				case "application/x-www-form-urlencoded": {
					// Form
					req = new UrlEncodedParams(parseForm(request.getRequestBodyAsString()));
					break;
				}

				default: {
					// Invalid type
					return postProcess(new FunctionResult(415, "Unsupported Media Type"), function, service);
				}

				}
			} catch (IOException e) {
				return postProcess(new FunctionResult(400, "Bad Request"), function, service);
			}
		}

		// Handle
		try {
			for (String func : functions.keySet()) {
				// Get function
				for (Method mth : functions.get(func.toLowerCase())) {
					// Check validity
					boolean valid = true;
					for (IMethodAnnotationProcessor<Annotation> proc : AbstractWebService
							.getMethodAnnotationProcessors()) {
						if (mth.isAnnotationPresent(proc.annotation())) {
							// Process
							try {
								if (proc.process(mth.getAnnotation(proc.annotation()), mth, function, req,
										service) == MatchResult.SKIP_METHOD) {
									valid = false;
									break;
								}
							} catch (HttpException e) {
							}
						}
					}
					if (!valid)
						continue;

					// Check method
					if (mth.isAnnotationPresent(Function.class)) {
						Function anno = mth.getAnnotation(Function.class);
						String name = mth.getName();
						if (!anno.value().equals("<auto>"))
							name = anno.value();

						// Preverify method
						boolean allowed = false;
						for (String meth : anno.allowedMethods()) {
							if (meth.equalsIgnoreCase(method)) {
								allowed = true;
								break;
							}
						}

						// Check
						if (anno.allowSubPaths()) {
							// Check sub path
							if (path.toLowerCase().equals(name.toLowerCase())
									|| path.toLowerCase().startsWith(name.toLowerCase() + "/")) {
								// Found it

								// Check method
								if (!allowed) {
									FunctionResult res = new FunctionResult(405, "Method not allowed");
									res = postProcess(res, function, service);
									return res;
								}

								// Run function
								FunctionResult res = executeFunction(mth, function, service, req);
								return res;
							}
						}
					}

					// Done
					break;
				}
			}
			if (functions.containsKey(path.toLowerCase())) {
				// Get function
				for (Method mth : functions.get(path.toLowerCase())) {
					// Check validity
					boolean valid = true;
					for (IMethodAnnotationProcessor<Annotation> proc : AbstractWebService
							.getMethodAnnotationProcessors()) {
						if (mth.isAnnotationPresent(proc.annotation())) {
							// Process
							try {
								if (proc.process(mth.getAnnotation(proc.annotation()), mth, function, req,
										service) == MatchResult.SKIP_METHOD) {
									valid = false;
									break;
								}
							} catch (HttpException e) {
							}
						}
					}
					if (!valid)
						continue;

					// Check method
					if (mth.isAnnotationPresent(Function.class)) {
						Function anno = mth.getAnnotation(Function.class);
						boolean allowed = false;
						for (String meth : anno.allowedMethods()) {
							if (meth.equalsIgnoreCase(method)) {
								allowed = true;
								break;
							}
						}
						if (!allowed) {
							FunctionResult res = new FunctionResult(405, "Method not allowed");
							res = postProcess(res, function, service);
							return res;
						}
					}

					// Run function
					FunctionResult res = executeFunction(mth, function, service, req);
					return res;
				}
			}
		} catch (HttpException e) {
			return postProcess(
					new FunctionResult(e.getStatusCode(), e.getStatusMessage(), e.getBodyMediaType(), e.getBody()),
					function, service);
		}

		// None found
		return null;
	}

	private FunctionResult executeFunction(Method mth, FunctionInfo func, AbstractWebService<?> service,
			ApiRequestParams requestParams) throws IOException {
		// Check annotation
		Object[] args = new Object[] { func };
		if (mth.isAnnotationPresent(ApiHandler.class)) {
			// Populate arguments
			args = populateArguments(mth, func, requestParams, service);
		}

		try {
			// Run function
			FunctionResult res = (FunctionResult) mth.invoke(service, args);
			res = postProcess(res, func, service);

			// Return
			return res;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Post-processes function result objects
	 * 
	 * @param res     FunctionResult instance
	 * @param func    Function information object instance
	 * @param service Webservice instance
	 * @return Post-processed FunctionResult instance
	 */
	public FunctionResult postProcess(FunctionResult res, FunctionInfo func, AbstractWebService<?> service) {
		// Post-process function result
		for (IFunctionResultPostProcessor processor : AbstractWebService.getFunctionResultPostProcessors()) {
			res = processor.postProcess(res, func, service);
		}

		// Return
		return res;
	}

	private Object[] populateArguments(Method mth, FunctionInfo fI, ApiRequestParams requestParams,
			AbstractWebService<?> service) throws IOException {
		// Create array
		Parameter[] params = mth.getParameters();
		Object[] args = new Object[params.length];
		HttpException pendingException = null;

		// Go through annotation processors
		for (IMethodAnnotationProcessor<Annotation> processor : AbstractWebService.getMethodAnnotationProcessors()) {
			if (mth.isAnnotationPresent(processor.annotation())) {
				try {
					// Process
					if (processor.process(mth.getAnnotation(processor.annotation()), mth, fI, requestParams,
							service) == MatchResult.BAD_REQUEST) {
						// Bad request
						if (pendingException == null)
							pendingException = new HttpException(400, "Bad Request");
					}
				} catch (HttpException e) {
					if (pendingException == null)
						pendingException = e;
				}
			}
		}

		// Go through parameters
		for (int i = 0; i < args.length; i++) {
			// Check
			Parameter param = params[i];

			// Check type
			if (param.getType().isAssignableFrom(FunctionInfo.class)) {
				// Function information instance
				args[i] = fI;
			} else if (param.getType().isAssignableFrom(ApiRequestParams.class)) {
				// API request params
				args[i] = requestParams;
			} else {
				boolean processed = false;

				// Go through function processors
				for (IParameterProcessor processor : AbstractWebService.getParameterProcessors()) {
					try {
						// Process
						if (processor.match(mth, param.getType(), param, fI, requestParams, service)) {
							args[i] = processor.process(mth, param.getType(), param, fI, requestParams, service);
							processed = true;
						}
					} catch (HttpException e) {
						if (pendingException == null)
							pendingException = e;
					}
				}

				// Go through annotation processors
				for (IParameterAnnotationProcessor<Annotation> processor : AbstractWebService
						.getParameterAnnotationProcessors()) {
					if (param.isAnnotationPresent(processor.annotation())) {
						try {
							// Process
							if (processor.match(mth.getAnnotation(processor.annotation()), mth, param, fI,
									requestParams, service)) {
								args[i] = processor.process(mth.getAnnotation(processor.annotation()), mth, param, fI,
										requestParams, service);
								processed = true;
							}
						} catch (HttpException e) {
							if (pendingException == null)
								pendingException = e;
						}
					}
				}

				// Check annotation
				if (param.isAnnotationPresent(RequestParam.class)) {
					// Mark processed
					processed = true;

					// Handle param
					RequestParam anno = param.getAnnotation(RequestParam.class);
					String name = anno.value();
					if (name.isEmpty())
						name = param.getName();

					// Check if present
					if (!requestParams.has(name)) {
						// Not present

						// Check
						if (anno.required())
							if (pendingException == null)
								pendingException = new HttpException(400, "Bad request");
							else {
								// Create default
								if (param.getType().isPrimitive()) {
									// Decode primitive
									switch (param.getType().getTypeName()) {

									case "boolean": {
										args[i] = false;
										break;
									}

									case "byte": {
										args[i] = (byte) 0;
										break;
									}

									case "char": {
										args[i] = (char) 0;
										break;
									}

									case "short": {
										args[i] = (short) 0;
										break;
									}

									case "int": {
										args[i] = (int) 0;
										break;
									}

									case "long": {
										args[i] = 0l;
										break;
									}

									case "float": {
										args[i] = 0f;
										break;
									}

									case "double": {
										args[i] = 0d;
										break;
									}

									}
								}
							}
					} else {
						// Decode value
						if (param.getType().isPrimitive()) {
							// Decode primitive
							switch (param.getType().getTypeName()) {

							case "boolean": {
								try {
									args[i] = requestParams.getBoolean(name);
								} catch (Exception e) {
									if (pendingException == null)
										pendingException = new HttpException(400, "Bad request");
								}
								break;
							}

							case "byte": {
								try {
									args[i] = requestParams.getByte(name);
								} catch (Exception e) {
									if (pendingException == null)
										pendingException = new HttpException(400, "Bad request");
								}
								break;
							}

							case "char": {
								try {
									args[i] = requestParams.getChar(name);
								} catch (Exception e) {
									if (pendingException == null)
										pendingException = new HttpException(400, "Bad request");
								}
								break;
							}

							case "short": {
								try {
									args[i] = requestParams.getShort(name);
								} catch (Exception e) {
									if (pendingException == null)
										pendingException = new HttpException(400, "Bad request");
								}
								break;
							}

							case "int": {
								try {
									args[i] = requestParams.getInt(name);
								} catch (Exception e) {
									if (pendingException == null)
										pendingException = new HttpException(400, "Bad request");
								}
								break;
							}

							case "long": {
								try {
									args[i] = requestParams.getLong(name);
								} catch (Exception e) {
									if (pendingException == null)
										pendingException = new HttpException(400, "Bad request");
								}
								break;
							}

							case "float": {
								try {
									args[i] = requestParams.getFloat(name);
								} catch (Exception e) {
									if (pendingException == null)
										pendingException = new HttpException(400, "Bad request");
								}
								break;
							}

							case "double": {
								try {
									args[i] = requestParams.getDouble(name);
								} catch (Exception e) {
									if (pendingException == null)
										pendingException = new HttpException(400, "Bad request");
								}
								break;
							}

							}
						} else {
							if (param.getType().isAssignableFrom(String.class)) {
								args[i] = requestParams.getString(name);
							} else {
								try {
									args[i] = requestParams.getObject(name, param.getType());
								} catch (Exception e) {
									if (pendingException == null)
										pendingException = new HttpException(400, "Bad request");
								}
							}
						}
					}
				}

				// Check result
				if (!processed) {
					// Invalid parameter
					throw new RuntimeException("Invalid parameter " + param.getName() + " in method " + mth.getName()
							+ " of " + service.getClass().getTypeName() + "!");
				}
			}
		}

		// Check exception
		if (pendingException != null)
			throw pendingException;

		// Return
		return args;

	}

	private Map<String, String> parseForm(String payload) {
		HashMap<String, String> frm = new HashMap<String, String>();
		String key = "";
		String value = "";
		boolean isKey = true;
		for (int i = 0; i < payload.length(); i++) {
			char ch = payload.charAt(i);
			if (ch == '&') {
				if (isKey && !key.isEmpty()) {
					frm.put(key, "");
					key = "";
				} else if (!isKey && !key.isEmpty()) {
					try {
						frm.put(key, URLDecoder.decode(value, "UTF-8"));
					} catch (Exception e) {
						frm.put(key, value);
					}
					isKey = true;
					key = "";
					value = "";
				}
			} else if (ch == '=') {
				isKey = !isKey;
			} else {
				if (isKey) {
					key += ch;
				} else {
					value += ch;
				}
			}
		}
		if (!key.isEmpty() || !value.isEmpty()) {
			try {
				frm.put(key, URLDecoder.decode(value, "UTF-8"));
			} catch (Exception e) {
				frm.put(key, value);
			}
		}
		return frm;
	}
}
