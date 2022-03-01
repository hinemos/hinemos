/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * RPAシナリオジョブでRPAツールエグゼキューターへ渡すシナリオ実行情報を保持するクラス
 */
public class RoboRunInfo extends RoboInfo {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;
	/** シナリオ実行コマンド */
	private String execCommand;
	/** プロセス終了コマンド */
	private String destroyCommand;
	/**
	 * シナリオ実行完了前にログインを実行したかどうかのフラグ<br>
	 * trueの場合はシナリオ実行完了後にログアウトします。
	 */
	private Boolean login;
	/**
	 * シナリオ実行完了後にログアウトを行うかどうかのフラグ<br>
	 * trueの場合はシナリオ実行が異常終了した場合もログアウトします。
	 */
	private Boolean logout;
	/** ジョブを停止する際にRPAのプロセスを終了するかどうかのフラグ */
	private Boolean destroy;

	/**
	 * コンストラクタ
	 */
	public RoboRunInfo() {
	}

	/**
	 * コンストラクタ
	 * 
	 * @param datetime
	 *            ジョブ実行日時
	 * @param sessionId
	 *            セッションID
	 * @param jobunitId
	 *            ジョブユニットID
	 * @param jobId
	 *            ジョブID
	 * @param facilityId
	 *            ファシリティID
	 * @param execCommand
	 *            シナリオ実行コマンド
	 * @param destroyCommand
	 *            プロセス終了コマンド
	 * @param login
	 *            ログインを行うかどうかのフラグ
	 * @param logout
	 *            異常発生時にログアウトするかどうかのフラグ
	 * @param destroy
	 *            ジョブ停止時にRPAツールのプロセスを停止するかどうかのフラグ
	 */
	public RoboRunInfo(Long datetime, String sessionId, String jobunitId, String jobId, String facilityId,
			String execCommand, String destroyCommand, Boolean login, Boolean logout, Boolean destroy) {
		super(datetime, sessionId, jobunitId, jobId, facilityId);
		this.execCommand = execCommand;
		this.destroyCommand = destroyCommand;
		this.login = login;
		this.logout = logout;
		this.destroy = destroy;
	}

	/**
	 * コンストラクタ
	 * 
	 * @param roboInfo
	 *            RPAツールエグゼキューター指示情報
	 */
	public RoboRunInfo(RoboInfo roboInfo) {
		super(roboInfo.getDatetime(), roboInfo.getSessionId(), roboInfo.getJobunitId(), roboInfo.getJobId(),
				roboInfo.getFacilityId());
	}

	public String getExecCommand() {
		return execCommand;
	}

	public void setExecCommand(String execCommand) {
		this.execCommand = execCommand;
	}

	public String getDestroyCommand() {
		return destroyCommand;
	}

	public void setDestroyCommand(String destroyCommand) {
		this.destroyCommand = destroyCommand;
	}

	public Boolean getLogin() {
		return login;
	}

	public void setLogin(Boolean login) {
		this.login = login;
	}

	public Boolean getLogout() {
		return logout;
	}

	public void setLogout(Boolean logout) {
		this.logout = logout;
	}

	public Boolean getDestroy() {
		return destroy;
	}

	public void setDestroy(Boolean destroy) {
		this.destroy = destroy;
	}

	@Override
	public String toString() {
		return "RoboRunInfo [getExecCommand()=" + getExecCommand() + ", getDestroyCommand()=" + getDestroyCommand()
				+ ", getLogin()=" + getLogin() + ", getLogout()=" + getLogout() + ", getDestroy()=" + getDestroy()
				+ ", getDatetime()=" + getDatetime() + ", getSessionId()=" + getSessionId() + ", getJobunitId()="
				+ getJobunitId() + ", getJobId()=" + getJobId() + ", getFacilityId()=" + getFacilityId() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((destroy == null) ? 0 : destroy.hashCode());
		result = prime * result + ((destroyCommand == null) ? 0 : destroyCommand.hashCode());
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		result = prime * result + ((logout == null) ? 0 : logout.hashCode());
		result = prime * result + ((execCommand == null) ? 0 : execCommand.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoboRunInfo other = (RoboRunInfo) obj;
		if (destroy == null) {
			if (other.destroy != null)
				return false;
		} else if (!destroy.equals(other.destroy))
			return false;
		if (destroyCommand == null) {
			if (other.destroyCommand != null)
				return false;
		} else if (!destroyCommand.equals(other.destroyCommand))
			return false;
		if (login == null) {
			if (other.login != null)
				return false;
		} else if (!login.equals(other.login))
			return false;
		if (logout == null) {
			if (other.logout != null)
				return false;
		} else if (!logout.equals(other.logout))
			return false;
		if (execCommand == null) {
			if (other.execCommand != null)
				return false;
		} else if (!execCommand.equals(other.execCommand))
			return false;
		return true;
	}
}
