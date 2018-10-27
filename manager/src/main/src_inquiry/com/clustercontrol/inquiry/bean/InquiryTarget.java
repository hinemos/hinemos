/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.inquiry.bean;

/**
 * 
 * 遠隔管理機能の対象情報。
 * 
 */
public class InquiryTarget implements Cloneable {
	private String id;
	private String name;
	private Long starTime;
	private Long endTime;
	private TargetStatus status;
	
	private String fileName;
	private String command;
	
	public InquiryTarget() {
	}
	
	public InquiryTarget(InquiryTarget other) {
		setId(other.getId());
		setDisplayName(other.getDisplayName());
		setStarTime(other.getStarTime());
		setEndTime(other.getEndTime());
		setStatus(other.getStatus());
		setCommand(other.getCommand());
		setFileName(other.getFileName());
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getDisplayName() {
		return name;
	}
	public void setDisplayName(String name) {
		this.name = name;
	}
	
	public Long getStarTime() {
		return starTime;
	}
	public void setStarTime(Long starTime) {
		this.starTime = starTime;
	}
	
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	
	public TargetStatus getStatus() {
		return status;
	}
	public void setStatus(TargetStatus status) {
		this.status = status;
	}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	public String toString() {
		return "InquiryTarget [id=" + id + ", name=" + name + ", starTime=" + starTime + ", endTime=" + endTime
				+ ", status=" + status + ", fileName=" + fileName + ", command=" + command + "]";
	}
	
	@Override
	public InquiryTarget clone() throws CloneNotSupportedException {
		InquiryTarget copy = (InquiryTarget)super.clone();
		copy.setId(getId());
		copy.setDisplayName(getDisplayName());
		copy.setStarTime(getStarTime());
		copy.setEndTime(getEndTime());
		copy.setStatus(getStatus());
		copy.setCommand(getCommand());
		copy.setFileName(getFileName());
		return copy;
	}
}