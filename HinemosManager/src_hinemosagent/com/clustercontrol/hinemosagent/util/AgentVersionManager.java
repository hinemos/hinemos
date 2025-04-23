/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.hinemosagent.util;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.version.util.VersionUtil;

/**
 * エージェントのバージョン判定に関するクラス
 *
 */
public class AgentVersionManager {
	private static Log logger = LogFactory.getLog(AgentVersionManager.class);

	// TopicInfoに設定するバージョンはここで定義する
	/** バージョン7.0 */
	public static final String VERSION_7_0 = "7.0";

	/** 本バージョン */
	public static final String VERSION_MAJOR = VersionUtil.getVersionMajor();

	private static final Pattern _versionPattern = Pattern.compile("[0-9]{1}\\.[0-9]{1}");

	/**
	 * エージェントのバージョンチェック<BR>
	 * 対象エージェントにそのトピックを送信してよいか判定する<BR>
	 * 
	 * @param targetVersion
	 * @param supportedVersion
	 * @return true:送信対象、false:対象外
	 * @throws InvalidSetting
	 */
	public static boolean checkVersion(String targetVersion, String supportedVersion) {
		if (supportedVersion == null || supportedVersion.isEmpty()) {
			// マネージャ側で送信対象とするバージョンの指定がない場合は全てのバージョンのエージェントを対象とする
			return true;
		}
		if (targetVersion == null || targetVersion.isEmpty() || !_versionPattern.matcher(targetVersion).matches()) {
			// 送信対象とするバージョンの指定があり、判定対象のエージェントのバージョンがない場合
			// あるいは形式外のゴミが入っているなど判定不能の場合は対象外とする
			return false;
		}
		if (!_versionPattern.matcher(supportedVersion).matches()) {
			logger.error("checkVersion() : supportedVersion is invalid formart. supportedVersion=" + supportedVersion);
			// 通常到達しないがマネージャ側で指定するバージョンが形式外の場合は想定外のエージェントに送信しないように対象外とする
			return false;
		}

		Double tgtVer = Double.parseDouble(targetVersion);
		Double sptVer = Double.parseDouble(supportedVersion);
		logger.debug("checkVersion() : targetVersion=" + tgtVer + ", supportedVersion=" + sptVer);
		return (tgtVer >= sptVer);
	}

	/**
	 * エージェントのバージョンチェック<BR>
	 * エージェントとマネージャのバージョンが不正でないかを確認し、各バージョンの一致結果を返す<BR>
	 * 
	 * @param agentVersion
	 * @param managerVersion
	 * @param pattern
	 * @return true:バージョン一致、false:不一致
	 * @throws InvalidSetting
	 */
	public static boolean isSameVersion(String agentVersion, String managerVersion, Pattern pattern) {
		if (agentVersion == null || agentVersion.isEmpty() || !pattern.matcher(agentVersion).matches()) {
			// 判定対象のエージェントのバージョンがない、または形式外の場合は対象外とする。
			return false;
		}
		if (managerVersion == null || managerVersion.isEmpty() || !pattern.matcher(managerVersion).matches()) {
			logger.error("isSameVersion() : managerVersion is invalid.");
			// マネージャのバージョンの指定がない、または形式外の場合は対象外とする。通常は到達しない。
			return false;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("isSameVersion() : agentVersion=" + agentVersion + ", managerVersion=" + managerVersion);
		}
		return agentVersion.equals(managerVersion);
	}

	/**
	 * エージェントのバージョンチェック<BR>
	 * エージェントとマネージャのメジャーバージョンを比較し、対象エージェントをエージェントアップデートの対象とするかを判定する<BR>
	 * エージェントがマネージャのメジャーバージョンと一致する場合、エージェントアップデートの対象とする<BR>
	 * 
	 * @param agentVersion
	 * @param managerVersion
	 * @return true:エージェントアップデート対象、false:対象外
	 * @throws InvalidSetting
	 */
	public static boolean isSameVersionMajor(String agentVersion, String managerVersion) {
		return isSameVersion(agentVersion, managerVersion, _versionPattern);
	}

	/**
	 * バージョンチェック後<BR>
	 * エージェントが該当トピックのサポート対象外だった場合の処理<BR>
	 * 
	 * @param facilityId
	 * @param topic
	 */
	public static void processAfterCheck(String facilityId, TopicInfo topic) {
		if (topic.getRunInstructionInfo() == null) {
			// 現時点ではジョブ以外は想定していないため実行指示がない場合は何もしない
			return;
		}
		RunInstructionInfo instruction = topic.getRunInstructionInfo();
		// INTERNAL通知
		String[] args = new String[] {
				instruction.getSessionId(),
				instruction.getJobId(),
				facilityId,
				topic.getSupportedAgentVersion()
			};
		AplLogger.put(InternalIdCommon.JOB_SYS_031, args);

		// endNode
		RunResultInfo result = new AgtUnsupportedVerRunResultInfo();
		result.setSessionId(instruction.getSessionId());
		result.setJobunitId(instruction.getJobunitId());
		result.setJobId(instruction.getJobId());
		result.setFacilityId(facilityId);
		result.setCommand(instruction.getCommand());
		result.setCommandType(instruction.getCommandType());
		result.setStatus(RunStatusConstant.ERROR);
		result.setMessage(MessageConstant.MESSAGE_JOB_NOT_SUPPORTED_AGENT_VERSION.getMessage());
		result.setErrorMessage("");
		try {
			new JobSessionNodeImpl().endNode(result);
		} catch (InvalidRole | JobInfoNotFound | FacilityNotFound | HinemosUnknown e) {
			logger.warn("processAfterCheckForJob() : endNode failure. (sessionId = " + result.getSessionId()
					+ ", jobId=" + result.getJobId() + ", facilityId = " + result.getFacilityId() + ") "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		} catch (Exception e) {
			logger.error("processAfterCheckForJob() : endNode failure. (sessionId = " + result.getSessionId()
					+ ", jobId=" + result.getJobId() + ", facilityId = " + result.getFacilityId() + ") "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
	}

	// エージェントのサポート対象外バージョン専用の実行結果情報
	private static class AgtUnsupportedVerRunResultInfo extends RunResultInfo {
		private static final long serialVersionUID = 467863993222857510L;

		// 「エージェントのバージョンチェックによって生成されたRunResultInfoを識別する」という目的は、
		// 専用クラスであるということだけで果たせるため、特別な実装はない。
	}

	/**
	 * エージェントのサポート対象外バージョンによるエラーかどうかを判定する
	 * 
	 * @param info
	 * @return
	 */
	public static boolean isUnsupportedVersionError(RunResultInfo info) {
		return (info instanceof AgtUnsupportedVerRunResultInfo);
	}

	/**
	 * 対象エージェントがREST接続であるかどうかを判定する。
	 * 
	 * @param facilityId ファシリティID
	 * @return true / false
	 */
	public static boolean isRestConnetctAgent(String facilityId) {
		String agentVersion = AgentConnectUtil.getAgentVersion(facilityId);
		// バージョン情報が取得できている場合のみREST接続とみなす。
		if( agentVersion == null ){
			return true;
		}
		// バージョン情報が不明な場合非REST接続とみなす。
		return false;
	}
	
}
