/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer.bean;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;

/**
 * RegisterJobunitRequestで定義されたスキーマのJSONを、JobTreeItemクラスに変換するためのクラス
 */
public class RegisterJobunitWrapper implements RequestDto {
	@RestValidateObject(notNull = true)
	JobTreeItem jobTreeItem;

	public RegisterJobunitWrapper() {
	}

	public JobTreeItem getJobTreeItem() {
		return jobTreeItem;
	}

	public void setJobTreeItem(JobTreeItem jobTreeItem) {
		this.jobTreeItem = jobTreeItem;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		jobTreeItem.correlationCheck();
	}

}
