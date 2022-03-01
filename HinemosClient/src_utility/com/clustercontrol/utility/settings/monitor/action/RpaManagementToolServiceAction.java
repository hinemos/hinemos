/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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

import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.RpaManagementToolMonitorInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.RpaManagementToolServiceConv;
import com.clustercontrol.utility.settings.monitor.xml.RpaManagementToolServiceMonitor;
import com.clustercontrol.utility.settings.monitor.xml.RpaManagementToolServiceMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * RPA管理ツール 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された RPA管理ツール 監視情報をマネージャに反映させるクラス<br>
 *
 */
public class RpaManagementToolServiceAction extends AbstractMonitorAction<RpaManagementToolServiceMonitors> {

	public RpaManagementToolServiceAction() throws ConvertorException {
		super();
	}

	@Override
	protected Class<RpaManagementToolServiceMonitors> getDataClass() {
		return RpaManagementToolServiceMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(RpaManagementToolServiceMonitors object) {
		int res = RpaManagementToolServiceConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				RpaManagementToolServiceConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	protected List<MonitorInfoResponse> createMonitorInfoList(RpaManagementToolServiceMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return RpaManagementToolServiceConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList()
			throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<RpaManagementToolMonitorInfoResponse> resDtoList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRpaManagementToolServiceList();
		List<MonitorInfoResponse> retList = new ArrayList<MonitorInfoResponse>();
		for(RpaManagementToolMonitorInfoResponse recSrc :resDtoList){
			MonitorInfoResponse recDest = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(recSrc, recDest);
			retList.add(recDest);
		}
		return retList;
	}

	@Override
	protected RpaManagementToolServiceMonitors createCastorData(
			List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return RpaManagementToolServiceConv.createRpaManagementToolServiceMonitors(monitorInfoList);
	}

	@Override
	protected void sort(RpaManagementToolServiceMonitors object) {
		RpaManagementToolServiceMonitor[] ms = object.getRpaManagementToolServiceMonitor();
		Arrays.sort(
			ms,
			new Comparator<RpaManagementToolServiceMonitor>() {
				@Override
				public int compare(RpaManagementToolServiceMonitor obj1, RpaManagementToolServiceMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setRpaManagementToolServiceMonitor(ms);
	}
}