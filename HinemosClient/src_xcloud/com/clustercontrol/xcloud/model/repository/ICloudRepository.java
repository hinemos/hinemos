/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;



public interface ICloudRepository extends IElement {
	public interface p {
		static final PropertyId<CollectionObserver<ICloudScopeRootScope>> rootScopes = new PropertyId<CollectionObserver<ICloudScopeRootScope>>("rootScopes", true){};
	}
	
	IHinemosManager getHinemosManager();
	ICloudScopeRootScope[] getRootScopes();
}
