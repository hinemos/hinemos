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
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingJobControllerBean;
import com.clustercontrol.util.Messages;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRCsvDataSource;


/**
 * ジョブセッション情報のレポート作成元となるデータソースを作成するクラス
 */
public class DatasourceJobSession extends DatasourceBase {

	private static Log m_log = LogFactory.getLog(DatasourceJobSession.class);
	
	private static final String JOB_UNIT_REGEX = "job.unit.id";
	private static final String JOB_ID_REGEX = "job.id";
	private static final String JOB_ID_REGEX_EXC = "job.id.exc";
	
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
		
		String jobUnitRegex = isDefine(JOB_UNIT_REGEX+"."+num, "%%");		
		String jobIdRegex = isDefine(JOB_ID_REGEX+"."+num, "%%");
		String jobIdRegexExc = isDefine(JOB_ID_REGEX_EXC+"."+num, "");
		
		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, suffix + "_" + dayString);
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		String[] columns = { "session_id", "job_id", "schedule_date", "end_date", "trigger_type", "trigger_info", "end_status", "elapsed_time", "job_label" };
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
				int maxLength = Integer.parseInt(isDefine("max.jobid.length", "65"));

				ReportingJobControllerBean controller = new ReportingJobControllerBean();
				List<JobSessionEntity> jobSessionList = controller.getReportingJobSessionList(jobUnitRegex, jobIdRegex, jobIdRegexExc, "_ROOT_", ownerRoleId, m_startDate.getTime(), m_endDate.getTime());
				if (jobSessionList != null) {
					for (JobSessionEntity entity : jobSessionList) {
						String sessionId = entity.getSessionId();
						String jobunitId = entity.getJobunitId();
						String jobId = entity.getJobId();
						JobInfoEntity jobInfo = controller.getJobInfo(sessionId, jobunitId, jobId);
						String jobName = jobInfo != null ? jobInfo.getJobName() : null;
						Timestamp scheduleDate = new Timestamp(entity.getScheduleDate());
						List<JobSessionJobEntity> jobSessionJob = entity.getJobSessionJobEntities();
						Timestamp endDate = null;
						if (jobSessionJob != null && !jobSessionJob.isEmpty()) {
							endDate = jobSessionJob.get(0).getEndDate() != null ? new Timestamp(jobSessionJob.get(0).getEndDate()) : null;
						}
						int triggerType = entity.getTriggerType();
						String triggerInfo = entity.getTriggerInfo();
						triggerInfo = '"' + triggerInfo.replace("\"", "\"\"") + '"';
						Integer endStatus = jobSessionJob != null ? jobSessionJob.get(0).getEndStatus() : null;
						String endStatusStr = endStatus == null ? "" : ReportUtil.getEndStatusString(endStatus);
						Long elapsedDate = endDate != null ? TimeUnit.MILLISECONDS.toSeconds(jobSessionJob.get(0).getEndDate()) - TimeUnit.MILLISECONDS.toSeconds(jobSessionJob.get(0).getStartDate()) : null;
						String elapsedTime = millsecToTime(elapsedDate);
						String jobLabel = jobId + " (" + jobName + ")";
						jobLabel = (jobLabel.length() <= maxLength ? jobLabel :
							jobLabel.substring(0, maxLength) + "...");
						jobLabel = '"' + jobLabel.replace("\"", "\"\"") + '"';
	
						bw.write(sessionId + "," + jobId + "," + scheduleDate + "," + (endDate == null ? "" : endDate) + ","
								 + triggerType + "," + triggerInfo + "," + endStatusStr + "," + elapsedTime
								 + "," + jobLabel);
						bw.newLine();
						
					}
				}
				bw.close();
				
				// 生成されたcsvファイルをもとにDatasourceを生成
				JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
				ds.setUseFirstRowAsHeader(true);
				
				retMap.put(ReportingConstant.STR_DS+"_"+num, ds);
			}
		} catch (IOException | JRException e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		}
		
		return retMap;
	}
	
	private String millsecToTime(Long millsec) {
		if (millsec == null || millsec <= 0L) {
			return Messages.getString("");
		}

		long sessionTime = millsec;
		long oneSeconds = 1L;
		long oneMinutes = 60L;
		long oneHours = 60L;

		long basicSeconds = sessionTime/oneSeconds;
		long sessionMinutes = basicSeconds / oneMinutes;
		long sessionSeconds = basicSeconds%oneMinutes;
		long sessionHours = sessionMinutes / oneHours;
		long sessionMinutesRest = sessionMinutes%oneHours;
		String diffTime = String.format("%1$02d", sessionHours) + ":" + String.format("%1$02d", sessionMinutesRest) + ":" + String.format("%1$02d", sessionSeconds);

		return diffTime;
	}
	
}
