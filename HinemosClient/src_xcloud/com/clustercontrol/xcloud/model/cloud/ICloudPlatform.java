/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.List;

import org.openapitools.client.model.CloudSpecResponse;

import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.base.ValueObserver;

public interface ICloudPlatform extends IElement {
	public interface p {
		static final PropertyId<ValueObserver<String>> id = new PropertyId<ValueObserver<String>>("id"){};
		static final PropertyId<ValueObserver<String>> name = new PropertyId<ValueObserver<String>>("name"){};
		static final PropertyId<ValueObserver<String>> description = new PropertyId<ValueObserver<String>>("description"){};
		static final PropertyId<ValueObserver<CloudSpecResponse>> cloudSpec = new PropertyId<ValueObserver<CloudSpecResponse>>("cloudSpec"){};
	}
	
	String getId();
	String getName();
	String getDescription();
	CloudSpecResponse getCloudSpec();
	
	IHinemosManager getHinemosManager();
	List<ICloudScope> getChildCloudScopes();
}
