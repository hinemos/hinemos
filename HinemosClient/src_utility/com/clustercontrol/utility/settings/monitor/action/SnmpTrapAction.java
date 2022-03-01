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

import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.SnmptrapMonitorInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.conv.SnmpTrapConv;
import com.clustercontrol.utility.settings.monitor.xml.SnmpTrapMonitors;
import com.clustercontrol.utility.settings.monitor.xml.TrapMonitor;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * SNMPTRAP 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された SNMPTRAP 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている SNMPTRAP 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class SnmpTrapAction extends AbstractMonitorAction<SnmpTrapMonitors> {
	public SnmpTrapAction() throws ConvertorException {
		super();
	}

	@Override
	protected boolean checkSchemaVersionScope(SnmpTrapMonitors object) {
		int res = SnmpTrapConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				SnmpTrapConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	protected Class<SnmpTrapMonitors> getDataClass() {
		return SnmpTrapMonitors.class;
	}

	@Override
	protected List<MonitorInfoResponse> createMonitorInfoList(SnmpTrapMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return SnmpTrapConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList()
			throws HinemosUnknown, InvalidRole,
			InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		List<SnmptrapMonitorInfoResponse> snmptrapMonitorInfoList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSnmptrapList(null);
		for(SnmptrapMonitorInfoResponse snmptrapMonitorInfo:snmptrapMonitorInfoList){
			MonitorInfoResponse monitorInfoResponse = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(snmptrapMonitorInfo, monitorInfoResponse);
			monitorInfoList.add(monitorInfoResponse);
		}
		return monitorInfoList;
	}

	@Override
	protected SnmpTrapMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return SnmpTrapConv.createSnmpTrapMonitors(monitorInfoList);
	}

	@Override
	protected void sort(SnmpTrapMonitors object) {
		TrapMonitor[] ms = object.getTrapMonitor();
		Arrays.sort(
			ms,
			new Comparator<TrapMonitor>() {
				@Override
				public int compare(TrapMonitor obj1, TrapMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setTrapMonitor(ms);
	}
}