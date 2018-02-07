/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.base.ValueObserver;

public interface IEndpoint extends IElement {
	// プロパティの Id
	public interface p {
		static final PropertyId<ValueObserver<ILocation>> location = new PropertyId<ValueObserver<ILocation>>("location"){};
		static final PropertyId<ValueObserver<String>> id = new PropertyId<ValueObserver<String>>("id"){};
		static final PropertyId<ValueObserver<String>> url = new PropertyId<ValueObserver<String>>("url"){};
	}
	
	ILocation getLocation();
	
	String getId();
	String getUrl();
}
