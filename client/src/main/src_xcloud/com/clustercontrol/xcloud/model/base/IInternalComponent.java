/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;


public interface IInternalComponent<C extends IInternalContainer<?, S>, S> extends IUpdatable {
	C getContainer();
	
	boolean equalValues(S source);

	void internalUpdate(S source);
}
