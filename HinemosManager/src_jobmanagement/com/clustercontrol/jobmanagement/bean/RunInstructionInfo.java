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
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RpaScreenshotTriggerTypeConstant;

/**
 * 実行指示情報を保持するクラス<BR>
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class RunInstructionInfo extends RunInfo implements Serializable {
	private static final long serialVersionUID = -4324117941296918253L;

	/** 実行種別 */
	private String runType;
	/** 入力ファイル */
	private String inputFile;
	/** ファイルリスト取得パス */
	private String filePath;

	/** ファイルチェックジョブ実行指示情報 */
	private RunInstructionFileCheckInfo runInstructionFileCheckInfo = null;
	/** RPAシナリオジョブ ログディレクトリ */
	private String rpaLogDirectory;
	/** RPAシナリオジョブ ログファイル名 */
	private String rpaLogFileName;
	/** RPAシナリオジョブ ログファイルエンコーディング */
	private String rpaLogFileEncoding;
	/** RPAシナリオジョブ ログファイル改行コード */
	private String rpaLogFileReturnCode;
	/** RPAシナリオジョブ ログファイル先頭パターン */
	private String rpaLogPatternHead; 
	/** RPAシナリオジョブ ログファイル終端パターン */
	private String rpaLogPatternTail; 
	/** RPAシナリオジョブ ログファイル最大読み取り文字数 */
	private Integer rpaLogMaxBytes;  
	/** RPAシナリオジョブ デフォルトの終了値 */
	private Integer rpaDefaultEndValue;
	/** RPAシナリオジョブ シナリオ終了値判定条件 */
	private List<RpaJobEndValueConditionInfo> rpaEndValueConditionInfoList = new ArrayList<>();
	/** RPAシナリオジョブ ログインされるまで待機する時間 */
	private Integer rpaLoginWaitMills;
	/** RPAシナリオジョブ スクリーンショット取得契機 */
	private Integer rpaScreenshotTriggerType;
	/** RPAシナリオジョブ スクリーンショットを取得する終了値 */
	private String rpaScreenshotEndValue;
	/** RPAシナリオジョブ スクリーンショットを取得する終了値判定条件 */
	private Integer rpaScreenshotEndValueCondition;
	/** RPAシナリオジョブ 正常値 下限 */
	private Integer rpaNormalEndValueFrom;
	/** RPAシナリオジョブ 正常値 上限 */
	private Integer rpaNormalEndValueTo;
	/** RPAシナリオジョブ 警告値 下限 */
	private Integer rpaWarnEndValueFrom;
	/** RPAシナリオジョブ 警告値 上限 */
	private Integer rpaWarnEndValueTo;
	/** RPAシナリオジョブ 実行ファイル名 */
	private String rpaExeName;
	/** RPAシナリオジョブ ツールエグゼキュータへ渡すシナリオ実行情報 */
	private RoboRunInfo rpaRoboRunInfo;

	/**
	 * 入力ファイルを返します。
	 * 
	 * @return 入力ファイル
	 */
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * 入力ファイルを設定します。
	 * 
	 * @param inputFile 入力ファイル
	 */
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * 実行種別を返します。
	 * 
	 * @return 実行種別
	 */
	public String getRunType() {
		return runType;
	}

	/**
	 * 実行種別を設定します。
	 * 
	 * @param runType 実行種別
	 */
	public void setRunType(String runType) {
		this.runType = runType;
	}

	/**
	 * ファイルリスト取得パスを返します。
	 * 
	 * @return ファイルリスト取得パス
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * ファイルリスト取得パスを設定します。
	 * 
	 * @param filePath ファイルリスト取得パス
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * ファイルチェックジョブ実行指示情報を返します。
	 * 
	 * @return
	 */
	@XmlTransient
	public RunInstructionFileCheckInfo getRunInstructionFileCheckInfo() {
		return runInstructionFileCheckInfo;
	}

	/**
	 * ファイルチェックジョブ実行指示情報を設定します。
	 * 
	 * @param
	 */
	public void setRunInstructionFileCheckInfo(RunInstructionFileCheckInfo runInstructionFileCheckInfo) {
		this.runInstructionFileCheckInfo = runInstructionFileCheckInfo;
	}

	/**
	 * RPAログファイルディレクトリを返します。
	 * 
	 * @return rpaLogDirectory RPAログファイルディレクトリ
	 */
	public String getRpaLogDirectory() {
		return rpaLogDirectory;
	}

	/**
	 * RPAログファイルディレクトリを設定します。
	 * 
	 * @param rpaLogDirectory RPAログファイルディレクトリ
	 */
	public void setRpaLogDirectory(String rpaLogDirectory) {
		this.rpaLogDirectory = rpaLogDirectory;
	}

	/**
	 * RPAログファイル名を返します。
	 * 
	 * @return rpaLogFileName RPAログファイル名
	 */
	public String getRpaLogFileName() {
		return rpaLogFileName;
	}

	/**
	 * RPAログファイル名を設定します。
	 * 
	 * @param rpaLogFileName RPAログファイル名
	 */
	public void setRpaLogFileName(String rpaLogFileName) {
		this.rpaLogFileName = rpaLogFileName;
	}

	/**
	 * RPAログファイルエンコーディングを返します。
	 * 
	 * @return rpaLogFileEncoding RPAログファイルエンコーディング
	 */
	public String getRpaLogFileEncoding() {
		return rpaLogFileEncoding;
	}

	/**
	 * RPAログファイルエンコーディングを設定します。
	 * 
	 * @param rpaLogFileEncoding RPAログファイルエンコーディング
	 */
	public void setRpaLogFileEncoding(String rpaLogFileEncoding) {
		this.rpaLogFileEncoding = rpaLogFileEncoding;
	}

	/**
	 * RPAログファイル改行コードを返します。
	 * 
	 * @return rpaLogFileReturnCode RPAログファイル改行コード
	 */
	public String getRpaLogFileReturnCode() {
		return rpaLogFileReturnCode;
	}

	/**
	 * RPAログファイル改行コードを設定します。
	 * 
	 * @param rpaLogFileReturnCode RPAログファイル改行コード
	 */
	public void setRpaLogFileReturnCode(String rpaLogFileReturnCode) {
		this.rpaLogFileReturnCode = rpaLogFileReturnCode;
	}

	/**
	 * RPAログファイル先頭パターンを返します。
	 * 
	 * @return rpaLogPatternHead RPAログファイル先頭パターン
	 */
	public String getRpaLogPatternHead() {
		return rpaLogPatternHead;
	}

	/**
	 * RPAログファイル先頭パターンを設定します。
	 * 
	 * @param rpaLogPatternHead RPAログファイル先頭パターン
	 */
	public void setRpaLogPatternHead(String rpaLogPatternHead) {
		this.rpaLogPatternHead = rpaLogPatternHead;
	}

	/**
	 * RPAログファイル終端パターンを返します。
	 * 
	 * @return rpaLogPatternTail RPAログファイル終端パターン
	 */
	public String getRpaLogPatternTail() {
		return rpaLogPatternTail;
	}

	/**
	 * RPAログファイル終端パターンを設定します。
	 * 
	 * @param rpaLogPatternTail RPAログファイル終端パターン
	 */
	public void setRpaLogPatternTail(String rpaLogPatternTail) {
		this.rpaLogPatternTail = rpaLogPatternTail;
	}

	/**
	 * RPAログファイル最大読み取り文字数を返します。
	 * 
	 * @return rpaLogMaxBytes RPAログファイル最大読み取り文字数
	 */
	public Integer getRpaLogMaxBytes() {
		return rpaLogMaxBytes;
	}

	/**
	 * RPAログファイル最大読み取り文字数を設定します。
	 * 
	 * @param rpaLogMaxBytes RPAログファイル最大読み取り文字数
	 */
	public void setRpaLogMaxBytes(Integer rpaLogMaxBytes) {
		this.rpaLogMaxBytes = rpaLogMaxBytes;
	}

	/**
	 * RPAデフォルトの終了値を返します。
	 * 
	 * @return rpaDefaultEndValue RPAデフォルトの終了値
	 */
	public Integer getRpaDefaultEndValue() {
		return rpaDefaultEndValue;
	}

	/**
	 * RPAデフォルトの終了値を設定します。
	 * 
	 * @param rpaDefaultEndValue RPAデフォルトの終了値
	 */
	public void setRpaDefaultEndValue(Integer rpaDefaultEndValue) {
		this.rpaDefaultEndValue = rpaDefaultEndValue;
	}

	/**
	 * RPAシナリオ終了値判定条件リストを返します。
	 * 
	 * @return rpaEndValueConditionInfoList RPAシナリオ終了値判定条件リスト
	 */
	public List<RpaJobEndValueConditionInfo> getRpaEndValueConditionInfoList() {
		return rpaEndValueConditionInfoList;
	}

	/**
	 * RPAシナリオ終了値判定条件リストを設定します。
	 * 
	 * @param rpaEndValueConditionInfoList RPAシナリオ終了値判定条件リスト
	 */
	public void setRpaEndValueConditionInfoList(
			List<RpaJobEndValueConditionInfo> rpaEndValueConditionInfoList) {
		this.rpaEndValueConditionInfoList = rpaEndValueConditionInfoList;
	}

	/**
	 * @return RPA ログインされるまで待機する時間を返します。
	 */
	public Integer getRpaLoginWaitMills() {
		return rpaLoginWaitMills;
	}

	/**
	 * @param RPA ログインされるまで待機する時間を設定します。
	 */
	public void setRpaLoginWaitMills(Integer rpaLoginWaitMills) {
		this.rpaLoginWaitMills = rpaLoginWaitMills;
	}

	/**
	 * @return RPA スクリーンショット取得契機を返します。
	 */
	public Integer getRpaScreenshotTriggerType() {
		return rpaScreenshotTriggerType;
	}

	/**
	 * @param RPA スクリーンショット取得契機を設定します。
	 */
	public void setRpaScreenshotTriggerType(Integer rpaScreenshotTriggerType) {
		this.rpaScreenshotTriggerType = rpaScreenshotTriggerType;
	}

	/**
	 * @return RPA スクリーンショットを取得する終了値を返します。
	 */
	public String getRpaScreenshotEndValue() {
		return rpaScreenshotEndValue;
	}

	/**
	 * @param RPA スクリーンショットを取得する終了値を設定します。
	 */
	public void setRpaScreenshotEndValue(String rpaScreenshotEndValue) {
		this.rpaScreenshotEndValue = rpaScreenshotEndValue;
	}

	/**
	 * @return RPA スクリーンショットを取得する終了値判定条件を返します。
	 */
	public Integer getRpaScreenshotEndValueCondition() {
		return rpaScreenshotEndValueCondition;
	}

	/**
	 * @param RPA スクリーンショットを取得する終了値判定条件を設定します。
	 */
	public void setRpaScreenshotEndValueCondition(Integer rpaScreenshotEndValueCondition) {
		this.rpaScreenshotEndValueCondition = rpaScreenshotEndValueCondition;
	}

	/**
	 * @return RPA 終了値の正常値下限を返します。
	 */
	public Integer getRpaNormalEndValueFrom() {
		return rpaNormalEndValueFrom;
	}

	/**
	 * @param RPA 終了値の正常値下限を設定します。
	 */
	public void setRpaNormalEndValueFrom(Integer rpaNormalEndValueFrom) {
		this.rpaNormalEndValueFrom = rpaNormalEndValueFrom;
	}

	/**
	 * @return RPA 終了値の正常値上限を返します。
	 */
	public Integer getRpaNormalEndValueTo() {
		return rpaNormalEndValueTo;
	}

	/**
	 * @param RPA 終了値の正常値上限を設定します。
	 */
	public void setRpaNormalEndValueTo(Integer rpaNormalEndValueTo) {
		this.rpaNormalEndValueTo = rpaNormalEndValueTo;
	}

	/**
	 * @return RPA 終了値の警告値下限を返します。
	 */
	public Integer getRpaWarnEndValueFrom() {
		return rpaWarnEndValueFrom;
	}

	/**
	 * @param RPA 終了値の警告値下限を設定します。
	 */
	public void setRpaWarnEndValueFrom(Integer rpaWarnEndValueFrom) {
		this.rpaWarnEndValueFrom = rpaWarnEndValueFrom;
	}


	/**
	 * @return RPA 終了値の警告値上限を返します。
	 */
	public Integer getRpaWarnEndValueTo() {
		return rpaWarnEndValueTo;
	}

	/**
	 * @return RPA 終了値の警告値下限を返します。
	 */
	public void setRpaWarnEndValueTo(Integer rpaWarnEndValueTo) {
		this.rpaWarnEndValueTo = rpaWarnEndValueTo;
	}

	/**
	 * @return RPA 実行ファイル名を返します。
	 */
	public String getRpaExeName() {
		return rpaExeName;
	}

	/**
	 * @param RPA 実行ファイル名を設定します。
	 */
	public void setRpaExeName(String rpaExeName) {
		this.rpaExeName = rpaExeName;
	}

	/**
	 * RPAツールエグゼキュータへ渡すシナリオ実行情報を返します。
	 * @return
	 */
	public RoboRunInfo getRpaRoboRunInfo() {
		return rpaRoboRunInfo;
	}

	/**
	 * RPAツールエグゼキュータへ渡すシナリオ実行情報を設定します。
	 * @param rpaRoboRunInfo
	 */
	public void setRpaRoboRunInfo(RoboRunInfo rpaRoboRunInfo) {
		this.rpaRoboRunInfo = rpaRoboRunInfo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + ((inputFile == null) ? 0 : inputFile.hashCode());
		result = prime * result + ((rpaDefaultEndValue == null) ? 0 : rpaDefaultEndValue.hashCode());
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result
				+ ((inputFile == null) ? 0 : inputFile.hashCode());
		result = prime * result + ((runType == null) ? 0 : runType.hashCode());
		result = prime * result + ((runInstructionFileCheckInfo == null) ? 0 : runInstructionFileCheckInfo.hashCode());
		result = prime * result + ((rpaEndValueConditionInfoList == null) ? 0 : rpaEndValueConditionInfoList.hashCode());
		result = prime * result + ((rpaExeName == null) ? 0 : rpaExeName.hashCode());
		result = prime * result + ((rpaLogDirectory == null) ? 0 : rpaLogDirectory.hashCode());
		result = prime * result + ((rpaLogFileEncoding == null) ? 0 : rpaLogFileEncoding.hashCode());
		result = prime * result + ((rpaLogFileName == null) ? 0 : rpaLogFileName.hashCode());
		result = prime * result + ((rpaLogFileReturnCode == null) ? 0 : rpaLogFileReturnCode.hashCode());
		result = prime * result + ((rpaLogMaxBytes == null) ? 0 : rpaLogMaxBytes.hashCode());
		result = prime * result + ((rpaLogPatternHead == null) ? 0 : rpaLogPatternHead.hashCode());
		result = prime * result + ((rpaLogPatternTail == null) ? 0 : rpaLogPatternTail.hashCode());
		result = prime * result + ((rpaLoginWaitMills == null) ? 0 : rpaLoginWaitMills.hashCode());
		result = prime * result + ((rpaNormalEndValueFrom == null) ? 0 : rpaNormalEndValueFrom.hashCode());
		result = prime * result + ((rpaNormalEndValueTo == null) ? 0 : rpaNormalEndValueTo.hashCode());
		result = prime * result + ((rpaRoboRunInfo == null) ? 0 : rpaRoboRunInfo.hashCode());
		result = prime * result + ((rpaScreenshotEndValue == null) ? 0 : rpaScreenshotEndValue.hashCode());
		result = prime * result
				+ ((rpaScreenshotEndValueCondition == null) ? 0 : rpaScreenshotEndValueCondition.hashCode());
		result = prime * result + ((rpaScreenshotTriggerType == null) ? 0 : rpaScreenshotTriggerType.hashCode());
		result = prime * result + ((rpaWarnEndValueFrom == null) ? 0 : rpaWarnEndValueFrom.hashCode());
		result = prime * result + ((rpaWarnEndValueTo == null) ? 0 : rpaWarnEndValueTo.hashCode());
		result = prime * result + ((runType == null) ? 0 : runType.hashCode());
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
		RunInstructionInfo other = (RunInstructionInfo) obj;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (inputFile == null) {
			if (other.inputFile != null)
				return false;
		} else if (!inputFile.equals(other.inputFile))
			return false;
		if (rpaDefaultEndValue == null) {
			if (other.rpaDefaultEndValue != null)
				return false;
		} else if (!rpaDefaultEndValue.equals(other.rpaDefaultEndValue))
			return false;
		if (rpaEndValueConditionInfoList == null) {
			if (other.rpaEndValueConditionInfoList != null)
				return false;
		} else if (!rpaEndValueConditionInfoList.equals(other.rpaEndValueConditionInfoList))
			return false;
		if (rpaExeName == null) {
			if (other.rpaExeName != null)
				return false;
		} else if (!rpaExeName.equals(other.rpaExeName))
			return false;
		if (runInstructionFileCheckInfo == null) {
			if (other.runInstructionFileCheckInfo != null)
				return false;
		} else if (!runInstructionFileCheckInfo.equals(other.runInstructionFileCheckInfo))
			return false;
		if (rpaLogDirectory == null) {
			if (other.rpaLogDirectory != null)
				return false;
		} else if (!rpaLogDirectory.equals(other.rpaLogDirectory))
			return false;
		if (rpaLogFileEncoding == null) {
			if (other.rpaLogFileEncoding != null)
				return false;
		} else if (!rpaLogFileEncoding.equals(other.rpaLogFileEncoding))
			return false;
		if (rpaLogFileName == null) {
			if (other.rpaLogFileName != null)
				return false;
		} else if (!rpaLogFileName.equals(other.rpaLogFileName))
			return false;
		if (rpaLogFileReturnCode == null) {
			if (other.rpaLogFileReturnCode != null)
				return false;
		} else if (!rpaLogFileReturnCode.equals(other.rpaLogFileReturnCode))
			return false;
		if (rpaLogMaxBytes == null) {
			if (other.rpaLogMaxBytes != null)
				return false;
		} else if (!rpaLogMaxBytes.equals(other.rpaLogMaxBytes))
			return false;
		if (rpaLogPatternHead == null) {
			if (other.rpaLogPatternHead != null)
				return false;
		} else if (!rpaLogPatternHead.equals(other.rpaLogPatternHead))
			return false;
		if (rpaLogPatternTail == null) {
			if (other.rpaLogPatternTail != null)
				return false;
		} else if (!rpaLogPatternTail.equals(other.rpaLogPatternTail))
			return false;
		if (rpaLoginWaitMills == null) {
			if (other.rpaLoginWaitMills != null)
				return false;
		} else if (!rpaLoginWaitMills.equals(other.rpaLoginWaitMills))
			return false;
		if (rpaNormalEndValueFrom == null) {
			if (other.rpaNormalEndValueFrom != null)
				return false;
		} else if (!rpaNormalEndValueFrom.equals(other.rpaNormalEndValueFrom))
			return false;
		if (rpaNormalEndValueTo == null) {
			if (other.rpaNormalEndValueTo != null)
				return false;
		} else if (!rpaNormalEndValueTo.equals(other.rpaNormalEndValueTo))
			return false;
		if (rpaRoboRunInfo == null) {
			if (other.rpaRoboRunInfo != null)
				return false;
		} else if (!rpaRoboRunInfo.equals(other.rpaRoboRunInfo))
			return false;
		if (rpaScreenshotEndValue == null) {
			if (other.rpaScreenshotEndValue != null)
				return false;
		} else if (!rpaScreenshotEndValue.equals(other.rpaScreenshotEndValue))
			return false;
		if (rpaScreenshotEndValueCondition == null) {
			if (other.rpaScreenshotEndValueCondition != null)
				return false;
		} else if (!rpaScreenshotEndValueCondition.equals(other.rpaScreenshotEndValueCondition))
			return false;
		if (rpaScreenshotTriggerType == null) {
			if (other.rpaScreenshotTriggerType != null)
				return false;
		} else if (!rpaScreenshotTriggerType.equals(other.rpaScreenshotTriggerType))
			return false;
		if (rpaWarnEndValueFrom == null) {
			if (other.rpaWarnEndValueFrom != null)
				return false;
		} else if (!rpaWarnEndValueFrom.equals(other.rpaWarnEndValueFrom))
			return false;
		if (rpaWarnEndValueTo == null) {
			if (other.rpaWarnEndValueTo != null)
				return false;
		} else if (!rpaWarnEndValueTo.equals(other.rpaWarnEndValueTo))
			return false;
		if (runType == null) {
			if (other.runType != null)
				return false;
		} else if (!runType.equals(other.runType))
			return false;
		return true;
	}

	public static void main(String args[]) {
		RunInstructionInfo a = new RunInstructionInfo();
		a.setCheckSum("checksum");
		a.setCommand("command");
		a.setCommandType(1);
		a.setFacilityId("facilityId");
		a.setFilePath("filePath");
		a.setInputFile("inputFile");
		a.setJobId("jobId");
		a.setJobunitId("jobunitId");
		a.setPublicKey("publicKey");
		a.setRunType("runType");
		a.setSessionId("sessionId");
		a.setUser("user");
		a.setRpaLogDirectory("C:/RPA/log");
		a.setRpaLogFileName("winactor.log");
		a.setRpaLogFileEncoding("UTF-8");
		a.setRpaLogFileReturnCode("CRLF");
		a.setRpaLogPatternHead("");
		a.setRpaLogPatternTail("");
		a.setRpaLogMaxBytes(1024);
		a.setRpaDefaultEndValue(0);
		a.setRpaLoginWaitMills(1000);
		a.setRpaScreenshotTriggerType(RpaScreenshotTriggerTypeConstant.END_DELAY);
		a.setRpaNormalEndValueFrom(0);
		a.setRpaNormalEndValueTo(0);
		a.setRpaWarnEndValueFrom(1);
		a.setRpaWarnEndValueTo(1);
		a.setRpaRoboRunInfo(new RoboRunInfo(
				0L, "sessionId", "jobunitId", "jobId", "fasilityId", "startCommand", "stopCommand",
				true, true, true));
		a.setRpaEndValueConditionInfoList(Arrays.asList(new RpaJobEndValueConditionInfo()));

		RunInstructionInfo b = new RunInstructionInfo();
		b.setCheckSum("checksum");
		b.setCommand("command");
		b.setCommandType(1);
		b.setFacilityId("facilityId");
		b.setFilePath("filePath");
		b.setInputFile("inputFile");
		b.setJobId("jobId");
		b.setJobunitId("jobunitId");
		b.setPublicKey("publicKey");
		b.setRunType("runType");
		b.setSessionId("sessionId");
		b.setUser("user");
		b.setRpaLogDirectory("C:/RPA/log");
		b.setRpaLogFileName("winactor.log");
		b.setRpaLogFileEncoding("UTF-8");
		b.setRpaLogFileReturnCode("CRLF");
		b.setRpaLogPatternHead("");
		b.setRpaLogPatternTail("");
		b.setRpaLogMaxBytes(1024);
		b.setRpaDefaultEndValue(0);
		b.setRpaLoginWaitMills(1000);
		b.setRpaScreenshotTriggerType(RpaScreenshotTriggerTypeConstant.END_DELAY);
		b.setRpaNormalEndValueFrom(0);
		b.setRpaNormalEndValueTo(0);
		b.setRpaWarnEndValueFrom(1);
		b.setRpaWarnEndValueTo(1);
		b.setRpaRoboRunInfo(new RoboRunInfo(
				0L, "sessionId", "jobunitId", "jobId", "fasilityId", "startCommand", "stopCommand",
				true, true, true));
		b.setRpaEndValueConditionInfoList(Arrays.asList(new RpaJobEndValueConditionInfo()));

		System.out.println("a.equals(b)=" + a.equals(b));
	}
}
