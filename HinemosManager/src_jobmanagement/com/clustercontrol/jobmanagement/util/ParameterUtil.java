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
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.model.JobParamInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.StringBinder;

/**
 * ジョブ変数ユーティリティクラス<BR>
 *
 * @version 3.0.0
 * @since 2.1.0
 */
public class ParameterUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( ParameterUtil.class );
	
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
		// 無効なシステムジョブ変数
		String[] disableStrAry = null;
		
		// ログ出力情報が存在しない場合、処理終了
		if(info == null) {
			return params;
		}
		
		Locale locale = NotifyUtil.getNotifyLocale();
		
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
			params.put(SystemParameterConstant.MESSAGE, HinemosMessage.replace(info.getMessage(), locale));
		}
		
		// オリジナルメッセージ
		if (!ObjectValidator.isEmptyString(info.getMessageOrg()) && !disableParam(SystemParameterConstant.ORG_MESSAGE, disableStrAry)) {
			params.put(SystemParameterConstant.ORG_MESSAGE, HinemosMessage.replace(info.getMessageOrg(), locale));
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
		// 無効なシステムジョブ変数
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
	 * セッションIDからジョブ変数の置換文字列用Mapを取得します。
	 *
	 * @param sessionId ジョブセッションID
	 * @return 置換文字列用Map
	 */
	public static Map<String, String> getJobSessionParamsMap(String sessionId) throws JobInfoNotFound {
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
		
		if (isContainParamOrParamOrg(paramIdList, SystemParameterConstant.SESSION_ID)) {
			params.put(SystemParameterConstant.SESSION_ID, sessionId);
		}
		
		if (isContainParamOrParamOrg(paramIdList, SystemParameterConstant.START_DATE) ||
				isContainParamOrParamOrg(paramIdList, SystemParameterConstant.TRIGGER_TYPE) ||
				isContainParamOrParamOrg(paramIdList, SystemParameterConstant.TRIGGER_INFO)) {
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
		
		if (isContainParamOrParamOrg(paramIdList, SystemParameterConstant.START_DATE)) {
			params.put(SystemParameterConstant.START_DATE, df.format(jobSessionEntity.getScheduleDate()));
		}
		if (isContainParamOrParamOrg(paramIdList, SystemParameterConstant.TRIGGER_TYPE)) {
			params.put(SystemParameterConstant.TRIGGER_TYPE, Messages.getString(JobTriggerTypeConstant.typeToMessageCode(jobSessionEntity.getTriggerType()), locale));
		}
		
		if (isContainParamOrParamOrg(paramIdList, SystemParameterConstant.TRIGGER_INFO)) {
			params.put(SystemParameterConstant.TRIGGER_INFO, jobSessionEntity.getTriggerInfo());
		}
		return params;
	}

	private static boolean isContainParamOrParamOrg(List<String> paramIdList, String paramName) {
		if (paramIdList.contains(paramName)) {
			return true;
		}
		if (paramIdList.contains(paramName + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE) ) {
			return true;
		}
		
		return false;
	}

	/**
	 * ジョブ変数を置換する
	 * 
	 * @param paramId ジョブ変数
	 * @param paramMap ジョブ変数の編集元Map
	 * @param isEscape エスケープするか
	 * @return
	 */
	public static String getReplacedValue(String paramId, Map<String, String> paramMap, boolean isEscape) {
		String ret = null;
		
		if (paramId == null || paramMap == null) {
			return ret;
		}
		
		if (isEscape) {
			ret = paramMap.get(paramId);
			
			if (ret != null) {
				return StringBinder.escapeStr(ret);
			}
			
			String notOrignalParamId = SystemParameterConstant.getNotOriginalParam(paramId);
			
			if (notOrignalParamId != null) {
				ret = paramMap.get(notOrignalParamId);
			}
		} else {
			ret = paramMap.get(paramId);
			
			if (ret != null) {
				return ret;
			}
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
	public static String getJobSessionParamValue(String paramId, Map<String, String> jobSessionParamsMap, String nodeFacilityId) {
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

	public static void main(String args[]) {
		String source = "";
		// source = "ls #[RETURN:jobId1:facilityId1]a";
		source = "ls #[RETURN:jobId1:facilityId1]a -l#[RETURN:jobId2:facilityId2]a";
		System.out.println("source=" + source);
		System.out.println("replace=" + replaceReturnCodeParameter(null, null, source));
	}
}
