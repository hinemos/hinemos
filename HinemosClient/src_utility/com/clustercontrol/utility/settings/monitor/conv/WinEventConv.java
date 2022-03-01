/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorStringValueInfoResponse;
import org.openapitools.client.model.WinEventCheckInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.Category;
import com.clustercontrol.utility.settings.monitor.xml.EventId;
import com.clustercontrol.utility.settings.monitor.xml.Keyword;
import com.clustercontrol.utility.settings.monitor.xml.Log;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.Source;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.settings.monitor.xml.WinEventInfo;
import com.clustercontrol.utility.settings.monitor.xml.WinEventMonitor;
import com.clustercontrol.utility.settings.monitor.xml.WinEventMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 *Windowsイベント 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 2.2.0
 *
 */
public class WinEventConv {
	private final static org.apache.commons.logging.Log logger = LogFactory.getLog(WinEventConv.class);

	private final static String SCHEMA_TYPE = "H";
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
	 * @throws ParseException 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(WinEventMonitors WinEventMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList =
				new LinkedList<MonitorInfoResponse>();

		for (WinEventMonitor WinEventMonitor : WinEventMonitors.getWinEventMonitor()) {
			logger.debug("Monitor Id : " + WinEventMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo =
					MonitorConv.createMonitorInfo(WinEventMonitor.getMonitor());
			StringValue[] values = WinEventMonitor.getStringValue();
			MonitorConv.sort(values);
			for (StringValue stringValue : values) {
				monitorInfo.getStringValueInfo().add(MonitorConv.createMonitorStringValueInfo(stringValue));
			}

			monitorInfo.setWinEventCheckInfo(createWinEventCheckInfo(WinEventMonitor.getWinEventInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式のWindowsイベント 監視設定情報へ変換する<BR>
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
	public static WinEventMonitors createWinEventMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		WinEventMonitors WinEventMonitors = new WinEventMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			WinEventMonitor winEventMonitor = new WinEventMonitor();
			winEventMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			int orderNo = 0;
			for (MonitorStringValueInfoResponse monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				winEventMonitor.addStringValue(MonitorConv.createStringValue(monitorInfo.getMonitorId(),monitorStringValueInfo, ++orderNo));
			}

			winEventMonitor.setWinEventInfo(createWinEventInfo(monitorInfo));
			WinEventMonitors.addWinEventMonitor(winEventMonitor);
		}

		WinEventMonitors.setCommon(MonitorConv.versionDto2Xml());
		WinEventMonitors.setSchemaInfo(getSchemaVersion());

		return WinEventMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static WinEventInfo createWinEventInfo(MonitorInfoResponse monitorInfo) {
		WinEventCheckInfoResponse WinEventCheckInfo = monitorInfo.getWinEventCheckInfo();

		WinEventInfo WinEventInfo = new WinEventInfo();
		WinEventInfo.setMonitorTypeId("");

		WinEventInfo.setMonitorId(monitorInfo.getMonitorId());
		WinEventInfo.setLevelCritical(WinEventCheckInfo.getLevelCritical());
		WinEventInfo.setLevelError(WinEventCheckInfo.getLevelError());
		WinEventInfo.setLevelWarning(WinEventCheckInfo.getLevelWarning());
		WinEventInfo.setLevelInformational(WinEventCheckInfo.getLevelInformational());
		WinEventInfo.setLevelVerbose(WinEventCheckInfo.getLevelVerbose());

		Collections.sort(
				WinEventCheckInfo.getCategory(),
				new Comparator<Integer>() {
					@Override
					public int compare(Integer value1, Integer value2) {
						return value1.compareTo(value2);
					}
				});
		for(Integer entry : WinEventCheckInfo.getCategory()){
			Category category = new Category();
			category.setCategory(entry);
			WinEventInfo.addCategory(category);
		}

		Collections.sort(
				WinEventCheckInfo.getEventId(),
				new Comparator<Integer>() {
					@Override
					public int compare(Integer value1, Integer value2) {
						return value1.compareTo(value2);
					}
				});
		for(Integer entry : WinEventCheckInfo.getEventId()){
			EventId eventId = new EventId();
			eventId.setEventId(entry);
			WinEventInfo.addEventId(eventId);
		}

		Collections.sort(
				WinEventCheckInfo.getKeywords(),
				new Comparator<Long>() {
					@Override
					public int compare(Long value1, Long value2) {
						return value1.compareTo(value2);
					}
				});
		for(Long entry : WinEventCheckInfo.getKeywords()){
			Keyword keyword = new Keyword();
			keyword.setKeyword(entry);
			WinEventInfo.addKeyword(keyword);
		}

		Collections.sort(
				WinEventCheckInfo.getLogName(),
				new Comparator<String>() {
					@Override
					public int compare(String value1, String value2) {
						return value1.compareTo(value2);
					}
				});
		for(String entry : WinEventCheckInfo.getLogName()){
			Log log = new Log();
			log.setLog(entry);
			WinEventInfo.addLog(log);
		}

		Collections.sort(
				WinEventCheckInfo.getSource(),
				new Comparator<String>() {
					@Override
					public int compare(String value1, String value2) {
						return value1.compareTo(value2);
					}
				});
		for(String entry : WinEventCheckInfo.getSource()){
			Source source = new Source();
			source.setSource(entry);
			WinEventInfo.addSource(source);
		}

		return WinEventInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static WinEventCheckInfoResponse createWinEventCheckInfo(WinEventInfo winEventInfo) {
		WinEventCheckInfoResponse winEventCheckInfo =
				new WinEventCheckInfoResponse();

		winEventCheckInfo.setLevelCritical(winEventInfo.getLevelCritical());
		winEventCheckInfo.setLevelError(winEventInfo.getLevelError());
		winEventCheckInfo.setLevelWarning(winEventInfo.getLevelWarning());
		winEventCheckInfo.setLevelInformational(winEventInfo.getLevelInformational());
		winEventCheckInfo.setLevelVerbose(winEventInfo.getLevelVerbose());

		for(Category category : winEventInfo.getCategory()){
			winEventCheckInfo.getCategory().add(category.getCategory());
		}

		for(EventId eventId : winEventInfo.getEventId()){
			winEventCheckInfo.getEventId().add(eventId.getEventId());
		}

		for(Keyword keyword : winEventInfo.getKeyword()){
			winEventCheckInfo.getKeywords().add(keyword.getKeyword());
		}

		for(Log log : winEventInfo.getLog()){
			winEventCheckInfo.getLogName().add(log.getLog());
		}

		for(Source source : winEventInfo.getSource()){
			winEventCheckInfo.getSource().add(source.getSource());
		}

		return winEventCheckInfo;
	}
}
