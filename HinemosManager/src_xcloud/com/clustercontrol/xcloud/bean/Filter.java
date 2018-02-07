/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.Arrays;
import java.util.List;

import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.NotNullContainer;

public class Filter {
	private String name;
	private List<String> values;

	public Filter() {
	}

	public Filter(String name, String... values) {
		this.name = name;
		this.values = Arrays.asList(values);
	}

	public Filter(String name, List<String> values) {
		this.name = name;
		this.values = values;
	}
	@ElementId("Filter_name")
	@NotNull
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ElementId("Filter_value")
	//	@ListElement
	@NotNullContainer
	@NotNull
	public List<String> getValues() {
		return values;
	}
	public void setValues(List<String> values) {
		this.values = values;
	}
	
	public static Filter apply(String name, String... values) {
		return new Filter(name, values);
	}
	
	public static Filter apply(String name, List<String> values) {
		return new Filter(name, values);
	}
}
