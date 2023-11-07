package org.asf.nexus.common.services;

import org.asf.nexus.events.IEventReceiver;

/**
 * 
 * Abstract Service Class
 * 
 * @author Sky Swimmer
 *
 */
public abstract class AbstractService implements IEventReceiver {

	boolean inited;

	/**
	 * Called to initialize the service
	 */
	public abstract void initService();

}
