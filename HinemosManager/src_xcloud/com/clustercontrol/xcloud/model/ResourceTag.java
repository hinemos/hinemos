/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.clustercontrol.xcloud.bean.TagType;

@Embeddable
public class ResourceTag {
	private TagType tagType;
	private String key;
	private String value;

	@Column(name="tag_key")
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	@Column(name="tag_type")
	@Enumerated(EnumType.STRING)
	public TagType getTagType() {
		return tagType;
	}
	public void setTagType(TagType tagType) {
		this.tagType = tagType;
	}

	@Column(name="value")
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
