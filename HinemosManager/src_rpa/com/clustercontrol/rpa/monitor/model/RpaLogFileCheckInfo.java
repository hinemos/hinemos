/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.monitor.model;

import javax.xml.bind.annotation.XmlTransient;

import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * RPAログファイル監視のファイル情報を格納するEntity定義.
 * 
 */
@Entity
@Table(name="cc_monitor_rpalogfile_info", schema="setting")
@Cacheable(true)
public class RpaLogFileCheckInfo extends MonitorCheckInfo {
	private static final long serialVersionUID = 1L;
	
	/** 環境毎のRPAツールID */
	private String rpaToolEnvId;
	/** ディレクトリ */
	private String directory;
	/** ファイル名(正規表現) */
	private String fileName;
	/** ファイルエンコーディング */
	private String fileEncoding;

	/** 監視設定 */
	private MonitorInfo monitorInfo;

	/** 環境毎のRPAツールID */
	@Column(name="rpa_tool_env_id")
	public String getRpaToolEnvId() {
		return rpaToolEnvId;
	}
	public void setRpaToolEnvId(String rpaToolEnv) {
		this.rpaToolEnvId = rpaToolEnv;
	}

	/** ディレクトリ */
	@Column(name="directory")
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	/** ファイル名(正規表現) */
	@Column(name="file_name")
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/** ファイルエンコーディング */
	@Column(name="file_encoding")
	public String getFileEncoding() {
		return fileEncoding;
	}
	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	/** 監視設定 */
	//bi-directional one-to-one association to MonitorInfo
	@XmlTransient
	@OneToOne(fetch=FetchType.LAZY, optional=false)
	@PrimaryKeyJoinColumn
	public MonitorInfo getMonitorInfo() {
		return monitorInfo;
	}
	@Deprecated
	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}
}
