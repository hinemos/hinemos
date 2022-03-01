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
import org.openapitools.client.model.SnmpNumericMonitorInfoResponse;
import org.openapitools.client.model.SnmpStringMonitorInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.conv.SnmpConv;
import com.clustercontrol.utility.settings.monitor.xml.SnmpMonitor;
import com.clustercontrol.utility.settings.monitor.xml.SnmpMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * SNMP 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された SNMP 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている SNMP 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class SnmpAction extends AbstractMonitorAction<SnmpMonitors> {
	public SnmpAction() throws ConvertorException {
		super();
	}

	@Override
	protected boolean checkSchemaVersionScope(SnmpMonitors object) {
		int res = SnmpConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				SnmpConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	protected Class<SnmpMonitors> getDataClass() {
		return SnmpMonitors.class;
	}

	@Override
	protected List<MonitorInfoResponse> createMonitorInfoList(SnmpMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return SnmpConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList()
			throws HinemosUnknown, InvalidRole,
			InvalidUserPass, MonitorNotFound, RestConnectFailed {
		
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		
		List<SnmpNumericMonitorInfoResponse> snmpNumericList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSnmpNumericList(null);
		List<SnmpStringMonitorInfoResponse> snmpStringList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSnmpStringList(null);
		
		for(SnmpNumericMonitorInfoResponse snmpNumeric:snmpNumericList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(snmpNumeric, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		for(SnmpStringMonitorInfoResponse snmpString:snmpStringList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(snmpString, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected SnmpMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return SnmpConv.createSnmpMonitors(monitorInfoList);
	}

	@Override
	protected void sort(SnmpMonitors object) {
		SnmpMonitor[] ms = object.getSnmpMonitor();
		Arrays.sort(
			ms,
			new Comparator<SnmpMonitor>() {
				@Override
				public int compare(SnmpMonitor obj1, SnmpMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setSnmpMonitor(ms);
	}
}