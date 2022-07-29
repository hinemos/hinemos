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

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.BinaryfileConv;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileMonitor;
import com.clustercontrol.utility.settings.monitor.xml.BinaryfileMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * バイナリファイル 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された バイナリファイル 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている バイナリファイル 監視情報と重複した場合はダイアログにて上書き/スキップをユーザに選択させる。
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinaryfileAction extends AbstractMonitorAction<BinaryfileMonitors> {
	public BinaryfileAction() throws ConvertorException {
		super();
	}
	@Override
	public Class<BinaryfileMonitors> getDataClass() {
		return BinaryfileMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(BinaryfileMonitors binaryfileMonitors) {
		int res = BinaryfileConv.checkSchemaVersion(binaryfileMonitors.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				BinaryfileConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfoResponse> createMonitorInfoList(BinaryfileMonitors logfileMonitors) throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, ParseException {
		return BinaryfileConv.createMonitorInfoList(logfileMonitors);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		GetMonitorListRequest monitorFilterInfo = new GetMonitorListRequest();
				
		MonitorFilterInfoRequest monitorFilter = new MonitorFilterInfoRequest();
		monitorFilter.setMonitorTypeId(HinemosModuleConstant.MONITOR_BINARYFILE_BIN);
		
		monitorFilterInfo.setMonitorFilterInfo(monitorFilter);
		
		List<MonitorInfoResponse> list;
		try {
			list = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitorListByCondition(monitorFilterInfo);
		} catch (InvalidSetting e) {
			throw new HinemosUnknown(e);
		}
		
		return list;
	}

	@Override
	protected BinaryfileMonitors createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		return BinaryfileConv.createBinaryMonitors(monitorInfoList);
	}

	@Override
	protected void sort(BinaryfileMonitors object) {
		BinaryfileMonitor[] ms = object.getBinaryfileMonitor();
		Arrays.sort(
			ms,
			new Comparator<BinaryfileMonitor>() {
				@Override
				public int compare(BinaryfileMonitor obj1, BinaryfileMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setBinaryfileMonitor(ms);
	}
}
