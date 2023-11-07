package org.asf.nexus.webservices.requestparams.impl;

import java.util.HashMap;

import org.asf.nexus.webservices.ApiRequestParams;
import org.asf.nexus.webservices.requestparams.ParamValueProvider;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonObjectParams implements ApiRequestParams {

	private HashMap<String, ParamValueProvider> providers = new HashMap<String, ParamValueProvider>();

	public JacksonObjectParams(ObjectNode node) {
		// Add providers
		for (String key : node.properties().stream().map(t -> t.getKey()).toArray(t -> new String[t])) {
			setProvider(key, new JacksonObjectParamsProvider(node.get(key)));
		}
	}

	@Override
	public String[] keys() {
		return providers.keySet().toArray(t -> new String[t]);
	}

	@Override
	public boolean has(String key) {
		return providers.containsKey(key);
	}

	@Override
	public ParamValueProvider getProvider(String key) {
		return providers.get(key);
	}

	@Override
	public void setProvider(String key, ParamValueProvider provider) {
		providers.put(key, provider);
	}

}
