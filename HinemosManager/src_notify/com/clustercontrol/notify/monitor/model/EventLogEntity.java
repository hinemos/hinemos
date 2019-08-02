/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.monitor.model;


import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.run.util.EventUtil;


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
	private String userItem01;
	private String userItem02;
	private String userItem03;
	private String userItem04;
	private String userItem05;
	private String userItem06;
	private String userItem07;
	private String userItem08;
	private String userItem09;
	private String userItem10;
	private String userItem11;
	private String userItem12;
	private String userItem13;
	private String userItem14;
	private String userItem15;
	private String userItem16;
	private String userItem17;
	private String userItem18;
	private String userItem19;
	private String userItem20;
	private String userItem21;
	private String userItem22;
	private String userItem23;
	private String userItem24;
	private String userItem25;
	private String userItem26;
	private String userItem27;
	private String userItem28;
	private String userItem29;
	private String userItem30;
	private String userItem31;
	private String userItem32;
	private String userItem33;
	private String userItem34;
	private String userItem35;
	private String userItem36;
	private String userItem37;
	private String userItem38;
	private String userItem39;
	private String userItem40;
	
	@Deprecated
	public EventLogEntity() {
	}

	public EventLogEntity(EventLogEntityPK pk) {
		this.setId(pk);
		this.setObjectId(this.getId().getMonitorId());
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

	@Column(name="position", insertable=false)
	public Long getPosition(){
		return this.position;
	}
	public void setPosition(Long position){
		this.position = position;
	}
	
	@Column(name="user_item01")
	public String getUserItem01() {
		return this.userItem01;
	}

	public void setUserItem01(String userItem01) {
		this.userItem01 = userItem01;
	}

	@Column(name="user_item02")
	public String getUserItem02() {
		return this.userItem02;
	}

	public void setUserItem02(String userItem02) {
		this.userItem02 = userItem02;
	}

	@Column(name="user_item03")
	public String getUserItem03() {
		return this.userItem03;
	}

	public void setUserItem03(String userItem03) {
		this.userItem03 = userItem03;
	}

	@Column(name="user_item04")
	public String getUserItem04() {
		return this.userItem04;
	}

	public void setUserItem04(String userItem04) {
		this.userItem04 = userItem04;
	}

	@Column(name="user_item05")
	public String getUserItem05() {
		return this.userItem05;
	}

	public void setUserItem05(String userItem05) {
		this.userItem05 = userItem05;
	}

	@Column(name="user_item06")
	public String getUserItem06() {
		return this.userItem06;
	}

	public void setUserItem06(String userItem06) {
		this.userItem06 = userItem06;
	}

	@Column(name="user_item07")
	public String getUserItem07() {
		return this.userItem07;
	}

	public void setUserItem07(String userItem07) {
		this.userItem07 = userItem07;
	}

	@Column(name="user_item08")
	public String getUserItem08() {
		return this.userItem08;
	}

	public void setUserItem08(String userItem08) {
		this.userItem08 = userItem08;
	}

	@Column(name="user_item09")
	public String getUserItem09() {
		return this.userItem09;
	}

	public void setUserItem09(String userItem09) {
		this.userItem09 = userItem09;
	}

	@Column(name="user_item10")
	public String getUserItem10() {
		return this.userItem10;
	}

	public void setUserItem10(String userItem10) {
		this.userItem10 = userItem10;
	}

	@Column(name="user_item11")
	public String getUserItem11() {
		return this.userItem11;
	}

	public void setUserItem11(String userItem11) {
		this.userItem11 = userItem11;
	}

	@Column(name="user_item12")
	public String getUserItem12() {
		return this.userItem12;
	}

	public void setUserItem12(String userItem12) {
		this.userItem12 = userItem12;
	}

	@Column(name="user_item13")
	public String getUserItem13() {
		return this.userItem13;
	}

	public void setUserItem13(String userItem13) {
		this.userItem13 = userItem13;
	}

	@Column(name="user_item14")
	public String getUserItem14() {
		return this.userItem14;
	}

	public void setUserItem14(String userItem14) {
		this.userItem14 = userItem14;
	}

	@Column(name="user_item15")
	public String getUserItem15() {
		return this.userItem15;
	}

	public void setUserItem15(String userItem15) {
		this.userItem15 = userItem15;
	}

	@Column(name="user_item16")
	public String getUserItem16() {
		return this.userItem16;
	}

	public void setUserItem16(String userItem16) {
		this.userItem16 = userItem16;
	}

	@Column(name="user_item17")
	public String getUserItem17() {
		return this.userItem17;
	}

	public void setUserItem17(String userItem17) {
		this.userItem17 = userItem17;
	}

	@Column(name="user_item18")
	public String getUserItem18() {
		return this.userItem18;
	}

	public void setUserItem18(String userItem18) {
		this.userItem18 = userItem18;
	}

	@Column(name="user_item19")
	public String getUserItem19() {
		return this.userItem19;
	}

	public void setUserItem19(String userItem19) {
		this.userItem19 = userItem19;
	}

	@Column(name="user_item20")
	public String getUserItem20() {
		return this.userItem20;
	}

	public void setUserItem20(String userItem20) {
		this.userItem20 = userItem20;
	}

	@Column(name="user_item21")
	public String getUserItem21() {
		return this.userItem21;
	}

	public void setUserItem21(String userItem21) {
		this.userItem21 = userItem21;
	}

	@Column(name="user_item22")
	public String getUserItem22() {
		return this.userItem22;
	}

	public void setUserItem22(String userItem22) {
		this.userItem22 = userItem22;
	}

	@Column(name="user_item23")
	public String getUserItem23() {
		return this.userItem23;
	}

	public void setUserItem23(String userItem23) {
		this.userItem23 = userItem23;
	}

	@Column(name="user_item24")
	public String getUserItem24() {
		return this.userItem24;
	}

	public void setUserItem24(String userItem24) {
		this.userItem24 = userItem24;
	}

	@Column(name="user_item25")
	public String getUserItem25() {
		return this.userItem25;
	}

	public void setUserItem25(String userItem25) {
		this.userItem25 = userItem25;
	}

	@Column(name="user_item26")
	public String getUserItem26() {
		return this.userItem26;
	}

	public void setUserItem26(String userItem26) {
		this.userItem26 = userItem26;
	}

	@Column(name="user_item27")
	public String getUserItem27() {
		return this.userItem27;
	}

	public void setUserItem27(String userItem27) {
		this.userItem27 = userItem27;
	}

	@Column(name="user_item28")
	public String getUserItem28() {
		return this.userItem28;
	}

	public void setUserItem28(String userItem28) {
		this.userItem28 = userItem28;
	}

	@Column(name="user_item29")
	public String getUserItem29() {
		return this.userItem29;
	}

	public void setUserItem29(String userItem29) {
		this.userItem29 = userItem29;
	}

	@Column(name="user_item30")
	public String getUserItem30() {
		return this.userItem30;
	}

	public void setUserItem30(String userItem30) {
		this.userItem30 = userItem30;
	}

	@Column(name="user_item31")
	public String getUserItem31() {
		return this.userItem31;
	}

	public void setUserItem31(String userItem31) {
		this.userItem31 = userItem31;
	}

	@Column(name="user_item32")
	public String getUserItem32() {
		return this.userItem32;
	}

	public void setUserItem32(String userItem32) {
		this.userItem32 = userItem32;
	}

	@Column(name="user_item33")
	public String getUserItem33() {
		return this.userItem33;
	}

	public void setUserItem33(String userItem33) {
		this.userItem33 = userItem33;
	}

	@Column(name="user_item34")
	public String getUserItem34() {
		return this.userItem34;
	}

	public void setUserItem34(String userItem34) {
		this.userItem34 = userItem34;
	}

	@Column(name="user_item35")
	public String getUserItem35() {
		return this.userItem35;
	}

	public void setUserItem35(String userItem35) {
		this.userItem35 = userItem35;
	}

	@Column(name="user_item36")
	public String getUserItem36() {
		return this.userItem36;
	}

	public void setUserItem36(String userItem36) {
		this.userItem36 = userItem36;
	}

	@Column(name="user_item37")
	public String getUserItem37() {
		return this.userItem37;
	}

	public void setUserItem37(String userItem37) {
		this.userItem37 = userItem37;
	}

	@Column(name="user_item38")
	public String getUserItem38() {
		return this.userItem38;
	}

	public void setUserItem38(String userItem38) {
		this.userItem38 = userItem38;
	}

	@Column(name="user_item39")
	public String getUserItem39() {
		return this.userItem39;
	}

	public void setUserItem39(String userItem39) {
		this.userItem39 = userItem39;
	}

	@Column(name="user_item40")
	public String getUserItem40() {
		return this.userItem40;
	}

	public void setUserItem40(String userItem40) {
		this.userItem40 = userItem40;
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
		
		for (int i = 1 ; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			String thisValue = EventUtil.getUserItemValue(this, i);
			
			result = prime * result + ((thisValue == null) ? 0 : thisValue.hashCode());
		}

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
		
		for (int i = 1 ; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			String thisValue = EventUtil.getUserItemValue(this, i);
			String otherValue =  EventUtil.getUserItemValue(other, i);
			
			if (thisValue == null) {
				if (otherValue != null)
					return false;
			} else if (!thisValue.equals(otherValue))
				return false;
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 1 ; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			sb.append(String.format(", userItem%02d=", i));
			sb.append(EventUtil.getUserItemValue(this, i));
		}
		
		return "EventLogEntity [id=" + id + ", application=" + application + ", comment=" + comment + ", commentDate="
				+ commentDate + ", commentUser=" + commentUser + ", confirmDate=" + confirmDate + ", confirmFlg="
				+ confirmFlg + ", confirmUser=" + confirmUser + ", duplicationCount=" + duplicationCount
				+ ", generationDate=" + generationDate + ", inhibitedFlg=" + inhibitedFlg + ", message=" + message
				+ ", messageOrg=" + messageOrg + ", priority=" + priority + ", scopeText=" + scopeText
				+ ", collectGraphFlg=" + collectGraphFlg + sb.toString()
				+ "]";
	}
	
	@Override
	public EventLogEntity clone() {
		try {
			EventLogEntity ret = (EventLogEntity) super.clone();
			ret.id =new EventLogEntityPK(
				this.id.getMonitorId(),
				this.id.getMonitorDetailId(),
				this.id.getPluginId(),
				this.id.getOutputDate(),
				this.id.getFacilityId());
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
			ret.position = this.position;
			for (int i = 1 ; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
				EventUtil.setUserItemValue(ret, i, EventUtil.getUserItemValue(this, i));
			}
			ret.setOwnerRoleId(super.getOwnerRoleId());
			ret.setObjectId(super.getObjectId());
			return ret;
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}

}