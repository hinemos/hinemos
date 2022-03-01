/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;

public class JobFileInfoResponse {
	/** スコープ処理方法 */
	@RestBeanConvertEnum
	private ProcessingMethodEnum processingMethod = ProcessingMethodEnum.ALL_NODE;

	/** 転送ファシリティID */
	private String srcFacilityID;

	/** 受信ファシリティID */
	private String destFacilityID;

	/** 転送スコープ */
	private String srcScope;

	/** 受信スコープ */
	@RestPartiallyTransrateTarget
	private String destScope;

	/** ファイル */
	private String srcFile;

	/** 転送作業ディレクトリ */
	private String srcWorkDir = "";

	/** 受信ディレクトリ */
	private String destDirectory;

	/** 受信作業ディレクトリ */
	private String destWorkDir = "";

	/** ファイル圧縮 */
	private Boolean compressionFlg = false;

	/** ファイルチェック */
	private Boolean checkFlg = false;

	/** ユーザ種別 */
	private Boolean specifyUser = false;

	/** 実効ユーザ */
	private String user;

	/** リトライ回数 */
	private Integer messageRetry = 0;
	
	/** コマンド実行失敗時終了フラグ */
	private Boolean messageRetryEndFlg = false;

	/** コマンド実行失敗時終了値 */
	private Integer messageRetryEndValue = 0;

	public JobFileInfoResponse() {
	}

	public ProcessingMethodEnum getProcessingMethod() {
		return processingMethod;
	}

	public void setProcessingMethod(ProcessingMethodEnum processingMethod) {
		this.processingMethod = processingMethod;
	}

	public String getSrcFacilityID() {
		return srcFacilityID;
	}

	public void setSrcFacilityID(String srcFacilityID) {
		this.srcFacilityID = srcFacilityID;
	}

	public String getDestFacilityID() {
		return destFacilityID;
	}

	public void setDestFacilityID(String destFacilityID) {
		this.destFacilityID = destFacilityID;
	}

	public String getSrcScope() {
		return srcScope;
	}

	public void setSrcScope(String srcScope) {
		this.srcScope = srcScope;
	}

	public String getDestScope() {
		return destScope;
	}

	public void setDestScope(String destScope) {
		this.destScope = destScope;
	}

	public String getSrcFile() {
		return srcFile;
	}

	public void setSrcFile(String srcFile) {
		this.srcFile = srcFile;
	}

	public String getSrcWorkDir() {
		return srcWorkDir;
	}

	public void setSrcWorkDir(String srcWorkDir) {
		this.srcWorkDir = srcWorkDir;
	}

	public String getDestDirectory() {
		return destDirectory;
	}

	public void setDestDirectory(String destDirectory) {
		this.destDirectory = destDirectory;
	}

	public String getDestWorkDir() {
		return destWorkDir;
	}

	public void setDestWorkDir(String destWorkDir) {
		this.destWorkDir = destWorkDir;
	}

	public Boolean getCompressionFlg() {
		return compressionFlg;
	}

	public void setCompressionFlg(Boolean compressionFlg) {
		this.compressionFlg = compressionFlg;
	}

	public Boolean getCheckFlg() {
		return checkFlg;
	}

	public void setCheckFlg(Boolean checkFlg) {
		this.checkFlg = checkFlg;
	}

	public Boolean getSpecifyUser() {
		return specifyUser;
	}

	public void setSpecifyUser(Boolean specifyUser) {
		this.specifyUser = specifyUser;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Integer getMessageRetry() {
		return messageRetry;
	}

	public void setMessageRetry(Integer messageRetry) {
		this.messageRetry = messageRetry;
	}

	public Boolean getMessageRetryEndFlg() {
		return messageRetryEndFlg;
	}

	public void setMessageRetryEndFlg(Boolean messageRetryEndFlg) {
		this.messageRetryEndFlg = messageRetryEndFlg;
	}

	public Integer getMessageRetryEndValue() {
		return messageRetryEndValue;
	}

	public void setMessageRetryEndValue(Integer messageRetryEndValue) {
		this.messageRetryEndValue = messageRetryEndValue;
	}
}
