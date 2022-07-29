/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusSelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationJobOutputEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PriorityRequiredEnum;
import com.clustercontrol.util.MessageConstant;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class JobOutputInfoRequest implements RequestDto {
	/** 標準出力と同じ出力先を使用する */
	@RestItemName(value=MessageConstant.JOB_OUTPUT_FAILURE_SAME_NORMAL)
	private Boolean sameNormalFlg;
	
	/** 出力先 - ディレクトリ */
	@RestValidateString(minLen=1, maxLen=1024)
	private String directory;
	
	/** 出力先 - ファイル名 */
	@RestValidateString(minLen=1, maxLen=1024)
	private String fileName;
	
	/** 追記フラグ */
	private Boolean appendFlg;
	
	/** ファイル出力失敗時の操作を指定 */
	private Boolean failureOperationFlg;
	
	/** ファイル出力失敗時の操作 */
	@RestBeanConvertEnum
	private OperationJobOutputEnum failureOperationType;
	
	/** ファイル出力失敗時 - 終了状態 */
	@RestBeanConvertEnum
	private EndStatusSelectEnum failureOperationEndStatus;
	
	/** ファイル出力失敗時 - 終了値 */
	private Integer failureOperationEndValue;
	
	/** ファイル出力失敗時 - ファイル出力失敗時に通知する */
	private Boolean failureNotifyFlg;

	/** ファイル出力失敗時 - 通知の重要度 */
	@RestBeanConvertEnum
	private PriorityRequiredEnum failureNotifyPriority;

	/** 有効／無効 */
	private Boolean valid;

	public JobOutputInfoRequest() {
	}

	public Boolean getSameNormalFlg() {
		return sameNormalFlg;
	}

	public void setSameNormalFlg(Boolean sameNormalFlg) {
		this.sameNormalFlg = sameNormalFlg;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Boolean getAppendFlg() {
		return appendFlg;
	}

	public void setAppendFlg(Boolean appendFlg) {
		this.appendFlg = appendFlg;
	}

	public Boolean getFailureOperationFlg() {
		return failureOperationFlg;
	}

	public void setFailureOperationFlg(Boolean failureOperationFlg) {
		this.failureOperationFlg = failureOperationFlg;
	}

	public OperationJobOutputEnum getFailureOperationType() {
		return failureOperationType;
	}

	public void setFailureOperationType(OperationJobOutputEnum failureOperationType) {
		this.failureOperationType = failureOperationType;
	}

	public EndStatusSelectEnum getFailureOperationEndStatus() {
		return failureOperationEndStatus;
	}

	public void setFailureOperationEndStatus(EndStatusSelectEnum failureOperationEndStatus) {
		this.failureOperationEndStatus = failureOperationEndStatus;
	}

	public Integer getFailureOperationEndValue() {
		return failureOperationEndValue;
	}

	public void setFailureOperationEndValue(Integer failureOperationEndValue) {
		this.failureOperationEndValue = failureOperationEndValue;
	}

	public Boolean getFailureNotifyFlg() {
		return failureNotifyFlg;
	}

	public void setFailureNotifyFlg(Boolean failureNotifyFlg) {
		this.failureNotifyFlg = failureNotifyFlg;
	}

	public PriorityRequiredEnum getFailureNotifyPriority() {
		return failureNotifyPriority;
	}

	public void setFailureNotifyPriority(PriorityRequiredEnum failureNotifyPriority) {
		this.failureNotifyPriority = failureNotifyPriority;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
