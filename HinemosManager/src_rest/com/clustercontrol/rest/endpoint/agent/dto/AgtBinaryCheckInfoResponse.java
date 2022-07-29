/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = BinaryCheckInfo.class, exclude = { "monitorInfo" })
public class AgtBinaryCheckInfoResponse {

	// ---- from MonitorCheckInfo
	private String monitorId;
	private String monitorTypeId;

	// ---- from BinaryCheckInfo
	// private String monitorId;
	private String directory;
	private String fileName;
	private String collectType;
	private String cutType;
	private String tagType;
	private String lengthType;
	private Boolean haveTs;
	private Long fileHeadSize;
	private Integer recordSize;
	private Integer recordHeadSize;
	private Integer sizePosition;
	private Integer sizeLength;
	private Integer tsPosition;
	private String tsType;
	private Boolean littleEndian;
	private String binaryfile;
	private String errMsg;

	public AgtBinaryCheckInfoResponse() {
	}

	// ---- accessors

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

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

	public String getCollectType() {
		return collectType;
	}

	public void setCollectType(String collectType) {
		this.collectType = collectType;
	}

	public String getCutType() {
		return cutType;
	}

	public void setCutType(String cutType) {
		this.cutType = cutType;
	}

	public String getTagType() {
		return tagType;
	}

	public void setTagType(String tagType) {
		this.tagType = tagType;
	}

	public String getLengthType() {
		return lengthType;
	}

	public void setLengthType(String lengthType) {
		this.lengthType = lengthType;
	}

	public Boolean getHaveTs() {
		return haveTs;
	}

	public void setHaveTs(Boolean haveTs) {
		this.haveTs = haveTs;
	}

	public Long getFileHeadSize() {
		return fileHeadSize;
	}

	public void setFileHeadSize(Long fileHeadSize) {
		this.fileHeadSize = fileHeadSize;
	}

	public Integer getRecordSize() {
		return recordSize;
	}

	public void setRecordSize(Integer recordSize) {
		this.recordSize = recordSize;
	}

	public Integer getRecordHeadSize() {
		return recordHeadSize;
	}

	public void setRecordHeadSize(Integer recordHeadSize) {
		this.recordHeadSize = recordHeadSize;
	}

	public Integer getSizePosition() {
		return sizePosition;
	}

	public void setSizePosition(Integer sizePosition) {
		this.sizePosition = sizePosition;
	}

	public Integer getSizeLength() {
		return sizeLength;
	}

	public void setSizeLength(Integer sizeLength) {
		this.sizeLength = sizeLength;
	}

	public Integer getTsPosition() {
		return tsPosition;
	}

	public void setTsPosition(Integer tsPosition) {
		this.tsPosition = tsPosition;
	}

	public String getTsType() {
		return tsType;
	}

	public void setTsType(String tsType) {
		this.tsType = tsType;
	}

	public Boolean getLittleEndian() {
		return littleEndian;
	}

	public void setLittleEndian(Boolean littleEndian) {
		this.littleEndian = littleEndian;
	}

	public String getBinaryfile() {
		return binaryfile;
	}

	public void setBinaryfile(String binaryfile) {
		this.binaryfile = binaryfile;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public String getMonitorTypeId() {
		return monitorTypeId;
	}

	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}

}
