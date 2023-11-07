package org.asf.nexus.webservices.requestparams;

public interface ParamValueProvider {

	public Object provide(ParamValueType type, Class<?> cls);

}
