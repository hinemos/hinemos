/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;


/**
 * 
 * ステータス情報を保持するDTOです。<BR>
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class StatusDataInfo implements Serializable {

	private static final long serialVersionUID = 5615298892458986612L;
	private String monitorId = null;
	private String monitorDetailId = null;
	private String pluginId = null;
	private String facilityId = null;
	private String application = null;
	private Long expirationDate = null;
	private Integer expirationFlg = null;
	private Long generationDate = null;
	private String message = null;
	private Long outputDate = null;
	private Integer priority = null;
	private String facilityPath = null;
	private String ownerRoleId = null;

	public StatusDataInfo() {
		super();
	}

	public StatusDataInfo(String monitorId,
			String pluginId, String monitorDetailId, String facilityId,
			String application, Long expirationDate,
			Integer expirationFlg, Long generationDate,
			String message, Long outputDate,
			Integer priority, String ownerRoleId) {
		setMonitorId(monitorId);
		setMonitorDetailId(monitorDetailId);
		setPluginId(pluginId);
		setFacilityId(facilityId);
		setApplication(application);
		setExpirationDate(expirationDate);
		setExpirationFlg(expirationFlg);
		setGenerationDate(generationDate);
		setMessage(message);
		setOutputDate(outputDate);
		setPriority(priority);
		setOwnerRoleId(ownerRoleId);
	}

	public StatusDataInfo(StatusDataInfo otherData) {
		setMonitorId(otherData.getMonitorId());
		setMonitorDetailId(otherData.getMonitorDetailId());
		setPluginId(otherData.getPluginId());
		setFacilityId(otherData.getFacilityId());
		setApplication(otherData.getApplication());
		setExpirationDate(otherData.getExpirationDate());
		setExpirationFlg(otherData.getExpirationFlg());
		setGenerationDate(otherData.getGenerationDate());
		setMessage(otherData.getMessage());
		setOutputDate(otherData.getOutputDate());
		setPriority(otherData.getPriority());
		setOwnerRoleId(otherData.getOwnerRoleId());
	}

	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public String getPluginId() {
		return this.pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Long getExpirationDate() {
		return this.expirationDate;
	}

	public void setExpirationDate(Long expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Integer getExpirationFlg() {
		return this.expirationFlg;
	}

	public void setExpirationFlg(Integer expirationFlg) {
		this.expirationFlg = expirationFlg;
	}

	public Long getGenerationDate() {
		return this.generationDate;
	}

	public void setGenerationDate(Long generationDate) {
		this.generationDate = generationDate;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getOutputDate() {
		return this.outputDate;
	}

	public void setOutputDate(Long outputDate) {
		this.outputDate = outputDate;
	}

	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("monitorId=" + getMonitorId() + " " + "monitorDetailId=" + getMonitorDetailId() + " " + "pluginId="
				+ getPluginId() + " " + "facilityId=" + getFacilityId() + " "
				+ "application=" + getApplication() + " " + "expirationDate="
				+ getExpirationDate() + " " + "expirationFlg="
				+ getExpirationFlg() + " " + "generationDate="
				+ getGenerationDate() + " " + "message=" + getMessage() + " "
				+ "outputDate=" + getOutputDate() + " " + "priority=" + getPriority()
				+ " " + "ownerRoleID=" + getOwnerRoleId());
		str.append('}');

		return (str.toString());
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((application == null) ? 0 : application.hashCode());
		result = prime * result
				+ ((expirationDate == null) ? 0 : expirationDate.hashCode());
		result = prime * result
				+ ((expirationFlg == null) ? 0 : expirationFlg.hashCode());
		result = prime * result
				+ ((facilityId == null) ? 0 : facilityId.hashCode());
		result = prime * result
				+ ((facilityPath == null) ? 0 : facilityPath.hashCode());
		result = prime * result
				+ ((generationDate == null) ? 0 : generationDate.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result
				+ ((monitorDetailId == null) ? 0 : monitorDetailId.hashCode());
		result = prime * result
				+ ((monitorId == null) ? 0 : monitorId.hashCode());
		result = prime * result
				+ ((outputDate == null) ? 0 : outputDate.hashCode());
		result = prime * result
				+ ((ownerRoleId == null) ? 0 : ownerRoleId.hashCode());
		result = prime * result
				+ ((pluginId == null) ? 0 : pluginId.hashCode());
		result = prime * result
				+ ((priority == null) ? 0 : priority.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatusDataInfo other = (StatusDataInfo) obj;
		if (application == null) {
			if (other.application != null)
				return false;
		} else if (!application.equals(other.application))
			return false;
		if (expirationDate == null) {
			if (other.expirationDate != null)
				return false;
		} else if (!expirationDate.equals(other.expirationDate))
			return false;
		if (expirationFlg == null) {
			if (other.expirationFlg != null)
				return false;
		} else if (!expirationFlg.equals(other.expirationFlg))
			return false;
		if (facilityId == null) {
			if (other.facilityId != null)
				return false;
		} else if (!facilityId.equals(other.facilityId))
			return false;
		if (facilityPath == null) {
			if (other.facilityPath != null)
				return false;
		} else if (!facilityPath.equals(other.facilityPath))
			return false;
		if (generationDate == null) {
			if (other.generationDate != null)
				return false;
		} else if (!generationDate.equals(other.generationDate))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (monitorDetailId == null) {
			if (other.monitorDetailId != null)
				return false;
		} else if (!monitorDetailId.equals(other.monitorDetailId))
			return false;
		if (monitorId == null) {
			if (other.monitorId != null)
				return false;
		} else if (!monitorId.equals(other.monitorId))
			return false;
		if (outputDate == null) {
			if (other.outputDate != null)
				return false;
		} else if (!outputDate.equals(other.outputDate))
			return false;
		if (ownerRoleId == null) {
			if (other.ownerRoleId != null)
				return false;
		} else if (!ownerRoleId.equals(other.ownerRoleId))
			return false;
		if (pluginId == null) {
			if (other.pluginId != null)
				return false;
		} else if (!pluginId.equals(other.pluginId))
			return false;
		if (priority == null) {
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
			return false;
		return true;
	}

	/**
	 * ファシリティパスを返します
	 * 
	 * @return 重要度
	 * @ejb.interface-method
	 * 
	 */
	public String getFacilityPath() {
		return facilityPath;
	}

	/**
	 * ファシリティパスを設定します
	 * 
	 * @param facilityPath
	 * @ejb.interface-method
	 * 
	 */
	public void setFacilityPath(String facilityPath) {
		this.facilityPath = facilityPath;
	}

	/**
	 * オーナーロールIDを返します
	 * 
	 * @return オーナーロールID
	 * 
	 */
	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	/**
	 * オーナーロールIDを設定します
	 * 
	 * @param ownerRoleId
	 * 
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}


}
