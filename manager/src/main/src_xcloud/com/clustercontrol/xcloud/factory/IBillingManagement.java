/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.ModifyBillingSettingRequest;
import com.clustercontrol.xcloud.factory.IBillings.PlatformServiceBilling;

public interface IBillingManagement {
	public static class BillingPeriod {
		private Integer targetYear;
		private Integer targetMonth;
		private Long beginTime;
		private Long endTime;
		
		public BillingPeriod(Integer targetYear, Integer targetMonth, Long beginTime, Long endTime) {
			super();
			this.targetYear = targetYear;
			this.targetMonth = targetMonth;
			this.beginTime = beginTime;
			this.endTime = endTime;
		}
		
		public Integer getTargetYear() {
			return targetYear;
		}
		public void setTargetYear(Integer targetYear) {
			this.targetYear = targetYear;
		}
		public Integer getTargetMonth() {
			return targetMonth;
		}
		public void setTargetMonth(Integer targetMonth) {
			this.targetMonth = targetMonth;
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
	}
	
	void updateBillingSetting(ModifyBillingSettingRequest request) throws CloudManagerException;
	void updateBillingDetail() throws CloudManagerException;
	
	default BillingPeriod getBillingPeriod(Integer targetYear, Integer targetMonth) throws CloudManagerException {
		LocalDateTime beginTime = LocalDateTime.of(targetYear, targetMonth, 1, 0, 0);
		LocalDateTime endTime = beginTime.plusMonths(1);
		return new BillingPeriod(targetYear, targetMonth, beginTime.toInstant(ZoneOffset.UTC).toEpochMilli(), endTime.toInstant(ZoneOffset.UTC).toEpochMilli());
	}
	
	PlatformServiceBilling getPlatformServiceBilling(String target) throws CloudManagerException;
	List<String> getPlatformServices() throws CloudManagerException;
}
