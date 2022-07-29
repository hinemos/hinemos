/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.annotation.EnumerateConstant;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer.EnumToConstantDeserializer;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusSelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationJobOutputEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer.ConstantToEnumSerializer;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * 標準出力のファイル出力に関する情報を保持するクラス
 * 
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobOutputInfo implements Serializable, RequestDto {

	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 364327644303818965L;

	@JsonIgnore
	private static Log m_log = LogFactory.getLog( JobOutputInfo.class );

	/** 出力先と同じ出力先を使用する */
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
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=OperationJobOutputEnum.class)
	private Integer failureOperationType;
	
	/** ファイル出力失敗時 - 終了状態 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=EndStatusSelectEnum.class)
	private Integer failureOperationEndStatus;
	
	/** ファイル出力失敗時 - 終了値 */
	private Integer failureOperationEndValue;
	
	/** ファイル出力失敗時 - ファイル出力失敗時に通知する */
	private Boolean failureNotifyFlg;

	/** ファイル出力失敗時 - 通知の重要度 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=PriorityEnum.class)
	private Integer failureNotifyPriority;

	/** 有効／無効 */
	private Boolean valid;

	/**
	 * 出力先と同じ出力先を使用するを返す。<BR>
	 * @return true:出力先と同じ出力先を使用する
	 */
	public Boolean getSameNormalFlg() {
		return sameNormalFlg;
	}

	/**
	 * 出力先と同じ出力先を使用するを設定する。<BR>
	 * @param sameNormalFlg true:出力先と同じ出力先を使用する
	 */
	public void setSameNormalFlg(Boolean sameNormalFlg) {
		this.sameNormalFlg = sameNormalFlg;
	}

	/**
	 * 出力先 - ディレクトリを返す。<BR>
	 * @return 出力先 - ディレクトリ
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * 出力先 - ディレクトリを設定する。<BR>
	 * @param directory 出力先 - ディレクトリ
	 */
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	/**
	 * 出力先 - ファイル名を返す。<BR>
	 * @return 出力先 - ファイル名
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * 出力先 - ファイル名を設定する。<BR>
	 * @param fileName 出力先 - ファイル名
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 追記フラグを返す。<BR>
	 * @return 追記フラグ
	 */
	public Boolean getAppendFlg() {
		return appendFlg;
	}

	/**
	 * 追記フラグを設定する。<BR>
	 * @param appendFlg 追記フラグ
	 */
	public void setAppendFlg(Boolean appendFlg) {
		this.appendFlg = appendFlg;
	}

	/**
	 * ファイル出力失敗時の操作を指定を返す。<BR>
	 * @return true:ファイル出力失敗時の操作を指定
	 */
	public Boolean getFailureOperationFlg() {
		return failureOperationFlg;
	}

	/**
	 * ファイル出力失敗時の操作を指定を設定する。<BR>
	 * @param failureOperationFlg true:ファイル出力失敗時の操作を指定
	 */
	public void setFailureOperationFlg(Boolean failureOperationFlg) {
		this.failureOperationFlg = failureOperationFlg;
	}

	/**
	 * ファイル出力失敗時の操作を返す。<BR>
	 * @return ファイル出力失敗時の操作
	 */
	public Integer getFailureOperationType() {
		return failureOperationType;
	}

	/**
	 * ファイル出力失敗時の操作を設定する。<BR>
	 * @param failureOperationType ファイル出力失敗時の操作
	 */
	public void setFailureOperationType(Integer failureOperationType) {
		this.failureOperationType = failureOperationType;
	}

	/**
	 * ファイル出力失敗時 - 終了状態を返す。<BR>
	 * @return ファイル出力失敗時 - 終了状態
	 */
	public Integer getFailureOperationEndStatus() {
		return failureOperationEndStatus;
	}

	/**
	 * ファイル出力失敗時 - 終了状態を設定する。<BR>
	 * @param failureOperationEndStatus ファイル出力失敗時 - 終了状態
	 */
	public void setFailureOperationEndStatus(Integer failureOperationEndStatus) {
		this.failureOperationEndStatus = failureOperationEndStatus;
	}

	/**
	 * ファイル出力失敗時 - 終了値を返す。<BR>
	 * @return ファイル出力失敗時 - 終了値
	 */
	public Integer getFailureOperationEndValue() {
		return failureOperationEndValue;
	}

	/**
	 * ファイル出力失敗時 - 終了値を設定する。<BR>
	 * @param failureOperationEndValue ファイル出力失敗時 - 終了値
	 */
	public void setFailureOperationEndValue(Integer failureOperationEndValue) {
		this.failureOperationEndValue = failureOperationEndValue;
	}

	/**
	 * ファイル出力失敗時に通知するを返す。<BR>
	 * @return true:ファイル出力失敗時に通知する
	 */
	public Boolean getFailureNotifyFlg() {
		return failureNotifyFlg;
	}

	/**
	 * ファイル出力失敗時に通知するを設定する。<BR>
	 * @param failureNotifyFlg true:ファイル出力失敗時に通知する
	 */
	public void setFailureNotifyFlg(Boolean failureNotifyFlg) {
		this.failureNotifyFlg = failureNotifyFlg;
	}

	/**
	 * ファイル出力失敗時 - 通知の重要度を返す。<BR>
	 * @return ファイル出力失敗時 - 通知の重要度
	 */
	public Integer getFailureNotifyPriority() {
		return failureNotifyPriority;
	}

	/**
	 * ファイル出力失敗時 - 通知の重要度を設定する。<BR>
	 * @param failureNotifyFlg ファイル出力失敗時 - 通知の重要度
	 */
	public void setFailureNotifyPriority(Integer failureNotifyPriority) {
		this.failureNotifyPriority = failureNotifyPriority;
	}

	/**
	 * 有効/無効を返す。<BR>
	 * @return true:有効
	 */
	public Boolean getValid() {
		return valid;
	}

	/**
	 * 有効/無効を設定する。<BR>
	 * @param valid true:有効
	 */
	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appendFlg == null) ? 0 : appendFlg.hashCode());
		result = prime * result + ((directory == null) ? 0 : directory.hashCode());
		result = prime * result + ((failureNotifyFlg == null) ? 0 : failureNotifyFlg.hashCode());
		result = prime * result + ((failureNotifyPriority == null) ? 0 : failureNotifyPriority.hashCode());
		result = prime * result + ((failureOperationEndStatus == null) ? 0 : failureOperationEndStatus.hashCode());
		result = prime * result + ((failureOperationEndValue == null) ? 0 : failureOperationEndValue.hashCode());
		result = prime * result + ((failureOperationFlg == null) ? 0 : failureOperationFlg.hashCode());
		result = prime * result + ((failureOperationType == null) ? 0 : failureOperationType.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((sameNormalFlg == null) ? 0 : sameNormalFlg.hashCode());
		result = prime * result + ((valid == null) ? 0 : valid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobOutputInfo)) {
			return false;
		}
		JobOutputInfo o1 = this;
		JobOutputInfo o2 = (JobOutputInfo)o;

		boolean ret = false;
		ret = 	equalsSub(o1.getSameNormalFlg(), o2.getSameNormalFlg()) &&
				equalsSub(o1.getDirectory(), o2.getDirectory()) &&
				equalsSub(o1.getFileName(), o2.getFileName()) &&
				equalsSub(o1.getAppendFlg(), o2.getAppendFlg()) &&
				equalsSub(o1.getFailureOperationFlg(), o2.getFailureOperationFlg()) &&
				equalsSub(o1.getFailureOperationType(), o2.getFailureOperationType()) &&
				equalsSub(o1.getFailureOperationEndStatus(), o2.getFailureOperationEndStatus()) &&
				equalsSub(o1.getFailureOperationEndValue(), o2.getFailureOperationEndValue()) &&
				equalsSub(o1.getFailureNotifyFlg(), o2.getFailureNotifyFlg()) &&
				equalsSub(o1.getFailureNotifyPriority(), o2.getFailureNotifyPriority()) &&
				equalsSub(o1.getValid(), o2.getValid());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2)
			return true;
		
		if (o1 == null)
			return false;
		
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}