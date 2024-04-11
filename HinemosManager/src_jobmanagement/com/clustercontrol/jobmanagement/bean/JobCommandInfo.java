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

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.dto.deserializer.ConvertNewlineCharacterDeserializer;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.annotation.EnumerateConstant;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer.EnumToConstantDeserializer;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.CommandRetryEndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.StopTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer.ConstantToEnumSerializer;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer.LanguageTranslateSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * ジョブのコマンドに関する情報を保持するクラス
 *
 * @version 2.0.0
 * @since 1.0.0
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE) //JSONから変換する際、getter名、setter名を無視し、フィールド名のみを参照して変換する。
public class JobCommandInfo implements Serializable, RequestDto {
	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 333607610499761260L;

	@JsonIgnore
	private static Log m_log = LogFactory.getLog( JobCommandInfo.class );

	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	@JsonSerialize(using=LanguageTranslateSerializer.class)
	private String scope;

	/** スコープ処理 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=ProcessingMethodEnum.class)
	private Integer processingMethod = 0;

	/** マネージャから配布 */
	private Boolean managerDistribution = false;
	
	/** スクリプト名 */
	private String scriptName;
	
	/** スクリプトエンコーディング */
	private String scriptEncoding;
	
	/** スクリプト */
	@JsonDeserialize(using=ConvertNewlineCharacterDeserializer.class)
	private String scriptContent;
	
	/** 起動コマンド */
	private String startCommand;

	/** コマンド停止方式 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=StopTypeEnum.class)
	private Integer stopType;

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
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=CommandRetryEndStatusEnum.class)
	private Integer commandRetryEndStatus;

	/** 標準出力のファイル出力情報 - 標準出力 */
	private JobOutputInfo normalJobOutputInfo;

	/** 標準出力のファイル出力情報 - 標準エラー出力 */
	private JobOutputInfo errorJobOutputInfo;

	/** ランタイムジョブ変数詳細情報 */
	private ArrayList<JobCommandParam> jobCommandParamList;

	/** 環境変数 */
	private List<JobEnvVariableInfo> envVariable;

	/**
	 * コマンド実行失敗時終了フラグを返す。<BR>
	 * @return コマンド実行失敗時終了フラグ
	 */
	public Boolean getMessageRetryEndFlg() {
		return messageRetryEndFlg;
	}

	/**
	 * コマンド実行失敗時終了フラグを設定する。<BR>
	 * @param messageRetryEndFlg コマンド実行失敗時終了フラグ
	 */
	public void setMessageRetryEndFlg(Boolean messageRetryEndFlg) {
		this.messageRetryEndFlg = messageRetryEndFlg;
	}

	/**
	 * コマンド実行失敗時終了値を返す。<BR>
	 * @return コマンド実行失敗時終了値
	 */
	public Integer getMessageRetryEndValue() {
		return messageRetryEndValue;
	}

	/**
	 * コマンド実行失敗時終了値を設定する。<BR>
	 * @param messageRetryEndValue コマンド実行失敗時終了値
	 */
	public void setMessageRetryEndValue(Integer messageRetryEndValue) {
		this.messageRetryEndValue = messageRetryEndValue;
	}

	/**
	 * スコープを返す。<BR>
	 * @return スコープ
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * スコープを設定する。<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	/**
	 * マネージャから配布するかしないかを返す。<BR>
	 * @return マネージャから配布するかしないか
	 */
	public Boolean getManagerDistribution() {
		return managerDistribution;
	}

	/**
	 * マネージャから配布するかしないかを設定する。<BR>
	 * @param managerDistribution マネージャから配布するかしないか
	 */
	public void setManagerDistribution(Boolean managerDistribution) {
		this.managerDistribution = managerDistribution;
	}
	
	/**
	 * スクリプト名を返す。<BR>
	 * @return スクリプト名
	 */
	public String getScriptName() {
		return scriptName;
	}

	/**
	 * スクリプト名を設定する。<BR>
	 * @param scriptName スクリプト名
	 */
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
	
	/**
	 * スクリプトエンコーディングを返す。<BR>
	 * @return スクリプトエンコーディング
	 */
	public String getScriptEncoding() {
		return scriptEncoding;
	}

	/**
	 * スクリプトエンコーディングを設定する。<BR>
	 * @param scriptEncoding スクリプトエンコーディング
	 */
	public void setScriptEncoding(String scriptEncoding) {
		this.scriptEncoding = scriptEncoding;
	}
	
	/**
	 * スクリプトを返す。<BR>
	 * @return スクリプト
	 */
	public String getScriptContent() {
		return scriptContent;
	}

	/**
	 * スクリプトを設定する。<BR>
	 * @param scriptContent スクリプト
	 */
	public void setScriptContent(String scriptContent) {
		this.scriptContent = scriptContent;
	}

	/**
	 * 起動コマンドを返す。<BR>
	 * @return 起動コマンド
	 */
	public String getStartCommand() {
		return startCommand;
	}

	/**
	 * 起動コマンドを設定する。<BR>
	 * @param startCommand 起動コマンド
	 */
	public void setStartCommand(String startCommand) {
		this.startCommand = startCommand;
	}

	/**
	 * ファシリティIDを返す。<BR>
	 * @return ファシリティID
	 */
	public String getFacilityID() {
		return facilityID;
	}

	/**
	 * ファシリティIDを設定する。<BR>
	 * @param facilityID ファシリティID
	 */
	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	/**
	 * 停止種別を返す。<BR>
	 * @return 停止種別
	 */
	public Integer getStopType() {
		return stopType;
	}

	/**
	 * 停止種別を設定する。<BR>
	 * @param stopType 停止種別
	 */
	public void setStopType(Integer stopType) {
		this.stopType = stopType;
	}

	/**
	 * 停止コマンドを返す。<BR>
	 * @return 停止コマンド
	 */
	public String getStopCommand() {
		return stopCommand;
	}

	/**
	 * 停止コマンドを設定する。<BR>
	 * @param stopCommand 停止コマンド
	 */
	public void setStopCommand(String stopCommand) {
		this.stopCommand = stopCommand;
	}

	/**
	 * スコープ処理を返す。<BR>
	 * @return スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public Integer getProcessingMethod() {
		return processingMethod;
	}

	/**
	 * スコープ処理を設定する。<BR>
	 * @param processingMethod スコープ処理
	 * @see com.clustercontrol.bean.ProcessingMethodConstant
	 */
	public void setProcessingMethod(Integer processingMethod) {
		this.processingMethod = processingMethod;
	}

	/**
	 * ユーザ種別を返す。<BR>
	 * @return ユーザ種別
	 */
	public Boolean getSpecifyUser() {
		return specifyUser;
	}

	/**
	 * ユーザ種別を設定する。<BR>
	 * @param specifyUser ユーザ種別
	 */
	public void setSpecifyUser(Boolean specifyUser) {
		this.specifyUser = specifyUser;
	}

	/**
	 * 実効ユーザを返す。<BR>
	 * @return 実効ユーザ
	 */
	public String getUser() {
		return user;
	}

	/**
	 * 実効ユーザを設定する。<BR>
	 * @param user 実効ユーザ
	 */
	public void setUser(String user) {
		this.user = user;
	}


	/**
	 * リトライ回数を返す。<BR>
	 * @return リトライ回数
	 */
	public Integer getMessageRetry() {
		return messageRetry;
	}

	/**
	 * リトライ回数を設定する。<BR>
	 * @param messageRetry リトライ回数
	 */
	public void setMessageRetry(Integer messageRetry) {
		this.messageRetry = messageRetry;
	}


	/**
	 * 繰り返し実行フラグを返す。<BR>
	 * @return 繰り返し実行フラグ
	 */
	public Boolean getCommandRetryFlg() {
		return commandRetryFlg;
	}

	/**
	 * 繰り返し実行フラグを設定する。<BR>
	 * @param commandRetryFlg 繰り返し実行フラグ
	 */
	public void setCommandRetryFlg(Boolean commandRetryFlg) {
		this.commandRetryFlg = commandRetryFlg;
	}


	/**
	 * 繰り返し実行回数を返す。<BR>
	 * @return 繰り返し実行回数
	 */
	public Integer getCommandRetry() {
		return commandRetry;
	}

	/**
	 * 繰り返し実行回数を設定する。<BR>
	 * @param commandRetry 繰り返し実行回数
	 */
	public void setCommandRetry(Integer commandRetry) {
		this.commandRetry = commandRetry;
	}
	
	/**
	 * 繰り返し完了状態を返す。<BR>
	 * @return
	 */
	public Integer getCommandRetryEndStatus() {
		return commandRetryEndStatus;
	}

	/**
	 * 繰り返し完了状態を設定する。<BR>
	 * @param commandRetryEndStatus
	 */
	public void setCommandRetryEndStatus(Integer commandRetryEndStatus) {
		this.commandRetryEndStatus = commandRetryEndStatus;
	}

	/**
	 * 標準出力のファイル出力情報 - 標準出力を返す。<BR>
	 * @return 標準出力のファイル出力情報 - 標準出力
	 */
	public JobOutputInfo getNormalJobOutputInfo() {
		return normalJobOutputInfo;
	}

	/**
	 * 標準出力のファイル出力情報 - 標準出力を設定する。<BR>
	 * @param 標準出力のファイル出力情報 - 標準出力
	 */
	public void setNormalJobOutputInfo(JobOutputInfo normalJobOutputInfo) {
		this.normalJobOutputInfo = normalJobOutputInfo;
	}

	/**
	 * 標準出力のファイル出力情報 - 標準エラー出力を返す。<BR>
	 * @return 標準出力のファイル出力情報 - 標準エラー出力
	 */
	public JobOutputInfo getErrorJobOutputInfo() {
		return errorJobOutputInfo;
	}

	/**
	 * 標準出力のファイル出力情報 - 標準エラー出力を設定する。<BR>
	 * @param 標準出力のファイル出力情報 - 標準エラー出力
	 */
	public void setErrorJobOutputInfo(JobOutputInfo errorJobOutputInfo) {
		this.errorJobOutputInfo = errorJobOutputInfo;
	}

	/**
	 * 環境変数のリストを返す。<BR>
	 * @return 環境変数のリスト
	 */
	public List<JobEnvVariableInfo> getEnvVariableInfo() {
		return envVariable;
	}

	/**
	 * 環境変数のリストを設定する。<BR>
	 * @param 環境変数のリスト
	 */
	public void setEnvVariableInfo(List<JobEnvVariableInfo> envVariable) {
		this.envVariable = envVariable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((commandRetry == null) ? 0 : commandRetry.hashCode());
		result = prime
				* result
				+ ((commandRetryFlg == null) ? 0 : commandRetryFlg
						.hashCode());
		result = prime
				* result
				+ ((commandRetryEndStatus == null) ? 0 : commandRetryEndStatus
						.hashCode());
		result = prime * result
				+ ((facilityID == null) ? 0 : facilityID.hashCode());
		result = prime * result
				+ ((messageRetry == null) ? 0 : messageRetry.hashCode());
		result = prime
				* result
				+ ((messageRetryEndFlg == null) ? 0 : messageRetryEndFlg
						.hashCode());
		result = prime
				* result
				+ ((messageRetryEndValue == null) ? 0
						: messageRetryEndValue.hashCode());
		result = prime
				* result
				+ ((processingMethod == null) ? 0 : processingMethod
						.hashCode());
		result = prime * result
				+ ((specifyUser == null) ? 0 : specifyUser.hashCode());
		result = prime * result
				+ ((startCommand == null) ? 0 : startCommand.hashCode());
		result = prime * result
				+ ((stopCommand == null) ? 0 : stopCommand.hashCode());
		result = prime * result
				+ ((stopType == null) ? 0 : stopType.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((managerDistribution == null) ? 0 : managerDistribution.hashCode());
		result = prime * result + ((scriptName == null) ? 0 : scriptName.hashCode());
		result = prime * result + ((scriptEncoding == null) ? 0 : scriptEncoding.hashCode());
		result = prime * result + ((scriptContent == null) ? 0 : scriptContent.hashCode());
		result = prime * result + ((envVariable == null) ? 0 : envVariable.hashCode());
		result = prime * result + ((normalJobOutputInfo == null) ? 0 : normalJobOutputInfo.hashCode());
		result = prime * result + ((errorJobOutputInfo == null) ? 0 : errorJobOutputInfo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JobCommandInfo)) {
			return false;
		}
		JobCommandInfo o1 = this;
		JobCommandInfo o2 = (JobCommandInfo)o;

		boolean ret = false;
		// スコープ(階層)は比較しない。
		ret = 	equalsSub(o1.getMessageRetryEndFlg(), o2.getMessageRetryEndFlg()) &&
				equalsSub(o1.getMessageRetryEndValue(), o2.getMessageRetryEndValue()) &&
				equalsSub(o1.getFacilityID(), o2.getFacilityID()) &&
				equalsSub(o1.getMessageRetry(), o2.getMessageRetry()) &&
				equalsSub(o1.getCommandRetryFlg(), o2.getCommandRetryFlg()) &&
				equalsSub(o1.getCommandRetry(), o2.getCommandRetry()) &&
				equalsSub(o1.getCommandRetryEndStatus(), o2.getCommandRetryEndStatus()) &&
				equalsSub(o1.getProcessingMethod(), o2.getProcessingMethod()) &&
				equalsSub(o1.getSpecifyUser(), o2.getSpecifyUser()) &&
				equalsSub(o1.getStartCommand(), o2.getStartCommand()) &&
				equalsSub(o1.getStopCommand(), o2.getStopCommand()) &&
				equalsSub(o1.getStopType(), o2.getStopType()) &&
				equalsSub(o1.getUser(), o2.getUser()) &&
				equalsArray(o1.getJobCommandParamList(), o2.getJobCommandParamList()) &&
				equalsSub(o1.getUser(), o2.getUser()) &&
				equalsSub(o1.getManagerDistribution(), o2.getManagerDistribution()) &&
				equalsSub(o1.getScriptName(), o2.getScriptName()) &&
				equalsSub(o1.getScriptEncoding(), o2.getScriptEncoding()) &&
				equalsSub(o1.getScriptContent(), o2.getScriptContent()) &&
				equalsSub(o1.getEnvVariableInfo(), o2.getEnvVariableInfo()) &&
				equalsSub(o1.getNormalJobOutputInfo(), o2.getNormalJobOutputInfo()) &&
				equalsSub(o1.getErrorJobOutputInfo(), o2.getErrorJobOutputInfo());
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

	private boolean equalsArray(ArrayList<?> list1, ArrayList<?> list2) {
		if (list1 != null && !list1.isEmpty()) {
			if (list2 != null && list1.size() == list2.size()) {
				Object[] ary1 = list1.toArray();
				Object[] ary2 = list2.toArray();
				Arrays.sort(ary1);
				Arrays.sort(ary2);

				for (int i = 0; i < ary1.length; i++) {
					if (!ary1[i].equals(ary2[i])) {
						if (m_log.isTraceEnabled()) {
							m_log.trace("equalsArray : " + ary1[i] + "!=" + ary2[i]);
						}
						return false;
					}
				}
				return true;
			}
		} else if (list2 == null || list2.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 単体テスト用
	 * @param args
	 */
	public static void main (String args[]) {
		testEquals();
	}
	/**
	 * 単体テスト
	 */
	public static void testEquals() {
		System.out.println("*** ALL ***");
		JobCommandInfo info1 = createSampleInfo();
		JobCommandInfo info2 = createSampleInfo();
		judge(true,info1.equals(info2));

		String[] str = {
				"コマンド実行失敗時終了フラグ",
				"コマンド実行失敗時終了値",
				"ファシリティID",
				"リトライ回数",
				"スコープ処理",
				"スコープ",
				"ユーザ種別",
				"起動コマンド",
				"停止コマンド",
				"コマンド停止方式",
				"実効ユーザ",
				"マネージャから配布",
				"スクリプト名",
				"スクリプトエンコーディング",
				"スクリプト",
				"環境変数"
		};
		/**
		 * カウントアップするごとに
		 * パラメータ1つ変えて単体テスト実行する
		 */
		for (int i = 0; i < 16; i++) {
			info2 = createSampleInfo();
			switch (i) {
			case 0 :
				info2.setMessageRetryEndFlg(true);
				break;
			case 1 :
				info2.setMessageRetryEndValue(1);
				break;
			case 2 :
				info2.setFacilityID("facility_ID");
				break;
			case 3 :
				info2.setMessageRetry(1);
				break;
			case 4 :
				info2.setProcessingMethod(1);
				break;
			case 5 :
				info2.setScope("Stope");
				break;
			case 6 :
				info2.setSpecifyUser(true);
				break;
			case 7 :
				info2.setStartCommand("echo");
				break;
			case 8 :
				info2.setStopCommand("echo 1");
				break;
			case 9 :
				info2.setStopType(1);
				break;
			case 10 :
				info2.setUser("admin");
				break;
			case 11 :
				info2.setManagerDistribution(true);
				break;
			case 12 :
				info2.setScriptName("test2.sh");
				break;
			case 13 :
				info2.setScriptEncoding("MS932");
				break;
			case 14 :
				info2.setScriptContent("echo test2");
				break;
			case 15 :
				info2.setEnvVariableInfo(createEnvVariableList("bbb"));
				break;
			default:
				break;
			}
			System.out.println("*** 「" + str[i] + "」 のみ違う***");
			judge(false,info1.equals(info2));
		}
	}
	/**
	 * 単体テスト用
	 * メンバ変数が比較できればいいので、値は適当
	 * @return
	 */
	public static JobCommandInfo createSampleInfo() {
		JobCommandInfo info = new JobCommandInfo();
		info.setMessageRetryEndFlg(false);
		info.setMessageRetryEndValue(0);
		info.setFacilityID("facilityID");
		info.setMessageRetry(0);
		info.setProcessingMethod(0);
		info.setScope("Scope");
		info.setSpecifyUser(false);
		info.setStartCommand("ls");
		info.setStopCommand("ls -l");
		info.setStopType(0);
		info.setUser("root");
		info.setManagerDistribution(false);
		info.setScriptName("test.sh");
		info.setScriptEncoding("UTF-8");
		info.setScriptContent("echo test");
		info.setEnvVariableInfo(createEnvVariableList("aaa"));
		return info;
	}

	private static List<JobEnvVariableInfo> createEnvVariableList(String s) {
		JobEnvVariableInfo info = new JobEnvVariableInfo();
		info.setEnvVariableId(s);
		info.setValue(s);
		info.setDescription(s);
		List<JobEnvVariableInfo> l = new ArrayList<JobEnvVariableInfo>();
		l.add(info);
		return l;
	}
	
	/**
	 * 単体テストの結果が正しいものか判断する
	 * @param judge
	 * @param result
	 */
	private static void judge(boolean judge, boolean result){

		System.out.println("expect : " + judge);
		System.out.print("result : " + result);
		String ret = "NG";
		if (judge == result) {
			ret = "OK";
		}
		System.out.println("    is ...  " + ret);
	}

	public ArrayList<JobCommandParam> getJobCommandParamList() {
		return jobCommandParamList;
	}

	public void setJobCommandParamList(ArrayList<JobCommandParam> jobCommandParamList) {
		this.jobCommandParamList = jobCommandParamList;
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
			for (JobCommandParam req : jobCommandParamList) {
				req.correlationCheck();
			}
		}
		if (envVariable != null) {
			for (JobEnvVariableInfo req : envVariable) {
				req.correlationCheck();
			}
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