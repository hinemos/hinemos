/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.clustercontrol.xcloud.bean.AutoAssignNodePatternEntryType;


@NamedQueries({
	@NamedQuery(
			name="findAutoAssigneNodePatternsByCloudScopeId",
			query="SELECT p FROM AutoAssignNodePatternEntryEntity p WHERE p.cloudScopeId = :cloudScopeId"
			)
})
@Entity
@Table(name="cc_cfg_xcloud_auto_assigne_node_pattern_entry", schema="setting")
@IdClass(AutoAssignNodePatternEntryEntity.AutoAssigneNodePatternEntryEntityPK.class)
public class AutoAssignNodePatternEntryEntity {
	public static class AutoAssigneNodePatternEntryEntityPK {
		private String cloudScopeId;
		private Integer priority;
		
		public AutoAssigneNodePatternEntryEntityPK() {
		}
		
		public AutoAssigneNodePatternEntryEntityPK(String cloudScopeId, Integer priority) {
			this.cloudScopeId = cloudScopeId;
			this.priority = priority;
		}

		public String getCloudScopeId() {
			return cloudScopeId;
		}
		public void setCloudScopeId(String cloudScopeId) {
			this.cloudScopeId = cloudScopeId;
		}
		
		public Integer getPriority() {
			return priority;
		}
		public void setPriority(Integer priority) {
			this.priority = priority;
		}
	}
	
	private String cloudScopeId;
	private String scopeId;
	private Integer priority;
	private AutoAssignNodePatternEntryType patternType;
	private String pattern;
	
	public AutoAssignNodePatternEntryEntity() {
	}
	
	@Id
	@Column(name="cloud_scope_id")
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	
	@Column(name="scope_id")
	public String getScopeId() {
		return scopeId;
	}
	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}

	@Id
	@Column(name="priority")
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Column(name="pattern_type")
	@Enumerated(EnumType.STRING)
	public AutoAssignNodePatternEntryType getPatternType() {
		return patternType;
	}
	public void setPatternType(AutoAssignNodePatternEntryType patternType) {
		this.patternType = patternType;
	}

	@Column(name="pattern")
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
