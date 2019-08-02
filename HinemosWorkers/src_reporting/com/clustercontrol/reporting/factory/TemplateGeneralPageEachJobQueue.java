package com.clustercontrol.reporting.factory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.queue.internal.JobQueueEntity;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.util.ReportingQueryUtil;

import net.sf.jasperreports.engine.JasperPrint;

/**
 * ジョブキュー毎のページの作成を行うクラス
 * 
 * @version 6.2.b
 * @since 6.2.b
 */
public class TemplateGeneralPageEachJobQueue extends TemplateGeneralPageOverall {
	
	private static Log m_log = LogFactory.getLog(TemplateGeneralPageEachJobQueue.class);
	private List<JobQueueEntity> m_jobQueues = new ArrayList<>();
	
	public TemplateGeneralPageEachJobQueue() {
		m_jobQueues = ReportingQueryUtil.getReportingJobQueueSettingList(ReportUtil.getOwnerRoleId());
		// queueIdの昇順でソートしておく
		m_jobQueues.sort(new Comparator<JobQueueEntity>() {
			@Override
			public int compare(JobQueueEntity q1, JobQueueEntity q2) {
				return q1.getQueueId().compareTo(q2.getQueueId());
			}
		});

	}

	@Override
	public List<JasperPrint> getReport(Integer pageOffset) throws ReportingPropertyNotFound {
		
		List<JasperPrint> jpList = new ArrayList<>();
		List<JasperPrint> tmpList;
		
		if (m_jobQueues.isEmpty()) {
			// 対象の同時実行制御キューが１つも無い場合、ダミーのオブジェクトで空のページを出力する。
			JobQueueEntity dummy = new JobQueueEntity("");
			dummy.setName("");
			dummy.setConcurrency(0);
			m_jobQueues.add(dummy);
			m_log.warn("getReport : JobQueue not found. ownerRoleId=" + ReportUtil.getOwnerRoleId());
		}

		for (JobQueueEntity jobQueue : m_jobQueues){
			m_params.put("JOBQUEUE_ID", jobQueue.getQueueId());
			m_params.put("JOBQUEUE_NAME", jobQueue.getName());
			m_params.put("MAX_CONCURRENCY", jobQueue.getConcurrency());
			m_log.debug("getReport: jobQueue: " + jobQueue.getName() + " (" + jobQueue.getQueueId() + ")");
			tmpList = super.getReport(pageOffset);
			if (tmpList != null) {
				jpList.addAll(tmpList);
				for (JasperPrint jp : tmpList) {
					pageOffset += jp.getPages().size();
				}
			}
		}
		return jpList;
	}
}
