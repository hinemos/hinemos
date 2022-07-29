/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ファシリティの関係性情報を保持するクラス
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class OutputFacilityInfo {
	private String facilityId;
	private String facilityName;
	private String parentId;

	public OutputFacilityInfo(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityId() {
		return facilityId;
	}
	public String getFacilityName() {
		return facilityName;
	}
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public static class OutputNodeInfo extends OutputFacilityInfo{

		public OutputNodeInfo(String facilityId) {
			super(facilityId);
		}
	}

	public static class OutputScopeInfo extends OutputFacilityInfo{

		private List<OutputScopeInfo> scopes = new ArrayList<>();
		private List<OutputNodeInfo> nodes = new ArrayList<>();

		public OutputScopeInfo(String facilityId) {
			super(facilityId);
		}
		public List<OutputScopeInfo> getScopes() {
			return scopes;
		}
		public List<OutputNodeInfo> getNodes() {
			return nodes;
		}
	}
	public static class FacilityComparator implements Comparator<Object>, Serializable {
		private static final long serialVersionUID = 1L;

		public int compare(Object arg0, Object arg1) {
			OutputNodeInfo node0 = (OutputNodeInfo) arg0;
			OutputNodeInfo node1 = (OutputNodeInfo) arg1;
			
			return node0.getFacilityId().compareTo(node1.getFacilityId());
		}
	}
}
