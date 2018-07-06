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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingMonitorControllerBean;
import com.clustercontrol.util.HinemosMessage;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * 監視情報のグラフの作成元となるイベント情報を日別の重要度別でまとめたデータソースを作成するクラス
 */
public class DatasourceMonitorDetail extends DatasourceBase {

	private static Log m_log = LogFactory.getLog(DatasourceMonitorDetail.class);

	
	/**
	 * データソース（CSVファイル）をHinemos DBから生成する
	 */
	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {
		
		if(m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(SUFFIX_KEY_VALUE+"."+num + " is not defined.");
		}
		String suffix = m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+num);
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);
		
		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, m_facilityId + "_" + suffix + "_" + dayString);
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		String[] columns = { "monitor_id", "priority", "generation_date", "output_date", "plugin_id", 
				"message", "application", "monitor_detail_id", "scope_text", "owner_role_id",
				"comment_date", "comment_user", "comment"};
		String columnsStr = ReportUtil.joinStrings(columns, ",");
		
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

				ReportingMonitorControllerBean controller = new ReportingMonitorControllerBean();
				List<EventLogEntity> eventlogList = controller.getMonitorDetailList(m_facilityId, m_startDate.getTime(), m_endDate.getTime(), ownerRoleId);
				
				Map<Integer, String> priorityMap = new HashMap<>();
				priorityMap.put(PriorityConstant.TYPE_CRITICAL, ReportUtil.STRING_CRITICAL);
				priorityMap.put(PriorityConstant.TYPE_WARNING, ReportUtil.STRING_WARNING);
				priorityMap.put(PriorityConstant.TYPE_INFO, ReportUtil.STRING_INFO);
				priorityMap.put(PriorityConstant.TYPE_UNKNOWN, ReportUtil.STRING_UNKNOWN);
				
				// write to csv file
				int max_length = Integer.parseInt(isDefine("max.message.length", "65"));
				if (eventlogList != null) {
					String[] results = new String[columns.length];
					for (EventLogEntity entity : eventlogList) {
						String value = priorityMap.get(entity.getPriority());

						results[0] = entity.getId().getMonitorId();
						results[1] = value.toString();
						results[2] = new Timestamp(entity.getGenerationDate()).toString();
						results[3] = new Timestamp(entity.getId().getOutputDate()).toString();
						results[4] = entity.getId().getPluginId();
						String message = HinemosMessage.replace(entity.getMessage());
						message = (message.length() <= max_length ? message : message.substring(0, max_length) + "...");
						results[5] = '"' + message.replace("\"", "\"\"") + '"';
						results[6] = entity.getApplication();
						results[7] = entity.getId().getMonitorDetailId();
						results[8] = entity.getScopeText();
						results[9] = entity.getOwnerRoleId();
						results[10] = entity.getCommentDate() != null ? entity.getCommentDate().toString() : null;
						results[11] = entity.getCommentUser();
						results[12] = entity.getComment();

						bw.write(ReportUtil.joinStrings(results, ","));
						bw.newLine();
					}
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
