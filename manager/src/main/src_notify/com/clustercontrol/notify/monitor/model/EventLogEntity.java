package com.clustercontrol.notify.monitor.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.notify.util.NotifyUtil;


/**
 * The persistent class for the cc_event_log database table.
 *
 */
@Entity
@Table(name="cc_event_log", schema="log")
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.MONITOR)
@AttributeOverride(name="objectId",
column=@Column(name="monitor_id", insertable=false, updatable=false))
public class EventLogEntity extends ObjectPrivilegeTargetInfo implements Cloneable{

	private static final long serialVersionUID = 1L;
	private EventLogEntityPK id;
	private String application;
	private String comment;
	private Long commentDate;
	private String commentUser;
	private Long confirmDate;
	private Integer confirmFlg;
	private String confirmUser;
	private Long duplicationCount;
	private Long generationDate;
	private Boolean inhibitedFlg;
	private String message;
	private String messageOrg;
	private Integer priority;
	private String scopeText;
	private Boolean collectGraphFlg;
	private Long position;

	@Deprecated
	public EventLogEntity() {
	}

	public EventLogEntity(EventLogEntityPK pk) {
		this.setId(pk);
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.setObjectId(this.getId().getMonitorId());

		this.setOwnerRoleId(NotifyUtil.getOwnerRoleId(pk.getPluginId(), pk.getMonitorId(),
				pk.getMonitorDetailId(), pk.getFacilityId(), true));
	}

	public EventLogEntity(String monitorId,
			String monitorDetailId,
			String pluginId,
			Long outputDate,
			String facilityId) {
		this(new EventLogEntityPK(monitorId,
				monitorDetailId,
				pluginId,
				outputDate,
				facilityId));
	}

	public EventLogEntity(String monitorId, String monitorDetailId,
			String pluginId, Long outputDate, String facilityId,
			String ownerRoleId) {

		this.setId(new EventLogEntityPK(monitorId, monitorDetailId, pluginId,
				outputDate, facilityId));
		this.setObjectId(this.getId().getMonitorId());
		this.setOwnerRoleId(ownerRoleId);
	}

	@EmbeddedId
	public EventLogEntityPK getId() {
		return this.id;
	}

	public void setId(EventLogEntityPK id) {
		this.id = id;
	}

	@Column(name="application")
	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	@Column(name="comment")
	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


	@Column(name="comment_date")
	public Long getCommentDate() {
		return this.commentDate;
	}

	public void setCommentDate(Long commentDate) {
		this.commentDate = commentDate;
	}


	@Column(name="comment_user")
	public String getCommentUser() {
		return this.commentUser;
	}

	public void setCommentUser(String commentUser) {
		this.commentUser = commentUser;
	}


	@Column(name="confirm_date")
	public Long getConfirmDate() {
		return this.confirmDate;
	}

	public void setConfirmDate(Long confirmDate) {
		this.confirmDate = confirmDate;
	}


	@Column(name="confirm_flg")
	public Integer getConfirmFlg() {
		return this.confirmFlg;
	}

	public void setConfirmFlg(Integer confirmFlg) {
		this.confirmFlg = confirmFlg;
	}


	@Column(name="confirm_user")
	public String getConfirmUser() {
		return this.confirmUser;
	}

	public void setConfirmUser(String confirmUser) {
		this.confirmUser = confirmUser;
	}


	@Column(name="duplication_count")
	public Long getDuplicationCount() {
		return this.duplicationCount;
	}

	public void setDuplicationCount(Long duplicationCount) {
		this.duplicationCount = duplicationCount;
	}


	@Column(name="generation_date")
	public Long getGenerationDate() {
		return this.generationDate;
	}

	public void setGenerationDate(Long generationDate) {
		this.generationDate = generationDate;
	}


	@Column(name="inhibited_flg")
	public Boolean getInhibitedFlg() {
		return this.inhibitedFlg;
	}

	public void setInhibitedFlg(Boolean inhibitedFlg) {
		this.inhibitedFlg = inhibitedFlg;
	}


	@Column(name="message")
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Column(name="message_org")
	public String getMessageOrg() {
		return this.messageOrg;
	}

	public void setMessageOrg(String messageOrg) {
		this.messageOrg = messageOrg;
	}


	@Column(name="priority")
	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}


	@Column(name="scope_text")
	public String getScopeText() {
		return this.scopeText;
	}

	public void setScopeText(String scopeText) {
		this.scopeText = scopeText;
	}


	@Column(name="collect_graph_flg")
	public Boolean getCollectGraphFlg() {
		return this.collectGraphFlg;
	}

	public void setCollectGraphFlg(Boolean collectGraphFlg) {
		this.collectGraphFlg = collectGraphFlg;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((commentDate == null) ? 0 : commentDate.hashCode());
		result = prime * result + ((commentUser == null) ? 0 : commentUser.hashCode());
		result = prime * result + ((confirmDate == null) ? 0 : confirmDate.hashCode());
		result = prime * result + ((confirmFlg == null) ? 0 : confirmFlg.hashCode());
		result = prime * result + ((confirmUser == null) ? 0 : confirmUser.hashCode());
		result = prime * result + ((duplicationCount == null) ? 0 : duplicationCount.hashCode());
		result = prime * result + ((generationDate == null) ? 0 : generationDate.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((inhibitedFlg == null) ? 0 : inhibitedFlg.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((messageOrg == null) ? 0 : messageOrg.hashCode());
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((scopeText == null) ? 0 : scopeText.hashCode());
		result = prime * result + ((collectGraphFlg == null) ? 0 : collectGraphFlg.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventLogEntity other = (EventLogEntity) obj;
		if (application == null) {
			if (other.application != null)
				return false;
		} else if (!application.equals(other.application))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (commentDate == null) {
			if (other.commentDate != null)
				return false;
		} else if (!commentDate.equals(other.commentDate))
			return false;
		if (commentUser == null) {
			if (other.commentUser != null)
				return false;
		} else if (!commentUser.equals(other.commentUser))
			return false;
		if (confirmDate == null) {
			if (other.confirmDate != null)
				return false;
		} else if (!confirmDate.equals(other.confirmDate))
			return false;
		if (confirmFlg == null) {
			if (other.confirmFlg != null)
				return false;
		} else if (!confirmFlg.equals(other.confirmFlg))
			return false;
		if (confirmUser == null) {
			if (other.confirmUser != null)
				return false;
		} else if (!confirmUser.equals(other.confirmUser))
			return false;
		if (duplicationCount == null) {
			if (other.duplicationCount != null)
				return false;
		} else if (!duplicationCount.equals(other.duplicationCount))
			return false;
		if (generationDate == null) {
			if (other.generationDate != null)
				return false;
		} else if (!generationDate.equals(other.generationDate))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (inhibitedFlg == null) {
			if (other.inhibitedFlg != null)
				return false;
		} else if (!inhibitedFlg.equals(other.inhibitedFlg))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (messageOrg == null) {
			if (other.messageOrg != null)
				return false;
		} else if (!messageOrg.equals(other.messageOrg))
			return false;
		if (priority == null) {
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
			return false;
		if (scopeText == null) {
			if (other.scopeText != null)
				return false;
		} else if (!scopeText.equals(other.scopeText))
			return false;
		if (collectGraphFlg == null) {
			if (other.collectGraphFlg != null)
				return false;
		} else if (!collectGraphFlg.equals(other.collectGraphFlg))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EventLogEntity [id=" + id + ", application=" + application + ", comment=" + comment + ", commentDate="
				+ commentDate + ", commentUser=" + commentUser + ", confirmDate=" + confirmDate + ", confirmFlg="
				+ confirmFlg + ", confirmUser=" + confirmUser + ", duplicationCount=" + duplicationCount
				+ ", generationDate=" + generationDate + ", inhibitedFlg=" + inhibitedFlg + ", message=" + message
				+ ", messageOrg=" + messageOrg + ", priority=" + priority + ", scopeText=" + scopeText
				+ ", collectGraphFlg=" + collectGraphFlg +
				"]";
	}
	
	@Override
	public EventLogEntity clone() {
		try {
			EventLogEntity ret = (EventLogEntity) super.clone();
			ret.id = this.id;
			ret.application = this.application;
			ret.comment = this.comment;
			ret.commentDate = this.commentDate;
			ret.commentUser = this.commentUser;
			ret.confirmDate = this.confirmDate;
			ret.confirmFlg = this.confirmFlg;
			ret.confirmUser = this.confirmUser;
			ret.duplicationCount = this.duplicationCount;
			ret.generationDate = this.generationDate;
			ret.inhibitedFlg = this.inhibitedFlg;
			ret.message = this.message;
			ret.messageOrg = this.messageOrg;
			ret.priority = this.priority;
			ret.scopeText = this.scopeText;
			ret.collectGraphFlg = this.collectGraphFlg;
			ret.setOwnerRoleId(super.getOwnerRoleId());
			ret.setObjectId(super.getObjectId());
			return ret;
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
	
	@Column(name="position", insertable=false)
	public Long getPosition(){
		return this.position;
	}
	public void setPosition(Long position){
		this.position = position;
	}
}