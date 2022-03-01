/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlType;

/**
 * ジョブの待ち条件グループに関する情報を保持するクラス<BR>
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobObjectGroupInfo implements Serializable, Comparable<JobObjectGroupInfo>, Cloneable {

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -4050301670424654620L;

	/** 待ち条件群の順番 */
	private Integer orderNo;

	/** AND/OR */
	private Integer conditionType = ConditionTypeConstant.TYPE_AND;

	/** 複数待ち条件を持つか */
	private Boolean isGroup = false;
	
	/** 待ち条件 */
	private ArrayList<JobObjectInfo> jobObjectList = new ArrayList<>();

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public Integer getConditionType() {
		return conditionType;
	}

	public void setConditionType(Integer conditionType) {
		this.conditionType = conditionType;
	}

	public Boolean getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(Boolean isGroup) {
		this.isGroup = isGroup;
	}

	public ArrayList<JobObjectInfo> getJobObjectList() {
		return jobObjectList;
	}

	public void setJobObjectList(ArrayList<JobObjectInfo> jobObjectList) {
		this.jobObjectList = jobObjectList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((orderNo == null) ? 0 : orderNo.hashCode());
		result = prime * result + ((conditionType == null) ? 0 : conditionType.hashCode());
		result = prime * result + ((isGroup == null) ? 0 : isGroup.hashCode());
		result = prime * result + ((jobObjectList == null) ? 0 : jobObjectList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobObjectGroupInfo)) {
			return false;
		}
		JobObjectGroupInfo o1 = this;
		JobObjectGroupInfo o2 = (JobObjectGroupInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getOrderNo(), o2.getOrderNo()) &&
				equalsSub(o1.getConditionType(), o2.getConditionType()) &&
				equalsSub(o1.getIsGroup(), o2.getIsGroup()) &&
				equalsArray(o1.getJobObjectList(), o2.getJobObjectList());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		
		if (o1 == null)
			return false;
		
		return o1.equals(o2);
	}

	private boolean equalsArray(ArrayList<?> list1, ArrayList<?> list2) {
		if (list1 != null && !list1.isEmpty()) {
			if (list2 != null && list1.size() == list2.size()) {
				Object[] ary1 = list1.toArray();
				Object[] ary2 = list2.toArray();
				Arrays.sort(ary1);
				Arrays.sort(ary2);

				for (int i = 0; i < ary1.length; i++) {
					if (!ary1[i].equals(ary2[i])) {
						return false;
					}
				}
				return true;
			}
		} else if (list2 == null || list2.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public int compareTo(JobObjectGroupInfo o) {
		return this.orderNo.compareTo(o.orderNo);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		JobObjectGroupInfo jobObjectGroupInfo = (JobObjectGroupInfo) super.clone();
		if (this.jobObjectList != null) {
			jobObjectGroupInfo.jobObjectList = new ArrayList<JobObjectInfo>();
			Iterator<JobObjectInfo> iterator = this.jobObjectList.iterator();
			while (iterator.hasNext()) {
				jobObjectGroupInfo.jobObjectList.add((JobObjectInfo) iterator.next().clone());
			}
		}
		return jobObjectGroupInfo;
	}
}