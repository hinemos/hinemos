package com.clustercontrol.reporting.factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingJobControllerBean;
import com.clustercontrol.util.Messages;

import net.sf.jasperreports.engine.data.JRCsvDataSource;

public class DatasourceJobDetailByJobQueue extends DatasourceJobDetail {

	private static Log m_log = LogFactory.getLog(DatasourceJobDetail.class);
	private int m_maxLabelLength;

	private static class JobQueueCSV {
		ArrayList<JobQueueCSVRow> jobQueueCSVRows = new ArrayList<>();
		String[] columns = { "start_date", "end_date", "schedule_date", "elapsed_time", "concurrency",
				"session_id", "job_label", "status_str", "end_status_str" };
		String columnsStr;

		JobQueueCSV() {
			columnsStr = ReportUtil.joinStrings(columns, ",");
		}

		void add(JobQueueCSVRow row) {
			jobQueueCSVRows.add(row);
		}

		void writeHeader(BufferedWriter bw) throws IOException {
			bw.write(columnsStr);
			bw.newLine();
		}

		void writeRows(BufferedWriter bw) throws IOException {
			// 各ジョブの同時実行数を算出する
			List<Map<String, Object>> jobStatusTimeline = new ArrayList<>();
			for (JobQueueCSVRow row : jobQueueCSVRows) {
				/*
				 * 同時実行制御キューの対象は実行状態が「実行中」のジョブ。
				 * 「実行中」から遷移する実行状態は「終了」「終了（終了遅延）」「コマンド停止」の3つ。
				 * 「コマンド停止」は終了時刻が設定されないため除外し、「実行中」「終了」「終了（終了遅延）」が対象。 
				 */
				if (row.status != StatusConstant.TYPE_RUNNING && 
					row.status != StatusConstant.TYPE_END &&
					row.status != StatusConstant.TYPE_END_END_DELAY) {
					continue;
				}
				if (row.startDateTime == null) {  // ジョブが未実行の場合、同時実行数の算出は不要
					continue;
				}
				Map<String, Object> jobStartTime = new HashMap<>();
				jobStartTime.put("id", row.sessionId + row.jobLabel + row.hashCode());
				jobStartTime.put("time", row.startDateTime.getTime());
				jobStartTime.put("status", "start");
				jobStatusTimeline.add(jobStartTime);
				m_log.debug("writeRows : jobStartTime id=" + jobStartTime.get("id") +
						", time=" + jobStartTime.get("time") + ", status=" + jobStartTime.get("status"));

				if (row.endDateTime == null) {  // ジョブが実行中の場合、"start"のentryのみが存在
					continue;
				}
				Map<String, Object> jobEndTime = new HashMap<>();
				jobEndTime.put("id", row.sessionId + row.jobLabel + row.hashCode());
				jobEndTime.put("time", row.endDateTime.getTime());
				jobEndTime.put("status", "end");
				jobStatusTimeline.add(jobEndTime);
				m_log.debug("writeRows : jobEndTime id=" + jobEndTime.get("id") +
						", time=" + jobEndTime.get("time") + ", status=" + jobEndTime.get("status"));
			}
			Map<String, Integer> concurrencyMap = calculateConcurerncy(jobStatusTimeline);
			for (JobQueueCSVRow row : jobQueueCSVRows) {
				// 未実行のジョブはconcurrencyMapにentryが無い
				row.concurrency = concurrencyMap.getOrDefault(row.sessionId + row.jobLabel + row.hashCode(), 0);
			}

			// CSVの書き出し
			for (JobQueueCSVRow row : jobQueueCSVRows) {
				bw.write(row.getCSVLine());
				bw.newLine();
			}
		}
	}

	private class JobQueueCSVRow {
		Timestamp startDateTime;
		Timestamp endDateTime;
		Timestamp scheduleDateTime;
		String elapsedTime;
		Integer concurrency;
		String sessionId;
		String jobLabel;
		String statusStr;
		String endStatusStr;
		Integer status;

		JobQueueCSVRow(String sessionId, String jobId, String jobName, Long startDate, Long endDate, Long scheduleDate,
				Integer status, Integer endStatus) {
			this.sessionId = sessionId;
			if (startDate != null) {
				startDateTime = new Timestamp(startDate);
				startDateTime.setNanos(0);
			}
			if (endDate != null) {
				endDateTime = new Timestamp(endDate);
				endDateTime.setNanos(0);
			}
			if (endDate != null && startDate != null) {
				elapsedTime = millsecToTime(
						TimeUnit.MILLISECONDS.toSeconds(endDate) - TimeUnit.MILLISECONDS.toSeconds(startDate), jobId);
			}
			scheduleDateTime = new Timestamp(scheduleDate);
			scheduleDateTime.setNanos(0);
			jobLabel = jobId + " (" + jobName + ")";
			jobLabel = jobLabel.length() <= m_maxLabelLength ? jobLabel
					: this.jobLabel.substring(0, m_maxLabelLength) + "...";
			jobLabel = '"' + jobLabel.replace("\"", "\"\"") + '"';
			statusStr = status == null ? "" : Messages.getString(ReportUtil.getStatusString(status));
			endStatusStr = endStatus == null ? "" : ReportUtil.getEndStatusString(endStatus);
			concurrency = 0;  // 同時実行数は0で初期化し、後の処理で算出
			this.status = status;  // CSVへの出力には不要
		}

		String getCSVLine() {
			// JobQueueCSVのcolumnsと並びを合わせる
			return ((startDateTime == null ? "" : startDateTime) + "," + (endDateTime == null ? "" : endDateTime) + ","
					+ (scheduleDateTime == null ? "" : scheduleDateTime) + ","
					+ (elapsedTime == null ? "" : elapsedTime) + "," + concurrency + "," + sessionId + ","
					+ jobLabel + "," + (statusStr == null ? "" : statusStr) + ","
					+ (endStatusStr == null ? "" : endStatusStr));
		}

	}

	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {

		if (m_propertiesMap.get(SUFFIX_KEY_VALUE + "." + num).isEmpty()) {
			throw new ReportingPropertyNotFound(SUFFIX_KEY_VALUE + "." + num + " is not defined.");
		}
		m_maxLabelLength = Integer.parseInt(isDefine("max.jobid.length", "65"));

		String suffix = m_propertiesMap.get(SUFFIX_KEY_VALUE + "." + num);
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);

		String jobUnitRegex = isDefine(JOB_UNIT_REGEX + "." + num, "%%");
		String jobIdRegex = isDefine(JOB_ID_REGEX + "." + num, "%%");
		String jobIdRegexExc = isDefine(JOB_ID_REGEX_EXC + "." + num, "");

		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, suffix + "_" + dayString);
		HashMap<String, Object> retMap = new HashMap<String, Object>();

		m_log.debug("createDataSource: jobQueueId=" + m_jobQueueId + ", startDate=" + m_startDate +
				", jobUnitRegex=" + jobUnitRegex + ", jobIdRegex=" + jobIdRegex + ", jobIdRegexExc=" + jobIdRegexExc + 
				", csvFileName=" + csvFileName);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(csvFileName), false))) {
			ReportingJobControllerBean controller = new ReportingJobControllerBean();
			List<JobSessionJobEntity> jobSessionJobList = controller.getReportingJobDetailListByQueueId(m_jobQueueId,
					m_startDate.getTime(), m_startDate.getTime() + m_daySec, jobUnitRegex, jobIdRegex, jobIdRegexExc,
					m_ownerRoleId);

			JobQueueCSV csv = new JobQueueCSV();
			csv.writeHeader(bw);
			for (JobSessionJobEntity sessionJob : jobSessionJobList) {
				JobSessionEntity session = sessionJob.getJobSessionEntity();
				JobInfoEntity info = sessionJob.getJobInfoEntity();
				JobQueueCSVRow row = new JobQueueCSVRow(session.getSessionId(), sessionJob.getId().getJobId(), info.getJobName(),
						sessionJob.getStartDate(), sessionJob.getEndDate(), session.getScheduleDate(),
						sessionJob.getStatus(), sessionJob.getEndStatus());
				csv.add(row);
			}
			csv.writeRows(bw);

			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);
			retMap.put(ReportingConstant.STR_DS + "_" + num, ds);

		} catch (IOException e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		}

		return retMap;
	}

	/**
	 * ジョブ履歴からの各ジョブの同時実行数の算出に使用します。
	 * 以下のentryをもつMapのListを受け取ります。 
	 * 
	 * key: "id",     value: ジョブの実行履歴を特定するID（String）
	 * key: "status", value: "start"または"end"（String）
	 * key: "time",   value: ジョブが開始、または終了した時間（Long）
	 * 
	 * @param jobStatusTimeline
	 * @return
	 */
	private static Map<String, Integer> calculateConcurerncy(List<Map<String, Object>> jobStatusTimeline) {
		// リストをtimeの昇順でソート
		jobStatusTimeline.sort(new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				int comparison = Long.compare((Long) o1.get("time"), (Long) o2.get("time"));
				if (comparison != 0) {
					return comparison;
				} else {
					// 時刻が同一の場合は"start"が後になるようにする
					if (o1.get("status").equals("start")) {
						return 1;
					} else {
						return -1;
					}
				}
			}
		});

		Map<String, Integer> concurrencyMap = new HashMap<>();  // 各ジョブの同時実行数を管理
		List<String> runningIds = new ArrayList<>();  // "start"したジョブを管理
		int concurrency = 0;  // 同時実行数のカウンタ
		/*
		* ソートされた順にリストを走査し、以下の処理を行う。
		* 1. "status"が"start"の場合
		*   - runningIdsに"id"を追加
		*   - concurrencyをインクリメント
		*   - runningIds内のジョブについて、concurrencyMapの同時実行数を更新
		* 2. "status"が"end"の場合
		*   - runningIdsから"id"を削除
		*   - concurrencyをデクリメント
		*/
		for (Map<String, Object> jobStatusTime : jobStatusTimeline) {
			String id = (String) jobStatusTime.get("id");
			String status = (String) jobStatusTime.get("status");
			if (status.equals("start")) {
				runningIds.add(id);
				concurrency++;
				for (String runningId : runningIds) {
					// 新たなジョブの開始によりこれまでの同時実行数より大きくなれば値を更新する
					if (concurrency > concurrencyMap.getOrDefault(runningId, 0)) {
						concurrencyMap.put(runningId, concurrency);
					}
				}
			} else {
				runningIds.remove(id);
				concurrency--;
			}
		}
		return concurrencyMap;
	}

	public static void main(String args[]) {
		/* 
		 * time
		 * 0   1   2   3   4   5   6
		 * |---|---|---|---|---|---|
		 * 
		 * |-------------------|      -> job1 (start 0 -> end 5)
		 * 
		 * |---|                      -> job2 (start 0 -> end 1)
		 * 
		 *             |-------|      -> job3 (start 3 -> end 5)
		 * 
		 *         |-------|          -> job4 (start 2 -> end 4)
		 * 
		 *                     |---|  -> job5 (start 5 -> end 6)
		 *                     
		 *                 |------->  -> job6 (start 4 -> 実行中)
		 * 
		 * concurrency
		 * job1 -> 3
		 * job2 -> 2
		 * job3 -> 3
		 * job4 -> 3
		 * job5 -> 2
		 * job6 -> 3
		 * 
		 */
		List<Map<String, Object>> jobStatusTimeline = new ArrayList<>();
		Map<String, Object> job1 = new HashMap<>();
		job1.put("id", "job1");
		job1.put("time", 0L);
		job1.put("status", "start");
		jobStatusTimeline.add(job1);
		job1 = new HashMap<>();
		job1.put("id", "job1");
		job1.put("time", 5L);
		job1.put("status", "end");
		jobStatusTimeline.add(job1);

		Map<String, Object> job2 = new HashMap<>();
		job2.put("id", "job2");
		job2.put("time", 0L);
		job2.put("status", "start");
		jobStatusTimeline.add(job2);
		job2 = new HashMap<>();
		job2.put("id", "job2");
		job2.put("time", 1L);
		job2.put("status", "end");
		jobStatusTimeline.add(job2);

		Map<String, Object> job3 = new HashMap<>();
		job3.put("id", "job3");
		job3.put("time", 3L);
		job3.put("status", "start");
		jobStatusTimeline.add(job3);
		job3 = new HashMap<>();
		job3.put("id", "job3");
		job3.put("time", 5L);
		job3.put("status", "end");
		jobStatusTimeline.add(job3);

		Map<String, Object> job4 = new HashMap<>();
		job4.put("id", "job4");
		job4.put("time", 2L);
		job4.put("status", "start");
		jobStatusTimeline.add(job4);
		job4 = new HashMap<>();
		job4.put("id", "job4");
		job4.put("time", 4L);
		job4.put("status", "end");
		jobStatusTimeline.add(job4);

		Map<String, Object> job5 = new HashMap<>();
		job5.put("id", "job5");
		job5.put("time", 5L);
		job5.put("status", "start");
		jobStatusTimeline.add(job5);
		job5 = new HashMap<>();
		job5.put("id", "job5");
		job5.put("time", 6L);
		job5.put("status", "end");
		jobStatusTimeline.add(job5);

		Map<String, Object> job6 = new HashMap<>();
		job6.put("id", "job6");
		job6.put("time", 4L);
		job6.put("status", "start");
		jobStatusTimeline.add(job6);
		// 実行中のジョブ場合"end"のentryは存在しない
		
		Map<String, Integer> concurrencyMap = calculateConcurerncy(jobStatusTimeline);
		for (String id : new String[]{"job1", "job2", "job3", "job4", "job5", "job6"}) {
			System.out.println("id: " + id + ", concurency: " + concurrencyMap.get(id));
		}
	}
}
