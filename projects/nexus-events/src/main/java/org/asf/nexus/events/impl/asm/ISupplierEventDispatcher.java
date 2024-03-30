package org.asf.nexus.events.impl.asm;

import org.asf.nexus.events.SupplierEventObject;
import org.asf.nexus.events.IEventReceiver;

public interface ISupplierEventDispatcher {

	public Object dispatch(IEventReceiver receiver, SupplierEventObject<?> event);

}
