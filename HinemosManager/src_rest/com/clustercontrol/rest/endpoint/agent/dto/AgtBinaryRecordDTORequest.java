/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.binary.bean.BinaryRecordDTO;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = BinaryRecordDTO.class)
public class AgtBinaryRecordDTORequest extends AgentRequestDto {

	// ---- from BinaryRecordDTO
	private String filePosition;
	private String sequential;
	private String key;
	private String base64Str;
	private String oxStr;
	private Long recTime;
	private String tags;
	private AgtBinaryPatternInfoRequest matchBinaryProvision;

	public AgtBinaryRecordDTORequest() {
	}

	// ---- accessors

	public String getFilePosition() {
		return filePosition;
	}

	public void setFilePosition(String filePosition) {
		this.filePosition = filePosition;
	}

	public String getSequential() {
		return sequential;
	}

	public void setSequential(String sequential) {
		this.sequential = sequential;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getBase64Str() {
		return base64Str;
	}

	public void setBase64Str(String base64Str) {
		this.base64Str = base64Str;
	}

	public String getOxStr() {
		return oxStr;
	}

	public void setOxStr(String oxStr) {
		this.oxStr = oxStr;
	}

	public Long getRecTime() {
		return recTime;
	}

	public void setRecTime(Long recTime) {
		this.recTime = recTime;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public AgtBinaryPatternInfoRequest getMatchBinaryProvision() {
		return matchBinaryProvision;
	}

	public void setMatchBinaryProvision(AgtBinaryPatternInfoRequest matchBinaryProvision) {
		this.matchBinaryProvision = matchBinaryProvision;
	}

}
