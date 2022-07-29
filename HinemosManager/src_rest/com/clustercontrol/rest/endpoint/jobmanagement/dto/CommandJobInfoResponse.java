/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class CommandJobInfoResponse extends AbstractJobResponse {

	/** ジョブコマンド情報 */
	private JobCommandInfoResponse command;

	public CommandJobInfoResponse() {
	}

	public JobCommandInfoResponse getCommand() {
		return command;
	}

	public void setCommand(JobCommandInfoResponse command) {
		this.command = command;
	}

}
