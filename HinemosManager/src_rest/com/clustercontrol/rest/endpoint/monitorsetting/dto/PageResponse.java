/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.http.model.Page;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;

@RestBeanConvertIdClassSet(infoClass = Page.class, idName = "id")
public class PageResponse {
	private Integer pageOrderNo;
	private String url;
	private String description;
	private String statusCode;
	private String post;
	@RestBeanConvertEnum
	private PriorityEnum priority;
	private String message;
	private List<PatternResponse> patterns = new ArrayList<>();
	private List<VariableResponse> variables = new ArrayList<>();

	public PageResponse() {
	}

	public Integer getPageOrderNo() {
		return pageOrderNo;
	}
	public void setPageOrderNo(Integer pageOrderNo) {
		this.pageOrderNo = pageOrderNo;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getPost() {
		return post;
	}
	public void setPost(String post) {
		this.post = post;
	}
	public PriorityEnum getPriority() {
		return priority;
	}
	public void setPriority(PriorityEnum priority) {
		this.priority = priority;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<PatternResponse> getPatterns() {
		return patterns;
	}
	public void setPatterns(List<PatternResponse> patterns) {
		this.patterns = patterns;
	}
	public List<VariableResponse> getVariables() {
		return variables;
	}
	public void setVariables(List<VariableResponse> variables) {
		this.variables = variables;
	}
	@Override
	public String toString() {
		return "PageResponse [pageOrderNo=" + pageOrderNo + ", url=" + url + ", description=" + description
				+ ", statusCode=" + statusCode + ", post=" + post + ", priority=" + priority + ", message=" + message
				+ ", patterns=" + patterns + ", variables=" + variables + "]";
	}

	
}
