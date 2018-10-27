/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.job.conv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.job.xml.CommandParam;
import com.clustercontrol.utility.settings.job.xml.EnvVariableInfo;
import com.clustercontrol.utility.settings.job.xml.ExclusiveJobValue;
import com.clustercontrol.utility.settings.job.xml.NotifyRelationInfo;
import com.clustercontrol.utility.settings.job.xml.Param;
import com.clustercontrol.utility.settings.job.xml.StartJob;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.DateUtil;
import com.clustercontrol.ws.jobmanagement.JobCommandInfo;
import com.clustercontrol.ws.jobmanagement.JobCommandParam;
import com.clustercontrol.ws.jobmanagement.JobEndStatusInfo;
import com.clustercontrol.ws.jobmanagement.JobEnvVariableInfo;
import com.clustercontrol.ws.jobmanagement.JobFileInfo;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobNextJobOrderInfo;
import com.clustercontrol.ws.jobmanagement.JobObjectInfo;
import com.clustercontrol.ws.jobmanagement.JobParameterInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;
import com.clustercontrol.ws.jobmanagement.MonitorJobInfo;

/**
 * ジョブの定義情報をXMLのBeanとHinemosとDTOとで変換します。<BR>
 *
 * @version 6.1.0
 * @since 1.1.0
 */
public class MasterConv {
	// ロガー
	private final static Log logger = LogFactory.getLog(MasterConv.class);
	private static Log log = LogFactory.getLog(MasterConv.class);

	// 対応スキーマバージョン
	private static final String schemaType = "I";
	private static final String schemaVersion = "1";
	private static final String schemaRevision = "1";

	// 日付フォーマッタ
	private static final String DATA_FORMAT = "yyyy-MM-dd HH:mm:ss";


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
	public static JobTreeItem masterXml2Dto(com.clustercontrol.utility.settings.job.xml.JobInfo[] jobMastersXML,
		List<String> jobunitList) throws ParseException {

		JobTreeItem top = new JobTreeItem();
//		JobTreeItem job = new JobTreeItem();
//		JobInfo topJobInfo = new JobInfo();
//		topJobInfo.setJobunitId("");
//		topJobInfo.setId("");
//		topJobInfo.setName("");
//		topJobInfo.setType(-1);
//		top.setData(topJobInfo);

		for (int i = 0; i < jobMastersXML.length; i++) {

			if (jobMastersXML[i].getParentJobId().equals(TOP_JOB_ID)) {
				for (String jobunitId : jobunitList) {
					if (jobunitId.equals(jobMastersXML[i].getJobunitId())) {

						// ツリーアイテムの生成と中身のジョブ情報のセット
						JobTreeItem jti = new JobTreeItem();
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

	private static void masterXml2Dto(JobTreeItem parent,
			com.clustercontrol.utility.settings.job.xml.JobInfo[] jobMastersXML) throws ParseException {

		for (int i = 0; i < jobMastersXML.length; i++) {

			// 子供のjobを探します。
			if (jobMastersXML[i].getParentJobId().equals(parent.getData().getId())
					&& jobMastersXML[i].getParentJobunitId().equals(parent.getData().getJobunitId())) {

				// ツリーアイテムの生成と中身のジョブ情報のセット
				JobTreeItem jti = new JobTreeItem();
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
	private static JobInfo setDTOJobData(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML)
	throws ParseException {
		//投入用データの作成
		JobInfo info= null;

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
						) {

					//コンストラクターには、IDとNameとTypeが必要
					info = new JobInfo();
					info.setJobunitId(jobMasterXML.getJobunitId());
					info.setId(jobMasterXML.getId());
					info.setName(jobMasterXML.getName());
					info.setType(jobMasterXML.getType());
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
		if (info.getType() == JobConstant.TYPE_JOB) {
			info.setCommand(getDTOCommand(jobMasterXML));
		}

		// 終了状態
		info.getEndStatus().addAll(getDTOEndStatus(jobMasterXML));

		// ファイル転送ジョブ
		if (info.getType() == JobConstant.TYPE_FILEJOB) {
			info.setFile(getDTOFile(jobMasterXML));
		}

		// モジュール登録
		info.setRegisteredModule(jobMasterXML.getRegisteredModule());
		// アイコンID
		info.setIconId(jobMasterXML.getIconId());

		// 承認ジョブ
		if (info.getType() == JobConstant.TYPE_APPROVALJOB) {
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
			info.setUseApprovalReqSentence(jobMasterXML.getUseApprovalReqSentence());
		}

		// 監視の場合
		if (info.getType().equals(JobConstant.TYPE_MONITORJOB)) {
			info.setMonitor(new MonitorJobInfo());
			//ファシリティID
			info.getMonitor().setFacilityID(jobMasterXML.getFacilityId());
			//スコープ処理
			info.getMonitor().setProcessingMethod(jobMasterXML.getProcessMode());
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

		// 待ち条件
		if (jobMasterXML.getStartJob() != null) {
			info.setWaitRule(getDTOWaitRule(jobMasterXML));
		}

		// ジョブ変数一覧
		if (jobMasterXML.getParam() != null) {
			info.getParam().addAll(getDTOParam(jobMasterXML));
		}

		if (jobMasterXML.getType() != JobConstant.TYPE_REFERJOB
				&& jobMasterXML.getType() != JobConstant.TYPE_REFERJOBNET){
			info.setBeginPriority(jobMasterXML.getBeginPriority());
			info.setAbnormalPriority(jobMasterXML.getAbnormalPriority());
			info.setWarnPriority(jobMasterXML.getWarnPriority());
			info.setNormalPriority(jobMasterXML.getNormalPriority());
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
			info.setReferJobSelectType(jobMasterXML.getReferJobSelectType());
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(DATA_FORMAT);
		info.setUpdateTime(dateFormat.parse(jobMasterXML.getUpdateTime()).getTime());



		return info;
	}

	/**
	 * コマンドジョブに関するDTOを返します.
	 * @param jobMasterXML
	 * @return
	 */
	private static JobCommandInfo getDTOCommand(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML){
		JobCommandInfo ret = new JobCommandInfo();

		ret.setFacilityID(jobMasterXML.getFacilityId());
		ret.setProcessingMethod(jobMasterXML.getProcessMode());
		ret.setStartCommand(jobMasterXML.getStartCommand());
		ret.setStopType(jobMasterXML.getStopType());
		ret.setStopCommand(jobMasterXML.getStopCommand());
		ret.setSpecifyUser(jobMasterXML.getSpecifyUser());
		if(jobMasterXML.getEffectiveUser() != null && !jobMasterXML.getEffectiveUser().equals("")){
			ret.setUser(jobMasterXML.getEffectiveUser());
		}else{
			log.info("User not found " + jobMasterXML.getId());
		}
		ret.setMessageRetryEndFlg(jobMasterXML.getMessageRetryEndFlg());
		ret.setMessageRetry(jobMasterXML.getMessageRetry());
		ret.setMessageRetryEndValue(jobMasterXML.getMessageRetryEndValue());

		ret.setCommandRetryFlg(jobMasterXML.getCommandRetryFlg());
		ret.setCommandRetryEndStatus(jobMasterXML.hasCommandRetryEndStatus() ? jobMasterXML.getCommandRetryEndStatus() : EndStatusConstant.INITIAL_VALUE_NORMAL);

		//未設定時はデフォルトの10を設定する。
		ret.setCommandRetry(jobMasterXML.hasCommandRetry() ? jobMasterXML.getCommandRetry() : 10);

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
			JobCommandParam e = new JobCommandParam();
			e.setJobStandardOutputFlg(cp.getJobStandardOutputFlg());
			e.setParamId(cp.getParamId());
			e.setValue(cp.getValue());
			ret.getJobCommandParamList().add(e);
		}
		// コマンドジョブ環境変数
		for (EnvVariableInfo env : jobMasterXML.getEnvVariableInfo()) {
			JobEnvVariableInfo e = new JobEnvVariableInfo();
			e.setEnvVariableId(env.getEnvVariableId());
			e.setValue(env.getValue());
			e.setDescription(env.getDescription());
			ret.getEnvVariableInfo().add(e);
		}

		return ret;
	}

	/**
	 * jobMasterXMLから終了状態を取得して返します.
	 * @param jobMasterXML
	 * @return 終了状態のリスト
	 */
	private static List<JobEndStatusInfo> getDTOEndStatus(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML){
		List<JobEndStatusInfo> ret = new ArrayList<JobEndStatusInfo>();

		// 終了状態(正常)
		JobEndStatusInfo endInfo = new JobEndStatusInfo();
		endInfo.setType(0);
		endInfo.setValue(jobMasterXML.getNormalEndValue());
		endInfo.setStartRangeValue(jobMasterXML.getNormalEndValueFrom());
		endInfo.setEndRangeValue(jobMasterXML.getNormalEndValueTo());
		ret.add(endInfo);

		// 終了状態(警告)
		endInfo = new JobEndStatusInfo();
		endInfo.setType(1);
		endInfo.setValue(jobMasterXML.getWarnEndValue());
		endInfo.setStartRangeValue(jobMasterXML.getWarnEndValueFrom());
		endInfo.setEndRangeValue(jobMasterXML.getWarnEndValueTo());
		ret.add(endInfo);

		// 終了状態(異常)
		endInfo = new JobEndStatusInfo();
		endInfo.setType(2);
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
	private static JobFileInfo getDTOFile(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML){
		JobFileInfo ret = new  JobFileInfo();

		// 転送元ファシリティID
		ret.setSrcFacilityID(jobMasterXML.getSrcFacilityId());
		// 転送ファイル
		ret.setSrcFile(jobMasterXML.getSrcFile());
		// 送信先ファシリティID
		ret.setDestFacilityID(jobMasterXML.getDestFacilityId());
		// 送信先ディレクトリ
		ret.setDestDirectory(jobMasterXML.getDestDirectory());
		// 処理方式
		ret.setProcessingMethod(jobMasterXML.getProcessMode());
		// ファイル転送時に圧縮するかのフラグ
		ret.setCompressionFlg(jobMasterXML.getCompressionFlg());
		// 転送ファイルのチェックを行うかのフラグ
		ret.setCheckFlg(jobMasterXML.getCheckFlg());
		// 実行ユーザーを指定するかのフラグ
		ret.setSpecifyUser(jobMasterXML.getSpecifyUser());
		// 実行ユーザー名
		ret.setUser(jobMasterXML.getEffectiveUser());
		// エージェントに接続できない時に終了するかのフラグ
		ret.setMessageRetryEndFlg(jobMasterXML.getMessageRetryEndFlg());
		// エージェントに接続できない時の試行回数
		ret.setMessageRetry(jobMasterXML.getMessageRetry());
		// エージェントに接続できない時に終了する時の終了値
		// Import with constant otherwise it will be set to "0"
		ret.setMessageRetryEndValue(jobMasterXML.hasMessageRetryEndValue() ? jobMasterXML.getMessageRetryEndValue() : 0);

		// ジョブが正常終了するまでコマンドを繰り返すかのフラグ
		ret.setCommandRetryFlg(jobMasterXML.getCommandRetryFlg());
		// ジョブの繰り返し実行時の終了状態//未設定時はデフォルトの正常状態を設定する。
		ret.setCommandRetryEndStatus(jobMasterXML.hasCommandRetryEndStatus() ? jobMasterXML.getCommandRetryEndStatus() : EndStatusConstant.INITIAL_VALUE_NORMAL);
		// ジョブが指定された終了状態になるまでコマンドを繰り返す時の試行回数//未設定時はデフォルトの10を設定する。
		ret.setCommandRetry(jobMasterXML.hasCommandRetry() ? jobMasterXML.getCommandRetry() : 10);

		return ret;
	}

	/**
	 * jobMasterXMLから終了遅延情報を返します.
	 * @param jobMasterXML
	 * @return 終了遅延情報を格納するクラスの参照
	 */
	private static JobWaitRuleInfo getDTOWaitRule(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) throws ParseException {
		JobWaitRuleInfo ret = new JobWaitRuleInfo();

		// カレンダーフラグ
		ret.setCalendar(jobMasterXML.getCalendar());
		// カレンダーの終了状態
		ret.setCalendarEndStatus(jobMasterXML.getCalendarEndStatus());
		// カレンダーの終了値
		ret.setCalendarEndValue(jobMasterXML.getCalendarEndValue());
		// カレンダーID
		ret.setCalendarId(jobMasterXML.getCalendarId());
		// 判定対象の条件関係
		ret.setCondition(jobMasterXML.getConditionType());
		// 終了遅延有無
		ret.setEndDelay(jobMasterXML.getEndDelay());
		// 終了遅延判定対象の条件関係
		ret.setEndDelayConditionType(jobMasterXML.getEndDelayConditionType());
		// ジョブ開始後の経過時間監視有無
		ret.setEndDelayJob(jobMasterXML.getEndDelayJob());
		// ジョブ開始後の経過時間(分)
		ret.setEndDelayJobValue(jobMasterXML.getEndDelayJobValue());
		// 終了遅延通知有無
		ret.setEndDelayNotify(jobMasterXML.getEndDelayNotify());
		// 終了遅延通知重要度
		ret.setEndDelayNotifyPriority(jobMasterXML.getEndDelayNotifyPriority());
		// 終了遅延操作有無
		ret.setEndDelayOperation(jobMasterXML.getEndDelayOperation());
		// 終了遅延操作終了状態(0:正常,1:警告,2:異常)
		ret.setEndDelayOperationEndStatus(jobMasterXML.getEndDelayOperationEndStatus());
		// 終了遅延操作終了値
		ret.setEndDelayOperationEndValue(jobMasterXML.getEndDelayOperationEndValue());
		// 終了遅延操作種別（0:停止[コマンド], 2:停止[中断], 10:停止[状態指定]）
		ret.setEndDelayOperationType(jobMasterXML.getEndDelayOperationType());
		// セッション開始後の経過時間使用有無
		ret.setEndDelaySession(jobMasterXML.getEndDelaySession());
		// セッション開始後の経過時間（分）
		ret.setEndDelaySessionValue(jobMasterXML.getEndDelaySessionValue());
		// 終了遅延監視時刻監視有無
		ret.setEndDelayTime(jobMasterXML.getEndDelayTime());
		// 終了監視遅延時刻の値
		if (jobMasterXML.getEndDelayTimeValue() != null && !jobMasterXML.getEndDelayTimeValue().isEmpty()) {
			try {
				long timeWk = DateUtil.convTimeString2Epoch(jobMasterXML.getEndDelayTimeValue());
				ret.setEndDelayTimeValue(timeWk);
			} catch (ParseException e) {
				logger.error(e);
				throw e;
			}
		}
		// 実行履歴からの変化量使用有無
		ret.setEndDelayChangeMount(jobMasterXML.getEndDelayChangeMount());
		// 実行履歴からの変化量
		ret.setEndDelayChangeMountValue(jobMasterXML.getEndDelayChangeMountValue());

		// 終了状態
		ret.setEndStatus(jobMasterXML.getUnmatchEndStatus());
		// 終了値
		ret.setEndValue(jobMasterXML.getUnmatchEndValue());
		// 条件を満たさなければ終了する
		ret.setEndCondition(jobMasterXML.getUnmatchEndFlg());
		// スキップ
		ret.setSkip(jobMasterXML.getSkip());
		// スキップ終了状態
		ret.setSkipEndStatus(jobMasterXML.getSkipEndStatus());
		// スキップ終了値
		ret.setSkipEndValue(jobMasterXML.getSkipEndValue());
		// 開始遅延有無
		ret.setStartDelay(jobMasterXML.getStartDelay());
		// 開始遅延判定対象の条件関係
		ret.setStartDelayConditionType(jobMasterXML.getStartDelayConditionType());
		// 開始遅延通知有無
		ret.setStartDelayNotify(jobMasterXML.getStartDelayNotify());
		// 開始遅延通知重要度
		ret.setStartDelayNotifyPriority(jobMasterXML.getStartDelayNotifyPriority());
		// 開始遅延操作有無
		ret.setStartDelayOperation(jobMasterXML.getStartDelayOperation());
		// 開始遅延操作終了状態
		ret.setStartDelayOperationEndStatus(jobMasterXML.getStartDelayOperationEndStatus());
		// 開始遅延操作終了値
		ret.setStartDelayOperationEndValue(jobMasterXML.getStartDelayOperationEndValue());
		// 開始遅延操作種別
		ret.setStartDelayOperationType(jobMasterXML.getStartDelayOperationType());
		// セッション開始後からの開始遅延有無
		ret.setStartDelaySession(jobMasterXML.getStartDelaySession());
		// 開始遅延セッション開始後の時間の値
		ret.setStartDelaySessionValue(jobMasterXML.getStartDelaySessionValue());
		// 開始遅延時刻有無
		ret.setStartDelayTime(jobMasterXML.getStartDelayTime());
		// セッション開始後の経過時間（分）
		if (jobMasterXML.getStartDelayTimeValue() != null && !jobMasterXML.getStartDelayTimeValue().isEmpty()) {
			try {
				long timeWk = DateUtil.convTimeString2Epoch(jobMasterXML.getStartDelayTimeValue());
				ret.setStartDelayTimeValue(timeWk);
			} catch (ParseException e) {
				logger.error(e);
				throw e;
			}
		}
		// 開始時保留
		ret.setSuspend(jobMasterXML.getSuspend());

		if ((jobMasterXML.getType() == JobConstant.TYPE_FILEJOB)
			|| (jobMasterXML.getType() == JobConstant.TYPE_JOB)) {
			// 多重度が上限に達した時に通知する
			ret.setMultiplicityNotify(jobMasterXML.getMultiplicityNotify());
			// 通知重要度
			ret.setMultiplicityNotifyPriority(jobMasterXML.getMultiplicityNotifyPriority());
			// 操作
			ret.setMultiplicityOperation(jobMasterXML.getMultiplicityOperation());
			// 操作終了値
			ret.setMultiplicityEndValue(jobMasterXML.getMultiplicityEndValue());
		}

		// 判定対象一覧
		ret.getObject().addAll(getDTOJobObjectInfo(jobMasterXML.getStartJob()));
		//ret.getObject().addAll(getDTOJobObjectInfo(jobMasterXML));

		//排他分岐
		ret.setExclusiveBranch(jobMasterXML.getExclusiveBranch());
		//実行されなかったジョブの終了状態
		ret.setExclusiveBranchEndStatus(jobMasterXML.getExclusiveBranchEndStatus());
		//実行されなかったジョブの終了値
		ret.setExclusiveBranchEndValue(jobMasterXML.getExclusiveBranchEndValue());

		ExclusiveJobValue[] exclusiveJobValues = jobMasterXML.getExclusiveJobValue();
		sort(exclusiveJobValues);
		for (ExclusiveJobValue oder : exclusiveJobValues) {
			JobNextJobOrderInfo nextJob = new JobNextJobOrderInfo();
			nextJob.setJobunitId(jobMasterXML.getJobunitId());
			nextJob.setJobId(jobMasterXML.getId());
			nextJob.setNextJobId(oder.getNextJobId());
			ret.getExclusiveBranchNextJobOrderList().add(nextJob);
		}

		//繰り返し実行
		ret.setJobRetryFlg(jobMasterXML.getJobRetryFlg());
		ret.setJobRetryEndStatus(jobMasterXML.hasJobRetryEndStatus() ? jobMasterXML.getJobRetryEndStatus() : EndStatusConstant.INITIAL_VALUE_NORMAL);
		ret.setJobRetry(jobMasterXML.hasJobRetry() ? jobMasterXML.getJobRetry() : 10);

		return ret;
	}

	/**
	 * 判定対象一覧のオブジェクトを返します.
	 * @param masterXML
	 * @return
	 * @throws ParseException
	 */
	private static List<JobObjectInfo> getDTOJobObjectInfo(com.clustercontrol.utility.settings.job.xml.JobStartJobInfo[] objectXML) throws ParseException {
		List<JobObjectInfo> ret = new ArrayList<JobObjectInfo>();

		for (com.clustercontrol.utility.settings.job.xml.JobStartJobInfo one : objectXML) {
			JobObjectInfo infoBean = new JobObjectInfo();

			// Common part
			infoBean.setType(one.getTargetJobType());
			infoBean.setJobId(one.getTargetJobId());
			infoBean.setDescription(one.getTargetJobDescription());

			switch (one.getTargetJobType()) {
			case JudgmentObjectConstant.TYPE_JOB_END_STATUS:
			case JudgmentObjectConstant.TYPE_JOB_END_VALUE:
				infoBean.setValue(one.getTargetJobEndValue());
				break;
			case JudgmentObjectConstant.TYPE_TIME:
				long timeWk = 0;
				try {
					timeWk = DateUtil.convTimeString2Epoch(one.getStartTime());
				} catch (ParseException e) {
					logger.error(e);
					continue;
				}
				infoBean.setTime(timeWk);
				break;
			case JudgmentObjectConstant.TYPE_START_MINUTE:
				infoBean.setStartMinute(one.getTargetJobEndValue());
				break;

			case JudgmentObjectConstant.TYPE_JOB_PARAMETER:
				infoBean.setDecisionValue01(one.getStartDecisionValue01());
				infoBean.setDecisionValue02(one.getStartDecisionValue02());
				infoBean.setDecisionCondition(one.getStartDecisionCondition());
				break;
			case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS:
				infoBean.setCrossSessionRange(one.getTargetCrossSessionRange());
				infoBean.setValue(one.getTargetJobEndValue());
				break;
			case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE:
				infoBean.setCrossSessionRange(one.getTargetCrossSessionRange());
				infoBean.setValue(one.getTargetJobEndValue());
				break;
			default:
				String msg = "Unknown start job type : " + one.getTargetJobType();
				logger.error(msg);
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
	private static List<JobParameterInfo> getDTOParam(com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML) {
		List<JobParameterInfo> ret = new ArrayList<JobParameterInfo>();

		for (Param param : jobMasterXML.getParam()) {
			JobParameterInfo paramInfo = new JobParameterInfo();
			paramInfo.setDescription(param.getDescription());
			paramInfo.setParamId(param.getParamId());
			paramInfo.setType(param.getParamType());
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
	private static void getDTONotices(JobInfo info, com.clustercontrol.utility.settings.job.xml.JobInfo jobMasterXML){
		List<com.clustercontrol.ws.notify.NotifyRelationInfo> relations = info.getNotifyRelationInfos();
		com.clustercontrol.ws.notify.NotifyRelationInfo relation ;

		for(NotifyRelationInfo nInfo: jobMasterXML.getNotifyRelationInfos()){
			relation = new com.clustercontrol.ws.notify.NotifyRelationInfo();
			relation.setNotifyId(nInfo.getNotifyId());
			relation.setNotifyType(nInfo.getNotifyType());
			relations.add(relation);
		}
	}

	/**
	 * ジョブ詳細データを生成
	 *
	 * @return
	 *
	 * @see
	 */
	public static com.clustercontrol.utility.settings.job.xml.JobInfo setXMLJobData(JobInfo jobMgr,String parentJobID){
		com.clustercontrol.utility.settings.job.xml.JobInfo jobXML = new com.clustercontrol.utility.settings.job.xml.JobInfo();

		if (jobMgr.getJobunitId() != null) {
			jobXML.setJobunitId(jobMgr.getJobunitId());
		}

		// Check if it is not top level
		if (parentJobID != null) {
			jobXML.setParentJobId(parentJobID);
			jobXML.setParentJobunitId(jobMgr.getJobunitId());
		} else if (jobMgr.getType().equals(JobConstant.TYPE_JOBUNIT)) {
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
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATA_FORMAT);
			jobXML.setCreateTime(dateFormat.format(jobMgr.getCreateTime()));
		}
		if (jobMgr.getUpdateTime() != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATA_FORMAT);
			jobXML.setUpdateTime(dateFormat.format(jobMgr.getUpdateTime()));
		}
		if (jobMgr.getCreateUser() != null) {
			jobXML.setCreateUser(jobMgr.getCreateUser());
		}
		if (jobMgr.getUpdateUser() != null) {
			jobXML.setUpdateUser(jobMgr.getUpdateUser());
		}

		jobXML.setType(jobMgr.getType());
		jobXML.setOwnerRoleId(jobMgr.getOwnerRoleId());

		jobXML.setRegisteredModule(jobMgr.isRegisteredModule());
		jobXML.setIconId(jobMgr.getIconId());

		JobCommandInfo commandInfo = jobMgr.getCommand();
		if (commandInfo != null) {
			jobXML.setFacilityId(commandInfo.getFacilityID());
			// jobMgr.getCommand().getScope() should be ignored because of no
			// use
			jobXML.setProcessMode(commandInfo.getProcessingMethod());

			// スクリプトをマネージャから配布
			jobXML.setManagerDistribution(commandInfo.isManagerDistribution());
			// スクリプト名
			jobXML.setScriptName(commandInfo.getScriptName());
			// スクリプトエンコーディング
			jobXML.setScriptEncoding(commandInfo.getScriptEncoding());
			// スクリプト内容
			jobXML.setScriptContent(commandInfo.getScriptContent());
			// コマンドジョブパラメータ
			for(JobCommandParam param : commandInfo.getJobCommandParamList()){
				CommandParam p = new CommandParam();
				p.setJobStandardOutputFlg(param.isJobStandardOutputFlg());
				p.setParamId(param.getParamId());
				p.setValue(param.getValue());
				jobXML.addCommandParam(p);
			}
			// コマンドジョブ環境変数
			for(JobEnvVariableInfo var : commandInfo.getEnvVariableInfo()) {
				EnvVariableInfo e = new EnvVariableInfo();
				e.setEnvVariableId(var.getEnvVariableId());
				e.setValue(var.getValue());
				e.setDescription(var.getDescription());
				jobXML.addEnvVariableInfo(e);
			}

			jobXML.setStartCommand(commandInfo.getStartCommand());
			jobXML.setStopCommand(commandInfo.getStopCommand());
			jobXML.setStopType(commandInfo.getStopType());
			jobXML.setSpecifyUser(commandInfo.isSpecifyUser());
			jobXML.setEffectiveUser(commandInfo.getUser());
			jobXML.setMessageRetryEndFlg(commandInfo.isMessageRetryEndFlg());
			jobXML.setMessageRetry(commandInfo.getMessageRetry());
			jobXML.setMessageRetryEndValue(commandInfo.getMessageRetryEndValue());
			jobXML.setCommandRetryFlg(commandInfo.isCommandRetryFlg());

			// Hard-code : CommandRetry could be NULL
			jobXML.setCommandRetryEndStatus(commandInfo.getCommandRetryEndStatus() != null ? commandInfo.getCommandRetryEndStatus() : EndStatusConstant.INITIAL_VALUE_NORMAL);
			jobXML.setCommandRetry(commandInfo.getCommandRetry() != null ? commandInfo.getCommandRetry() : 10);
		}

		// 終了状態
		List<JobEndStatusInfo> endStatusInfos = jobMgr.getEndStatus();
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
		if (jobMgr.getType().equals(JobConstant.TYPE_APPROVALJOB)) {
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
			jobXML.setUseApprovalReqSentence(jobMgr.isUseApprovalReqSentence());
		}

		// 監視の場合
		if (jobMgr.getType().equals(JobConstant.TYPE_MONITORJOB)) {
			// ファシリティID
			jobXML.setFacilityId(jobMgr.getMonitor().getFacilityID());
			//スコープ処理
			jobXML.setProcessMode(jobMgr.getMonitor().getProcessingMethod());
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
		JobFileInfo fileInfo = jobMgr.getFile();
		if (fileInfo != null) {
			jobXML.setSrcFacilityId(fileInfo.getSrcFacilityID());
			jobXML.setSrcScope(fileInfo.getSrcScope()); // Skip
																// this
			jobXML.setSrcFile(fileInfo.getSrcFile());
			jobXML.setDestFacilityId(fileInfo.getDestFacilityID());
			jobXML.setDestDirectory(fileInfo.getDestDirectory());
			jobXML.setProcessMode(fileInfo.getProcessingMethod());
			jobXML.setCompressionFlg(fileInfo.isCompressionFlg());
			jobXML.setCheckFlg(fileInfo.isCheckFlg());
			jobXML.setSpecifyUser(fileInfo.isSpecifyUser());
			jobXML.setEffectiveUser(fileInfo.getUser());
			jobXML.setMessageRetryEndFlg(fileInfo.isMessageRetryEndFlg());
			jobXML.setMessageRetry(fileInfo.getMessageRetry());
			// jobMgr.getFile().getMessageRetryEndValue() should be ignored
			// because of no use. It is already deprecated from 5.0
			jobXML.setCommandRetryFlg(fileInfo.isCommandRetryFlg());
			// Hard-code : CommandRetry could be NULL
			jobXML.setCommandRetryEndStatus(fileInfo.getCommandRetryEndStatus() != null ? fileInfo.getCommandRetryEndStatus() : EndStatusConstant.INITIAL_VALUE_NORMAL);
			jobXML.setCommandRetry(fileInfo.getCommandRetry() != null ? fileInfo.getCommandRetry() : 10);
		}

		//待ち条件,遅延監視,開始遅延
		JobWaitRuleInfo ruleInfo = jobMgr.getWaitRule();
		if (ruleInfo != null) {
			if (jobMgr.getType() == JobConstant.TYPE_JOBUNIT
					|| jobMgr.getType() == JobConstant.TYPE_REFERJOB
					|| jobMgr.getType() == JobConstant.TYPE_REFERJOBNET) {
				if (jobMgr.getType() == JobConstant.TYPE_REFERJOB || jobMgr.getType() == JobConstant.TYPE_REFERJOBNET) {
					// 参照ジョブは制御タブの情報は必要な為、DTOの値を設定
					jobXML.setConditionType(ruleInfo.getCondition());
					jobXML.setUnmatchEndFlg(ruleInfo.isEndCondition());
					jobXML.setUnmatchEndStatus(ruleInfo.getEndStatus());
					jobXML.setUnmatchEndValue(ruleInfo.getEndValue());
					if (ruleInfo.getObject() != null && ruleInfo.getObject().size() != 0) {
						jobXML.setStartJob(getXMLJobObject(ruleInfo.getObject(), jobMgr.getId()));
					}
				} else {
					// ジョブユニットは不要な為、値を設定しない
				}
			} else {
				jobXML.setCalendar(ruleInfo.isCalendar());
				jobXML.setCalendarEndStatus(ruleInfo.getCalendarEndStatus());
				jobXML.setCalendarEndValue(ruleInfo.getCalendarEndValue());
				jobXML.setCalendarId(ruleInfo.getCalendarId());
				jobXML.setSuspend(ruleInfo.isSuspend());
				jobXML.setSkip(ruleInfo.isSkip());
				jobXML.setSkipEndStatus(ruleInfo.getSkipEndStatus());
				jobXML.setSkipEndValue(ruleInfo.getSkipEndValue());

				jobXML.setConditionType(ruleInfo.getCondition());
				jobXML.setUnmatchEndFlg(ruleInfo.isEndCondition());
				jobXML.setUnmatchEndStatus(ruleInfo.getEndStatus());
				jobXML.setUnmatchEndValue(ruleInfo.getEndValue());
				if (ruleInfo.getObject() != null && ruleInfo.getObject().size() != 0) {
					jobXML.setStartJob(getXMLJobObject(ruleInfo.getObject(), jobMgr.getId()));
				}

				jobXML.setStartDelay(ruleInfo.isStartDelay());
				jobXML.setStartDelayConditionType(ruleInfo.getStartDelayConditionType());
				jobXML.setStartDelayNotify(ruleInfo.isStartDelayNotify());
				jobXML.setStartDelayNotifyPriority(ruleInfo.getStartDelayNotifyPriority());
				jobXML.setStartDelayOperation(ruleInfo.isStartDelayOperation());
				jobXML.setStartDelayOperationEndStatus(ruleInfo.getStartDelayOperationEndStatus());
				jobXML.setStartDelayOperationEndValue(ruleInfo.getStartDelayOperationEndValue());
				jobXML.setStartDelayOperationType(ruleInfo.getStartDelayOperationType());
				jobXML.setStartDelaySession(ruleInfo.isStartDelaySession());
				jobXML.setStartDelaySessionValue(ruleInfo.getStartDelaySessionValue());
				jobXML.setStartDelayTime(ruleInfo.isStartDelayTime());
				if (ruleInfo.getStartDelayTimeValue() != null) {
					jobXML.setStartDelayTimeValue(DateUtil.convEpoch2TimeString(ruleInfo.getStartDelayTimeValue()));
				}

				jobXML.setEndDelay(ruleInfo.isEndDelay());
				jobXML.setEndDelayConditionType(ruleInfo.getEndDelayConditionType());
				jobXML.setEndDelayJob(ruleInfo.isEndDelayJob());
				jobXML.setEndDelayJobValue(ruleInfo.getEndDelayJobValue());
				jobXML.setEndDelayNotify(ruleInfo.isEndDelayNotify());
				jobXML.setEndDelayNotifyPriority(ruleInfo.getEndDelayNotifyPriority());
				jobXML.setEndDelayOperation(ruleInfo.isEndDelayOperation());
				jobXML.setEndDelayOperationEndStatus(ruleInfo.getEndDelayOperationEndStatus());
				jobXML.setEndDelayOperationEndValue(ruleInfo.getEndDelayOperationEndValue());
				jobXML.setEndDelayOperationType(ruleInfo.getEndDelayOperationType());
				jobXML.setEndDelaySession(ruleInfo.isEndDelaySession());
				jobXML.setEndDelaySessionValue(ruleInfo.getEndDelaySessionValue());
				jobXML.setEndDelayTime(ruleInfo.isEndDelayTime());
				if (ruleInfo.getEndDelayTimeValue() != null) {
					jobXML.setEndDelayTimeValue(DateUtil.convEpoch2TimeString(ruleInfo.getEndDelayTimeValue()));
				}
				jobXML.setEndDelayChangeMount(ruleInfo.isEndDelayChangeMount());
				jobXML.setEndDelayChangeMountValue(ruleInfo.getEndDelayChangeMountValue());

				if ((jobXML.getType() == JobConstant.TYPE_FILEJOB)
						|| (jobXML.getType() == JobConstant.TYPE_JOB)) {
					jobXML.setMultiplicityNotify(ruleInfo.isMultiplicityNotify());
					jobXML.setMultiplicityNotifyPriority(ruleInfo.getMultiplicityNotifyPriority());
					jobXML.setMultiplicityOperation(ruleInfo.getMultiplicityOperation());
					jobXML.setMultiplicityEndValue(ruleInfo.getMultiplicityEndValue());
				}

				jobXML.setExclusiveBranch(ruleInfo.isExclusiveBranch());
				if (ruleInfo.isExclusiveBranch()) {
					jobXML.setExclusiveBranchEndStatus(ruleInfo.getExclusiveBranchEndStatus());
					jobXML.setExclusiveBranchEndValue(ruleInfo.getExclusiveBranchEndValue());
					List<ExclusiveJobValue> values = new ArrayList<>();
					int order = 0;
					for (JobNextJobOrderInfo nextJob : ruleInfo.getExclusiveBranchNextJobOrderList()) {
						ExclusiveJobValue value = new ExclusiveJobValue();
						value.setNextJobId(nextJob.getNextJobId());
						value.setOrder(++order);
						values.add(value);
					}
					jobXML.setExclusiveJobValue(values.toArray(new ExclusiveJobValue[0]));
				}
				jobXML.setJobRetryFlg(ruleInfo.isJobRetryFlg());
				jobXML.setJobRetryEndStatus(ruleInfo.getJobRetryEndStatus() != null ? ruleInfo.getJobRetryEndStatus() : EndStatusConstant.INITIAL_VALUE_NORMAL);
				jobXML.setJobRetry(ruleInfo.getJobRetry() != null ? ruleInfo.getJobRetry() : 10);
			}
		}

		if (jobMgr.getReferJobId() != null) {
			jobXML.setReferJobId(jobMgr.getReferJobId());
		}
		if (jobMgr.getReferJobUnitId() != null) {
			jobXML.setReferJobunitId(jobMgr.getReferJobUnitId());
		}
		if (jobMgr.getType() == JobConstant.TYPE_REFERJOB || jobMgr.getType() == JobConstant.TYPE_REFERJOBNET){
			jobXML.setReferJobSelectType(jobMgr.getReferJobSelectType());
		}
		if (jobMgr.getParam() != null && jobMgr.getParam().size() != 0) {
			jobXML.setParam(getXMLParam(jobMgr));
		}

		// 通知先指定
		setXMLNotices(jobMgr, jobXML);
		if (jobMgr.getType() != JobConstant.TYPE_REFERJOB && jobMgr.getType() != JobConstant.TYPE_REFERJOBNET) {
			jobXML.setBeginPriority(jobMgr.getBeginPriority());
			jobXML.setNormalPriority(jobMgr.getNormalPriority());
			jobXML.setWarnPriority(jobMgr.getWarnPriority());
			jobXML.setAbnormalPriority(jobMgr.getAbnormalPriority());
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
	private static void setXMLNotices(JobInfo jobMgr, com.clustercontrol.utility.settings.job.xml.JobInfo jobXML)
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
			for(com.clustercontrol.ws.notify.NotifyRelationInfo info : jobMgr.getNotifyRelationInfos()) {
				com.clustercontrol.utility.settings.job.xml.NotifyRelationInfos notify
					= new com.clustercontrol.utility.settings.job.xml.NotifyRelationInfos();

				notify.setNotifyGroupId(info.getNotifyGroupId());
				notify.setNotifyId(info.getNotifyId());
				notify.setNotifyType(info.getNotifyType());

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
	private static StartJob[] getXMLJobObject(List<JobObjectInfo> objectMgr, String jobId) {
		List<StartJob> ret = new ArrayList<StartJob>();

		for(JobObjectInfo info : objectMgr){
			StartJob startJob = new StartJob();

			// Set common part
			startJob.setTargetJobType(info.getType());
			startJob.setTargetJobId("");
			startJob.setStartTime("");
			startJob.setTargetJobEndValue(0);
			startJob.setTargetJobDescription(info.getDescription());
			startJob.setStartDecisionValue01("");
			startJob.setStartDecisionValue02("");
			// startJob.setStartDecisionCondition(0);未設定

			switch(info.getType().intValue()){
				case JudgmentObjectConstant.TYPE_JOB_END_STATUS:
					startJob.setTargetJobId(info.getJobId());
					startJob.setTargetJobEndValue(info.getValue());
					break;
				case JudgmentObjectConstant.TYPE_JOB_END_VALUE:
					startJob.setTargetJobId(info.getJobId());
					startJob.setTargetJobEndValue(info.getValue());
					break;
				case JudgmentObjectConstant.TYPE_TIME:
					try {
						startJob.setStartTime(DateUtil.convEpoch2TimeString(info.getTime()));
					} catch (Exception e) {
						logger.error(e);
					}
					break;
				case JudgmentObjectConstant.TYPE_START_MINUTE:
					startJob.setTargetJobEndValue(info.getStartMinute()); // Use value as startMinute
					break;
				case JudgmentObjectConstant.TYPE_JOB_PARAMETER:
					startJob.setStartDecisionValue01(info.getDecisionValue01());
					startJob.setStartDecisionValue02(info.getDecisionValue02());
					startJob.setStartDecisionCondition(info.getDecisionCondition());
					break;
				case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS:
				case JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE:
					startJob.setTargetCrossSessionRange(info.getCrossSessionRange());
					startJob.setTargetJobId(info.getJobId());
					startJob.setTargetJobEndValue(info.getValue());

					break;
				default:
					String msg = "Unknown start job type : " + info.getType();
					logger.error(msg);
					throw new IllegalArgumentException(msg);
			}
			ret.add(startJob);
		}

		return ret.toArray(new com.clustercontrol.utility.settings.job.xml.StartJob[0]);
	}

	/**
	 * ジョブパラメータをDTOからXMLのBeanに変換します。
	 * @param jobMgr　コマンドのDTO
	 * @return　コマンドのXML Bean
	 */
	private static com.clustercontrol.utility.settings.job.xml.Param[] getXMLParam(JobInfo jobMgr) {
		Param[] ret = new com.clustercontrol.utility.settings.job.xml.Param[jobMgr.getParam().size()];

		for (int i=0 ; i< jobMgr.getParam().size(); i++) {
			ret[i] = new com.clustercontrol.utility.settings.job.xml.Param();
			if(jobMgr.getParam().get(i).getParamId() != null)
				ret[i].setParamId(jobMgr.getParam().get(i).getParamId());
			if(jobMgr.getParam().get(i).getDescription() != null)
				ret[i].setDescription(jobMgr.getParam().get(i).getDescription());

			ret[i].setParamType(jobMgr.getParam().get(i).getType());
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
