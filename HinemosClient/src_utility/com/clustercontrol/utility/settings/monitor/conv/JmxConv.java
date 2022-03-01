/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.JmxCheckInfoResponse;
import org.openapitools.client.model.JmxMasterInfoResponseP1;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse.MonitorTypeEnum;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.PriorityEnum;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.JmxInfo;
import com.clustercontrol.utility.settings.monitor.xml.JmxMonitor;
import com.clustercontrol.utility.settings.monitor.xml.JmxMonitors;
import com.clustercontrol.utility.settings.monitor.xml.NumericChangeAmount;
import com.clustercontrol.utility.settings.monitor.xml.NumericValue;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * Jmx 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 5.0.a
 *
 *
 */
public class JmxConv {
	private final static Log logger = LogFactory.getLog(JmxConv.class);

	static private String SCHEMA_TYPE = "I";
	static private String SCHEMA_VERSION = "1";
	static private String SCHEMA_REVISION = "2";

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
	
	/*スキーマのバージョンチェック*/
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
	 * <BR>
	 *
	 * @return
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static JmxMonitors createJmxMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		JmxMonitors jmxMonitors = new JmxMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			JmxMonitor jmxMonitor = new JmxMonitor();
			jmxMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorNumericValueInfoResponse numericValueInfo : monitorInfo.getNumericValueInfo()) {
				if(numericValueInfo.getPriority() == PriorityEnum.INFO ||
						numericValueInfo.getPriority() == PriorityEnum.WARNING){
					if(numericValueInfo.getMonitorNumericType().name().equals("CHANGE")) {
						jmxMonitor.addNumericChangeAmount(MonitorConv.createNumericChangeAmount(monitorInfo.getMonitorId(),numericValueInfo));
					}
					else{
						jmxMonitor.addNumericValue(MonitorConv.createNumericValue(monitorInfo.getMonitorId(),numericValueInfo));
					}
				}
			}

			jmxMonitor.setJmxInfo(createJmxInfo(monitorInfo));
			jmxMonitors.addJmxMonitor(jmxMonitor);
		}

		jmxMonitors.setCommon(MonitorConv.versionDto2Xml());
		jmxMonitors.setSchemaInfo(getSchemaVersion());

		return jmxMonitors;
	}

	public static List<MonitorInfoResponse> createMonitorInfoList(JmxMonitors JmxMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		String managerName = UtilityManagerUtil.getCurrentManagerName();
		// JMXマスタを取得
		List<JmxMasterInfoResponseP1> jmxMstInfoList = null;
		try {
			jmxMstInfoList = getJmxMonitorItemList(managerName);
		} catch (Exception e) {
			jmxMstInfoList = new ArrayList<>();
			logger.warn("createMonitorInfoList() getJmxMasterInfoList, " + e.getMessage(), e);
		}
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (JmxMonitor jmxMonitor : JmxMonitors.getJmxMonitor()) {
			logger.debug("Monitor Id : " + jmxMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(jmxMonitor.getMonitor());

			if(monitorInfo.getMonitorType() == MonitorTypeEnum.NUMERIC){
				for (NumericValue numericValue : jmxMonitor.getNumericValue()) {
					if(numericValue.getPriority() == PriorityConstant.TYPE_INFO ||
							numericValue.getPriority() == PriorityConstant.TYPE_WARNING){
						monitorInfo.getNumericValueInfo().add(MonitorConv.createMonitorNumericValueInfo(numericValue));
					}
				}
				if(monitorInfo.getNumericValueInfo().size() != 2){
					throw new ConvertorException(
							jmxMonitor.getMonitor().getMonitorId()
							+ " " + Messages.getString("SettingTools.NumericValueInvalid"));
				}

				for (NumericChangeAmount changeValue : jmxMonitor.getNumericChangeAmount()){
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
				if( monitorInfo.getChangeFlg() ==false && jmxMonitor.getNumericChangeAmount().length == 0 ){
					MonitorConv.setMonitorChangeAmountDefault(monitorInfo);
				}
				
				// 変化量についても閾値判定と同様にTYPE_CRITICALとTYPE_UNKNOWNを定義する
				monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE);
				monitorNumericValueInfo.setPriority(PriorityEnum.CRITICAL);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
				
				monitorNumericValueInfo = new MonitorNumericValueInfoResponse();
				monitorNumericValueInfo.setMonitorNumericType(MonitorNumericValueInfoResponse.MonitorNumericTypeEnum.CHANGE);
				monitorNumericValueInfo.setPriority(PriorityEnum.UNKNOWN);
				monitorNumericValueInfo.setThresholdLowerLimit(0.0);
				monitorNumericValueInfo.setThresholdUpperLimit(0.0);
				monitorInfo.getNumericValueInfo().add(monitorNumericValueInfo);
			}

			monitorInfo.setJmxCheckInfo(createJmxCheckInfo(jmxMonitor.getJmxInfo()));

			// 監視項目コードに対応した収集値表示名と収集値単位を設定
			setJmxMasterInfo(managerName, jmxMstInfoList, monitorInfo);

			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static JmxInfo createJmxInfo(MonitorInfoResponse jmxCheckInfo) {
		JmxInfo jmxInfo = new JmxInfo();
		jmxInfo.setMonitorTypeId("");

		jmxInfo.setMonitorId(jmxCheckInfo.getMonitorId());
		jmxInfo.setAuthUser(ifNull2Empty(jmxCheckInfo.getJmxCheckInfo().getAuthUser()));
		jmxInfo.setAuthPassword(ifNull2Empty(jmxCheckInfo.getJmxCheckInfo().getAuthPassword()));
		jmxInfo.setMasterId(ifNull2Empty(jmxCheckInfo.getJmxCheckInfo().getMasterId()));
		jmxInfo.setPort(jmxCheckInfo.getJmxCheckInfo().getPort());
		int convertFlgInt = OpenApiEnumConverter.enumToInteger(jmxCheckInfo.getJmxCheckInfo().getConvertFlg());
		jmxInfo.setConvertFlg(convertFlgInt);
		jmxInfo.setUrlFormatName(jmxCheckInfo.getJmxCheckInfo().getUrlFormatName());
		return jmxInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static JmxCheckInfoResponse createJmxCheckInfo(JmxInfo jmxInfo) throws InvalidSetting, HinemosUnknown {
		JmxCheckInfoResponse jmxCheckInfo = new JmxCheckInfoResponse();

		jmxCheckInfo.setAuthUser(jmxInfo.getAuthUser());
		jmxCheckInfo.setAuthPassword(jmxInfo.getAuthPassword());
		jmxCheckInfo.setMasterId(jmxInfo.getMasterId());
		jmxCheckInfo.setPort(jmxInfo.getPort());
		JmxCheckInfoResponse.ConvertFlgEnum convertFlgEnum = OpenApiEnumConverter.integerToEnum(jmxInfo.getConvertFlg(), JmxCheckInfoResponse.ConvertFlgEnum.class);
		jmxCheckInfo.setConvertFlg(convertFlgEnum);
		jmxCheckInfo.setUrlFormatName(jmxInfo.getUrlFormatName());
		return jmxCheckInfo;
	}

	private static String ifNull2Empty(String str){
		if(str == null){
			return "";
		}
		return str;
	}
	
	private static List<JmxMasterInfoResponseP1> getJmxMonitorItemList(String managerName) throws HinemosUnknown, InvalidRole, InvalidUserPass, RestConnectFailed {
		List<JmxMasterInfoResponseP1> jmxMasterInfoList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJmxMonitorItemList();
		return jmxMasterInfoList;
	}
	
	/**
	 * JMXマスタよりマスタIDに対応した収集値表示名と収集値単位を設定する<BR>
	 * XMLに定義されていた情報は上書きする<BR>
	 * 
	 * @return
	 */
	private static void setJmxMasterInfo(String managerName, List<JmxMasterInfoResponseP1> jmxMstInfoList, MonitorInfoResponse monitorInfo) {
		if (managerName == null || managerName.equals("")) {
			return;
		}
		if (jmxMstInfoList == null || jmxMstInfoList.size() == 0) {
			logger.warn("setJmxMasterInfo() can not get JmxMaster. monitorId=" + monitorInfo.getMonitorId()
					+ ", facilityId=" + monitorInfo.getFacilityId() + ", managerName=" + managerName);
			return;
		}
		
		JmxMasterInfoResponseP1 jmxMstInfo = null;
		Iterator<JmxMasterInfoResponseP1> itr = jmxMstInfoList.iterator();
		while(itr.hasNext()){
			JmxMasterInfoResponseP1 tmpJmxMstInfo = itr.next();
			if (tmpJmxMstInfo.getId().equals(monitorInfo.getJmxCheckInfo().getMasterId())) {
				jmxMstInfo = tmpJmxMstInfo;
				break;
			}
		}
		if (jmxMstInfo != null) {
			monitorInfo.setItemName(jmxMstInfo.getName());
			monitorInfo.setMeasure(jmxMstInfo.getMeasure());
		} else {
			logger.warn("setJmxMasterInfo() This masterId is not exist in JmxMaster. masterId="
					+ monitorInfo.getJmxCheckInfo().getMasterId());
		}
	}
}