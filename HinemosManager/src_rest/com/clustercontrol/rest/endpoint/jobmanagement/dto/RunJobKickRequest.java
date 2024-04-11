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
import com.clustercontrol.rest.dto.RequestDto;

public class RunJobKickRequest implements RequestDto {
	
	public RunJobKickRequest() {
	}

	/**
	 * ジョブの待ち条件（時刻）の有効・無効の判定。
	 */
	private boolean jobWaitTime;

	/**
	 * ジョブの待ち条件（ジョブセッション開始後の時間の有効・無効の判定。
	 */
	private boolean jobWaitMinute;

	/**
	 * ジョブの起動コマンドの置換の有無の判定。
	 */
	private boolean jobCommand;

	/**
	 * ジョブ起動コマンド置換の文字列。
	 */
	private String jobCommandText;

	/** ランタイムジョブ変数実行情報 */
	private ArrayList<JobRuntimeParamRunRequest> jobRuntimeParamList;

	public void setJobWaitTime(boolean jobwaittime) {
		this.jobWaitTime = jobwaittime;
	}

	public boolean getJobWaitTime() {
		return jobWaitTime;
	}

	public void setJobWaitMinute(boolean jobwaitminute) {
		this.jobWaitMinute = jobwaitminute;
	}

	public boolean getJobWaitMinute() {
		return jobWaitMinute;
	}

	public void setJobCommand(boolean jobcommand) {
		this.jobCommand = jobcommand;
	}

	public boolean getJobCommand() {
		return jobCommand;
	}

	public void setJobCommandText(String jobcommandtext) {
		this.jobCommandText = jobcommandtext;
	}

	public String getJobCommandText() {
		return jobCommandText;
	}

	public void setJobRuntimeParamList(ArrayList<JobRuntimeParamRunRequest> jobRuntimeParamList) {
		this.jobRuntimeParamList = jobRuntimeParamList;
	}

	public ArrayList<JobRuntimeParamRunRequest> getJobRuntimeParamList() {
		return jobRuntimeParamList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
