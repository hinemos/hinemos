/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.CommandRetryEndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.StopTypeEnum;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class JobCommandInfoRequest implements RequestDto {
	/** ファシリティID */
	private String facilityID;

	/** スコープ処理 */
	@RestBeanConvertEnum
	private ProcessingMethodEnum processingMethod;

	/** マネージャから配布 */
	private Boolean managerDistribution = false;
	
	/** スクリプト名 */
	private String scriptName;
	
	/** スクリプトエンコーディング */
	private String scriptEncoding;
	
	/** スクリプト */
	private String scriptContent;
	
	/** 起動コマンド */
	private String startCommand;

	/** コマンド停止方式 */
	@RestBeanConvertEnum
	private StopTypeEnum stopType;

	/** 停止コマンド */
	private String stopCommand;

	/** ユーザ種別 */
	private Boolean specifyUser = false;

	/** 実効ユーザ */
	private String user;

	/** リトライ回数 */
	private Integer messageRetry;

	/** コマンド実行失敗時終了フラグ */
	private Boolean messageRetryEndFlg = false;

	/** コマンド実行失敗時終了値 */
	private Integer messageRetryEndValue = 0;

	/** 繰り返し実行フラグ */
	private Boolean commandRetryFlg = false;

	/** 繰り返し実行回数 */
	private Integer commandRetry;
	
	/** 繰り返し完了状態 */
	@RestBeanConvertEnum
	private CommandRetryEndStatusEnum commandRetryEndStatus;

	/** 標準出力のファイル出力情報 - 標準出力 */
	private JobOutputInfoRequest normalJobOutputInfo;

	/** 標準出力のファイル出力情報 - 標準エラー出力 */
	private JobOutputInfoRequest errorJobOutputInfo;

	/** ランタイムジョブ変数詳細情報 */
	private ArrayList<JobCommandParamRequest> jobCommandParamList = new ArrayList<>();

	/** 環境変数 */
	private List<JobEnvVariableInfoRequest> envVariable = new ArrayList<>();

	public JobCommandInfoRequest() {
	}


	public String getFacilityID() {
		return facilityID;
	}


	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	public ProcessingMethodEnum getProcessingMethod() {
		return processingMethod;
	}


	public void setProcessingMethod(ProcessingMethodEnum processingMethod) {
		this.processingMethod = processingMethod;
	}


	public Boolean getManagerDistribution() {
		return managerDistribution;
	}


	public void setManagerDistribution(Boolean managerDistribution) {
		this.managerDistribution = managerDistribution;
	}


	public String getScriptName() {
		return scriptName;
	}


	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}


	public String getScriptEncoding() {
		return scriptEncoding;
	}


	public void setScriptEncoding(String scriptEncoding) {
		this.scriptEncoding = scriptEncoding;
	}


	public String getScriptContent() {
		return scriptContent;
	}


	public void setScriptContent(String scriptContent) {
		this.scriptContent = scriptContent;
	}


	public String getStartCommand() {
		return startCommand;
	}


	public void setStartCommand(String startCommand) {
		this.startCommand = startCommand;
	}


	public StopTypeEnum getStopType() {
		return stopType;
	}


	public void setStopType(StopTypeEnum stopType) {
		this.stopType = stopType;
	}


	public String getStopCommand() {
		return stopCommand;
	}


	public void setStopCommand(String stopCommand) {
		this.stopCommand = stopCommand;
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


	public Boolean getCommandRetryFlg() {
		return commandRetryFlg;
	}


	public void setCommandRetryFlg(Boolean commandRetryFlg) {
		this.commandRetryFlg = commandRetryFlg;
	}


	public Integer getCommandRetry() {
		return commandRetry;
	}


	public void setCommandRetry(Integer commandRetry) {
		this.commandRetry = commandRetry;
	}


	public CommandRetryEndStatusEnum getCommandRetryEndStatus() {
		return commandRetryEndStatus;
	}


	public void setCommandRetryEndStatus(CommandRetryEndStatusEnum commandRetryEndStatus) {
		this.commandRetryEndStatus = commandRetryEndStatus;
	}

	public JobOutputInfoRequest getNormalJobOutputInfo() {
		return normalJobOutputInfo;
	}

	public void setNormalJobOutputInfo(JobOutputInfoRequest normalJobOutputInfo) {
		this.normalJobOutputInfo = normalJobOutputInfo;
	}

	public JobOutputInfoRequest getErrorJobOutputInfo() {
		return errorJobOutputInfo;
	}


	public void setErrorJobOutputInfo(JobOutputInfoRequest errorJobOutputInfo) {
		this.errorJobOutputInfo = errorJobOutputInfo;
	}


	public ArrayList<JobCommandParamRequest> getJobCommandParamList() {
		return jobCommandParamList;
	}


	public void setJobCommandParamList(ArrayList<JobCommandParamRequest> jobCommandParamList) {
		this.jobCommandParamList = jobCommandParamList;
	}


	public List<JobEnvVariableInfoRequest> getEnvVariable() {
		return envVariable;
	}


	public void setEnvVariable(List<JobEnvVariableInfoRequest> envVariable) {
		this.envVariable = envVariable;
	}


	@Override
	public void correlationCheck() throws InvalidSetting {
		if (normalJobOutputInfo != null) {
			normalJobOutputInfo.correlationCheck();
		}
		if (errorJobOutputInfo != null) {
			errorJobOutputInfo.correlationCheck();
		}
		if (jobCommandParamList != null) {
			for (JobCommandParamRequest req : jobCommandParamList) {
				req.correlationCheck();
			}
		}
		if (envVariable != null) {
			for (JobEnvVariableInfoRequest req : envVariable) {
				req.correlationCheck();
			}
		}

		// [標準出力と同じ出力先を使用する]がNullの場合はエラー
		if (errorJobOutputInfo != null && errorJobOutputInfo.getSameNormalFlg() == null) {
			String r1 = RestItemNameResolver.resolveItenName(errorJobOutputInfo.getClass(), "sameNormalFlg");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
		}

		//標準エラー出力設定の出力先が同じとする設定の場合、標準出力の設定がなかったら不正な設定とする
		if (errorJobOutputInfo != null && errorJobOutputInfo.getSameNormalFlg()) {
			if (normalJobOutputInfo == null) {
				throw new InvalidSetting("please set normalJobOutputInfo if sameNomalFlg of errorJobOutputInfo is true.");
			}
			//標準出力のディレクトリチェック
			if (normalJobOutputInfo.getDirectory() == null ||
					normalJobOutputInfo.getDirectory().isEmpty()) {
				throw new InvalidSetting("please set directory of normalJobOutputInfo if sameNomalFlg of errorJobOutputInfo is true.");
			}
			//標準出力のファイル名チェック
			if (normalJobOutputInfo.getFileName() == null ||
					normalJobOutputInfo.getFileName().isEmpty()) {
				throw new InvalidSetting("please set fileName of normalJobOutputInfo if sameNomalFlg of errorJobOutputInfo is true.");
			}
		}
	}

}
