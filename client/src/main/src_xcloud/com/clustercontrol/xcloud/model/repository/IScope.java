/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.PropertyId;



public interface IScope extends IFacility {
	// プロパティの Id
	interface p {
		static final PropertyId<CollectionObserver<IFacility>> facilities = new PropertyId<CollectionObserver<IFacility>>("facilities", true){};
	}
	
	IScope getParent();
	IFacility[] getFacilities();
}
