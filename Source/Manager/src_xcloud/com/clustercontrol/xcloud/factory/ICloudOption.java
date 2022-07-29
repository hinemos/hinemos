/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.Date;

import org.apache.log4j.Logger;

import com.clustercontrol.poller.IPoller;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.AddCloudScopeRequest;
import com.clustercontrol.xcloud.bean.ModifyCloudScopeRequest;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudPlatformEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.LocationEntity;


public interface ICloudOption {
	public static enum PlatformServiceStatus {
		normal,
		warn,
		abnormal,
		unknown,
		exception;
	}
	
	public interface ICloudScopeListener {
		public void postAddCloudScope(CloudScopeEntity cloudScope, AddCloudScopeRequest request) throws CloudManagerException;
		public void postModifyCloudScope(CloudScopeEntity cloudScope, ModifyCloudScopeRequest request) throws CloudManagerException;
		public void postRemoveCloudScope(CloudScopeEntity cloudScope) throws CloudManagerException;
	}

	public static class PlatformServiceCondition {
		private String serviceId;
		private String serviceName;
		private PlatformServiceStatus status; 
		private String message;
		private String detail;
		private Date monitorDate;
		
		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			// DBの桁数にあわせてカットする
			if(serviceId.length() > 64) {
				Logger.getLogger(ICloudOption.class).debug("Cut off for long messages. Original serviceId is " + serviceId);
				this.serviceId = serviceId.substring(0, 64);
			} else {
				this.serviceId = serviceId;
			}
		}
		public String getServiceName() {
			return serviceName;
		}
		public void setServiceName(String serviceName) {
			// DBの桁数にあわせてカットする
			if(serviceName.length() > 64) {
				Logger.getLogger(ICloudOption.class).debug("Cut off for long messages. Original serviceName is " + serviceName);
				this.serviceName = serviceName.substring(0, 64);
			} else {
				this.serviceName = serviceName;
			}
		}
		
		public PlatformServiceStatus getStatus() {
			return status;
		}
		public void setStatus(PlatformServiceStatus status) {
			this.status = status;
		}
		
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		
		public String getDetail() {
			return detail;
		}
		public void setDetail(String detail) {
			this.detail = detail;
		}
		
		public Date getMonitorDate() {
			return monitorDate;
		}
		public void setMonitorDate(Date monitorDate) {
			this.monitorDate = monitorDate;
		}
	}
	
	public interface IVisitor {
		void visit(IPrivateCloudOption cloudOption) throws CloudManagerException;
		void visit(IPublicCloudOption cloudOption) throws CloudManagerException;
	}
	
	public interface ITransformer<T> {
		T transform(IPrivateCloudOption cloudOption) throws CloudManagerException;
		T transform(IPublicCloudOption cloudOption) throws CloudManagerException;
	}
	
	CloudPlatformEntity getPlatform();

	ICloudSpec getCloudSpec();
	
	ICloudScopeListener getCloudScopeListener();

	IResourceManagement getResourceManagement(LocationEntity location, CloudLoginUserEntity user);
	
	IUserManagement getUserManagement(CloudScopeEntity scope);
	
	IPlatformServiceMonitor getPlatformServiceMonitor();
	
	IBillingManagement getBillingManagement(CloudScopeEntity cloudScope);
	
	IPoller getPoller(CloudScopeEntity cloudscope);
	
	void start();
	
	void stop();
	
	public abstract void visit(IVisitor visitor) throws CloudManagerException;
	
	public abstract <T> T transform(ITransformer<T> tranformer) throws CloudManagerException;
}
