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
 * The persistent class for the cc_job_session_node database table.
 *
 */
@Entity
@Table(name="cc_job_session_node", schema="log")
public class JobSessionNodeEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobSessionNodeEntityPK id;
	private String nodeName				=	null;
	private Integer status				=	null;
	private Long startDate			=	null;
	private Long endDate			=	null;
	private Integer endValue			=	null;
	private String message				=	null;
	private Integer retryCount			=	0;
	private Integer errorRetryCount = 0;
	private String result				=	null;
	private long startupTime	= 0;
	private String instanceId = null;
	private Integer approvalStatus		=null;
	private Integer approvalResult		=null;
	private String approvalRequestUser	="";
	private String approvalUser		="";
	private String approvalComment		="";
	private JobSessionJobEntity jobSessionJobEntity;

	@Deprecated
	public JobSessionNodeEntity() {
	}

	public JobSessionNodeEntity(JobSessionNodeEntityPK pk,
			JobSessionJobEntity jobSessionJobEntity) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToJobSessionJobEntity(jobSessionJobEntity);
	}

	public JobSessionNodeEntity(JobSessionJobEntity jobSessionJobEntity, String facilityId) {
		this(new JobSessionNodeEntityPK(
				jobSessionJobEntity.getId().getSessionId(),
				jobSessionJobEntity.getId().getJobunitId(),
				jobSessionJobEntity.getId().getJobId(),
				facilityId), jobSessionJobEntity);
	}


	@EmbeddedId
	public JobSessionNodeEntityPK getId() {
		return this.id;
	}

	public void setId(JobSessionNodeEntityPK id) {
		this.id = id;
	}

	@Column(name="end_date")
	public Long getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}


	@Column(name="end_value")
	public Integer getEndValue() {
		return this.endValue;
	}

	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}


	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	@Column(name="node_name")
	public String getNodeName() {
		return this.nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}


	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result;
	}


	@Column(name="retry_count")
	public Integer getRetryCount() {
		return this.retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}


	@Column(name="error_retry_count")
	public Integer getErrorRetryCount() {
		return this.errorRetryCount;
	}

	public void setErrorRetryCount(Integer errorRetryCount) {
		this.errorRetryCount = errorRetryCount;
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


	@Column(name="startup_time")
	public long getStartupTime() {
		return this.startupTime;
	}

	public void setStartupTime(long startupTime) {
		this.startupTime = startupTime;
	}

	@Column(name="instance_id")
	public String getInstanceId() {
		return this.instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	@Column(name="approval_status")
	public Integer getApprovalStatus() {
		return this.approvalStatus;
	}

	public void setApprovalStatus(Integer status) {
		this.approvalStatus = status;
	}

	@Column(name="approval_result")
	public Integer getApprovalResult() {
		return this.approvalResult;
	}

	public void setApprovalResult(Integer result) {
		this.approvalResult = result;
	}

	@Column(name="approval_request_user")
	public String getApprovalRequestUser() {
		return this.approvalRequestUser;
	}

	public void setApprovalRequestUser(String approvalRequestUser) {
		this.approvalRequestUser = approvalRequestUser;
	}

	@Column(name="approval_user")
	public String getApprovalUser() {
		return this.approvalUser;
	}

	public void setApprovalUser(String approvalUser) {
		this.approvalUser = approvalUser;
	}
	
	@Column(name="approval_comment")
	public String getApprovalComment() {
		return this.approvalComment;
	}

	public void setApprovalComment(String approvalComment) {
		this.approvalComment = approvalComment;
	}

	//bi-directional many-to-one association to JobSessionJobEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false),
		@JoinColumn(name="session_id", referencedColumnName="session_id", insertable=false, updatable=false)
	})
	public JobSessionJobEntity getJobSessionJobEntity() {
		return this.jobSessionJobEntity;
	}

	@Deprecated
	public void setJobSessionJobEntity(JobSessionJobEntity jobSessionJobEntity) {
		this.jobSessionJobEntity = jobSessionJobEntity;
	}

	/**
	 * JobSessionJobEntityオブジェクト参照設定<BR>
	 *
	 * JobSessionJobEntity設定時はSetterに代わりこちらを使用すること。
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
	public void relateToJobSessionJobEntity(JobSessionJobEntity jobSessionJobEntity) {
		this.setJobSessionJobEntity(jobSessionJobEntity);
		if (jobSessionJobEntity != null) {
			List<JobSessionNodeEntity> list = jobSessionJobEntity.getJobSessionNodeEntities();
			if(list == null) {
				list = new ArrayList<JobSessionNodeEntity>();
			} else {
				for(JobSessionNodeEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobSessionJobEntity.setJobSessionNodeEntities(list);
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

		// JobSessionJobEntity
		if (this.jobSessionJobEntity != null) {
			List<JobSessionNodeEntity> list = this.jobSessionJobEntity.getJobSessionNodeEntities();
			if (list != null) {
				Iterator<JobSessionNodeEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobSessionNodeEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}