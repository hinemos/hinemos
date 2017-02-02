/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.run.util;

import java.util.List;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.notify.monitor.model.EventLogEntity;

public class EventCacheModifyCallback implements JpaTransactionCallback {

	private boolean addFlag = false; // add=true, modify=false
	private EventLogEntity entity = null;
	private Long removeGenerationDate = null;
	private boolean removeAllFlg = false;
	private List<String> facilityIdList = null;
	private List<Integer> priorityList = null;
	private Long outputFromDate = null;
	private Long outputToDate = null;
	private Long generationFromDate = null;
	private Long generationToDate = null;
	private String monitorId = null;
	private String monitorDetailId = null;
	private String application = null;
	private String message = null;
	private Integer confirmFlg = null;
	private String confirmUser = null;
	private String comment = null;
	private String commentUser = null;
	private Integer confirmType = null;
	private Long confirmDate = null;
	private Boolean collectGraphFlg = null;
	private String ownerRoleId = null;
	
	public EventCacheModifyCallback (boolean addFlag, EventLogEntity e) {
		this.addFlag = addFlag;
		this.entity = e;
	}
	public EventCacheModifyCallback(long generationDate, boolean removeAllFlg, String ownerRoleId) {
		this.removeGenerationDate = generationDate;
		this.removeAllFlg = removeAllFlg; // 全消しかどうか(falseの場合はconfirmFlgが1(確認)のもの)
		this.ownerRoleId = ownerRoleId; // nullの場合はownerRoleIdを条件に入れない
	}
	public EventCacheModifyCallback (
			List<String> facilityIdList,
			List<Integer> priorityList,
			Long outputFromDate,
			Long outputToDate,
			Long generationFromDate,
			Long generationToDate,
			String monitorId,
			String monitorDetailId,
			String application,
			String message,
			Integer confirmFlg,
			String confirmUser,
			String comment,
			String commentUser,
			Integer confirmType,
			Long confirmDate,
			Boolean collectGraphFlg,
			String ownerRoleId) {
		this.facilityIdList = facilityIdList;
		this.priorityList = priorityList;
		this.outputFromDate = outputFromDate;
		this.outputToDate = outputToDate;
		this.generationFromDate = generationFromDate;
		this.generationToDate = generationToDate;
		this.monitorId = monitorId;
		this.monitorDetailId = monitorDetailId;
		this.application = application;
		this.message = message;
		this.confirmFlg = confirmFlg;
		this.confirmUser = confirmUser;
		this.comment = comment;
		this.commentUser = commentUser;
		this.confirmType = confirmType;
		this.confirmDate = confirmDate;
		this.collectGraphFlg = collectGraphFlg;
		this.ownerRoleId = ownerRoleId;
	}
	
	@Override
	public void preBegin() { }

	@Override
	public void postBegin() { }

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		if (this.removeGenerationDate != null) {
			EventCache.removeEventCache(removeGenerationDate, removeAllFlg, ownerRoleId);
		} else if (addFlag) {
			EventCache.addEventCache(entity);
		} else {
			if (entity != null) {
				EventCache.modifyEventCache(entity);
			} else {
				EventCache.confirmEventCache(facilityIdList, priorityList, outputFromDate,
						outputToDate, generationFromDate, generationToDate, monitorId,
						monitorDetailId, application, message, confirmFlg, confirmUser,
						comment, commentUser, confirmType, confirmDate, collectGraphFlg, ownerRoleId);
			}
		}
	}

	@Override
	public void preRollback() { }

	@Override
	public void postRollback() { }

	@Override
	public void preClose() { }

	@Override
	public void postClose() {}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (addFlag ? 1231 : 1237);
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((collectGraphFlg == null) ? 0 : collectGraphFlg.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((commentUser == null) ? 0 : commentUser.hashCode());
		result = prime * result + ((confirmDate == null) ? 0 : confirmDate.hashCode());
		result = prime * result + ((confirmFlg == null) ? 0 : confirmFlg.hashCode());
		result = prime * result + ((confirmType == null) ? 0 : confirmType.hashCode());
		result = prime * result + ((confirmUser == null) ? 0 : confirmUser.hashCode());
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((facilityIdList == null) ? 0 : facilityIdList.hashCode());
		result = prime * result + ((generationFromDate == null) ? 0 : generationFromDate.hashCode());
		result = prime * result + ((generationToDate == null) ? 0 : generationToDate.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((monitorDetailId == null) ? 0 : monitorDetailId.hashCode());
		result = prime * result + ((monitorId == null) ? 0 : monitorId.hashCode());
		result = prime * result + ((outputFromDate == null) ? 0 : outputFromDate.hashCode());
		result = prime * result + ((outputToDate == null) ? 0 : outputToDate.hashCode());
		result = prime * result + ((ownerRoleId == null) ? 0 : ownerRoleId.hashCode());
		result = prime * result + ((priorityList == null) ? 0 : priorityList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventCacheModifyCallback other = (EventCacheModifyCallback) obj;
		if (addFlag != other.addFlag)
			return false;
		if (application == null) {
			if (other.application != null)
				return false;
		} else if (!application.equals(other.application))
			return false;
		if (collectGraphFlg == null) {
			if (other.collectGraphFlg != null)
				return false;
		} else if (!collectGraphFlg.equals(other.collectGraphFlg))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
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
		if (confirmType == null) {
			if (other.confirmType != null)
				return false;
		} else if (!confirmType.equals(other.confirmType))
			return false;
		if (confirmUser == null) {
			if (other.confirmUser != null)
				return false;
		} else if (!confirmUser.equals(other.confirmUser))
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		if (facilityIdList == null) {
			if (other.facilityIdList != null)
				return false;
		} else if (!facilityIdList.equals(other.facilityIdList))
			return false;
		if (generationFromDate == null) {
			if (other.generationFromDate != null)
				return false;
		} else if (!generationFromDate.equals(other.generationFromDate))
			return false;
		if (generationToDate == null) {
			if (other.generationToDate != null)
				return false;
		} else if (!generationToDate.equals(other.generationToDate))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (monitorDetailId == null) {
			if (other.monitorDetailId != null)
				return false;
		} else if (!monitorDetailId.equals(other.monitorDetailId))
			return false;
		if (monitorId == null) {
			if (other.monitorId != null)
				return false;
		} else if (!monitorId.equals(other.monitorId))
			return false;
		if (outputFromDate == null) {
			if (other.outputFromDate != null)
				return false;
		} else if (!outputFromDate.equals(other.outputFromDate))
			return false;
		if (outputToDate == null) {
			if (other.outputToDate != null)
				return false;
		} else if (!outputToDate.equals(other.outputToDate))
			return false;
		if (ownerRoleId == null) {
			if (other.ownerRoleId != null)
				return false;
		} else if (!ownerRoleId.equals(other.ownerRoleId))
			return false;
		if (priorityList == null) {
			if (other.priorityList != null)
				return false;
		} else if (!priorityList.equals(other.priorityList))
			return false;
		return true;
	}
	
}
