/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;

public class JobTreeItemRequest implements RequestDto{
	/** ジョブ情報 */
	@RestValidateObject(notNull = true)
	private JobInfoRequest data;

	/** 子のジョブツリーアイテムのリスト */
	private ArrayList<JobTreeItemRequest> children = new ArrayList<JobTreeItemRequest>();

	public JobTreeItemRequest() {
	}


	public JobInfoRequest getData() {
		return data;
	}

	public void setData(JobInfoRequest data) {
		this.data = data;
	}

	public ArrayList<JobTreeItemRequest> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<JobTreeItemRequest> children) {
		this.children = children;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		data.correlationCheck();

		if (children != null) {
			for (JobTreeItemRequest child : children) {
				child.correlationCheck();
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class JobTreeItemRequest {\n");
		sb.append("    data: ").append(toIndentedString(data)).append("\n");
		sb.append("    children: ").append(toIndentedString(children)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	* Convert the given object to string with each line indented by 4 spaces
	* (except the first line).
	*/
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}
