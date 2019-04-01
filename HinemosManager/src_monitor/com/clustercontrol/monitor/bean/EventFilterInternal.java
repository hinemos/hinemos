/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * マネージャ内部で使用するイベントのフィルタ条件の抽象クラス
 *
 */
public abstract class EventFilterInternal<T> {
	
	private List<String> facilityIdList;
	private Integer facilityType;
	private List<Integer> priorityList;
	private Long outputFromDate;
	private Long outputToDate;
	private Long generationFromDate;
	private Long generationToDate;
	private String monitorId;
	private String monitorDetailId;
	private String application;
	private String message;
	private String comment;
	private String commentUser;
	private Boolean collectGraphFlg;
	
	public EventFilterInternal() {
	}
	
	public abstract void setFilter(String facilityId, T filter) throws HinemosUnknown;
	
	public List<String> getFacilityIdList() {
		return facilityIdList;
	}
	public void setFacilityIdList(List<String> facilityIdList) {
		this.facilityIdList = facilityIdList;
	}
	public List<Integer> getPriorityList() {
		return priorityList;
	}
	public void setPriorityList(List<Integer> priorityList) {
		this.priorityList = priorityList;
	}
	public Long getOutputFromDate() {
		return outputFromDate;
	}
	public void setOutputFromDate(Long outputFromDate) {
		this.outputFromDate = outputFromDate;
	}
	public Long getOutputToDate() {
		return outputToDate;
	}
	public void setOutputToDate(Long outputToDate) {
		this.outputToDate = outputToDate;
	}
	public Long getGenerationFromDate() {
		return generationFromDate;
	}
	public void setGenerationFromDate(Long generationFromDate) {
		this.generationFromDate = generationFromDate;
	}
	public Long getGenerationToDate() {
		return generationToDate;
	}
	public void setGenerationToDate(Long generationToDate) {
		this.generationToDate = generationToDate;
	}
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public String getMonitorDetailId() {
		return monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getCommentUser() {
		return commentUser;
	}
	public void setCommentUser(String commentUser) {
		this.commentUser = commentUser;
	}
	public Boolean getCollectGraphFlg() {
		return collectGraphFlg;
	}
	public void setCollectGraphFlg(Boolean collectGraphFlg) {
		this.collectGraphFlg = collectGraphFlg;
	}
	
	public Integer getFacilityType() {
		return facilityType;
	}
	public void setFacilityType(Integer facilityType) {
		this.facilityType = facilityType;
	}
	
	protected static String convEmptyToNull(String value) {
		if ("".equals(value)) {
			return null;
		} else {
			return value;
		}
	}
	
	protected static Long convFromDate(Long value) {
		if (value == null) {
			return null;
		} else {
			value -= (value % 1000);	//ミリ秒の桁を0にする
			return value;
		}
	}
	
	protected static Long convToDate(Long value) {
		if (value == null) {
			return null;
		} else {
			value += (999 - (value % 1000));	//ミリ秒の桁を999にする
			return value;
		}
	}
	
	protected static List<String> getFacilityIds(String facilityId, Integer facilityType) throws HinemosUnknown {
		if (facilityId == null || "".equals(facilityId)) {
			//ファシリティIDの指定が無いとき、すべてのファシリティIDが対象
			return null;
		}
		
		int lFacilityType = FacilityTargetConstant.TYPE_BENEATH;
		
		if (facilityType != null) {
			lFacilityType = facilityType;
		}
		
		// 対象ファシリティのファシリティIDを取得
		int level = RepositoryControllerBean.ALL;
		if (FacilityTargetConstant.TYPE_BENEATH == lFacilityType) {
			level = RepositoryControllerBean.ONE_LEVEL;
		}
		
		List<String> facilityIdList = null;
		
		facilityIdList = new RepositoryControllerBean().getFacilityIdList(facilityId, level);
		
		if (facilityIdList != null && facilityIdList.size() > 0) {
			// スコープの場合
			if (facilityId.equals(RoleSettingTreeConstant.ROOT_ID)) {
				//ルートスコープが選択されている場合、ファシリティＩＤ＝空白を追加
				//ファシリティＩＤ＝空白はジョブネットが起点の通知が該当する
				facilityIdList.add("");
			}
		} else {
			// ノードの場合
			facilityIdList = new ArrayList<String>();
			facilityIdList.add(facilityId);
		}
		
		return facilityIdList;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facilityIdList == null) ? 0 : facilityIdList.hashCode());
		result = prime * result + ((facilityType == null) ? 0 : facilityType.hashCode());
		result = prime * result + ((priorityList == null) ? 0 : priorityList.hashCode());
		result = prime * result + ((outputFromDate == null) ? 0 : outputFromDate.hashCode());
		result = prime * result + ((outputToDate == null) ? 0 : outputToDate.hashCode());
		result = prime * result + ((generationFromDate == null) ? 0 : generationFromDate.hashCode());
		result = prime * result + ((generationToDate == null) ? 0 : generationToDate.hashCode());
		result = prime * result + ((monitorId == null) ? 0 : monitorId.hashCode());
		result = prime * result + ((monitorDetailId == null) ? 0 : monitorDetailId.hashCode());
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((commentUser == null) ? 0 : commentUser.hashCode());
		result = prime * result + ((collectGraphFlg == null) ? 0 : collectGraphFlg.hashCode());
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
		EventFilterInternal<?> other = (EventFilterInternal<?>) obj;
		if (!isEquals(facilityIdList, other.facilityIdList)) {return false;}
		if (!isEquals(facilityType, other.facilityType)) {return false;}
		if (!isEquals(priorityList, other.priorityList)) {return false;}
		if (!isEquals(outputFromDate, other.outputFromDate)) {return false;}
		if (!isEquals(outputToDate, other.outputToDate)) {return false;}
		if (!isEquals(generationFromDate, other.generationFromDate)) {return false;}
		if (!isEquals(generationToDate, other.generationToDate)) {return false;}
		if (!isEquals(monitorId, other.monitorId)) {return false;}
		if (!isEquals(monitorDetailId, other.monitorDetailId)) {return false;}
		if (!isEquals(application, other.application)) {return false;}
		if (!isEquals(message, other.message)) {return false;}
		if (!isEquals(comment, other.comment)) {return false;}
		if (!isEquals(commentUser, other.commentUser)) {return false;}
		if (!isEquals(collectGraphFlg, other.collectGraphFlg)) {return false;}
		
		return true;
	}
	
	protected static boolean isEquals(Object o1, Object o2) {
		if (o1 == null) {
			if (o2 != null) {
				return false;
			}
			return true;
		} else {
			return !o1.equals(o2);
		}
	}
	
}
