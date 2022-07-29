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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openapitools.client.model.ImportMonitorCommonRecordRequest;
import org.openapitools.client.model.ImportMonitorCommonRequest;
import org.openapitools.client.model.ImportMonitorCommonResponse;
import org.openapitools.client.model.JmxMonitorInfoResponse;
import org.openapitools.client.model.MonitorInfoRequestForUtility;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.JmxConv;
import com.clustercontrol.utility.settings.monitor.xml.JmxMonitor;
import com.clustercontrol.utility.settings.monitor.xml.JmxMonitors;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;

/**
 * Jmx 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された Jmx 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている Jmx 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 5.0.a
 *
 */
public class JmxAction extends AbstractMonitorAction<JmxMonitors> {
	public JmxAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<JmxMonitors> getDataClass() {
		return JmxMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(JmxMonitors object) {
		int res = JmxConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				JmxConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(JmxMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return JmxConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<JmxMonitorInfoResponse> jmxList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJmxList(null);
		List<MonitorInfoResponse> monitorInfoList = new ArrayList<MonitorInfoResponse>();
		
		for(JmxMonitorInfoResponse jmxInfo:jmxList){
			MonitorInfoResponse monitorInfo = new MonitorInfoResponse();
			RestClientBeanUtil.convertBeanSimple(jmxInfo, monitorInfo);
			monitorInfoList.add(monitorInfo);
		}
		
		return monitorInfoList;
	}

	@Override
	protected JmxMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return JmxConv.createJmxMonitors(monitorInfoList);
	}

	@Override
	protected void sort(JmxMonitors object) {
		JmxMonitor[] ms = object.getJmxMonitor();
		Arrays.sort(
			ms,
			new Comparator<JmxMonitor>() {
				@Override
				public int compare(JmxMonitor obj1, JmxMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setJmxMonitor(ms);
	}

}
