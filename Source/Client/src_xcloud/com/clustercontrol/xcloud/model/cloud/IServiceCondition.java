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

public interface IServiceCondition extends IElement {
	// プロパティの Id
	public interface p {
		static final PropertyId<ValueObserver<String>> name = new PropertyId<ValueObserver<String>>("name"){};
		static final PropertyId<ValueObserver<String>> status = new PropertyId<ValueObserver<String>>("status"){};
		static final PropertyId<ValueObserver<String>> detail = new PropertyId<ValueObserver<String>>("detail"){};
	}

	String getId();
	String getName();
	String getStatus();
	String getDetail();
}
