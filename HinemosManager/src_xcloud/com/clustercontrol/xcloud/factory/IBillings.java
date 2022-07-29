/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.io.File;
import java.util.List;

import javax.activation.DataHandler;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.BillingResult;
import com.clustercontrol.xcloud.model.CloudScopeEntity;

public interface IBillings {
	public static class PlatformServiceBilling {
		private String currency;
		private double price;
		private Long updateDate;
		private boolean valid;

		public double getPrice() {
			return price;
		}
		public void setPrice(double price) {
			this.price = price;
		}
		public String getCurrency() {
			return currency;
		}
		public void setCurrency(String currency) {
			this.currency = currency;
		}
		public Long getUpdateDate() {
			return updateDate;
		}
		public void setUpdateDate(Long updateDate) {
			this.updateDate = updateDate;
		}
		public boolean isValid() {
			return valid;
		}
		public void setValid(boolean valid) {
			this.valid = valid;
		}
	}	
	BillingResult getBillingDetailsByCloudScope(String cloudScopeId, Integer year, Integer month) throws CloudManagerException, InvalidRole;
	BillingResult getBillingDetailsByFacility(String facilityId, Integer year, Integer month) throws CloudManagerException, InvalidRole;
	DataHandler downloadBillingDetailsByCloudScope(String cloudScopeId, Integer year, Integer month) throws CloudManagerException, InvalidRole;
	DataHandler downloadBillingDetailsByFacility(String facilityId, Integer year, Integer month) throws CloudManagerException, InvalidRole;
	
	String writeBillingDetailsByCloudScope(String cloudScopeId, Integer year, Integer month, File tempFile) throws CloudManagerException, InvalidRole;

	String writeBillingDetailsByFacility(String facilityId, Integer year, Integer month, File tempFile) throws CloudManagerException, InvalidRole;

	void refreshBillingDetails(String cloudScopeId) throws CloudManagerException, InvalidRole;
	
	List<String> getPlatformServices(String cloudScopeId) throws CloudManagerException, InvalidRole;
	PlatformServiceBilling getPlatformServiceBilling(CloudScopeEntity cloudScope, String service) throws CloudManagerException;
}
