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
import com.clustercontrol.utility.settings.monitor.conv.CustomConv;
import com.clustercontrol.utility.settings.monitor.xml.CustomMonitor;
import com.clustercontrol.utility.settings.monitor.xml.CustomMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * カスタム 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された コマンド 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている コマンド 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 *
 */
public class CustomAction extends AbstractMonitorAction<CustomMonitors> {

	public CustomAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<CustomMonitors> getDataClass() {
		return CustomMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(CustomMonitors customMonitors) {
		int res = CustomConv.checkSchemaVersion(customMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				CustomConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(CustomMonitors customMonitors) throws ConvertorException {
		return CustomConv.createMonitorInfoList(customMonitors);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getCommandList();
	}

	@Override
	protected CustomMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return CustomConv.createCustomMonitors(monitorInfoList);
	}

	@Override
	protected void sort(CustomMonitors object) {
		CustomMonitor[] ms = object.getCustomMonitor();
		Arrays.sort(
			ms,
			new Comparator<CustomMonitor>() {
				@Override
				public int compare(CustomMonitor obj1, CustomMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setCustomMonitor(ms);
	}
}
