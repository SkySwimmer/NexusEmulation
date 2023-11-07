package org.asf.nexus.webservices.requestparams.impl;

import java.util.HashMap;
import java.util.Map;

import org.asf.nexus.webservices.ApiRequestParams;
import org.asf.nexus.webservices.requestparams.ParamValueProvider;

public class UrlEncodedParams implements ApiRequestParams {

	private HashMap<String, ParamValueProvider> providers = new HashMap<String, ParamValueProvider>();

	public UrlEncodedParams(Map<String, String> form) {
		// Add providers
		for (String key : form.keySet()) {
			setProvider(key, new UrlEncodedParamsProvider(form.get(key)));
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
