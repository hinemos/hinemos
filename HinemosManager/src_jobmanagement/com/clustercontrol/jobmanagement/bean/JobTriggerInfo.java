/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

/**
 * ジョブの実行契機情報を保持するクラス
 *
 * @version 2.4.0
 * @since 2.4.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobTriggerInfo implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -7125732021266469904L;

	/** ジョブ実行契機種別（スケジュール、監視連動、手動実行のいづれか） */
	private Integer trigger_type;
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

	/**
	 * ジョブ実行予定時間 (ジョブスケジュールで使用)
	 */
	private Long executeTime = 0L;

	/** ランタイムジョブ変数実行情報 */
	private ArrayList<JobRuntimeParamRun> jobRuntimeParamList;

	/**
	 * 実行契機種別を返す。
	 *
	 * @return 実行契機種別
	 */
	public Integer getTrigger_type() {
		return trigger_type;
	}

	/**
	 * 実行契機種別を設定する。
	 *
	 * @param trigger_type 実行契機種別
	 */
	public void setTrigger_type(Integer trigger_type) {
		this.trigger_type = trigger_type;
	}

	/**
	 * 実行契機情報を返す。
	 * @return 実行契機情報
	 */
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

	/**
	 * ジョブの待ち条件（時刻）の有効・無効を設定する。
	 *
	 * @param jobwaittime ジョブの待ち条件（時刻）の有効・無効
	 */
	public void setJobWaitTime(boolean jobwaittime) {
		this.jobWaitTime = jobwaittime;
	}

	/**
	 * ジョブの待ち条件（時刻）の有効・無効を返す。
	 * @return ジョブの待ち条件（時刻）の有効・無効
	 */
	public boolean getJobWaitTime() {
		return jobWaitTime;
	}

	/**
	 *ジョブの待ち条件（ジョブセッション開始後の時間の有効・無効を設定する。
	 *
	 * @param jobWaitMinute ジョブの待ち条件（ジョブセッション開始後の時間の有効・無効
	 */
	public void setJobWaitMinute(boolean jobwaitminute) {
		this.jobWaitMinute = jobwaitminute;
	}

	/**
	 * ジョブの待ち条件（ジョブセッション開始後の時間の有効・無効を返す。
	 * @return ジョブの待ち条件（ジョブセッション開始後の時間の有効・無効
	 */
	public boolean getJobWaitMinute() {
		return jobWaitMinute;
	}

	/**
	 *ジョブの起動コマンドの置換の有無を設定する。
	 *
	 * @param jobCommand ジョブの起動コマンドの置換の有無
	 */
	public void setJobCommand(boolean jobcommand) {
		this.jobCommand = jobcommand;
	}

	/**
	 * ジョブの起動コマンドの置換の有無を返す。
	 * @return ジョブの起動コマンドの置換の有無
	 */
	public boolean getJobCommand() {
		return jobCommand;
	}

	/**
	 *ジョブ起動コマンド置換の文字列を設定する。
	 *
	 * @param jobCommand ジョブ起動コマンド置換の文字列
	 */
	public void setJobCommandText(String jobcommandtext) {
		this.jobCommandText = jobcommandtext;
	}

	/**
	 *ジョブ起動コマンド置換の文字列を返す。
	 * @return ジョブ起動コマンド置換の文字列
	 */
	public String getJobCommandText() {
		return jobCommandText;
	}

	/**
	 * ランタイムジョブ変数のリストを設定する。
	 * 
	 * @param jobRuntimeParamList ランタイムジョブ変数リスト
	 */
	public void setJobRuntimeParamList(ArrayList<JobRuntimeParamRun> jobRuntimeParamList) {
		this.jobRuntimeParamList = jobRuntimeParamList;
	}

	/**
	 * ランタイムジョブ変数のリストを返す。
	 * @return ランタイムジョブ変数リスト
	 */
	public ArrayList<JobRuntimeParamRun> getJobRuntimeParamList() {
		return jobRuntimeParamList;
	}


	/**
	 *ジョブ実行契機IDを設定する。
	 *
	 * @param jobkickId ジョブ実行契機ID
	 */
	public void setJobkickId(String jobkickId) {
		this.jobkickId = jobkickId;
	}

	/**
	 *ジョブ実行契機IDを返す。
	 * @return ジョブ実行契機ID
	 */
	public String getJobkickId() {
		return jobkickId;
	}

	public Long getExecuteTime() {
		return executeTime;
	}

	public void setExecuteTime(Long executeTime) {
		this.executeTime = executeTime;
	}
}