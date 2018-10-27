/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

import java.util.List;


public interface IInternalContainer<C extends IInternalComponent<?, S>, S> extends IUpdatable {
	List<C> getComponents();
}
