/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.repository.model.NodeNoteInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(from = NodeNoteInfo.class)
@RestBeanConvertIdClassSet(infoClass = NodeNoteInfo.class, idName = "id")
public class AgtNodeNoteInfoResponse {

	// ---- from NodeNoteInfoPK
	// private String facilityId;
	private Integer noteId;

	// ---- from NodeNoteInfo
	// private NodeNoteInfoPK id;
	private String note;
	// private NodeInfo nodeEntity; // 循環参照させない

	public AgtNodeNoteInfoResponse() {
	}

	public Integer getNoteId() {
		return noteId;
	}

	public void setNoteId(Integer noteId) {
		this.noteId = noteId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
