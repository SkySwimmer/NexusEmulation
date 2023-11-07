package org.asf.nexus.webservices.functions.processors;

public enum MatchResult {

	/**
	 * Causes a 400 bad request error
	 */
	BAD_REQUEST,

	/**
	 * Skips the method during function selection
	 */
	SKIP_METHOD,

	/**
	 * Success
	 */
	SUCCESS

}
