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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
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
 * ジョブ詳細情報のレポート作成元となるデータソースを作成するクラス
 */
public class DatasourceJobDetail extends DatasourceBase {

	private static Log m_log = LogFactory.getLog(DatasourceJobDetail.class);

	protected static final String JOB_UNIT_REGEX = "job.unit.id";
	protected static final String JOB_ID_REGEX = "job.id";
	protected static final String JOB_ID_REGEX_EXC = "job.id.exc";

	protected int m_daySec = 1000*24*60*60;
	protected String m_ownerRoleId = null;

	public DatasourceJobDetail() {
		if (!"ADMINISTRATORS".equals(ReportUtil.getOwnerRoleId())) {
			m_ownerRoleId = ReportUtil.getOwnerRoleId();
		}

	}

	// Class for creating job tree
	private static class JobSession {
		private String sessionId;
		private JobNode rootJob;
		private Map<String, JobNode> allJobs = new LinkedHashMap<>();
		private List<JobNode> jobTreeList = null;

		JobSession(String sessionId) {
			this.sessionId = sessionId;
		}

		void setRootJob(JobNode job) {
			rootJob = job;
		}

		void addJob(JobNode job) {
			allJobs.put(job.jobId, job);
		}

		JobNode findJob(String jobId) {
			return allJobs.get(jobId);
		}

		void traverseJobTree(JobNode node, int level) {
			node.level = level;
			jobTreeList.add(node);
			for (JobNode child : node.getChildren()) {
				traverseJobTree(child, level + 1);
			}
		}

		boolean isNodeAncestor(JobNode target, JobNode node) {
			if (target == node) {
				return false;
			}
			int i = 0;
			for (JobNode cur = target; cur != rootJob && i < 64; ) {
				JobNode parent = findJob(cur.parentJobId);
				if (parent == cur || parent == null) {
					return false;
				}
				if (parent == node) {
					return true;
				}
				cur = parent;
				i++;
			}

			return false;
		}

		List<JobNode> getJobTreeList() {
			if (jobTreeList == null) {
				// link parents-children
				for (Iterator<Map.Entry<String, JobNode>> it = allJobs.entrySet().iterator(); it.hasNext(); ) {
					Map.Entry<String, JobNode> entry = it.next();
					JobNode node = entry.getValue();
					if (node != rootJob) {
						JobNode parent = findJob(node.parentJobId);
						if (parent != null) {
							if (parent == node || isNodeAncestor(parent, node)) {
								m_log.debug("broken tree: " + parent.jobId + "/" + node.jobId);
							} else {
								parent.addChild(node);
							}
						} else {
							m_log.debug(node.jobId + ": cannot find parent: " + node.parentJobId);
						}
					} else {
						// 何もしない
					}
				}

				jobTreeList = new ArrayList<>();
				traverseJobTree(rootJob, 0);
			}

			return jobTreeList;
		}
	}

	// Class for creating job tree
	private static class JobNode {
		private String jobId;
		private String parentJobId;
		private int level = 0;
		private List<JobNode> children = new ArrayList<>();

		private static final int MAX_JOBID_INDENT_LEVEL = 10;

		String job_name;
		Timestamp start_date;
		Timestamp end_date;
		int trigger_type;
		String trigger_info;
		Integer end_status;
		String job_series;
		String elapsed_time;
		Integer status;
		Timestamp schedule_date;

		JobNode(String jobId, String parentJobId) {
			this.jobId = jobId;
			this.parentJobId = parentJobId;
		}

		void addChild(JobNode child) {
			if (jobId.equals(child.parentJobId)) {
				children.add(child);
			} else {
				m_log.debug("child.parentJobId ("+child.parentJobId+") != jobId ("+jobId+")");
			}
		}

		List<JobNode> getChildren() {
			return children;
		}

		String getCsvLine(String sessionId, int maxLabelLength) {
			String indent = "                    ".substring(0, (level > MAX_JOBID_INDENT_LEVEL ? MAX_JOBID_INDENT_LEVEL : level) * 2);
			String jobLabel = indent + jobId + " (" + job_name + ")";
			jobLabel = (jobLabel.length() <= maxLabelLength ? jobLabel :
				jobLabel.substring(0, maxLabelLength) + "...");

			String endStatusStr = end_status == null ? "" : ReportUtil.getEndStatusString(end_status);
			String statusStr = status == null ? "" : Messages.getString(ReportUtil.getStatusString(status));

			String[] data = { sessionId, jobId, (start_date == null ? "" : start_date.toString()),
					(end_date == null ? "" : end_date.toString()), Integer.toString(trigger_type), trigger_info,
					(end_status == null ? "" : end_status.toString()), job_series,
					(elapsed_time == null ? "" : elapsed_time), (status == null ? "" : Integer.toString(status)),
					endStatusStr, statusStr, schedule_date.toString(), jobLabel };
			return ReportUtil.joinStringsToCsv(data);
		}
	}

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
		String dayString = new SimpleDateFormat("yyyyMMdd").format(m_startDate);

		String jobUnitRegex = isDefine(JOB_UNIT_REGEX+"."+num, "%%");		
		String jobIdRegex = isDefine(JOB_ID_REGEX+"."+num, "%%");
		String jobIdRegexExc = isDefine(JOB_ID_REGEX_EXC+"."+num, "");

		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, suffix + "_" + dayString);
		HashMap<String, Object> retMap = new HashMap<String, Object>();

		String[] columns = {
				"session_id", "job_id", "start_date", "end_date",
				"trigger_type", "trigger_info", "end_status", "job_series", "elapsed_time",
				"status", "end_status_str", "status_str",
				"schedule_date", "job_label" };
		String columnsStr = ReportUtil.joinStringsToCsv(columns);

		// get data from Hinemos DB
		BufferedWriter bw = null;
		try {
			if(new File(csvFileName).exists()){
				m_log.info("File : " + csvFileName + " is exists.");
			} else {
				m_log.info("output csv: " + csvFileName);
				File csv = new File(csvFileName);
				bw = new BufferedWriter(new FileWriter(csv, false));
				bw.write(columnsStr);
				bw.newLine();
				int maxLength = Integer.parseInt(isDefine("max.jobid.length", "65"));

				// write to csv file
				Map<String, JobSession> sessionMap = new LinkedHashMap<>();
				ReportingJobControllerBean controller = new ReportingJobControllerBean();

				String rootparentUnitJobId = "_ROOT_";
				List<JobSessionJobEntity> rootJobSessionJobList = controller.getRootJobSessionJobByParentJobunitId(rootparentUnitJobId, m_startDate.getTime(), m_startDate.getTime() + m_daySec);
				for (JobSessionJobEntity rootJobSessionJobEntity : rootJobSessionJobList) {
					if (rootJobSessionJobEntity != null) {
						List<JobSessionJobEntity> jobSessionList = controller.getReportingJobDetailList(rootJobSessionJobEntity.getId().getSessionId(), jobUnitRegex, jobIdRegex, jobIdRegexExc, m_ownerRoleId);
						for (JobSessionJobEntity entity : jobSessionList) {
							String sessionId = entity.getId().getSessionId();
							String jobId = entity.getId().getJobId();
							String parentJobId = entity.getParentJobId();
							JobNode job = new JobNode(jobId, parentJobId);
							JobSessionEntity jobSessionEntity = entity.getJobSessionEntity();
							JobInfoEntity jobInfo = entity.getJobInfoEntity();
							if (jobSessionEntity == null || jobInfo == null) {
								throw new HinemosUnknown("job info is null." + entity.getId().toString());
							}
							job.job_name = jobInfo.getJobName();
							if (entity.getStartDate() != null) {
								job.start_date = new Timestamp(entity.getStartDate());
								job.start_date.setNanos(0);
							} else {
								job.start_date = null;
							}
							if (entity.getEndDate() != null) {
								job.end_date = new Timestamp(entity.getEndDate());
								job.end_date.setNanos(0);
							} else {
								job.end_date = null;
							}
							if (jobSessionEntity.getTriggerType() != null) {
								job.trigger_type = jobSessionEntity.getTriggerType();
							} else {
								throw new HinemosUnknown("triggerType is null");
							}
							job.trigger_info = jobSessionEntity.getTriggerInfo();
							job.end_status = entity.getEndStatus();
							job.job_series = "job1";
							Long elapsedTime = null;
							if (entity.getEndDate() != null && entity.getStartDate() != null) {
								elapsedTime = TimeUnit.MILLISECONDS.toSeconds(entity.getEndDate()) - TimeUnit.MILLISECONDS.toSeconds(entity.getStartDate());
							}
							job.elapsed_time = millsecToTime(elapsedTime, jobId);
							job.status = entity.getStatus();
							job.schedule_date = new Timestamp(jobSessionEntity.getScheduleDate());
							job.schedule_date.setNanos(0);
							job.parentJobId = entity.getParentJobId();

							JobSession jobSession = sessionMap.get(sessionId);

							if (jobSession == null) {
								jobSession = new JobSession(sessionId);
								sessionMap.put(sessionId, jobSession);
							}
							if ("TOP".equals(parentJobId)) {
								jobSession.setRootJob(job);
							}
							jobSession.addJob(job);
						}
					}
				}
				if (!sessionMap.isEmpty()) {
					for (Iterator<Map.Entry<String, JobSession>> it = sessionMap.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry<String, JobSession> entry = it.next();
						JobSession session = entry.getValue();
						List<JobNode> jobList = session.getJobTreeList();
						for (JobNode node : jobList) {
							bw.write(node.getCsvLine(session.sessionId, maxLength));
							bw.newLine();
						}
					}
					sessionMap.clear();
				}
				JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
				ds.setUseFirstRowAsHeader(true);

				retMap.put(ReportingConstant.STR_DS+"_"+num, ds);
			}
		} catch (IOException | JRException e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					m_log.warn(e.getMessage());
				}
			}
		}

		return retMap;
	}

	protected String millsecToTime(Long millsec, String job_id) {

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
