/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.text.ParseException;
import java.util.ArrayList;
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
import com.clustercontrol.utility.settings.monitor.conv.BillingConv;
import com.clustercontrol.utility.settings.monitor.xml.BillingMonitor;
import com.clustercontrol.utility.settings.monitor.xml.BillingMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.xcloud.plugin.monitor.PlatformServiceBillingDetailMonitorPlugin;
import com.clustercontrol.xcloud.plugin.monitor.PlatformServiceBillingMonitorPlugin;

/**
 * 課金 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された 課金   監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている 課金  監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 6.0.0
 * 
 */
public class BillingAction extends AbstractMonitorAction<BillingMonitors> {
	public BillingAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<BillingMonitors> getDataClass() {
		return BillingMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(BillingMonitors object) {
		int res = BillingConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				BillingConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(BillingMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, RestConnectFailed, ParseException {
		return BillingConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, RestConnectFailed {
		
		GetMonitorListRequest dtoReq = new GetMonitorListRequest();
		MonitorFilterInfoRequest monitorFilterInfo = new MonitorFilterInfoRequest();
		dtoReq.setMonitorFilterInfo(monitorFilterInfo);
		
		monitorFilterInfo.setMonitorTypeId(PlatformServiceBillingMonitorPlugin.monitorPluginId);
		List<MonitorInfoResponse> list = MonitorsettingRestClientWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(dtoReq);

		monitorFilterInfo.setMonitorTypeId(PlatformServiceBillingDetailMonitorPlugin.monitorPluginId);
		List<MonitorInfoResponse> list2 = MonitorsettingRestClientWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(dtoReq);

		list.addAll(list2);
		return list;
	}

	@Override
	protected BillingMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return BillingConv.createBillingMonitors(monitorInfoList);
	}

	@Override
	protected void sort(BillingMonitors object) {
		BillingMonitor[] ms = object.getBillingMonitor();
		Arrays.sort(
			ms,
			new Comparator<BillingMonitor>() {
				@Override
				public int compare(BillingMonitor obj1, BillingMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setBillingMonitor(ms);
	}
}
