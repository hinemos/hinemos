/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.ValueSeparatorConstant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


/**
 * The persistent class for the cc_job_wait_info database table.
 * 
 */
@Entity
@Table(name="cc_job_wait_info", schema="log")
public class JobWaitInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobWaitInfoEntityPK id;
	private JobWaitGroupInfoEntity jobWaitGroupInfoEntity;
	private String description;

	/** 終了値(FromTo) */
	private ArrayList<Integer[]> intValueRangeList;

	/** 終了値 */
	private ArrayList<Integer> intValueList;

	/** 終了値(FromTo)(ジョブ変数用[変数変換前]) */
	private ArrayList<String[]> stringValueRangeList;

	/** 終了値(ジョブ変数用[変数変換前]) */
	private ArrayList<String> stringValueList;

	@Deprecated
	public JobWaitInfoEntity() {
	}

	public JobWaitInfoEntity(JobWaitInfoEntityPK pk) {
		this.setId(pk);
	}

	public JobWaitInfoEntity(JobWaitGroupInfoEntity jobWaitGroupInfoEntity,
			Integer targetJobType,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			Integer targetInt2,
			String targetStr1,
			String targetStr2,
			Long targetLong) {
		this(new JobWaitInfoEntityPK(
				jobWaitGroupInfoEntity.getId().getSessionId(),
				jobWaitGroupInfoEntity.getId().getJobunitId(),
				jobWaitGroupInfoEntity.getId().getJobId(),
				jobWaitGroupInfoEntity.getId().getOrderNo(),
				targetJobType,
				targetJobunitId,
				targetJobId,
				targetInt1,
				targetInt2,
				targetStr1,
				targetStr2,
				targetLong));
	}

	@EmbeddedId
	public JobWaitInfoEntityPK getId() {
		return this.id;
	}

	public void setId(JobWaitInfoEntityPK id) {
		this.id = id;
	}

	//bi-directional many-to-one association to JobWaitGroupInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="order_no", referencedColumnName="order_no", insertable=false, updatable=false),
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false),
		@JoinColumn(name="session_id", referencedColumnName="session_id", insertable=false, updatable=false)
	})
	public JobWaitGroupInfoEntity getJobWaitGroupInfoEntity() {
		return this.jobWaitGroupInfoEntity;
	}

	@Deprecated
	public void setJobWaitGroupInfoEntity(JobWaitGroupInfoEntity jobWaitGroupInfoEntity) {
		this.jobWaitGroupInfoEntity = jobWaitGroupInfoEntity;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Transient
	public String getTargetJobunitId() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE) {
			return getId().getTargetJobunitId();
		} else {
			return null;
		}
	}

	@Transient
	public String getTargetJobId() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE) {
			return getId().getTargetJobId();
		} else {
			return null;
		}
	}

	@Transient
	public Integer getStatus() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS) {
			return getId().getTargetInt1();
		} else {
			return null;
		}
	}

	@Transient
	public ArrayList<Integer[]> getIntValueRangeList() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE) {
			if (intValueRangeList == null) {
				settingIntValue(getId().getTargetStr1());
			}
			return intValueRangeList;

		} else {
			return null;
		}
	}

	@Transient
	public ArrayList<Integer> getIntValueList() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE) {
			if (intValueList == null) {
				settingIntValue(getId().getTargetStr1());
			}
			return intValueList;

		} else {
			return null;
		}
	}

	@Transient
	public ArrayList<String[]> getStringValueRangeList() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
			if (stringValueRangeList == null) {
				settingStringValue(getId().getTargetStr2());
			}
			return stringValueRangeList;

		} else {
			return null;
		}
	}

	@Transient
	public ArrayList<String> getStringValueList() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
			if (stringValueList == null) {
				settingStringValue(getId().getTargetStr2());
			}
			return stringValueList;

		} else {
			return null;
		}
	}

	@Transient
	public Long getTime() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_TIME) {
			return getId().getTargetLong();
		} else {
			return null;
		}
	}

	@Transient
	public Integer getStartMinute() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_START_MINUTE) {
			return getId().getTargetInt1();
		} else {
			return null;
		}
	}

	@Transient
	public String getDecisionValue01() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
			return getId().getTargetStr1();
		} else {
			return null;
		}
	}

	@Transient
	public String getDecisionValue02() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER
				&& (stringValueRangeList == null && stringValueList == null)) {
			return getId().getTargetStr2();
		} else {
			return null;
		}
	}

	@Transient
	public Integer getDecisionCondition() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE) {
			return getId().getTargetInt1();
		} else {
			return null;
		}
	}

	@Transient
	public Integer getCrossSessionRange() {
		if (getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS
				|| getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE) {
			return getId().getTargetInt2();
		} else {
			return null;
		}
	}

	/**
	 * JobWaitGroupInfoEntityオブジェクト参照設定<BR>
	 * 
	 * JobWaitGroupInfoEntity設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToJobWaitGroupInfoEntity(JobWaitGroupInfoEntity jobWaitGroupInfoEntity) {
		this.setJobWaitGroupInfoEntity(jobWaitGroupInfoEntity);
		if (jobWaitGroupInfoEntity != null) {
			List<JobWaitInfoEntity> list = jobWaitGroupInfoEntity.getJobWaitInfoEntities();
			if (list == null) {
				list = new ArrayList<JobWaitInfoEntity>();
			} else {
				for (JobWaitInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobWaitGroupInfoEntity.setJobWaitInfoEntities(list);
		}
	}

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// JobWaitGroupInfoEntity
		if (this.jobWaitGroupInfoEntity != null) {
			List<JobWaitInfoEntity> list = this.jobWaitGroupInfoEntity.getJobWaitInfoEntities();
			if (list != null) {
				Iterator<JobWaitInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobWaitInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

	/** ジョブ終了状態 */
	public static JobWaitInfoEntity createJobEndStatus(
			JobWaitGroupInfoEntity jobWaitGroupInfoEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1) {
		return new JobWaitInfoEntity(jobWaitGroupInfoEntity,
				JudgmentObjectConstant.TYPE_JOB_END_STATUS,
				targetJobunitId, 
				targetJobId,
				targetInt1,
				0,
				"",
				"",
				0L);
	}

	/** ジョブ終了値 */
	public static JobWaitInfoEntity createJobEndValue(
			JobWaitGroupInfoEntity jobWaitGroupInfoEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			String targetStr1) {
		return new JobWaitInfoEntity(jobWaitGroupInfoEntity,
				JudgmentObjectConstant.TYPE_JOB_END_VALUE,
				targetJobunitId,
				targetJobId,
				targetInt1,
				0,
				targetStr1,
				"",
				0L);
	}

	/** 時刻 */
	public static JobWaitInfoEntity createTime(
			JobWaitGroupInfoEntity jobWaitGroupInfoEntity,
			Long targetLong) {
		return new JobWaitInfoEntity(jobWaitGroupInfoEntity,
				JudgmentObjectConstant.TYPE_TIME,
				"",
				"",
				0,
				0,
				"",
				"",
				targetLong);
	}

	/** セッション開始時の時間（分）  */
	public static JobWaitInfoEntity createStartMinute(
			JobWaitGroupInfoEntity jobWaitGroupInfoEntity,
			Integer targetInt1) {
		return new JobWaitInfoEntity(jobWaitGroupInfoEntity,
				JudgmentObjectConstant.TYPE_START_MINUTE,
				"",
				"",
				targetInt1,
				0,
				"",
				"",
				0L);
	}

	/** ジョブ変数 */
	public static JobWaitInfoEntity createJobParameter(
			JobWaitGroupInfoEntity jobWaitGroupInfoEntity,
			Integer targetInt1,
			String targetStr1,
			String targetStr2) {
		return new JobWaitInfoEntity(jobWaitGroupInfoEntity,
				JudgmentObjectConstant.TYPE_JOB_PARAMETER,
				"",
				"",
				targetInt1,
				0,
				targetStr1,
				targetStr2,
				0L);
	}

	/** セッション横断ジョブ終了状態 */
	public static JobWaitInfoEntity createCrossSessionJobEndStatus(
			JobWaitGroupInfoEntity jobWaitGroupInfoEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			Integer targetInt2) {
		return new JobWaitInfoEntity(jobWaitGroupInfoEntity,
				JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS,
				targetJobunitId,
				targetJobId,
				targetInt1,
				targetInt2,
				"",
				"",
				0L);
	}

	/** セッション横断ジョブ終了値 */
	public static JobWaitInfoEntity createCrossSessionJobEndValue(
			JobWaitGroupInfoEntity jobWaitGroupInfoEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			Integer targetInt2,
			String targetStr1) {
		return new JobWaitInfoEntity(jobWaitGroupInfoEntity,
				JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE,
				targetJobunitId,
				targetJobId,
				targetInt1,
				targetInt2,
				targetStr1,
				"",
				0L);
	}

	/** ジョブリターンコード */
	public static JobWaitInfoEntity createJobReturnValue(
			JobWaitGroupInfoEntity jobWaitGroupInfoEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetInt1,
			String targetStr1) {
		return new JobWaitInfoEntity(jobWaitGroupInfoEntity,
				JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE,
				targetJobunitId,
				targetJobId,
				targetInt1,
				0,
				targetStr1,
				"",
				0L);
	}

	private void settingIntValue(String strValue) {
		// 初期化
		intValueRangeList = new ArrayList<>();
		intValueList = new ArrayList<>();
		if (strValue == null) {
			return;
		}
		String[] valueArray = strValue.split(ValueSeparatorConstant.MULTIPLE);
		try {
			for (String value : valueArray) {
				if (value.indexOf(ValueSeparatorConstant.RANGE) < 0) {
					intValueList.add(Integer.parseInt(value));
				} else {
					Integer[] valueRange = { Integer.parseInt(value.split(ValueSeparatorConstant.RANGE)[0]),
							Integer.parseInt(value.split(ValueSeparatorConstant.RANGE)[1]) };
					intValueRangeList.add(valueRange);
				}
			}
		} catch (NumberFormatException e) {
			// 何もしない
		}
	}

	private void settingStringValue(String strValue) {
		// 初期化
		stringValueRangeList = new ArrayList<>();
		stringValueList = new ArrayList<>();
		if (strValue == null) {
			return;
		}
		String[] valueArray = strValue.split(ValueSeparatorConstant.MULTIPLE);
		try {
			for (String value : valueArray) {
				if (value.indexOf(ValueSeparatorConstant.RANGE) < 0) {
					stringValueList.add(value);
				} else {
					String[] valueRange = { value.split(ValueSeparatorConstant.RANGE)[0],
							value.split(ValueSeparatorConstant.RANGE)[1] };
					stringValueRangeList.add(valueRange);
				}
			}
		} catch (NumberFormatException e) {
			// 何もしない
		}
	}

	@Override
	public String toString() {
		return "JobWaitInfoEntity ["
				+ "id=" + id
//				+ ", jobWaitGroupInfoEntity=" + jobWaitGroupInfoEntity	// 再帰になるので出力しない
				+ ", description=" + description
				+ ", intValueRangeList=" + intValueRangeList
				+ ", intValueList=" + intValueList
				+ ", stringValueRangeList=" + stringValueRangeList
				+ ", stringValueList=" + stringValueList
				+ "]";
	}
	
}