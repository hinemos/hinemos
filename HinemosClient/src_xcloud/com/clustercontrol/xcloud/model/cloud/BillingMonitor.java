/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.xcloud.model.base.Element;

public class BillingMonitor extends Element implements IBillingMonitor{

	private MonitorInfoResponse monitorInfo;

	public BillingMonitor(BillingMonitors billingAlarms){
		setOwner(billingAlarms);
	}

	public BillingMonitors getBillingMonitors() {
		return (BillingMonitors)getOwner();
	}
	
	public void update(MonitorInfoResponse source) {
		setMonitorInfo(source);
	}
	@Override
	public MonitorInfoResponse getMonitorInfo() {
		return monitorInfo;
	}
	public void setMonitorInfo(MonitorInfoResponse source) {
		this.monitorInfo = source;
	}
	
	public static BillingMonitor convert(BillingMonitors billingAlarms, MonitorInfoResponse source) {
		BillingMonitor billingAlarm = new BillingMonitor(billingAlarms);
		billingAlarm.update(source);
		return billingAlarm;
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IHinemosManager.class) {
			if (getBillingMonitors() != null)
				return getBillingMonitors().getHinemosManager();
		}
		return super.getAdapter(adapter);
	}
}
