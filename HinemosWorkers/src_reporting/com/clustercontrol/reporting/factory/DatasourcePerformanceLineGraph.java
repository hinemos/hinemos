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
import java.util.LinkedHashMap;
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
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingCollectControllerBean;
import com.clustercontrol.reporting.session.ReportingMonitorControllerBean;
import com.clustercontrol.util.HinemosMessage;

import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * 性能情報のグラフの作成元となる性能値のデータソースを作成するクラス
 */
public class DatasourcePerformanceLineGraph extends DatasourceBase{

	private static Log m_log = LogFactory.getLog(DatasourcePerformanceLineGraph.class);
	
	private static final String ITEM_FILTER_KEY_VALUE = "item.filter";
	private static final String ITEM_CODE_KEY_VALUE = "item.codes";
	private static final String DIVIDER_KEY_VALUE = "divider";
	private static final String DEVICE_FLG_KEY_VALUE = "device.flg";
	
	private Map<List<String>, OptimalMonitorIdInfo> m_monitorIdMap = null;
	
	private static class OptimalMonitorIdInfo {
		String monitorId;
		String itemCode;
		String displayName;
		int runInterval;
		boolean breakdownFlg;
	}
	
	/**
	 * データソース（CSVファイル）をHinemos DBから生成する
	 */
	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {
		
		if(m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(SUFFIX_KEY_VALUE+"."+num + " is not defined.");
		}
		String suffix = m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+num);
		
		if(m_propertiesMap.get(ITEM_FILTER_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(ITEM_FILTER_KEY_VALUE+"."+num + " is not defined.");
		}
		String itemFilter = m_propertiesMap.get(ITEM_FILTER_KEY_VALUE+"."+num);
		
		if(m_propertiesMap.get(ITEM_CODE_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(ITEM_CODE_KEY_VALUE+"."+num + " is not defined.");
		}
		String itemCodes = m_propertiesMap.get(ITEM_CODE_KEY_VALUE+"."+num);
		
		if(m_propertiesMap.get(DIVIDER_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(DIVIDER_KEY_VALUE+"."+num + " is not defined.");
		}
		int divider = Integer.parseInt(m_propertiesMap.get(DIVIDER_KEY_VALUE+"."+num));
		
		if(m_propertiesMap.get(DEVICE_FLG_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(DEVICE_FLG_KEY_VALUE+"."+num + " is not defined.");
		}
		boolean deviceFlg = Boolean.parseBoolean(m_propertiesMap.get(DEVICE_FLG_KEY_VALUE+"."+num));
		
		m_monitorIdMap = getOptimalMonitorId(m_facilityId, itemFilter);
		String[] items = itemCodes.split(",");
		
		String[] columns = { "collectorid", "item_code", "item_name", "display_name", "date_time", "facilityid", "value" };
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);
		
		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, m_facilityId + "_" + suffix + "_" + dayString);
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		// CSVファイルが既に存在している場合は、データ作成処理をスキップ
		if(new File(csvFileName).exists()){
			m_log.info("File : " + csvFileName + " is exists.");
		} else {
			// 「デバイス別」のグラフに関するデータ
			if(deviceFlg) {
				getCsvFromDBDevice(csvFileName, m_facilityId, items, columns, divider);
			} 
			// 「デバイス別」ではないグラフに関するデータ
			else {
				getCsvFromDB(csvFileName, m_facilityId, items, columns, divider);
			}
		}
			
		try {
			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);
			
			retMap.put(ReportingConstant.STR_DS+"_"+num, ds);
			
		} catch (Exception e) {
			m_log.error(e, e);
		}

		return retMap;
	}

	/**
	 * 「デバイス別」のリソース情報に関するCSVファイルを作成する
	 */
	private String getCsvFromDBDevice(String csvFileName, String facilityId, String[] items, String[] columns, int divider) {
		String columnsStr = ReportUtil.joinStrings(columns,  ",");
		m_log.info("output csv: " + csvFileName);

		// get data from Hinemos DB
		BufferedWriter bw = null;
		try {
			File csv = new File(csvFileName);
			bw = new BufferedWriter(new FileWriter(csv, false));
			bw.write(columnsStr);
			bw.newLine();
			
			List<String[]> resultList = new ArrayList<String[]>();
			
			for (Map.Entry<List<String>, OptimalMonitorIdInfo> entry : m_monitorIdMap.entrySet()) {
				for (String code : items) {
					// キーのひとつであるitemCodeが対象とマッチする場合
					if (entry.getKey().get(0).equals(code)) {
						if (entry.getValue() != null) {
							
							// Hinemos DB内の情報を基に出力値を算出する
							// 監視項目ID、収集項目、デバイス名ごとに算出情報を取得する
							m_log.debug("display name = " + entry.getValue().displayName);
							
							resultList.addAll(this.getResultList(facilityId, entry.getValue().monitorId, entry.getValue().displayName, entry.getValue().itemCode, columns, divider));
						}
					}
				}
			}
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy/M/d HH:mm:ss");
			m_log.debug("start date : " + fmt.format(m_startDate));
			m_log.debug("end date : " + fmt.format(m_endDate));
			
			// 出力値をCSVファイルに書き込む
			if (!resultList.isEmpty()) {
				// write to csv file
				for (String[] results : resultList) {
					bw.write(ReportUtil.joinStrings(results, ","));
					bw.newLine();
				}
			}
		} catch (IOException | InvalidRole | HinemosUnknown e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					m_log.warn("bw is not close.", e);
				}
			}
		}

		return csvFileName;
	}
	
	/**
	 * 「デバイス別」以外のリソース情報に関するCSVファイルを作成する
	 * 
	 * @param csvFileName
	 * @param facilityId
	 * @param items
	 * @param columns
	 * @param divider
	 * @return
	 */
	private String getCsvFromDB(String csvFileName, String facilityId, String[] items, String[] columns, int divider) {
		String columnsStr = ReportUtil.joinStrings(columns,  ",");
		m_log.info("output csv: " + csvFileName);

		// get data from Hinemos DB
		try {

			File csv = new File(csvFileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(csv, false));
			bw.write(columnsStr);
			bw.newLine();
			
			// 監視項目ID、収集項目のセットごとに算出情報を確認する
			List<String> keys = null;
			
			List<String[]> resultList = new ArrayList<String[]>();
			for (String code : items) {
				keys = new ArrayList<String>();
				keys.add(code);
				keys.add("");
				OptimalMonitorIdInfo info = m_monitorIdMap.get(keys);
				
				if (info != null) {
					// Hinemos DB内の情報を基に出力値を算出する
					// 監視項目ID、収集項目、デバイス名ごとに算出情報を取得する
					resultList.addAll(this.getResultList(facilityId, info.monitorId, null, info.itemCode, columns, divider));
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
		} catch (Exception e) {
			m_log.error(e, e);
		}

		return csvFileName;
	}

	/**
	 * 収集データの中から、レポート出力に使用する監視項目を収集項目（さらにいえばデバイス）単位で選定する。
	 * キー：item_code, display_name
	 * 
	 * @param facilityId
	 * @param itemFilter
	 * @return 
	 */
	private Map<List<String>, OptimalMonitorIdInfo> getOptimalMonitorId(String facilityId, String itemFilter) {
		Map<List<String>, OptimalMonitorIdInfo> map = new LinkedHashMap<>();
		m_log.info("getting optimal monitor_id for each item_code in " + facilityId);
		
		try {
			String ownerRoleId = null;
			if (!"ADMINISTRATORS".equals(ReportUtil.getOwnerRoleId())) {
				ownerRoleId = ReportUtil.getOwnerRoleId();
			}
			
			// レポート出力に使用する監視項目を収集項目を取得する。
			ReportingMonitorControllerBean controller = new ReportingMonitorControllerBean();
			List<Object[]> monitorInfoList = controller.getMonitorList(itemFilter, facilityId, m_startDate.getTime(), m_endDate.getTime(), ownerRoleId);
			
			if (monitorInfoList != null) {
				OptimalMonitorIdInfo info = null;
				OptimalMonitorIdInfo checkInfo = null;
				for (Object[] monitorInfo : monitorInfoList) {
					// データが存在しない場合は、次のデータを処理する。
					info = new OptimalMonitorIdInfo();
					info.monitorId = monitorInfo[0].toString();
					info.itemCode = monitorInfo[1].toString();
					if (monitorInfo[2] instanceof Integer) {
						info.runInterval = ((Integer)monitorInfo[2]).intValue();
					} else if (monitorInfo[2] instanceof Short) {
						info.runInterval = ((Short)monitorInfo[2]).intValue();
					}
					info.displayName = monitorInfo[3].toString();
					
					// 以降、既にあるキーセット（収集項目/デバイス名）との比較
					List<String> keys = new ArrayList<String>();
					keys.add(info.itemCode);
					keys.add(info.displayName);
					
					// 既存データの取得
					checkInfo = map.get(keys);
					
					// 既存データが存在する場合は、収集間隔が短い方で上書きする
					if (checkInfo != null) {
						if(checkInfo.runInterval > info.runInterval) {
							map.put(keys, info);
							m_log.debug("update put " + info.monitorId + ", " + info.itemCode + ", " + info.displayName + "," + info.runInterval);
						}
					}
					// 存在しない場合は、新規に追加
					else {
						map.put(keys, info);
						m_log.debug("new    put " + info.monitorId + ", " + info.itemCode + ", " + info.displayName + "," + info.runInterval);
					}
					
					// 内訳の収集を行っているかどうかをチェックする。
					info.breakdownFlg = (boolean)monitorInfo[4];
					if (info.breakdownFlg) {
						// 内訳のItemCodeを取得する。
						ReportingCollectControllerBean collectContoller = new ReportingCollectControllerBean();
						List<Object> items = collectContoller.getCollectItemCodes(info.monitorId);
						if (items != null) {
							for (Object item : items) {
								OptimalMonitorIdInfo breakdowninfo = null;
								OptimalMonitorIdInfo breakdowncheckInfo = null;
								breakdowninfo = new OptimalMonitorIdInfo();
								
								breakdowninfo.monitorId = monitorInfo[0].toString();
								breakdowninfo.itemCode = item.toString();
	
								if (monitorInfo[2] instanceof Integer) {
									breakdowninfo.runInterval = ((Integer)monitorInfo[2]).intValue();
								} else if (monitorInfo[2] instanceof Short) {
									breakdowninfo.runInterval = ((Short)monitorInfo[2]).intValue();
								}
								breakdowninfo.displayName = monitorInfo[3].toString();
								
								// 以降、既にあるキーセット（監視項目ID/収集項目/デバイス名）との比較
								List<String> breakDownkeys = new ArrayList<String>();
								breakDownkeys.add(item.toString());
								breakDownkeys.add(breakdowninfo.displayName);
								// 既存データの取得
								breakdowncheckInfo = map.get(breakDownkeys);
								
								// 既存データが存在する場合は、収集間隔が短い方で上書きする
								if (breakdowncheckInfo != null) {
									if(breakdowncheckInfo.runInterval > breakdowninfo.runInterval) {
										map.put(breakDownkeys, breakdowninfo);
										m_log.debug("update put " + breakdowninfo.monitorId + ", " + breakdowninfo.itemCode
												+ ", " + breakdowninfo.displayName + "," + breakdowninfo.runInterval);
									}
								}
								// 存在しない場合は、新規に追加
								else {
									map.put(breakDownkeys, breakdowninfo);
									m_log.debug("new    put " + breakdowninfo.monitorId + ", " + breakdowninfo.itemCode + ", "
											+ breakdowninfo.displayName + "," + breakdowninfo.runInterval);
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
	 * @param facilityId
	 * @param monitorId
	 * @param displayName
	 * @param itemCode
	 * @param columns
	 * @param divider
	 * @return list
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private List<String[]> getResultList(String facilityId, String monitorId, String displayName, String itemCode, String[] columns, int divider) throws InvalidRole, HinemosUnknown {
		List<String[]> resultList = new ArrayList<String[]>();
		
		ReportingCollectControllerBean controller = new ReportingCollectControllerBean();
		// サマリデータ、または収集データ(raw)のタイプでスイッチ
		int summaryType = ReportUtil.getSummaryType(m_startDate.getTime(), m_endDate.getTime());
		switch (summaryType) {
		case SummaryTypeConstant.TYPE_AVG_HOUR:
			List<Object[]> summaryHList = null;
			try {
				summaryHList = controller.getSummaryHourList(facilityId, m_startDate.getTime(), m_endDate.getTime(), monitorId, displayName, itemCode);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryHList == null || summaryHList.isEmpty())
				break;
			for (Object[] summaryH : summaryHList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = (CollectKeyInfo)summaryH[0];
				SummaryHour summaryHour = (SummaryHour)summaryH[1];
				results[0] = keyInfo.getMonitorId().toString();
				results[1] = summaryH[2].toString();
				results[2] = HinemosMessage.replace(keyInfo.getItemName());
				results[3] = keyInfo.getDisplayName();
				Timestamp time = new Timestamp(summaryHour.getTime());
				results[4] = time.toString();
				results[5] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryHour.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[6] = Double.toString(value / divider);
					} else {
						results[6] = value.toString();
					}
					resultList.add(results);
				}
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_DAY:
			List<Object[]> summaryDList = null;
			try {
				summaryDList = controller.getSummaryDayList(facilityId, m_startDate.getTime(), m_endDate.getTime(), monitorId, displayName, itemCode);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryDList == null || summaryDList.isEmpty())
				break;
			for (Object[] summaryD : summaryDList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = (CollectKeyInfo)summaryD[0];
				SummaryDay summaryDay = (SummaryDay)summaryD[1];
				results[0] = keyInfo.getMonitorId().toString();
				results[1] = summaryD[2].toString();
				results[2] = HinemosMessage.replace(keyInfo.getItemName());
				results[3] = keyInfo.getDisplayName();
				Timestamp time = new Timestamp(summaryDay.getTime());
				results[4] = time.toString();
				results[5] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryDay.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[6] = Double.toString(value / divider);
					} else {
						results[6] = value.toString();
					}
					resultList.add(results);
				}
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_MONTH:
			List<Object[]> summaryMList = null;
			try {
				summaryMList = controller.getSummaryMonthList(facilityId, m_startDate.getTime(), m_endDate.getTime(), monitorId, displayName, itemCode);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryMList == null || summaryMList.isEmpty())
				break;
			for (Object[] summaryM : summaryMList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = (CollectKeyInfo)summaryM[0];
				SummaryMonth summaryDay = (SummaryMonth)summaryM[1];
				results[0] = keyInfo.getMonitorId().toString();
				results[1] = summaryM[2].toString();
				results[2] = HinemosMessage.replace(keyInfo.getItemName());
				results[3] = keyInfo.getDisplayName();
				Timestamp time = new Timestamp(summaryDay.getTime());
				results[4] = time.toString();
				results[5] = keyInfo.getFacilityid();
				Double value = Double.valueOf(summaryDay.getAvg());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[6] = Double.toString(value / divider);
					} else {
						results[6] = value.toString();
					}
					resultList.add(results);
				}
			}
			break;
		default: // defaultはRAWとする
			List<Object[]> summaryList = null;
			try {
				summaryList = controller.getCollectDataList(facilityId, m_startDate.getTime(), m_endDate.getTime(), monitorId, displayName, itemCode);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getResultList() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryList == null || summaryList.isEmpty())
				break;
			for (Object[] data : summaryList) {
				String[] results = new String[columns.length];
				CollectKeyInfo keyInfo = (CollectKeyInfo)data[0];
				CollectData rawData = (CollectData)data[1];
				results[0] = keyInfo.getMonitorId().toString();
				results[1] = data[2].toString();
				results[2] = HinemosMessage.replace(keyInfo.getItemName());
				results[3] = keyInfo.getDisplayName();
				Timestamp time = new Timestamp(rawData.getTime());
				results[4] = time.toString();
				results[5] = keyInfo.getFacilityid();
				Double value = Double.valueOf(rawData.getValue());
				if (!value.isNaN()) {
					if (divider > 1) {
						results[6] = Double.toString(value / divider);
					} else {
						results[6] = value.toString();
					}
					resultList.add(results);
				}
			}
			break;
		}
		return resultList;
	}
}
