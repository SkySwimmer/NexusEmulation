package org.asf.nexus.events.impl.asm;

import org.asf.nexus.events.SupplierEventObject;

public interface IStaticSupplierEventDispatcher {

	public Object dispatch(SupplierEventObject<?> event);

}
