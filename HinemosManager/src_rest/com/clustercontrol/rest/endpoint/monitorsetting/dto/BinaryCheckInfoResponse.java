/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.BinaryCollectTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.BinaryCutTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.BinaryLengthTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.BinaryTimeStampTypeEnum;

public class BinaryCheckInfoResponse {
	private String directory;
	private String fileName;
	@RestBeanConvertEnum
	private BinaryCollectTypeEnum collectType;
	@RestBeanConvertEnum
	private BinaryCutTypeEnum cutType;
	private String tagType;
	@RestBeanConvertEnum
	private BinaryLengthTypeEnum lengthType;
	private Boolean haveTs;
	private Long fileHeadSize;
	private Integer recordSize;
	private Integer recordHeadSize;
	private Integer sizePosition;
	private Integer sizeLength;
	private Integer tsPosition;
	@RestBeanConvertEnum
	private BinaryTimeStampTypeEnum tsType;
	private Boolean littleEndian;
	@RestPartiallyTransrateTarget
	private String errMsg;

	public BinaryCheckInfoResponse() {
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
	public BinaryCollectTypeEnum getCollectType() {
		return collectType;
	}
	public void setCollectType(BinaryCollectTypeEnum collectType) {
		this.collectType = collectType;
	}
	public BinaryCutTypeEnum getCutType() {
		return cutType;
	}
	public void setCutType(BinaryCutTypeEnum cutType) {
		this.cutType = cutType;
	}
	public String getTagType() {
		return tagType;
	}
	public void setTagType(String tagType) {
		this.tagType = tagType;
	}
	public BinaryLengthTypeEnum getLengthType() {
		return lengthType;
	}
	public void setLengthType(BinaryLengthTypeEnum lengthType) {
		this.lengthType = lengthType;
	}
	public Boolean isHaveTs() {
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
	public BinaryTimeStampTypeEnum getTsType() {
		return tsType;
	}
	public void setTsType(BinaryTimeStampTypeEnum tsType) {
		this.tsType = tsType;
	}
	public Boolean isLittleEndian() {
		return littleEndian;
	}
	public void setLittleEndian(Boolean littleEndian) {
		this.littleEndian = littleEndian;
	}
	public String getErrMsg() {
		return errMsg;
	}
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	@Override
	public String toString() {
		return "BinaryCheckInfo [directory=" + directory + ", fileName=" + fileName + ", collectType=" + collectType
				+ ", cutType=" + cutType + ", tagType=" + tagType + ", lengthType=" + lengthType + ", haveTs=" + haveTs
				+ ", fileHeadSize=" + fileHeadSize + ", recordSize=" + recordSize + ", recordHeadSize=" + recordHeadSize
				+ ", sizePosition=" + sizePosition + ", sizeLength=" + sizeLength + ", tsPosition=" + tsPosition
				+ ", tsType=" + tsType + ", littleEndian=" + littleEndian + ", errMsg=" + errMsg + "]";
	}

}
