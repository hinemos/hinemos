/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.vcloud.factory;


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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.factory.DatasourceBase;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingCollectControllerBean;
import com.clustercontrol.reporting.vcloud.session.ReportingVCloudControllerBean;

import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * 課金アラートの収集値（計算結果）についてレポート作成を行うクラス
 */
public class DatasourceBillingAlarmLineGraph extends DatasourceBase {

	private static Log m_log = LogFactory.getLog(DatasourceBillingAlarmLineGraph.class);

	private static final String OUTPUT_MODE_KEY_VALUE = "output.mode";
	private static final String DIVIDER_KEY_VALUE = "divider";
	private static final String MONITOR_ID_KEY_VALUE = "graph.monitor.id";
	private static final String MONITOR_TYPE_KEY_VALUE = "monitor.type";
	private static final String CHART_TITLE_KEY_VALUE = "chart.title";
	private static final String LABEL_NAME_KEY_VALUE = "label";

	private static final String MODE_AUTO = "auto";
	private static final String MODE_MAN = "manual";

	private HashMap<String, Object> m_retMap = new HashMap<String, Object>();
	private int m_num;
	private String m_monitorId =null;

	/**
	 * データソース（CSVファイル）をHinemos DBから生成する
	 * 「label」「chart.title」がプロパティファイルに記載されていない場合、DBから取得した情報を差し込む
	 */
	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {
		this.m_num = num;

		String outputMode = isDefine(OUTPUT_MODE_KEY_VALUE, MODE_AUTO);
		// 条件に合致する監視項目の情報をすべて出力する
		if(outputMode.equals(MODE_AUTO)) {
			return createDataSourceAuto(num);
		}
		else if (outputMode.equals(MODE_MAN)) {
			return createDataSourceManual(num);
		} else {
			throw new ReportingPropertyNotFound("unknown output mode : " + outputMode);
		}
	}

	/*
	 * オートモード
	 * ver.4.1のレポーティングオプションと同様の動きをする
	 */
	private HashMap<String, Object> createDataSourceAuto(int num) throws ReportingPropertyNotFound {
		String dayString = new SimpleDateFormat("yyyyMMdd").format(m_startDate);
		// プロパティからグラフ生成にかかわる情報を取得
		try {
			m_log.info("start create csvfile. " + m_propertiesMap.get("template.name"+num));
			String csvFileName = getCsvFromDB(dayString, m_facilityId);
			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);
			m_retMap.put(ReportingConstant.STR_DS+"_"+num, ds);
		} catch (Exception e) {
			m_log.error(e, e);
		}
		return m_retMap;
	}

	/*
	 * マニュアルモード
	 * プロパティに設定されている監視項目IDの情報のみを出力する
	 */
	private HashMap<String, Object> createDataSourceManual(int num) throws ReportingPropertyNotFound {
		if(m_propertiesMap.get(MONITOR_ID_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(MONITOR_ID_KEY_VALUE+"."+num + " is not defined.");
		}
		// プロパティからマニュアル固有動作にかかわる情報を取得
		m_monitorId = m_propertiesMap.get(MONITOR_ID_KEY_VALUE+"."+num);
		String dayString = new SimpleDateFormat("yyyyMMdd").format(m_startDate);
		try {
			m_log.info("createDataSourceManual() : facility_id : " + m_facilityId + ", monitor_id : " + m_monitorId);
			String csvFileName = getCsvFromDB(dayString, m_facilityId);

			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);
			m_retMap.put(ReportingConstant.STR_DS+"_"+num, ds);
		} catch (Exception e) {
			m_log.error(e, e);
		}
		return m_retMap;
	}

	/**
	 * 計算結果（収集）情報に関するCSVファイルを作成する
	 * 
	 * @param dayString 実行日時
	 * @param facilityId
	 * @return csvFileName
	 */
	private String getCsvFromDB(String dayString, String facilityId) {
		String[] columns = { "collectorid", "display_name", "date_time" ,"facilityid", "value"};
		
		String columnsStr = ReportUtil.joinStringsToCsv(columns);

		// プロパティからグラフ生成にかかわる情報を取得
		int divider = Integer.parseInt(isDefine(m_propertiesMap.get(DIVIDER_KEY_VALUE+"."+m_num), "1"));
		String type = m_propertiesMap.get(MONITOR_TYPE_KEY_VALUE+"."+m_num);
		String chartTitle = m_propertiesMap.get(CHART_TITLE_KEY_VALUE+"."+m_num);
		String labelName = m_propertiesMap.get(LABEL_NAME_KEY_VALUE+"."+m_num);

		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, type + "_" + dayString);
		try {
			// 現在収集中の収集値をレポーティングオプションの設定で指定された監視設定の種別ごとに取得しに行く
			// ここで対象とするのは課金アラーム設定で存在している種別ごとのみとする
			ReportingVCloudControllerBean vCloudControllerBean = new ReportingVCloudControllerBean();
			List<Object[]> alarmIdList = vCloudControllerBean.getBillingMonitorIdByMonitorKind(m_monitorId, type);
			
			if (alarmIdList != null) {
				ReportingCollectControllerBean collectController = new ReportingCollectControllerBean();
				List<Integer> idList = new LinkedList<>();
				Map<Integer, CollectKeyInfo> itemDataMap = new HashMap<>();
				for (Object alarmId : alarmIdList) {
					List<CollectKeyInfo> keyInfoList = collectController.getReportCollectKeyList(alarmId.toString(), facilityId);
					if (keyInfoList == null) {
						m_log.warn("data not found in log.cc_collect_key");
					} else {
						for (CollectKeyInfo keyInfo : keyInfoList) {
							idList.add(keyInfo.getCollectorid());
							itemDataMap.put(keyInfo.getCollectorid(), keyInfo);
							if (chartTitle == null || chartTitle.isEmpty()) {
								chartTitle= keyInfo.getDisplayName();
							}
						}
					}
					if (labelName == null || labelName.isEmpty()) {
						labelName = "USD";
					}
				}
				List<String[]> resultList = this.getResultList(idList, columns, divider, itemDataMap);
				
				// write to csv file
				BufferedWriter bw = null;
				try {
					File csv = new File(csvFileName);
					m_log.debug("debug print  facilityId : " + m_facilityId
							+ ", monitorType : " + type);
					bw = new BufferedWriter(new FileWriter(csv, false));
					bw.write(columnsStr);
					bw.newLine();
					m_log.info("create csv file. " + csvFileName);
					if (!resultList.isEmpty()) {
						for (String[] results : resultList) {
							bw.write(ReportUtil.joinStringsToCsv(results));
							bw.newLine();
						}
					}
					bw.close();
				} catch (Exception e1) {
					m_log.error(e1, e1);
					if (bw != null) {
						try {
							bw.close();
						} catch (IOException e) {
							m_log.error(e, e);
						}
					}
				}
			} else {
				m_log.warn("No Table Information. ");
			}
		} catch (Exception e1) {
			m_log.error(e1, e1);
		}
		
		m_retMap.put(CHART_TITLE_KEY_VALUE+"."+m_num,chartTitle);
		m_retMap.put(LABEL_NAME_KEY_VALUE+"."+m_num,labelName);
		return csvFileName;
	}

	private List<String[]> getResultList(List<Integer> idList, String[] columns, int divider,
			Map<Integer, CollectKeyInfo> itemDataMap) {
		List<String[]> resultList = new ArrayList<String[]>();

		// オーナーロールID取得
		String ownerRoleId = null;
		if (!"ADMINISTRATORS".equals(ReportUtil.getOwnerRoleId())) {
			ownerRoleId = ReportUtil.getOwnerRoleId();
		}

		ReportingCollectControllerBean controller = new ReportingCollectControllerBean();
		// サマリデータ、または収集データ(raw)のタイプでスイッチ
		int summaryType = ReportUtil.getSummaryType(m_startDate.getTime(), m_endDate.getTime());
		switch (summaryType) {
		case SummaryTypeConstant.TYPE_AVG_HOUR:
			List<SummaryHour> summaryHList = null;
			try {
				summaryHList = controller.getSummaryHourList(idList, m_startDate.getTime(), m_endDate.getTime(), ownerRoleId);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryHList == null || summaryHList.isEmpty())
				break;
			for (SummaryHour summaryHour : summaryHList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = itemDataMap.get(summaryHour.getCollectorId());

				Timestamp time = new Timestamp(summaryHour.getTime());

				results[0] = keyInfo.getMonitorId();
				results[1] = keyInfo.getMonitorId() +" ("+keyInfo.getFacilityid()+")";
				results[2] = time.toString();
				results[3] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryHour.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[4] = Double.toString(value / divider);
					} else {
						results[4] = value.toString();
					}
					resultList.add(results);
				}
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_DAY:
			List<SummaryDay> summaryDList = null;
			try {
				summaryDList = controller.getSummaryDayList(idList, m_startDate.getTime(), m_endDate.getTime(), ownerRoleId);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryDList == null || summaryDList.isEmpty())
				break;
			for (SummaryDay summaryDay : summaryDList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = itemDataMap.get(summaryDay.getCollectorId());

				Timestamp time = new Timestamp(summaryDay.getTime());

				results[0] = keyInfo.getMonitorId();
				results[1] = keyInfo.getMonitorId() +" ("+keyInfo.getFacilityid()+")";
				results[2] = time.toString();
				results[3] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryDay.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[4] = Double.toString(value / divider);
					} else {
						results[4] = value.toString();
					}
					resultList.add(results);
				}
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_MONTH:
			List<SummaryMonth> summaryMList = null;
			try {
				summaryMList = controller.getSummaryMonthList(idList, m_startDate.getTime(), m_endDate.getTime(), ownerRoleId);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryMList == null || summaryMList.isEmpty())
				break;
			for (SummaryMonth summaryMonth : summaryMList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = itemDataMap.get(summaryMonth.getCollectorId());

				Timestamp time = new Timestamp(summaryMonth.getTime());

				results[0] = keyInfo.getMonitorId();
				results[1] = keyInfo.getMonitorId() +" ("+keyInfo.getFacilityid()+")";
				results[2] = time.toString();
				results[3] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryMonth.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[4] = Double.toString(value / divider);
					} else {
						results[4] = value.toString();
					}
					resultList.add(results);
				}
			}
			break;
		default: // defaultはRAWとする
			List<CollectData> summaryList = null;
			try {
				summaryList = controller.getCollectDataList(idList, m_startDate.getTime(), m_endDate.getTime(), ownerRoleId);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryList == null || summaryList.isEmpty())
				break;
			for (CollectData data : summaryList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = itemDataMap.get(data.getCollectorId());

				Timestamp time = new Timestamp(data.getTime());

				results[0] = keyInfo.getMonitorId();
				results[1] = keyInfo.getMonitorId() +" ("+keyInfo.getFacilityid()+")";
				results[2] = time.toString();
				results[3] = keyInfo.getFacilityid();
				Double value = Double.valueOf(data.getValue());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[4] = Double.toString(value / divider);
					} else {
						results[4] = value.toString();
					}
					resultList.add(results);
				}
			}
			break;
		}
		return resultList;
	}
}
