package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.jobmanagement.bean.DelayNotifyConstant;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;


/**
 * The persistent class for the cc_job_session_job database table.
 * 
 */
@Entity
@Table(name="cc_job_session_job", schema="log")
public class JobSessionJobEntity  implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobSessionJobEntityPK id;
	private String scopeText						=	null;
	private Integer status							=	null;
	private Long startDate					=	null;
	private Long endDate						=	null;
	private Integer endValue						=	null;
	private Integer endStatus						=	null;
	private String result							=	null;
	private Integer endStausCheckFlg				=	null;
	private Integer delayNotifyFlg					=	DelayNotifyConstant.NONE;
	private JobInfoEntity jobInfoEntity;
	private JobSessionEntity jobSessionEntity;
	private List<JobSessionNodeEntity> jobSessionNodeEntities = new ArrayList<JobSessionNodeEntity>();
	private String parentJobunitId;
	private String parentJobId;
	private String ownerRoleId;

	@Deprecated
	public JobSessionJobEntity() {
	}

	public JobSessionJobEntity(JobSessionJobEntityPK pk,
			JobSessionEntity jobSessionEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);

		// オブジェクト権限チェックのため、cc_job_mstのowner_role_idを設定する
		if (CreateJobSession.TOP_JOBUNIT_ID.equals(this.getId().getJobunitId())) {
			this.setOwnerRoleId(RoleIdConstant.ALL_USERS);
		} else {
			JobMstEntity jobMstEntity
			= em.find(JobMstEntity.class,
					new JobMstEntityPK(this.getId().getJobunitId(), this.getId().getJobunitId()), ObjectPrivilegeMode.NONE);
			if (jobMstEntity != null && jobMstEntity.getOwnerRoleId() != null) {
				this.setOwnerRoleId(jobMstEntity.getOwnerRoleId());
			} else {
				this.setOwnerRoleId(RoleIdConstant.INTERNAL);
			}
		}
		this.relateToJobSessionEntity(jobSessionEntity);
	}

	public JobSessionJobEntity(JobSessionEntity jobSessionEntity, String jobunitId, String jobId) {
		this(new JobSessionJobEntityPK(
				jobSessionEntity.getSessionId(),
				jobunitId,
				jobId), jobSessionEntity);
	}

	@EmbeddedId
	public JobSessionJobEntityPK getId() {
		return this.id;
	}

	public void setId(JobSessionJobEntityPK id) {
		this.id = id;
	}


	@Column(name="delay_notify_flg")
	public Integer getDelayNotifyFlg() {
		return this.delayNotifyFlg;
	}

	public void setDelayNotifyFlg(Integer delayNotifyFlg) {
		this.delayNotifyFlg = delayNotifyFlg;
	}


	@Column(name="end_date")
	public Long getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}


	@Column(name="end_status")
	public Integer getEndStatus() {
		return this.endStatus;
	}

	public void setEndStatus(Integer endStatus) {
		this.endStatus = endStatus;
	}


	@Column(name="end_staus_check_flg")
	public Integer getEndStausCheckFlg() {
		return this.endStausCheckFlg;
	}

	public void setEndStausCheckFlg(Integer endStausCheckFlg) {
		this.endStausCheckFlg = endStausCheckFlg;
	}


	@Column(name="end_value")
	public Integer getEndValue() {
		return this.endValue;
	}

	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}


	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result;
	}


	@Column(name="scope_text")
	public String getScopeText() {
		return this.scopeText;
	}

	public void setScopeText(String scopeText) {
		this.scopeText = scopeText;
	}


	@Column(name="start_date")
	public Long getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}


	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name="parent_jobunit_id")
	public String getParentJobunitId() {
		return parentJobunitId;
	}

	public void setParentJobunitId(String parentJobunitId) {
		this.parentJobunitId = parentJobunitId;
	}

	@Column(name="parent_job_id")
	public String getParentJobId() {
		return parentJobId;
	}

	public void setParentJobId(String parentJobId) {
		this.parentJobId = parentJobId;
	}

	@Column(name="owner_role_id")
	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	//bi-directional one-to-one association to JobInfoEntity
	@OneToOne(mappedBy="jobSessionJobEntity", cascade=CascadeType.ALL)
	public JobInfoEntity getJobInfoEntity() {
		return this.jobInfoEntity;
	}

	public void setJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.jobInfoEntity = jobInfoEntity;
	}


	//bi-directional many-to-one association to JobSessionEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="session_id", insertable=false, updatable=false)
	public JobSessionEntity getJobSessionEntity() {
		return this.jobSessionEntity;
	}

	@Deprecated
	public void setJobSessionEntity(JobSessionEntity jobSessionEntity) {
		this.jobSessionEntity = jobSessionEntity;
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
	public void relateToJobSessionEntity(JobSessionEntity jobSessionEntity) {
		this.setJobSessionEntity(jobSessionEntity);
		if (jobSessionEntity != null) {
			List<JobSessionJobEntity> list = jobSessionEntity.getJobSessionJobEntities();
			if (list == null) {
				list = new ArrayList<JobSessionJobEntity>();
			} else {
				for (JobSessionJobEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobSessionEntity.setJobSessionJobEntities(list);
		}
	}


	//bi-directional many-to-one association to JobSessionNodeEntity
	@OneToMany(mappedBy="jobSessionJobEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<JobSessionNodeEntity> getJobSessionNodeEntities() {
		return this.jobSessionNodeEntities;
	}

	public void setJobSessionNodeEntities(List<JobSessionNodeEntity> jobSessionNodeEntities) {
		this.jobSessionNodeEntities = jobSessionNodeEntities;
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
		// JobSessionEntity
		if (this.jobSessionEntity != null) {
			List<JobSessionJobEntity> list = this.jobSessionEntity.getJobSessionJobEntities();
			if (list != null) {
				Iterator<JobSessionJobEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobSessionJobEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}