/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.CollectorItemCodeMstResponseP1;
import org.openapitools.client.model.CollectorItemInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.MonitorNumericTypeEnum;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.PriorityEnum;
import org.openapitools.client.model.PerfCheckInfoResponse;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.util.CollectRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.performance.util.CollectorItemCodeFactory;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.PerfInfo;
import com.clustercontrol.utility.settings.monitor.xml.PerfMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PerfMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * リソース 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 *
 */
public class PerfConv {
	private final static Log logger = LogFactory.getLog(PerfConv.class);

	private final static String SCHEMA_TYPE = "I";
	private final static String SCHEMA_VERSION = "1";
	private final static String SCHEMA_REVISION = "2";

	/**
	 * <BR>
	 *
	 * @return
	 */
	public static SchemaInfo getSchemaVersion(){
		SchemaInfo schema = new SchemaInfo();

		schema.setSchemaType(SCHEMA_TYPE);
		schema.setSchemaVersion(SCHEMA_VERSION);
		schema.setSchemaRevision(SCHEMA_REVISION);

		return schema;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	public static int checkSchemaVersion(SchemaInfo schemaInfo) {
		return BaseConv.checkSchemaVersion(
				SCHEMA_TYPE,
				SCHEMA_VERSION,
				SCHEMA_REVISION,
				schemaInfo.getSchemaType(),
				schemaInfo.getSchemaVersion(),
				schemaInfo.getSchemaRevision()
				);
	}

	/**
	 * Castor で作成した形式の リソース 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ConvertorException
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * @throws ParseException 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(PerfMonitors perfMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		String managerName = UtilityManagerUtil.getCurrentManagerName();
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (PerfMonitor perfMonitor : perfMonitors.getPerfMonitor()) {
			logger.debug("Monitor Id : " + perfMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(perfMonitor.getMonitor());

			for (NumericValue numericValue : perfMonitor.getNumericValue()) {
				if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
						numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
					monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
				}
			}
			if(monitorInfo.getNumericValueInfo().size() != 2){
				throw new ConvertorException(
						perfMonitor.getMonitor().getMonitorId()
						+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
			}
			
			for (NumericChangeAmount changeValue : perfMonitor.getNumericChangeAmount()){
				if(changeValue.getPriority() == PriorityConstant.TYPE_INFO ||
						changeValue.getPriority() == PriorityConstant.TYPE_WARNING){
					monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(changeValue));
				}
			}			
			MonitorNumericValueInfoResponse monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
			monitorNumericValueInfo.setMonitorNumericType(null);
			monitorNumericValueInfo.setPriority(PriorityEnum.CRITICAL);
			monitorNumericValueInfo.setThresholdLowerLimit(0.0);
			monitorNumericValueInfo.setThresholdUpperLimit(0.0);
			monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);

			monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
			monitorNumericValueInfo.setMonitorNumericType(null);
			monitorNumericValueInfo.setPriority(PriorityEnum.UNKNOWN);
			monitorNumericValueInfo.setThresholdLowerLimit(0.0);
			monitorNumericValueInfo.setThresholdUpperLimit(0.0);
			monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);

			// 変化量監視が無効の場合、関連閾値が未入力なら、画面デフォルト値にて補完
			if( monitorInfo.getChangeFlg() ==false && perfMonitor.getNumericChangeAmount().length == 0 ){
				MonitorConv.setMonitorChangeAmountDefault(monitorInfo);
			}
			
			// 変化量についても閾値判定と同様にTYPE_CRITICALとTYPE_UNKNOWNを定義する
			monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
			monitorNumericValueInfo.setMonitorNumericType(MonitorNumericTypeEnum.CHANGE);
			monitorNumericValueInfo.setPriority(PriorityEnum.CRITICAL);
			monitorNumericValueInfo.setThresholdLowerLimit(0.0);
			monitorNumericValueInfo.setThresholdUpperLimit(0.0);
			monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
			
			monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
			monitorNumericValueInfo.setMonitorNumericType(MonitorNumericTypeEnum.CHANGE);
			monitorNumericValueInfo.setPriority(PriorityEnum.UNKNOWN);
			monitorNumericValueInfo.setThresholdLowerLimit(0.0);
			monitorNumericValueInfo.setThresholdUpperLimit(0.0);
			monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);

			monitorInfo.setPerfCheckInfo(createPerfCheckInfo(perfMonitor.getPerfInfo()));
			
			// 収集項目コードに対応した収集値表示名と収集値単位を設定
			setCollectorItemInfo(managerName, monitorInfo);

			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の リソース 監視設定情報へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static PerfMonitors createPerfMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		PerfMonitors perfMonitors = new PerfMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			PerfMonitor perfMonitor = new PerfMonitor();
			perfMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityEnum.INFO ||
						numericValueInfo.getPriority() == PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().name().equals("CHANGE")) {
						perfMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						perfMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}

			perfMonitor.setPerfInfo(createPerfInfo(monitorInfo));
			perfMonitors.addPerfMonitor(perfMonitor);
		}

		perfMonitors.setCommon(MonitorConv.versionDto2Xml());
		perfMonitors.setSchemaInfo(getSchemaVersion());

		return perfMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static PerfInfo createPerfInfo(MonitorInfoResponse monitorInfo) {
		PerfInfo perfInfo = new PerfInfo();
		perfInfo.setMonitorTypeId("");
		perfInfo.setMonitorId(monitorInfo.getMonitorId());
		perfInfo.setBreakdownFlg(monitorInfo.getPerfCheckInfo().getBreakdownFlg());

	    // 収集IDです。 監視設定では使用しません。(monitor_check_perf.xsd から引用)
		//perfInfo.setCollectorId(null);

		perfInfo.setDeviceDisplayName(monitorInfo.getPerfCheckInfo().getDeviceDisplayName());
		perfInfo.setItemCode(monitorInfo.getPerfCheckInfo().getItemCode());

		return perfInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static PerfCheckInfoResponse createPerfCheckInfo(PerfInfo perfInfo) {
		PerfCheckInfoResponse perfCheckInfo = new PerfCheckInfoResponse();
		perfCheckInfo.setBreakdownFlg(perfInfo.getBreakdownFlg());
		perfCheckInfo.setDeviceDisplayName(perfInfo.getDeviceDisplayName());
		perfCheckInfo.setItemCode(perfInfo.getItemCode());

		return perfCheckInfo;
	}
	
	/**
	 * 指定のファシリティで収集可能な項目のリストより収集項目コードに対応した収集値表示名と収集値単位を設定する<BR>
	 * XMLに定義されていた情報は上書きする（該当するものを取得できなかった時はそのまま）<BR>
	 * 
	 * @return
	 */
	private static void setCollectorItemInfo(String managerName, MonitorInfoResponse monitorInfo) {
		if (managerName == null || managerName.equals("")) {
			return;
		}
		// 指定したFacilityIDで収集可能な収集項目一覧を取得する
		List<CollectorItemInfoResponse>  itemInfoList = null;
		try {			
			CollectorItemCodeMstResponseP1 res = CollectRestClientWrapper
			.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getAvailableCollectorItemList(monitorInfo.getFacilityId());
			itemInfoList = res.getAvailableCollectorItemList();
		} catch (Exception e){
			logger.warn("setCollectorItemInfo() getAvailableCollectorItemList, " + e.getMessage(), e);
		}
		if (itemInfoList == null || itemInfoList.size() == 0) {
			logger.warn("setCollectorItemInfo() can not get CollectItemList. monitorId=" + monitorInfo.getMonitorId()
					+ ", facilityId=" + monitorInfo.getFacilityId() + ", managerName=" + managerName);
			return;
		}

		CollectorItemInfoResponse itemInfo = null;
		Iterator<CollectorItemInfoResponse> itr = itemInfoList.iterator();
		while(itr.hasNext()){
			CollectorItemInfoResponse tmpItemInfo = itr.next();
			if (tmpItemInfo.getItemCode().equals(monitorInfo.getPerfCheckInfo().getItemCode())) {
				itemInfo = tmpItemInfo;
				break;
			}
		}
		if (itemInfo != null) {
			// 収集項目コードに対応した収集値表示名と収集値単位を設定
			String itemName = CollectorItemCodeFactory.getItemName(managerName, itemInfo.getItemCode());
			String measure = CollectorItemCodeFactory.getMeasure(managerName, itemInfo.getItemCode());
			monitorInfo.setItemName(itemName);
			monitorInfo.setMeasure(measure);
		} else {
			logger.warn("setCollectorItemInfo() This itemCode is not exist in CollectItemList. itemCode="
					+ monitorInfo.getPerfCheckInfo().getItemCode());
		}
	}
}