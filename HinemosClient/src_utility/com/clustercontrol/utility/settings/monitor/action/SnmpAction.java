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
import com.clustercontrol.utility.settings.monitor.conv.SnmpConv;
import com.clustercontrol.utility.settings.monitor.xml.SnmpMonitor;
import com.clustercontrol.utility.settings.monitor.xml.SnmpMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * SNMP 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された SNMP 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている SNMP 監視情報と重複する場合はスキップされる。
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
	protected List<MonitorInfo> createMonitorInfoList(SnmpMonitors object) throws ConvertorException {
		return SnmpConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList()
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSnmpList();
	}

	@Override
	protected SnmpMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
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