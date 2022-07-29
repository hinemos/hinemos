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

import org.openapitools.client.model.CustomStringMonitorInfoResponse;
import org.openapitools.client.model.CustomtrapNumericMonitorInfoResponse;
import org.openapitools.client.model.CustomtrapStringMonitorInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;

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
import com.clustercontrol.utility.settings.monitor.conv.CustomTrapConv;
import com.clustercontrol.utility.settings.monitor.xml.CustomTrapMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CustomTrapMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * カスタムトラップ監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された カスタムトラップ監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている カスタムトラップ監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 * @version 6.1.0
 * @since 6.0.0
 */
public class CustomTrapAction extends AbstractMonitorAction<CustomTrapMonitors> {

	public CustomTrapAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<CustomTrapMonitors> getDataClass() {
		return CustomTrapMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(CustomTrapMonitors CustomTrapMonitors) {
		int res = CustomTrapConv.checkSchemaVersion(CustomTrapMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				CustomTrapConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(CustomTrapMonitors CustomTrapMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return CustomTrapConv.createMonitorInfoList(CustomTrapMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		List<CustomtrapNumericMonitorInfoResponse> numericList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCustomtrapNumericList(null);
		List<CustomtrapStringMonitorInfoResponse>  stringList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCustomtrapStringList(null);
		
		for(CustomtrapNumericMonitorInfoResponse numericMonitor:numericList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(numericMonitor, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		for(CustomtrapStringMonitorInfoResponse stringMonitor:stringList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(stringMonitor, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected CustomTrapMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return CustomTrapConv.createCustomTrapMonitors(monitorInfoList);
	}

	@Override
	protected void sort(CustomTrapMonitors object) {
		CustomTrapMonitor[] ms = object.getCustomTrapMonitor();
		Arrays.sort(
			ms,
			new Comparator<CustomTrapMonitor>() {
				@Override
				public int compare(CustomTrapMonitor obj1, CustomTrapMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setCustomTrapMonitor(ms);
	}
}
