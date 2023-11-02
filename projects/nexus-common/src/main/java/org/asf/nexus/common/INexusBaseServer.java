package org.asf.nexus.common;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

/**
 * 
 * Base Server Interface
 * 
 * @author Sky Swimmer
 * 
 */
public interface INexusBaseServer {

	/**
	 * Retrieves the server logger
	 * 
	 * @return Logger instance
	 */
	public Logger getLogger();

	/**
	 * Retrieves the server version
	 * 
	 * @return Server version string
	 */
	public String getVersion();

	/**
	 * Called to set up the server
	 * 
	 * @throws IOException If setup fails
	 */
	public void setupServer() throws IOException;

	/**
	 * Starts the server
	 */
	public void startServer() throws IOException;

	/**
	 * Stops the server
	 */
	public void stopServer();

	/**
	 * Stops the server forcefully
	 */
	public void killServer();

	/**
	 * Checks if the server is running
	 * 
	 * @return True if running, false otherwise
	 */
	public boolean isRunning();

	/**
	 * Waits for the server to quit
	 */
	public void waitForExit();

}
