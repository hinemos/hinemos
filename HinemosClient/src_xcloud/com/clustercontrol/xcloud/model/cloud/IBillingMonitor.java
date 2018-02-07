/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;

public interface IBillingMonitor extends IElement {
	// プロパティの Id
	public interface p {
		static final PropertyId<CollectionObserver<MonitorInfo>> billingMonitor = new PropertyId<CollectionObserver<MonitorInfo>>("billingMonitor"){};
	}

	IBillingMonitors getBillingMonitors();

	MonitorInfo getMonitorInfo();
}
