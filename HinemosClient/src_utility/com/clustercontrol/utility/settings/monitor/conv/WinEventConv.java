/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
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
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

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
	public static List<com.clustercontrol.ws.monitor.MonitorInfo> createMonitorInfoList(WinEventMonitors WinEventMonitors) throws ConvertorException {
		List<com.clustercontrol.ws.monitor.MonitorInfo> monitorInfoList =
				new LinkedList<com.clustercontrol.ws.monitor.MonitorInfo>();

		for (WinEventMonitor WinEventMonitor : WinEventMonitors.getWinEventMonitor()) {
			logger.debug("Monitor Id : " + WinEventMonitor.getMonitor().getMonitorId());

			com.clustercontrol.ws.monitor.MonitorInfo monitorInfo =
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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static WinEventMonitors createWinEventMonitors(List<com.clustercontrol.ws.monitor.MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WinEventMonitors WinEventMonitors = new WinEventMonitors();

		for (com.clustercontrol.ws.monitor.MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			WinEventMonitor winEventMonitor = new WinEventMonitor();
			winEventMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			int orderNo = 0;
			for (com.clustercontrol.ws.monitor.MonitorStringValueInfo monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				winEventMonitor.addStringValue(MonitorConv.createStringValue(monitorStringValueInfo, ++orderNo));
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
	private static WinEventInfo createWinEventInfo(com.clustercontrol.ws.monitor.MonitorInfo monitorInfo) {
		com.clustercontrol.ws.monitor.WinEventCheckInfo WinEventCheckInfo = monitorInfo.getWinEventCheckInfo();

		WinEventInfo WinEventInfo = new WinEventInfo();
		WinEventInfo.setMonitorTypeId("");

		WinEventInfo.setMonitorId(monitorInfo.getMonitorId());
		WinEventInfo.setLevelCritical(WinEventCheckInfo.isLevelCritical());
		WinEventInfo.setLevelError(WinEventCheckInfo.isLevelError());
		WinEventInfo.setLevelWarning(WinEventCheckInfo.isLevelWarning());
		WinEventInfo.setLevelInformational(WinEventCheckInfo.isLevelInformational());
		WinEventInfo.setLevelVerbose(WinEventCheckInfo.isLevelVerbose());

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
	private static com.clustercontrol.ws.monitor.WinEventCheckInfo createWinEventCheckInfo(WinEventInfo winEventInfo) {
		com.clustercontrol.ws.monitor.WinEventCheckInfo winEventCheckInfo =
				new com.clustercontrol.ws.monitor.WinEventCheckInfo();
		winEventCheckInfo.setMonitorTypeId("");

		winEventCheckInfo.setMonitorId(winEventInfo.getMonitorId());
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
