/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class RegistAutoAssigneNodePatternRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_AUTOASSIGNE_NODEPATTERN_ENTRIES)
	@RestValidateObject(notNull = true)
	private List<AutoAssignNodePatternEntryInfoRequest> patterns = new ArrayList<>();
	public RegistAutoAssigneNodePatternRequest() {
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
		for(AutoAssignNodePatternEntryInfoRequest pattern : patterns) {
			pattern.correlationCheck();
		}
	}

	public List<AutoAssignNodePatternEntryInfoRequest> getPatterns() {
		return patterns;
	}
	public void setPatterns(List<AutoAssignNodePatternEntryInfoRequest> patterns) {
		this.patterns = patterns;
	}

}
