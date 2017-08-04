/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.custom.bean;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.custom.bean.Type;

/**
 * コマンド監視におけるコマンドの実行結果を格納するDTOクラス<br/>
 * 
 * @version 6.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class CommandResultDTO {

	// コマンド実行に起因する監視項目ID
	private String monitorId = null;

	// コマンド実行結果の対象となるファシリティID(どのファシリティIDに対する実行結果かを示す)
	private String facilityId = null;

	// コマンド実行がタイムアウトしたかどうか
	private boolean timeout = false;

	// 実際に実行したコマンド文字列
	private String command = null;

	// コマンドを実行したユーザ
	private String user = null;

	// コマンドの終了値
	private Integer exitCode = null;
	// コマンド実行時に生じた標準出力
	private String stdout = null;
	// コマンド実行時に生じた標準エラー
	private String stderr = null;
	// 標準出力をパースし、キー(デバイス名)・ 値(監視対象値)で格納したハッシュ
	private HashMap<String, Object> results = null;
	// 標準出力をパースし、不正な行(2カラムでない、デバイス名が重複)をキー(行番号)/値(行の文字列)で格納したハッシュ
	private HashMap<Integer, String> invalidLines = null;

	// 出力日時(コマンドの実行日時をサンプリング間隔にあわせて切り上げたものとする)
	private Long collectDate = null;

	// コマンドの実行日時(厳密には、コマンドを発行する直前の日時)
	private Long executeDate = null;

	// コマンドの終了日時(厳密には、コマンドが終了した or タイムアウトした直後の日時)
	private Long exitDate = null;

	// 監視ジョブ指示情報
	private RunInstructionInfo runInstructionInfo;

	// 種別（数値・文字列）
	private Type type = Type.NUMBER;

	/**
	 * コンストラクタ(no-argument for JAXB)<br/>
	 */
	public CommandResultDTO() {

	}

	/**
	 * コマンド実行に起因する監視項目IDを格納する。<br/>
	 * @param monitorId 監視項目ID
	 */
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	/**
	 * コマンド実行に起因する監視項目IDを返す。<br/>
	 * @return 監視項目ID
	 */
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * コマンド実行結果の対象となるファシリティIDを格納する。<br/>
	 * @param facilityId ファシリティID
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * コマンド実行結果の対象となるファシリティIDを返す。<br/>
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * ハッシュ(デバイス名 => 監視対象値)を格納する。<br/>
	 * @param results ハッシュ(デバイス名 => 監視対象値)
	 */
	public void setResults(HashMap<String, Object> results) {
		this.results = results;
	}

	/**
	 * ハッシュ(デバイス名 => 監視対象値)を返す。<br/>
	 * @return ハッシュ(デバイス名 => 監視対象値)
	 */
	public HashMap<String, Object> getResults() {
		return results;
	}

	/**
	 * ハッシュ(行番号 => 行文字列)を格納する。<br/>
	 * @param lines ハッシュ(行番号 => 行文字列)
	 */
	public void setInvalidLines(HashMap<Integer, String> lines) {
		this.invalidLines = lines;
	}

	/**
	 * ハッシュ(行番号 => 行文字列)を返す。<br/>
	 * @return ハッシュ(行番号 => 行文字列)
	 */
	public HashMap<Integer, String> getInvalidLines() {
		return invalidLines;
	}

	/**
	 * コマンドがタイムアウトしたかどうかを格納する。<br/>
	 * @param timeout タイムアウトした場合はtrue, 時間内に終了した場合はfalse
	 */
	public void setTimeout(boolean timeout) {
		this.timeout = timeout;
	}

	/**
	 * コマンドがタイムアウトしたかどうかを返す。<br/>
	 * @return タイムアウトした場合はtrue, 時間内に終了した場合はfalse
	 */
	public boolean getTimeout() {
		return timeout;
	}

	/**
	 * 実行されたコマンド文字列を格納する。<br/>
	 * @param command 実行されたコマンド文字列
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * 実行されたコマンド文字列を返す。<br/>
	 * @return 実行されたコマンド文字列
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * コマンドを実行したユーザ名を格納する。<br/>
	 * @param user コマンドを実行したユーザ名
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * コマンドを実行したユーザ名を返す。<br/>
	 * @return コマンドを実行したユーザ名
	 */
	public String getUser() {
		return user;
	}

	/**
	 * コマンドの終了値を格納する。<br/>
	 * コマンドの実行がタイムアウトした場合はnullとする。<br/>
	 * @param exitCode
	 */
	public void setExitCode(Integer exitCode) {
		this.exitCode = exitCode;
	}

	/**
	 * コマンドの終了値を返す。<br/>
	 * コマンドの実行がタイムアウトした場合はnullが返される。<br/>
	 * @return
	 */
	public Integer getExitCode() {
		return exitCode;
	}

	/**
	 * コマンドから出力された標準出力(文字列)を格納する。<br/>
	 * @param stdout 標準出力(文字列)
	 */
	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	/**
	 * コマンドから出力された標準出力(文字列)を返す。<br/>
	 * @return 標準出力(文字列)
	 */
	public String getStdout() {
		return stdout;
	}

	/**
	 * コマンドから出力された標準エラー出力(文字列)を格納する。<br/>
	 * @param stderr 標準エラー出力(文字列)
	 */
	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	/**
	 * コマンドから出力された標準エラー出力(文字列)を返す。<br/>
	 * @return 標準エラー出力(文字列)
	 */
	public String getStderr() {
		return stderr;
	}

	/**
	 * 出力日時を格納する。<br/>
	 * @param date 出力日時
	 */
	public void setCollectDate(Long date) {
		this.collectDate = date;
	}

	/**
	 * 出力日時を返す<br/>
	 * @return 出力日時
	 */
	public Long getCollectDate() {
		return collectDate;
	}

	/**
	 * コマンドの実行日時を格納する。<br/>
	 * @param date コマンドの実行日時
	 */
	public void setExecuteDate(Long date) {
		this.executeDate = date;
	}

	/**
	 * コマンドの実行日時を返す。<br/>
	 * @return コマンドの実行日時
	 */
	public Long getExecuteDate() {
		return executeDate;
	}

	/**
	 * コマンドの終了日時を格納する。<br/>
	 * @param date コマンドの終了日時
	 */
	public void setExitDate(Long date) {
		this.exitDate = date;
	}

	/**
	 * コマンドの終了日時を返す。<br/>
	 * @return コマンドの終了日時
	 */
	public Long getExitDate() {
		return exitDate;
	}

	/**
	 * 監視ジョブ指示情報を格納する。<br/>
	 * @param runInstructionInfo 監視ジョブ指示情報
	 */
	public void setRunInstructionInfo(RunInstructionInfo runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}

	/**
	 * 監視ジョブ指示情報を返す。<br/>
	 * @return 監視ジョブ指示情報
	 */
	public RunInstructionInfo getRunInstructionInfo() {
		return runInstructionInfo;
	}

	/**
	 * コマンドの種別を格納する。<br/>
	 * @param type コマンドの種別
	 */	
	public void setType(Type type){
		this.type = type;
	}

	/**
	 * コマンドの種別を返す。<br/>
	 * @return コマンドの種別
	 */
	public Type getType(){
		return type;
	}
	
	/**
	 * DTO内のメンバ変数の整合性をチェックする。<br/>
	 * 送受したタイミングなどに実行することを想定している。<br/>
	 * @throws HinemosUnknown
	 */
	public void validate() throws CustomInvalid {
		// check null
		if (monitorId == null) {
			throw new CustomInvalid("monitorId is not defined. (" + this + ")");
		}
		if (facilityId == null) {
			throw new CustomInvalid("facilityId is not defined. (" + this + ")");
		}
		if (command == null) {
			throw new CustomInvalid("command is not defined. (" + this +")");
		}
		if (user == null) {
			throw new CustomInvalid("user is not defined. (" + this +")");
		}
		if (executeDate == null) {
			throw new CustomInvalid("executeDate is not defined. (" + this +")");
		}
		if (exitDate == null) {
			throw new CustomInvalid("exitDate is not defined. (" + this +")");
		}

		// check consistency
		if (! timeout && exitCode == null) {
			throw new CustomInvalid("not timeout, but exitCode is not defined. (" + this +")");
		}
		if (! timeout && stdout == null) {
			throw new CustomInvalid("not timeout, but stdout is not defined. (" + this +")");
		}
		if (! timeout && stderr == null) {
			throw new CustomInvalid("not timeout, but stderr is not defined. (" + this +")");
		}
		if (! timeout && results == null) {
			throw new CustomInvalid("results is not defined. set 0 length hash if command failure. (" + this +")");
		}
		if (! timeout && invalidLines == null) {
			throw new CustomInvalid("invalidLines is not defined. set 0 length hash if command failure. (" + this +")");
		}
	}

	@Override
	public String toString() {
		// MAIN
		StringBuilder resultsStr = new StringBuilder();
		if (results != null) {
			for (Map.Entry<String, Object> entry : results.entrySet()) {
				resultsStr.append(resultsStr.length() == 0 ? "" : ", ");
				resultsStr.append(String.format("[key = %s, value = %s]", entry.getKey(), entry.getValue()));
			}
		}
		StringBuilder invalidStr = new StringBuilder();
		if (invalidLines != null) {
			for (Map.Entry<Integer, String> entry : invalidLines.entrySet()) {
				invalidStr.append(invalidStr.length() == 0 ? "" : ", ");
				invalidStr.append(String.format("[key = %s, value = %s]", entry.getKey(), entry.getValue()));
			}
		}
		String ret = this.getClass().getCanonicalName() + " [monitorId = " + monitorId
				+ ", facilityId = " + facilityId
				+ ", command = " + command
				+ ", user = " + user
				+ ", timeout = " + timeout
				+ ", stdout = " + stdout
				+ ", stderr = " + stderr
				+ ", colllectDate = " + collectDate
				+ ", executeDate = " + executeDate
				+ ", exitDate = " + exitDate
				+ ", results = (" + resultsStr + ")"
				+ ", invalidLines = (" + invalidStr + ")"
				+ (runInstructionInfo != null ? "monitorJob" : "")
				+ ", type = (" + type.name() + ")"
				+ "]";

		return ret;
	}

}
