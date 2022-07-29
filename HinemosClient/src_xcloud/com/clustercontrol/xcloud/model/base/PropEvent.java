/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

public abstract class PropEvent<P, O extends PropertyObserver<P>> {
	private Object source;
	private PropertyId<O> pid;
	public PropEvent(Object source, PropertyId<O> pid) {
		this.source = source;
		this.pid = pid;
	}
	public Object getSource() {
		return source;
	}
	public PropertyId<O> getPropertyId() {
		return pid;
	}
	public abstract void dispatch(O observer);
}
