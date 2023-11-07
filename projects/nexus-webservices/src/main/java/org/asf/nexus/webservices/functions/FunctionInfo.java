package org.asf.nexus.webservices.functions;

import java.util.HashMap;
import java.util.Map;

import org.asf.connective.ConnectiveHttpServer;
import org.asf.connective.RemoteClient;
import org.asf.connective.objects.HttpRequest;
import org.asf.connective.objects.HttpResponse;
import org.asf.nexus.webservices.cookies.CookieContext;

/**
 * 
 * Function call information, contains request information.
 * 
 * @author Sky Swimmer
 *
 */
public class FunctionInfo {
	private String path = null;

	private HttpRequest request = null;
	private HttpResponse response = null;

	private ConnectiveHttpServer server = null;
	private String method = null;

	private RemoteClient client = null;

	private CookieContext cookies = null;

	public FunctionInfo(String path, HttpRequest request, HttpResponse response, ConnectiveHttpServer server,
			String method, RemoteClient client, CookieContext cookies) {
		this.path = path;
		this.request = request;
		this.response = response;
		this.server = server;
		this.method = method;
		this.client = client;
		this.cookies = cookies;
	}

	/**
	 * Retrieves the HTTP cookies associated with the request
	 * 
	 * @return CookieContext instance
	 */
	public CookieContext getCookies() {
		return cookies;
	}

	/**
	 * Retrieves the client that is making the request
	 * 
	 * @return RemoteClient instance
	 */
	public RemoteClient getClient() {
		return client;
	}

	/**
	 * Retrieves the request method
	 * 
	 * @return Request method string
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Retrieves the request query as a map (read-only)
	 * 
	 * @return Map of query variables
	 */
	public Map<String, String> getQuery() {
		return request.getRequestQueryParameters();
	}

	/**
	 * Retrieves the requested path relative to the API processor
	 * 
	 * @return Request path string relative to the API processor path
	 */
	public String getRequestedPath() {
		return path;
	}

	/**
	 * Retrieves the HTTP response instance
	 * 
	 * @return HttpResponse instance
	 */
	public HttpResponse getResponse() {
		return response;
	}

	/**
	 * Retrieves the HTTP request instance
	 * 
	 * @return HttpRequest instance
	 */
	public HttpRequest getRequest() {
		return request;
	}

	/**
	 * Retrieves the HTTP server instance
	 * 
	 * @return ConnectiveHttpServer instance
	 */
	public ConnectiveHttpServer getServer() {
		return server;
	}

	private HashMap<String, Object> processorMemory = new HashMap<String, Object>();

	/**
	 * Retrieves processor memory objects
	 * 
	 * @param <T>  Object type
	 * @param type Object class
	 * @return Object instance or null
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProcessorMemoryObject(Class<T> type) {
		return (T) processorMemory.get(type.getTypeName());
	}

	/**
	 * Stores processor memory objects
	 * 
	 * @param <T>    Object type
	 * @param type   Object class
	 * @param object Object instance
	 */
	public <T> void setProcessorMemoryObject(Class<T> type, T object) {
		processorMemory.put(type.getTypeName(), object);
	}

	/**
	 * Removes processor memory objects
	 * 
	 * @param <T>  Object type
	 * @param type Object class
	 */
	public <T> void removeProcessorMemoryObject(Class<T> type) {
		processorMemory.remove(type.getTypeName());
	}
}
