package org.asf.nexus.webservices;

import org.asf.nexus.webservices.requestparams.ParamValueProvider;
import org.asf.nexus.webservices.requestparams.ParamValueType;

/**
 * 
 * Request content interface
 * 
 * @author Sky Swimmer
 * 
 */
public interface ApiRequestParams {

	/**
	 * Request keys
	 * 
	 * @return Array of request key strings
	 */
	public String[] keys();

	/**
	 * Checks if the parameters have a specific field
	 * 
	 * @param key Field key
	 * @return True if present, false otherwise
	 */
	public boolean has(String key);

	/**
	 * Retrieves parameter value providers
	 * 
	 * @param key Parameter key
	 * @return ParamValueProvider instance
	 */
	public ParamValueProvider getProvider(String key);

	/**
	 * Assigns parameter value providers
	 * 
	 * @param key      Parameter key
	 * @param provider New parameter value provider
	 */
	public void setProvider(String key, ParamValueProvider provider);

	/**
	 * Retrieves values by key
	 * 
	 * @param key Parameter key
	 * @return Parameter value
	 */
	public default String getString(String key) {
		ParamValueProvider prov = getProvider(key);
		if (prov == null)
			return null;
		return (String) prov.provide(ParamValueType.STRING, String.class);
	}

	/**
	 * Retrieves values by key
	 * 
	 * @param key Parameter key
	 * @return Parameter value
	 */
	public default boolean getBoolean(String key) {
		ParamValueProvider prov = getProvider(key);
		if (prov == null)
			return false;
		return (boolean) prov.provide(ParamValueType.BOOLEAN, Boolean.class);
	}

	/**
	 * Retrieves values by key
	 * 
	 * @param key Parameter key
	 * @return Parameter value
	 */
	public default byte getByte(String key) {
		ParamValueProvider prov = getProvider(key);
		if (prov == null)
			return 0;
		return (byte) prov.provide(ParamValueType.BYTE, Byte.class);
	}

	/**
	 * Retrieves values by key
	 * 
	 * @param key Parameter key
	 * @return Parameter value
	 */
	public default char getChar(String key) {
		ParamValueProvider prov = getProvider(key);
		if (prov == null)
			return 0;
		return (char) prov.provide(ParamValueType.CHARACTER, Character.class);
	}

	/**
	 * Retrieves values by key
	 * 
	 * @param key Parameter key
	 * @return Parameter value
	 */
	public default double getDouble(String key) {
		ParamValueProvider prov = getProvider(key);
		if (prov == null)
			return 0;
		return (double) prov.provide(ParamValueType.DOUBLE, Double.class);
	}

	/**
	 * Retrieves values by key
	 * 
	 * @param key Parameter key
	 * @return Parameter value
	 */
	public default float getFloat(String key) {
		ParamValueProvider prov = getProvider(key);
		if (prov == null)
			return 0;
		return (float) prov.provide(ParamValueType.FLOAT, Float.class);
	}

	/**
	 * Retrieves values by key
	 * 
	 * @param key Parameter key
	 * @return Parameter value
	 */
	public default int getInt(String key) {
		ParamValueProvider prov = getProvider(key);
		if (prov == null)
			return 0;
		return (int) prov.provide(ParamValueType.INTEGER, Integer.class);
	}

	/**
	 * Retrieves values by key
	 * 
	 * @param key Parameter key
	 * @return Parameter value
	 */
	public default long getLong(String key) {
		ParamValueProvider prov = getProvider(key);
		if (prov == null)
			return 0;
		return (long) prov.provide(ParamValueType.LONG, Long.class);
	}

	/**
	 * Retrieves values by key
	 * 
	 * @param key Parameter key
	 * @return Parameter value
	 */
	public default double getShort(String key) {
		ParamValueProvider prov = getProvider(key);
		if (prov == null)
			return 0;
		return (short) prov.provide(ParamValueType.SHORT, Short.class);
	}

	/**
	 * Retrieves values by key (deserializes objects)
	 * 
	 * @param key  Parameter key
	 * @param type Object type
	 * @return Parameter value
	 */
	public default Object getObject(String key, Class<?> type) {
		ParamValueProvider prov = getProvider(key);
		if (prov == null)
			return 0;
		return prov.provide(ParamValueType.OBJECT, type);
	}

}
