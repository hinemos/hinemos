/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.job.conv;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.JobCommandInfoResponse;
import org.openapitools.client.model.JobCommandParamResponse;
import org.openapitools.client.model.JobEndStatusInfoResponse;
import org.openapitools.client.model.JobEnvVariableInfoResponse;
import org.openapitools.client.model.JobFileCheckInfoResponse;
import org.openapitools.client.model.JobFileCheckInfoResponse.ModifyTypeEnum;
import org.openapitools.client.model.JobFileInfoResponse;
import org.openapitools.client.model.JobInfoResponse;
import org.openapitools.client.model.JobLinkExpInfoResponse;
import org.openapitools.client.model.JobLinkInheritInfoResponse;
import org.openapitools.client.model.JobLinkRcvInfoResponse;
import org.openapitools.client.model.JobLinkSendInfoResponse;
import org.openapitools.client.model.JobMonitorInfoResponse;
import org.openapitools.client.model.JobNextJobOrderInfoResponse;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;
import org.openapitools.client.model.JobOutputInfoResponse;
import org.openapitools.client.model.JobOutputInfoResponse.FailureNotifyPriorityEnum;
import org.openapitools.client.model.JobOutputInfoResponse.FailureOperationEndStatusEnum;
import org.openapitools.client.model.JobOutputInfoResponse.FailureOperationTypeEnum;
import org.openapitools.client.model.JobParameterInfoResponse;
import org.openapitools.client.model.JobResourceInfoResponse;
import org.openapitools.client.model.JobRpaCheckEndValueInfoResponse;
import org.openapitools.client.model.JobRpaEndValueConditionInfoResponse;
import org.openapitools.client.model.JobRpaEndValueConditionInfoResponse.ConditionTypeEnum;
import org.openapitools.client.model.JobRpaInfoResponse;
import org.openapitools.client.model.JobRpaInfoResponse.ProcessingMethodEnum;
import org.openapitools.client.model.JobRpaInfoResponse.RpaJobTypeEnum;
import org.openapitools.client.model.JobRpaInfoResponse.RpaScreenshotEndValueConditionEnum;
import org.openapitools.client.model.JobRpaOptionInfoResponse;
import org.openapitools.client.model.JobRpaRunParamInfoResponse;
import org.openapitools.client.model.JobWaitRuleInfoResponse;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobOutputType;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.job.xml.CommandParam;
import com.clustercontrol.utility.settings.job.xml.EnvVariableInfo;
import com.clustercontrol.utility.settings.job.xml.ErrorJobOutput;
import com.clustercontrol.utility.settings.job.xml.ExclusiveJobValue;
import com.clustercontrol.utility.settings.job.xml.JobLinkExp;
import com.clustercontrol.utility.settings.job.xml.JobLinkInherit;
import com.clustercontrol.utility.settings.job.xml.NormalJobOutput;
import com.clustercontrol.utility.settings.job.xml.NotifyRelationInfo;
import com.clustercontrol.utility.settings.job.xml.ObjectGroup;
import com.clustercontrol.utility.settings.job.xml.ObjectInfo;
import com.clustercontrol.utility.settings.job.xml.Param;
import com.clustercontrol.utility.settings.job.xml.RpaJobCheckEndValueInfos;
import com.clustercontrol.utility.settings.job.xml.RpaJobEndValueConditionInfos;
import com.clustercontrol.utility.settings.job.xml.RpaJobOptionInfos;
import com.clustercontrol.utility.settings.job.xml.RpaJobRunParamInfos;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.DateUtil;
import com.clustercontrol.utility.util.OpenApiEnumConverter;

/**
 * ジョブの定義情報をXMLのBeanとHinemosとDTOとで変換します。<BR>
 *
 * @version 6.1.0
 * @since 1.1.0
 */
public class MasterConv {
	// ロガー
	private static Log log = LogFactory.getLog(MasterConv.class);

	// 対応スキーマバージョン
	private static final String schemaType = "K";
	private static final String schemaVersion = "1";
	private static final String schemaRevision = "1";

	/**
	 * ツリートップのID
	 *
	 * @link com.clustercontrol.jobmanagement.factory.CreateJobSession TOP_JOBUNIT_ID,TOP_JOB_ID
	 */
	private static final String TOP_JOBUNIT_ID = "ROOT";
	private static final String TOP_JOB_ID = "TOP";

	/**
	 * XMLとツールの対応バージョンをチェック します。
	 * @param type
	 * @param version
	 * @param revision
	 * @return
	 */
	static public int checkSchemaVersion(String type, String version, String revision) {
		return BaseConv.checkSchemaVersion(schemaType, schemaVersion, schemaRevision, 
				type, version, revision);
	}

	/**
	 * スキーマのバージョンを返します。
	 *
	 * @return スキーマのバージョンを示すオブジェクト
	 */
	static public com.clustercontrol.utility.settings.job.xml.SchemaInfo getSchemaVersion() {

		com.clustercontrol.utility.settings.job.xml.SchemaInfo schema = new com.clustercontrol.utility.settings.job.xml.SchemaInfo();

		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);

		return schema;
	}

	/**
	 * XMLのBeanからHinemosのBeanに変換しします。
	 * @param scheduleData XMLのBean
	 * @param jobunitList インポート対象となるジョブユニットIDのリスト
	 * @return
	 */
	public static JobTreeItemWrapper masterXml2Dto(com.clustercontrol.utility.settings.job.xml.JobInfo[] jobMastersXML,
		List<String> jobunitList) throws ParseException ,InvalidSetting, HinemosUnknown {

		JobTreeItemWrapper top = new JobTreeItemWrapper();
		JobInfoWrapper topJobInfo =JobTreeItemUtil.createJobInfoWrapper();
		topJobInfo.setJobunitId("");
		topJobInfo.setId("");
		topJobInfo.setName("");
		topJobInfo.setType(null);
		top.setData(topJobInfo);

		for (int i = 0; i < jobMastersXML.length; i++) {

			if (jobMastersXML[i].getParentJobId().equals(TOP_JOB_ID)) {
				for (String jobunitId : jobunitList) {
					if (jobunitId.equals(jobMastersXML[i].getJobunitId())) {

						// ツリーアイテムの生成と中身のジョブ情報のセット
						JobTreeItemWrapper jti = new JobTreeItemWrapper();
						jti.setData(setDTOJobData(jobMastersXML[i]));

						// 自分は"TOP"にぶら下がります。
						top.getChildren().add(jti);

						// 自分の子供を捜しに行きます。
						masterXml2Dto(jti, jobMastersXML);
					}
				}
			}
		}
		return top;
	}

	private static void masterXml2Dto(JobTreeItemWrapper parent,
			com.clustercontrol.utility.settings.job.xml.JobInfo[] jobMastersXML) throws ParseException,InvalidSetting, HinemosUnknown {

		for (int i = 0; i < jobMastersXML.length; i++) {

			// 子供のjobを探します。
			if (jobMastersXML[i].getParentJobId().equals(parent.getData().getId())
					&& jobMastersXML[i].getParentJobunitId().equals(parent.getData().getJobunitId())) {

				// ツリーアイテムの生成と中身のジョブ情報のセット
				JobTreeItemWrapper jti = new JobTreeItemWrapper();
				jti.setData(setDTOJobData(jobMastersXML[i]));

				// 親にぶら下がります。
				parent.getChildren().add(jti);

				// さらにの子供を捜しに行きます。
				masterXml2Dto(jti, jobMastersXML);

			}

		}
	}

	/**
	 * データの生成
	 * @param jobMasterXML
	 * @return
	 */
	private static JobInfoWrapper setDTOJobData(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML)
	throws ParseException, InvalidSetting, HinemosUnknown {
		//投入用データの作成
		JobInfoWrapper info= null;

		//ID
		if (jobMasterXML.getId() != null && !"".equals(jobMasterXML.getId().trim())) {

			//名前
			if (jobMasterXML.getName() != null && !"".equals(jobMasterXML.getName().trim())) {
				//タイプ
				if (jobMasterXML.getType() == JobConstant.TYPE_JOBUNIT
						|| jobMasterXML.getType() == JobConstant.TYPE_JOBNET
						|| jobMasterXML.getType() == JobConstant.TYPE_JOB
						|| jobMasterXML.getType() == JobConstant.TYPE_FILEJOB
						|| jobMasterXML.getType() == JobConstant.TYPE_REFERJOB
						|| jobMasterXML.getType() == JobConstant.TYPE_REFERJOBNET
						|| jobMasterXML.getType() == JobConstant.TYPE_APPROVALJOB
						|| jobMasterXML.getType() == JobConstant.TYPE_MONITORJOB
						|| jobMasterXML.getType() == JobConstant.TYPE_RESOURCEJOB
						|| jobMasterXML.getType() == JobConstant.TYPE_RPAJOB
						|| jobMasterXML.getType() == JobConstant.TYPE_FILECHECKJOB
						|| jobMasterXML.getType() == JobConstant.TYPE_JOBLINKSENDJOB
						|| jobMasterXML.getType() == JobConstant.TYPE_JOBLINKRCVJOB
						) {

					//コンストラクターには、IDとNameとTypeが必要
					info = JobTreeItemUtil.createJobInfoWrapper();
					info.setJobunitId(jobMasterXML.getJobunitId());
					info.setId(jobMasterXML.getId());
					info.setName(jobMasterXML.getName());
					info.setType(OpenApiEnumConverter.integerToEnum(jobMasterXML.getType(), JobInfoResponse.TypeEnum.class));
					info.setPropertyFull(true);

				} else {
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid") + "(JobType) : " + jobMasterXML.getId());
					return null;
				}

			} else {
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") + "(JobName) : " + jobMasterXML.getId());
				return null;
			}

		} else {
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid") + "(JobId) : " + jobMasterXML.toString());
			return null;
		}

		// 説明
		info.setDescription(jobMasterXML.getDescription());

		// オーナーロールID
		info.setOwnerRoleId(jobMasterXML.getOwnerRoleId());

		// コマンド
		if (info.getType() == JobInfoWrapper.TypeEnum.JOB) {
			info.setCommand(getDTOCommand(jobMasterXML));

		}

		// 終了状態
		info.getEndStatus().addAll(getDTOEndStatus(jobMasterXML));

		// ファイル転送ジョブ
		if (info.getType() == JobInfoWrapper.TypeEnum.FILEJOB) {
			info.setFile(getDTOFile(jobMasterXML));
		}

		// モジュール登録
		info.setRegistered(jobMasterXML.getRegisteredModule());
		// アイコンID
		info.setIconId(jobMasterXML.getIconId());

		// ジョブ開始時に実行対象ノードを決定する
		if (jobMasterXML.getType() == JobConstant.TYPE_JOBUNIT){
			if(jobMasterXML.hasExpNodeRuntimeFlg()){
				info.setExpNodeRuntimeFlg(jobMasterXML.getExpNodeRuntimeFlg());
			}
		}

		// 承認ジョブ
		if (info.getType() == JobInfoWrapper.TypeEnum.APPROVALJOB) {
			// 承認依頼先ロール
			info.setApprovalReqRoleId(jobMasterXML.getApprovalReqRoleId());
			// 承認依頼先ユーザー
			info.setApprovalReqUserId(jobMasterXML.getApprovalReqUserId());
			// 承認依頼文
			info.setApprovalReqSentence(jobMasterXML.getApprovalReqSentence());
			// 承認依頼メール件名
			info.setApprovalReqMailTitle(jobMasterXML.getApprovalReqMailTitle());
			// 承認依頼メール本文
			info.setApprovalReqMailBody(jobMasterXML.getApprovalReqMailBody());
			// 承認依頼文を利用するか？
			info.setIsUseApprovalReqSentence(jobMasterXML.getUseApprovalReqSentence());
		}

		// 監視の場合
		if (info.getType().equals(JobInfoWrapper.TypeEnum.MONITORJOB)) {
			info.setMonitor(new JobMonitorInfoResponse());
			//ファシリティID
			info.getMonitor().setFacilityID(jobMasterXML.getFacilityId());
			//スコープ処理
			info.getMonitor().setProcessingMethod(OpenApiEnumConverter.integerToEnum(jobMasterXML.getProcessMode(), JobMonitorInfoResponse.ProcessingMethodEnum.class));
			// 監視設定ID
			info.getMonitor().setMonitorId(jobMasterXML.getMonitorId());
			// 終了値(情報)
			info.getMonitor().setMonitorInfoEndValue(jobMasterXML.getMonitorInfoEndValue());
			// 終了値(警告)
			info.getMonitor().setMonitorWarnEndValue(jobMasterXML.getMonitorWarnEndValue());
			// 終了値(危険)
			info.getMonitor().setMonitorCriticalEndValue(jobMasterXML.getMonitorCriticalEndValue());
			// 終了値(不明)
			info.getMonitor().setMonitorUnknownEndValue(jobMasterXML.getMonitorUnknownEndValue());
			// 終了値(タイムアウト)
			info.getMonitor().setMonitorWaitTime(jobMasterXML.getMonitorWaitTime());
			// 終了値
			info.getMonitor().setMonitorWaitEndValue(jobMasterXML.getMonitorWaitEndValue());
		}

		// リソース制御ジョブの場合
		if (info.getType().equals(JobInfoWrapper.TypeEnum.RESOURCEJOB)) {
			info.setResource(getDTOResource(jobMasterXML));
		}

		// 待ち条件
		if (jobMasterXML.getObjectGroup() != null) {
			info.setWaitRule(getDTOWaitRule(jobMasterXML));
		}

		// ジョブ変数一覧
		if (jobMasterXML.getParam() != null) {
			info.getParam().addAll(getDTOParam(jobMasterXML));
		}

		if (jobMasterXML.getType() != JobConstant.TYPE_REFERJOB
				&& jobMasterXML.getType() != JobConstant.TYPE_REFERJOBNET){
			info.setBeginPriority(OpenApiEnumConverter.integerToEnum(jobMasterXML.getBeginPriority(), JobInfoResponse.BeginPriorityEnum.class));
			info.setAbnormalPriority(OpenApiEnumConverter.integerToEnum(jobMasterXML.getAbnormalPriority(), JobInfoResponse.AbnormalPriorityEnum.class));
			info.setWarnPriority(OpenApiEnumConverter.integerToEnum(jobMasterXML.getWarnPriority(), JobInfoResponse.WarnPriorityEnum.class));
			info.setNormalPriority(OpenApiEnumConverter.integerToEnum(jobMasterXML.getNormalPriority(), JobInfoResponse.NormalPriorityEnum.class));
		}

		// 通知ID一覧
		getDTONotices(info, jobMasterXML);

		if (jobMasterXML.getReferJobId() != null) {
			info.setReferJobId(jobMasterXML.getReferJobId());
		}

		if (jobMasterXML.getReferJobunitId() != null) {
			info.setReferJobUnitId(jobMasterXML.getReferJobunitId());
		}

		// 参照ジョブ種別
		if (jobMasterXML.getType() == JobConstant.TYPE_REFERJOB
				|| jobMasterXML.getType() == JobConstant.TYPE_REFERJOBNET){
			info.setReferJobSelectType(OpenApiEnumConverter.integerToEnum(jobMasterXML.getReferJobSelectType(), JobInfoResponse.ReferJobSelectTypeEnum.class) );
		}

		info.setUpdateTime(DateUtil.convDateFormatIso86012Hinemos(jobMasterXML.getUpdateTime()));

		// ジョブ連携送信ジョブ
		if(jobMasterXML.getType() == JobConstant.TYPE_JOBLINKSENDJOB){
			info.setJobLinkSend(getDTOLinkSend(jobMasterXML));
		}

		// ジョブ連携待機ジョブ
		if(jobMasterXML.getType() == JobConstant.TYPE_JOBLINKRCVJOB){
			info.setJobLinkRcv(getDTOLinkRcv(jobMasterXML));
		}
		
		// ファイルチェックジョブ
		if(jobMasterXML.getType() == JobConstant.TYPE_FILECHECKJOB){
			info.setJobFileCheck(getDTOFileCheck(jobMasterXML));
		}

		// RPAシナリオジョブ
		if(jobMasterXML.getType() == JobConstant.TYPE_RPAJOB){
			info.setRpa(getDTORpa(jobMasterXML));
		}
		
		return info;
	}

	/**
	 * コマンドジョブに関するDTOを返します.
	 * @param jobMasterXML
	 * @return
	 */
	private static JobCommandInfoResponse getDTOCommand(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws InvalidSetting, HinemosUnknown{
		JobCommandInfoResponse ret = new JobCommandInfoResponse();

		ret.setFacilityID(jobMasterXML.getFacilityId());
		ret.setProcessingMethod(OpenApiEnumConverter.integerToEnum(jobMasterXML.getProcessMode(), JobCommandInfoResponse.ProcessingMethodEnum.class));
		ret.setStartCommand(jobMasterXML.getStartCommand());
		ret.setStopType(OpenApiEnumConverter.integerToEnum(jobMasterXML.getStopType(), JobCommandInfoResponse.StopTypeEnum.class));
		if (jobMasterXML.getStopType() == 0) {
			ret.setStopCommand(jobMasterXML.getStopCommand());
		}
		ret.setSpecifyUser(jobMasterXML.getSpecifyUser());
		if (jobMasterXML.getSpecifyUser()) {
			if (jobMasterXML.getEffectiveUser() != null && !jobMasterXML.getEffectiveUser().equals("")) {
				ret.setUser(jobMasterXML.getEffectiveUser());
			} else {
				log.info("User not found " + jobMasterXML.getId());
			}
		}
		ret.setMessageRetryEndFlg(jobMasterXML.getMessageRetryEndFlg());
		// エージェントに接続できない時の試行回数
		ret.setMessageRetry(jobMasterXML.getMessageRetry());
		ret.setMessageRetryEndValue(jobMasterXML.getMessageRetryEndValue());

		ret.setCommandRetryFlg(jobMasterXML.getCommandRetryFlg());
		ret.setCommandRetryEndStatus(jobMasterXML.hasCommandRetryEndStatus() ? OpenApiEnumConverter.integerToEnum(jobMasterXML.getCommandRetryEndStatus(), JobCommandInfoResponse.CommandRetryEndStatusEnum.class) : null);

		// ジョブが指定された終了状態になるまでコマンドを繰り返す時の試行回数
		if (jobMasterXML.hasCommandRetry()) {
			ret.setCommandRetry(jobMasterXML.getCommandRetry());
		} else {
			// 未設定時はデフォルトの10を設定する。
			ret.setCommandRetry(10);
		}

		// スクリプトをマネージャから配布
		ret.setManagerDistribution(jobMasterXML.getManagerDistribution());
		// スクリプト名
		ret.setScriptName(jobMasterXML.getScriptName());
		// スクリプトエンコーディング
		ret.setScriptEncoding(jobMasterXML.getScriptEncoding());
		// スクリプト内容
		ret.setScriptContent(jobMasterXML.getScriptContent());
		// コマンドジョブパラメータ
		for(CommandParam cp : jobMasterXML.getCommandParam()){
			JobCommandParamResponse e = new JobCommandParamResponse();
			e.setJobStandardOutputFlg(cp.getJobStandardOutputFlg());
			e.setParamId(cp.getParamId());
			e.setValue(cp.getValue());
			ret.getJobCommandParamList().add(e);
		}
		// コマンドジョブ環境変数
		for (EnvVariableInfo env : jobMasterXML.getEnvVariableInfo()) {
			JobEnvVariableInfoResponse e = new JobEnvVariableInfoResponse();
			e.setEnvVariableId(env.getEnvVariableId());
			e.setValue(env.getValue());
			e.setDescription(env.getDescription());
			ret.getEnvVariable().add(e);
		}
		
		// ファイル出力（標準出力）
		if(jobMasterXML.getNormalJobOutput() != null){
			JobOutputInfoResponse jobOutputInfo = new JobOutputInfoResponse();
			jobOutputInfo.setDirectory(jobMasterXML.getNormalJobOutput().getDirectory());
			jobOutputInfo.setFileName(jobMasterXML.getNormalJobOutput().getFileName());
			if(jobMasterXML.getNormalJobOutput().hasAppendFlg()){
				jobOutputInfo.setAppendFlg(jobMasterXML.getNormalJobOutput().getAppendFlg());
			}
			if(jobMasterXML.getNormalJobOutput().hasFailureOperationFlg()){
				jobOutputInfo.setFailureOperationFlg(jobMasterXML.getNormalJobOutput().getFailureOperationFlg());
			}
			if(jobMasterXML.getNormalJobOutput().hasFailureOperationType()){
				jobOutputInfo.setFailureOperationType(OpenApiEnumConverter.integerToEnum(
						jobMasterXML.getNormalJobOutput().getFailureOperationType(), FailureOperationTypeEnum.class));
			}
			if(jobMasterXML.getNormalJobOutput().hasFailureOperationEndStatus()){
				jobOutputInfo.setFailureOperationEndStatus(OpenApiEnumConverter.integerToEnum(
						jobMasterXML.getNormalJobOutput().getFailureOperationEndStatus(), FailureOperationEndStatusEnum.class));
			}
			if(jobMasterXML.getNormalJobOutput().hasFailureOperationEndValue()){
				jobOutputInfo.setFailureOperationEndValue(jobMasterXML.getNormalJobOutput().getFailureOperationEndValue());
			}
			if(jobMasterXML.getNormalJobOutput().hasFailureNotifyFlg()){
				jobOutputInfo.setFailureNotifyFlg(jobMasterXML.getNormalJobOutput().getFailureNotifyFlg());
			}
			if(jobMasterXML.getNormalJobOutput().hasFailureNotifyPriority()){
				jobOutputInfo.setFailureNotifyPriority(OpenApiEnumConverter.integerToEnum(
						jobMasterXML.getNormalJobOutput().getFailureNotifyPriority(), FailureNotifyPriorityEnum.class));
			}
			if(jobMasterXML.getNormalJobOutput().hasValidFlg()){
				jobOutputInfo.setValid(jobMasterXML.getNormalJobOutput().getValidFlg());
			}
			ret.setNormalJobOutputInfo(jobOutputInfo);
		}
		
		// ファイル出力（標準エラー出力）
		if(jobMasterXML.getErrorJobOutput() != null){
			JobOutputInfoResponse jobOutputInfo = new JobOutputInfoResponse();
			if(jobMasterXML.getErrorJobOutput().hasSameNormalFlg()){
				jobOutputInfo.setSameNormalFlg(jobMasterXML.getErrorJobOutput().getSameNormalFlg());
			}
			jobOutputInfo.setDirectory(jobMasterXML.getErrorJobOutput().getDirectory());
			jobOutputInfo.setFileName(jobMasterXML.getErrorJobOutput().getFileName());
			if(jobMasterXML.getErrorJobOutput().hasAppendFlg()){
				jobOutputInfo.setAppendFlg(jobMasterXML.getErrorJobOutput().getAppendFlg());
			}
			if(jobMasterXML.getErrorJobOutput().hasFailureOperationFlg()){
				jobOutputInfo.setFailureOperationFlg(jobMasterXML.getErrorJobOutput().getFailureOperationFlg());
			}
			if(jobMasterXML.getErrorJobOutput().hasFailureOperationType()){
				jobOutputInfo.setFailureOperationType(OpenApiEnumConverter.integerToEnum(
						jobMasterXML.getErrorJobOutput().getFailureOperationType(), FailureOperationTypeEnum.class));
			}
			if(jobMasterXML.getErrorJobOutput().hasFailureOperationEndStatus()){
				jobOutputInfo.setFailureOperationEndStatus(OpenApiEnumConverter.integerToEnum(
						jobMasterXML.getErrorJobOutput().getFailureOperationEndStatus(), FailureOperationEndStatusEnum.class));
			}
			if(jobMasterXML.getErrorJobOutput().hasFailureOperationEndValue()){
				jobOutputInfo.setFailureOperationEndValue(jobMasterXML.getErrorJobOutput().getFailureOperationEndValue());
			}
			if(jobMasterXML.getErrorJobOutput().hasFailureNotifyFlg()){
				jobOutputInfo.setFailureNotifyFlg(jobMasterXML.getErrorJobOutput().getFailureNotifyFlg());
			}
			if(jobMasterXML.getErrorJobOutput().hasFailureNotifyPriority()){
				jobOutputInfo.setFailureNotifyPriority(OpenApiEnumConverter.integerToEnum(
						jobMasterXML.getErrorJobOutput().getFailureNotifyPriority(), FailureNotifyPriorityEnum.class));
			}
			if(jobMasterXML.getErrorJobOutput().hasValidFlg()){
				jobOutputInfo.setValid(jobMasterXML.getErrorJobOutput().getValidFlg());
			}
			ret.errorJobOutputInfo(jobOutputInfo);
		}

		return ret;
	}

	/**
	 * jobMasterXMLから終了状態を取得して返します.
	 * @param jobMasterXML
	 * @return 終了状態のリスト
	 */
	private static List<JobEndStatusInfoResponse> getDTOEndStatus(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML){
		List<JobEndStatusInfoResponse> ret = new ArrayList<JobEndStatusInfoResponse>();

		// 終了状態(正常)
		JobEndStatusInfoResponse endInfo = new JobEndStatusInfoResponse();
		endInfo.setType(JobEndStatusInfoResponse.TypeEnum.NORMAL);
		endInfo.setValue(jobMasterXML.getNormalEndValue());
		endInfo.setStartRangeValue(jobMasterXML.getNormalEndValueFrom());
		endInfo.setEndRangeValue(jobMasterXML.getNormalEndValueTo());
		ret.add(endInfo);

		// 終了状態(警告)
		endInfo = new JobEndStatusInfoResponse();
		endInfo.setType(JobEndStatusInfoResponse.TypeEnum.WARNING);
		endInfo.setValue(jobMasterXML.getWarnEndValue());
		endInfo.setStartRangeValue(jobMasterXML.getWarnEndValueFrom());
		endInfo.setEndRangeValue(jobMasterXML.getWarnEndValueTo());
		ret.add(endInfo);

		// 終了状態(異常)
		endInfo = new JobEndStatusInfoResponse();
		endInfo.setType(JobEndStatusInfoResponse.TypeEnum.ABNORMAL);
		endInfo.setValue(jobMasterXML.getAbnormalEndValue());
		endInfo.setStartRangeValue(jobMasterXML.getAbnormalEndValueFrom());
		endInfo.setEndRangeValue(jobMasterXML.getAbnormalEndValueTo());
		ret.add(endInfo);

		return ret;
	}

	/**
	 * jobMasterXMLからファイル転送ジョブ詳細情報を返します.
	 * @param jobMasterXML
	 * @return
	 */
	private static JobFileInfoResponse getDTOFile(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws InvalidSetting, HinemosUnknown{
		JobFileInfoResponse ret = new JobFileInfoResponse();

		// 転送元ファシリティID
		ret.setSrcFacilityID(jobMasterXML.getSrcFacilityId());
		// 転送ファイル
		ret.setSrcFile(jobMasterXML.getSrcFile());
		// 送信先ファシリティID
		ret.setDestFacilityID(jobMasterXML.getDestFacilityId());
		// 送信先ディレクトリ
		ret.setDestDirectory(jobMasterXML.getDestDirectory());
		// 処理方式
		ret.setProcessingMethod(OpenApiEnumConverter.integerToEnum(jobMasterXML.getProcessMode(), JobFileInfoResponse.ProcessingMethodEnum.class));
		// ファイル転送時に圧縮するかのフラグ
		ret.setCompressionFlg(jobMasterXML.getCompressionFlg());
		// 転送ファイルのチェックを行うかのフラグ
		ret.setCheckFlg(jobMasterXML.getCheckFlg());
		// 実行ユーザーを指定するかのフラグ
		ret.setSpecifyUser(jobMasterXML.getSpecifyUser());
		// 実行ユーザー名
		if (jobMasterXML.getSpecifyUser()) {
			ret.setUser(jobMasterXML.getEffectiveUser());
		}
		// エージェントに接続できない時に終了するかのフラグ
		ret.setMessageRetryEndFlg(jobMasterXML.getMessageRetryEndFlg());
		// エージェントに接続できない時の試行回数
		ret.setMessageRetry(jobMasterXML.getMessageRetry());
		// エージェントに接続できない時に終了する時の終了値
		// Import with constant otherwise it will be set to "0"
		ret.setMessageRetryEndValue(jobMasterXML.hasMessageRetryEndValue() ? jobMasterXML.getMessageRetryEndValue() : 0);

		return ret;
	}

	/**
	 * jobMasterXMLから終了遅延情報を返します.
	 * @param jobMasterXML
	 * @return 終了遅延情報を格納するクラスの参照
	 */
	private static JobWaitRuleInfoResponse getDTOWaitRule(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws InvalidSetting, HinemosUnknown {
		JobWaitRuleInfoResponse ret = JobTreeItemUtil.getNewJobWaitRuleInfo();
		// カレンダーフラグ
		ret.setCalendar(jobMasterXML.getCalendar());
		// カレンダーの終了状態
		ret.setCalendarEndStatus(OpenApiEnumConverter.integerToEnum(jobMasterXML.getCalendarEndStatus() , JobWaitRuleInfoResponse.CalendarEndStatusEnum.class));
		// カレンダーの終了値
		ret.setCalendarEndValue(jobMasterXML.getCalendarEndValue());
		// カレンダーID
		ret.setCalendarId(jobMasterXML.getCalendarId());
		// 判定対象の条件関係
		ret.setCondition(OpenApiEnumConverter.integerToEnum(jobMasterXML.getConditionType() , JobWaitRuleInfoResponse.ConditionEnum.class));
		// 終了遅延有無
		ret.setEndDelay(jobMasterXML.getEndDelay());
		// 終了遅延判定対象の条件関係
		ret.setEndDelayConditionType(OpenApiEnumConverter.integerToEnum(jobMasterXML.getEndDelayConditionType()  , JobWaitRuleInfoResponse.EndDelayConditionTypeEnum.class) );
		// ジョブ開始後の経過時間監視有無
		ret.setEndDelayJob(jobMasterXML.getEndDelayJob());
		// ジョブ開始後の経過時間(分)
		ret.setEndDelayJobValue(jobMasterXML.getEndDelayJobValue());
		// 終了遅延通知有無
		ret.setEndDelayNotify(jobMasterXML.getEndDelayNotify());
		// 終了遅延通知重要度
		ret.setEndDelayNotifyPriority(OpenApiEnumConverter.integerToEnum(jobMasterXML.getEndDelayNotifyPriority() , JobWaitRuleInfoResponse.EndDelayNotifyPriorityEnum.class)  );
		// 終了遅延操作有無
		ret.setEndDelayOperation(jobMasterXML.getEndDelayOperation());
		// 終了遅延操作終了状態(0:正常,1:警告,2:異常)
		ret.setEndDelayOperationEndStatus(OpenApiEnumConverter.integerToEnum(jobMasterXML.getEndDelayOperationEndStatus() , JobWaitRuleInfoResponse.EndDelayOperationEndStatusEnum.class) );
		// 終了遅延操作終了値
		ret.setEndDelayOperationEndValue(jobMasterXML.getEndDelayOperationEndValue());
		// 終了遅延操作種別（0:停止[コマンド], 2:停止[中断], 10:停止[状態指定]）
		ret.setEndDelayOperationType(OpenApiEnumConverter.integerToEnum(jobMasterXML.getEndDelayOperationType()  , JobWaitRuleInfoResponse.EndDelayOperationTypeEnum.class) );
		// セッション開始後の経過時間使用有無
		ret.setEndDelaySession(jobMasterXML.getEndDelaySession());
		// セッション開始後の経過時間（分）
		ret.setEndDelaySessionValue(jobMasterXML.getEndDelaySessionValue());
		// 終了遅延監視時刻監視有無
		ret.setEndDelayTime(jobMasterXML.getEndDelayTime());
		// 終了監視遅延時刻の値
		if (jobMasterXML.getEndDelayTimeValue() != null && !jobMasterXML.getEndDelayTimeValue().isEmpty()) {
			ret.setEndDelayTimeValue(jobMasterXML.getEndDelayTimeValue());
		}
		// 実行履歴からの変化量使用有無
		ret.setEndDelayChangeMount(jobMasterXML.getEndDelayChangeMount());
		// 実行履歴からの変化量
		ret.setEndDelayChangeMountValue(jobMasterXML.getEndDelayChangeMountValue());

		// 終了状態
		ret.setEndStatus(OpenApiEnumConverter.integerToEnum(jobMasterXML.getUnmatchEndStatus() , JobWaitRuleInfoResponse.EndStatusEnum.class) );
		// 終了値
		ret.setEndValue(jobMasterXML.getUnmatchEndValue());
		// 条件を満たさなければ終了する
		ret.setEndCondition(jobMasterXML.getUnmatchEndFlg());
		// スキップ
		ret.setSkip(jobMasterXML.getSkip());
		// スキップ終了状態
		ret.setSkipEndStatus(OpenApiEnumConverter.integerToEnum(jobMasterXML.getSkipEndStatus() , JobWaitRuleInfoResponse.SkipEndStatusEnum.class) );
		// スキップ終了値
		ret.setSkipEndValue(jobMasterXML.getSkipEndValue());
		// 開始遅延有無
		ret.setStartDelay(jobMasterXML.getStartDelay());
		// 開始遅延判定対象の条件関係
		ret.setStartDelayConditionType(OpenApiEnumConverter.integerToEnum(jobMasterXML.getStartDelayConditionType() , JobWaitRuleInfoResponse.StartDelayConditionTypeEnum.class) );
		// 開始遅延通知有無
		ret.setStartDelayNotify(jobMasterXML.getStartDelayNotify());
		// 開始遅延通知重要度
		ret.setStartDelayNotifyPriority( OpenApiEnumConverter.integerToEnum(jobMasterXML.getStartDelayNotifyPriority(), JobWaitRuleInfoResponse.StartDelayNotifyPriorityEnum.class) );
		// 開始遅延操作有無
		ret.setStartDelayOperation(jobMasterXML.getStartDelayOperation());
		// 開始遅延操作終了状態
		ret.setStartDelayOperationEndStatus(OpenApiEnumConverter.integerToEnum(jobMasterXML.getStartDelayOperationEndStatus(), JobWaitRuleInfoResponse.StartDelayOperationEndStatusEnum.class) );
		// 開始遅延操作終了値
		ret.setStartDelayOperationEndValue(jobMasterXML.getStartDelayOperationEndValue());
		// 開始遅延操作種別
		ret.setStartDelayOperationType( OpenApiEnumConverter.integerToEnum(jobMasterXML.getStartDelayOperationType(), JobWaitRuleInfoResponse.StartDelayOperationTypeEnum.class) );
		// セッション開始後からの開始遅延有無
		ret.setStartDelaySession(jobMasterXML.getStartDelaySession());
		// 開始遅延セッション開始後の時間の値
		ret.setStartDelaySessionValue(jobMasterXML.getStartDelaySessionValue());
		// 開始遅延時刻有無
		ret.setStartDelayTime(jobMasterXML.getStartDelayTime());
		// セッション開始後の経過時間（分）
		if (jobMasterXML.getStartDelayTimeValue() != null && !jobMasterXML.getStartDelayTimeValue().isEmpty()) {
			ret.setStartDelayTimeValue(jobMasterXML.getStartDelayTimeValue());
		}
		// 開始時保留
		ret.setSuspend(jobMasterXML.getSuspend());

		if ((jobMasterXML.getType() == JobConstant.TYPE_FILEJOB)
			|| (jobMasterXML.getType() == JobConstant.TYPE_JOB)
			|| (jobMasterXML.getType() == JobConstant.TYPE_FILECHECKJOB)
			|| (jobMasterXML.getType() == JobConstant.TYPE_RPAJOB)) {
			// 多重度が上限に達した時に通知する
			ret.setMultiplicityNotify(jobMasterXML.getMultiplicityNotify());
			// 通知重要度
			ret.setMultiplicityNotifyPriority( OpenApiEnumConverter.integerToEnum(jobMasterXML.getMultiplicityNotifyPriority(), JobWaitRuleInfoResponse.MultiplicityNotifyPriorityEnum.class) );
			// 操作
			ret.setMultiplicityOperation(OpenApiEnumConverter.integerToEnum(jobMasterXML.getMultiplicityOperation() , JobWaitRuleInfoResponse.MultiplicityOperationEnum.class) );
			// 操作終了値
			ret.setMultiplicityEndValue(jobMasterXML.getMultiplicityEndValue());
		}

		// 判定対象一覧
		ret.setObjectGroup(new ArrayList<JobObjectGroupInfoResponse>() );
		for(ObjectGroup objectGroup : jobMasterXML.getObjectGroup()){
			JobObjectGroupInfoResponse objectGroupRet = new JobObjectGroupInfoResponse();
			objectGroupRet.setConditionType(OpenApiEnumConverter.integerToEnum(objectGroup.getConditionType(), 
					JobObjectGroupInfoResponse.ConditionTypeEnum.class));
			objectGroupRet.setJobObjectList(getDTOJobObjectInfo(objectGroup.getObjectInfo()));
			ret.getObjectGroup().add(objectGroupRet);
		}

		//排他分岐
		ret.setExclusiveBranch(jobMasterXML.getExclusiveBranch());
		//実行されなかったジョブの終了状態
		ret.setExclusiveBranchEndStatus(OpenApiEnumConverter.integerToEnum(jobMasterXML.getExclusiveBranchEndStatus() , JobWaitRuleInfoResponse.ExclusiveBranchEndStatusEnum.class) );
		//実行されなかったジョブの終了値
		ret.setExclusiveBranchEndValue(jobMasterXML.getExclusiveBranchEndValue());

		ExclusiveJobValue[] exclusiveJobValues = jobMasterXML.getExclusiveJobValue();
		sort(exclusiveJobValues);
		for (ExclusiveJobValue oder : exclusiveJobValues) {
			JobNextJobOrderInfoResponse nextJob = new JobNextJobOrderInfoResponse();
			nextJob.setJobunitId(jobMasterXML.getJobunitId());
			nextJob.setJobId(jobMasterXML.getId());
			nextJob.setNextJobId(oder.getNextJobId());
			ret.getExclusiveBranchNextJobOrderList().add(nextJob);
		}

		// 繰り返し実行
		// ジョブユニットは待ち条件の変更をしない(JobTreeItemUtil.getNewJobWaitRuleInfo();でデフォルト値設定のままとする)
		if (jobMasterXML.getType() != JobConstant.TYPE_JOBUNIT) {
			ret.setJobRetryFlg(jobMasterXML.getJobRetryFlg());
			ret.setJobRetryEndStatus(jobMasterXML.hasJobRetryEndStatus() ? OpenApiEnumConverter.integerToEnum(
					jobMasterXML.getJobRetryEndStatus(), JobWaitRuleInfoResponse.JobRetryEndStatusEnum.class) : null);
			if (jobMasterXML.hasJobRetry()) {
				ret.setJobRetry(jobMasterXML.getJobRetry());
			} else {
				// 未設定時はデフォルトの10を設定する。
				ret.setJobRetry(10);
			}
			ret.setJobRetryInterval(jobMasterXML.hasJobRetryInterval() ? jobMasterXML.getJobRetryInterval() : 0);
		}

		//同時実行制御キュー
		ret.setQueueFlg(jobMasterXML.getQueueFlg());
		ret.setQueueId(jobMasterXML.getQueueId());
		return ret;
	}

	/**
	 * 判定対象一覧のオブジェクトを返します.
	 * @param masterXML
	 * @return
	 * @throws ParseException
	 */
	private static List<JobObjectInfoResponse> getDTOJobObjectInfo(ObjectInfo[] objectInfoXML) throws InvalidSetting, HinemosUnknown {
		List<JobObjectInfoResponse> ret = new ArrayList<JobObjectInfoResponse>();
		
		for(ObjectInfo one : objectInfoXML){
			JobObjectInfoResponse infoBean = new JobObjectInfoResponse();
			
			// Common part
			infoBean.setType(OpenApiEnumConverter.integerToEnum(one.getType(), JobObjectInfoResponse.TypeEnum.class));
			infoBean.setDescription(one.getDescription());

			switch (one.getType()) {
			case JudgmentObjectConstant.TYPE_JOB_END_STATUS:
				infoBean.setJobId(one.getJobId());
				infoBean.setStatus(OpenApiEnumConverter.integerToEnum(one.getStatus(), 
						JobObjectInfoResponse.StatusEnum.class));
				break;
			case JudgmentObjectConstant.TYPE_JOB_END_VALUE:
				infoBean.setJobId(one.getJobId());
				infoBean.setValue(one.getValue());
				infoBean.setDecisionCondition(OpenApiEnumConverter.integerToEnum(one.getNumberDecisionCondition(), 
						JobObjectInfoResponse.DecisionConditionEnum.class));
				break;
			case JudgmentObjectConstant.TYPE_TIME:
				infoBean.setTime(one.getTime());
				break;
			case JudgmentObjectConstant.TYPE_START_MINUTE:
				infoBean.setStartMinute(one.getStartMinute());
				break;

			case JudgmentObjectConstant.TYPE_JOB_PARAMETER:
				infoBean.setDecisionValue(one.getDecisionValue());
				infoBean.setValue(one.getValue());
				infoBean.setDecisionCondition(OpenApiEnumConverter.integerToEnum(one.getDecisionCondition(), 
						JobObjectInfoResponse.DecisionConditionEnum.class));
				break;
			case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS:
				infoBean.setCrossSessionRange(one.getCrossSessionRange());
				infoBean.setStatus(OpenApiEnumConverter.integerToEnum(one.getStatus(), 
						JobObjectInfoResponse.StatusEnum.class));
				infoBean.setJobId(one.getJobId());
				break;
			case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE:
				infoBean.setCrossSessionRange(one.getCrossSessionRange());
				infoBean.setValue(one.getValue());
				infoBean.setJobId(one.getJobId());
				infoBean.setDecisionCondition(OpenApiEnumConverter.integerToEnum(one.getNumberDecisionCondition(), 
						JobObjectInfoResponse.DecisionConditionEnum.class));
				break;
			case JudgmentObjectConstant.TYPE_JOB_RETURN_VALUE:
				infoBean.setValue(one.getValue());
				infoBean.setJobId(one.getJobId());
				infoBean.setDecisionCondition(OpenApiEnumConverter.integerToEnum(one.getNumberDecisionCondition(), 
						JobObjectInfoResponse.DecisionConditionEnum.class));
				break;
			default:
				String msg = "Unknown start job type : " + one.getType();
				log.error(msg);
				throw new IllegalArgumentException(msg);
			}
			
			ret.add(infoBean);
		}

		return ret;
	}

	/**
	 * ジョブ変数一覧を返します.
	 * @param jobMasterXML
	 * @return
	 */
	private static List<JobParameterInfoResponse> getDTOParam(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws InvalidSetting, HinemosUnknown {
		List<JobParameterInfoResponse> ret = new ArrayList<JobParameterInfoResponse>();

		for (Param param : jobMasterXML.getParam()) {
			JobParameterInfoResponse paramInfo = new JobParameterInfoResponse();
			paramInfo.setDescription(param.getDescription());
			paramInfo.setParamId(param.getParamId());
			paramInfo.setType( OpenApiEnumConverter.integerToEnum(param.getParamType() , JobParameterInfoResponse.TypeEnum.class)  );
			paramInfo.setValue(param.getValue());
			ret.add(paramInfo);
		}

		return ret;

	}

	/**
	 * 通知ID一覧を返します.
	 * @param info
	 * @param jobMasterXML
	 */
	private static void getDTONotices(JobInfoWrapper info, com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws InvalidSetting, HinemosUnknown {
		List<NotifyRelationInfoResponse> relations = info.getNotifyRelationInfos();
		NotifyRelationInfoResponse relation ;

		for(NotifyRelationInfo nInfo: jobMasterXML.getNotifyRelationInfos()){
			relation = new NotifyRelationInfoResponse();
			relation.setNotifyId(nInfo.getNotifyId());
			relation.setNotifyType(OpenApiEnumConverter.integerToEnum((int)nInfo.getNotifyType() , NotifyRelationInfoResponse.NotifyTypeEnum.class) );
			relations.add(relation);
		}
	}
	
	/**
	 * RPAシナリオジョブに関するDTOを返します.
	 * @param jobMasterXML
	 * @return
	 */
	private static JobRpaInfoResponse getDTORpa(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws InvalidSetting, HinemosUnknown{
		JobRpaInfoResponse ret = new JobRpaInfoResponse();

		ret.setRpaJobType(OpenApiEnumConverter.integerToEnum(jobMasterXML.getRpaJobType(), RpaJobTypeEnum.class));
		
		//直接実行
		ret.setFacilityID(jobMasterXML.getRpaScenarioFacilityId());
		ret.setProcessingMethod(OpenApiEnumConverter.integerToEnum(jobMasterXML.getRpaProcessMode(), ProcessingMethodEnum.class));
		ret.setRpaToolId(jobMasterXML.getRpaToolId());
		ret.setRpaExeFilepath(jobMasterXML.getRpaExeFilepath());
		ret.setRpaScenarioFilepath(jobMasterXML.getRpaScenarioFilepath());
		ret.setRpaLogDirectory(jobMasterXML.getRpaLogDirectory());
		ret.setRpaLogFileName(jobMasterXML.getRpaLogFileName());
		ret.setRpaLogEncoding(jobMasterXML.getRpaLogEncoding());
		ret.setRpaLogReturnCode(jobMasterXML.getRpaLogReturnCode());
		ret.setRpaLogPatternHead(jobMasterXML.getRpaLogPatternHead());
		ret.setRpaLogPatternTail(jobMasterXML.getRpaLogPatternTail());
		if(jobMasterXML.hasRpaLogMaxBytes()){
			ret.setRpaLogMaxBytes(jobMasterXML.getRpaLogMaxBytes());
		}
		if(jobMasterXML.hasRpaDefaultEndValue()){
			ret.setRpaDefaultEndValue(jobMasterXML.getRpaDefaultEndValue());
		}
		if(jobMasterXML.hasRpaLoginFlg()){
			ret.setRpaLoginFlg(jobMasterXML.getRpaLoginFlg());
		}
		ret.setRpaLoginUserId(jobMasterXML.getRpaLoginUserId());
		ret.setRpaLoginPassword(jobMasterXML.getRpaLoginPassword());
		if(jobMasterXML.hasRpaLoginRetry()){
			ret.setRpaLoginRetry(jobMasterXML.getRpaLoginRetry());
		}
		if(jobMasterXML.hasRpaLoginEndValue()){
			ret.setRpaLoginEndValue(jobMasterXML.getRpaLoginEndValue());
		}
		ret.setRpaLoginResolution(jobMasterXML.getRpaLoginResolution());
		if(jobMasterXML.hasRpaLogoutFlg()){
			ret.setRpaLogoutFlg(jobMasterXML.getRpaLogoutFlg());
		}
		if(jobMasterXML.hasRpaScreenshotEndDelayFlg()){
			ret.setRpaScreenshotEndDelayFlg(jobMasterXML.getRpaScreenshotEndDelayFlg());
		}
		if(jobMasterXML.hasRpaScreenshotEndValueFlg()){
			ret.setRpaScreenshotEndValueFlg(jobMasterXML.getRpaScreenshotEndValueFlg());
		}
		ret.setRpaScreenshotEndValue(jobMasterXML.getRpaScreenshotEndValue());
		if(jobMasterXML.hasRpaScreenshotEndValueCondition()){
			ret.setRpaScreenshotEndValueCondition(OpenApiEnumConverter.integerToEnum(
					jobMasterXML.getRpaScreenshotEndValueCondition(), RpaScreenshotEndValueConditionEnum.class));
		}
		
		if(jobMasterXML.hasMessageRetryEndFlg()){
			ret.setMessageRetryEndFlg(jobMasterXML.getMessageRetryEndFlg());
		}
		if(jobMasterXML.hasMessageRetry()){
			ret.setMessageRetry(jobMasterXML.getMessageRetry());
		} else {
			// 未設定時はデフォルトの10を設定する。
			ret.setMessageRetry(10);
		}
		if(jobMasterXML.hasMessageRetryEndValue()){
			ret.setMessageRetryEndValue(jobMasterXML.getMessageRetryEndValue());
		}
		if(jobMasterXML.hasCommandRetryFlg()){
			ret.setCommandRetryFlg(jobMasterXML.getCommandRetryFlg());
		}
		if(jobMasterXML.hasCommandRetry()){
			ret.setCommandRetry(jobMasterXML.getCommandRetry());
		} else {
			// 未設定時はデフォルトの10を設定する。
			ret.setCommandRetry(10);
		}
		// nullの場合はEnumの先頭の値がセットされる
		ret.setCommandRetryEndStatus(OpenApiEnumConverter.integerToEnum(jobMasterXML.getCommandRetryEndStatus(), 
				JobRpaInfoResponse.CommandRetryEndStatusEnum.class));
		
		if(jobMasterXML.hasRpaNotLoginNotify()){
			ret.setRpaNotLoginNotify(jobMasterXML.getRpaNotLoginNotify());
		}
		if(jobMasterXML.hasRpaNotLoginNotifyPriority()){
			ret.setRpaNotLoginNotifyPriority(OpenApiEnumConverter.integerToEnum(
					jobMasterXML.getRpaNotLoginNotifyPriority(), JobRpaInfoResponse.RpaNotLoginNotifyPriorityEnum.class));
		}
		if(jobMasterXML.hasRpaNotLoginEndValue()){
			ret.setRpaNotLoginEndValue(jobMasterXML.getRpaNotLoginEndValue());
		}
		if(jobMasterXML.hasRpaAlreadyRunningNotify()){
			ret.setRpaAlreadyRunningNotify(jobMasterXML.getRpaAlreadyRunningNotify());
		}
		if(jobMasterXML.hasRpaAlreadyRunningNotifyPriority()){
			ret.setRpaAlreadyRunningNotifyPriority(OpenApiEnumConverter.integerToEnum(
					jobMasterXML.getRpaAlreadyRunningNotifyPriority(), JobRpaInfoResponse.RpaAlreadyRunningNotifyPriorityEnum.class));
		}
		if(jobMasterXML.hasRpaAlreadyRunningEndValue()){
			ret.setRpaAlreadyRunningEndValue(jobMasterXML.getRpaAlreadyRunningEndValue());
		}
		if(jobMasterXML.hasRpaAbnormalExitNotify()){
			ret.setRpaAbnormalExitNotify(jobMasterXML.getRpaAbnormalExitNotify());
		}
		if(jobMasterXML.hasRpaAbnormalExitNotifyPriority()){
			ret.setRpaAbnormalExitNotifyPriority(OpenApiEnumConverter.integerToEnum(
					jobMasterXML.getRpaAbnormalExitNotifyPriority(), JobRpaInfoResponse.RpaAbnormalExitNotifyPriorityEnum.class));
		}
		if(jobMasterXML.hasRpaAbnormalExitEndValue()){
			ret.setRpaAbnormalExitEndValue(jobMasterXML.getRpaAbnormalExitEndValue());
		}
		for(RpaJobOptionInfos res : jobMasterXML.getRpaJobOptionInfos()){
			JobRpaOptionInfoResponse mst = new JobRpaOptionInfoResponse();
			mst.setOrderNo(res.getOrderNo());
			mst.setDescription(res.getDescription());
			mst.setOption(res.getOption());
			ret.addRpaJobOptionInfosItem(mst);
		}
		if(ret.getRpaJobOptionInfos() == null){
			ret.setRpaJobOptionInfos(new ArrayList<JobRpaOptionInfoResponse>());
		}
		for(RpaJobEndValueConditionInfos res : jobMasterXML.getRpaJobEndValueConditionInfos()){
			JobRpaEndValueConditionInfoResponse mst = new JobRpaEndValueConditionInfoResponse();
			mst.setOrderNo(res.getOrderNo());
			mst.setDescription(res.getDescription());
			mst.setConditionType(OpenApiEnumConverter.integerToEnum(res.getConditionType(), ConditionTypeEnum.class));
			mst.setPattern(res.getPattern());
			if(res.hasCaseSensitivityFlg()){
				mst.setCaseSensitivityFlg(res.getCaseSensitivityFlg());
			}
			if(res.hasProcessType()){
				mst.setProcessType(res.getProcessType());
			}
			mst.setReturnCode(res.getReturnCode());
			if(res.hasReturnCodeCondition()){
				mst.setReturnCodeCondition(OpenApiEnumConverter.integerToEnum(res.getReturnCodeCondition(), 
						JobRpaEndValueConditionInfoResponse.ReturnCodeConditionEnum.class));
			}
			if(res.hasUseCommandReturnCodeFlg()){
				mst.setUseCommandReturnCodeFlg(res.getUseCommandReturnCodeFlg());
			}
			if(res.hasEndValue()){
				mst.setEndValue(res.getEndValue());
			}
			ret.addRpaJobEndValueConditionInfosItem(mst);
		}
		if(ret.getRpaJobEndValueConditionInfos() == null){
			ret.setRpaJobEndValueConditionInfos(new ArrayList<JobRpaEndValueConditionInfoResponse>());
		}
		
		// 間接実行
		ret.setRpaScopeId(jobMasterXML.getRpaScopeId());
		if(jobMasterXML.hasRpaRunType()){
			ret.setRpaRunType(jobMasterXML.getRpaRunType());
		}
		
		ret.setRpaScenarioParam(jobMasterXML.getRpaScenarioParam());
		if(jobMasterXML.hasRpaStopMode()){
			ret.setRpaStopMode(jobMasterXML.getRpaStopMode());
		}
		for(RpaJobRunParamInfos res : jobMasterXML.getRpaJobRunParamInfos()){
			JobRpaRunParamInfoResponse mst = new JobRpaRunParamInfoResponse();
			mst.setParamId(res.getParamId());
			mst.setParamValue(res.getParamValue());
			ret.addRpaJobRunParamInfosItem(mst);
		}
		if(ret.getRpaJobRunParamInfos() == null){
			ret.setRpaJobRunParamInfos(new ArrayList<JobRpaRunParamInfoResponse>());
		}
		if(jobMasterXML.hasRpaStopType()){
			ret.setRpaStopType(OpenApiEnumConverter.integerToEnum(
					jobMasterXML.getRpaStopType(), JobRpaInfoResponse.RpaStopTypeEnum.class));
		}
		if(jobMasterXML.hasRpaRunConnectTimeout()){
			ret.setRpaRunConnectTimeout(jobMasterXML.getRpaRunConnectTimeout());
		}
		if(jobMasterXML.hasRpaRunRequestTimeout()){
			ret.setRpaRunRequestTimeout(jobMasterXML.getRpaRunRequestTimeout());
		}
		if(jobMasterXML.hasRpaRunEndFlg()){
			ret.setRpaRunEndFlg(jobMasterXML.getRpaRunEndFlg());
		}
		if(jobMasterXML.hasRpaRunRetry()){
			ret.setRpaRunRetry(jobMasterXML.getRpaRunRetry());
		}
		if(jobMasterXML.hasRpaRunEndValue()){
			ret.setRpaRunEndValue(jobMasterXML.getRpaRunEndValue());
		}
		if(jobMasterXML.hasRpaCheckConnectTimeout()){
			ret.setRpaCheckConnectTimeout(jobMasterXML.getRpaCheckConnectTimeout());
		}
		if(jobMasterXML.hasRpaCheckRequestTimeout()){
			ret.setRpaCheckRequestTimeout(jobMasterXML.getRpaCheckRequestTimeout());
		}
		if(jobMasterXML.hasRpaCheckEndFlg()){
			ret.setRpaCheckEndFlg(jobMasterXML.getRpaCheckEndFlg());
		}
		if(jobMasterXML.hasRpaCheckRetry()){
			ret.setRpaCheckRetry(jobMasterXML.getRpaCheckRetry());
		}
		if(jobMasterXML.hasRpaCheckEndValue()){
			ret.setRpaCheckEndValue(jobMasterXML.getRpaCheckEndValue());
		}
		for(RpaJobCheckEndValueInfos res : jobMasterXML.getRpaJobCheckEndValueInfos()){
			JobRpaCheckEndValueInfoResponse mst = new JobRpaCheckEndValueInfoResponse();
			mst.setEndStatusId(res.getEndStatusId());
			mst.setEndValue(res.getEndValue());
			ret.addRpaJobCheckEndValueInfosItem(mst);
		}
		if(ret.getRpaJobCheckEndValueInfos() == null){
			ret.setRpaJobCheckEndValueInfos(new ArrayList<JobRpaCheckEndValueInfoResponse>());
		}

		return ret;
	}
	
	/**
	 * ファイルチェックジョブに関するDTOを返します.
	 * @param jobMasterXML
	 * @return
	 */
	private static JobFileCheckInfoResponse getDTOFileCheck(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws InvalidSetting, HinemosUnknown{
		JobFileCheckInfoResponse ret = new JobFileCheckInfoResponse();
		
		ret.setFacilityID(jobMasterXML.getFileCheckFacilityId());
		ret.setProcessingMethod(OpenApiEnumConverter.integerToEnum(jobMasterXML.getFileCheckProcessMode(), 
				JobFileCheckInfoResponse.ProcessingMethodEnum.class));
		
		if(jobMasterXML.hasFileCheckSuccessEndValue()){
			ret.setSuccessEndValue(jobMasterXML.getFileCheckSuccessEndValue());
		}
		if(jobMasterXML.hasFileCheckFailureEndFlg()){
			ret.setFailureEndFlg(jobMasterXML.getFileCheckFailureEndFlg());
		}
		if(jobMasterXML.hasFileCheckFailureWaitTime()){
			ret.setFailureWaitTime(jobMasterXML.getFileCheckFailureWaitTime());
		}
		if(jobMasterXML.hasFileCheckFailureEndValue()){
			ret.setFailureEndValue(jobMasterXML.getFileCheckFailureEndValue());
		}
		ret.setDirectory(jobMasterXML.getDirectory());
		ret.setFileName(jobMasterXML.getFileName());
		if(jobMasterXML.hasCreateValidFlg()){
			ret.setCreateValidFlg(jobMasterXML.getCreateValidFlg());
		}
		if(jobMasterXML.hasCreateBeforeJobStartFlg()){
			ret.setCreateBeforeJobStartFlg(jobMasterXML.getCreateBeforeJobStartFlg());
		}
		if(jobMasterXML.hasDeleteValidFlg()){
			ret.setDeleteValidFlg((jobMasterXML.getDeleteValidFlg()));
		}
		if(jobMasterXML.hasModifyValidFlg()){
			ret.setModifyValidFlg((jobMasterXML.getModifyValidFlg()));
		}
		if(jobMasterXML.hasModifyType()){
			ret.setModifyType(OpenApiEnumConverter.integerToEnum(jobMasterXML.getModifyType(), ModifyTypeEnum.class));
		}
		if(jobMasterXML.hasNotJudgeFileInUseFlg()){
			ret.setNotJudgeFileInUseFlg(jobMasterXML.getNotJudgeFileInUseFlg());
		}
		
		if(jobMasterXML.hasMessageRetryEndFlg()){
			ret.setMessageRetryEndFlg(jobMasterXML.getMessageRetryEndFlg());
		}
		if(jobMasterXML.hasMessageRetry()){
			ret.setMessageRetry(jobMasterXML.getMessageRetry());
		} else {
			// 未設定時はデフォルトの10を設定する。
			ret.setMessageRetry(10);
		}
		if(jobMasterXML.hasMessageRetryEndValue()){
			ret.setMessageRetryEndValue(jobMasterXML.getMessageRetryEndValue());
		}
		
		return ret;
	}
	
	/**
	 * ジョブ連携送信ジョブに関するDTOを返します.
	 * @param jobMasterXML
	 * @return
	 */
	private static JobLinkSendInfoResponse getDTOLinkSend(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws InvalidSetting, HinemosUnknown{
		JobLinkSendInfoResponse ret = new JobLinkSendInfoResponse();
		
		if(jobMasterXML.hasRetryFlg()){
			ret.setRetryFlg(jobMasterXML.getRetryFlg());
		}
		if(jobMasterXML.hasRetryCount()){
			ret.setRetryCount(jobMasterXML.getRetryCount());
		}
		if(jobMasterXML.hasFailureOperation()){
			ret.setFailureOperation(OpenApiEnumConverter.integerToEnum(jobMasterXML.getFailureOperation(), 
					JobLinkSendInfoResponse.FailureOperationEnum.class));
		}
		if(jobMasterXML.hasFailureEndStatus()){
			ret.setFailureEndStatus(OpenApiEnumConverter.integerToEnum(jobMasterXML.getFailureEndStatus(),
					JobLinkSendInfoResponse.FailureEndStatusEnum.class));
		}
		ret.setJoblinkMessageId(jobMasterXML.getJoblinkMessageId());
		if(jobMasterXML.hasPriority()){
			ret.setPriority(OpenApiEnumConverter.integerToEnum(jobMasterXML.getPriority(),
					JobLinkSendInfoResponse.PriorityEnum.class));
		}
		ret.setMessage(jobMasterXML.getMessage());
		if(jobMasterXML.hasLinkSendSuccessEndValue()){
			ret.setSuccessEndValue(jobMasterXML.getLinkSendSuccessEndValue());
		}
		if(jobMasterXML.hasLinkSendFailureEndValue()){
			ret.setFailureEndValue(jobMasterXML.getLinkSendFailureEndValue());
		}
		ret.setJoblinkSendSettingId(jobMasterXML.getJoblinkSendSettingId());
		
		for(JobLinkExp exp : jobMasterXML.getJobLinkExp()){
			JobLinkExpInfoResponse expInfo = new JobLinkExpInfoResponse();
			expInfo.setKey(exp.getKey());
			expInfo.setValue(exp.getValue());
			ret.addJobLinkExpListItem(expInfo);
		}
		
		return ret;
	}
	
	/**
	 * ジョブ連携待機ジョブに関するDTOを返します.
	 * @param jobMasterXML
	 * @return
	 */
	private static JobLinkRcvInfoResponse getDTOLinkRcv(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws InvalidSetting, HinemosUnknown{
		JobLinkRcvInfoResponse ret = new JobLinkRcvInfoResponse();
		
		ret.setFacilityID(jobMasterXML.getLinkStandbyFacilityId());
		if(jobMasterXML.hasLinkStandbyMonitorInfoEndValue()){
			ret.setMonitorInfoEndValue(jobMasterXML.getLinkStandbyMonitorInfoEndValue());
		}
		if(jobMasterXML.hasLinkStandbyMonitorWarnEndValue()){
			ret.setMonitorWarnEndValue(jobMasterXML.getLinkStandbyMonitorWarnEndValue());
		}
		if(jobMasterXML.hasLinkStandbyMonitorCriticalEndValue()){
			ret.setMonitorCriticalEndValue(jobMasterXML.getLinkStandbyMonitorCriticalEndValue());
		}
		if(jobMasterXML.hasLinkStandbyMonitorUnknownEndValue()){
			ret.setMonitorUnknownEndValue(jobMasterXML.getLinkStandbyMonitorUnknownEndValue());
		}
		if(jobMasterXML.hasLinkStandbyFailureEndFlg()){
			ret.setFailureEndFlg(jobMasterXML.getLinkStandbyFailureEndFlg());
		}
		if(jobMasterXML.hasLinkStandbyMonitorWaitTime()){
			ret.setMonitorWaitTime(jobMasterXML.getLinkStandbyMonitorWaitTime());
		}
		if(jobMasterXML.hasLinkStandbyMonitorWaitEndValue()){
			ret.setMonitorWaitEndValue(jobMasterXML.getLinkStandbyMonitorWaitEndValue());
		}
		ret.setJoblinkMessageId(jobMasterXML.getLinkStandbyJoblinkMessageId());
		if(jobMasterXML.hasPastFlg()){
			ret.setPastFlg(jobMasterXML.getPastFlg());
		}
		if(jobMasterXML.hasPastMin()){
			ret.setPastMin(jobMasterXML.getPastMin());
		}
		if(jobMasterXML.hasInfoValidFlg()){
			ret.setInfoValidFlg(jobMasterXML.getInfoValidFlg());
		}
		if(jobMasterXML.hasWarnValidFlg()){
			ret.setWarnValidFlg(jobMasterXML.getWarnValidFlg());
		}
		if(jobMasterXML.hasCriticalValidFlg()){
			ret.setCriticalValidFlg(jobMasterXML.getCriticalValidFlg());
		}
		if(jobMasterXML.hasUnknownValidFlg()){
			ret.setUnknownValidFlg(jobMasterXML.getUnknownValidFlg());
		}
		if(jobMasterXML.hasApplicationFlg()){
			ret.setApplicationFlg(jobMasterXML.getApplicationFlg());
		}
		ret.setApplication(jobMasterXML.getApplication());
		if(jobMasterXML.hasMonitorDetailIdFlg()){
			ret.setMonitorDetailIdFlg(jobMasterXML.getMonitorDetailIdFlg());
		}
		ret.setMonitorDetailId(jobMasterXML.getMonitorDetailId());
		if(jobMasterXML.hasMessageFlg()){
			ret.setMessageFlg(jobMasterXML.getMessageFlg());
		}
		ret.setMessage(jobMasterXML.getLinkStandbyMessage());
		if(jobMasterXML.hasExpFlg()){
			ret.setExpFlg(jobMasterXML.getExpFlg());
		}
		if(jobMasterXML.hasMonitorAllEndValueFlg()){
			ret.setMonitorAllEndValueFlg(jobMasterXML.getMonitorAllEndValueFlg());
		}
		if(jobMasterXML.hasMonitorAllEndValue()){
			ret.setMonitorAllEndValue(jobMasterXML.getMonitorAllEndValue());
		}
		
		for(JobLinkExp exp : jobMasterXML.getJobLinkExp()){
			JobLinkExpInfoResponse expInfo = new JobLinkExpInfoResponse();
			expInfo.setKey(exp.getKey());
			expInfo.setValue(exp.getValue());
			ret.addJobLinkExpListItem(expInfo);
		}
		
		for(JobLinkInherit inherit : jobMasterXML.getJobLinkInherit()){
			JobLinkInheritInfoResponse inheritInfo = new JobLinkInheritInfoResponse();
			inheritInfo.setParamId(inherit.getParamId());
			inheritInfo.setExpKey(inherit.getExpKey());
			inheritInfo.setKeyInfo(OpenApiEnumConverter.stringToEnum(
					inherit.getKeyInfo(), JobLinkInheritInfoResponse.KeyInfoEnum.class));
			ret.addJobLinkInheritListItem(inheritInfo);
		}
		
		return ret;
	}
	
	/**
	 * リソース制御ジョブに関するDTOを返します.
	 * @param jobMasterXML
	 * @return
	 */
	private static JobResourceInfoResponse getDTOResource(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws InvalidSetting, HinemosUnknown{
		JobResourceInfoResponse ret = new JobResourceInfoResponse();
		
		ret.setResourceCloudScopeId(jobMasterXML.getResourceCloudScopeId());
		ret.setResourceLocationId(jobMasterXML.getResourceLocationId());
		if(jobMasterXML.hasResourceType()){
			ret.setResourceType(OpenApiEnumConverter.integerToEnum(jobMasterXML.getResourceType(), JobResourceInfoResponse.ResourceTypeEnum.class));
		}
		if(jobMasterXML.hasResourceAction()){
			ret.setResourceAction(OpenApiEnumConverter.integerToEnum(jobMasterXML.getResourceAction(), JobResourceInfoResponse.ResourceActionEnum.class));
		}
		switch(OpenApiEnumConverter.integerToEnum(jobMasterXML.getResourceType(), JobResourceInfoResponse.ResourceTypeEnum.class)) {
		case COMPUTE_FACILITY_ID:
			ret.setResourceTargetId(jobMasterXML.getResourceTargetFacilityId());
			break;
		case COMPUTE_COMPUTE_ID:
			ret.setResourceTargetId(jobMasterXML.getResourceTargetComputeId());
			break;
		case STORAGE:
			ret.setResourceTargetId(jobMasterXML.getResourceTargetStorageId());
			break;
		
		}
		if(jobMasterXML.hasResourceStatusConfirmTime()){
			ret.setResourceStatusConfirmTime(jobMasterXML.getResourceStatusConfirmTime());
		}
		if(jobMasterXML.hasResourceStatusConfirmInterval()){
			ret.setResourceStatusConfirmInterval(jobMasterXML.getResourceStatusConfirmInterval());
		}
		ret.setResourceAttachNode(jobMasterXML.getResourceAttachNode());
		ret.setResourceAttachDevice(jobMasterXML.getResourceAttachDevice());
		ret.setResourceNotifyScope(jobMasterXML.getResourceNotifyScope());
		if(jobMasterXML.hasResourceSuccessEndValue()){
			ret.setResourceSuccessValue(jobMasterXML.getResourceSuccessEndValue());
		}
		if(jobMasterXML.hasResourceFailureEndValue()){
			ret.setResourceFailureValue(jobMasterXML.getResourceFailureEndValue());
		}
		
		return ret;
	}

	/**
	 * ジョブ詳細データを生成
	 *
	 * @return
	 * @throws ParseException 
	 * @throws NullPointerException 
	 *
	 * @see
	 */
	public static com.clustercontrol.utility.settings.job.xml.JobInfo setXMLJobData(JobInfoWrapper jobMgr,String parentJobID) throws NullPointerException, ParseException{
		com.clustercontrol.utility.settings.job.xml.JobInfo jobXML = new com.clustercontrol.utility.settings.job.xml.JobInfo();

		if (jobMgr.getJobunitId() != null) {
			jobXML.setJobunitId(jobMgr.getJobunitId());
		}

		// Check if it is not top level
		if (parentJobID != null) {
			jobXML.setParentJobId(parentJobID);
			jobXML.setParentJobunitId(jobMgr.getJobunitId());
		} else if (jobMgr.getType().equals(JobInfoResponse.TypeEnum.JOBUNIT)) {
			jobXML.setParentJobId(TOP_JOB_ID);
			jobXML.setParentJobunitId(TOP_JOBUNIT_ID);
		}

		if (jobMgr.getId() != null) {
			jobXML.setId(jobMgr.getId());
		}
		if (jobMgr.getName() != null) {
			jobXML.setName(jobMgr.getName());
		}
		if (jobMgr.getDescription() != null) {
			jobXML.setDescription(jobMgr.getDescription());
		}
		if (jobMgr.getCreateTime() != null) {
			jobXML.setCreateTime( DateUtil.convDateFormatHinemos2Iso8601(jobMgr.getCreateTime()));
		}
		if (jobMgr.getUpdateTime() != null) {
			jobXML.setUpdateTime( DateUtil.convDateFormatHinemos2Iso8601(jobMgr.getUpdateTime()));
		}
		if (jobMgr.getCreateUser() != null) {
			jobXML.setCreateUser(jobMgr.getCreateUser());
		}
		if (jobMgr.getUpdateUser() != null) {
			jobXML.setUpdateUser(jobMgr.getUpdateUser());
		}

		jobXML.setType(OpenApiEnumConverter.enumToInteger(jobMgr.getType()));
		jobXML.setOwnerRoleId(jobMgr.getOwnerRoleId());

		jobXML.setRegisteredModule(jobMgr.getRegistered());
		jobXML.setIconId(jobMgr.getIconId());

		if (jobMgr.getType().equals(JobInfoResponse.TypeEnum.JOBUNIT)) {
			if (jobMgr.getExpNodeRuntimeFlg() != null) {
				jobXML.setExpNodeRuntimeFlg(jobMgr.getExpNodeRuntimeFlg());
			}
		}

		JobCommandInfoResponse commandInfo = jobMgr.getCommand();
		if (commandInfo != null) {
			jobXML.setFacilityId(commandInfo.getFacilityID());
			// jobMgr.getCommand().getScope() should be ignored because of no
			// use
			jobXML.setProcessMode(OpenApiEnumConverter.enumToInteger(commandInfo.getProcessingMethod()));

			// スクリプトをマネージャから配布
			jobXML.setManagerDistribution(commandInfo.getManagerDistribution());
			// スクリプト名
			jobXML.setScriptName(commandInfo.getScriptName());
			// スクリプトエンコーディング
			jobXML.setScriptEncoding(commandInfo.getScriptEncoding());
			// スクリプト内容
			jobXML.setScriptContent(commandInfo.getScriptContent());
			// コマンドジョブパラメータ
			for(JobCommandParamResponse param : commandInfo.getJobCommandParamList()){
				CommandParam p = new CommandParam();
				p.setJobStandardOutputFlg(param.getJobStandardOutputFlg());
				p.setParamId(param.getParamId());
				p.setValue(param.getValue());
				jobXML.addCommandParam(p);
			}
			// コマンドジョブ環境変数
			for(JobEnvVariableInfoResponse var : commandInfo.getEnvVariable() ) {
				EnvVariableInfo e = new EnvVariableInfo();
				e.setEnvVariableId(var.getEnvVariableId());
				e.setValue(var.getValue());
				e.setDescription(var.getDescription());
				jobXML.addEnvVariableInfo(e);
			}

			jobXML.setStartCommand(commandInfo.getStartCommand());
			jobXML.setStopCommand(commandInfo.getStopCommand());
			jobXML.setStopType(OpenApiEnumConverter.enumToInteger(commandInfo.getStopType()));
			jobXML.setSpecifyUser(commandInfo.getSpecifyUser());
			jobXML.setEffectiveUser(commandInfo.getUser());
			jobXML.setMessageRetryEndFlg(commandInfo.getMessageRetryEndFlg());
			jobXML.setMessageRetry(commandInfo.getMessageRetry());
			jobXML.setMessageRetryEndValue(commandInfo.getMessageRetryEndValue());
			jobXML.setCommandRetryFlg(commandInfo.getCommandRetryFlg());

			// Hard-code : CommandRetry could be NULL
			if (commandInfo.getCommandRetryEndStatus() == null) {
				jobXML.deleteCommandRetryEndStatus();
			} else {
				jobXML.setCommandRetryEndStatus(OpenApiEnumConverter.enumToInteger(commandInfo.getCommandRetryEndStatus()));
			}
			jobXML.setCommandRetry(commandInfo.getCommandRetry() != null ? commandInfo.getCommandRetry() : 10);
			
			// ファイル出力（標準出力）
			if(commandInfo.getNormalJobOutputInfo() != null){
				NormalJobOutput normalJobOutput = new NormalJobOutput();
				normalJobOutput.setOutputType(JobOutputType.STDOUT.getCode());
				normalJobOutput.setDirectory(commandInfo.getNormalJobOutputInfo().getDirectory());
				normalJobOutput.setFileName(commandInfo.getNormalJobOutputInfo().getFileName());
				if(commandInfo.getNormalJobOutputInfo().getAppendFlg() != null){
					normalJobOutput.setAppendFlg(commandInfo.getNormalJobOutputInfo().getAppendFlg());
				}
				if(commandInfo.getNormalJobOutputInfo().getFailureOperationFlg() != null){
					normalJobOutput.setFailureOperationFlg(commandInfo.getNormalJobOutputInfo().getFailureOperationFlg());
				}
				if(commandInfo.getNormalJobOutputInfo().getFailureOperationType() != null){
					normalJobOutput.setFailureOperationType(OpenApiEnumConverter.enumToInteger(
							commandInfo.getNormalJobOutputInfo().getFailureOperationType()));
				}
				if(commandInfo.getNormalJobOutputInfo().getFailureOperationEndStatus() != null){
					normalJobOutput.setFailureOperationEndStatus(OpenApiEnumConverter.enumToInteger(
							commandInfo.getNormalJobOutputInfo().getFailureOperationEndStatus()));
				}
				if(commandInfo.getNormalJobOutputInfo().getFailureOperationEndValue() != null){
					normalJobOutput.setFailureOperationEndValue(commandInfo.getNormalJobOutputInfo().getFailureOperationEndValue());
				}
				if(commandInfo.getNormalJobOutputInfo().getFailureNotifyFlg() != null){
					normalJobOutput.setFailureNotifyFlg(commandInfo.getNormalJobOutputInfo().getFailureNotifyFlg());
				}
				if(commandInfo.getNormalJobOutputInfo().getFailureNotifyPriority() != null){
					normalJobOutput.setFailureNotifyPriority(OpenApiEnumConverter.enumToInteger(
							commandInfo.getNormalJobOutputInfo().getFailureNotifyPriority()));
				}
				if(commandInfo.getNormalJobOutputInfo().getValid() != null){
					normalJobOutput.setValidFlg(commandInfo.getNormalJobOutputInfo().getValid());
				}
				jobXML.setNormalJobOutput(normalJobOutput);
			}
			
			// ファイル出力（標準エラー出力）
			if(commandInfo.getErrorJobOutputInfo() != null){
				ErrorJobOutput errorJobOutput = new ErrorJobOutput();
				errorJobOutput.setOutputType(JobOutputType.STDERR.getCode());
				if(commandInfo.getErrorJobOutputInfo().getSameNormalFlg() != null){
					errorJobOutput.setSameNormalFlg(commandInfo.getErrorJobOutputInfo().getSameNormalFlg());
				}
				errorJobOutput.setDirectory(commandInfo.getErrorJobOutputInfo().getDirectory());
				errorJobOutput.setFileName(commandInfo.getErrorJobOutputInfo().getFileName());
				if(commandInfo.getErrorJobOutputInfo().getAppendFlg() != null){
					errorJobOutput.setAppendFlg(commandInfo.getErrorJobOutputInfo().getAppendFlg());
				}
				if(commandInfo.getErrorJobOutputInfo().getFailureOperationFlg() != null){
					errorJobOutput.setFailureOperationFlg(commandInfo.getErrorJobOutputInfo().getFailureOperationFlg());
				}
				if(commandInfo.getErrorJobOutputInfo().getFailureOperationType() != null){
					errorJobOutput.setFailureOperationType(OpenApiEnumConverter.enumToInteger(
							commandInfo.getErrorJobOutputInfo().getFailureOperationType()));
				}
				if(commandInfo.getErrorJobOutputInfo().getFailureOperationEndStatus() != null){
					errorJobOutput.setFailureOperationEndStatus(OpenApiEnumConverter.enumToInteger(
							commandInfo.getErrorJobOutputInfo().getFailureOperationEndStatus()));
				}
				if(commandInfo.getErrorJobOutputInfo().getFailureOperationEndValue() != null){
					errorJobOutput.setFailureOperationEndValue(commandInfo.getErrorJobOutputInfo().getFailureOperationEndValue());
				}
				if(commandInfo.getErrorJobOutputInfo().getFailureNotifyFlg() != null){
					errorJobOutput.setFailureNotifyFlg(commandInfo.getErrorJobOutputInfo().getFailureNotifyFlg());
				}
				if(commandInfo.getErrorJobOutputInfo().getFailureNotifyPriority() != null){
					errorJobOutput.setFailureNotifyPriority(OpenApiEnumConverter.enumToInteger(
							commandInfo.getErrorJobOutputInfo().getFailureNotifyPriority()));
				}
				if(commandInfo.getErrorJobOutputInfo().getValid() != null){
					errorJobOutput.setValidFlg(commandInfo.getErrorJobOutputInfo().getValid());
				}
				jobXML.setErrorJobOutput(errorJobOutput);
			}
		}

		// 終了状態
		List<JobEndStatusInfoResponse> endStatusInfos = jobMgr.getEndStatus();
		if (endStatusInfos.size() > 0) {
			// 正常
			jobXML.setNormalEndValue(Objects.isNull(endStatusInfos.get(0).getValue())?0:endStatusInfos.get(0).getValue());
			jobXML.setNormalEndValueFrom(Objects.isNull(endStatusInfos.get(0).getStartRangeValue())?0:endStatusInfos.get(0).getStartRangeValue());
			jobXML.setNormalEndValueTo(Objects.isNull(endStatusInfos.get(0).getEndRangeValue())?0:endStatusInfos.get(0).getEndRangeValue());
			// 警告
			jobXML.setWarnEndValue(Objects.isNull(endStatusInfos.get(1).getValue())?0:endStatusInfos.get(1).getValue());
			jobXML.setWarnEndValueFrom(Objects.isNull(endStatusInfos.get(1).getStartRangeValue())?0:endStatusInfos.get(1).getStartRangeValue());
			jobXML.setWarnEndValueTo(Objects.isNull(endStatusInfos.get(1).getEndRangeValue())?0:endStatusInfos.get(1).getEndRangeValue());
			// 異常
			jobXML.setAbnormalEndValue(Objects.isNull(endStatusInfos.get(2).getValue())?0:endStatusInfos.get(2).getValue());
			jobXML.setAbnormalEndValueFrom(Objects.isNull(endStatusInfos.get(2).getStartRangeValue())?0:endStatusInfos.get(2).getStartRangeValue());
			jobXML.setAbnormalEndValueTo(Objects.isNull(endStatusInfos.get(2).getEndRangeValue())?0:endStatusInfos.get(2).getEndRangeValue());
		}

		// 承認の場合
		if (jobMgr.getType().equals(JobInfoResponse.TypeEnum.APPROVALJOB)) {
			// 承認依頼先ロール
			jobXML.setApprovalReqRoleId(jobMgr.getApprovalReqRoleId());
			// 承認依頼先ユーザー
			jobXML.setApprovalReqUserId(jobMgr.getApprovalReqUserId());
			// 承認依頼文
			jobXML.setApprovalReqSentence(jobMgr.getApprovalReqSentence());
			// 承認依頼メール件名
			jobXML.setApprovalReqMailTitle(jobMgr.getApprovalReqMailTitle());
			// 承認依頼メール本文
			jobXML.setApprovalReqMailBody(jobMgr.getApprovalReqMailBody());
			// 承認依頼文を利用するか？
			jobXML.setUseApprovalReqSentence(jobMgr.getIsUseApprovalReqSentence());
		}

		// 監視の場合
		if (jobMgr.getType().equals(JobInfoResponse.TypeEnum.MONITORJOB)) {
			// ファシリティID
			jobXML.setFacilityId(jobMgr.getMonitor().getFacilityID());
			//スコープ処理
			jobXML.setProcessMode(OpenApiEnumConverter.enumToInteger(jobMgr.getMonitor().getProcessingMethod()));
			// 監視設定ID
			jobXML.setMonitorId(jobMgr.getMonitor().getMonitorId());
			// 終了値(情報)
			jobXML.setMonitorInfoEndValue(jobMgr.getMonitor().getMonitorInfoEndValue());
			// 終了値(警告)
			jobXML.setMonitorWarnEndValue(jobMgr.getMonitor().getMonitorWarnEndValue());
			// 終了値(危険)
			jobXML.setMonitorCriticalEndValue(jobMgr.getMonitor().getMonitorCriticalEndValue());
			// 終了値(不明)
			jobXML.setMonitorUnknownEndValue(jobMgr.getMonitor().getMonitorUnknownEndValue());
			// 終了値(タイムアウト)
			jobXML.setMonitorWaitTime(jobMgr.getMonitor().getMonitorWaitTime());
			// 終了値
			jobXML.setMonitorWaitEndValue(jobMgr.getMonitor().getMonitorWaitEndValue());
		}

		//ファイル転送ジョブの場合
		JobFileInfoResponse fileInfo = jobMgr.getFile();
		if (fileInfo != null) {
			jobXML.setSrcFacilityId(fileInfo.getSrcFacilityID());
			jobXML.setSrcScope(fileInfo.getSrcScope()); // Skip
																// this
			jobXML.setSrcFile(fileInfo.getSrcFile());
			jobXML.setDestFacilityId(fileInfo.getDestFacilityID());
			jobXML.setDestDirectory(fileInfo.getDestDirectory());
			jobXML.setProcessMode(OpenApiEnumConverter.enumToInteger(fileInfo.getProcessingMethod()));
			jobXML.setCompressionFlg(fileInfo.getCompressionFlg());
			jobXML.setCheckFlg(fileInfo.getCheckFlg());
			jobXML.setSpecifyUser(fileInfo.getSpecifyUser());
			jobXML.setEffectiveUser(fileInfo.getUser());
			jobXML.setMessageRetryEndFlg(fileInfo.getMessageRetryEndFlg());
			jobXML.setMessageRetry(fileInfo.getMessageRetry());
			// jobMgr.getFile().getMessageRetryEndValue() should be ignored
			// because of no use. It is already deprecated from 5.0
			// Hard-code : CommandRetry could be NULL
		}

		// ジョブ連携送信ジョブの場合
		JobLinkSendInfoResponse linkSendInfo = jobMgr.getJobLinkSend();
		if(linkSendInfo != null){
			
			if(linkSendInfo.getRetryFlg() != null){
				jobXML.setRetryFlg(linkSendInfo.getRetryFlg());
			}
			if(linkSendInfo.getRetryCount() != null){
				jobXML.setRetryCount(linkSendInfo.getRetryCount());
			}
			if(linkSendInfo.getFailureOperation() != null){
				jobXML.setFailureOperation(OpenApiEnumConverter.enumToInteger(linkSendInfo.getFailureOperation()));
			}
			if(linkSendInfo.getFailureEndStatus() != null){
				jobXML.setFailureEndStatus(OpenApiEnumConverter.enumToInteger(linkSendInfo.getFailureEndStatus()));
			}
			jobXML.setJoblinkMessageId(linkSendInfo.getJoblinkMessageId());
			if(linkSendInfo.getPriority() != null){
				jobXML.setPriority(OpenApiEnumConverter.enumToInteger(linkSendInfo.getPriority()));
			}
			jobXML.setMessage(linkSendInfo.getMessage());
			if(linkSendInfo.getSuccessEndValue() != null){
				jobXML.setLinkSendSuccessEndValue(linkSendInfo.getSuccessEndValue());
			}
			if(linkSendInfo.getFailureEndValue() != null){
				jobXML.setLinkSendFailureEndValue(linkSendInfo.getFailureEndValue());
			}
			jobXML.setJoblinkSendSettingId(linkSendInfo.getJoblinkSendSettingId());
			
			for(JobLinkExpInfoResponse exp : linkSendInfo.getJobLinkExpList()){
				JobLinkExp expInfo = new JobLinkExp();
				expInfo.setKey(exp.getKey());
				expInfo.setValue(exp.getValue());
				jobXML.addJobLinkExp(expInfo);
			}
		}
		
		// ジョブ連携待機ジョブの場合
		JobLinkRcvInfoResponse fileLinkRcvInfo = jobMgr.getJobLinkRcv();
		if(fileLinkRcvInfo != null){
			jobXML.setLinkStandbyFacilityId(fileLinkRcvInfo.getFacilityID());
			if(fileLinkRcvInfo.getMonitorInfoEndValue() != null){
				jobXML.setLinkStandbyMonitorInfoEndValue(fileLinkRcvInfo.getMonitorInfoEndValue());
			}
			if(fileLinkRcvInfo.getMonitorWarnEndValue() != null){
				jobXML.setLinkStandbyMonitorWarnEndValue(fileLinkRcvInfo.getMonitorWarnEndValue());
			}
			if(fileLinkRcvInfo.getMonitorCriticalEndValue() != null){
				jobXML.setLinkStandbyMonitorCriticalEndValue(fileLinkRcvInfo.getMonitorCriticalEndValue());
			}
			if(fileLinkRcvInfo.getMonitorUnknownEndValue() != null){
				jobXML.setLinkStandbyMonitorUnknownEndValue(fileLinkRcvInfo.getMonitorUnknownEndValue());
			}
			if(fileLinkRcvInfo.getFailureEndFlg() != null){
				jobXML.setLinkStandbyFailureEndFlg(fileLinkRcvInfo.getFailureEndFlg());
			}
			if(fileLinkRcvInfo.getMonitorWaitTime() != null){
				jobXML.setLinkStandbyMonitorWaitTime(fileLinkRcvInfo.getMonitorWaitTime());
			}
			if(fileLinkRcvInfo.getMonitorWaitEndValue() != null){
				jobXML.setLinkStandbyMonitorWaitEndValue(fileLinkRcvInfo.getMonitorWaitEndValue());
			}
			jobXML.setLinkStandbyJoblinkMessageId(fileLinkRcvInfo.getJoblinkMessageId());
			if(fileLinkRcvInfo.getPastFlg() != null){
				jobXML.setPastFlg(fileLinkRcvInfo.getPastFlg());
			}
			if(fileLinkRcvInfo.getPastMin() != null){
				jobXML.setPastMin(fileLinkRcvInfo.getPastMin());
			}
			if(fileLinkRcvInfo.getInfoValidFlg() != null){
				jobXML.setInfoValidFlg(fileLinkRcvInfo.getInfoValidFlg());
			}
			if(fileLinkRcvInfo.getWarnValidFlg() != null){
				jobXML.setWarnValidFlg(fileLinkRcvInfo.getWarnValidFlg());
			}
			if(fileLinkRcvInfo.getCriticalValidFlg() != null){
				jobXML.setCriticalValidFlg(fileLinkRcvInfo.getCriticalValidFlg());
			}
			if(fileLinkRcvInfo.getUnknownValidFlg() != null){
				jobXML.setUnknownValidFlg(fileLinkRcvInfo.getUnknownValidFlg());
			}
			if(fileLinkRcvInfo.getApplicationFlg() != null){
				jobXML.setApplicationFlg(fileLinkRcvInfo.getApplicationFlg());
			}
			jobXML.setApplication(fileLinkRcvInfo.getApplication());
			if(fileLinkRcvInfo.getMonitorDetailIdFlg() != null){
				jobXML.setMonitorDetailIdFlg(fileLinkRcvInfo.getMonitorDetailIdFlg());
			}
			jobXML.setMonitorDetailId(fileLinkRcvInfo.getMonitorDetailId());
			if(fileLinkRcvInfo.getMessageFlg() != null){
				jobXML.setMessageFlg(fileLinkRcvInfo.getMessageFlg());
			}
			jobXML.setLinkStandbyMessage(fileLinkRcvInfo.getMessage());
			if(fileLinkRcvInfo.getExpFlg() != null){
				jobXML.setExpFlg(fileLinkRcvInfo.getExpFlg());
			}
			if(fileLinkRcvInfo.getMonitorAllEndValueFlg() != null){
				jobXML.setMonitorAllEndValueFlg(fileLinkRcvInfo.getMonitorAllEndValueFlg());
			}
			if(fileLinkRcvInfo.getMonitorAllEndValue() != null){
				jobXML.setMonitorAllEndValue(fileLinkRcvInfo.getMonitorAllEndValue());
			}
			
			for(JobLinkExpInfoResponse expInfo : fileLinkRcvInfo.getJobLinkExpList()){
				JobLinkExp exp = new JobLinkExp();
				exp.setKey(expInfo.getKey());
				exp.setValue(expInfo.getValue());
				jobXML.addJobLinkExp(exp);
			}
			
			for(JobLinkInheritInfoResponse inheritInfo : fileLinkRcvInfo.getJobLinkInheritList()){
				JobLinkInherit inherit = new JobLinkInherit();
				inherit.setParamId(inheritInfo.getParamId());
				inherit.setExpKey(inheritInfo.getExpKey());
				inherit.setKeyInfo(OpenApiEnumConverter.enumToString(inheritInfo.getKeyInfo()));
				jobXML.addJobLinkInherit(inherit);
			}
		}
		
		// ファイルチェックジョブの場合
		JobFileCheckInfoResponse fileCheckInfo = jobMgr.getJobFileCheck();
		if(fileCheckInfo != null){
			jobXML.setFileCheckFacilityId(fileCheckInfo.getFacilityID());
			jobXML.setFileCheckProcessMode((OpenApiEnumConverter.enumToInteger(fileCheckInfo.getProcessingMethod())));
			
			if(fileCheckInfo.getSuccessEndValue() != null){
				jobXML.setFileCheckSuccessEndValue(fileCheckInfo.getSuccessEndValue());
			}
			if(fileCheckInfo.getFailureEndFlg() != null){
				jobXML.setFileCheckFailureEndFlg(fileCheckInfo.getFailureEndFlg());
			}
			if(fileCheckInfo.getFailureWaitTime() != null){
				jobXML.setFileCheckFailureWaitTime(fileCheckInfo.getFailureWaitTime());
			}
			if(fileCheckInfo.getFailureEndValue() != null){
				jobXML.setFileCheckFailureEndValue(fileCheckInfo.getFailureEndValue());
			}
			jobXML.setDirectory(fileCheckInfo.getDirectory());
			jobXML.setFileName(fileCheckInfo.getFileName());
			if(fileCheckInfo.getCreateValidFlg() != null){
				jobXML.setCreateValidFlg(fileCheckInfo.getCreateValidFlg());
			}
			if(fileCheckInfo.getCreateBeforeJobStartFlg() != null){
				jobXML.setCreateBeforeJobStartFlg(fileCheckInfo.getCreateBeforeJobStartFlg());
			}
			if(fileCheckInfo.getDeleteValidFlg() != null){
				jobXML.setDeleteValidFlg((fileCheckInfo.getDeleteValidFlg()));
			}
			if(fileCheckInfo.getModifyValidFlg() != null){
				jobXML.setModifyValidFlg((fileCheckInfo.getModifyValidFlg()));
			}
			if(fileCheckInfo.getModifyType() != null){
				jobXML.setModifyType(OpenApiEnumConverter.enumToInteger(fileCheckInfo.getModifyType()));
			}
			if(fileCheckInfo.getNotJudgeFileInUseFlg() != null){
				jobXML.setNotJudgeFileInUseFlg(fileCheckInfo.getNotJudgeFileInUseFlg());
			}
			
			if(fileCheckInfo.getMessageRetryEndFlg() != null){
				jobXML.setMessageRetryEndFlg(fileCheckInfo.getMessageRetryEndFlg());
			}
			if(fileCheckInfo.getMessageRetry() != null){
				jobXML.setMessageRetry(fileCheckInfo.getMessageRetry());
			} else {
				// 未設定時はデフォルトの10を設定する。
				jobXML.setMessageRetry(10);
			}
			if(fileCheckInfo.getMessageRetryEndValue() != null){
				jobXML.setMessageRetryEndValue(fileCheckInfo.getMessageRetryEndValue());
			}
		}
		
		// リソース制御ジョブの場合
		JobResourceInfoResponse resourceInfo = jobMgr.getResource();
		if(resourceInfo != null){
			jobXML.setResourceCloudScopeId(resourceInfo.getResourceCloudScopeId());
			jobXML.setResourceLocationId(resourceInfo.getResourceLocationId());
			if(resourceInfo.getResourceType() != null){
				jobXML.setResourceType(OpenApiEnumConverter.enumToInteger(resourceInfo.getResourceType()));
			}
			if(resourceInfo.getResourceAction() != null){
				jobXML.setResourceAction(OpenApiEnumConverter.enumToInteger(resourceInfo.getResourceAction()));
			}
			switch (resourceInfo.getResourceType()) {
			case COMPUTE_FACILITY_ID:
				jobXML.setResourceTargetFacilityId(resourceInfo.getResourceTargetId());
				break;
			case COMPUTE_COMPUTE_ID:
				jobXML.setResourceTargetComputeId(resourceInfo.getResourceTargetId());
				break;
			case STORAGE:
				jobXML.setResourceTargetStorageId(resourceInfo.getResourceTargetId());
				break;
			}
			
			if(resourceInfo.getResourceStatusConfirmTime() != null){
				jobXML.setResourceStatusConfirmTime(resourceInfo.getResourceStatusConfirmTime());
			}
			if(resourceInfo.getResourceStatusConfirmInterval() != null){
				jobXML.setResourceStatusConfirmInterval(resourceInfo.getResourceStatusConfirmInterval());
			}
			jobXML.setResourceAttachNode(resourceInfo.getResourceAttachNode());
			jobXML.setResourceAttachDevice(resourceInfo.getResourceAttachDevice());
			jobXML.setResourceNotifyScope(resourceInfo.getResourceNotifyScope());
			if(resourceInfo.getResourceSuccessValue() != null){
				jobXML.setResourceSuccessEndValue((resourceInfo.getResourceSuccessValue()));
			}
			if(resourceInfo.getResourceFailureValue() != null){
				jobXML.setResourceFailureEndValue((resourceInfo.getResourceFailureValue()));
			}
		}

		//待ち条件,遅延監視,開始遅延
		JobWaitRuleInfoResponse ruleInfo = jobMgr.getWaitRule();
		if (ruleInfo != null) {
			if (jobMgr.getType() == JobInfoWrapper.TypeEnum.JOBUNIT
					|| jobMgr.getType() ==  JobInfoWrapper.TypeEnum.REFERJOB
					|| jobMgr.getType() ==  JobInfoWrapper.TypeEnum.REFERJOBNET) {
				if (jobMgr.getType() ==  JobInfoWrapper.TypeEnum.REFERJOB || jobMgr.getType() ==  JobInfoWrapper.TypeEnum.REFERJOBNET) {
					// 参照ジョブは制御タブの情報は必要な為、DTOの値を設定
					jobXML.setConditionType(OpenApiEnumConverter.enumToInteger(ruleInfo.getCondition()));
					jobXML.setUnmatchEndFlg(ruleInfo.getEndCondition());
					jobXML.setUnmatchEndStatus(OpenApiEnumConverter.enumToInteger(ruleInfo.getEndStatus()));
					jobXML.setUnmatchEndValue(ruleInfo.getEndValue());
					
					if (ruleInfo.getObjectGroup() != null && ruleInfo.getObjectGroup().size() != 0) {
						for(JobObjectGroupInfoResponse objectGroupRes : ruleInfo.getObjectGroup()){
							ObjectGroup objectGroup = new ObjectGroup();
							objectGroup.setOrderNo(objectGroupRes.getOrderNo());
							objectGroup.setConditionType(OpenApiEnumConverter.enumToInteger(objectGroupRes.getConditionType()));
							objectGroup.setObjectInfo(getXMLJobObject(objectGroupRes.getJobObjectList(), jobMgr.getId()));
							jobXML.addObjectGroup(objectGroup);
						}
					}
				} else {
					// ジョブユニットは不要な為、値を設定しない
				}
			} else {
				jobXML.setCalendar(ruleInfo.getCalendar());
				jobXML.setCalendarEndStatus(OpenApiEnumConverter.enumToInteger(ruleInfo.getCalendarEndStatus()));
				jobXML.setCalendarEndValue(ruleInfo.getCalendarEndValue());
				jobXML.setCalendarId(ruleInfo.getCalendarId());
				jobXML.setSuspend(ruleInfo.getSuspend());
				jobXML.setSkip(ruleInfo.getSkip());
				jobXML.setSkipEndStatus(OpenApiEnumConverter.enumToInteger(ruleInfo.getSkipEndStatus()));
				jobXML.setSkipEndValue(ruleInfo.getSkipEndValue());

				jobXML.setConditionType(OpenApiEnumConverter.enumToInteger(ruleInfo.getCondition()));
				jobXML.setUnmatchEndFlg(ruleInfo.getEndCondition());
				jobXML.setUnmatchEndStatus(OpenApiEnumConverter.enumToInteger(ruleInfo.getEndStatus()));
				jobXML.setUnmatchEndValue(ruleInfo.getEndValue());
				if (ruleInfo.getObjectGroup() != null && ruleInfo.getObjectGroup().size() != 0) {
					// orderNoで昇順ソートして出力する
					Collections.sort(ruleInfo.getObjectGroup(), Comparator.comparing(JobObjectGroupInfoResponse::getOrderNo));
					
					for(JobObjectGroupInfoResponse objectGroupRes : ruleInfo.getObjectGroup()){
						ObjectGroup objectGroup = new ObjectGroup();
						objectGroup.setOrderNo(objectGroupRes.getOrderNo());
						objectGroup.setConditionType(OpenApiEnumConverter.enumToInteger(objectGroupRes.getConditionType()));
						objectGroup.setObjectInfo(getXMLJobObject(objectGroupRes.getJobObjectList(), jobMgr.getId()));
						jobXML.addObjectGroup(objectGroup);
					}
				}

				jobXML.setStartDelay(ruleInfo.getStartDelay());
				jobXML.setStartDelayConditionType(OpenApiEnumConverter.enumToInteger(ruleInfo.getStartDelayConditionType()));
				jobXML.setStartDelayNotify(ruleInfo.getStartDelayNotify());
				jobXML.setStartDelayNotifyPriority(OpenApiEnumConverter.enumToInteger(ruleInfo.getStartDelayNotifyPriority()));
				jobXML.setStartDelayOperation(ruleInfo.getStartDelayOperation());
				jobXML.setStartDelayOperationEndStatus(OpenApiEnumConverter.enumToInteger(ruleInfo.getStartDelayOperationEndStatus()));
				jobXML.setStartDelayOperationEndValue(ruleInfo.getStartDelayOperationEndValue());
				jobXML.setStartDelayOperationType(OpenApiEnumConverter.enumToInteger(ruleInfo.getStartDelayOperationType()));
				jobXML.setStartDelaySession(ruleInfo.getStartDelaySession());
				jobXML.setStartDelaySessionValue(ruleInfo.getStartDelaySessionValue());
				jobXML.setStartDelayTime(ruleInfo.getStartDelayTime());
				if (ruleInfo.getStartDelayTimeValue() != null) {
					jobXML.setStartDelayTimeValue(ruleInfo.getStartDelayTimeValue());
				}

				jobXML.setEndDelay(ruleInfo.getEndDelay());
				jobXML.setEndDelayConditionType(OpenApiEnumConverter.enumToInteger(ruleInfo.getEndDelayConditionType()));
				jobXML.setEndDelayJob(ruleInfo.getEndDelayJob());
				jobXML.setEndDelayJobValue(ruleInfo.getEndDelayJobValue());
				jobXML.setEndDelayNotify(ruleInfo.getEndDelayNotify());
				jobXML.setEndDelayNotifyPriority(OpenApiEnumConverter.enumToInteger(ruleInfo.getEndDelayNotifyPriority()));
				jobXML.setEndDelayOperation(ruleInfo.getEndDelayOperation());
				jobXML.setEndDelayOperationEndStatus(OpenApiEnumConverter.enumToInteger(ruleInfo.getEndDelayOperationEndStatus()));
				jobXML.setEndDelayOperationEndValue(ruleInfo.getEndDelayOperationEndValue());
				jobXML.setEndDelayOperationType(OpenApiEnumConverter.enumToInteger(ruleInfo.getEndDelayOperationType()));
				jobXML.setEndDelaySession(ruleInfo.getEndDelaySession());
				jobXML.setEndDelaySessionValue(ruleInfo.getEndDelaySessionValue());
				jobXML.setEndDelayTime(ruleInfo.getEndDelayTime());
				if (ruleInfo.getEndDelayTimeValue() != null) {
					jobXML.setEndDelayTimeValue(ruleInfo.getEndDelayTimeValue());
				}
				jobXML.setEndDelayChangeMount(ruleInfo.getEndDelayChangeMount());
				jobXML.setEndDelayChangeMountValue(ruleInfo.getEndDelayChangeMountValue());

				if ((jobXML.getType() == JobConstant.TYPE_FILEJOB)
						|| (jobXML.getType() == JobConstant.TYPE_JOB)
						|| (jobXML.getType() == JobConstant.TYPE_FILECHECKJOB)
						|| (jobXML.getType() == JobConstant.TYPE_RPAJOB)) {
					jobXML.setMultiplicityNotify(ruleInfo.getMultiplicityNotify());
					jobXML.setMultiplicityNotifyPriority(OpenApiEnumConverter.enumToInteger(ruleInfo.getMultiplicityNotifyPriority()));
					jobXML.setMultiplicityOperation(OpenApiEnumConverter.enumToInteger(ruleInfo.getMultiplicityOperation()));
					jobXML.setMultiplicityEndValue(ruleInfo.getMultiplicityEndValue());
				}

				jobXML.setExclusiveBranch(ruleInfo.getExclusiveBranch());
				if (ruleInfo.getExclusiveBranch()) {
					jobXML.setExclusiveBranchEndStatus(OpenApiEnumConverter.enumToInteger(ruleInfo.getExclusiveBranchEndStatus()));
					jobXML.setExclusiveBranchEndValue(ruleInfo.getExclusiveBranchEndValue());
					List<ExclusiveJobValue> values = new ArrayList<>();
					int order = 0;
					for (JobNextJobOrderInfoResponse nextJob : ruleInfo.getExclusiveBranchNextJobOrderList()) {
						ExclusiveJobValue value = new ExclusiveJobValue();
						value.setNextJobId(nextJob.getNextJobId());
						value.setOrder(++order);
						values.add(value);
					}
					jobXML.setExclusiveJobValue(values.toArray(new ExclusiveJobValue[0]));
				}
				jobXML.setJobRetryFlg(ruleInfo.getJobRetryFlg());
				if (ruleInfo.getJobRetryEndStatus() == null) {
					jobXML.deleteJobRetryEndStatus();
				} else {
					jobXML.setJobRetryEndStatus(OpenApiEnumConverter.enumToInteger(ruleInfo.getJobRetryEndStatus()));
				}
				jobXML.setJobRetry(ruleInfo.getJobRetry() != null ? ruleInfo.getJobRetry() : 10);
				jobXML.setJobRetryInterval(ruleInfo.getJobRetryInterval() != null ? ruleInfo.getJobRetryInterval() : 0);
				jobXML.setQueueFlg(ruleInfo.getQueueFlg());
				jobXML.setQueueId(ruleInfo.getQueueId());
			}
		}
		
		//RPAシナリオジョブの場合
		JobRpaInfoResponse rpaInfo = jobMgr.getRpa();
		if (rpaInfo != null) {
			jobXML.setRpaJobType(OpenApiEnumConverter.enumToInteger(rpaInfo.getRpaJobType()));
			
			//直接実行
			jobXML.setRpaScenarioFacilityId(rpaInfo.getFacilityID());
			jobXML.setRpaProcessMode(OpenApiEnumConverter.enumToInteger(rpaInfo.getProcessingMethod()));
			jobXML.setRpaToolId(rpaInfo.getRpaToolId());
			jobXML.setRpaExeFilepath(rpaInfo.getRpaExeFilepath());
			jobXML.setRpaScenarioFilepath(rpaInfo.getRpaScenarioFilepath());
			jobXML.setRpaLogDirectory(rpaInfo.getRpaLogDirectory());
			jobXML.setRpaLogFileName(rpaInfo.getRpaLogFileName());
			jobXML.setRpaLogEncoding(rpaInfo.getRpaLogEncoding());
			jobXML.setRpaLogReturnCode(rpaInfo.getRpaLogReturnCode());
			jobXML.setRpaLogPatternHead(rpaInfo.getRpaLogPatternHead());
			jobXML.setRpaLogPatternTail(rpaInfo.getRpaLogPatternTail());
			if(rpaInfo.getRpaLogMaxBytes() != null){
				jobXML.setRpaLogMaxBytes(rpaInfo.getRpaLogMaxBytes());
			}
			if(rpaInfo.getRpaDefaultEndValue() != null){
				jobXML.setRpaDefaultEndValue(rpaInfo.getRpaDefaultEndValue());
			}
			if(rpaInfo.getRpaLoginFlg() != null){
				jobXML.setRpaLoginFlg(rpaInfo.getRpaLoginFlg());
			}
			jobXML.setRpaLoginUserId(rpaInfo.getRpaLoginUserId());
			jobXML.setRpaLoginPassword(rpaInfo.getRpaLoginPassword());
			if(rpaInfo.getRpaLoginRetry() != null){
				jobXML.setRpaLoginRetry(rpaInfo.getRpaLoginRetry());
			}
			if(rpaInfo.getRpaLoginEndValue() != null){
				jobXML.setRpaLoginEndValue(rpaInfo.getRpaLoginEndValue());
			}
			jobXML.setRpaLoginResolution(rpaInfo.getRpaLoginResolution());
			if(rpaInfo.getRpaLogoutFlg() != null){
				jobXML.setRpaLogoutFlg(rpaInfo.getRpaLogoutFlg());
			}
			if(rpaInfo.getRpaScreenshotEndDelayFlg() != null){
				jobXML.setRpaScreenshotEndDelayFlg(rpaInfo.getRpaScreenshotEndDelayFlg());
			}
			if(rpaInfo.getRpaScreenshotEndValueFlg() != null){
				jobXML.setRpaScreenshotEndValueFlg(rpaInfo.getRpaScreenshotEndValueFlg());
			}
			if(rpaInfo.getRpaScreenshotEndValue() != null){
				jobXML.setRpaScreenshotEndValue(rpaInfo.getRpaScreenshotEndValue());
			}
			if(rpaInfo.getRpaScreenshotEndValueCondition() != null){
				jobXML.setRpaScreenshotEndValueCondition(OpenApiEnumConverter.enumToInteger(rpaInfo.getRpaScreenshotEndValueCondition()));
			}
			if(rpaInfo.getMessageRetryEndFlg() != null){
				jobXML.setMessageRetryEndFlg(rpaInfo.getMessageRetryEndFlg());
			}
			if(rpaInfo.getMessageRetry() != null){
				jobXML.setMessageRetry(rpaInfo.getMessageRetry());
			} else {
				// 未設定時はデフォルトの10を設定する。
				jobXML.setMessageRetry(10);
			}
			if(rpaInfo.getMessageRetryEndValue() != null){
				jobXML.setMessageRetryEndValue(rpaInfo.getMessageRetryEndValue());
			}
			if(rpaInfo.getCommandRetryFlg() != null){
				jobXML.setCommandRetryFlg(rpaInfo.getCommandRetryFlg());
			}
			if(rpaInfo.getCommandRetry() != null){
				jobXML.setCommandRetry(rpaInfo.getCommandRetry());
			} else {
				// 未設定時はデフォルトの10を設定する。
				jobXML.setCommandRetry(10);
			}
			if(rpaInfo.getCommandRetryEndStatus() != null){
				jobXML.setCommandRetryEndStatus(OpenApiEnumConverter.enumToInteger(rpaInfo.getCommandRetryEndStatus()));
			}
			for(JobRpaOptionInfoResponse res : rpaInfo.getRpaJobOptionInfos()){
				RpaJobOptionInfos mst = new RpaJobOptionInfos();
				mst.setOrderNo(res.getOrderNo());
				mst.setDescription(res.getDescription());
				mst.setOption(res.getOption());
				jobXML.addRpaJobOptionInfos(mst);
			}
			for(JobRpaEndValueConditionInfoResponse res : rpaInfo.getRpaJobEndValueConditionInfos()){
				RpaJobEndValueConditionInfos mst = new RpaJobEndValueConditionInfos();
				mst.setOrderNo(res.getOrderNo());
				mst.setDescription(res.getDescription());
				mst.setConditionType(OpenApiEnumConverter.enumToInteger(res.getConditionType()));
				mst.setPattern(res.getPattern());
				if(res.getCaseSensitivityFlg()!=null){
					mst.setCaseSensitivityFlg(res.getCaseSensitivityFlg());
				}
				if(res.getProcessType()!=null){
					mst.setProcessType(res.getProcessType());
				}
				mst.setReturnCode(res.getReturnCode());
				if(res.getReturnCodeCondition()!=null){
					mst.setReturnCodeCondition(OpenApiEnumConverter.enumToInteger(res.getReturnCodeCondition()));
				}
				if(res.getUseCommandReturnCodeFlg()!=null){
					mst.setUseCommandReturnCodeFlg(res.getUseCommandReturnCodeFlg());
				}
				if(res.getEndValue() != null){
					mst.setEndValue(res.getEndValue());
				}
				jobXML.addRpaJobEndValueConditionInfos(mst);
			}
			if(rpaInfo.getRpaNotLoginNotify() != null){
				jobXML.setRpaNotLoginNotify(rpaInfo.getRpaNotLoginNotify());
			}
			if(rpaInfo.getRpaNotLoginNotifyPriority() != null){
				jobXML.setRpaNotLoginNotifyPriority(OpenApiEnumConverter.enumToInteger(rpaInfo.getRpaNotLoginNotifyPriority()));
			}
			if(rpaInfo.getRpaNotLoginEndValue() != null){
				jobXML.setRpaNotLoginEndValue(rpaInfo.getRpaNotLoginEndValue());
			}
			if(rpaInfo.getRpaAlreadyRunningNotify() != null){
				jobXML.setRpaAlreadyRunningNotify(rpaInfo.getRpaAlreadyRunningNotify());
			}
			if(rpaInfo.getRpaAlreadyRunningNotifyPriority() != null){
				jobXML.setRpaAlreadyRunningNotifyPriority(OpenApiEnumConverter.enumToInteger(rpaInfo.getRpaAlreadyRunningNotifyPriority()));
			}
			if(rpaInfo.getRpaAlreadyRunningEndValue() != null){
				jobXML.setRpaAlreadyRunningEndValue(rpaInfo.getRpaAlreadyRunningEndValue());
			}
			if(rpaInfo.getRpaAbnormalExitNotify() != null){
				jobXML.setRpaAbnormalExitNotify(rpaInfo.getRpaAbnormalExitNotify());
			}
			if(rpaInfo.getRpaAbnormalExitNotifyPriority() != null){
				jobXML.setRpaAbnormalExitNotifyPriority(OpenApiEnumConverter.enumToInteger(rpaInfo.getRpaAbnormalExitNotifyPriority()));
			}
			if(rpaInfo.getRpaAbnormalExitEndValue() != null){
				jobXML.setRpaAbnormalExitEndValue(rpaInfo.getRpaAbnormalExitEndValue());
			}
			
			// 間接実行
			jobXML.setRpaScopeId(rpaInfo.getRpaScopeId());
			if(rpaInfo.getRpaRunType() != null){
				jobXML.setRpaRunType(rpaInfo.getRpaRunType());
			}
			jobXML.setRpaScenarioParam(rpaInfo.getRpaScenarioParam());
			if(rpaInfo.getRpaStopMode() != null){
				jobXML.setRpaStopMode(rpaInfo.getRpaStopMode());
			}
			for(JobRpaRunParamInfoResponse res : rpaInfo.getRpaJobRunParamInfos()){
				RpaJobRunParamInfos mst = new RpaJobRunParamInfos();
				mst.setParamId(res.getParamId());
				mst.setParamValue(res.getParamValue());
				jobXML.addRpaJobRunParamInfos(mst);;
			}
			for(JobRpaCheckEndValueInfoResponse res : rpaInfo.getRpaJobCheckEndValueInfos()){
				RpaJobCheckEndValueInfos mst = new RpaJobCheckEndValueInfos();
				mst.setEndStatusId(res.getEndStatusId());
				mst.setEndValue(res.getEndValue());
				jobXML.addRpaJobCheckEndValueInfos(mst);
			}
			jobXML.setRpaStopType(OpenApiEnumConverter.enumToInteger(rpaInfo.getRpaStopType()));
			if(rpaInfo.getRpaRunConnectTimeout() != null){
				jobXML.setRpaRunConnectTimeout(rpaInfo.getRpaRunConnectTimeout());
			}
			if(rpaInfo.getRpaRunRequestTimeout() != null){
				jobXML.setRpaRunRequestTimeout(rpaInfo.getRpaRunRequestTimeout());
			}
			if(rpaInfo.getRpaRunEndFlg() != null){
				jobXML.setRpaRunEndFlg(rpaInfo.getRpaRunEndFlg());
			}
			if(rpaInfo.getRpaRunRetry() != null){
				jobXML.setRpaRunRetry(rpaInfo.getRpaRunRetry());
			}
			if(rpaInfo.getRpaRunEndValue() != null){
				jobXML.setRpaRunEndValue(rpaInfo.getRpaRunEndValue());
			}
			if(rpaInfo.getRpaCheckConnectTimeout() != null){
				jobXML.setRpaCheckConnectTimeout(rpaInfo.getRpaCheckConnectTimeout());
			}
			if(rpaInfo.getRpaCheckRequestTimeout() != null){
				jobXML.setRpaCheckRequestTimeout(rpaInfo.getRpaCheckRequestTimeout());
			}
			if(rpaInfo.getRpaCheckEndFlg() != null){
				jobXML.setRpaCheckEndFlg(rpaInfo.getRpaCheckEndFlg());
			}
			if(rpaInfo.getRpaCheckRetry() != null){
				jobXML.setRpaCheckRetry(rpaInfo.getRpaCheckRetry());
			}
			if(rpaInfo.getRpaCheckEndValue() != null){
				jobXML.setRpaCheckEndValue(rpaInfo.getRpaCheckEndValue());
			}
		}

		if (jobMgr.getReferJobId() != null) {
			jobXML.setReferJobId(jobMgr.getReferJobId());
		}
		if (jobMgr.getReferJobUnitId() != null) {
			jobXML.setReferJobunitId(jobMgr.getReferJobUnitId());
		}
		if (jobMgr.getType() ==  JobInfoWrapper.TypeEnum.REFERJOB || jobMgr.getType() ==  JobInfoWrapper.TypeEnum.REFERJOBNET){
			jobXML.setReferJobSelectType(OpenApiEnumConverter.enumToInteger(jobMgr.getReferJobSelectType()));
		}
		if (jobMgr.getParam() != null && jobMgr.getParam().size() != 0) {
			jobXML.setParam(getXMLParam(jobMgr));
		}

		// 通知先指定
		setXMLNotices(jobMgr, jobXML);
		if (jobMgr.getType() !=  JobInfoWrapper.TypeEnum.REFERJOB && jobMgr.getType() !=  JobInfoWrapper.TypeEnum.REFERJOBNET) {
			jobXML.setBeginPriority(OpenApiEnumConverter.enumToInteger(jobMgr.getBeginPriority()));
			jobXML.setNormalPriority(OpenApiEnumConverter.enumToInteger(jobMgr.getNormalPriority()));
			jobXML.setWarnPriority(OpenApiEnumConverter.enumToInteger(jobMgr.getWarnPriority()));
			jobXML.setAbnormalPriority(OpenApiEnumConverter.enumToInteger(jobMgr.getAbnormalPriority()));
		}
		return jobXML;
	}

//	/**
//	 * 終了値と終了状態のマップ（EndStatus）をDTOからXMLに変換します。
//	 * @param jobMgr　Hinemos DTO
//	 * @return XML Bean
//	 */
//	public static EndStatus[] getXMLEndStatus(JobInfo jobMgr)
//	{
//		com.clustercontrol.utility.settings.job.xml.EndStatus[] ret =
//			new com.clustercontrol.utility.settings.job.xml.EndStatus[3];
//
//		if(jobMgr.getEndStatus() != null && jobMgr.getEndStatus().size() != 0){
//			for(int i = 0; i <3 ; i++){
//				ret[i] = new com.clustercontrol.utility.settings.job.xml.EndStatus();
//
//				for(JobEndStatusInfo info : jobMgr.getEndStatus()){
//					if(i == info.getType()){
//						ret[i].setType(info.getType());
//						ret[i].setValue(info.getValue());
//						ret[i].setStartRangeValue(info.getStartRangeValue());
//						ret[i].setEndRangeValue(info.getEndRangeValue());
//					}
//				}
//			}
//		}
//		else{
//			//終了状態が取得できない場合、差分比較で差異が出ないようにExcelと合わせて空の枠のみ出力する。
//			for(int i = 0; i <3 ; i++){
//				ret[i] = new com.clustercontrol.utility.settings.job.xml.EndStatus();
//				ret[i].setType(i);
//			}
//		}
//
//		return (EndStatus[]) ret;
//	}

	/**
	 * 通知情報をDTOからXMLのBeanに変換します。
	 * @param jobMgr　コマンドのDTO
	 * @return　コマンドのXML Bean
	 */
	private static void setXMLNotices(JobInfoWrapper jobMgr, com.clustercontrol.utility.settings.job.xml.JobInfo jobXML)
	{

		/*
		 * すごくわかりづらいですが、
		 * XMLでは、
		 *
		 * Notices       → ジョブの終了状態から通知の重要度に変換
		 * Notification → 通知先の設定になります。
		 *
		 * 対してDTOは
		 * 両者は一緒です。
		 */

		//MGRのDTOを取得
		//notificationを初期化
		List<com.clustercontrol.utility.settings.job.xml.NotifyRelationInfos> notifyList = null;
		if(jobMgr.getNotifyRelationInfos() != null && jobMgr.getNotifyRelationInfos().size() != 0) {
			notifyList = new ArrayList<>();
			for(NotifyRelationInfoResponse info : jobMgr.getNotifyRelationInfos()) {
				com.clustercontrol.utility.settings.job.xml.NotifyRelationInfos notify
					= new com.clustercontrol.utility.settings.job.xml.NotifyRelationInfos();

				notify.setNotifyId(info.getNotifyId());
				notify.setNotifyType(OpenApiEnumConverter.enumToInteger(info.getNotifyType()));

				notifyList.add(notify);
			}
		}
		if(notifyList != null){
			jobXML.setNotifyRelationInfos(notifyList.toArray(new com.clustercontrol.utility.settings.job.xml.NotifyRelationInfos[0]));
		}
	}

	/**
	 * 待ち条件判定対象一覧のXMLオブジェクトを返します.
	 * @param objectMgr
	 * @param jobId
	 * @return
	 */
	private static ObjectInfo[] getXMLJobObject(List<JobObjectInfoResponse> objectMgr, String jobId) {
		List<ObjectInfo> ret = new ArrayList<ObjectInfo>();

		for(JobObjectInfoResponse info : objectMgr){
			ObjectInfo objectInfo = new ObjectInfo();

			// Set common part
			objectInfo.setType(OpenApiEnumConverter.enumToInteger(info.getType()));
			objectInfo.setJobId("");
			objectInfo.setTime("");
			objectInfo.setStatus(0);
			objectInfo.setDescription(info.getDescription());
			objectInfo.setDecisionValue("");
			objectInfo.setValue("");
			// startJob.setStartDecisionCondition(0);未設定

			switch(info.getType()){
				case JOB_END_STATUS:
					objectInfo.setJobId(info.getJobId());
					objectInfo.setStatus(OpenApiEnumConverter.enumToInteger(info.getStatus()));
					break;
				case JOB_END_VALUE:
					objectInfo.setJobId(info.getJobId());
					objectInfo.setNumberDecisionCondition(OpenApiEnumConverter.enumToInteger(info.getDecisionCondition()));
					objectInfo.setValue(info.getValue());
					break;
				case TIME:
					try {
						objectInfo.setTime(info.getTime());
					} catch (Exception e) {
						log.error(e);
					}
					break;
				case START_MINUTE:
					objectInfo.setStartMinute(info.getStartMinute());
					break;
				case JOB_PARAMETER:
					objectInfo.setDecisionValue(info.getDecisionValue());
					objectInfo.setValue(info.getValue());
					objectInfo.setDecisionCondition(OpenApiEnumConverter.enumToInteger(info.getDecisionCondition()));
					break;
				case CROSS_SESSION_JOB_END_STATUS:
					objectInfo.setCrossSessionRange(info.getCrossSessionRange());
					objectInfo.setJobId(info.getJobId());
					objectInfo.setStatus(OpenApiEnumConverter.enumToInteger(info.getStatus()));
					break;
				case CROSS_SESSION_JOB_END_VALUE:
					objectInfo.setCrossSessionRange(info.getCrossSessionRange());
					objectInfo.setJobId(info.getJobId());
					objectInfo.setValue(info.getValue());
					objectInfo.setNumberDecisionCondition(OpenApiEnumConverter.enumToInteger(info.getDecisionCondition()));
					break;
				case JOB_RETURN_VALUE:
					objectInfo.setJobId(info.getJobId());
					objectInfo.setValue(info.getValue());
					objectInfo.setNumberDecisionCondition(OpenApiEnumConverter.enumToInteger(info.getDecisionCondition()));
					break;
				default:
					String msg = "Unknown start job type : " + info.getType();
					log.error(msg);
					throw new IllegalArgumentException(msg);
			}
			ret.add(objectInfo);
		}

		return ret.toArray(new ObjectInfo[0]);
	}

	/**
	 * ジョブパラメータをDTOからXMLのBeanに変換します。
	 * @param jobMgr　コマンドのDTO
	 * @return　コマンドのXML Bean
	 */
	private static com.clustercontrol.utility.settings.job.xml.Param[] getXMLParam(JobInfoWrapper jobMgr) {
		Param[] ret = new com.clustercontrol.utility.settings.job.xml.Param[jobMgr.getParam().size()];

		for (int i=0 ; i< jobMgr.getParam().size(); i++) {
			ret[i] = new com.clustercontrol.utility.settings.job.xml.Param();
			if(jobMgr.getParam().get(i).getParamId() != null)
				ret[i].setParamId(jobMgr.getParam().get(i).getParamId());
			if(jobMgr.getParam().get(i).getDescription() != null)
				ret[i].setDescription(jobMgr.getParam().get(i).getDescription());

			ret[i].setParamType(OpenApiEnumConverter.enumToInteger(jobMgr.getParam().get(i).getType()));
			if(jobMgr.getParam().get(i).getValue() != null)
				ret[i].setValue(jobMgr.getParam().get(i).getValue());
		}

		return ret;
	}
	
	public static void sort(ExclusiveJobValue[] objects) {
		try {
			Arrays.sort(
				objects,
				new Comparator<ExclusiveJobValue>() {
					@Override
					public int compare(ExclusiveJobValue obj1, ExclusiveJobValue obj2) {
						return obj1.getOrder() - obj2.getOrder();
					}
				});
		}
		catch (Exception e) {
		}
	}
}
