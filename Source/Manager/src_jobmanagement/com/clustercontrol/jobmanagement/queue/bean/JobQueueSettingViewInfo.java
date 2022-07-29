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

/**
 * ジョブキュー(同時実行制御キュー)の設定を一覧表示するビューのための情報です。
 * 
 * @since 6.2.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobQueueSettingViewInfo implements Serializable {

	// 実装を変更したときのバージョン番号に合わせる。 {major(high)}_{major(low)}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	private List<JobQueueSettingViewInfoListItem> items;

	public List<JobQueueSettingViewInfoListItem> getItems() {
		return items;
	}

	public void setItems(List<JobQueueSettingViewInfoListItem> items) {
		this.items = items;
	}

	public static class JobQueueSettingViewInfoListItem implements Serializable {

		// 実装を変更したときのバージョン番号に合わせる。 {major(high)}_{major(low)}_{minor}_{patch}
		private static final long serialVersionUID = 6_02_00_00000000L;

		private String queueId;
		private String name;
		private Integer concurrency;
		private String ownerRoleId;
		private Long regDate;
		private String regUser;
		private Long updateDate;
		private String updateUser;

		public JobQueueSettingViewInfoListItem() {
		}

		public JobQueueSettingViewInfoListItem(String queueId, String name, Integer concurrency, String ownerRoleId, Long regDate, String regUser,
				Long updateDate, String updateUser) {
			this.queueId = queueId;
			this.name = name;
			this.concurrency = concurrency;
			this.ownerRoleId = ownerRoleId;
			this.regDate = regDate;
			this.regUser = regUser;
			this.updateDate = updateDate;
			this.updateUser = updateUser;
		}

		public String getQueueId() {
			return queueId;
		}

		public void setQueueId(String queueId) {
			this.queueId = queueId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getConcurrency() {
			return concurrency;
		}

		public void setConcurrency(Integer concurrency) {
			this.concurrency = concurrency;
		}

		public String getOwnerRoleId() {
			return ownerRoleId;
		}

		public void setOwnerRoleId(String ownerRoleId) {
			this.ownerRoleId = ownerRoleId;
		}

		public Long getRegDate() {
			return regDate;
		}

		public void setRegDate(Long regDate) {
			this.regDate = regDate;
		}

		public String getRegUser() {
			return regUser;
		}

		public void setRegUser(String regUser) {
			this.regUser = regUser;
		}

		public Long getUpdateDate() {
			return updateDate;
		}

		public void setUpdateDate(Long updateDate) {
			this.updateDate = updateDate;
		}

		public String getUpdateUser() {
			return updateUser;
		}

		public void setUpdateUser(String updateUser) {
			this.updateUser = updateUser;
		}
	}
}
