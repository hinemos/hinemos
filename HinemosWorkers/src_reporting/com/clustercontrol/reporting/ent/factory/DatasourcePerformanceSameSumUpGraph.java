/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.OutputMonitorInfo;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.ent.bean.DataKey;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo.OutputNodeInfo;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo.OutputScopeInfo;
import com.clustercontrol.reporting.ent.bean.ResourceChart;
import com.clustercontrol.reporting.ent.session.ReportingPerformanceControllerBean;
import com.clustercontrol.reporting.ent.util.PropertiesConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.util.ReportingQueryUtil;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.util.HinemosMessage;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * 指定されたリソースに関して
 * 先に情報を取得しその後、レポート作成のためのデータソース作成を行うクラス
 * 
 * @version 5.0.a
 * @since 5.0.a
 */
public class DatasourcePerformanceSameSumUpGraph extends DatasourceSamePattern {
	private static Log m_log = LogFactory.getLog(DatasourcePerformanceSameSumUpGraph.class);

	private Map<String, OutputMonitorInfo> m_outputMonitorInfoMap = new HashMap<>();
	private Map<String,Map<String,List<String[]>>> m_databaseMap = new HashMap<>();

	private String m_title;
	private String m_suffix;
	private String m_label;
	private boolean m_adjustMax = false;
	private boolean m_adjustMin = false;
	private Double maxval = 0.0;
	private Double minval = 0.0;

	private Integer count = 0;

	/**
	 * csvのカラム情報
	 * collectorid : 監視設定ID
	 * display_name : デバイス名
	 * date_time : 時間
	 * legend_name : グラフに出力した際の凡例での表示名
	 * facilityid : ファシリティID
	 * value : 収集値
	 * 
	 */
	private static String[] columns() {
		return new String[]{  "collectorid", "item_code", "display_name", "date_time", "legend_name", "facilityid", "value" };
	}

	/**
	 * 監視結果（収集）情報に関するCSVファイルを作成する
	 * 
	 * @param csvFileName
	 * @param facilityId
	 * @param columns
	 * @return
	 */
	protected void getCsvFromDB(String csvFileName, List<DataKey> datakeys) {
		m_log.info("create csv file. file name : " + csvFileName);
		File csv = new File(csvFileName);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(csv, false))) {
			bw.write(ReportUtil.joinStrings(columns(), ","));
			bw.newLine();

			Map<String,Integer> legendNames = new HashMap<>();
			for (DataKey dataKey : datakeys) {
				List<String[]> dataLines = m_databaseMap.get(dataKey.getFacilityId()).get(dataKey.getDisplayName());
				if (dataLines != null && !dataLines.isEmpty()) {
					String addNumbering = "";
					if (legendNames.containsKey(dataLines.get(0)[4])) {
						int i = legendNames.get(dataLines.get(0)[4]);
						addNumbering = " #" +String.valueOf(++i);
					} else {
						legendNames.put(dataLines.get(0)[4], 0);
					}
					
					for (String[] line : dataLines) {
						if (!addNumbering.isEmpty()) {
							String[] modifiedLine = new String[columns().length];
							modifiedLine[0] = line[0];
							modifiedLine[1] = line[1];
							modifiedLine[2] = line[2];
							modifiedLine[3] = line[3];
							StringBuffer legendName = new StringBuffer();
							if (!line[2].isEmpty()) {
								legendName.append(line[4].substring(0, line[4].length()-(line[2].length()+3)));
								legendName.append(addNumbering);
								legendName.append(" - "+line[2]);
							} else {
								legendName.append(line[4]);
								legendName.append(addNumbering);
							}
							modifiedLine[4] = legendName.toString();
							modifiedLine[5] = line[5];
							modifiedLine[6] = line[6];
							bw.write(ReportUtil.joinStrings(modifiedLine, ","));
						} else {
							bw.write(ReportUtil.joinStrings(line, ","));
						}
						bw.newLine();
					}
				}
			}
		} catch (IOException e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		}
	}

	private List<String> createNodeIdList(OutputFacilityInfo rootFacility) {
		List<String> facilityIdList = new ArrayList<>();
		if (rootFacility instanceof OutputScopeInfo) {
			OutputScopeInfo rootScope = (OutputScopeInfo) rootFacility;
			for (OutputScopeInfo scope : rootScope.getScopes()) {
				for (OutputNodeInfo node: scope.getNodes()) {
					facilityIdList.add(node.getFacilityId());
				}
			}
			for (OutputNodeInfo node: rootScope.getNodes()) {
				facilityIdList.add(node.getFacilityId());
			}
		} else if (rootFacility instanceof OutputNodeInfo) {
			facilityIdList.add(rootFacility.getFacilityId());
		}
		return facilityIdList;
	}

	/**
	 * 収集データの中から、レポート出力に使用する監視項目情報を格納する。
	 * 
	 * @param facilityIds
	 * @param itemFilter
	 * @return monitorIDと監視情報のマップ
	 */
	private TreeMap<String, OutputMonitorInfo> getOutputMonitorInfo(List<String> facilityIds, List<String> itemFilter) {
		TreeMap<String, OutputMonitorInfo> map = new TreeMap<>();
		try {
			ReportingPerformanceControllerBean performanceController = new ReportingPerformanceControllerBean();
			List<MonitorInfo> monitorInfoList = performanceController.getMonitorInfoByItemCode(itemFilter);
			if (monitorInfoList != null) {
				OutputMonitorInfo info = null;
				OutputMonitorInfo checkInfo = null;
				String monitorId = null;
				for (MonitorInfo entity : monitorInfoList) {
					List<CollectKeyInfo> keyInfoList = performanceController
							.getCollectKeyInfoListByMonitorIdAndFacilityidList(entity.getMonitorId(), facilityIds);
					List<Integer> idList = new LinkedList<>();
					for (CollectKeyInfo keyInfo : keyInfoList) {
						idList.add(keyInfo.getCollectorid());
					}
					boolean hasSummaryData = new ReportingQueryUtil().hasSummaryData(idList, m_startDate.getTime(), m_endDate.getTime());
					// データが存在しない場合は、次のデータを処理する。
					if (hasSummaryData) {
						for (CollectKeyInfo existKeyInfo : keyInfoList) {
							monitorId = existKeyInfo.getMonitorId();
							checkInfo = map.get(monitorId);
							if(m_log.isDebugEnabled()){
								m_log.debug("from db data " + existKeyInfo.getMonitorId() + ", " + existKeyInfo.getItemName() + ", " + entity.getRunInterval()
										+ ", " + existKeyInfo.getDisplayName() + "," + entity.getMeasure());
							}

							// 既存データが存在する場合は、displayNameが複数存在するケースのみであるため、
							// displayNameを追加し、デバイスフラグをtrueに変更
							if (checkInfo != null) {
								checkInfo.getDisplayName().add(existKeyInfo.getDisplayName());
								checkInfo.setDeviceFlg(true);

								map.put(monitorId, checkInfo);
								if(m_log.isDebugEnabled()){
									m_log.debug("update put " + checkInfo.getMonitorId() + ", " + checkInfo.getItemName() + ", " + checkInfo.getMeasure() 
											+ ", " + existKeyInfo.getDisplayName() + "," + checkInfo.getRunInterval() + "," + checkInfo.isDeviceFlg());
								}
							}
							// 存在しない場合は、新規作成
							else {
								info = new OutputMonitorInfo();
								info.setMonitorId(monitorId);
								String itemName = existKeyInfo.getItemName();
								info.setItemName(itemName);
								if (m_title == null || m_title.isEmpty()) {
									m_title = itemName;
								}
								info.setMeasure(HinemosMessage.replace(entity.getMeasure()));
								info.getDisplayName().add(existKeyInfo.getDisplayName());
								info.setRunInterval(entity.getRunInterval());
								info.setDeviceFlg(false);
								info.setMaxValue(0.0);

								map.put(monitorId, info);
								if(m_log.isDebugEnabled()){
									m_log.debug("new    put " + info.getMonitorId() + ", " + info.getItemName() + ", " + info.getMeasure() + ", " + info.getDisplayName() + "," + info.getRunInterval());
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}

		return map;
	}
	
	private Map<String, List<String[]>> getItemDataMap(ReportingPerformanceControllerBean controller, String facilityId,
			String MonitorId, List<String> itemCodeList, int divider, Boolean trimFlg, String legendTypeName,
			String facilityName, Integer first, Integer last) {
		Map<String, List<String[]>> itemDataMap = new TreeMap<>();
		int summaryType = ReportUtil.getSummaryType(m_startDate.getTime(), m_endDate.getTime());
		// サマリデータ、または収集データ(raw)のタイプでスイッチ
		switch (summaryType) {
		case SummaryTypeConstant.TYPE_AVG_HOUR:
			List<Object[]> summaryHList = null;
			try {
				summaryHList = controller.getSummaryPrefAvgHour(facilityId, m_startDate.getTime(),
						m_endDate.getTime(), MonitorId, itemCodeList);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getItemDataMap() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryHList == null || summaryHList.isEmpty())
				break;
			for (Object[] summaryHour : summaryHList) {
				String itemCode = summaryHour[1].toString();
				if (!itemDataMap.containsKey(itemCode)) {
					itemDataMap.put(itemCode, new ArrayList<String[]>());
				}
				String[] result = new String[columns().length];
				result[0] = summaryHour[0].toString();
				result[1] = itemCode;
				result[2] = ""; // display_name
				result[3] = new Timestamp((Long) summaryHour[3]).toString();

				// 凡例表記にファシリティ名が設定されている場合
				StringBuffer legendValue = null;
				if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendTypeName)) {
					legendValue = new StringBuffer(facilityName);
				} else {
					legendValue = new StringBuffer(summaryHour[4].toString());
				}

				if (m_log.isDebugEnabled()) {
					m_log.debug("legendValue : " + legendValue);
				}

				StringBuffer legendName = new StringBuffer(legendValue);
				if (trimFlg && legendName.length() - 1 > first + last) {
					legendName = new StringBuffer();
					legendName.append(legendValue.substring(0, first));
					legendName.append(isDefine(PropertiesConstant.TRIM_STR_KEY, " ... "));
					legendName.append(legendValue.substring(legendValue.length() - last, legendValue.length()));
				}
				if (!result[2].isEmpty()) {
					legendName.append(" - " + result[2]);
				}

				if (m_log.isDebugEnabled()) {
					m_log.debug("legendName : " + legendName.toString());
				}

				result[4] = legendName.toString();
				result[5] = summaryHour[4].toString();
				Double value = Double.NaN;
				if (summaryHour[5] != null) {
					value = Double.valueOf(summaryHour[5].toString());
				}
				if (!value.isNaN()) {
					if (divider > 1) {
						result[6] = Double.toString(value / divider);
					} else {
						result[6] = value.toString();
					}
					if (this.maxval < Double.valueOf(result[6])) {
						this.maxval = Double.valueOf(result[6]) + 1;
					} else if (this.minval > Double.valueOf(result[6])) {
						this.minval = Double.valueOf(result[6]);
					}
					itemDataMap.get(itemCode).add(result);
				}
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_DAY:
			List<Object[]> summaryDList = null;
			try {
				summaryDList = controller.getSummaryPrefAvgDay(facilityId, m_startDate.getTime(),
						m_endDate.getTime(), MonitorId, itemCodeList);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getItemDataMap() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryDList == null || summaryDList.isEmpty())
				break;
			for (Object[] summaryDay : summaryDList) {
				String itemCode = summaryDay[1].toString();
				if (!itemDataMap.containsKey(itemCode)) {
					itemDataMap.put(itemCode, new ArrayList<String[]>());
				}
				String[] result = new String[columns().length];
				result[0] = summaryDay[0].toString();
				result[1] = itemCode;
				result[2] = ""; // display_name
				result[3] = new Timestamp((Long) summaryDay[3]).toString();

				// 凡例表記にファシリティ名が設定されている場合
				StringBuffer legendValue = null;
				if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendTypeName)) {
					legendValue = new StringBuffer(facilityName);
				} else {
					legendValue = new StringBuffer(summaryDay[4].toString());
				}

				if (m_log.isDebugEnabled()) {
					m_log.debug("legendValue : " + legendValue);
				}

				StringBuffer legendName = new StringBuffer(legendValue);
				if (trimFlg && legendName.length() - 1 > first + last) {
					legendName = new StringBuffer();
					legendName.append(legendValue.substring(0, first));
					legendName.append(isDefine(PropertiesConstant.TRIM_STR_KEY, " ... "));
					legendName.append(legendValue.substring(legendValue.length() - last, legendValue.length()));
				}
				if (!result[2].isEmpty()) {
					legendName.append(" - " + result[2]);
				}

				if (m_log.isDebugEnabled()) {
					m_log.debug("legendName : " + legendName.toString());
				}

				result[4] = legendName.toString();
				result[5] = summaryDay[4].toString();
				Double value = Double.NaN;
				if (summaryDay[5] != null) {
					value = Double.valueOf(summaryDay[5].toString());
				}
				if (!value.isNaN()) {
					if (divider > 1) {
						result[6] = Double.toString(value / divider);
					} else {
						result[6] = value.toString();
					}
					if (this.maxval < Double.valueOf(result[6])) {
						this.maxval = Double.valueOf(result[6]) + 1;
					} else if (this.minval > Double.valueOf(result[6])) {
						this.minval = Double.valueOf(result[6]);
					}
					itemDataMap.get(itemCode).add(result);
				}
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_MONTH:
			List<Object[]> summaryMList = null;
			try {
				summaryMList = controller.getSummaryPrefAvgMonth(facilityId, m_startDate.getTime(),
						m_endDate.getTime(), MonitorId, itemCodeList);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getItemDataMap() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryMList == null || summaryMList.isEmpty())
				break;
			for (Object[] summaryMonth : summaryMList) {
				String itemCode = summaryMonth[1].toString();
				if (!itemDataMap.containsKey(itemCode)) {
					itemDataMap.put(itemCode, new ArrayList<String[]>());
				}
				String[] result = new String[columns().length];
				result[0] = summaryMonth[0].toString();
				result[1] = itemCode;
				result[2] = ""; // display_name
				result[3] = new Timestamp((Long) summaryMonth[3]).toString();

				// 凡例表記にファシリティ名が設定されている場合
				StringBuffer legendValue = null;
				if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendTypeName)) {
					legendValue = new StringBuffer(facilityName);
				} else {
					legendValue = new StringBuffer(summaryMonth[4].toString());
				}

				if (m_log.isDebugEnabled()) {
					m_log.debug("legendValue : " + legendValue);
				}

				StringBuffer legendName = new StringBuffer(legendValue);
				if (trimFlg && legendName.length() - 1 > first + last) {
					legendName = new StringBuffer();
					legendName.append(legendValue.substring(0, first));
					legendName.append(isDefine(PropertiesConstant.TRIM_STR_KEY, " ... "));
					legendName.append(legendValue.substring(legendValue.length() - last, legendValue.length()));
				}
				if (!result[2].isEmpty()) {
					legendName.append(" - " + result[2]);
				}

				if (m_log.isDebugEnabled()) {
					m_log.debug("legendName : " + legendName.toString());
				}

				result[4] = legendName.toString();
				result[5] = summaryMonth[4].toString();
				Double value = Double.NaN;
				if (summaryMonth[5] != null) {
					value = Double.valueOf(summaryMonth[5].toString());
				}
				if (!value.isNaN()) {
					if (divider > 1) {
						result[6] = Double.toString(value / divider);
					} else {
						result[6] = value.toString();
					}
					if (this.maxval < Double.valueOf(result[6])) {
						this.maxval = Double.valueOf(result[6]) + 1;
					} else if (this.minval > Double.valueOf(result[6])) {
						this.minval = Double.valueOf(result[6]);
					}
					itemDataMap.get(itemCode).add(result);
				}
			}
			break;
		default: // defaultはRAWとする
			List<Object[]> summaryList = null;
			try {
				summaryList = controller.getSummaryPrefAvgData(facilityId, m_startDate.getTime(),
						m_endDate.getTime(), MonitorId, itemCodeList);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getItemDataMap() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryList == null || summaryList.isEmpty())
				break;
			for (Object[] data : summaryList) {
				String itemCode = data[1].toString();
				if (!itemDataMap.containsKey(itemCode)) {
					itemDataMap.put(itemCode, new ArrayList<String[]>());
				}
				String[] result = new String[columns().length];
				result[0] = data[0].toString();
				result[1] = itemCode;
				result[2] = ""; // display_name
				result[3] = new Timestamp((Long) data[3]).toString();

				// 凡例表記にファシリティ名が設定されている場合
				StringBuffer legendValue = null;
				if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendTypeName)) {
					legendValue = new StringBuffer(facilityName);
				} else {
					legendValue = new StringBuffer(data[4].toString());
				}

				if (m_log.isDebugEnabled()) {
					m_log.debug("legendValue : " + legendValue);
				}

				StringBuffer legendName = new StringBuffer(legendValue);
				if (trimFlg && legendName.length() - 1 > first + last) {
					legendName = new StringBuffer();
					legendName.append(legendValue.substring(0, first));
					legendName.append(isDefine(PropertiesConstant.TRIM_STR_KEY, " ... "));
					legendName.append(legendValue.substring(legendValue.length() - last, legendValue.length()));
				}
				if (!result[2].isEmpty()) {
					legendName.append(" - " + result[2]);
				}

				if (m_log.isDebugEnabled()) {
					m_log.debug("legendName : " + legendName.toString());
				}

				result[4] = legendName.toString();
				result[5] = data[4].toString();
				Double value = Double.NaN;
				if (data[5] != null) {
					value = Double.valueOf(data[5].toString());
				}
				if (!value.isNaN()) {
					if (divider > 1) {
						result[6] = Double.toString(value / divider);
					} else {
						result[6] = value.toString();
					}
					if (this.maxval < Double.valueOf(result[6])) {
						this.maxval = Double.valueOf(result[6]) + 1;
					} else if (this.minval > Double.valueOf(result[6])) {
						this.minval = Double.valueOf(result[6]);
					}
					itemDataMap.get(itemCode).add(result);
				}
			}
			break;
		}
		return itemDataMap;
	}

	public void collectDataSource(OutputFacilityInfo rootFacility, int chartType) throws ReportingPropertyNotFound {
		List<String> facilityIds = createNodeIdList(rootFacility);
		// プロパティからグラフ生成にかかわる情報を取得
		String itemStr = m_propertiesMap.get(PropertiesConstant.ITEM_CODE_KEY+"."+chartType);
		if (itemStr == null || itemStr.isEmpty()) {
			throw new ReportingPropertyNotFound(PropertiesConstant.ITEM_CODE_KEY+"."+chartType+"  is not defined.");
		}
		m_log.info("collect item " + PropertiesConstant.ITEM_CODE_KEY+" : " + itemStr);

		int divider = Integer.parseInt(isDefine(PropertiesConstant.DIVIDER_KEY+"."+chartType, "1"));
		m_title = m_propertiesMap.get(PropertiesConstant.CHART_TITLE_KEY+"."+chartType);
		m_suffix = isDefine(SUFFIX_KEY_VALUE+"."+chartType, itemStr.split(",")[0].toLowerCase());
		m_label = m_propertiesMap.get(PropertiesConstant.LABEL_KEY+"."+chartType);

		m_adjustMax = Boolean.valueOf(m_propertiesMap.get(PropertiesConstant.ADJUST_MAX_VALUE_KEY+"."+chartType));
		m_adjustMin = Boolean.valueOf(m_propertiesMap.get(PropertiesConstant.ADJUST_MIN_VALUE_KEY+"."+chartType));

		Boolean trimFlg = false;
		Integer first = 0;
		if ((m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_PREFIX_KEY+"."+chartType) != null) && !m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_PREFIX_KEY+"."+chartType).isEmpty()) {
			try {
				first = Integer.valueOf(m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_PREFIX_KEY+"."+chartType));
				if (first > 0)
					trimFlg = true;
			} catch (NumberFormatException e) {
				m_log.info("check your property. key: " + PropertiesConstant.LEGEND_TRIM_PREFIX_KEY+"."+chartType +" value : "+ m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_PREFIX_KEY+"."+chartType));
			}
		}
		Integer last = 0;
		if ((m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_SUFFIX_KEY+"."+chartType) != null) && !m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_SUFFIX_KEY+"."+chartType).isEmpty()) {
			try {
				last = Integer.valueOf(m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_SUFFIX_KEY+"."+chartType));
				if (last > 0)
					trimFlg = true;
				else
					trimFlg = false;
			} catch (NumberFormatException e) {
				m_log.info("check your property. key: " + PropertiesConstant.LEGEND_TRIM_SUFFIX_KEY+"."+chartType +" value : "+ m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_SUFFIX_KEY+"."+chartType));
				trimFlg = false;
			}
		}
		
		String legendTypeName = isDefine(PropertiesConstant.LEGEND_TYPE_KEY, PropertiesConstant.TYPE_FACILITY_ID);
		m_log.info("legend.type.name : " + legendTypeName);
		
		//指定ファシリティからと指定アイテムコードから該当する監視設定を取得
		m_outputMonitorInfoMap = getOutputMonitorInfo(facilityIds, Arrays.asList(itemStr.split(",")));
		String outputMode = isDefine(PropertiesConstant.OUTPUT_MODE_KEY, PropertiesConstant.OUTPUT_MODE_DEFAULT);
		Map<String, OutputMonitorInfo> monitorMap = new HashMap<>();
		if(!outputMode.equals(PropertiesConstant.MODE_AUTO)) {
			if(m_propertiesMap.get(PropertiesConstant.GRAPH_OUTPUT_ID_KEY+"."+chartType).isEmpty()) {
				throw new ReportingPropertyNotFound(PropertiesConstant.GRAPH_OUTPUT_ID_KEY+"."+chartType + " is not defined.");
			}
			String monitorIds = m_propertiesMap.get(PropertiesConstant.GRAPH_OUTPUT_ID_KEY+"."+chartType);
			if (m_outputMonitorInfoMap.get(monitorIds) != null) {
				for (String monitorId :monitorIds.split(",")) {
					monitorMap.put(monitorId, m_outputMonitorInfoMap.get(monitorId));
				}
			}
		} else {
			monitorMap = m_outputMonitorInfoMap;
		}

		m_databaseMap = new HashMap<>();
		String facilityName = null;
		for(Entry<String, OutputMonitorInfo> info : monitorMap.entrySet()) {

			//DBからIDごとに情報を取得していく
			for (String id : facilityIds) {
				// 出力値をCSVファイルに書き込む
				try {
					ReportingPerformanceControllerBean performanceController = new ReportingPerformanceControllerBean();
					
					// 凡例表記にファシリティ名が設定されている場合
					if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendTypeName)) {
						FacilityInfo facilityInfo = performanceController.getFacilityInfo(id);
						
						if (facilityInfo != null) {
							facilityName = facilityInfo.getFacilityName();
						}	
					}
					
					List<String> itemCodeList = Arrays.asList(itemStr.split(","));
					Map<String, List<String[]>> itemDataMap = getItemDataMap(performanceController, id, info.getValue().getMonitorId(), itemCodeList, divider, trimFlg, legendTypeName, facilityName, first, last);
					
					if (!itemDataMap.isEmpty()) {
						//DBから取得した情報を整理する
						// TODO display nameの取得方法について、調べる。5.0も取れていない。
						List<String[]> colData = new ArrayList<>();
						List<String[]> currentData = itemDataMap.get(itemStr.split(",")[0]);
						if (itemStr.split(",").length > 1) {
							for (String[] line : currentData) {
								String[] newLine = new String[columns().length];
								newLine[0] = line[0];
								newLine[1] = line[1];
								String displayName = line[2];
								newLine[2] = displayName;
								newLine[3] = line[3];
								newLine[4] = line[4];
								newLine[5] = line[5];
								newLine[6] = line[6];
								colData.add(newLine);
							}
						} else {
							colData = currentData;
						}
	
						if (!colData.isEmpty()) {
							Map<String, List<String[]>> dataMap = new TreeMap<>();
							String currentDisplayName = colData.get(0)[2];
							List<String[]> modifiedData = new ArrayList<>();
							for (String[] result : colData) {
								String displayName = result[2];
								if (!currentDisplayName.equals(displayName)) {
									dataMap.put(currentDisplayName, modifiedData);
									currentDisplayName = displayName;
									modifiedData = new ArrayList<>();
								}
								modifiedData.add(result);
							}
							if (!modifiedData.isEmpty())
								dataMap.put(currentDisplayName, modifiedData);
	
							m_databaseMap.put(id, dataMap);
						}
					}
				} catch (FacilityNotFound e) {
					m_log.error(e,e);
				}
			}
		}
	}

	@Override
	public List<DataKey> getKeys(String facilityId) {
		List<DataKey> keys = new ArrayList<>();
		Map<String, List<String[]>> dataMap = m_databaseMap.get(facilityId);
		if (dataMap == null)
			return new ArrayList<DataKey>();

		for (String displaname : m_databaseMap.get(facilityId).keySet()) {
			DataKey key = new DataKey(facilityId, displaname);
			keys.add(key);
		}
		return keys;
	}

	@Override
	public Map<String, Object> createDataSource(ResourceChart chart, int chartNum) throws ReportingPropertyNotFound {
		count++;
		m_retMap = new HashMap<String, Object>();
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);
		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, m_suffix + count + "_" + chartNum + "_" + dayString);

		getCsvFromDB(csvFileName, chart.getItems());

		try {
			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);
			m_retMap.put(ReportingConstant.STR_DS+"_"+chartNum, ds);
		} catch (JRException e) {
			m_log.error(e, e);
		}

		String chartTitle = m_title;
		String subTitle = chart.getSubTitle();
		if (null != subTitle) {
			chartTitle+= " - " + subTitle;
		}

		m_retMap.put(PropertiesConstant.CHART_TITLE_KEY+"."+chartNum, chartTitle);
		m_retMap.put(PropertiesConstant.LABEL_KEY+"."+chartNum, m_label);

		if (m_label.equals("%")) {
			m_retMap.put(PropertiesConstant.FIXVAL_KEY+"."+chartNum, 100.0);
		}
		if (m_label.equals("%")) {
			m_retMap.put(PropertiesConstant.MINVAL_KEY+"."+chartNum, 0.0);
		}
		if (m_adjustMax) {
			// Set max except min == max (properly == 0)
			if (! this.minval.equals(this.maxval)) {
				m_retMap.put(PropertiesConstant.FIXVAL_KEY+"."+chartNum, this.maxval);
			}
		}
		if (m_adjustMin) {
			m_retMap.put(PropertiesConstant.MINVAL_KEY+"."+chartNum, this.minval);
		}

		return m_retMap;
	}
}
