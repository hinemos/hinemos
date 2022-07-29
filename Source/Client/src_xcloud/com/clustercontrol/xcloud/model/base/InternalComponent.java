/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

import com.clustercontrol.xcloud.model.CloudModelException;


public abstract class InternalComponent<C extends IInternalContainer<?, S>, S> extends Element implements IInternalComponent<C, S> {
	private C container;
	
	public InternalComponent(C container) {
		this.container = container;
	}
	
	public C getContainer() {
		return container;
	}
	
	@Override
	public void update() throws CloudModelException {
		S source = getSource();

		if (source != null) {
			internalUpdate(source);
		}
	}

	public abstract S getSource() throws CloudModelException;
	
	@Override
	public void internalUpdate(S source) {
		overwrite(source);
		fireEvent(new UpdateEvent(this));
	}

	protected abstract void overwrite(S source);
}
