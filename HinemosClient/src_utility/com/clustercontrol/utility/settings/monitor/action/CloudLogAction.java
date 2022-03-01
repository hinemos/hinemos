/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.openapitools.client.model.GetMonitorListRequest;
import org.openapitools.client.model.MonitorFilterInfoRequest;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.CloudLogConv;
import com.clustercontrol.utility.settings.monitor.xml.CloudLogMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CloudLogMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.xcloud.plugin.monitor.CloudLogMonitorPlugin;

/**
 * Cloud ログ 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された Cloud ログ 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている Cloud ログ 監視情報と重複する場合はスキップされる。
 *
 */
public class CloudLogAction extends AbstractMonitorAction<CloudLogMonitors> {
	public CloudLogAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<CloudLogMonitors> getDataClass() {
		return CloudLogMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(CloudLogMonitors colodLogMonitors) {
		int res = CloudLogConv.checkSchemaVersion(colodLogMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				CloudLogConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(CloudLogMonitors cloudLogMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, RestConnectFailed, ParseException {
		return CloudLogConv.createMonitorInfoList(cloudLogMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, RestConnectFailed {
		
		MonitorFilterInfoRequest monitorFilterInfo = new MonitorFilterInfoRequest();
		monitorFilterInfo.setMonitorTypeId(CloudLogMonitorPlugin.monitorPluginId);
		GetMonitorListRequest dtoReq = new GetMonitorListRequest();
		dtoReq.setMonitorFilterInfo(monitorFilterInfo);
		
		List<MonitorInfoResponse> list = MonitorsettingRestClientWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(dtoReq);
		
		return list;
	
	}

	@Override
	protected CloudLogMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return CloudLogConv.createCloudLogMonitors(monitorInfoList);
	}

	@Override
	protected void sort(CloudLogMonitors object) {
		CloudLogMonitor[] ms = object.getCloudLogMonitor();
		Arrays.sort(
			ms,
			new Comparator<CloudLogMonitor>() {
				@Override
				public int compare(CloudLogMonitor obj1, CloudLogMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setCloudLogMonitor(ms);
	}
}
