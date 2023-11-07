package org.asf.nexus.webservices.functions.processors.impl;

import java.lang.reflect.Method;

import org.asf.nexus.common.experiments.ExperimentManager;
import org.asf.nexus.webservices.AbstractWebService;
import org.asf.nexus.webservices.ApiRequestParams;
import org.asf.nexus.webservices.functions.FunctionInfo;
import org.asf.nexus.webservices.functions.annotations.ExperimentalFeature;
import org.asf.nexus.webservices.functions.processors.IMethodAnnotationProcessor;
import org.asf.nexus.webservices.functions.processors.MatchResult;

public class ExperimentalFeatureAnnotationProcessor implements IMethodAnnotationProcessor<ExperimentalFeature> {

	@Override
	public Class<ExperimentalFeature> annotation() {
		return ExperimentalFeature.class;
	}

	@Override
	public MatchResult process(ExperimentalFeature annotation, Method meth, FunctionInfo function,
			ApiRequestParams apiRequestParams, AbstractWebService<?> webservice) {
		String key = annotation.value();
		if (annotation.isReverse()) {
			// Check
			if (ExperimentManager.getInstance().isExperimentEnabled(key))
				return MatchResult.SKIP_METHOD;
		} else {
			// Check
			if (!ExperimentManager.getInstance().isExperimentEnabled(key))
				return MatchResult.SKIP_METHOD;
		}
		return MatchResult.SUCCESS;
	}

}
