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
 * The persistent class for the cc_job_start_job_mst database table.
 * 
 */
@Entity
@Table(name="cc_job_start_job_mst", schema="setting")
@Cacheable(true)
public class JobStartJobMstEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobStartJobMstEntityPK id;
	private JobMstEntity jobMstEntity;
	private String targetJobDescription;

	@Deprecated
	public JobStartJobMstEntity() {
	}

	public JobStartJobMstEntity(JobStartJobMstEntityPK pk,
			JobMstEntity jobMstEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToJobMstEntity(jobMstEntity);
	}

	public JobStartJobMstEntity(JobMstEntity jobMstEntity,
			String targetJobunitId,
			String targetJobId,
			Integer targetJobType,
			Integer targetJobEndValue) {
		this(new JobStartJobMstEntityPK(
				jobMstEntity.getId().getJobunitId(),
				jobMstEntity.getId().getJobId(),
				targetJobunitId,
				targetJobId,
				targetJobType,
				targetJobEndValue), jobMstEntity);
	}


	@EmbeddedId
	public JobStartJobMstEntityPK getId() {
		return this.id;
	}

	public void setId(JobStartJobMstEntityPK id) {
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

	@Column(name="target_job_description")
	public String getTargetJobDescription() {
		return this.targetJobDescription;
	}
	public void setTargetJobDescription(String targetJobDescription) {
		this.targetJobDescription = targetJobDescription;
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
			List<JobStartJobMstEntity> list = jobMstEntity.getJobStartJobMstEntities();
			if (list == null) {
				list = new ArrayList<JobStartJobMstEntity>();
			} else {
				for(JobStartJobMstEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobMstEntity.setJobStartJobMstEntities(list);
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
			List<JobStartJobMstEntity> list = this.jobMstEntity.getJobStartJobMstEntities();
			if (list != null) {
				Iterator<JobStartJobMstEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobStartJobMstEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}