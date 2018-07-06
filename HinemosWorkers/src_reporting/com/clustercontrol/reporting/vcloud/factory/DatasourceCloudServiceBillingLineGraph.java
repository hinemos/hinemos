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
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.factory.DatasourceBase;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingCollectControllerBean;
import com.clustercontrol.reporting.vcloud.session.ReportingVCloudControllerBean;
import com.clustercontrol.util.HinemosMessage;

import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * クラウドのサービス課金監視情報のレポート作成を行うクラス
 */
public class DatasourceCloudServiceBillingLineGraph extends DatasourceBase {

	private static Log m_log = LogFactory.getLog(DatasourceCloudServiceBillingLineGraph.class);

	private static final String OUTPUT_MODE_KEY_VALUE = "output.mode";
	private static final String DIVIDER_KEY_VALUE = "divider";
	private static final String MONITOR_ID_KEY_VALUE = "graph.monitor.id";
	private static final String LABEL_NAME_KEY_VALUE = "chart.title";
	
	private static final String MODE_AUTO = "auto";
	private static final String MODE_MAN = "manual";
	

	private HashMap<String, Object> m_retMap = new HashMap<String, Object>();
	private int m_num;

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
			throw new ReportingPropertyNotFound("Unkwon property. key: output.mode, value: " +outputMode);
		}
	}

	/*
	 * オートモード
	 * ver.4.1のレポーティングオプションと同様にfacilityIdと関連付いている監視情報を取得する
	 */
	private HashMap<String, Object> createDataSourceAuto(int num) throws ReportingPropertyNotFound {
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);

		m_log.info("createDataSourceAuto() : facility_id : " + m_facilityId);
		try {
			String csvFileName = getCsvFromDB(dayString, m_facilityId, null);

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
		// プロパティからグラフ生成にかかわる情報を取得
		if(m_propertiesMap.get(MONITOR_ID_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(MONITOR_ID_KEY_VALUE+"."+num + " is not defined.");
		}
		String monitorId = m_propertiesMap.get(MONITOR_ID_KEY_VALUE+"."+num);
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);
		try {
			m_log.info("createDataSourceManual() : facility_id : " + m_facilityId + ", monitor_id : " + monitorId);
			String csvFileName = getCsvFromDB(dayString, m_facilityId, monitorId);

			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);
			m_retMap.put(ReportingConstant.STR_DS+"_"+num, ds);

		} catch (Exception e) {
			m_log.error(e, e);
		}
		return m_retMap;
	}

	/**
	 * 監視結果（収集）情報に関するCSVファイルを作成する
	 * 
	 * @param dayString レポートの実行日時
	 * @param facilityId レポートの設定ファシリティID
	 * @param monitorId Auto：特に指定なし,Manual:プロパティ指定値
	 * @return csvFileName 作成したCSVファイルの名前
	 */
	private String getCsvFromDB(String dayString, String facilityId, String monitorId) {
		String[] columns = { "collectorid", "display_name", "date_time","facilityid", "value"};
		String columnsStr = ReportUtil.joinStrings(columns,  ",");

		int divider = Integer.parseInt(isDefine(m_propertiesMap.get(DIVIDER_KEY_VALUE+"."+m_num), "1"));
		String labelName = m_propertiesMap.get(LABEL_NAME_KEY_VALUE+"."+m_num);
		
		String suffix;
		if (monitorId == null) {
			suffix = isDefine(m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+m_num), "cm");
		} else {
			suffix = monitorId;
		}

		m_log.debug("debug print  facilityId : " + facilityId
				+ ", monitorId : " + (monitorId != null ? monitorId : "AllCloudMonitorType:MON_CLOUD")
				+ ", driver : " + String.valueOf(divider));
		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, m_facilityId + "_" + suffix + "_" + dayString);
		try {
			//監視設定から監視種別をMON_CLOUD%でフィルタリングした設定を取得し、
			//その監視種別で設定されている監視IDと同一の収集IDを持つデータを取得収集が格納されているテーブルから取得する
			//ここでは監視種別を固定で指定している
			ReportingVCloudControllerBean vCloudControllerBean = new ReportingVCloudControllerBean();
			List<MonitorInfo> monitorInfoList = vCloudControllerBean.getMonitorInfoListByMonitorTypeId(monitorId, "MON_CLOUD%");
			
			if (monitorInfoList != null) {
				ReportingCollectControllerBean collectController = new ReportingCollectControllerBean();
				List<Integer> idList = new LinkedList<>();
				Map<Integer, Object[]> itemDataMap = new HashMap<>();
				for (MonitorInfo monitorInfo : monitorInfoList) {
					List<CollectKeyInfo> keyInfoList = collectController.getReportCollectKeyList(monitorInfo.getMonitorId(), facilityId);
					if (keyInfoList == null) {
						m_log.warn("data not found in log.cc_collect_key");
					} else {
						for (CollectKeyInfo keyInfo : keyInfoList) {
							idList.add(keyInfo.getCollectorid());
							Object[] keyObj = new Object[2];
							keyObj[0] = keyInfo;
							keyObj[1] = monitorInfo.getItemName();
							itemDataMap.put(keyInfo.getCollectorid(), keyObj);
						}
					}
					if (labelName == null || labelName.isEmpty()) {
						labelName = monitorInfo.getMeasure();
					}
				}
				List<String[]> resultList = this.getResultList(idList, columns, divider, itemDataMap);

				// write to csv file
				BufferedWriter bw = null;
				File csv = new File(csvFileName);
				try {
					bw = new BufferedWriter(new FileWriter(csv, false));
					bw.write(columnsStr);
					bw.newLine();
					m_log.info("create csv file. " + csvFileName);

					if (!resultList.isEmpty()) {
						for (String[] results : resultList) {
							bw.write(ReportUtil.joinStrings(results, ","));
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
			}
		} catch (Exception e1) {
			m_log.error(e1, e1);
		}
		
		m_retMap.put(LABEL_NAME_KEY_VALUE+"."+m_num,labelName);
		return csvFileName;
	}

	private List<String[]> getResultList(List<Integer> idList, String[] columns, int divider, Map<Integer, Object[]> itemDataMap) {
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
				Object[] keyObj = itemDataMap.get(summaryHour.getCollectorId());
				CollectKeyInfo keyInfo = (CollectKeyInfo) keyObj[0];

				// csvに出力する内容についてほぼテーブルから取得し値を用いる（csvのdisplay_nameに入れる情報はクラウド課金監視の監視項目）
				//"collectorid", "display_name", "date_time", "value","facilityid"
				//"collectorid" :クラウド課金設定の監視ID,
				//"display_name" :監視の収集項目に置き換え（同一グラフ上での識別子）,
				//"date_time" :レポート出力時の日時範囲による時間調整後の時間,
				//"value" :収集してきたデータをそのまま出力する,
				//"facilityid" :レポートの設定値から入力されたデータ
				results[0] = keyInfo.getMonitorId();
				String itemName = HinemosMessage.replace(keyObj[1].toString());
				results[1] = '"' + itemName.replace("\"", "\"\"") + '"';
				Timestamp time = new Timestamp(summaryHour.getTime());
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
				summaryDList = controller.getSummaryDayList(idList, m_startDate.getTime(), m_endDate.getTime());
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryDList == null || summaryDList.isEmpty())
				break;
			for (SummaryDay summaryDay : summaryDList) {
				String[] results = new String[columns.length];
				Object[] keyObj = itemDataMap.get(summaryDay.getCollectorId());
				CollectKeyInfo keyInfo = (CollectKeyInfo) keyObj[0];

				// csvに出力する内容についてほぼテーブルから取得し値を用いる（csvのdisplay_nameに入れる情報はクラウド課金監視の監視項目）
				//"collectorid", "display_name", "date_time", "value","facilityid"
				//"collectorid" :クラウド課金設定の監視ID,
				//"display_name" :監視の収集項目に置き換え（同一グラフ上での識別子）,
				//"date_time" :レポート出力時の日時範囲による時間調整後の時間,
				//"value" :収集してきたデータをそのまま出力する,
				//"facilityid" :レポートの設定値から入力されたデータ
				results[0] = keyInfo.getMonitorId();
				String itemName = HinemosMessage.replace(keyObj[1].toString());
				results[1] = '"' + itemName.replace("\"", "\"\"") + '"';
				Timestamp time = new Timestamp(summaryDay.getTime());
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
				summaryMList = controller.getSummaryMonthList(idList, m_startDate.getTime(), m_endDate.getTime());
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryMList == null || summaryMList.isEmpty())
				break;
			for (SummaryMonth summaryMonth : summaryMList) {
				String[] results = new String[columns.length];
				Object[] keyObj = itemDataMap.get(summaryMonth.getCollectorId());
				CollectKeyInfo keyInfo = (CollectKeyInfo) keyObj[0];

				// csvに出力する内容についてほぼテーブルから取得し値を用いる（csvのdisplay_nameに入れる情報はクラウド課金監視の監視項目）
				//"collectorid", "display_name", "date_time", "value","facilityid"
				//"collectorid" :クラウド課金設定の監視ID,
				//"display_name" :監視の収集項目に置き換え（同一グラフ上での識別子）,
				//"date_time" :レポート出力時の日時範囲による時間調整後の時間,
				//"value" :収集してきたデータをそのまま出力する,
				//"facilityid" :レポートの設定値から入力されたデータ
				results[0] = keyInfo.getMonitorId();
				String itemName = HinemosMessage.replace(keyObj[1].toString());
				results[1] = '"' + itemName.replace("\"", "\"\"") + '"';
				Timestamp time = new Timestamp(summaryMonth.getTime());
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
				summaryList = controller.getCollectDataList(idList, m_startDate.getTime(), m_endDate.getTime());
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryList == null || summaryList.isEmpty())
				break;
			for (CollectData data : summaryList) {
				String[] results = new String[columns.length];
				Object[] keyObj = itemDataMap.get(data.getCollectorId());
				CollectKeyInfo keyInfo = (CollectKeyInfo) keyObj[0];

				// csvに出力する内容についてほぼテーブルから取得し値を用いる（csvのdisplay_nameに入れる情報はクラウド課金監視の監視項目）
				//"collectorid", "display_name", "date_time", "value","facilityid"
				//"collectorid" :クラウド課金設定の監視ID,
				//"display_name" :監視の収集項目に置き換え（同一グラフ上での識別子）,
				//"date_time" :レポート出力時の日時範囲による時間調整後の時間,
				//"value" :収集してきたデータをそのまま出力する,
				//"facilityid" :レポートの設定値から入力されたデータ
				results[0] = keyInfo.getMonitorId();
				String itemName = HinemosMessage.replace(keyObj[1].toString());
				results[1] = '"' + itemName.replace("\"", "\"\"") + '"';
				Timestamp time = new Timestamp(data.getTime());
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
