/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.rest.endpoint.infra.dto.enumtype.OkNgEnum;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.RunCheckTypeEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;

public class ModuleNodeResultResponse {

	@RestBeanConvertEnum
	private RunCheckTypeEnum runCheckType;
	private String facilityId;
	@RestBeanConvertEnum
	private OkNgEnum result;
	private String message;
	private String oldFilename;
	private String oldFile;
	private String newFilename;
	private String newFile;
	private Boolean fileDiscarded;

	public ModuleNodeResultResponse() {
	}

	public RunCheckTypeEnum getRunCheckType() {
		return runCheckType;
	}

	public void setRunCheckType(RunCheckTypeEnum runCheckType) {
		this.runCheckType = runCheckType;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public OkNgEnum getResult() {
		return result;
	}

	public void setResult(OkNgEnum result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOldFilename() {
		return oldFilename;
	}

	public void setOldFilename(String oldFilename) {
		this.oldFilename = oldFilename;
	}

	public String getOldFile() {
		return oldFile;
	}

	public void setOldFile(String oldFile) {
		this.oldFile = oldFile;
	}

	public String getNewFilename() {
		return newFilename;
	}

	public void setNewFilename(String newFilename) {
		this.newFilename = newFilename;
	}

	public String getNewFile() {
		return newFile;
	}

	public void setNewFile(String newFile) {
		this.newFile = newFile;
	}

	public Boolean isFileDiscarded() {
		return fileDiscarded;
	}

	public void setFileDiscarded(Boolean fileDiscarded) {
		this.fileDiscarded = fileDiscarded;
	}

	@Override
	public String toString() {
		return "ModuleNodeResultResponse [runCheckType=" + runCheckType + ", facilityId=" + facilityId + ", result="
				+ result + ", message=" + message + ", oldFilename=" + oldFilename + ", oldFile=" + oldFile
				+ ", newFilename=" + newFilename + ", newFile=" + newFile + ", fileDiscarded=" + fileDiscarded + "]";
	}

}
