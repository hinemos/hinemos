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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
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
import com.clustercontrol.jmx.model.JmxCheckInfo;
import com.clustercontrol.jmx.model.JmxMasterInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.ent.session.ReportingJmxControllerBean;
import com.clustercontrol.reporting.ent.util.PropertiesConstant;
import com.clustercontrol.reporting.factory.DatasourceBase;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingCollectControllerBean;
import com.clustercontrol.reporting.util.ReportingQueryUtil;
import com.clustercontrol.util.HinemosMessage;

import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * JMX情報のグラフの作成元となる性能値のデータソースを作成するクラス
 * 
 * @version 5.0.b
 * @since 5.0.b
 */
public class DatasourceJMXLineGraph extends DatasourceBase{
	private static Log m_log = LogFactory.getLog(DatasourceJMXLineGraph.class);

	private final String[] COLUMNS = { "collectorid", "item_code", "item_name", "date_time", "facilityid", "value" };

	private Map<String, List<OptimalMonitorIdInfo>> m_monitorIdMap = null;

	private static class OptimalMonitorIdInfo {
		private String monitorId;
		private String itemCode;
		private int runInterval;

		OptimalMonitorIdInfo(String monitorId, String itemCode, int runInterval){
			this.monitorId = monitorId;
			this.itemCode = itemCode;
			this.runInterval = runInterval;
		}
		
		public String getMonitorId(){
			return this.monitorId;
		}
		public String getItemCode(){
			return this.itemCode;
		}

		@Override
		public String toString() {
			return String.format("%s, %s, %d", this.monitorId, this.itemCode, this.runInterval);
		}
	}

	/**
	 * データソース（CSVファイル）をHinemos DBから生成する
	 */
	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {

		if(m_propertiesMap.get(SUFFIX_KEY_VALUE + "." + num).isEmpty()) {
			throw new ReportingPropertyNotFound(SUFFIX_KEY_VALUE + "." + num + " is not defined.");
		}
		String suffix = m_propertiesMap.get(SUFFIX_KEY_VALUE + "." + num);

		if(m_propertiesMap.get(PropertiesConstant.ITEM_CODE_KEY + "." + num).isEmpty()) {
			throw new ReportingPropertyNotFound(PropertiesConstant.ITEM_CODE_KEY + "." + num + " is not defined.");
		}
		String itemCodes = m_propertiesMap.get(PropertiesConstant.ITEM_CODE_KEY + "." + num);

		if(m_propertiesMap.get(PropertiesConstant.DIVIDER_KEY + "." + num).isEmpty()) {
			throw new ReportingPropertyNotFound(PropertiesConstant.DIVIDER_KEY + "." + num + " is not defined.");
		}
		int divider = Integer.parseInt(m_propertiesMap.get(PropertiesConstant.DIVIDER_KEY + "." + num));

		m_monitorIdMap = getOptimalMonitorId(m_facilityId);
		String[] items = itemCodes.split(",");

		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);

		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, m_facilityId + "_" + suffix + "_" + dayString);
		HashMap<String, Object> retMap = new HashMap<String, Object>();

		// CSVファイルが既に存在している場合は、データ作成処理をスキップ
		if(new File(csvFileName).exists()){
			m_log.info("File : " + csvFileName + " is exists.");
		} else {
			getCsvFromDB(csvFileName, m_facilityId, items, COLUMNS, divider);
		}

		try {
			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);

			retMap.put(ReportingConstant.STR_DS + "_" + num, ds);

		} catch (Exception e) {
			m_log.error(e, e);
		}

		return retMap;
	}

	/**
	 * リソース情報に関するCSVファイルを作成する
	 * 
	 * @param csvFileName
	 * @param facilityId
	 * @param items
	 * @param columns
	 * @param divider
	 * @return
	 */
	private String getCsvFromDB(String csvFileName, String facilityId, String[] items, String[] columns, int divider) {
		String columnsStr = ReportUtil.joinStrings(columns, ",");
		m_log.info("output csv: " + csvFileName);

		// get data from Hinemos DB
		BufferedWriter bw = null;
		try {
			File csv = new File(csvFileName);
			bw = new BufferedWriter(new FileWriter(csv, false));
			bw.write(columnsStr);
			bw.newLine();

			// 監視項目ID、収集項目のセットごとに算出情報を確認する
			Map<Integer, Object[]> itemDataMap = new HashMap<>();
			List<Integer> idList = new LinkedList<>();
			for (String code : items) {
				List<OptimalMonitorIdInfo> infoList = m_monitorIdMap.get(code);

				// Skip if not found in monitor id map
				if(null == infoList)
					continue;

				for (OptimalMonitorIdInfo info : m_monitorIdMap.get(code)) {

					if (info != null) {
						// 出力値をCSVファイルに書き込む	
						// Hinemos DB内の情報を基に出力値を算出する
						ReportingJmxControllerBean controller = new ReportingJmxControllerBean();
						JmxCheckInfo jmxCheckInfo = controller.getMonitorJmxInfo(info.getMonitorId(), info.getItemCode());
						if (jmxCheckInfo == null) {
							m_log.warn("monitorId and masterId not found in setting.cc_monitor_jmx_info");
						} else {
							JmxMasterInfo jmxMasterInfo = controller.getJmxMasterInfoPK(jmxCheckInfo.getMasterId());
							if (jmxMasterInfo != null) {
								ReportingCollectControllerBean collectController = new ReportingCollectControllerBean();
								List<CollectKeyInfo> keyInfoList = collectController.getReportingCollectKeyInfoList(null, null, jmxCheckInfo.getMonitorId(), facilityId);
								for (CollectKeyInfo keyInfo : keyInfoList) {
									idList.add(keyInfo.getCollectorid());
									Object[] data = new Object[2];
									data[0] = keyInfo;
									data[1] = jmxMasterInfo;
									itemDataMap.put(keyInfo.getCollectorid(), data);
								}
							}
						}
					}
				}
			}
			List<String[]> resultList = this.getResultList(idList, columns.length, divider, itemDataMap);

			if (!resultList.isEmpty()) {
				// write to CSV file
				for (String[] row : resultList) {
					bw.write(ReportUtil.joinStrings(row, ","));
					bw.newLine();
				}
			}
		} catch (IOException | MonitorNotFound e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					m_log.warn("bw is not closed.", e);
				}
			}
		}

		return csvFileName;
	}

	/**
	 * 収集データの中から、レポート出力に使用する監視項目を収集項目単位で選定する。
	 * キー：item_code, display_name
	 * 
	 * @param facilityId
	 * @return 
	 */
	private Map<String, List<OptimalMonitorIdInfo>> getOptimalMonitorId( String facilityId ){
		Map<String, List<OptimalMonitorIdInfo>> map = new LinkedHashMap<>();
		m_log.info("getting optimal monitor_id for each item_code in " + facilityId);

		try {
			// Skip owner role clause if ADMINISTRATORS
			String role_id = ReportUtil.getOwnerRoleId();
			if ("ADMINISTRATORS".equals(role_id)) {
				role_id = null;
			}
			ReportingJmxControllerBean controller = new ReportingJmxControllerBean();
			List<MonitorInfo> monitorInfoList = controller.getMonitorInfoByMonitorTypeId(HinemosModuleConstant.MONITOR_JMX, role_id);
			if (monitorInfoList != null) {
				OptimalMonitorIdInfo info = null;
				List<OptimalMonitorIdInfo> infoList;
				for (MonitorInfo monitorInfo : monitorInfoList) {
				JmxCheckInfo jmxCheckInfo = controller.getMonitorJmxInfoPK(monitorInfo.getMonitorId());
				if (jmxCheckInfo == null) {
					m_log.warn("monitorId and masterId not found in setting.cc_monitor_jmx_info");
				} else {
					JmxMasterInfo jmxMasterInfo = controller.getJmxMasterInfoPK(jmxCheckInfo.getMasterId());
					if (jmxMasterInfo != null) {
						ReportingCollectControllerBean collectContoller = new ReportingCollectControllerBean();
							List<CollectKeyInfo> keyInfoList = collectContoller.getReportCollectKeyList(monitorInfo.getMonitorId(), facilityId);
							List<Integer> idList = new LinkedList<>();
							for (CollectKeyInfo keyInfo : keyInfoList) {
								idList.add(keyInfo.getCollectorid());
							}
							boolean hasSummaryData = new ReportingQueryUtil().hasSummaryData(idList, m_startDate.getTime(), m_endDate.getTime());
							// データが存在しない場合は、次のデータを処理する。
							if (hasSummaryData) {
								for (CollectKeyInfo existKeyInfo : keyInfoList) {
									info = new OptimalMonitorIdInfo(existKeyInfo.getMonitorId(), jmxMasterInfo.getId(), monitorInfo.getRunInterval());

									// 以降、既にあるキーとの比較
									String key = info.getItemCode();

									// 既存データの取得
									infoList = map.get(key);
									if (null == infoList) {
										// 存在しない場合は、New and Add
										infoList = new ArrayList<>();
										infoList.add(info);

										map.put(key, infoList);
										m_log.debug("new put " + info.toString());
									}else{
										// 既存データが存在する場合は、Add only
										infoList.add(info);
										m_log.debug("add " + info.toString());
									}
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

	/**
	 * サマリデータの取得<BR>
	 * 
	 * @param idList
	 * @param columns
	 * @param divider
	 * @param itemDataMap
	 * @return list
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private List<String[]> getResultList(List<Integer> idList, int length, int divider, Map<Integer, Object[]> itemDataMap) {
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
				String[] results = new String[length];
				Object[] resultData = itemDataMap.get(summaryHour.getCollectorId());
				CollectKeyInfo keyInfo = (CollectKeyInfo)resultData[0];
				JmxMasterInfo jmxMasterInfo = (JmxMasterInfo)resultData[1];
				results[0] = keyInfo.getMonitorId();
				results[1] = jmxMasterInfo.getId();
				String itemName = HinemosMessage.replace(keyInfo.getItemName());
				results[2] = '"' + itemName.replace("\"", "\"\"") + '"';
				results[3] = new Timestamp(summaryHour.getTime()).toString();
				results[4] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryHour.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[5] = Double.toString(value / divider);
					} else {
						results[5] = value.toString();
					}
					resultList.add(results);
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
				String[] results = new String[length];
				Object[] resultData = itemDataMap.get(summaryDay.getCollectorId());
				CollectKeyInfo keyInfo = (CollectKeyInfo)resultData[0];
				JmxMasterInfo jmxMasterInfo = (JmxMasterInfo)resultData[1];
				results[0] = keyInfo.getMonitorId();
				results[1] = jmxMasterInfo.getId();
				String itemName = HinemosMessage.replace(keyInfo.getItemName());
				results[2] = '"' + itemName.replace("\"", "\"\"") + '"';
				results[3] = new Timestamp(summaryDay.getTime()).toString();
				results[4] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryDay.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[5] = Double.toString(value / divider);
					} else {
						results[5] = value.toString();
					}
					resultList.add(results);
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
				String[] results = new String[length];
				Object[] resultData = itemDataMap.get(summaryMonth.getCollectorId());
				CollectKeyInfo keyInfo = (CollectKeyInfo)resultData[0];
				JmxMasterInfo jmxMasterInfo = (JmxMasterInfo)resultData[1];
				results[0] = keyInfo.getMonitorId();
				results[1] = jmxMasterInfo.getId();
				String itemName = HinemosMessage.replace(keyInfo.getItemName());
				results[2] = '"' + itemName.replace("\"", "\"\"") + '"';
				results[3] = new Timestamp(summaryMonth.getTime()).toString();
				results[4] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryMonth.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[5] = Double.toString(value / divider);
					} else {
						results[5] = value.toString();
					}
					resultList.add(results);
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
				String[] results = new String[length];
				Object[] resultData = itemDataMap.get(data.getCollectorId());
				CollectKeyInfo keyInfo = (CollectKeyInfo)resultData[0];;
				JmxMasterInfo jmxMasterInfo = (JmxMasterInfo)resultData[1];
				results[0] = keyInfo.getMonitorId();
				results[1] = jmxMasterInfo.getId();
				String itemName = HinemosMessage.replace(keyInfo.getItemName());
				results[2] = '"' + itemName.replace("\"", "\"\"") + '"';
				results[3] = new Timestamp(data.getTime()).toString();
				results[4] = keyInfo.getFacilityid();
				Double value = Double.valueOf(data.getValue());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[5] = Double.toString(value / divider);
					} else {
						results[5] = value.toString();
					}
					resultList.add(results);
				}
			}
			break;
		}
		return resultList;
	}
}
