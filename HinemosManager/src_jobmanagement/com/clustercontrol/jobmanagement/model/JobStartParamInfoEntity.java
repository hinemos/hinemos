package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;


/**
 * The persistent class for the cc_job_decision_info database table.
 * 
 */
@Entity
@Table(name="cc_job_start_param_info", schema="log")
public class JobStartParamInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobStartParamInfoEntityPK id;
	private JobInfoEntity jobInfoEntity;
	private String decisionDescription;

	@Deprecated
	public JobStartParamInfoEntity() {
	}

	public JobStartParamInfoEntity(JobStartParamInfoEntityPK pk,
			JobInfoEntity jobInfoEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToJobInfoEntity(jobInfoEntity);
	}

	public JobStartParamInfoEntity(JobInfoEntity jobInfoEntity,
			String startDecisionValue01,
			String startDecisionValue02,
			Integer targetJobType,
			Integer startDecisionCondition) {
		this(new JobStartParamInfoEntityPK(
				jobInfoEntity.getId().getSessionId(),
				jobInfoEntity.getId().getJobunitId(),
				jobInfoEntity.getId().getJobId(),
				startDecisionValue01,
				startDecisionValue02,
				targetJobType,
				startDecisionCondition), jobInfoEntity);
	}


	@EmbeddedId
	public JobStartParamInfoEntityPK getId() {
		return this.id;
	}

	public void setId(JobStartParamInfoEntityPK id) {
		this.id = id;
	}


	//bi-directional many-to-one association to JobInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false),
		@JoinColumn(name="session_id", referencedColumnName="session_id", insertable=false, updatable=false)
	})
	public JobInfoEntity getJobInfoEntity() {
		return this.jobInfoEntity;
	}

	@Deprecated
	public void setJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.jobInfoEntity = jobInfoEntity;
	}

	@Column(name="decision_description")
	public String getDecisionDescription() {
		return this.decisionDescription;
	}
	public void setDecisionDescription(String decisionDescription) {
		this.decisionDescription = decisionDescription;
	}

	/**
	 * JobInfoEntityオブジェクト参照設定<BR>
	 * 
	 * JobInfoEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.setJobInfoEntity(jobInfoEntity);
		if (jobInfoEntity != null) {
			List<JobStartParamInfoEntity> list = jobInfoEntity.getJobStartParamInfoEntities();
			if (list == null) {
				list = new ArrayList<JobStartParamInfoEntity>();
			} else {
				for (JobStartParamInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobInfoEntity.setJobStartParamInfoEntities(list);
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

		// JobInfoEntity
		if (this.jobInfoEntity != null) {
			List<JobStartParamInfoEntity> list = this.jobInfoEntity.getJobStartParamInfoEntities();
			if (list != null) {
				Iterator<JobStartParamInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobStartParamInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}