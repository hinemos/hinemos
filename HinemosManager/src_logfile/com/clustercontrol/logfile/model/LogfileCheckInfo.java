/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logfile.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;


/**
 * The persistent class for the cc_monitor_logfile_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_logfile_info", schema="setting")
@Cacheable(true)
public class LogfileCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String monitorTypeId;
	private String directory;
	private String fileName;
	private String fileEncoding;
	private String fileReturnCode;
	private String patternHead; 
	private String patternTail; 
	private Integer maxBytes;  
	private MonitorInfo monitorInfo;
	
	private String logfile;

	public LogfileCheckInfo() {
	}

	@XmlTransient
	@Id
	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	
	@Transient
	public String getMonitorTypeId(){
		return this.monitorTypeId;
	}
	public void setMonitorTypeId(String monitorTypeId){
		this.monitorTypeId = monitorTypeId;
	}

	@Column(name="directory")
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	@Column(name="file_name")
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Transient
	public String getLogfile() {
		return logfile;
	}
	
	public void setLogfile(String logfile) {
		this.logfile = logfile;
	}
	
	@Column(name="file_encoding")
	public String getFileEncoding() {
		return fileEncoding;
	}
	
	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}
	
	@Column(name="file_return_code")
	public String getFileReturnCode() {
		return fileReturnCode;
	}

	public void setFileReturnCode(String fileReturnCode) {
		this.fileReturnCode = fileReturnCode;
	}

	@Column(name="file_pattern_head")
	public String getPatternHead() {
		return patternHead;
	}
	
	public void setPatternHead(String patternHead) {
		this.patternHead = patternHead;
	}

	@Column(name="file_pattern_tail")
	public String getPatternTail() {
		return patternTail;
	}
	
	public void setPatternTail(String patternTail) {
		this.patternTail = patternTail;
	}

	@Column(name="file_max_bytes")
	public Integer getMaxBytes() {
		return maxBytes;
	}
	
	public void setMaxBytes(Integer maxBytes) {
		this.maxBytes = maxBytes;
	}

	//bi-directional one-to-one association to MonitorInfo
	@XmlTransient
	@OneToOne(fetch=FetchType.LAZY, optional=false)
	@PrimaryKeyJoinColumn
	public MonitorInfo getMonitorInfo() {
		return this.monitorInfo;
	}

	@Deprecated
	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}

	/**
	 * MonitorInfoオブジェクト参照設定<BR>
	 * 
	 * MonitorInfo設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToMonitorInfo(MonitorInfo monitorInfo) {
		this.setMonitorInfo(monitorInfo);
		if (monitorInfo != null) {
			monitorInfo.setLogfileCheckInfo(this);
		}
	}

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// MonitorInfo
		if (this.monitorInfo != null) {
			this.monitorInfo.setLogfileCheckInfo(null);
		}
	}

	@Override
	public String toString() {
		return "LogfileCheckInfo [monitorId=" + monitorId + ", monitorTypeId=" + monitorTypeId + ", directory="
				+ directory + ", fileName=" + fileName + ", fileEncoding=" + fileEncoding + ", fileReturnCode="
				+ fileReturnCode + ", patternHead=" + patternHead + ", patternTail=" + patternTail + ", maxBytes="
				+ maxBytes + ", logfile=" + logfile + "]";
	}
}