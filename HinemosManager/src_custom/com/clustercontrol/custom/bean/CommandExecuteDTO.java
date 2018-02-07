/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;

/**
 * 情報収集に利用するコマンド実行情報の格納クラス<BR />
 * エージェントのJava VM上で、監視設定1つに対して1つのWSインスタンスが存在する。<BR />
 * 
 * @version 6.0.0
 * @since 4.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class CommandExecuteDTO {

	// コマンド実行に起因する監視項目ID
	private String monitorId;

	// コマンド実行に起因するファシリティID
	private String facilityId;

	// コマンドの実効ユーザ
	private Boolean specifyUser;
	private String effectiveUser;
	// コマンド文字列(変数込み)
	private String command;
	private Integer timeout = 0;

	private Integer interval = 0;
	private CalendarInfo calendar;

	// 監視ジョブ指示情報
	private RunInstructionInfo runInstructionInfo = null;

	// 種別（数値・文字列）
	private Type type;
	
	// 対象となっているノード情報のハッシュ(facilityId -> hash of variables)
	//   ノードごとに実行する場合 : 1セットのみ
	//   特定のノードでまとめて実行する場合 : 監視対象スコープに含まれるノード数分のセット
	private List<CommandVariableDTO> variables;

	/**
	 * コンストラクタ(no-argument for JAXB)
	 */
	public CommandExecuteDTO() {

	}

	/**
	 * コンストラクタ
	 * @throws CustomInvalid メンバ変数に不整合が存在する場合
	 */
	public CommandExecuteDTO(String monitorId, String facilityId, Boolean specifyUser, String effectiveUser, String command, Integer timeout,
			Integer interval, CalendarInfo calendar, List<CommandVariableDTO> variables, Type type) throws CustomInvalid {
		this(monitorId, facilityId, specifyUser, effectiveUser, command, timeout,
				interval, calendar, variables, null,type);
	}

	/**
	 * コンストラクタ
	 * @throws CustomInvalid メンバ変数に不整合が存在する場合
	 */
	public CommandExecuteDTO(String monitorId, String facilityId, Boolean specifyUser, String effectiveUser, String command, Integer timeout,
			Integer interval, CalendarInfo calendar, List<CommandVariableDTO> variables, RunInstructionInfo runInstructionInfo, Type type) throws CustomInvalid {
		this.monitorId = monitorId;
		this.facilityId = facilityId;
		this.specifyUser = specifyUser;
		this.effectiveUser = effectiveUser;
		this.command = command;
		this.timeout = timeout;
		this.interval = interval;
		this.calendar = calendar;
		this.variables = variables;
		this.runInstructionInfo = runInstructionInfo;
		this.type = type;

		validate();
	}

	/**
	 * コマンド実行情報に対応する監視項目IDを付与する。<br/>
	 * @param monitorId 監視項目ID
	 */
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	/**
	 * コマンド実行情報に対応する監視項目IDを返す。<br/>
	 * @return 監視項目ID
	 */
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * コマンド実行情報に対応するファシリティIDを付与する。<br/>
	 * @param facilityId ファシリティID
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * コマンド実行情報に対応するファシリティIDを返す。<br/>
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * 実効ユーザの種別を付与する。<br/>
	 * @param specifyUser 実効ユーザ種別
	 */
	public void setSpecifyUser(Boolean specifyUser) {
		this.specifyUser = specifyUser;
	}

	/**
	 * 実効ユーザの種別を返す。<br/>
	 * @return 実効ユーザ種別
	 */
	public Boolean isSpecifyUser() {
		return specifyUser;
	}

	/**
	 * コマンドの実効ユーザを付与する。<br/>
	 * @param effectiveUser 実効ユーザ
	 */
	public void setEffectiveUser(String effectiveUser) {
		this.effectiveUser = effectiveUser;
	}

	/**
	 * コマンドの実効ユーザを返す。<br/>
	 * @return 実効ユーザ
	 */
	public String getEffectiveUser() {
		return effectiveUser;
	}

	/**
	 * 実行されるコマンド文字列を付与する。<br/>
	 * @param command コマンド文字列
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * 実行されるコマンド文字列を返す。<br/>
	 * @return コマンド文字列
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * コマンド実行のタイムアウト時間を付与する。<br/>
	 * @param timeout タイムアウト時間[msec]
	 */
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	/**
	 * コマンド実行のタイムアウト時間を返す。<br/>
	 * @return タイムアウト時間[msec]
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 * コマンド実行情報に割り当てられたカレンダ情報を付与する。<br/>
	 * @param calendar カレンダ情報
	 */
	public void setCalendar(CalendarInfo calendar) {
		this.calendar = calendar;
	}

	/**
	 * コマンド実行情報に割り当てられたカレンダ情報を返す。<br/>
	 * @return カレンダ情報
	 */
	public CalendarInfo getCalendar() {
		return calendar;
	}

	/**
	 * コマンドの実行間隔を付与する。<br/>
	 * @param interval コマンドの実行間隔[msec]
	 */
	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	/**
	 * コマンドの実行間隔を返す。<br/>
	 * @return コマンド実行間隔[msec]
	 */
	public Integer getInterval() {
		return interval;
	}

	/**
	 * 実行対象となっているノード情報の一覧を付与する。<br/>
	 * このノード情報に基づいて、変数が埋め込まれたコマンドが実行される。<br/>
	 * @param variables 実行対象となっているノード情報の一覧
	 */
	public void setVariables(List<CommandVariableDTO> variables) {
		this.variables = variables;
	}

	/**
	 * 実行対象となっているノード情報の一覧を返す。<br/>
	 * @return 実行対象となっているノード情報の一覧
	 */
	public List<CommandVariableDTO> getVariables() {
		return variables;
	}

	/**
	 * 監視ジョブ指示情報を付与する。<br/>
	 * @param runInstructionInfo 監視ジョブ指示情報
	 */
	public void setRunInstructionInfo(RunInstructionInfo runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}

	/**
	 * 監視ジョブ指示情報を返す。<br/>
	 * @return runInstructionInfo 監視ジョブ指示情報
	 */
	public RunInstructionInfo getRunInstructionInfo() {
		return runInstructionInfo;
	}

	public void setType(Type type){
		this.type = type;
	}

	public Type getType(){
		return type;
	}
	
	/**
	 * メンバ変数の妥当性を確認する。<br/>
	 * @throws CustomInvalid 妥当でない値が含まれる場合
	 */
	public void validate() throws CustomInvalid {
		// null check
		if (effectiveUser == null) {
			throw new CustomInvalid("effectiveUser must be defined(not null). (" + this + ")");
		}
		if (command == null) {
			throw new CustomInvalid("command must be defined(not null). (" + this + ")");
		}
		if (variables == null) {
			throw new CustomInvalid("variables must be defined(not null). 0 length list is allowed. (" + this + ")");
		}

		// validate consistency
		if (timeout <= 0) {
			throw new CustomInvalid("interval must be greater than 0. (" + this + ")");
		}
		if (timeout > interval) {
			throw new CustomInvalid("timeout must be less than interval. (" + this + ")");
		}
	}

	@Override
	public String toString() {
		String variablesStr = "";

		if (variables != null) {
			variablesStr = "";
			for (CommandVariableDTO variable : variables) {
				variablesStr += variablesStr.length() == 0 ? variable : ", " +variable;
			}
		}

		return this.getClass().getCanonicalName() + " [monitorId = " + monitorId
				+ ", facilityID = " + facilityId
				+ ", effectiveUser = " + effectiveUser
				+ ", command = " + command
				+ ", timeout = " + timeout
				+ ", interval = " + interval
				+ (runInstructionInfo != null ? "monitorJob" : "")
				+ "]";
	}

}
