/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * ファイルチェックジョブの実行結果情報を保持するクラス
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class RunResultFileCheckInfo implements Serializable {
	private static final long serialVersionUID = -2514328383724964188L;

	/** ディレクトリ */
	private String directory;
	/** ファイル名 */
	private String fileName;

	/** 実際に判定されたチェック種別 */
	private Integer passedEventType = 0;
	/** 条件に一致したファイルのファイル更新日時 */
	private Long fileTimestamp = 0L;
	/** 条件に一致したファイルのファイルサイズ */
	private Long fileSize = 0L;

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getPassedEventType() {
		return passedEventType;
	}

	public void setPassedEventType(Integer passedEventType) {
		this.passedEventType = passedEventType;
	}

	public Long getFileTimestamp() {
		return fileTimestamp;
	}

	public void setFileTimestamp(Long fileTimestamp) {
		this.fileTimestamp = fileTimestamp;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	// --- auto generated from eclipse.
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((directory == null) ? 0 : directory.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((fileSize == null) ? 0 : fileSize.hashCode());
		result = prime * result + ((fileTimestamp == null) ? 0 : fileTimestamp.hashCode());
		result = prime * result + ((passedEventType == null) ? 0 : passedEventType.hashCode());
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
		RunResultFileCheckInfo other = (RunResultFileCheckInfo) obj;
		if (directory == null) {
			if (other.directory != null)
				return false;
		} else if (!directory.equals(other.directory))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (fileSize == null) {
			if (other.fileSize != null)
				return false;
		} else if (!fileSize.equals(other.fileSize))
			return false;
		if (fileTimestamp == null) {
			if (other.fileTimestamp != null)
				return false;
		} else if (!fileTimestamp.equals(other.fileTimestamp))
			return false;
		if (passedEventType == null) {
			if (other.passedEventType != null)
				return false;
		} else if (!passedEventType.equals(other.passedEventType))
			return false;
		return true;
	}
}
