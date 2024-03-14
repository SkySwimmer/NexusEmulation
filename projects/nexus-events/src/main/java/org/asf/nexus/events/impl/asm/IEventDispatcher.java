package org.asf.nexus.events.impl.asm;

import org.asf.nexus.events.EventObject;
import org.asf.nexus.events.IEventReceiver;

public interface IEventDispatcher {

	public void dispatch(IEventReceiver receiver, EventObject event);

}
