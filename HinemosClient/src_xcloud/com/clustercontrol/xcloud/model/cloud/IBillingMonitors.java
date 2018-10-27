/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;

public interface IBillingMonitors extends IElement{
	public interface p {
		static final PropertyId<CollectionObserver<BillingMonitor>> billingMonitors = new PropertyId<CollectionObserver<BillingMonitor>>("billingMonitors", true){};
	}

	BillingMonitor[] getBillingMonitors();
	BillingMonitor getBillingMonitor(String billingAlarmId);
	BillingMonitor[] getBillingMonitorsWithInitializing();
	void updateBillingMonitors();
	IHinemosManager getHinemosManager();
}
