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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingMonitorControllerBean;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * 監視情報のグラフの作成元となるイベント情報を重要度別でまとめたデータソースを作成するクラス
 */
public class DatasourceMonitorPriorityTotal extends DatasourceBase {

	private static Log m_log = LogFactory.getLog(DatasourceMonitorPriorityTotal.class);
	
	/**
	 * データソース（CSVファイル）をHinemos DBから生成する
	 */
	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {
		
		if(m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(SUFFIX_KEY_VALUE+"."+num + " is not defined.");
		}
		String suffix = m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+num);
		String dayString = new SimpleDateFormat("yyyyMMdd").format(m_startDate);
		
		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, m_facilityId + "_" + suffix + "_" + dayString);
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		String[] columns = { "priority", "count"};
		String columnsStr = ReportUtil.joinStringsToCsv(columns);
		
		// get data from Hinemos DB
		try {
			// CSVファイルが既に存在している場合は、データ作成処理をスキップ
			if(new File(csvFileName).exists()){
				m_log.info("File : " + csvFileName + " is exists.");
			} else {
				m_log.info("output csv: " + csvFileName);
	
				File csv = new File(csvFileName);
				BufferedWriter bw = new BufferedWriter(new FileWriter(csv, false));
				bw.write(columnsStr);
				bw.newLine();
	
				String ownerRoleId = null;
				if (!"ADMINISTRATORS".equals(ReportUtil.getOwnerRoleId())) {
					ownerRoleId = ReportUtil.getOwnerRoleId();
				}
				
				// 重要度別の集計をする。
				ReportingMonitorControllerBean controller = new ReportingMonitorControllerBean();
				List<Object[]> nums = controller.getMonitorPriorityTotalList(m_facilityId, m_startDate.getTime(), m_endDate.getTime(), ownerRoleId);
				
				// write to csv file
				String[] results = new String[columns.length];
				for (Object[] object : nums) {
					results[0] = object[0].toString();
					results[1] = object[1].toString();
					bw.write(ReportUtil.joinStringsToCsv(results));
					bw.newLine();
				}
				bw.close();
			}
			
			// 生成されたcsvファイルをもとにDatasourceを生成
			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);
			
			retMap.put(ReportingConstant.STR_DS+"_"+num, ds);
		} catch (IOException | JRException e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		}
		
		return retMap;
	}
}
