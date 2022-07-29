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
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 実行情報を保持するクラス<BR>
 *
 * @version 4.1.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class RunInfo implements Serializable {
	private static final long serialVersionUID = -4752419336473888616L;

	/** ファシリティID */
	private String facilityId;
	/** セッションID */
	private String sessionId;
	/** 所属ジョブユニットのジョブID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** コマンドタイプ */
	private Integer commandType = 0;
	/** コマンド */
	private String command;
	/** ユーザ種別 */
	private Boolean specifyUser = false;
	/** 実行ユーザ */
	private String user;
	/** 停止種別 */
	private Integer stopType = 0;

	/** scp(ssh)公開鍵 */
	private String publicKey;
	/** チェックサム */
	private String checkSum;

	/** 環境変数情報 */
	private List<JobEnvVariableInfo> jobEnvVariableInfoList = new ArrayList<JobEnvVariableInfo>();

	/** 標準出力のファイル出力情報 - 標準出力 */
	private JobOutputInfo normalJobOutputInfo;

	/** 標準出力のファイル出力情報 - 標準エラー出力 */
	private JobOutputInfo errorJobOutputInfo;
	
	/**
	 * コマンドを返します。
	 * 
	 * @return コマンド
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * コマンドを設定します。
	 * 
	 * @param command コマンド
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * コマンド種別を返します。
	 * 
	 * @return コマンド種別
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.CommandTypeConstant
	 */
	public Integer getCommandType() {
		return commandType;
	}

	/**
	 * コマンド種別を設定します。
	 * 
	 * @param commandType コマンド種別
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.CommandTypeConstant
	 */
	public void setCommandType(Integer commandType) {
		this.commandType = commandType;
	}

	/**
	 * 停止種別を返します。
	 * 
	 * @return 停止種別
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant
	 */
	public Integer getStopType() {
		return stopType;
	}

	/**
	 * 停止種別を設定します。
	 * 
	 * @param stopType 停止種別
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant
	 */
	public void setStopType(Integer stopType) {
		this.stopType = stopType;
	}

	/**
	 * ファシリティIDを返します。
	 * 
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * ファシリティIDを設定します。
	 * 
	 * @param facilityId ファシリティID
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを返します。<BR>
	 * 
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return jobunitId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを設定します。<BR>
	 * 
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	/**
	 * ジョブIDを返します。
	 * 
	 * @return ジョブID
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * ジョブIDを設定します。
	 * 
	 * @param jobId ジョブID
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * セッションIDを返します。
	 * 
	 * @return セッションID
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * セッションIDを設定します。
	 * 
	 * @param sessionId セッションID
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * 公開キーを返します。
	 * 
	 * @return 公開キー
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * 公開キーを設定します。
	 * 
	 * @param publicKey 公開キー
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * ユーザ種別を返します。
	 * 
	 * @return ユーザ種別
	 */
	public Boolean isSpecifyUser() {
		return specifyUser;
	}

	/**
	 * ユーザ種別を設定します。
	 * 
	 * @param specifyUser ユーザ種別
	 */
	public void setSpecifyUser(Boolean specifyUser) {
		this.specifyUser = specifyUser;
	}

	/**
	 * 実行ユーザを返します。
	 * 
	 * @return 実行ユーザ
	 */
	public String getUser() {
		return user;
	}

	/**
	 * 実行ユーザを設定します。
	 * 
	 * @param user 実行ユーザ
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * チェックサムを返します。
	 * 
	 * @return チェックサム
	 */
	public String getCheckSum() {
		return checkSum;
	}

	/**
	 * チェックサムを設定します。
	 * 
	 * @param checkSum チェックサム
	 */
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
	
	/**
	 * 環境変数情報を返します。
	 * 
	 * @return 環境変数情報
	 */
	public List<JobEnvVariableInfo> getJobEnvVariableInfoList() {
		return jobEnvVariableInfoList;
	}

	/**
	 * 環境変数情報を設定します。
	 * 
	 * @param jobEnvVariableInfoList 環境変数情報
	 */
	public void setJobEnvVariableInfoList(List<JobEnvVariableInfo> jobEnvVariableInfoList) {
		this.jobEnvVariableInfoList = jobEnvVariableInfoList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((checkSum == null) ? 0 : checkSum.hashCode());
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result
				+ ((commandType == null) ? 0 : commandType.hashCode());
		result = prime * result
				+ ((facilityId == null) ? 0 : facilityId.hashCode());
		result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
		result = prime * result
				+ ((jobunitId == null) ? 0 : jobunitId.hashCode());
		result = prime * result
				+ ((publicKey == null) ? 0 : publicKey.hashCode());
		result = prime * result
				+ ((sessionId == null) ? 0 : sessionId.hashCode());
		result = prime * result
				+ ((specifyUser == null) ? 0 : specifyUser.hashCode());
		result = prime * result
				+ ((stopType == null) ? 0 : stopType.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result
				+ ((jobEnvVariableInfoList == null) ? 0 : jobEnvVariableInfoList.hashCode());
		result = prime * result
				+ ((normalJobOutputInfo == null) ? 0 : normalJobOutputInfo.hashCode());
		result = prime * result
				+ ((errorJobOutputInfo == null) ? 0 : errorJobOutputInfo.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RunInfo other = (RunInfo) obj;
		if (checkSum == null) {
			if (other.checkSum != null)
				return false;
		} else if (!checkSum.equals(other.checkSum))
			return false;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		if (commandType == null) {
			if (other.commandType != null)
				return false;
		} else if (!commandType.equals(other.commandType))
			return false;
		if (facilityId == null) {
			if (other.facilityId != null)
				return false;
		} else if (!facilityId.equals(other.facilityId))
			return false;
		if (jobId == null) {
			if (other.jobId != null)
				return false;
		} else if (!jobId.equals(other.jobId))
			return false;
		if (jobunitId == null) {
			if (other.jobunitId != null)
				return false;
		} else if (!jobunitId.equals(other.jobunitId))
			return false;
		if (publicKey == null) {
			if (other.publicKey != null)
				return false;
		} else if (!publicKey.equals(other.publicKey))
			return false;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		if (specifyUser == null) {
			if (other.specifyUser != null)
				return false;
		} else if (!specifyUser.equals(other.specifyUser))
			return false;
		if (stopType == null) {
			if (other.stopType != null)
				return false;
		} else if (!stopType.equals(other.stopType))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (jobEnvVariableInfoList == null) {
			if (other.jobEnvVariableInfoList != null)
				return false;
		} else if (!jobEnvVariableInfoList.equals(other.jobEnvVariableInfoList))
			return false;
		if (normalJobOutputInfo == null) {
			if (other.normalJobOutputInfo != null) {
				return false;
			}
		} else if (!normalJobOutputInfo.equals(other.normalJobOutputInfo)) {
			return false;
		}
		if (errorJobOutputInfo == null) {
			if (other.errorJobOutputInfo != null) {
				return false;
			}
		} else if (!errorJobOutputInfo.equals(other.errorJobOutputInfo)) {
			return false;
		}
		
		return true;
	}

	public JobOutputInfo getNormalJobOutputInfo() {
		return normalJobOutputInfo;
	}

	public void setNormalJobOutputInfo(JobOutputInfo normalJobOutputInfo) {
		this.normalJobOutputInfo = normalJobOutputInfo;
	}

	public JobOutputInfo getErrorJobOutputInfo() {
		return errorJobOutputInfo;
	}

	public void setErrorJobOutputInfo(JobOutputInfo errorJobOutputInfo) {
		this.errorJobOutputInfo = errorJobOutputInfo;
	}
}
