package org.asf.nexus.webservices.requestparams.impl;

import java.io.IOException;

import org.asf.nexus.webservices.requestparams.ParamValueProvider;
import org.asf.nexus.webservices.requestparams.ParamValueType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonObjectParamsProvider implements ParamValueProvider {

	private JsonNode node;
	private static ObjectMapper mapper = new ObjectMapper();

	public JacksonObjectParamsProvider(JsonNode node) {
		this.node = node;
	}

	@Override
	public Object provide(ParamValueType type, Class<?> cls) {
		switch (type) {

		case BOOLEAN:
			return node.asBoolean();

		case BYTE:
			try {
				byte[] bytes = node.binaryValue();
				if (bytes == null)
					return (byte) node.asInt();
				return bytes[0];
			} catch (IOException e) {
				return (byte) node.asInt();
			}

		case CHARACTER:
			return node.asText().charAt(0);

		case DOUBLE:
			return node.doubleValue();

		case FLOAT:
			return node.floatValue();

		case INTEGER:
			return node.intValue();

		case LONG:
			return node.longValue();

		case SHORT:
			return node.shortValue();

		case STRING:
			return node.asText();

		case OBJECT:
			return mapper.convertValue(node, cls);

		}

		// Default
		return null;
	}

}
