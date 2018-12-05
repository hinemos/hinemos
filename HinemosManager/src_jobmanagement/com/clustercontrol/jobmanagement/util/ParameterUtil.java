/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.ObjectValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;

/**
 * ジョブ変数ユーティリティクラス<BR>
 *
 * @version 3.0.0
 * @since 2.1.0
 */
public class ParameterUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ParameterUtil.class );
	
	/** 無効なジョブ変数のリスト */
	private static final String PARAM_JOB_PARAM_DISABLE = "job.param.disable";

	/**
	 * 監視管理の情報をログ出力情報から取得します。
	 * ジョブセッション作成時に、ジョブ変数に対応する値を取得するために使用します。
	 *
	 * @param info ログ出力情報
	 * @return 値
	 */
	public static Map<String, String> createParamInfo(OutputBasicInfo info) {
		// 戻り値
		Map<String, String> params = new HashMap<String, String>();
		// 無効なジョブ変数
		String[] disableStrAry = null;
		
		// ログ出力情報が存在しない場合、処理終了
		if(info == null) {
			return params;
		}
		
		// ファシリティID
		if (!ObjectValidator.isEmptyString(info.getFacilityId()) && !disableParam(SystemParameterConstant.FACILITY_ID, disableStrAry)) {
			params.put(SystemParameterConstant.FACILITY_ID, info.getFacilityId());
		}
		
		// プラグインID
		if (!ObjectValidator.isEmptyString(info.getPluginId()) && !disableParam(SystemParameterConstant.PLUGIN_ID, disableStrAry)) {
			params.put(SystemParameterConstant.PLUGIN_ID, info.getPluginId());
		}
		
		// 監視項目ID
		if (!ObjectValidator.isEmptyString(info.getMonitorId()) && !disableParam(SystemParameterConstant.MONITOR_ID, disableStrAry)) {
			params.put(SystemParameterConstant.MONITOR_ID, info.getMonitorId());
		}
		
		// 監視詳細
		if (!ObjectValidator.isEmptyString(info.getSubKey()) && !disableParam(SystemParameterConstant.MONITOR_DETAIL_ID, disableStrAry)) {
			params.put(SystemParameterConstant.MONITOR_DETAIL_ID, info.getSubKey());
		}
		
		// アプリケーション
		if (!ObjectValidator.isEmptyString(info.getApplication()) && !disableParam(SystemParameterConstant.APPLICATION, disableStrAry)) {
			params.put(SystemParameterConstant.APPLICATION, info.getApplication());
		}
		
		// 重要度
		if (!disableParam(SystemParameterConstant.PRIORITY, disableStrAry)) { 
			params.put(SystemParameterConstant.PRIORITY, String.valueOf(info.getPriority()));
		}

		// メッセージ
		if (!ObjectValidator.isEmptyString(info.getMessage()) && !disableParam(SystemParameterConstant.MESSAGE, disableStrAry)) {
			Locale locale = NotifyUtil.getNotifyLocale();
			params.put(SystemParameterConstant.MESSAGE, HinemosMessage.replace(info.getMessage(), locale));
		}
		
		// オリジナルメッセージ
		if (!ObjectValidator.isEmptyString(info.getMessageOrg()) && !disableParam(SystemParameterConstant.ORG_MESSAGE, disableStrAry)) {
			params.put(SystemParameterConstant.ORG_MESSAGE, info.getMessageOrg());
		}

		return params;
	}

	/**
	 * ジョブ契機（ファイルチェック）情報を設定します。
	 * ジョブセッション作成時に、ジョブ変数に対応する値を取得するために使用します。
	 *
	 * @param info ログ出力情報
	 * @return 値
	 */
	public static Map<String, String> createParamInfo(JobTriggerInfo info) {
		// 戻り値
		Map<String, String> params = new HashMap<String, String>();
		// 無効なジョブ変数
		String[] disableStrAry = null;

		// ジョブ契機（ファイルチェック）情報が存在しない場合、処理終了
		if(info == null) {
			return params;
		}
		
		// ファイル名
		if (!ObjectValidator.isEmptyString(info.getFilename()) && !disableParam(SystemParameterConstant.FILENAME, disableStrAry)) {
			params.put(SystemParameterConstant.FILENAME, info.getFilename());
		}
		
		// ディレクトリ
		if (!ObjectValidator.isEmptyString(info.getDirectory()) && !disableParam(SystemParameterConstant.DIRECTORY, disableStrAry)) {
			params.put(SystemParameterConstant.DIRECTORY, info.getDirectory());
		}

		return params;
	}

	/**
	 * ジョブパラメータ情報からパラメータ値を取得します。
	 *
	 * @param paramId パラメータID
	 * @param sessionId ジョブセッションID
	 * @param jobSessionParams ジョブパラメータ情報
	 * @return パラメータ値
	 */
	public static String getJobSessionParamValue(String paramId, String sessionId, Map<String, String> jobSessionParams) 
			throws JobInfoNotFound {

		m_log.debug("getJobSessionParamValue() start paramId=" + paramId + ",sessionId=" + sessionId);
		String ret = null;

		m_log.debug("getting parameters of job session... (session_id = " + sessionId + ")" );
		if (jobSessionParams == null) {
			jobSessionParams = new HashMap<String, String>();
			
			HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
			Collection<JobParamInfoEntity> collection 
				= em.createNamedQuery("JobParamInfoEntity.findBySessionId", JobParamInfoEntity.class)
					.setParameter("sessionId", sessionId)
					.getResultList();
			if (collection == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobParamInfoEntity.findBySessionId"
						+ ", sessionId = " + sessionId);
				m_log.info("getJobSessionParamValue() : " + je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
			if(collection.size() > 0){
				Iterator<JobParamInfoEntity> itr = collection.iterator();
				while(itr.hasNext()){
					JobParamInfoEntity param = itr.next();
					jobSessionParams.put(param.getId().getParamId(), param.getValue());
				}
			}
		}

		// 変換する。
		if (jobSessionParams.containsKey(paramId)) { 
			ret = jobSessionParams.get(paramId) == null ? "" : jobSessionParams.get(paramId) ;
		}

		m_log.debug("getJobSessionParamValue() end paramId=" + paramId + ",sessionId=" + sessionId + ",value=" + ret);
		return ret;
	}

	/**
	 *
	 * ジョブセッション情報からパラメータ値を取得します。
	 *
	 * @param paramId パラメータID
	 * @param sessionId ジョブセッションID
	 * @param jobSessionEntity ジョブセッション情報
	 * @return パラメータ値
	 * @throws JobInfoNotFound
	 */
	private static String getJobSessionValue(String paramId, String sessionId, JobSessionEntity jobSessionEntity) 
			throws JobInfoNotFound{

		m_log.debug("getJobSessionValue() start paramId=" + paramId + ",sessionId=" + sessionId);
		String ret = null;

		if (paramId.equals(SystemParameterConstant.SESSION_ID)){
			// セッションID
			ret = sessionId;
		} else {
			if (jobSessionEntity == null) {
				HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

				// ジョブセッションより値を取得する
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

			if(paramId.equals(SystemParameterConstant.START_DATE)){
				// セッション開始日時
				DateFormat df = DateFormat.getDateTimeInstance();
				df.setTimeZone(HinemosTime.getTimeZone());
				ret = df.format(jobSessionEntity.getScheduleDate());

			} else if (paramId.equals(SystemParameterConstant.TRIGGER_TYPE)){
				// ジョブの実行契機種別
				Locale locale = NotifyUtil.getNotifyLocale();
				ret = Messages.getString(JobTriggerTypeConstant.typeToMessageCode(jobSessionEntity.getTriggerType()), locale);

			} else if (paramId.equals(SystemParameterConstant.TRIGGER_INFO)){
				// ジョブの実行契機情報
				ret = jobSessionEntity.getTriggerInfo();

			}
		}
		m_log.debug("getJobSessionValue() end paramId=" + paramId + ",sessionId=" + sessionId + ",value=" + ret);
		return ret;
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
	private static String getNodeValue(String paramId, String facilityId, Map<String, String> nodeParams)
			throws HinemosUnknown, FacilityNotFound, InvalidRole {

		m_log.debug("getNodeValue() start paramId=" + paramId + ",facilityId=" + facilityId);
		String ret = null;

		if (facilityId != null && !facilityId.isEmpty()) {
			if (paramId.equals(SystemParameterConstant.FACILITY_ID)){
				// セッションID
				ret = facilityId;
			} else {
				if (new RepositoryControllerBean().isNode(facilityId)) {
					if (nodeParams == null) {
						// ノードプロパティを取得
						NodeInfo nodeInfo = new RepositoryControllerBean().getNode(facilityId);
						nodeParams = RepositoryUtil.createNodeParameter(nodeInfo);
					}
					if (nodeParams.get(paramId) != null) {
						ret = nodeParams.get(paramId);
					}
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
			String disableStr = HinemosPropertyUtil.getHinemosPropertyStr(PARAM_JOB_PARAM_DISABLE, "");
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
	 * @param sessionId セッションID
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
		// ジョブセッション情報用変数
		JobSessionEntity jobSessionEntity = null;
		// ノードセッション情報用変数
		Map<String, String> nodeParams = null;
		// ジョブパラメータ情報用変数
		Map<String, String> jobSessionParams = null;
		
		// 存在するパラメータ分処理を行う。
		for (String paramId : list) {
			String paramValue = null;
			// ジョブパラメータ情報、ユーザ情報
			paramValue = getJobSessionParamValue(paramId, sessionId, jobSessionParams);
			// 存在しない場合はパラメタIDにファシリティIDを結合して再度検索を行う。
			if (paramValue == null) {
				paramValue = getJobSessionParamValue(paramId + ":" + facilityId, sessionId, jobSessionParams);
				if (paramValue == null) {
					ArrayList<String> facilityIdList = FacilitySelector.getNodeFacilityIdList(true);
					for (String str : facilityIdList) {
						// ジョブ実行するノードは実施済みのため除外
						if (str.equals(facilityId)) continue;
						paramValue = getJobSessionParamValue(paramId + ":" + str, sessionId, jobSessionParams);
						if (paramValue != null) break;
					}
				}
			}
			// ジョブ変数（システム）
			if (paramValue == null) {
				if (!disableParam(paramId, disableStrAry)) {
					if (Arrays.asList(SystemParameterConstant.SYSTEM_ID_LIST_JOB_SESSION).contains(paramId)) {
						// ジョブセッション情報
						paramValue = getJobSessionValue(paramId, sessionId, jobSessionEntity);
					} else {
						// ノード情報
						paramValue = getNodeValue(paramId, facilityId, nodeParams);
					}
				}
			}
			if (paramValue != null) {
				// パラメータ値がnull以外の場合に置換処理を行う。
				commandConv = commandConv.replace(SystemParameterConstant.getParamText(paramId), paramValue);
			}
		}

		m_log.debug("successful in generating command string... (session_id = " + sessionId + ", facility_id = " + facilityId + ", command_orig = " + commandOrig + ", command_conv = " + commandConv + ")");
		return commandConv;
	}


	/**
	 * 変数#[RETURN:jobId:facilityId]を置換する。
	 * @param sessionId
	 * @param jobunitId
	 * @param jobId
	 * @param source
	 * @return
	 */
	public static String replaceReturnCodeParameter(String sessionId, String jobunitId, String source) {
		String regex = "#\\[RETURN:([^:]*):([^:]*)\\]";
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
						ret = ret.replaceFirst(regex, "null");
					}
				} catch (JobInfoNotFound e) {
					m_log.warn("replaceReturnCodeParameter : jobId=" + rJobId +
							", facilityId=" + rFacilityId);
					// ジョブ、ファシリティIDが存在しない。
					ret = ret.replaceFirst(regex, "null");
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

	public static void main(String args[]) {
		String source = "";
		// source = "ls #[RETURN:jobId1:facilityId1]a";
		source = "ls #[RETURN:jobId1:facilityId1]a -l#[RETURN:jobId2:facilityId2]a";
		System.out.println("source=" + source);
		System.out.println("replace=" + replaceReturnCodeParameter(null, null, source));
	}
}
