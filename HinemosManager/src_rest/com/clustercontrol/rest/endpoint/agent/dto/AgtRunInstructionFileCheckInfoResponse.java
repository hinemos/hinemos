/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.jobmanagement.bean.RunInstructionFileCheckInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = RunInstructionFileCheckInfo.class)
public class AgtRunInstructionFileCheckInfoResponse {

	// ---- from RunInstructionFileCheckInfo
	private String directory;
	private String fileName;
	private Boolean createValidFlg;
	private Boolean createBeforeJobStartFlg;
	private Boolean deleteValidFlg;
	private Boolean modifyValidFlg;
	private Integer modifyType;
	private Boolean notJudgeFileInUseFlg;
	private Integer successEndValue;

	public AgtRunInstructionFileCheckInfoResponse() {
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

	public Boolean getCreateValidFlg() {
		return createValidFlg;
	}

	public void setCreateValidFlg(Boolean createValidFlg) {
		this.createValidFlg = createValidFlg;
	}

	public Boolean getCreateBeforeJobStartFlg() {
		return createBeforeJobStartFlg;
	}

	public void setCreateBeforeJobStartFlg(Boolean createBeforeJobStartFlg) {
		this.createBeforeJobStartFlg = createBeforeJobStartFlg;
	}

	public Boolean getDeleteValidFlg() {
		return deleteValidFlg;
	}

	public void setDeleteValidFlg(Boolean deleteValidFlg) {
		this.deleteValidFlg = deleteValidFlg;
	}

	public Boolean getModifyValidFlg() {
		return modifyValidFlg;
	}

	public void setModifyValidFlg(Boolean modifyValidFlg) {
		this.modifyValidFlg = modifyValidFlg;
	}

	public Integer getModifyType() {
		return modifyType;
	}

	public void setModifyType(Integer modifyType) {
		this.modifyType = modifyType;
	}

	public Boolean getNotJudgeFileInUseFlg() {
		return notJudgeFileInUseFlg;
	}

	public void setNotJudgeFileInUseFlg(Boolean notJudgeFileInUseFlg) {
		this.notJudgeFileInUseFlg = notJudgeFileInUseFlg;
	}

	public Integer getSuccessEndValue() {
		return successEndValue;
	}

	public void setSuccessEndValue(Integer successEndValue) {
		this.successEndValue = successEndValue;
	}
}
