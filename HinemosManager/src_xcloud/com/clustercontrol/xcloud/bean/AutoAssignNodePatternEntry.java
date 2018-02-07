/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.model.AutoAssignNodePatternEntryEntity;
import com.clustercontrol.xcloud.util.Cidr;
import com.clustercontrol.xcloud.validation.CustomEntityValidator;
import com.clustercontrol.xcloud.validation.EntityValidator.EntityValidationContext;
import com.clustercontrol.xcloud.validation.ValidationConstants;
import com.clustercontrol.xcloud.validation.annotation.CustomEntityValidation;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Identity;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.Size;


@CustomEntityValidation(AutoAssignNodePatternEntry.PatternValidator.class)
public class AutoAssignNodePatternEntry {
	public static class PatternValidator implements CustomEntityValidator<AutoAssignNodePatternEntry>, ValidationConstants {
		@Override
		public void validate(final AutoAssignNodePatternEntry entity, String group, EntityValidationContext context) throws PluginException {
			switch (entity.getPatternType()) {
			case cidr:
				if (!Cidr.cidrPattern.matcher(entity.getPattern()).matches())
					throw ErrorCode.AUTO_ASSIGN_NODE_UNMATCHED_CIDR_FORMAT.cloudManagerFault(entity.getPattern());
				break;
			case instanceName:
				break;
			}
		}
	}
	
	private String scopeId;
	private AutoAssignNodePatternEntryType patternType;
	private String pattern;
	
	public AutoAssignNodePatternEntry() {
	}
	
	@ElementId("autoAssigneNodePatternEntryType")
	@NotNull
	public AutoAssignNodePatternEntryType getPatternType() {
		return patternType;
	}
	public void setPatternType(AutoAssignNodePatternEntryType patternType) {
		this.patternType = patternType;
	}
	
	@ElementId("scopeId")
	@Identity
	public String getScopeId() {
		return scopeId;
	}
	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}
	
	@ElementId("autoAssigneNodePattern")
	@Size(min=1, max=256)
	@NotNull
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public static AutoAssignNodePatternEntry convertWebEntity(AutoAssignNodePatternEntryEntity entity) {
		AutoAssignNodePatternEntry entry = new AutoAssignNodePatternEntry();
		entry.setPatternType(entity.getPatternType());
		entry.setPattern(entity.getPattern());
		entry.setScopeId(entity.getScopeId());
		return entry;
	}
	
	public static List<AutoAssignNodePatternEntry> convertWebEntities(List<AutoAssignNodePatternEntryEntity> entities) {
		Collections.sort(entities, new Comparator<AutoAssignNodePatternEntryEntity>() {
			@Override
			public int compare(AutoAssignNodePatternEntryEntity o1, AutoAssignNodePatternEntryEntity o2) {
				return o1.getPriority().compareTo(o2.getPriority());
			}
		});
		List<AutoAssignNodePatternEntry> webEntries = new ArrayList<>();
		for (AutoAssignNodePatternEntryEntity entity: entities) {
			webEntries.add(convertWebEntity(entity));
		}
		return webEntries;
	}
}
