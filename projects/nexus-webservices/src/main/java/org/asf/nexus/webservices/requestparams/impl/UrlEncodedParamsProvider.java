package org.asf.nexus.webservices.requestparams.impl;

import org.asf.nexus.webservices.requestparams.ParamValueProvider;
import org.asf.nexus.webservices.requestparams.ParamValueType;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class UrlEncodedParamsProvider implements ParamValueProvider {

	private String val;

	private static XmlMapper xmlMapper = new XmlMapper();
	private static ObjectMapper jsonMapper = new ObjectMapper();
	static {
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
		xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		jsonMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}

	public UrlEncodedParamsProvider(String val) {
		this.val = val;
	}

	@Override
	public Object provide(ParamValueType type, Class<?> cls) {
		switch (type) {

		case BOOLEAN:
			if (val.equalsIgnoreCase("true"))
				return true;
			else if (val.equalsIgnoreCase("false"))
				return false;
			else
				throw new IllegalArgumentException("Invalid boolean value");

		case BYTE:
			try {
				return Byte.parseByte(val);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid byte value");
			}

		case CHARACTER:
			try {
				if (val.length() != 1)
					throw new Exception();
				return val.charAt(0);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid char value");
			}

		case DOUBLE:
			try {
				return Double.parseDouble(val);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid double value");
			}

		case FLOAT:
			try {
				return Float.parseFloat(val);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid float value");
			}

		case INTEGER:
			try {
				return Integer.parseInt(val);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid int value");
			}

		case LONG:
			try {
				return Long.parseLong(val);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid long value");
			}

		case SHORT:
			try {
				return Short.parseShort(val);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid short value");
			}

		case STRING:
			return val;

		case OBJECT:
			try {
				if (!val.startsWith("<"))
					return jsonMapper.readValue(val, cls);
				else
					return xmlMapper.readValue(val, cls);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid object value");
			}

		}

		// Default
		return null;
	}

}
