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
import com.clustercontrol.ws.monitor.MonitorFilterInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.plugin.monitor.PlatformServiceBillingDetailMonitorPlugin;


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
		
		if (!billingMonitors.isEmpty())
			billingMonitors.clear();
		
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(getHinemosManager().getManagerName());
		
		MonitorFilterInfo filter = new MonitorFilterInfo();
		filter.setMonitorTypeId(PlatformServiceBillingDetailMonitorPlugin.monitorPluginId);
		
		try {
			List<MonitorInfo> monitors = wrapper.getMonitorListByCondition(filter);
			for (MonitorInfo info : monitors) {
				BillingMonitor billingMonitor = new BillingMonitor(this);
				billingMonitor.setMonitorInfo(info);
				billingMonitors.add(billingMonitor);
			}
		} catch (com.clustercontrol.ws.monitor.InvalidRole_Exception
					| com.clustercontrol.ws.monitor.InvalidUserPass_Exception e) {
			logger.debug("Not have enough permission.");
		} catch (MonitorNotFound_Exception e) {
			logger.debug("Billing monitor is not found.");
		} catch (HinemosUnknown_Exception e) {
			logger.warn("Unexpected internal failure occurred in Hinemos Manager.");
		}
//		try {
//			List<MonitorInfo> webBillingAlarm = wrapper.getMonitorListByCondition(filter);
//			CollectionComparator.compareCollection(billingAlarms, webBillingAlarm, new CollectionComparator.Comparator<BillingMonitor, MonitorInfo>() {
//				public boolean match(BillingMonitor o1, MonitorInfo o2) {return o1.getMonitorInfo().equals(o2);}
//				public void matched(BillingMonitor o1, MonitorInfo o2) {o1.update(o2);}
//				public void afterO1(BillingMonitor o1) {internalRemoveProperty(p.billingAlarms, o1, billingAlarms);}
//				public void afterO2(MonitorInfo o2) {
//					BillingMonitor newBillingAlarm = BillingMonitor.convert(BillingMonitors.this, o2);
//					internalAddProperty(p.billingAlarms, newBillingAlarm, billingAlarms);
//				}
//			});
//		} catch (HinemosUnknown_Exception | MonitorNotFound_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
//			Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
//		}
	}
}
