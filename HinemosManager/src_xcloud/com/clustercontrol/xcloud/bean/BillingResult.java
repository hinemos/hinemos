/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.List;

public class BillingResult {

	public static enum TargetType {
		Facility,
		CloudScope
	}
	
	public static class FacilityBilling {
		private String facilityId;
		private String facilityName;
		private List<DataPoint> totalsPerDate;
		private List<ResourceBilling> resources;

		public FacilityBilling() {
			super();
		}

		public FacilityBilling(String facilityId, String facilityName,
				List<DataPoint> totalsPerDate, List<ResourceBilling> resources) {
			super();
			this.facilityId = facilityId;
			this.facilityName = facilityName;
			this.totalsPerDate = totalsPerDate;
			this.resources = resources;
		}

		public String getFacilityId() {
			return facilityId;
		}
		public void setFacilityId(String facilityId) {
			this.facilityId = facilityId;
		}

		public String getFacilityName() {
			return facilityName;
		}
		public void setFacilityName(String facilityName) {
			this.facilityName = facilityName;
		}

		public List<DataPoint> getTotalsPerDate() {
			return totalsPerDate;
		}
		public void setTotalsPerDate(List<DataPoint> totalsPerDate) {
			this.totalsPerDate = totalsPerDate;
		}

		public List<ResourceBilling> getResources() {
			return resources;
		}
		public void setResources(List<ResourceBilling> resources) {
			this.resources = resources;
		}

		@Override
		public String toString() {
			return "FacilityBilling [facilityId=" + facilityId + ", facilityName=" + facilityName + ", totalsPerDate="
					+ totalsPerDate + ", resources=" + resources + "]";
		}
	}

	public static class ResourceBilling {
		public ResourceBilling() {
			super();
		}
		public ResourceBilling(String accountResourceId, String accountResourceName, String category, String categoryDetail,
				String displayName, String resourceId, List<DataPoint> prices) {
			super();
			this.setCloudScopeId(accountResourceId);
			this.accountResourceName = accountResourceName;
			this.category = category; 
			this.categoryDetail = categoryDetail;
			this.displayName = displayName;
			this.prices = prices;
		}
		private String accountResourceId;
		private String accountResourceName;
		private String category;
		private String categoryDetail;
		private String displayName;
		private String resourceId;
		private List<DataPoint> prices;
		private String unit;

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
		public List<DataPoint> getPrices() {
			return prices;
		}
		public void setPrices(List<DataPoint> prices) {
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
		@Override
		public String toString() {
			return "ResourceBilling [accountResourceId=" + accountResourceId + ", accountResourceName="
					+ accountResourceName + ", category=" + category + ", categoryDetail=" + categoryDetail
					+ ", displayName=" + displayName + ", resourceId=" + resourceId + ", prices=" + prices + ", unit="
					+ unit + "]";
		}
	}

	public static class DataPoint {
		private int month;
		private int day;
		private double price;

		public DataPoint() {
			super();
		}
		
		public DataPoint(int month, int date, double price) {
			super();
			this.month = month;
			this.day = date;
			this.price = price;
		}

		public int getDay() {
			return day;
		}
		public void setDay(int day) {
			this.day = day;
		}
		
		public double getPrice() {
			return price;
		}
		public void setPrice(double price) {
			this.price = price;
		}

		public int getMonth() {
			return month;
		}
		public void setMonth(int month) {
			this.month = month;
		}

		@Override
		public String toString() {
			return "DataPoint [month=" + month + ", day=" + day + ", price=" + price + "]";
		}
	}
	
	private TargetType type;
	private String targetId;
	private String targetName;
	private Integer targetYear;
	private Integer targetMonth;
	private Long beginTime;
	private Long endTime;
	private List<FacilityBilling> facilities;
	private String unit;

	public BillingResult() {
		super();
	}
	
	public BillingResult(TargetType type, String targetId, String targetName,
			Integer targetYear, Integer targetMonth, Long beginTime, Long endTime,
			List<FacilityBilling> facilities, String unit) {
		super();
		this.type = type;
		this.targetId = targetId;
		this.targetName = targetName;
		this.targetYear = targetYear;
		this.targetMonth = targetMonth;
		this.facilities = facilities;
		this.unit = unit;
		this.targetYear = targetYear;
		this.targetMonth = targetMonth;
		this.beginTime = beginTime;
		this.endTime = endTime;
	}
	
	public Integer getTargetMonth() {
		return targetMonth;
	}
	public void setTargetMonth(Integer targetMonth) {
		this.targetMonth = targetMonth;
	}

	public List<FacilityBilling> getFacilities() {
		return facilities;
	}
	public void setFacilities(List<FacilityBilling> facilities) {
		this.facilities = facilities;
	}
	
	@Deprecated
	public String getUnit() {
		return unit;
	}
	@Deprecated
	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getTargetId() {
		return targetId;
	}
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public TargetType getType() {
		return type;
	}
	public void setType(TargetType type) {
		this.type = type;
	}

	public Integer getTargetYear() {
		return targetYear;
	}
	public void setTargetYear(Integer targetYear) {
		this.targetYear = targetYear;
	}

	public Long getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(Long beginTime) {
		this.beginTime = beginTime;
	}

	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "BillingResult [type=" + type + ", targetId=" + targetId + ", targetName=" + targetName + ", targetYear="
				+ targetYear + ", targetMonth=" + targetMonth + ", beginTime=" + beginTime + ", endTime=" + endTime
				+ ", facilities=" + facilities + ", unit=" + unit + "]";
	}
}
