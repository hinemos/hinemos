/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

public class ResourceBillingResponse {
	private String accountResourceId;
	private String accountResourceName;
	private String category;
	private String categoryDetail;
	private String displayName;
	private String resourceId;
	private List<DataPointResponse> prices;
	private String unit;

	public ResourceBillingResponse() {
	}

	public String getCloudScopeName() {
		return accountResourceName;
	}

	public void setCloudScopeName(String accountResourceName) {
		this.accountResourceName = accountResourceName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public List<DataPointResponse> getPrices() {
		return prices;
	}

	public void setPrices(List<DataPointResponse> prices) {
		this.prices = prices;
	}

	public String getCloudScopeId() {
		return accountResourceId;
	}

	public void setCloudScopeId(String accountResourceId) {
		this.accountResourceId = accountResourceId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategoryDetail() {
		return categoryDetail;
	}

	public void setCategoryDetail(String categoryDetail) {
		this.categoryDetail = categoryDetail;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
}
