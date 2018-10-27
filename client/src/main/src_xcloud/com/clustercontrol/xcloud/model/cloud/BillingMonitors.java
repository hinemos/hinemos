/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorFilterInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.plugin.monitor.PlatformServiceBillingDetailMonitorPlugin;
import com.clustercontrol.xcloud.util.CollectionComparator;


public class BillingMonitors extends Element implements IBillingMonitors {
	
	private static final Log logger = LogFactory.getLog(BillingMonitors.class);
	
	protected List<BillingMonitor> billingMonitors;
	
	public BillingMonitors(HinemosManager hinemosManager) {
		setOwner(hinemosManager);
	}

	@Override
	public BillingMonitor[] getBillingMonitors() {
		if (billingMonitors == null)
			return new BillingMonitor[]{};
		return billingMonitors.toArray(new BillingMonitor[billingMonitors.size()]);
	}

	@Override
	public BillingMonitor[] getBillingMonitorsWithInitializing() {
		if (billingMonitors == null)
			updateBillingMonitors();
		return billingMonitors.toArray(new BillingMonitor[billingMonitors.size()]);
	}

	@Override
	public BillingMonitor getBillingMonitor(String alarmId) {
		for (BillingMonitor billingAlarm: billingMonitors) {
			if (billingAlarm.getMonitorInfo().getMonitorId().equals(alarmId)) {
				return billingAlarm;
			}
		}
		return null;
	}
	
	@Override
	public HinemosManager getHinemosManager() {
		return (HinemosManager)getOwner();
	}
	
	@Override
	public void updateBillingMonitors() {
		if (billingMonitors == null)
			billingMonitors = new ArrayList<>();
		
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(getHinemosManager().getManagerName());
		
		MonitorFilterInfo filter = new MonitorFilterInfo();
		filter.setMonitorTypeId(PlatformServiceBillingDetailMonitorPlugin.monitorPluginId);
		
		try {
			List<MonitorInfo> webBillingAlarm = wrapper.getMonitorListByCondition(filter);
			CollectionComparator.compareCollection(billingMonitors, webBillingAlarm, new CollectionComparator.Comparator<BillingMonitor, MonitorInfo>() {
				public boolean match(BillingMonitor o1, MonitorInfo o2) {return o1.getMonitorInfo().equals(o2);}
				public void matched(BillingMonitor o1, MonitorInfo o2) {o1.update(o2);}
				public void afterO1(BillingMonitor o1) {internalRemoveProperty(p.billingMonitors, o1, billingMonitors);}
				public void afterO2(MonitorInfo o2) {
					BillingMonitor newBillingAlarm = BillingMonitor.convert(BillingMonitors.this, o2);
					internalAddProperty(p.billingMonitors, newBillingAlarm, billingMonitors);
				}
			});
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception
				| MonitorNotFound_Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}
}
