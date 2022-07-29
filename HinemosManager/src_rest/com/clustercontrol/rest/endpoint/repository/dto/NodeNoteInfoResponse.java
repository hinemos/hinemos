/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.repository.model.NodeNoteInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass = NodeNoteInfo.class, idName = "id")
public class NodeNoteInfoResponse {

	private Integer noteId;
	private String note;

	public NodeNoteInfoResponse() {
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
