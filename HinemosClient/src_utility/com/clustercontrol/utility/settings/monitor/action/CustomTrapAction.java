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
import com.clustercontrol.utility.settings.monitor.conv.CustomTrapConv;
import com.clustercontrol.utility.settings.monitor.xml.CustomTrapMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CustomTrapMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * カスタムトラップ監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された カスタムトラップ監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている カスタムトラップ監視情報と重複する場合はスキップされる。
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
	public List<MonitorInfo> createMonitorInfoList(CustomTrapMonitors CustomTrapMonitors) throws ConvertorException {
		return CustomTrapConv.createMonitorInfoList(CustomTrapMonitors);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCustomTrapList();
	}

	@Override
	protected CustomTrapMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
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
