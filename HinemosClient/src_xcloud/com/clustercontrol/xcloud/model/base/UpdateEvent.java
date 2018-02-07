/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

import com.clustercontrol.xcloud.model.base.IUpdatable.ElementListerner;

public class UpdateEvent extends Event<IElement, ElementListerner> {
	private static ListenerType<ElementListerner> TYPE = new ListenerType<ElementListerner>(){};

	public UpdateEvent(IElement source) {
		super(source);
	}
	public void dispatch(ElementListerner listener) {
		listener.elementUpdated(this);
	}
	public ListenerType<ElementListerner> getAssociatedType() {
		return getType();
	}
	public static ListenerType<ElementListerner> getType() {
		return TYPE;
	}
}
