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
import com.clustercontrol.utility.settings.monitor.conv.SnmpTrapConv;
import com.clustercontrol.utility.settings.monitor.xml.SnmpTrapMonitors;
import com.clustercontrol.utility.settings.monitor.xml.TrapMonitor;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * SNMPTRAP 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された SNMPTRAP 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている SNMPTRAP 監視情報と重複する場合はスキップされる。
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
	protected List<MonitorInfo> createMonitorInfoList(SnmpTrapMonitors object) throws ConvertorException {
		return SnmpTrapConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList()
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getTrapList();
	}

	@Override
	protected SnmpTrapMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
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