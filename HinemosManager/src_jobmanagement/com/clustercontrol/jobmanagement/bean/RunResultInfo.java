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

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * 実行結果情報を保持するクラス<BR>
 *
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class RunResultInfo extends RunInfo implements Serializable {
	private static final long serialVersionUID = -5920913289024178396L;

	/** 実行状態 */
	private Integer status = 0;
	/** 時刻 */
	private Long time = 0l;
	/** ファイルリスト */
	private List<String> fileList = new ArrayList<>();
	/** ジョブ連携メッセージ情報 */
	private JobLinkMessageInfo jobLinkMessageInfo;
	/** 終了値 */
	private Integer endValue = 0;
	/** メッセージ */
	private String message;
	/** エラーメッセージ */
	private String errorMessage;

	/** RPAシナリオジョブ エラー種別 */
	private Integer rpaJobErrorType;
	/** RPAシナリオジョブ リターンコード */
	private Integer rpaJobReturnCode;
	/** RPAシナリオジョブ ログファイル名 */
	private String rpaJobLogfileName;
	/** RPAシナリオジョブ ログメッセージ */
	private String rpaJobLogMessage;
	/** RPAシナリオジョブ 終了値判定条件 */
	private RpaJobEndValueConditionInfo rpaJobEndValueConditionInfo;

	/** ファイルチェックジョブ実行結果情報 */
	private RunResultFileCheckInfo runResultFileCheckInfo = null;

	/**
	 * 時刻を返します。
	 * 
	 * @return 時刻
	 */
	public Long getTime() {
		return time;
	}

	/**
	 * 時刻を設定します。
	 * 
	 * @param time 時刻
	 */
	public void setTime(Long time) {
		this.time = time;
	}

	/**
	 * 終了値を返します。
	 * 
	 * @return 終了値
	 */
	public Integer getEndValue() {
		return endValue;
	}

	/**
	 * 終了値を設定します。
	 * 
	 * @param endValue 終了値
	 */
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	/**
	 * エラーメッセージを返します。
	 * 
	 * @return エラーメッセージ
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * エラーメッセージを設定します。
	 * 
	 * @param errorMessage エラーメッセージ
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * メッセージを返します。
	 * 
	 * @return メッセージ
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * メッセージを設定します。
	 * 
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 実行状態を返します。
	 * 
	 * @return 実行状態
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.RunStatusConstant
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * 実行状態を設定します。
	 * 
	 * @param status 実行状態
	 * 
	 * @see com.clustercontrol.jobmanagement.bean.RunStatusConstant
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * ファイルリストを返します。
	 * 
	 * @return ファイルリスト
	 */
	public List<String> getFileList() {
		return fileList;
	}

	/**
	 * ファイルリストを設定します。
	 * 
	 * @param fileList ファイルリスト
	 */
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

	/**
	 * @return the rpaJobErrorType
	 */
	public Integer getRpaJobErrorType() {
		return rpaJobErrorType;
	}

	/**
	 * @param rpaJobErrorType the rpaJobErrorType to set
	 */
	public void setRpaJobErrorType(Integer rpaJobErrorType) {
		this.rpaJobErrorType = rpaJobErrorType;
	}

	/**
	 * @return the rpaJobReturnCode
	 */
	public Integer getRpaJobReturnCode() {
		return rpaJobReturnCode;
	}

	/**
	 * @param rpaJobReturnCode the rpaJobReturnCode to set
	 */
	public void setRpaJobReturnCode(Integer rpaJobReturnCode) {
		this.rpaJobReturnCode = rpaJobReturnCode;
	}

	/**
	 * @return the rpaJobLogfileName
	 */
	public String getRpaJobLogfileName() {
		return rpaJobLogfileName;
	}

	/**
	 * @param rpaJobLogfileName the rpaJobLogfileName to set
	 */
	public void setRpaJobLogfileName(String rpaJobLogfileName) {
		this.rpaJobLogfileName = rpaJobLogfileName;
	}

	/**
	 * @return the rpaJobLogMessage
	 */
	public String getRpaJobLogMessage() {
		return rpaJobLogMessage;
	}

	/**
	 * @param rpaJobLogMessage the rpaJobLogMessage to set
	 */
	public void setRpaJobLogMessage(String rpaJobLogMessage) {
		this.rpaJobLogMessage = rpaJobLogMessage;
	}

	/**
	 * @return the rpaJobEndValueConditionInfo
	 */
	public RpaJobEndValueConditionInfo getRpaJobEndValueConditionInfo() {
		return rpaJobEndValueConditionInfo;
	}

	/**
	 * @param rpaJobEndValueConditionInfo the rpaJobEndValueConditionInfo to set
	 */
	public void setRpaJobEndValueConditionInfo(RpaJobEndValueConditionInfo rpaJobEndValueConditionInfo) {
		this.rpaJobEndValueConditionInfo = rpaJobEndValueConditionInfo;
	}

	/**
	 * ジョブ連携メッセージ情報を返します。
	 * 
	 * @return ジョブ連携メッセージ情報
	 */
	public JobLinkMessageInfo getJobLinkMessageInfo() {
		return jobLinkMessageInfo;
	}

	/**
	 * ジョブ連携メッセージ情報を設定します。
	 * 
	 * @param jobLinkMessageInfo ジョブ連携メッセージ情報
	 */
	public void setJobLinkMessageInfo(JobLinkMessageInfo jobLinkMessageInfo) {
		this.jobLinkMessageInfo = jobLinkMessageInfo;
	}

	/**
	 * ファイルチェックジョブ実行結果情報を返します。
	 * 
	 * @return
	 */
	@XmlTransient
	public RunResultFileCheckInfo getRunResultFileCheckInfo() {
		return runResultFileCheckInfo;
	}

	/**
	 * ファイルチェックジョブ実行結果情報を設定します。
	 * 
	 * @param
	 */
	public void setRunResultFileCheckInfo(RunResultFileCheckInfo runResultFileCheckInfo) {
		this.runResultFileCheckInfo = runResultFileCheckInfo;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((endValue == null) ? 0 : endValue.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((fileList == null) ? 0 : fileList.hashCode());
		result = prime * result
				+ ((jobLinkMessageInfo == null) ? 0 : jobLinkMessageInfo.hashCode());
		result = prime * result + ((endValue == null) ? 0 : endValue.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((fileList == null) ? 0 : fileList.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((runResultFileCheckInfo == null) ? 0 : runResultFileCheckInfo.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((rpaJobEndValueConditionInfo == null) ? 0 : rpaJobEndValueConditionInfo.hashCode());
		result = prime * result + ((rpaJobErrorType == null) ? 0 : rpaJobErrorType.hashCode());
		result = prime * result + ((rpaJobLogMessage == null) ? 0 : rpaJobLogMessage.hashCode());
		result = prime * result + ((rpaJobLogfileName == null) ? 0 : rpaJobLogfileName.hashCode());
		result = prime * result + ((rpaJobReturnCode == null) ? 0 : rpaJobReturnCode.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RunResultInfo other = (RunResultInfo) obj;
		if (endValue == null) {
			if (other.endValue != null) {
				return false;
			}
		} else if (!endValue.equals(other.endValue)) {
			return false;
		}
		if (errorMessage == null) {
			if (other.errorMessage != null) {
				return false;
			}
		} else if (!errorMessage.equals(other.errorMessage)) {
			return false;
		}
		if (fileList == null) {
			if (other.fileList != null) {
				return false;
			}
		} else if (!fileList.equals(other.fileList)) {
			return false;
		}
		if (jobLinkMessageInfo == null) {
			if (other.jobLinkMessageInfo != null) {
				return false;
			}
		} else if (!jobLinkMessageInfo.equals(other.jobLinkMessageInfo)) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		if (runResultFileCheckInfo == null) {
			if (other.runResultFileCheckInfo != null)
				return false;
		} else if (!runResultFileCheckInfo.equals(other.runResultFileCheckInfo))
			return false;
		if (status == null) {
			if (other.status != null) {
				return false;
			}
		} else if (!status.equals(other.status)) {
		if (rpaJobEndValueConditionInfo == null) {
			if (other.rpaJobEndValueConditionInfo != null)
				return false;
		} else if (!rpaJobEndValueConditionInfo.equals(other.rpaJobEndValueConditionInfo))
			return false;
		}
		if (rpaJobErrorType == null) {
			if (other.rpaJobErrorType != null)
				return false;
		} else if (!rpaJobErrorType.equals(other.rpaJobErrorType))
			return false;
		if (rpaJobLogMessage == null) {
			if (other.rpaJobLogMessage != null)
				return false;
		} else if (!rpaJobLogMessage.equals(other.rpaJobLogMessage))
			return false;
		if (rpaJobLogfileName == null) {
			if (other.rpaJobLogfileName != null)
				return false;
		} else if (!rpaJobLogfileName.equals(other.rpaJobLogfileName))
			return false;
		if (rpaJobReturnCode == null) {
			if (other.rpaJobReturnCode != null)
				return false;
		} else if (!rpaJobReturnCode.equals(other.rpaJobReturnCode))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (time == null) {
			if (other.time != null) {
				return false;
			}
		} else if (!time.equals(other.time)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RunResultInfo ["
				+ "status=" + status
				+ ", time=" + time
				+ ", fileList=" + fileList
				+ ", jobLinkMessageInfo=" + jobLinkMessageInfo
				+ ", endValue=" + endValue
				+ ", message=" + message
				+ ", errorMessage=" + errorMessage
				+ ", rpaJobErrorType=" + rpaJobErrorType
				+ ", rpaJobReturnCode=" + rpaJobReturnCode
				+ ", rpaJobLogfileName=" + rpaJobLogfileName
				+ ", rpaJobLogMessage=" + rpaJobLogMessage
				+ ", rpaJobEndValueConditionInfo=" + rpaJobEndValueConditionInfo
				+ ", runResultFileCheckInfo=" + runResultFileCheckInfo
				+ "]";
	}

}
