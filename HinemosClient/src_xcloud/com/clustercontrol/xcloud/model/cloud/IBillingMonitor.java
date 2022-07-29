/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;

public interface IBillingMonitor extends IElement {
	// プロパティの Id
	public interface p {
		static final PropertyId<CollectionObserver<MonitorInfoResponse>> billingMonitor = new PropertyId<CollectionObserver<MonitorInfoResponse>>("billingMonitor"){};
	}

	IBillingMonitors getBillingMonitors();

	MonitorInfoResponse getMonitorInfo();
}
