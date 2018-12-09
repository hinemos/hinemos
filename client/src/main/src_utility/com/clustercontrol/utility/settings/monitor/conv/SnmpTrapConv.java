/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.SnmpTrapInfo;
import com.clustercontrol.utility.settings.monitor.xml.SnmpTrapMonitors;
import com.clustercontrol.utility.settings.monitor.xml.TrapMonitor;
import com.clustercontrol.utility.settings.monitor.xml.TrapValueInfos;
import com.clustercontrol.utility.settings.monitor.xml.VarBindPatterns;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.TrapCheckInfo;

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

	private final static String SCHEMA_TYPE = "H";
	private final static String SCHEMA_VERSION = "1";
	private final static String SCHEMA_REVISION = "1";
	
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
	 */
	public static List<MonitorInfo> createMonitorInfoList(SnmpTrapMonitors snmpTrapMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();
		
		for (TrapMonitor trapMonitor : snmpTrapMonitors.getTrapMonitor()) {
			logger.debug("Monitor Id : " + trapMonitor.getMonitor().getMonitorId());
			
			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(trapMonitor.getMonitor());
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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static SnmpTrapMonitors createSnmpTrapMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		SnmpTrapMonitors snmpTrapMonitors = new SnmpTrapMonitors();
		
		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			TrapMonitor trapMonitor = new TrapMonitor();
			trapMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			trapMonitor.setSnmpTrapInfo(createSnmpTrapInfo(monitorInfo.getTrapCheckInfo()));

			snmpTrapMonitors.addTrapMonitor(trapMonitor);
		}
		
		snmpTrapMonitors.setCommon(MonitorConv.versionDto2Xml());
		snmpTrapMonitors.setSchemaInfo(getSchemaVersion());
		
		return snmpTrapMonitors;
	}
	
	private static SnmpTrapInfo createSnmpTrapInfo(TrapCheckInfo trapCheckInfo) {
		SnmpTrapInfo snmpTrapInfo = new SnmpTrapInfo();
		snmpTrapInfo.setCharsetConvert(trapCheckInfo.isCharsetConvert());
		snmpTrapInfo.setCharsetName(trapCheckInfo.getCharsetName());
		snmpTrapInfo.setCommunityCheck(trapCheckInfo.isCommunityCheck());
		snmpTrapInfo.setCommunityName(trapCheckInfo.getCommunityName());
		snmpTrapInfo.setNotifyofReceivingUnspecifiedFlg(trapCheckInfo.isNotifyofReceivingUnspecifiedFlg());
		snmpTrapInfo.setPriorityUnspecified(trapCheckInfo.getPriorityUnspecified());

		List<TrapValueInfos> infos = new ArrayList<>();
		TrapValueInfos info = null;
		for (com.clustercontrol.ws.monitor.TrapValueInfo value : trapCheckInfo.getTrapValueInfos()) {
			info = new TrapValueInfos();
			info.setDescription(value.getDescription());
			info.setFormatVarBinds(value.getFormatVarBinds());
			info.setGenericId(value.getGenericId());
			info.setLogmsg(value.getLogmsg());
			info.setMib(value.getMib());
			info.setPriorityAnyVarbind(Objects.isNull(value.getPriorityAnyVarbind())?0:value.getPriorityAnyVarbind());
			info.setSpecificId(value.getSpecificId());
			info.setProcessingVarbindSpecified(value.isProcessingVarbindSpecified());
			info.setTrapOid(value.getTrapOid());
			info.setUei(value.getUei());
			info.setValidFlg(value.isValidFlg());
			info.setVersion(value.getVersion());

			List<VarBindPatterns> patterns = new ArrayList<>();
			VarBindPatterns pattern = null;
			int orderNo = 0;
			for(com.clustercontrol.ws.monitor.VarBindPattern pat: value.getVarBindPatterns()){
				pattern = new VarBindPatterns();
				pattern.setCaseSensitivityFlg(pat.isCaseSensitivityFlg());
				pattern.setDescription(pat.getDescription());
				pattern.setOrderNo(++orderNo);
				pattern.setPattern(pat.getPattern());
				pattern.setPriority(pat.getPriority());
				pattern.setProcessType(pat.isProcessType());
				pattern.setValidFlg(pat.isValidFlg());
				patterns.add(pattern);
			}
			
			info.setVarBindPatterns(patterns.toArray(new VarBindPatterns[0]));
						
			infos.add(info);
		}
		
		snmpTrapInfo.setTrapValueInfos(infos.toArray(new TrapValueInfos[0]));
		
		return snmpTrapInfo;
	}

	private static TrapCheckInfo createTrapCheckInfo(SnmpTrapInfo snampTrapInfo, String monitorId, String monitorTypeId) {
		TrapCheckInfo trapCheckInfo = new TrapCheckInfo();
		trapCheckInfo.setMonitorId(monitorId);
		trapCheckInfo.setMonitorTypeId("");
		
		trapCheckInfo.setCharsetConvert(snampTrapInfo.getCharsetConvert());
		trapCheckInfo.setCharsetName(snampTrapInfo.getCharsetName());
		trapCheckInfo.setCommunityCheck(snampTrapInfo.getCommunityCheck());
		trapCheckInfo.setCommunityName(snampTrapInfo.getCommunityName());
		trapCheckInfo.setNotifyofReceivingUnspecifiedFlg(snampTrapInfo.getNotifyofReceivingUnspecifiedFlg());
		trapCheckInfo.setPriorityUnspecified(snampTrapInfo.getPriorityUnspecified());

		List<com.clustercontrol.ws.monitor.TrapValueInfo> infos = new ArrayList<>();
		com.clustercontrol.ws.monitor.TrapValueInfo info = null;
		for (TrapValueInfos value : snampTrapInfo.getTrapValueInfos()) {
			info = new com.clustercontrol.ws.monitor.TrapValueInfo();
			info.setDescription(value.getDescription());
			info.setFormatVarBinds(value.getFormatVarBinds());
			info.setGenericId(value.getGenericId());
			info.setLogmsg(value.getLogmsg());
			info.setMib(value.getMib());
			info.setPriorityAnyVarbind(value.getPriorityAnyVarbind());
			info.setSpecificId(value.getSpecificId());
			info.setProcessingVarbindSpecified(value.getProcessingVarbindSpecified());
			info.setTrapOid(value.getTrapOid());
			info.setUei(value.getUei());
			info.setValidFlg(value.getValidFlg());
			info.setVersion(value.getVersion());

			List<com.clustercontrol.ws.monitor.VarBindPattern> patterns = new ArrayList<>();
			com.clustercontrol.ws.monitor.VarBindPattern pattern = null;
			VarBindPatterns[] pats = value.getVarBindPatterns();
 			sort(pats);
			for(VarBindPatterns pat: pats){
				pattern = new com.clustercontrol.ws.monitor.VarBindPattern();
				pattern.setCaseSensitivityFlg(pat.getCaseSensitivityFlg());
				pattern.setDescription(pat.getDescription());
				pattern.setPattern(pat.getPattern());
				pattern.setPriority(pat.getPriority());
				pattern.setProcessType(pat.getProcessType());
				pattern.setValidFlg(pat.getValidFlg());
				patterns.add(pattern);
			}
			
			info.getVarBindPatterns().clear();
			info.getVarBindPatterns().addAll(patterns);
						
			infos.add(info);
		}
		
		trapCheckInfo.getTrapValueInfos().clear();
		trapCheckInfo.getTrapValueInfos().addAll(infos);
		
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
