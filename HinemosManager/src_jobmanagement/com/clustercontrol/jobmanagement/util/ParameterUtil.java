/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.ObjectValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.FileCheckConstant;
import com.clustercontrol.jobmanagement.bean.JobParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.RunInstructionFileCheckInfo;
import com.clustercontrol.jobmanagement.bean.RunResultFileCheckInfo;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.DateUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.StringBinder;

import jakarta.persistence.EntityExistsException;

/**
 * ジョブ変数ユーティリティクラス<BR>
 *
 * @version 3.0.0
 * @since 2.1.0
 */
public class ParameterUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ParameterUtil.class );

	public static final String REGEX_RETURN = "#\\[RETURN:([^:]*):([^:\\]]*)\\]";
	public static final String REGEX_END_NUM = "#\\[END_NUM:([^:\\]]*)\\]";

	/**
	 * 監視結果情報、ジョブ契機（ファイルチェック情報）を取得します。
	 * ジョブセッション作成時に、ジョブ変数に対応する値を取得するために使用します。
	 *
	 * @param outputBasicInfo 監視結果情報
	 * @param jobTriggerInfo ジョブ契機（ファイルチェック）情報
	 * @return 値
	 */
	public static Map<String, String> createParamInfo(OutputBasicInfo outputBasicInfo, JobTriggerInfo jobTriggerInfo) {
		// 戻り値
		Map<String, String> params = new HashMap<String, String>();
		// 無効なシステムジョブ変数
		String[] disableStrAry = null;
		
		/**
		 * 監視管理の情報をログ出力情報から取得します。
		 * ジョブセッション作成時に、ジョブ変数に対応する値を取得するために使用します。
		 */
		// ログ出力情報が存在する場合
		if(outputBasicInfo != null) {
			Locale locale = NotifyUtil.getNotifyLocale();
			
			// ファシリティID
			if (!ObjectValidator.isEmptyString(outputBasicInfo.getFacilityId()) && !disableParam(SystemParameterConstant.FACILITY_ID, disableStrAry)) {
				params.put(SystemParameterConstant.FACILITY_ID, outputBasicInfo.getFacilityId());
			}
			
			// プラグインID
			if (!ObjectValidator.isEmptyString(outputBasicInfo.getPluginId()) && !disableParam(SystemParameterConstant.PLUGIN_ID, disableStrAry)) {
				params.put(SystemParameterConstant.PLUGIN_ID, outputBasicInfo.getPluginId());
			}
			
			// 監視項目ID
			if (!ObjectValidator.isEmptyString(outputBasicInfo.getMonitorId()) && !disableParam(SystemParameterConstant.MONITOR_ID, disableStrAry)) {
				params.put(SystemParameterConstant.MONITOR_ID, outputBasicInfo.getMonitorId());
			}
			
			// 監視詳細
			if (!ObjectValidator.isEmptyString(outputBasicInfo.getSubKey()) && !disableParam(SystemParameterConstant.MONITOR_DETAIL_ID, disableStrAry)) {
				params.put(SystemParameterConstant.MONITOR_DETAIL_ID, outputBasicInfo.getSubKey());
			}
			
			// アプリケーション
			if (!ObjectValidator.isEmptyString(outputBasicInfo.getApplication()) && !disableParam(SystemParameterConstant.APPLICATION, disableStrAry)) {
				params.put(SystemParameterConstant.APPLICATION, outputBasicInfo.getApplication());
			}
			
			// 重要度
			if (!disableParam(SystemParameterConstant.PRIORITY, disableStrAry)) { 
				params.put(SystemParameterConstant.PRIORITY, String.valueOf(outputBasicInfo.getPriority()));
			}
	
			// メッセージ
			if (!ObjectValidator.isEmptyString(outputBasicInfo.getMessage()) && !disableParam(SystemParameterConstant.MESSAGE, disableStrAry)) {
				params.put(SystemParameterConstant.MESSAGE, HinemosMessage.replace(outputBasicInfo.getMessage(), locale));
			}
			
			// オリジナルメッセージ
			if (!ObjectValidator.isEmptyString(outputBasicInfo.getMessageOrg()) && !disableParam(SystemParameterConstant.ORG_MESSAGE, disableStrAry)) {
				params.put(SystemParameterConstant.ORG_MESSAGE, HinemosMessage.replace(outputBasicInfo.getMessageOrg(), locale));
			}
		}

		/**
		 * ジョブ契機（ファイルチェック）情報を設定します。
		 * ジョブセッション作成時に、ジョブ変数に対応する値を取得するために使用します。
		 */
		// ジョブ契機（ファイルチェック）情報が存在する場合
		if(jobTriggerInfo != null) {
			// ファイル名
			if (!ObjectValidator.isEmptyString(jobTriggerInfo.getFilename()) && !disableParam(SystemParameterConstant.FILENAME, disableStrAry)) {
				params.put(SystemParameterConstant.FILENAME, jobTriggerInfo.getFilename());
			}
			
			// ディレクトリ
			if (!ObjectValidator.isEmptyString(jobTriggerInfo.getDirectory()) && !disableParam(SystemParameterConstant.DIRECTORY, disableStrAry)) {
				params.put(SystemParameterConstant.DIRECTORY, jobTriggerInfo.getDirectory());
			}
		}

		return params;
	}

	/**
	 * ファイルチェックジョブの実行指示情報からシステムジョブ変数に格納する情報を取得します。<BR>
	 * 
	 * @param info
	 * @param jobId
	 * @return
	 */
	public static Map<String, String> createParamInfo(RunInstructionFileCheckInfo info, String jobId) {
		// 戻り値
		Map<String, String> params = new HashMap<>();
		// 無効なシステムジョブ変数
		String[] disableStrAry = null;

		// 実行指示情報が存在しない場合、処理終了
		if (info == null) {
			// 通常到達しない
			m_log.error("createParamInfo() : RunInstructionFileCheckInfo is null." + " jobId=" + jobId);
			return params;
		}

		// ディレクトリ
		if (!ObjectValidator.isEmptyString(info.getDirectory())
				&& !disableParam(SystemParameterConstant.FDIRECTORY, disableStrAry)) {
			params.put(createParamIdFrom(SystemParameterConstant.FDIRECTORY, jobId), info.getDirectory());
		}
		// ファイルチェックが開始しているか（ジョブ開始時点では"false"）
		if (!disableParam(SystemParameterConstant.FCISSTART, disableStrAry)) {
			params.put(createParamIdFrom(SystemParameterConstant.FCISSTART, jobId), "false");
		}

		return params;
	}

	/**
	 * ファイルチェックジョブの開始時点でシステムジョブ変数に格納する情報を取得します。<BR>
	 * 
	 * @param jobId
	 * @return
	 */
	public static Map<String, String> createParamInfoFcStart(String jobId) {
		// 戻り値
		Map<String, String> params = new HashMap<>();
		// 無効なシステムジョブ変数
		String[] disableStrAry = null;

		// ファイルチェックが開始しているか（エージェントから開始の応答があったら"true"）
		if (!disableParam(SystemParameterConstant.FCISSTART, disableStrAry)) {
			params.put(createParamIdFrom(SystemParameterConstant.FCISSTART, jobId), "true");
		}

		return params;
	}

	/**
	 * ファイルチェックジョブの実行結果情報からシステムジョブ変数に格納する情報を取得します。<BR>
	 * [全てのノード]で条件を満たした場合を考慮し、facilityIdを必要とします。
	 * 
	 * @param info
	 * @param jobId
	 * @return
	 */
	public static Map<String, String> createParamInfo(RunResultFileCheckInfo info, String jobId, String facilityId) {
		// 戻り値
		Map<String, String> params = new HashMap<>();
		// 無効なシステムジョブ変数
		String[] disableStrAry = null;

		// 実行結果情報が存在しない場合、処理終了
		if (info == null) {
			// タイムアウトの場合はnullになる
			m_log.info("createParamInfo() : RunResultFileCheckInfo is null." + " jobId=" + jobId);
			return params;
		}

		// ファイル名
		if (!ObjectValidator.isEmptyString(info.getFileName())
				&& !disableParam(SystemParameterConstant.FFILENAME, disableStrAry)) {
			params.put(createParamIdFrom(SystemParameterConstant.FFILENAME, jobId, facilityId), info.getFileName());
		}
		// チェック種別
		if (!ObjectValidator.isEmptyString(info.getPassedEventType())
				&& !disableParam(SystemParameterConstant.FCHECKCOND, disableStrAry)) {
			params.put(createParamIdFrom(SystemParameterConstant.FCHECKCOND, jobId, facilityId),
					info.getPassedEventType().toString());
		}
		// 条件に一致したファイルのファイル更新日時
		if (info.getPassedEventType() == FileCheckConstant.RESULT_MODIFY_TIMESTAMP) {
			if (!ObjectValidator.isEmptyString(info.getFileTimestamp())
					&& !disableParam(SystemParameterConstant.FTIMESTAMP, disableStrAry)) {
				try {
					String formatted = DateUtil.millisToString(info.getFileTimestamp(),
							HinemosPropertyCommon.job_filecheck_replace_date_format.getStringValue());
					params.put(createParamIdFrom(SystemParameterConstant.FTIMESTAMP, jobId, facilityId), formatted);
				} catch (InvalidSetting e) {
					// フォーマットによる変換に失敗した場合はそのまま格納する
					params.put(createParamIdFrom(SystemParameterConstant.FTIMESTAMP, jobId, facilityId),
							info.getFileTimestamp().toString());
				}
			}
		}
		// 条件に一致したファイルのファイルサイズ
		if (info.getPassedEventType() == FileCheckConstant.RESULT_MODIFY_FILESIZE) {
			if (!ObjectValidator.isEmptyString(info.getFileSize())
					&& !disableParam(SystemParameterConstant.FILESIZE, disableStrAry)) {
				params.put(createParamIdFrom(SystemParameterConstant.FILESIZE, jobId, facilityId),
						info.getFileSize().toString());
			}
		}

		return params;
	}

	/**
	 * 重複を考慮してシステムジョブ変数を登録します。
	 * 
	 * @param sessionJob
	 * @param paramMap
	 * @param update true:既に存在した場合値を更新する, false:更新しない
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public static void registerSystemJobParamInfo(JobSessionJobEntity sessionJob, Map<String, String> paramMap,
			boolean update) throws JobInfoNotFound, InvalidRole {
		if (paramMap == null || paramMap.isEmpty()) {
			return;
		}
		// ジョブセッションのジョブIDから親のジョブセッションジョブを取得
		JobSessionJobEntity rootSessionJob = QueryUtil.getJobSessionJobPK(sessionJob.getId().getSessionId(),
				sessionJob.getId().getJobunitId(), sessionJob.getJobSessionEntity().getJobId());
		// システムジョブ変数の紐づけ先はセッションの親のジョブになる
		JobInfoEntity job = rootSessionJob.getJobInfoEntity();

		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			registerParamInfo(job, entry.getKey(), entry.getValue(), null, JobParamTypeConstant.TYPE_SYSTEM_JOB, update);
		}
	}

	/**
	 * 重複を考慮してジョブ変数を登録します。
	 * 
	 * @param job
	 * @param paramId
	 * @param value
	 * @param description
	 * @param paramType
	 * @param update true:既に存在した場合値を更新する, false:更新しない
	 */
	public static void registerParamInfo(JobInfoEntity job, String paramId, String value, String description, int paramType,
			boolean update) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			JobParamInfoEntity jobParamInfoEntity = new JobParamInfoEntity(job, paramId);

			// 重複チェック
			JobParamInfoEntity existingEntity = null;
			for (JobParamInfoEntity jobParamInfo : job.getJobParamInfoEntities()) {
				if (jobParamInfo.getId().equals(jobParamInfoEntity.getId())) {
					existingEntity = jobParamInfo;
					break;
				}
			}
			if (existingEntity == null) {
				// JPAのキャッシュと乖離がある可能性があるため、最新のDBの状態でも重複がないか確認する
				try {
					jtm.checkEntityExists(JobParamInfoEntity.class, jobParamInfoEntity.getId());
				} catch (EntityExistsException e) {
					m_log.debug("registerParamInfo() : " + e.getClass().getSimpleName() + ", "
							+ jobParamInfoEntity.getId().toString());
					// checkEntityExistsでリフレッシュしているため通常のfindで取得
					existingEntity = em.find(JobParamInfoEntity.class, jobParamInfoEntity.getId(),
							ObjectPrivilegeMode.NONE);
					if (existingEntity == null) {
						// リフレッシュしてから取得しているため通常起こらない想定
						m_log.error("registerParamInfo() : JobParamInfoEntity is null.");
						return;
					}
				}
			}
			if (existingEntity != null) {
				// 既にエンティティが存在する場合
				m_log.debug("registerParamInfo() : exists. " + jobParamInfoEntity.getId().toString());
				if (update) {
					// 更新が必要な場合は値を更新する
					existingEntity.setValue(value);
					existingEntity.setDescription(description);
					m_log.debug("registerParamInfo() : update. value=" + existingEntity.getValue());
				}
				return;
			}
			// 新規登録
			jobParamInfoEntity.setValue(value);
			jobParamInfoEntity.setDescription(description);
			jobParamInfoEntity.setParamType(paramType);
			em.persist(jobParamInfoEntity);
			jobParamInfoEntity.relateToJobInfoEntity(job);
			m_log.debug("registerParamInfo() : registered. " + jobParamInfoEntity.getId().toString() + ", value="
					+ jobParamInfoEntity.getValue());
		}
	}

	/**
	 * セッションIDからジョブ変数の置換文字列用Mapを取得します。
	 *
	 * @param sessionId ジョブセッションID
	 * @return 置換文字列用Map
	 */
	private static Map<String, String> getJobSessionParamsMap(String sessionId) throws JobInfoNotFound {
		Map<String, String> jobSessionParams = new HashMap<String, String>();
		
		Collection<JobParamInfoEntity> collection = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			collection = em.createNamedQuery("JobParamInfoEntity.findBySessionId", JobParamInfoEntity.class)
					.setParameter("sessionId", sessionId)
					.getResultList();
			if (collection == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobParamInfoEntity.findBySessionId"
						+ ", sessionId = " + sessionId);
				m_log.info("getJobSessionParamValue() : " + je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
		}
		
		if (collection.size() > 0){
			Iterator<JobParamInfoEntity> itr = collection.iterator();
			while(itr.hasNext()){
				JobParamInfoEntity param = itr.next();
				jobSessionParams.put(param.getId().getParamId(), param.getValue());
			}
		}
	
		m_log.debug("getJobSessionParamValue() end sessionId=" + sessionId);
		return jobSessionParams;
	}

	/**
	 *
	 * セッションIDからジョブセッションの置換文字列用Mapを取得します。
	 *
	 * @param paramIdList 置換対象パラメータ
	 * @param sessionId セッションID
	 * @return
	 * @throws JobInfoNotFound
	 */
	private static Map<String, String> getJobSessionMap(List<String> paramIdList, String sessionId) throws JobInfoNotFound {
		JobSessionEntity jobSessionEntity = null;
		Map<String, String> params = new HashMap<String, String>();
		Locale locale = NotifyUtil.getNotifyLocale();
		
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(HinemosTime.getTimeZone());
		
		if (StringBinder.containsParam(paramIdList, SystemParameterConstant.SESSION_ID)) {
			params.put(SystemParameterConstant.SESSION_ID, sessionId);
		}
		
		if (StringBinder.containsParam(paramIdList, SystemParameterConstant.START_DATE) ||
				StringBinder.containsParam(paramIdList, SystemParameterConstant.TRIGGER_TYPE) ||
				StringBinder.containsParam(paramIdList, SystemParameterConstant.TRIGGER_INFO)) {
			//DBへのアクセス回数を極力少なくするため、パラメータがある場合のみ、ジョブセッションを取得する
			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				HinemosEntityManager em = jtm.getEntityManager();
				jobSessionEntity = em.find(JobSessionEntity.class, sessionId, ObjectPrivilegeMode.READ);
				if (jobSessionEntity == null) {
					JobInfoNotFound je = new JobInfoNotFound("JobSessionEntity.findByPrimaryKey"
							+ ", sessionId = " + sessionId);
					m_log.info("getJobParameterValue() : "
							+ je.getClass().getSimpleName() + ", " + je.getMessage());
					je.setSessionId(sessionId);
					throw je;
				}
			}
		}
		
		if (StringBinder.containsParam(paramIdList, SystemParameterConstant.START_DATE)) {
			params.put(SystemParameterConstant.START_DATE, df.format(jobSessionEntity.getScheduleDate()));
		}
		if (StringBinder.containsParam(paramIdList, SystemParameterConstant.TRIGGER_TYPE)) {
			params.put(SystemParameterConstant.TRIGGER_TYPE, Messages.getString(JobTriggerTypeConstant.typeToMessageCode(jobSessionEntity.getTriggerType()), locale));
		}
		
		if (StringBinder.containsParam(paramIdList, SystemParameterConstant.TRIGGER_INFO)) {
			params.put(SystemParameterConstant.TRIGGER_INFO, jobSessionEntity.getTriggerInfo());
		}
		return params;
	}

	/**
	 * ジョブ変数を置換する
	 * 
	 * @param paramId ジョブ変数
	 * @param paramMap ジョブ変数の編集元Map
	 * @param isEscape エスケープするか
	 * @return
	 */
	private static String getReplacedValue(String paramId, Map<String, String> paramMap, boolean isEscape) {
		String ret = null;
		
		if (paramId == null || paramMap == null) {
			return ret;
		}
		
		if (isEscape) {
			if (paramMap.containsKey(paramId)) {
				ret = paramMap.get(paramId);
				if (ret == null) {
					return "";
				} else {
					return StringBinder.escapeStr(ret);
				}
			}
			
			String notOrignalParamId = SystemParameterConstant.getNotOriginalParam(paramId);
			
			if (notOrignalParamId != null) {
				if (paramMap.containsKey(notOrignalParamId)) {
					ret = paramMap.get(notOrignalParamId);
					if (ret == null) {
						return "";
					} else {
						return ret;
					}
				} else {
					return null;
				}
			}
		} else {
			if (paramMap.containsKey(paramId)) {
				ret = paramMap.get(paramId);
				if (ret == null) {
					return "";
				} else {
					return ret;
				}
			}
		}
		
		// :quoteSh, :escapeCmdが付与されている場合はisEscape(プロパティ)に関わらず置換を行う。
		String[] splitedId = StringBinder.splitPostfix(paramId);
		String opt  = splitedId[1];
		if (paramMap.containsKey(splitedId[0])) {
			String param = paramMap.get(splitedId[0]);
			if (param == null) {
				param =  "";
			}
			return StringBinder.escapeShell(param, opt);
		}
		
		return ret;
	}
	
	private static String getFacilityIdParam(String paramId, String nodeFacilityId, boolean isEscape) {
		if (isEscape) {
			String notOrignalParamId = SystemParameterConstant.getNotOriginalParam(paramId);
			
			if (notOrignalParamId != null) {
				//末尾が:orignalのparamIdはorignalの前にfalicrtyIdを結合
				return notOrignalParamId + ":" + nodeFacilityId + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE;
			} 
		}
		
		// :quoteSh, :escapeCmdが付与されている場合はisEscape(プロパティ)に関わらず結合する。
		String[] splitedId = StringBinder.splitPostfix(paramId);
		if (splitedId[0] != null) {
			//:originalと同様、前にfalicrtyIdを結合
			return splitedId[0] + ":" + nodeFacilityId  + splitedId[1];			
		}
		
		return paramId + ":" + nodeFacilityId;
	}
	
	/**
	 *
	 * ノード情報からパラメータIDに対応する値を取得します。
	 *
	 * @param paramId パラメータID
	 * @param facilityId ファシリティID
	 * @param nodeParams ノード情報
	 * @return パラメータ値
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 */
	private static String getNodeValue(String paramId, String facilityId, ArrayList<String> keyList)
			throws HinemosUnknown, FacilityNotFound, InvalidRole {

		if (paramId == null) {
			m_log.info("getNodeValue() : paramId is null. facilityId=" + facilityId);
			return null;
		}
		
		m_log.debug("getNodeValue() start paramId=" + paramId + ",facilityId=" + facilityId);
		String ret = null;

		if (facilityId != null && !facilityId.isEmpty()) {
			if (paramId.equals(SystemParameterConstant.FACILITY_ID)){
				// セッションID
				ret = facilityId;
			} else {
				if (new RepositoryControllerBean().isNode(facilityId)) {
					
					// ノードプロパティを取得
					NodeInfo nodeInfo = new RepositoryControllerBean().getNode(facilityId);
					Map<String, String> nodeParams = RepositoryUtil.createNodeParameter(nodeInfo, keyList);
					
					ret = getReplacedValue(paramId, nodeParams, HinemosPropertyCommon.job_param_node_escape.getBooleanValue());
					
				}
			}
		}
		m_log.debug("getNodeValue() end paramId=" + paramId + ",facilityId=" + facilityId + ",value=" + ret);
		return ret;
	}

	/**
	 * HinemosPropertyより無効なジョブ変数かどうか判定する。
	 * 
	 * @param paramId パラメータID
	 * @param disableStrAry 無効なジョブ変数のリスト
	 * @return true:無効、false:有効
	 */
	private static boolean disableParam(String paramId, String[] disableStrAry) {
		// 戻り値
		boolean rtn = false;
		
		// HinemosPropertyより値を取得する
		if (disableStrAry == null) {
			String disableStr = HinemosPropertyCommon.job_param_disable.getStringValue();
			if (disableStr != null && !"".equals(disableStr)) {
				disableStrAry = disableStr.split(",");
			}
		}
		if (disableStrAry != null && disableStrAry.length > 0) {
			// 無効なジョブ変数が存在する場合
			if (Arrays.asList(disableStrAry).contains(paramId)) {
				rtn = true;
			}
		}
		
		return rtn;
	}

	/**
	 * 引数で指定された文字列からパラメータIDを取得し、<BR>
	 * セッションからパラメータIDに対応する値を取得します。<BR>
	 * 引数で指定された文字列のパラメータIDを値で置き換えます。
	 * 
	 * 以下の順に処理を行う。
	 * 1. ジョブ変数
	 * 2. 変数#[END_NUM:jobId]
	 * 2. 変数#[RETURN:jobId:facilityId]
	 *
	 * @param sessionId セッションID
	 * @param jobunitId ジョブユニットID
	 * @param facilityId ファシリティID
	 * @param source 置き換え対象文字列
	 * @return 置き換え後の文字列
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	public static String replaceAllSessionParameterValue(String sessionId, String jobunitId, String facilityId, String source) 
			throws JobInfoNotFound, HinemosUnknown, FacilityNotFound, InvalidRole {
		String commandConv = source;	// 変換後文字列
		// ジョブ変数
		commandConv = ParameterUtil.replaceSessionParameterValue(sessionId, facilityId, commandConv);
		try {
			// 変数#[END_NUM:jobId]
			commandConv = ParameterUtil.replaceEndNumParameter(sessionId, jobunitId, commandConv, false);
			// 変数#[RETURN:jobId:facilityId]
			commandConv = ParameterUtil.replaceReturnCodeParameter(sessionId, jobunitId, commandConv, false);
		} catch (JobInfoNotFound e) {
			// ここは通らない
		}
		return commandConv;
	}

	/**
	 * 引数で指定された文字列からパラメータIDを取得し、<BR>
	 * セッションからパラメータIDに対応する値を取得します。<BR>
	 * 引数で指定された文字列のパラメータIDを値で置き換えます。
	 *
	 * @param sessionId セッションID
	 * @param facilityId ファシリティID
	 * @param source 置き換え対象文字列
	 * @return 置き換え後の文字列
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound 
	 */
	public static String replaceSessionParameterValue(String sessionId, String facilityId, String source) 
			throws JobInfoNotFound, HinemosUnknown, FacilityNotFound, InvalidRole {
		// Local Variables
		String commandOrig = source;	// 変換前文字列
		String commandConv = source;	// 変換後文字列
		int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
		ArrayList<String> inKeyList = StringBinder.getKeyList(source, maxReplaceWord);

		// Main
		if (commandOrig == null) {
			m_log.info("registed command is invalid. (session_id = " + sessionId + ", facility_id = " + facilityId + ", command_orig = null)");
			throw new HinemosUnknown();
		} else {
			m_log.debug("generating command string... (session_id = " + sessionId + ", facility_id = " + facilityId + ", command_orig = " + commandOrig + ")");
		}
		
		// 文字列からパラメータIDを取得
		List<String> list = new ArrayList<String>();
		String regexp = "#\\[[a-zA-Z0-9-_:]+\\]";
		Pattern pattern = Pattern.compile(regexp);
		
		Matcher matcher = pattern.matcher(commandOrig);
		while(matcher.find()) {
			list.add(SystemParameterConstant.getParamId(matcher.group()));
		}
		// パラメータIDが指定されていない場合は処理を終了する。
		if (list.size() == 0) {
			return commandOrig;
		}
		
		// 無効なジョブ変数
		String[] disableStrAry = null;
		// ジョブセッション変数情報用変数
		Map<String, String> jobSessionParamsMap = getJobSessionParamsMap(sessionId);
		// ジョブセッション情報用変数
		Map<String, String> jobSessionMap = getJobSessionMap(inKeyList, sessionId);
		
		// 存在するパラメータ分処理を行う。
		for (String paramId : list) {
			String paramValue = null;
			// ジョブパラメータ情報、ユーザ情報
			paramValue = getJobSessionParamValue(paramId, jobSessionParamsMap, facilityId);
			
			if (paramValue != null) {
				// パラメータ値がnull以外の場合に置換処理を行う。
				commandConv = commandConv.replace(SystemParameterConstant.getParamText(paramId), paramValue);
				continue;
			}
			
			// ジョブ変数（システム）
			if (disableParam(paramId, disableStrAry)) {
				//システムジョブ変数として無効なパラメータに指定があった場合、変換しない
				continue;
			}

			// ジョブセッション情報
			paramValue = getReplacedValue(paramId, jobSessionMap, HinemosPropertyCommon.job_param_runjob_escape.getBooleanValue());
			
			if (paramValue != null) {
				// パラメータ値がnull以外の場合に置換処理を行う。
				commandConv = commandConv.replace(SystemParameterConstant.getParamText(paramId), paramValue);
				continue;
			}
			
			//ノード情報
			paramValue = getNodeValue(paramId, facilityId, inKeyList);
			
			if (paramValue != null) {
				// パラメータ値がnull以外の場合に置換処理を行う。
				commandConv = commandConv.replace(SystemParameterConstant.getParamText(paramId), paramValue);
				continue;
			}
		}

		m_log.debug("successful in generating command string... (session_id = " + sessionId + ", facility_id = " + facilityId + ", command_orig = " + commandOrig + ", command_conv = " + commandConv + ")");
		return commandConv;
	}
	
	/**
	 * ユーザジョブ変数のパラメータを置換する
	 * 以下の優先度で置換
	 * 1:指定されたパラメータをそのまま置換
	 * 2:指定されたパラメータ:指定されたファシリティIDで置換
	 * 3：指定されたパラメータ:ノード登録されているいずれかのファシリティIDで置換（Order by 未指定のため、マッチ順は保証していない）
	 * 
	 * @param paramId 置換対象のパラメータ 
	 * @param jobSessionParamsMap 置換用のジョブセッション情報
	 * @param nodeFacilityId ファシリティID
	 * @return 置換後のパラメータ（置換できなかった場合はnull）
	 */
	private static String getJobSessionParamValue(String paramId, Map<String, String> jobSessionParamsMap, String nodeFacilityId) {
		boolean isEscapeNotify = HinemosPropertyCommon.job_param_notify_escape.getBooleanValue();
		boolean isEscapeRunjob = HinemosPropertyCommon.job_param_runjob_escape.getBooleanValue();
		boolean isEscape = false;
		String paramValue = null;
		
		if (SystemParameterConstant.isNofityParam(paramId)) {
			//通知のパラメータの場合
			isEscape = isEscapeNotify;
		} else if (SystemParameterConstant.isNofityOrgParam(paramId)) {
			if (!isEscapeNotify) {
				//通知のパラメータで:original形式 かつエスケープなしの場合は置換しない
				return SystemParameterConstant.getParamText(paramId); 
			}
			isEscape = isEscapeNotify;
		} else if (SystemParameterConstant.isRunJobParam(paramId)) {
			//ジョブ実行のパラメータの場合
			isEscape = isEscapeRunjob;
		} else if (SystemParameterConstant.isRunJobOrgParam(paramId)) {
				if (!isEscapeRunjob) {
					//ジョブ実行のパラメータで:original形式 かつエスケープなしの場合は置換しない
					return SystemParameterConstant.getParamText(paramId); 
				}
				isEscape = isEscapeRunjob;
		} else {
			isEscape = HinemosPropertyCommon.job_param_user_escape.getBooleanValue();
		}
		
		paramValue = getReplacedValue(paramId, jobSessionParamsMap, isEscape);
		
		if (paramValue != null) {
			return paramValue;
		} else {
			if (SystemParameterConstant.isFilecheckJobParam(paramId)) {
				// ファイルチェックジョブの変数の場合存在しなかったら空白を返す
				return "";
			}
		}
		// 存在しない場合はパラメタIDにファシリティIDを結合して再度検索を行う。
		
		if (nodeFacilityId != null && !"".equals(nodeFacilityId)) {
			String facilityParamId = getFacilityIdParam(paramId, nodeFacilityId, isEscape);
			paramValue = getReplacedValue(facilityParamId, jobSessionParamsMap, isEscape);
		}
		
		if (paramValue != null) {
			return paramValue;
		}
		
		ArrayList<String> facilityIdList = FacilitySelector.getNodeFacilityIdList(true);
		for (String facilityId : facilityIdList) {
			// ジョブ実行するノードは実施済みのため除外
			if (facilityId.equals(nodeFacilityId)) {
				continue;
			}
			String facilityParamId = getFacilityIdParam(paramId, facilityId, isEscape);
			paramValue = getReplacedValue(facilityParamId, jobSessionParamsMap, isEscape);
			if (paramValue != null) {
				return paramValue;
			}
		}
		
		return paramValue;
	}

	/**
	 * ファシリティIDにジョブ変数が指定されている場合に置換する。
	 * 
	 * @param sessionId
	 * @param facilityId
	 * @return 置換された文字列
	 * @throws JobInfoNotFound
	 */
	public static String replaceFacilityId(String sessionId, String facilityId) throws JobInfoNotFound {
		if (!SystemParameterConstant.isParamFormat(facilityId)){
			return facilityId;
		}
	
		Map<String, String> jobSessionParamsMap = getJobSessionParamsMap(sessionId);
		String paramValue = ParameterUtil.getJobSessionParamValue(
			SystemParameterConstant.getParamId(facilityId), jobSessionParamsMap, sessionId);
		if (paramValue != null) {
			return paramValue;
		} else {
			return facilityId;
		}
	}

	/**
	 * 変数#[RETURN:jobId:facilityId]を置換する。
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param source
	 * @param throwException true:先行ジョブが存在しない場合に例外を発生させる false:発生させない
	 * @return
	 */
	public static String replaceReturnCodeParameter(String sessionId, String jobunitId, String source, boolean throwException)
		throws JobInfoNotFound {
		String regex = REGEX_RETURN;
		Pattern pattern = Pattern.compile(regex);
		String ret = source;
		for (int i = 0; i < 100; i ++) { //無限ループにならないように、上限を定める。
			Matcher matcher = pattern.matcher(ret);
			if (matcher.find()) {
				String rJobId = matcher.group(1);
				String rFacilityId = matcher.group(2);
				try {
					JobSessionNodeEntity node = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, rJobId, rFacilityId);
					Integer endValue = node.getEndValue();
					if (endValue != null) {
						ret = ret.replaceFirst(regex, endValue.toString());
					} else {
						// 先行ジョブが終了していない。
						if (throwException) {
							throw new JobInfoNotFound();
						} else {
							ret = ret.replaceFirst(regex, "null");
						}
					}
				} catch (JobInfoNotFound e) {
					m_log.warn("replaceReturnCodeParameter : jobId=" + rJobId +
							", facilityId=" + rFacilityId);
					// ジョブ、ファシリティIDが存在しない。
					if (throwException) {
						throw new JobInfoNotFound();
					} else {
						ret = ret.replaceFirst(regex, "null");
					}
				}
				/*
				 * for test
				 * 単体試験(このクラスをmainで実行)する際に利用
				 */
				// ret = ret.replaceFirst(regex, "12345");
			} else {
				break;
			}
		}
		return ret;
	}

	/**
	 * 変数#[RPA_EXEC_ENVID:facilityId]を置換する。
	 * @param source
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidRole 
	 * @throws FacilityNotFound 
	 */
	public static String replaceNodeRpaEnvIdParameter(String source) 
			throws FacilityNotFound, InvalidRole, HinemosUnknown {
		String paramId = SystemParameterConstant.RPA_EXEC_ENV_ID;
		ArrayList<String> inKeyList = new ArrayList<>();
		inKeyList.add(paramId);
		String regex = "#\\[" + paramId + ":([^:]*)\\]";
		Pattern pattern = Pattern.compile(regex);
		String ret = source;
		Matcher matcher = pattern.matcher(ret);
		if (matcher.find()) {
			String rFacilityId = matcher.group(1);
			String paramValue = getNodeValue(paramId, rFacilityId, inKeyList);
			if (paramValue != null) {
				// パラメータ値がnull以外の場合に置換処理を行う。
				ret = ret.replaceFirst(regex, paramValue);
				m_log.debug("replaceNodeRpaEnvIdParameter() : source=" + source + ", ret=" + ret);
			} else {
				m_log.warn("replaceNodeRpaEnvIdParameter() : paramValue is null");
			}
		}
		return ret;
	}

	/**
	 * RPAシナリオジョブ（直接実行）のシナリオ実行コマンドの変数を置換します。
	 * 変数#[RPA_TOOL_EXE_FILEPATH]/#[RPA_TOOL_SCENARIO_FILEPATH]/#[RPA_TOOL_OPTIONS]
	 * @param source
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidRole 
	 * @throws FacilityNotFound 
	 */
	public static String replaceRpaToolExecCommandParameter(String source, String exeFilepath, String scenarioFilepath, String options) 
			throws FacilityNotFound, InvalidRole, HinemosUnknown {
		String exeFilepathRegex = "#\\[" + SystemParameterConstant.RPA_TOOL_EXE_FILEPATH + "\\]";
		String scenarioFilepathRegex = "#\\[" + SystemParameterConstant.RPA_TOOL_SCENARIO_FILEPATH + "\\]";
		String optionRegex = "#\\[" + SystemParameterConstant.RPA_TOOL_OPTIONS + "\\]";
		// ファイルパスの"\"をエスケープ
		return source.replaceAll(exeFilepathRegex, StringBinder.escapeStr(exeFilepath))
				.replaceAll(scenarioFilepathRegex, StringBinder.escapeStr(scenarioFilepath))
				.replaceAll(optionRegex, StringBinder.escapeStr(options));
	}

	/**
	 * RPAシナリオジョブ（直接実行）のプロセス終了コマンドの変数を置換します。
	 * 変数#[RPA_TOOL_EXE_FILENAME]
	 * @param source
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidRole 
	 * @throws FacilityNotFound 
	 */
	public static String replaceRpaToolDestroyCommandParameter(String source, String exeFilename) 
			throws FacilityNotFound, InvalidRole, HinemosUnknown {
		String exeFilenameRegex = "#\\[" + SystemParameterConstant.RPA_TOOL_EXE_FILENAME + "\\]";
		// ファイルパスの"\"をエスケープ
		return source.replaceAll(exeFilenameRegex, StringBinder.escapeStr(exeFilename));
	}

	/**
	 * strが#[param]の形式であるかを判定する
	 *
	 * @param str
	 * @param param
	 * @return
	 */
	public static boolean isParamFormat(String str) {
		return SystemParameterConstant.isParamFormat(str);
	}
	
	/**
	 * ジョブ変数（パラメータ形式）からパラメータIDを返却する
	 *
	 * @param paramText
	 * @return
	 */
	public static String getParamId(String paramText){
		return SystemParameterConstant.getParamId(paramText);
	}

	/**
	 * 変数#[END_NUM:jobId]を置換する。
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param source
	 * @param throwException true:先行ジョブが存在しない場合に例外を発生させる false:発生させない
	 * @return
	 */
	public static String replaceEndNumParameter(String sessionId, String jobunitId, String source, boolean throwException)
			throws JobInfoNotFound {
		String regex = REGEX_END_NUM;
		Pattern pattern = Pattern.compile(regex);
		String ret = source;
		for (int i = 0; i < 100; i ++) { //無限ループにならないように、上限を定める。
			Matcher matcher = pattern.matcher(ret);
			if (matcher.find()) {
				String rJobId = matcher.group(1);
				try {
					JobSessionJobEntity job = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, rJobId);
					Integer endValue = job.getEndValue();
					if (endValue != null) {
						ret = ret.replaceFirst(regex, endValue.toString());
					} else {
						// 先行ジョブが終了していない。
						if (throwException) {
							throw new JobInfoNotFound();
						} else {
							ret = ret.replaceFirst(regex, "null");
						}
					}
				} catch (JobInfoNotFound | InvalidRole e) {
					m_log.warn("replaceEndNumParameter : jobId=" + rJobId);
					// 同ジョブユニット配下なのでInvaliedRole対応は不要。
					// ジョブが存在しない。
					if (throwException) {
						throw new JobInfoNotFound();
					} else {
						ret = ret.replaceFirst(regex, "null");
					}
				}
			} else {
				break;
			}
		}
		return ret;
	}

	/**
	 * 引数で受け取った文字列の配列に区切り文字を単純に結合してジョブ変数IDを返却します<BR>
	 * 
	 * @param args
	 * @return
	 */
	public static String createParamIdFrom(String... args) {
		StringBuilder sb = new StringBuilder();
		String deli = "";
		for (String str : args) {
			sb.append(deli);
			sb.append(str);
			deli = SystemParameterConstant.KEY_SEPARATOR;
		}
		return sb.toString();
	}

	public static void main(String args[]) {
		String source = "";
		// source = "ls #[RETURN:jobId1:facilityId1]a";
		source = "ls #[RETURN:jobId1:facilityId1]a -l#[RETURN:jobId2:facilityId2]a";
		System.out.println("source=" + source);
		try {
			System.out.println("replace=" + replaceReturnCodeParameter(null, null, source, false));
		} catch (JobInfoNotFound e) {
			// ここは通らない
		}
	}
}
