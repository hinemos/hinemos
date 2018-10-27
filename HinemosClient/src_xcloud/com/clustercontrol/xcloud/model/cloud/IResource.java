/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.List;

import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;

public interface IResource extends IElement {
	// プロパティの Id
	public interface p {
		static final PropertyId<CollectionObserver<IExtendedProperty>> extendedProperties = new PropertyId<CollectionObserver<IExtendedProperty>>("extendedProperties", true){};
	}

	IHinemosManager getHinemosManager();
	ICloudScope getCloudScope();
	ILocation getLocation();

	IExtendedProperty[] getExtendedProperties();
	String getExtendedProperty(String name);
	List<String> getExtendedPropertyAsList(String name);
}
