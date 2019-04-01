/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue.bean;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobEndpoint;

/**
 * ジョブキュー(同時実行制御キュー)の内部状況(すなわちジョブ詳細単位の状態)を表示するビューのための情報です。
 * 
 * @since 6.2.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobQueueContentsViewInfo implements Serializable {

	// 実装を変更したときのバージョン番号に合わせる。 {major(high)}_{major(low)}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	private String queueId;
	private String queueName;
	private Integer concurrency;
	private Integer count;
	private Integer activeCount;
	private List<JobQueueContentsViewInfoListItem> items;

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Integer getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}
	
	/**
	 * 現在、キューの制御下にあるジョブの数を返します。
	 */
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 * 現在、キューの制御下で「実行中」状態のジョブの数を返します。
	 */
	public Integer getActiveCount() {
		return activeCount;
	}

	public void setActiveCount(Integer activeCount) {
		this.activeCount = activeCount;
	}

	public List<JobQueueContentsViewInfoListItem> getItems() {
		return items;
	}

	public void setItems(List<JobQueueContentsViewInfoListItem> items) {
		this.items = items;
	}

	/**
	 * キューの制御下にあるジョブの情報です。
	 * <p>
	 * このクラスが保持する{@link JobTreeItem}には、一覧表示に必要のないプロパティは設定されていません。
	 * 完全なジョブ詳細情報が必要な場合は、
	 * {@link JobEndpoint#getSessionJobInfo(String, String, String)}を
	 * 使用してください。
	 */
	public static class JobQueueContentsViewInfoListItem implements Serializable {

		// 実装を変更したときのバージョン番号に合わせる。 {major(high)}_{major(low)}_{minor}_{patch}
		private static final long serialVersionUID = 6_02_00_00000000L;

		private JobTreeItem jobTreeItem;
		private String sessionId;
		private Long regDate;
		
		public JobTreeItem getJobTreeItem() {
			return jobTreeItem;
		}

		public void setJobTreeItem(JobTreeItem jobTreeItem) {
			this.jobTreeItem = jobTreeItem;
		}
		
		public String getSessionId() {
			return sessionId;
		}

		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		public Long getRegDate() {
			return regDate;
		}

		public void setRegDate(Long regDate) {
			this.regDate = regDate;
		}
	}	
}
