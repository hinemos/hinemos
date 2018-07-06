/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.CloudServiceConv;
import com.clustercontrol.utility.settings.monitor.xml.CloudServiceMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CloudServiceMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorFilterInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.xcloud.plugin.monitor.CloudServiceMonitorPlugin;

/**
 * Cloud サービス 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された Cloud サービス 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている Cloud サービス 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 *
 */
public class CloudServiceAction extends AbstractMonitorAction<CloudServiceMonitors> {
	public CloudServiceAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<CloudServiceMonitors> getDataClass() {
		return CloudServiceMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(CloudServiceMonitors colodServiceMonitors) {
		int res = CloudServiceConv.checkSchemaVersion(colodServiceMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				CloudServiceConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(CloudServiceMonitors cloudServiceMonitors) throws ConvertorException {
		return CloudServiceConv.createMonitorInfoList(cloudServiceMonitors);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		
		MonitorFilterInfo monitorFilterInfo = new MonitorFilterInfo();
		monitorFilterInfo.setMonitorTypeId(CloudServiceMonitorPlugin.monitorPluginId);
		
		List<MonitorInfo> list = MonitorSettingEndpointWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(monitorFilterInfo);
		
		return list;
	
	}

	@Override
	protected CloudServiceMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return CloudServiceConv.createCloudServiceMonitors(monitorInfoList);
	}

	@Override
	protected void sort(CloudServiceMonitors object) {
		CloudServiceMonitor[] ms = object.getCloudServiceMonitor();
		Arrays.sort(
			ms,
			new Comparator<CloudServiceMonitor>() {
				@Override
				public int compare(CloudServiceMonitor obj1, CloudServiceMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setCloudServiceMonitor(ms);
	}
}
