package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
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
 * The persistent class for the cc_job_decision_mst database table.
 * 
 */
@Entity
@Table(name="cc_job_start_param_mst", schema="setting")
@Cacheable(true)
public class JobStartParamMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobStartParamMstEntityPK id;
	private JobMstEntity jobMstEntity;
	private String decisionDescription;

	@Deprecated
	public JobStartParamMstEntity() {
	}

	public JobStartParamMstEntity(JobStartParamMstEntityPK pk,
			JobMstEntity jobMstEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToJobMstEntity(jobMstEntity);
	}

	public JobStartParamMstEntity(JobMstEntity jobMstEntity,
			String startDecisionValue01,
			String startDecisionValue02,
			Integer targetJobType,
			Integer startDecisionCondition) {
		this(new JobStartParamMstEntityPK(
				jobMstEntity.getId().getJobunitId(),
				jobMstEntity.getId().getJobId(),
				startDecisionValue01,
				startDecisionValue02,
				targetJobType,
				startDecisionCondition), jobMstEntity);
	}


	@EmbeddedId
	public JobStartParamMstEntityPK getId() {
		return this.id;
	}

	public void setId(JobStartParamMstEntityPK id) {
		this.id = id;
	}


	//bi-directional many-to-one association to JobMstEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false)
	})
	public JobMstEntity getJobMstEntity() {
		return this.jobMstEntity;
	}

	@Deprecated
	public void setJobMstEntity(JobMstEntity jobMstEntity) {
		this.jobMstEntity = jobMstEntity;
	}

	@Column(name="decision_description")
	public String getDecisionDescription() {
		return this.decisionDescription;
	}
	public void setDecisionDescription(String decisionDescription) {
		this.decisionDescription = decisionDescription;
	}

	/**
	 * JobMstEntityオブジェクト参照設定<BR>
	 * 
	 * JobMstEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToJobMstEntity(JobMstEntity jobMstEntity) {
		this.setJobMstEntity(jobMstEntity);
		if (jobMstEntity != null) {
			List<JobStartParamMstEntity> list = jobMstEntity.getJobStartParamMstEntities();
			if (list == null) {
				list = new ArrayList<JobStartParamMstEntity>();
			} else {
				for(JobStartParamMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobStartParamMstEntities(list);
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

		// JobMstEntity
		if (this.jobMstEntity != null) {
			List<JobStartParamMstEntity> list = this.jobMstEntity.getJobStartParamMstEntities();
			if (list != null) {
				Iterator<JobStartParamMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobStartParamMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}