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
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.TrapCheckInfoResponse;
import org.openapitools.client.model.TrapCheckInfoResponse.PriorityUnspecifiedEnum;
import org.openapitools.client.model.TrapValueInfoResponse;
import org.openapitools.client.model.TrapValueInfoResponse.PriorityAnyVarBindEnum;
import org.openapitools.client.model.TrapValueInfoResponse.VersionEnum;
import org.openapitools.client.model.VarBindPatternResponse;
import org.openapitools.client.model.VarBindPatternResponse.PriorityEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.SnmpTrapInfo;
import com.clustercontrol.utility.settings.monitor.xml.SnmpTrapMonitors;
import com.clustercontrol.utility.settings.monitor.xml.TrapMonitor;
import com.clustercontrol.utility.settings.monitor.xml.TrapValueInfos;
import com.clustercontrol.utility.settings.monitor.xml.VarBindPatterns;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * SNMPTRAP 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 * 
 */
public class SnmpTrapConv {
	private final static Log logger = LogFactory.getLog(SnmpTrapConv.class);

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	private final static String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.SNMPTRAP.SCHEMATYPE");
	private final static String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.SNMPTRAP.SCHEMAVERSION");
	private final static String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.SNMPTRAP.SCHEMAREVISION");
	
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
	 * @throws ParseException 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(SnmpTrapMonitors snmpTrapMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();
		
		for (TrapMonitor trapMonitor : snmpTrapMonitors.getTrapMonitor()) {
			logger.debug("Monitor Id : " + trapMonitor.getMonitor().getMonitorId());
			
			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(trapMonitor.getMonitor());
			monitorInfo.setTrapCheckInfo(createTrapCheckInfo(trapMonitor.getSnmpTrapInfo(), monitorInfo.getMonitorId(), monitorInfo.getMonitorTypeId()));
			
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
	public static SnmpTrapMonitors createSnmpTrapMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		SnmpTrapMonitors snmpTrapMonitors = new SnmpTrapMonitors();
		
		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			TrapMonitor trapMonitor = new TrapMonitor();
			trapMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			trapMonitor.setSnmpTrapInfo(createSnmpTrapInfo(monitorInfo.getTrapCheckInfo()));

			snmpTrapMonitors.addTrapMonitor(trapMonitor);
		}
		
		snmpTrapMonitors.setCommon(MonitorConv.versionDto2Xml());
		snmpTrapMonitors.setSchemaInfo(getSchemaVersion());
		
		return snmpTrapMonitors;
	}
	
	private static SnmpTrapInfo createSnmpTrapInfo(TrapCheckInfoResponse trapCheckInfo) {
		SnmpTrapInfo snmpTrapInfo = new SnmpTrapInfo();
		snmpTrapInfo.setCharsetConvert(trapCheckInfo.getCharsetConvert());
		snmpTrapInfo.setCharsetName(trapCheckInfo.getCharsetName());
		snmpTrapInfo.setCommunityCheck(trapCheckInfo.getCommunityCheck());
		snmpTrapInfo.setCommunityName(trapCheckInfo.getCommunityName());
		snmpTrapInfo.setNotifyofReceivingUnspecifiedFlg(trapCheckInfo.getNotifyofReceivingUnspecifiedFlg());
		int priorityInt = OpenApiEnumConverter.enumToInteger(trapCheckInfo.getPriorityUnspecified());
		snmpTrapInfo.setPriorityUnspecified(priorityInt);

		List<TrapValueInfos> infos = new ArrayList<>();
		TrapValueInfos info = null;
		for (TrapValueInfoResponse value : trapCheckInfo.getMonitorTrapValueInfoEntities()) {
			info = new TrapValueInfos();
			info.setDescription(value.getDescription());
			info.setFormatVarBinds(value.getFormatVarBinds());
			info.setGenericId(value.getGenericId());
			info.setLogmsg(value.getLogmsg());
			info.setMib(value.getMib());
			if (value.getPriorityAnyVarBind() != null) {
				int priorityAnyVarbindInt = OpenApiEnumConverter.enumToInteger(value.getPriorityAnyVarBind());
				info.setPriorityAnyVarbind(priorityAnyVarbindInt);
			}
			info.setSpecificId(value.getSpecificId());
			info.setProcessingVarbindSpecified(value.getProcVarbindSpecified());
			info.setTrapOid(value.getTrapOid());
			info.setUei(value.getUei());
			info.setValidFlg(value.getValidFlg());
			int versionInt =  OpenApiEnumConverter.enumToInteger(value.getVersion());
			info.setVersion(versionInt);

			List<VarBindPatterns> patterns = new ArrayList<>();
			VarBindPatterns pattern = null;
			int orderNo = 0;
			for(VarBindPatternResponse pat: value.getVarBindPatterns()){
				pattern = new VarBindPatterns();
				pattern.setCaseSensitivityFlg(pat.getCaseSensitivityFlg());
				pattern.setDescription(pat.getDescription());
				pattern.setOrderNo(++orderNo);
				pattern.setPattern(pat.getPattern());
				int varBindpriorityInt = OpenApiEnumConverter.enumToInteger(pat.getPriority());
				pattern.setPriority(varBindpriorityInt);
				pattern.setProcessType(pat.getProcessType());
				pattern.setValidFlg(pat.getValidFlg());
				patterns.add(pattern);
			}
			
			info.setVarBindPatterns(patterns.toArray(new VarBindPatterns[0]));
						
			infos.add(info);
		}
		
		snmpTrapInfo.setTrapValueInfos(infos.toArray(new TrapValueInfos[0]));
		
		return snmpTrapInfo;
	}

	private static TrapCheckInfoResponse createTrapCheckInfo(SnmpTrapInfo snampTrapInfo, String monitorId, String monitorTypeId) throws InvalidSetting, HinemosUnknown {
		TrapCheckInfoResponse trapCheckInfo = new TrapCheckInfoResponse();
		
		trapCheckInfo.setCharsetConvert(snampTrapInfo.getCharsetConvert());
		trapCheckInfo.setCharsetName(snampTrapInfo.getCharsetName());
		trapCheckInfo.setCommunityCheck(snampTrapInfo.getCommunityCheck());
		if (snampTrapInfo.getCommunityCheck()) {
			trapCheckInfo.setCommunityName(snampTrapInfo.getCommunityName());
		}
		trapCheckInfo.setNotifyofReceivingUnspecifiedFlg(snampTrapInfo.getNotifyofReceivingUnspecifiedFlg());
		PriorityUnspecifiedEnum priorityUnspecifiedEnum = OpenApiEnumConverter.integerToEnum(snampTrapInfo.getPriorityUnspecified(), PriorityUnspecifiedEnum.class);
		trapCheckInfo.setPriorityUnspecified(priorityUnspecifiedEnum);

		List<TrapValueInfoResponse> infos = new ArrayList<>();
		TrapValueInfoResponse info = null;
		for (TrapValueInfos value : snampTrapInfo.getTrapValueInfos()) {
			info = new TrapValueInfoResponse();
			info.setDescription(value.getDescription());
			if (value.getProcessingVarbindSpecified()) {
				info.setFormatVarBinds(value.getFormatVarBinds());
			}
			info.setGenericId(value.getGenericId());
			info.setLogmsg(value.getLogmsg());
			info.setMib(value.getMib());
			PriorityAnyVarBindEnum priorityAnyVarBindEnum = OpenApiEnumConverter.integerToEnum(value.getPriorityAnyVarbind(), PriorityAnyVarBindEnum.class);
			info.setPriorityAnyVarBind(priorityAnyVarBindEnum);
			info.setSpecificId(value.getSpecificId());
			info.setProcVarbindSpecified(value.getProcessingVarbindSpecified());
			info.setTrapOid(value.getTrapOid());
			info.setUei(value.getUei());
			info.setValidFlg(value.getValidFlg());
			VersionEnum versionEnum = OpenApiEnumConverter.integerToEnum(value.getVersion(), VersionEnum.class);
			info.setVersion(versionEnum);

			List<VarBindPatternResponse> patterns = new ArrayList<>();
			VarBindPatternResponse pattern = null;
			VarBindPatterns[] pats = value.getVarBindPatterns();
 			sort(pats);
			for(VarBindPatterns pat: pats){
				pattern = new VarBindPatternResponse();
				pattern.setCaseSensitivityFlg(pat.getCaseSensitivityFlg());
				pattern.setDescription(pat.getDescription());
				pattern.setPattern(pat.getPattern());
				PriorityEnum priorityEnum =OpenApiEnumConverter.integerToEnum(pat.getPriority(), PriorityEnum.class);
				pattern.setPriority(priorityEnum);
				pattern.setProcessType(pat.getProcessType());
				pattern.setValidFlg(pat.getValidFlg());
				patterns.add(pattern);
			}
			
			info.getVarBindPatterns().clear();
			info.getVarBindPatterns().addAll(patterns);
						
			infos.add(info);
		}
		
		trapCheckInfo.getMonitorTrapValueInfoEntities().clear();
		trapCheckInfo.getMonitorTrapValueInfoEntities().addAll(infos);
		
		return trapCheckInfo;
	}

	private static void sort(VarBindPatterns[] objects) {
		try {
			Arrays.sort(
				objects,
				new Comparator<VarBindPatterns>() {
					@Override
					public int compare(VarBindPatterns obj1, VarBindPatterns obj2) {
						return obj1.getOrderNo() - obj2.getOrderNo();
					}
				});
		}
		catch (Exception e) {
			
		}
	}
}
