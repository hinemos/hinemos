/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;

public class ModifyMonitorJobRequest extends AbstractModifyJobRequest implements RequestDto {

	/** 監視ジョブ情報 */
	@RestValidateObject(notNull = true)
	private JobMonitorInfoRequest monitor;

	public ModifyMonitorJobRequest() {
	}

	public JobMonitorInfoRequest getMonitor() {
		return monitor;
	}

	public void setMonitor(JobMonitorInfoRequest monitor) {
		this.monitor = monitor;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		monitor.correlationCheck();
	}

}
