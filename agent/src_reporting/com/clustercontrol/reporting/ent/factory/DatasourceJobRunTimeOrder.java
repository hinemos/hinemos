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
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.ent.session.ReportingJobEntControllerBean;
import com.clustercontrol.reporting.ent.util.PropertiesConstant;
import com.clustercontrol.reporting.factory.DatasourceBase;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.util.Messages;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRCsvDataSource;


/**
 * コマンドジョブ情報の実行時間上位一覧レポート作成元となるデータソースを作成するクラス
 * 
 * @version 5.0.b
 * @since 5.0.b
 */
public class DatasourceJobRunTimeOrder extends DatasourceBase {
	private static Log m_log = LogFactory.getLog(DatasourceJobRunTimeOrder.class);

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
		
		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, suffix + "_" + dayString);
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		int orderNum = Integer.parseInt(isDefine(PropertiesConstant.JOB_ORDER_NUM_KEY, PropertiesConstant.JOB_ORDER_NUM_DEFAULT));
		String jobUnitRegex = isDefine(PropertiesConstant.JOB_UNIT_REGEX_KEY_+"."+num, PropertiesConstant.REGEX_DEFAULT);
		String jobIdRegex = isDefine(PropertiesConstant.JOB_ID_REGEX_KEY_+"."+num, PropertiesConstant.REGEX_DEFAULT);
		String jobIdRegexExc = isDefine(PropertiesConstant.JOB_ID_REGEX_EXC_KEY_+"."+num, "");
		
		String jobOrderKey = isDefine(PropertiesConstant.JOB_ORDER_KEY_KEY_+"."+num, PropertiesConstant.ORDER_KEY_MAX);
		// 設定値がどの値にも当てはまらない場合は、maxをデフォルトとする
		if( !(jobOrderKey.equals(PropertiesConstant.ORDER_KEY_MAX) || jobOrderKey.equals(PropertiesConstant.ORDER_KEY_AVG) || jobOrderKey.equals(PropertiesConstant.ORDER_KEY_DIFF)) ) {
			jobOrderKey = PropertiesConstant.ORDER_KEY_MAX;
		}

		String[] columns = { "session_id", "job_id", "start_date", "end_date", "elapsed_time_max", "elapsed_time_avg", "elapsed_time_diff", "job_label" };

		String columnsStr = ReportUtil.joinStrings(columns, ",");
		
		// get data from Hinemos DB
		BufferedWriter bw = null;
		try {
			// CSVファイルが既に存在している場合は、データ作成処理をスキップ
			if(new File(csvFileName).exists()){
				m_log.info("File : " + csvFileName + " is exists.");
			} else {
				m_log.info("output csv: " + csvFileName);
				
				File csv = new File(csvFileName);
				bw = new BufferedWriter(new FileWriter(csv, false));
				bw.write(columnsStr);
				bw.newLine();
				
				String ownerRoleId = null;
				if (!"ADMINISTRATORS".equals(ReportUtil.getOwnerRoleId())) {
					ownerRoleId = ReportUtil.getOwnerRoleId();
				}
				
				int maxLength = Integer.parseInt(isDefine("max.jobid.length", "65"));
				
				ReportingJobEntControllerBean controller = new ReportingJobEntControllerBean();
				List<Object[]> summaryJobSessionJobList = controller.getSummaryJobSessionJob(m_startDate.getTime(), m_endDate.getTime(), jobUnitRegex, jobIdRegex, jobIdRegexExc, jobOrderKey, ownerRoleId, orderNum);
				
				if (summaryJobSessionJobList != null) {
					for (Object[] objects : summaryJobSessionJobList) {
						String sessionId = "";
						String jobunitId = objects[0].toString();
						String jobId = objects[1].toString();
						Long maxTime = (Long)objects[2];
						Timestamp startDate = null;
						Timestamp endDate = null;
						List<JobSessionJobEntity> jobsessionJobs = controller.getJobSessionJobEntityByMaxTime(maxTime, jobunitId, jobId);
						for (JobSessionJobEntity jobsessionJob : jobsessionJobs) {
							if (jobsessionJob != null) {
								sessionId = jobsessionJob.getId().getSessionId();
								startDate = new Timestamp(jobsessionJob.getStartDate());
								endDate =  new Timestamp(jobsessionJob.getEndDate());
							}
							
							// TODO: 実行時間、平均時間、差分の値の出し方は、これでいいのか、要確認。
							String elapsedTimeMax = millsecToTime((Long)objects[2] / 1000);
							String elapsedTimeAvg = millsecToTime(((Double)objects[3]).longValue() / 1000);
							String elapsedTimeDiff = millsecToTime(((Double)objects[4]).longValue() / 1000);
							
							if (elapsedTimeDiff.equals("")) {
								elapsedTimeDiff = "0";
							}
							
							String jobName = "";
							
							JobInfoEntity jobInfo = controller.getJobInfoEntityPK(sessionId, jobunitId, jobId);
							
							if (jobInfo != null) {
								jobName = jobInfo.getJobName();
							}
													
							String jobLabel = jobId + " (" + jobName + ")";
							jobLabel = (jobLabel.length() <= maxLength ? jobLabel :
								jobLabel.substring(0, maxLength) + "...");
							jobLabel = '"' + jobLabel.replace("\"", "\"\"") + '"';
		
							bw.write(sessionId + "," + jobId + "," + startDate + "," + endDate + ","
									 + elapsedTimeMax + "," + elapsedTimeAvg + "," + elapsedTimeDiff + ","
									 + jobLabel);
							bw.newLine();
						}
					}
					
					// 生成されたcsvファイルをもとにDatasourceを生成
					JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
					ds.setUseFirstRowAsHeader(true);
					
					retMap.put(ReportingConstant.STR_DS+"_"+num, ds);
				}
			}
		} catch (IOException | JRException | JobInfoNotFound e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					m_log.warn("bw close failure : ", e);
				}
			}
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
