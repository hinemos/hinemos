/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.Size;

public class Tag {
	private TagType tagType;
	private String key;
	private String value;
	
	public Tag() {
	}
	public Tag(TagType tagType, String key, String value) {
		this.tagType = tagType;
		this.key = key;
		this.value = value;
	}
	@ElementId("XCLOUD_CORE_TAG_TYPE")
	@NotNull
	public TagType getTagType() {
		return tagType;
	}
	public void setTagType(TagType tagType) {
		this.tagType = tagType;
	}
	@ElementId("XCLOUD_CORE_TAG_KEY")
	@NotNull
	@Size(min=1)
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	@ElementId("XCLOUD_CORE_TAG_VALUE")
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public static Tag convertWebEntity(com.clustercontrol.xcloud.model.ResourceTag entity) {
		Tag tag = new Tag();
		tag.setTagType(entity.getTagType());
		tag.setKey(entity.getKey());
		tag.setValue(entity.getValue());
		return tag;
	}

	public static List<Tag> convertWebEntities(List<com.clustercontrol.xcloud.model.ResourceTag> entities) {
		List<Tag> tags = new ArrayList<>();
		for (com.clustercontrol.xcloud.model.ResourceTag entity: entities) {
			tags.add(convertWebEntity(entity));
		}
		return tags;
	}
}
