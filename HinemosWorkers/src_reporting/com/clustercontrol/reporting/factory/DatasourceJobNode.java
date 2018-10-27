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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingJobControllerBean;
import com.clustercontrol.util.Messages;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRCsvDataSource;


/**
 * ジョブ-ノード詳細情報のレポート作成元となるデータソースを作成するクラス
 */
public class DatasourceJobNode extends DatasourceBase {

	private static Log m_log = LogFactory.getLog(DatasourceJobNode.class);

	private static final String JOB_UNIT_REGEX = "job.unit.id";
	private static final String JOB_ID_REGEX = "job.id";
	private static final String JOB_ID_REGEX_EXC= "job.id.exc";
	
	/**
	 * データソース（CSVファイル）をHinemos DBから生成する
	 * 1日単位の情報を生成する
	 */
	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {
		
		if(m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+num).isEmpty()) {
			throw new ReportingPropertyNotFound(SUFFIX_KEY_VALUE+"."+num + " is not defined.");
		}
		String suffix = m_propertiesMap.get(SUFFIX_KEY_VALUE+"."+num);		
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);
		
		String jobUnitRegex = isDefine(JOB_UNIT_REGEX+"."+num, "%%");		
		String jobIdRegex = isDefine(JOB_ID_REGEX+"."+num, "%%");
		String jobIdRegexExc = isDefine(JOB_ID_REGEX_EXC+"."+num, "");
		
		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, m_facilityId + "_" + suffix + "_" + dayString);
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		String[] columns = { 
				"session_id", "jobunit_id", "job_id", "status",
				"start_date", "end_date", "end_value", "job_series", "end_status",
				"status_str", "end_status_str", "jobunit_label", "job_label", "message" };
		String columnsStr = ReportUtil.joinStrings(columns, ",");

		int rows = 0;

		// get data from Hinemos DB
		try {
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
				int daySec = 1000*24*60*60;

				int maxLength = Integer.parseInt(isDefine("max.jobid.length", "65"));
				
				ReportingJobControllerBean controller = new ReportingJobControllerBean();
				List<JobSessionNodeEntity> jobSessionNodeList = controller.getReportingJobSessionNodeList(
						m_facilityId, jobUnitRegex, jobIdRegex, jobIdRegexExc, ownerRoleId, m_startDate.getTime(), m_startDate.getTime() + daySec);

				if (jobSessionNodeList != null) {
					for (JobSessionNodeEntity entity : jobSessionNodeList) {
						String sessionId = entity.getId().getSessionId();
						String jobunitId = entity.getId().getJobunitId();
						String jobId = entity.getId().getJobId();
						int status = entity.getStatus();
						String statusStr = Messages.getString(ReportUtil.getStatusString(status));
						Timestamp startDate = null;
						if (entity.getStartDate() != null) {
							startDate = new Timestamp(entity.getStartDate());
							startDate.setNanos(0);
						}
						Timestamp endDate = null;
						if (entity.getEndDate() != null) {
							endDate = new Timestamp(entity.getEndDate());
							endDate.setNanos(0);
						}
						int endValue = 0;
						if (entity.getEndValue() != null) {
							endValue = entity.getEndValue();
						}
						JobSessionJobEntity jobSessionJob = entity.getJobSessionJobEntity();
						int endStatus = 0;
						String endStatusStr = "";
						String rootjobId = null;
						String rootjob_name = null;
						String jobName = null;
						if (jobSessionJob != null) {
							if (jobSessionJob.getEndStatus() != null) {
								endStatus = jobSessionJob.getEndStatus();
								endStatusStr = ReportUtil.getEndStatusString(endStatus);
							}
							JobSessionEntity jobSession = jobSessionJob.getJobSessionEntity();
							if (jobSession != null) {
								rootjobId = jobSession.getJobId();
								String rootJobSessionId = jobSession.getSessionId();
								String rootJobunitId = jobSession.getJobunitId();
								JobInfoEntity rootJobInfo = controller.getJobInfo(rootJobSessionId, rootJobunitId, rootjobId);
								rootjob_name = rootJobInfo.getJobName();
							}
							if (jobSessionJob.getJobInfoEntity() != null) {
								jobName = jobSessionJob.getJobInfoEntity().getJobName();
							}
						}
						String message = entity.getMessage();
						
						String rootJobLabel = rootjobId + " (" + rootjob_name + ")";
						rootJobLabel = (rootJobLabel.length() <= maxLength ? rootJobLabel :
							rootJobLabel.substring(0, maxLength) + "...");
						rootJobLabel = '"' + rootJobLabel.replace("\"", "\"\"") + '"';
						String jobLabel = jobId + " (" + jobName + ")";
						jobLabel = (jobLabel.length() <= maxLength ? jobLabel :
							jobLabel.substring(0, maxLength) + "...");
						jobLabel = '"' + jobLabel.replace("\"", "\"\"") + '"';
						
						message = (message.length() <= maxLength ? message :
							message.substring(0, maxLength) + "...");
						// 改行コードをスペースに置換
						message = message.replaceAll("\r\n", "  ");
						// 「"」を「""」に置換し、ダブルクォーテーションで囲う
						message = '"' + message.replace("\"", "\"\"") + '"';
						
						bw.write(sessionId + "," + jobunitId + "," + jobId + "," + status + ","
								 + startDate + "," + (endDate == null ? "" : endDate) + ","
								 + endValue + "," + "job1" + "," + endStatus + "," + statusStr + "," + endStatusStr + ","
								 + rootJobLabel + "," + jobLabel + "," + message);
						bw.newLine();
						rows++;
					}
				}
				bw.close();
				
				JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
				ds.setUseFirstRowAsHeader(true);
				
				retMap.put(ReportingConstant.STR_DS+"_"+num, ds);
			}
		} catch (IOException | JRException e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		}

		if (rows == 0) {
			return null;
		}

		return retMap;
	}
}
