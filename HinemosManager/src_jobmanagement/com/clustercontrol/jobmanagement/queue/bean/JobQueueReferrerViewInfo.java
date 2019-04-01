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

import com.clustercontrol.jobmanagement.bean.JobInfo;

/**
 * ジョブキュー(同時実行制御キュー)を参照しているジョブを一覧表示するビューのための情報です。
 * 
 * @since 6.2.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobQueueReferrerViewInfo implements Serializable {

	// 実装を変更したときのバージョン番号に合わせる。 {major(high)}_{major(low)}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	private String queueId;
	private String queueName;
	private List<JobQueueReferrerViewInfoListItem> items;

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

	public List<JobQueueReferrerViewInfoListItem> getItems() {
		return items;
	}

	public void setItems(List<JobQueueReferrerViewInfoListItem> items) {
		this.items = items;
	}

	public static class JobQueueReferrerViewInfoListItem implements Serializable {

		// 実装を変更したときのバージョン番号に合わせる。 {major(high)}_{major(low)}_{minor}_{patch}
		private static final long serialVersionUID = 6_02_00_00000000L;

		private String jobunitId;
		private String jobId;
		private String ownerRoleId;

		private JobInfo jobInfoWithOwnerRoleId;
		
		public JobQueueReferrerViewInfoListItem() {
		}

		// JPQL用コンストラクタ
		public JobQueueReferrerViewInfoListItem(String jobunitId, String jobId, String ownerRoleId) {
			this.jobunitId = jobunitId;
			this.jobId = jobId;
			this.ownerRoleId = ownerRoleId;
		}

		public String getJobunitId() {
			return jobunitId;
		}

		public void setJobunitId(String jobunitId) {
			this.jobunitId = jobunitId;
		}

		public String getJobId() {
			return jobId;
		}

		public void setJobId(String jobId) {
			this.jobId = jobId;
		}

		public String getOwnerRoleId() {
			return ownerRoleId;
		}

		public void setOwnerRoleId(String ownerRoleId) {
			this.ownerRoleId = ownerRoleId;
		}

		/**
		 * ジョブの詳細情報を返します。
		 */
		public JobInfo getJobInfoWithOwnerRoleId() {
			return jobInfoWithOwnerRoleId;
		}

		/**
		 * ジョブの詳細情報を設定します。
		 */
		public void setJobInfoWithOwnerRoleId(JobInfo jobInfoWithOwnerRoleId) {
			this.jobInfoWithOwnerRoleId = jobInfoWithOwnerRoleId;
		}
	}
}
