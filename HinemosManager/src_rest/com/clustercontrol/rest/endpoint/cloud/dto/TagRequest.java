/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.bean.TagType;

public class TagRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_TAG_TYPE)
	@RestValidateObject(notNull = true)
	private TagType tagType;

	@RestItemName(MessageConstant.XCLOUD_CORE_TAG_KEY)
	@RestValidateString(notNull = true, minLen = 1)
	private String key;

	@RestItemName(MessageConstant.XCLOUD_CORE_TAG_VALUE)
	private String value;

	public TagRequest() {
	}

	public TagType getTagType() {
		return tagType;
	}

	public void setTagType(TagType tagType) {
		this.tagType = tagType;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
