/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.OutputMonitorInfo;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingCollectControllerBean;
import com.clustercontrol.reporting.session.ReportingMonitorControllerBean;
import com.clustercontrol.util.HinemosMessage;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * 監視情報のレポート作成を行うクラス
 */
public class DatasourceMonitorLineGraph extends DatasourceBase {

	private static Log m_log = LogFactory.getLog(DatasourceMonitorLineGraph.class);
	
	private static final String OUTPUT_MODE_KEY_VALUE = "output.mode";
	private static final String DIVIDER_KEY_VALUE = "divider";
	private static final String GRAPH_OUTPUT_ID_KEY_VALUE = "graph.output.id";
	private static final String MONITOR_ID_KEY_VALUE = "graph.monitor.id";
	private static final String CHART_TITLE_KEY_VALUE = "chart.title";
	private static final String LABEL_KEY_VALUE = "label";
	private static final String FIXVAL_KEY_VALUE = "fixval";
	
	private static final String MODE_AUTO = "auto";
	
	private HashMap<String, Object> m_retMap = new HashMap<String, Object>();
	
	/**
	 * データソース（CSVファイル）をHinemos DBから生成する
	 */
	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {
		
		String outputMode = isDefine(OUTPUT_MODE_KEY_VALUE, "auto");
		
		// 条件に合致する監視項目の情報をすべて出力する
		if(outputMode.equals(MODE_AUTO)) {
			return createDataSourceAuto(num);
		}
		// 
		else {
			return createDataSourceManual(num);
		}
	}
	
	/*
	 * オートモード
	 * ver.4.1のレポーティングオプションと同様の動きをする
	 */
	private HashMap<String, Object> createDataSourceAuto(int num) throws ReportingPropertyNotFound {
		// プロパティからグラフ生成にかかわる情報を取得
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);
		String csvFileName = "";
		
		String[] columns = { "monitor_id", "item_name", "display_name", "date_time", "facilityid", "value" };
		
		// グラフ表示対象となるファシリティIDと監視項目IDのセットを作成
		String filterStr = isDefine(GRAPH_OUTPUT_ID_KEY_VALUE, "%%");
		m_log.info("reporting.monitoring.graph.output.id : " + filterStr);
//		boolean collectorFlg = isDefine(GRAPH_COLLECT_KEY_VALUE, true); // TODO: collectorFlgの取得方法
		boolean collectorFlg = true;
		m_log.info("reporting.monitoring.graph.collect : " + collectorFlg);
		
		TreeMap<String, OutputMonitorInfo> outputMonitorInfoMap = getOutputMonitorInfo(m_facilityId, filterStr, collectorFlg);
		
		// 対象の項目に対する収集値が空の場合
		if(outputMonitorInfoMap.isEmpty()) {
			m_retMap.put(ReportingConstant.STR_DS + "_" + num, new JREmptyDataSource());
			
			return m_retMap;
		}
		
		for(Map.Entry<String, OutputMonitorInfo> entry : outputMonitorInfoMap.entrySet()) {
			
			m_log.info("createDataSourceAuto() : facility_id : " + m_facilityId + ", monitor_id : " + entry.getKey());
			
			csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, m_facilityId + "_" + entry.getKey() + "_" + dayString);
			
			try {
				// 「デバイス別」のグラフに関するデータ
				if(entry.getValue().getDisplayName() != null && entry.getValue().getDisplayName().size() > 1) {
					getCsvFromDBDevice(csvFileName, m_facilityId, entry.getKey(), columns, 1, entry.getValue());
				} 
				// 「デバイス別」ではないグラフに関するデータ
				else {
					getCsvFromDB(csvFileName, m_facilityId, entry.getKey(), columns, 1, entry.getValue());
				}
				JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
				ds.setUseFirstRowAsHeader(true);
				
				m_retMap.put(ReportingConstant.STR_DS + "_" + num, ds);
				m_retMap.put(CHART_TITLE_KEY_VALUE + "."+ num,
						HinemosMessage.replace(entry.getValue().getItemName()) + " (" + entry.getValue().getMonitorId() + ")");
				m_retMap.put(LABEL_KEY_VALUE + "." + num, entry.getValue().getMeasure());
				
				// グラフの上限値を設定 単位が「%」の場合は、100を上限とするグラフにする
				double fixval = 0;
				if (entry.getValue().getMeasure().equals("%")) {
					fixval = 100;
				}
				else {
					fixval = entry.getValue().getMaxValue();
				}
				m_retMap.put(FIXVAL_KEY_VALUE + "." + num, fixval);
				
			} catch (Exception e) {
				m_log.error(e, e);
			}
			
			// グラフの通番を増やす
			num++;
		}
		
		return m_retMap;
	}
	
	
	/*
	 * マニュアルモード
	 * プロパティに設定されている監視項目IDの情報のみを出力する
	 */
	private HashMap<String, Object> createDataSourceManual(int num) throws ReportingPropertyNotFound {
		// プロパティからグラフ生成にかかわる情報を取得
		if(m_propertiesMap.get(DIVIDER_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(DIVIDER_KEY_VALUE+"."+num + " is not defined.");
		}
		int divider = Integer.parseInt(m_propertiesMap.get(DIVIDER_KEY_VALUE+"."+num));
		
		if(m_propertiesMap.get(MONITOR_ID_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(MONITOR_ID_KEY_VALUE+"."+num + " is not defined.");
		}
		String monitorId = m_propertiesMap.get(MONITOR_ID_KEY_VALUE+"."+num);
		
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);
		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, m_facilityId + "_" + monitorId + "_" + dayString);
		
		String[] columns = { "monitor_id", "item_name", "display_name", "date_time", "facilityid", "value" };
		
		OutputMonitorInfo outputMonitorInfo = getOutputMonitorInfo(m_facilityId, monitorId);
		
		try {
			// 対象の項目に対する収集値が空の場合
			if(outputMonitorInfo == null) {
				// 空のCSVファイルを作成
				getEmptyCsv(csvFileName, m_facilityId, monitorId, columns);
				
				JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
				ds.setUseFirstRowAsHeader(true);
				m_retMap.put(ReportingConstant.STR_DS+"_"+num, ds);
				m_retMap.put(CHART_TITLE_KEY_VALUE+"."+num, "Data is empty(" + monitorId + ")");
				m_log.info("data is empty");
				
				return m_retMap;
			}
			
			m_log.info("createDataSourceManual() : facility_id : " + m_facilityId + ", monitor_id : " + monitorId);
			
			// 「デバイス別」のグラフに関するデータ
			if(outputMonitorInfo.getDisplayName() != null && outputMonitorInfo.getDisplayName().size() > 1) {
				getCsvFromDBDevice(csvFileName, m_facilityId, monitorId, columns, divider, outputMonitorInfo);
			} 
			// 「デバイス別」ではないグラフに関するデータ
			else {
				getCsvFromDB(csvFileName, m_facilityId, monitorId, columns, divider, outputMonitorInfo);
			}
			
			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);
			
			m_retMap.put(ReportingConstant.STR_DS+"_"+num, ds);
			
			// そのほかのパラメータも確認し、値が格納されていなければ入れる
			if(m_propertiesMap.get(CHART_TITLE_KEY_VALUE+"."+num) == null || m_propertiesMap.get(CHART_TITLE_KEY_VALUE+"."+num).isEmpty()) {
				m_retMap.put(CHART_TITLE_KEY_VALUE+"."+num, outputMonitorInfo.getItemName()+" ("+outputMonitorInfo.getMonitorId()+")");
			}
			
			if(m_propertiesMap.get(LABEL_KEY_VALUE+"."+num) == null || m_propertiesMap.get(LABEL_KEY_VALUE+"."+num).isEmpty()) {
				m_retMap.put(LABEL_KEY_VALUE+"."+num, outputMonitorInfo.getMeasure());
			}
			
			if(m_propertiesMap.get(FIXVAL_KEY_VALUE+"."+num) == null || m_propertiesMap.get(FIXVAL_KEY_VALUE+"."+num).isEmpty()) {
				// グラフの上限値を設定 単位が「%」の場合は、100を上限とするグラフにする
				double fixval = 0;
				if (outputMonitorInfo.getMeasure().equals("%")) {
					fixval = 100;
				}
				else {
					fixval = outputMonitorInfo.getMaxValue();
				}
				m_retMap.put(FIXVAL_KEY_VALUE+"."+num, fixval);
			}
			
		} catch (Exception e) {
			m_log.error(e, e);
		}
		
		return m_retMap;
	}
		
		
	/**
	 * 監視結果（収集）情報に関するCSVファイルを作成する
	 * 
	 * @param csvFileName
	 * @param facilityId
	 * @param monitorId
	 * @param columns
	 * @param divider
	 * @return
	 */
	private String getCsvFromDB(String csvFileName, String facilityId, String monitorId, String[] columns, int divider, OutputMonitorInfo outputMonitorInfo) {
		String columnsStr = ReportUtil.joinStrings(columns,  ",");
		m_log.info("output monitorId : " + monitorId + ", csv: " + csvFileName);

		// get data from Hinemos DB
		try {
			File csv = new File(csvFileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(csv, false));
			bw.write(columnsStr);
			bw.newLine();
			
			m_log.debug("debug print  monitor_id : " + outputMonitorInfo.getMonitorId()
					+ ", itemName : " + outputMonitorInfo.getItemName()
					+ ", measure : " + outputMonitorInfo.getMeasure()
					+ ", displayName : " + outputMonitorInfo.getDisplayName() 
					+ ", runInterval : " + outputMonitorInfo.getRunInterval());

			// 出力値をCSVファイルに書き込む	
			ReportingCollectControllerBean controller = new ReportingCollectControllerBean();
			List<CollectKeyInfo> keyInfoList = controller.getReportCollectKeyList(monitorId, facilityId);
			List<Integer> idList = new LinkedList<>();
			Map<Integer, CollectKeyInfo> itemDataMap = new HashMap<>();
			if (keyInfoList == null) {
				m_log.warn("data not found in log.cc_collect_key");
			} else {
				for (CollectKeyInfo collectKeyInfo : keyInfoList) {
					idList.add(collectKeyInfo.getCollectorid());
					itemDataMap.put(collectKeyInfo.getCollectorid(), collectKeyInfo);
				}
			}
			
			// Hinemos DB内の情報を基に出力値を算出する
			List<String[]> resultList = this.getResultList(idList, columns, divider, itemDataMap, outputMonitorInfo);
			
			// 出力値をCSVファイルに書き込む
			if (!resultList.isEmpty()) {
				// write to csv file
				for (String[] results : resultList) {
					bw.write(ReportUtil.joinStrings(results, ","));
					bw.newLine();
				}
			}
			bw.close();
		} catch (IOException | InvalidRole | HinemosUnknown e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		}

		return csvFileName;
	}

	/**
	 * 監視結果（収集）のデバイス別情報に関するCSVファイルを作成する
	 * 
	 * @param csvFileName
	 * @param facilityId
	 * @param monitorId
	 * @param columns
	 * @param divider
	 * @return
	 */
	private String getCsvFromDBDevice(String csvFileName, String facilityId, String monitorId, String[] columns, int divider, OutputMonitorInfo outputMonitorInfo) {
		String columnsStr = ReportUtil.joinStrings(columns,  ",");
		m_log.info("output csv: " + csvFileName);

		// get data from Hinemos DB
		try {
			File csv = new File(csvFileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(csv, false));
			bw.write(columnsStr);
			bw.newLine();
				
			LinkedList<String[]> resultList = new LinkedList<String[]>();

			if (outputMonitorInfo != null) {
				m_log.debug("debug print  monitor_id : " + outputMonitorInfo.getMonitorId()
				+ ", itemName : " + outputMonitorInfo.getItemName()
				+ ", measure : " + outputMonitorInfo.getMeasure() 
				+ ", displayName : " + outputMonitorInfo.getDisplayName() 
				+ ", runInterval : " + outputMonitorInfo.getRunInterval());
		
				for (String displayName : outputMonitorInfo.getDisplayName()) {
					List<Integer> idList = new LinkedList<>();
					Map<Integer, CollectKeyInfo> itemDataMap = new HashMap<>();
					
					ReportingMonitorControllerBean monotorController = new ReportingMonitorControllerBean();
					MonitorInfo monitorInfo = monotorController.getMonitorInfo(outputMonitorInfo.getMonitorId());
					
					if (monitorInfo != null) {
						ReportingCollectControllerBean controller = new ReportingCollectControllerBean();
						CollectKeyInfo collectKeyInfo = controller.getReportingCollectKeyInfo(
								monitorInfo.getItemName(), displayName, monitorInfo.getMonitorId(), facilityId);
						if (collectKeyInfo == null) {
							m_log.warn("data not found in log.cc_collect_key");
						} else {
							idList.add(collectKeyInfo.getCollectorid());
							itemDataMap.put(collectKeyInfo.getCollectorid(), collectKeyInfo);
						}
					}
					
					// Hinemos DB内の情報を基に出力値を算出する
					resultList.addAll(this.getResultList(idList, columns, divider, itemDataMap, outputMonitorInfo));
				}
			}
			// 出力値をCSVファイルに書き込む
			if (!resultList.isEmpty()) {
				// write to csv file
				for (String[] results : resultList) {
					bw.write(ReportUtil.joinStrings(results, ","));
					bw.newLine();
				}
			}
			bw.close();
		} catch (IOException | InvalidRole | HinemosUnknown | MonitorNotFound e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		}

		return csvFileName;
	}

	/**
	 * 空のCSVファイルを作成する
	 * 
	 * @param csvFileName
	 * @param facilityId
	 * @param monitorId
	 * @param columns
	 * @param divider
	 * @return
	 */
	private String getEmptyCsv(String csvFileName, String facilityId, String monitorId, String[] columns) {
		String columnsStr = ReportUtil.joinStrings(columns,  ",");
		m_log.info("output monitorId : " + monitorId + ", csv: " + csvFileName);

		// カラムだけの空のCSVファイルを作成
		try {
			File csv = new File(csvFileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(csv, false));
			bw.write(columnsStr);
			bw.newLine();
			bw.close();
		} catch (Exception e) {
			m_log.error(e, e);
		}

		return csvFileName;
	}
	
	/**
	 * 収集データの中から、レポート出力に使用する監視項目情報を格納する。
	 * キー：monitor_id
	 * 
	 * @param facilityId
	 * @param itemFilter
	 * @return 
	 */
	private OutputMonitorInfo getOutputMonitorInfo(String facilityId, String monitorId) {
		
		m_log.info("getting output monitorId = " + monitorId + ", facilityId = " + facilityId);
		
		OutputMonitorInfo info = null;
		
		try {
			String ownerRoleId = null;
			if (!"ADMINISTRATORS".equals(ReportUtil.getOwnerRoleId())) {
				ownerRoleId = ReportUtil.getOwnerRoleId();
			}
			
			ReportingMonitorControllerBean monitorController = new ReportingMonitorControllerBean();
			List<Object[]> monitorInfoList = monitorController.getMonitorInfoListByMonitorTypeId(facilityId, monitorId, m_startDate.getTime(), m_endDate.getTime(), null, ownerRoleId);
			
			if (monitorInfoList != null) {
				for (Object[] monitorInfo : monitorInfoList) {
					m_log.debug("from db data " + monitorInfo[0].toString() + ", " + monitorInfo[1].toString() + ", " + monitorInfo[2].toString()
					+ ", " + monitorInfo[3].toString() + "," + (Integer)monitorInfo[4]);
					// 既存データが存在する場合は、displayNameが複数存在するケースのみであるため、
					// displayNameを追加し、デバイスフラグをtrueに変更
					if (info != null) {
						info.getDisplayName().add(monitorInfo[3].toString());
						info.setDeviceFlg(true);
					}
					// 存在しない場合は、新規作成
					else {
						info = new OutputMonitorInfo();
						info.setMonitorId(monitorId);
						info.setItemName(monitorInfo[1].toString());
						info.setMeasure(HinemosMessage.replace(monitorInfo[2].toString()));
						info.getDisplayName().add(monitorInfo[3].toString());
						info.setRunInterval((Integer)monitorInfo[4]);
						info.setDeviceFlg(false);
						info.setMaxValue(0.0);
						
						m_log.debug("new    put " + info.getMonitorId() + ", " + info.getItemName() + ", " + info.getMeasure() + ", " + info.getDisplayName() + "," + info.getRunInterval());
					}
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}

		return info;
	}

	/**
	 * 収集データの中から、レポート出力に使用する監視項目情報を格納する。
	 * キー：monitor_id
	 * 
	 * @param facilityId
	 * @param itemFilter
	 * @return 
	 */
	private TreeMap<String, OutputMonitorInfo> getOutputMonitorInfo(String facilityId, String itemFilter, boolean collectorFlg) {
		TreeMap<String, OutputMonitorInfo> map = new TreeMap<>();
		m_log.info("getting output monitor_id in " + facilityId);
		
		try {
			String ownerRoleId = null;
			if (!"ADMINISTRATORS".equals(ReportUtil.getOwnerRoleId())) {
				ownerRoleId = ReportUtil.getOwnerRoleId();
			}

			ReportingMonitorControllerBean monitorController = new ReportingMonitorControllerBean();
			List<Object[]> monitorInfoList = monitorController.getMonitorInfoListByMonitorTypeId(facilityId, null, m_startDate.getTime(), m_endDate.getTime(), collectorFlg, ownerRoleId);
			if (monitorInfoList != null) {
				OutputMonitorInfo info = null;
				OutputMonitorInfo checkInfo = null;
				String monitorId = null;
				for (Object[] monitorInfo : monitorInfoList) {
					monitorId = monitorInfo[0].toString();
					String itemName = monitorInfo[1].toString();
					String measure = monitorInfo[2].toString();
					String displayName = monitorInfo[3].toString();
					String runInterval = monitorInfo[4].toString();
					checkInfo = map.get(monitorId);
					
					m_log.debug("from db data " + monitorId + ", " + itemName + ", " + measure
					+ ", " + displayName + "," + runInterval);
					// 既存データが存在する場合は、displayNameが複数存在するケースのみであるため、
					// displayNameを追加し、デバイスフラグをtrueに変更
					if (checkInfo != null) {
						
						checkInfo.getDisplayName().add(displayName);
						checkInfo.setDeviceFlg(true);
						
						map.put(monitorId, checkInfo);
						m_log.debug("update put " + checkInfo.getMonitorId() + ", " + checkInfo.getItemName() + ", " + checkInfo.getMeasure() 
								+ ", " + checkInfo.getDisplayName() + "," + checkInfo.getRunInterval() + "," + checkInfo.isDeviceFlg());
					}
					// 存在しない場合は、新規作成
					else {
						
						info = new OutputMonitorInfo();
						info.setMonitorId(monitorId);
						info.setItemName(itemName);
						info.setMeasure(HinemosMessage.replace(measure));
						info.getDisplayName().add(displayName);
						info.setRunInterval(Integer.parseInt(runInterval));
						info.setDeviceFlg(false);
						info.setMaxValue(0.0);
						
						map.put(monitorId, info);
						m_log.debug("new    put " + info.getMonitorId() + ", " + info.getItemName() + ", " + info.getMeasure() + ", " + info.getDisplayName() + "," + info.getRunInterval());
					}
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}

		return map;
	}
	
	/**
	 * サマリデータの取得<BR>
	 * 
	 * @param idList
	 * @param columns
	 * @param divider
	 * @param itemDataMap
	 * @param outputMonitorInfo
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private List<String[]> getResultList(List<Integer> idList, String[] columns, int divider, Map<Integer, CollectKeyInfo> itemDataMap, OutputMonitorInfo outputMonitorInfo) throws InvalidRole, HinemosUnknown {
		List<String[]> resultList = new ArrayList<String[]>();
		
		ReportingCollectControllerBean controller = new ReportingCollectControllerBean();
		// サマリデータ、または収集データ(raw)のタイプでスイッチ
		int summaryType = ReportUtil.getSummaryType(m_startDate.getTime(), m_endDate.getTime());
		switch (summaryType) {
		case SummaryTypeConstant.TYPE_AVG_HOUR:
			List<SummaryHour> summaryHList = null;
			try {
				summaryHList = controller.getSummaryHourList(idList, m_startDate.getTime(), m_endDate.getTime());
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryHList == null || summaryHList.isEmpty())
				break;
			for (SummaryHour summaryHour : summaryHList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = itemDataMap.get(summaryHour.getCollectorId());
				results[0] = keyInfo.getMonitorId();
				String itemName = HinemosMessage.replace(keyInfo.getItemName());
				results[1] = '"' + itemName.replace("\"", "\"\"") + '"';
				if(!"".equals(keyInfo.getDisplayName())){
					results[2] = keyInfo.getDisplayName();
				}
				else {
					results[2] = '"' + itemName.replace("\"", "\"\"") + '"';
				}
				Timestamp time = new Timestamp(summaryHour.getTime());
				results[3] = time.toString();
				results[4] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryHour.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[5] = Double.toString(value / divider);
					} else {
						results[5] = value.toString();
					}
					resultList.add(results);
					
					// 値の最大値を格納
					if(outputMonitorInfo != null) {
						if(outputMonitorInfo.getMaxValue() < Double.parseDouble(results[5]) ) {
							outputMonitorInfo.setMaxValue(Double.parseDouble(results[5]));
						}
					}
				}
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_DAY:
			List<SummaryDay> summaryDList = null;
			try {
				summaryDList = controller.getSummaryDayList(idList, m_startDate.getTime(), m_endDate.getTime());
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryDList == null || summaryDList.isEmpty())
				break;
			for (SummaryDay summaryDay : summaryDList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = itemDataMap.get(summaryDay.getCollectorId());
				results[0] = keyInfo.getMonitorId();
				String itemName = HinemosMessage.replace(keyInfo.getItemName());
				results[1] = '"' + itemName.replace("\"", "\"\"") + '"';
				if(!"".equals(keyInfo.getDisplayName())){
					results[2] = keyInfo.getDisplayName();
				}
				else {
					results[2] = '"' + itemName.replace("\"", "\"\"") + '"';
				}
				Timestamp time = new Timestamp(summaryDay.getTime());
				results[3] = time.toString();
				results[4] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryDay.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[5] = Double.toString(value / divider);
					} else {
						results[5] = value.toString();
					}
					resultList.add(results);
					
					// 値の最大値を格納
					if(outputMonitorInfo != null) {
						if(outputMonitorInfo.getMaxValue() < Double.parseDouble(results[5]) ) {
							outputMonitorInfo.setMaxValue(Double.parseDouble(results[5]));
						}
					}
				}
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_MONTH:
			List<SummaryMonth> summaryMList = null;
			try {
				summaryMList = controller.getSummaryMonthList(idList, m_startDate.getTime(), m_endDate.getTime());
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryMList == null || summaryMList.isEmpty())
				break;
			for (SummaryMonth summaryMonth : summaryMList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = itemDataMap.get(summaryMonth.getCollectorId());
				results[0] = keyInfo.getMonitorId();
				String itemName = HinemosMessage.replace(keyInfo.getItemName());
				results[1] = '"' + itemName.replace("\"", "\"\"") + '"';
				if(!"".equals(keyInfo.getDisplayName())){
					results[2] = keyInfo.getDisplayName();
				}
				else {
					results[2] = '"' + itemName.replace("\"", "\"\"") + '"';
				}
				Timestamp time = new Timestamp(summaryMonth.getTime());
				results[3] = time.toString();
				results[4] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryMonth.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[5] = Double.toString(value / divider);
					} else {
						results[5] = value.toString();
					}
					resultList.add(results);
					
					// 値の最大値を格納
					if(outputMonitorInfo != null) {
						if(outputMonitorInfo.getMaxValue() < Double.parseDouble(results[5]) ) {
							outputMonitorInfo.setMaxValue(Double.parseDouble(results[5]));
						}
					}
				}
			}
			break;
		default: // defaultはRAWとする
			List<CollectData> summaryList = null;
			try {
				summaryList = controller.getCollectDataList(idList, m_startDate.getTime(), m_endDate.getTime());
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryList == null || summaryList.isEmpty())
				break;
			for (CollectData data : summaryList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = itemDataMap.get(data.getCollectorId());
				results[0] = keyInfo.getMonitorId();
				String itemName = HinemosMessage.replace(keyInfo.getItemName());
				results[1] = '"' + itemName.replace("\"", "\"\"") + '"';
				if(!"".equals(keyInfo.getDisplayName())){
					results[2] = keyInfo.getDisplayName();
				}
				else {
					results[2] = '"' + itemName.replace("\"", "\"\"") + '"';
				}
				Timestamp time = new Timestamp(data.getTime());
				results[3] = time.toString();
				results[4] = keyInfo.getFacilityid();
				Double value = Double.valueOf(data.getValue());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[5] = Double.toString(value / divider);
					} else {
						results[5] = value.toString();
					}
					resultList.add(results);
					
					// 値の最大値を格納
					if(outputMonitorInfo != null) {
						if(outputMonitorInfo.getMaxValue() < Double.parseDouble(results[5]) ) {
							outputMonitorInfo.setMaxValue(Double.parseDouble(results[5]));
						}
					}
				}
			}
			break;
		}
		return resultList;
	}
}
