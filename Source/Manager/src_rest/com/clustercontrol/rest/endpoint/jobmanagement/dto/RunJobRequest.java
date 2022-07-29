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
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTriggerTypeEnum;

public class RunJobRequest implements RequestDto {
	
	public RunJobRequest() {
	}

	/** ジョブ実行契機種別（スケジュール、監視連動、手動実行のいづれか） */
	@RestBeanConvertEnum
	private JobTriggerTypeEnum trigger_type;
	
	/** 実行契機情報
	 *
	 * スケジュール：スケジュール名（スケジュールID）
	 * 監視連動　　：監視管理ID（プラグインID）
	 * 手動実行　　：ユーザ名
	 */
	private String trigger_info;

	/**
	 * 実行契機がファイルチェックの場合は、filenameに値が入る。
	 */
	private String filename;

	/**
	 * 実行契機がファイルチェックの場合は、directoryに値が入る。
	 */
	private String directory;

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

	/**
	 * ジョブ実行契機ID。
	 */
	private String jobkickId;

	/** ランタイムジョブ変数実行情報 */
	private ArrayList<JobRuntimeParamRunRequest> jobRuntimeParamList;

	public JobTriggerTypeEnum getTrigger_type() {
		return trigger_type;
	}

	public void setTrigger_type(JobTriggerTypeEnum trigger_type) {
		this.trigger_type = trigger_type;
	}

	public String getTrigger_info() {
		return trigger_info;
	}

	public void setTrigger_info(String trigger_info) {
		this.trigger_info = trigger_info;
	}

	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}

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

	public void setJobkickId(String jobkickId) {
		this.jobkickId = jobkickId;
	}

	public String getJobkickId() {
		return jobkickId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
