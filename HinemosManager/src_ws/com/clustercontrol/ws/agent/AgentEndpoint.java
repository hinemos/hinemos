/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.agent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.custom.bean.CommandExecuteDTO;
import com.clustercontrol.custom.session.MonitorCustomControllerBean;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.JobSessionDuplicate;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.SessionIdLocked;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.bean.AgentJavaInfo;
import com.clustercontrol.hinemosagent.bean.AgentLibMd5s;
import com.clustercontrol.hinemosagent.bean.AgentOutputBasicInfo;
import com.clustercontrol.hinemosagent.bean.HinemosTopicInfo;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.hinemosagent.util.AgentLibraryManager;
import com.clustercontrol.hinemosagent.util.AgentProfile;
import com.clustercontrol.hinemosagent.util.AgentProfiles;
import com.clustercontrol.hinemosagent.util.AgentUpdateList;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.jobmanagement.util.JobFileCheckDuplicationGuard;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Singletons;
import com.clustercontrol.util.WebServiceUtil;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.winevent.session.MonitorWinEventControllerBean;
import com.clustercontrol.ws.util.HashMapInfo;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * ジョブ操作用のWebAPIエンドポイント
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://agent.ws.clustercontrol.com")
public class AgentEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( AgentEndpoint.class );

	/**
	 * echo(WebサービスAPI疎通用)
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	/**
	 * [Agent Base] エージェントからwebサービスで利用。
	 * エージェントから取得要請のあるものをまとめて返す。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @return
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public HinemosTopicInfo getHinemosTopic (AgentInfo agentInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		HinemosTopicInfo hinemosTopicInfo = new HinemosTopicInfo();

		// TopicInfo のリストを設定
        ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(agentInfo);
		AgentProfiles agentProfiles = Singletons.get(AgentProfiles.class);
		for (String facilityId : facilityIdList) {
			// 未登録エージェントの場合は、プロファイル(ライブラリやJavaの情報)を送信するように指示する。
			// ※直下のコードですぐに処理される。
			if (!agentProfiles.hasProfile(facilityId)) {
				TopicInfo topicInfo = new TopicInfo();
				topicInfo.setNewFacilityFlag(true);
				AgentConnectUtil.setTopic(facilityId, topicInfo);
				break;
			}
		}

		ArrayList<TopicInfo> topicInfoList = new ArrayList<TopicInfo>();
		for (String facilityId : facilityIdList) {
			ArrayList<TopicInfo> list = AgentConnectUtil.getTopic(facilityId);
			/*
			 * Agent.propertiesでfacilityIdが直接指定されていない場合、
			 * agentInfoにfacilityIdが含まれていないので、ここで詰める。
			 */
			agentInfo.setFacilityId(facilityId);
			AgentConnectUtil.putAgentMap(agentInfo);
			if (list != null && list.size() != 0) {
				topicInfoList.addAll(list);
			}
		}

		int awakePort = 0;
		for (String facilityId : facilityIdList) {
			try {
				int tmp = NodeProperty.getProperty(facilityId).getAgentAwakePort();
				if (awakePort != 0 && tmp != awakePort) {
					m_log.warn("getHinemosTopic() different awake port " + tmp); 
				}
				awakePort = tmp;
				hinemosTopicInfo.setAwakePort(awakePort);
			} catch (FacilityNotFound e) {
				m_log.info("getHinemosTopic() : FacilityNotFound " + facilityId); 
			}
		}
		hinemosTopicInfo.setTopicInfoList(topicInfoList);

		// SettingUpdateInfo を設定
		hinemosTopicInfo.setSettingUpdateInfo(SettingUpdateInfo.getInstance());

		return hinemosTopicInfo;
	}


	/**
	 * [Agent Base] エージェントからwebサービスで利用。
	 * エージェントがシャットダウンする際に利用。(shutdownhook に登録)
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public void deleteAgent (AgentInfo agentInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

        ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(agentInfo);
		AgentProfiles agentProfiles = Singletons.get(AgentProfiles.class);
		AgentUpdateList agentUpdateList = Singletons.get(AgentUpdateList.class);
		for (String facilityId : facilityIdList) {
			m_log.info("deleteAgent " + facilityId + " is shutdown");
			AgentConnectUtil.deleteAgent(facilityId, agentInfo);
			agentProfiles.removeProfile(facilityId);
			agentUpdateList.release(facilityId);
		}
	}

	/**
	 * [Agent Base] Internalイベントに出力する際に利用する。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param message
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void sendMessage(AgentOutputBasicInfo message)
			throws HinemosUnknown, FacilityNotFound,
			InvalidUserPass, InvalidRole {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

        ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(message.getAgentInfo());
		OutputBasicInfo outputBasicInfo = message.getOutputBasicInfo();
		if (facilityIdList == null || facilityIdList.size() == 0) {
			m_log.info("sendMessage facilityId is null");
			return;
		}
		if (facilityIdList.size() == 0) {
			m_log.info("sendMessage facilityId.size() is 0");
			return;
		}
		AgentConnectUtil.sendMessageLocal(outputBasicInfo, facilityIdList);
	}

	/**
	 * [Job] ジョブの結果を送信する。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param info
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws SessionIdLocked 
	 */
	public boolean jobResult (RunResultInfo info)
			throws HinemosUnknown, JobInfoNotFound,
			InvalidUserPass, InvalidRole, SessionIdLocked {
		
		// ログが見にくなるので、短くして、改行を取り除く
		String command = info.getCommand();
		int length = 32;
		if (command != null) {
			if (length < command.length()){
				command = command.substring(0, length);
			}
			command = command.replaceAll("\n", "");
		}
		
		m_log.info("jobResult : " +
				info.getSessionId() + ", " +
				info.getJobunitId() + ", " +
				info.getJobId() + ", " +
				info.getCommandType() + ", " +
				command + ", " +
				info.getStatus() + ", " +
				info.getFacilityId() + ", "
				);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// セッション処理中の場合リジェクト
		new JobRunManagementBean().checkSessionIdLocked(info);
		
		return new JobRunManagementBean().endNode(info);
	}

	/**
	 * [Logfile] ログファイル監視の監視設定を取得
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getMonitorLogfile (AgentInfo agentInfo) throws MonitorNotFound, HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("getMonitorLogFile : " + agentInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		ArrayList<String> facilityIdList = new ArrayList<String>();

		try {
            facilityIdList.addAll(AgentConnectUtil.getFacilityIds(agentInfo));
		} catch (Exception e) {
			m_log.warn(e,e);
			return null;
		}

		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		MonitorLogfileControllerBean bean = new MonitorLogfileControllerBean();
		for (String facilityId : facilityIdList) {
			list.addAll(bean.getLogfileListForFacilityId(facilityId, true));
		}
		return list;
	}

	/**
	 * [JobFileCheck] ファイルチェック(ジョブ)の設定を取得
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobMasterNotFound
	 */
	public ArrayList<JobFileCheck> getFileCheckForAgent (AgentInfo agentInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole, JobMasterNotFound
	{
		m_log.debug("getFileCheckForAgent : " + agentInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		ArrayList<String> facilityIdList = new ArrayList<String>();

		try {
            facilityIdList.addAll(AgentConnectUtil.getFacilityIds(agentInfo));
		} catch (Exception e) {
			m_log.warn(e,e);
			return null;
		}

		return new JobControllerBean().getJobFileCheck(facilityIdList);
	}


	/**
	 * [JobFileCheck] ファイルチェック(ジョブ)の結果
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param jobFileCheck
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobMasterNotFound
	 * @throws JobInfoNotFound
	 * @throws FacilityNotFound
	 */
	public String jobFileCheckResult (JobFileCheck jobFileCheck, AgentInfo agentInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole, JobMasterNotFound, FacilityNotFound, JobInfoNotFound, JobSessionDuplicate
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		String sessionId = null;
		// 重複ガード
		try (JobFileCheckDuplicationGuard dg = new JobFileCheckDuplicationGuard(jobFileCheck)) {
			String id = jobFileCheck.getId();
			String jobunitId = jobFileCheck.getJobunitId();
			String jobId = jobFileCheck.getJobId();
			String filename = jobFileCheck.getFileName();
			String directory = jobFileCheck.getDirectory();
			Integer eventType = jobFileCheck.getEventType();
			Integer modifyType = jobFileCheck.getModifyType();
			m_log.info("jobFileCheckResult : id=" + id + ", jobunitId=" + jobunitId + ", jobId=" + jobId
					+ ", filename=" + filename + ", directory=" + directory + ", eventType=" + eventType + ", modifyType=" + modifyType
					+ ", uniqueId=" + dg.getUniqueId());
	
			String existingSessionId = dg.getSessionId();
			if (existingSessionId != null) {
				m_log.info("jobFileCheckResult : Detected duplication. jobSessionId=" + existingSessionId);
				return existingSessionId;
			}
	
			JobTriggerInfo trigger = new JobTriggerInfo();
			trigger.setJobkickId(jobFileCheck.getId());
			trigger.setTrigger_type(JobTriggerTypeConstant.TYPE_FILECHECK);
			trigger.setTrigger_info(dg.getTriggerInfo());
			trigger.setFilename(filename);
			trigger.setDirectory(directory);
			OutputBasicInfo output = null;
            for (String facilityId : AgentConnectUtil.getFacilityIds(agentInfo)) {
				ArrayList<String> facilityList =
						FacilitySelector.getFacilityIdList(jobFileCheck.getFacilityId(), jobFileCheck.getOwnerRoleId(), 0, false, false);
				if (facilityList.contains(facilityId)) {
					output = new OutputBasicInfo();
					output.setFacilityId(facilityId);
					try {
						sessionId = new JobControllerBean().runJob(jobunitId, jobId, output, trigger);
					} catch (Exception e) {
						m_log.warn("jobFileCheckResult() : " + e.getMessage());
						String[] args = { jobId, trigger.getTrigger_info() };
						AplLogger.put(InternalIdCommon.JOB_SYS_017, args);
						throw new HinemosUnknown(e.getMessage(), e);
					}
				}
			}
		}
		return sessionId;
	}

	/**
	 * [WinEvent] Windowsイベント監視の監視設定を取得
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getMonitorWinEvent (AgentInfo agentInfo) throws MonitorNotFound, HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("getMonitorWinEvent : " + agentInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		ArrayList<String> facilityIdList = new ArrayList<String>();

		try {
            facilityIdList.addAll(AgentConnectUtil.getFacilityIds(agentInfo));
		} catch (Exception e) {
			m_log.warn(e,e);
			return null;
		}

		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		MonitorWinEventControllerBean bean = new MonitorWinEventControllerBean();
		for (String facilityId : facilityIdList) {
			list.addAll(bean.getWinEventList(facilityId));
		}
		return list;
	}

	/**
	 * [Command Monitor] コマンド実行情報を問い合わせられた場合にコールされるメソッドであり、問い合わせたエージェントが実行すべきコマンド実行情報を返す。
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo メソッドをコールしたエージェント情報
	 * @return コマンド実行情報の一覧
	 * @throws CustomInvalid コマンド監視設定に不整合が見つかった場合にスローされる例外
	 * @throws HinemosUnknown 予期せぬ内部エラーによりスローされる例外
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<CommandExecuteDTO> getCommandExecuteDTO(AgentInfo agentInfo)
			throws CustomInvalid, HinemosUnknown,
			InvalidUserPass, InvalidRole
			{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// Local Variables
		ArrayList<CommandExecuteDTO> dtos = null;
		ArrayList<String> facilityIds = null;

		// MAIN
        facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);
		dtos = new ArrayList<CommandExecuteDTO>();
		MonitorCustomControllerBean monitorCmdCtrl = new MonitorCustomControllerBean();
		for (String facilityId : facilityIds) {
			dtos.addAll(monitorCmdCtrl.getCommandExecuteDTO(facilityId));
		}

		return dtos;
	}

	/**
	 * [Update] (ver.6.2先行版専用)アップデートファイルをダウンロード
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	// TODO: ver.6.2先行版以前のエージェントに接続する必要がなくなったら本メソッドは削除可能
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadModule(String libPath) throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		File file = Singletons.get(AgentLibraryManager.class).getFile(libPath);
		if (file == null) {
			m_log.info("downloadModule: File not found. path=" + libPath);
			return null;
		}

		FileDataSource source = new FileDataSource(file);
		DataHandler dataHandler = new DataHandler(source);
		return dataHandler;
	}

	/**
	 * [Update] (ver.6.2正式版以降用)アップデートファイルをダウンロード
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadAgentLib(String libPath, AgentInfo agentInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

        List<String> facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);

		m_log.debug("downloadAgentLib: libPath=" + libPath + " facilityIds=" + String.join(",", facilityIds));
		
		// 更新中エージェントからの要求でない場合は拒否する
		AgentUpdateList uplist = Singletons.get(AgentUpdateList.class);
		if (!uplist.isUpdating(facilityIds)) {
			throw new HinemosUnknown("Not marked as updating.");
		}
		
		// ファイルオブジェクトを取得する
		File file = Singletons.get(AgentLibraryManager.class).getFile(libPath);
		if (file == null) {
			m_log.info("downloadAgentLib: File not found. path=" + libPath + " facilityIds="
					+ String.join(",", facilityIds));
			return null;
		}
		
		// ダウンロード完了を検知可能にして、DataHandlerを返す。
		FileDataSource source = new FileDataSourceForAgentLibDownload(file, facilityIds);
		DataHandler dataHandler = new DataHandler(source);
		return dataHandler;
	}
	
	// ダウンロード完了を検知(InputStreamのcloseを捕捉)するためのクラス
	private static class FileDataSourceForAgentLibDownload extends FileDataSource {
		private List<String> facilityIds;
		
		public FileDataSourceForAgentLibDownload(File file, List<String> facilityIds) {
			super(file);
			this.facilityIds = facilityIds;

			// 更新中エージェントリストへダウンロード開始を記録
			AgentUpdateList agentUpdateList = Singletons.get(AgentUpdateList.class);
			facilityIds.forEach(facilityId -> agentUpdateList.recordDownloadStart(facilityId));
		}

		@Override
		public InputStream getInputStream() throws IOException {
			InputStream is = super.getInputStream();
			return new InputStream() {
				// 基本的に全処理をFileDataSourceのInputStreamへ委譲する
				@Override public int read(byte[] b) throws IOException { return is.read(b); }
				@Override public int read(byte[] b, int off, int len) throws IOException { return is.read(b, off, len); }
				@Override public long skip(long n) throws IOException { return is.skip(n); }
				@Override public int available() throws IOException { return is.available(); }
				@Override public synchronized void mark(int readlimit) { is.mark(readlimit); }
				@Override public synchronized void reset() throws IOException { is.reset(); }
				@Override public boolean markSupported() { return is.markSupported(); }
				@Override public int read() throws IOException { return is.read(); }

				@Override public void close() throws IOException {
					// これがやりたいだけ
					AgentUpdateList agentUpdateList = Singletons.get(AgentUpdateList.class);
					facilityIds.forEach(facilityId -> agentUpdateList.recordDownloadEnd(facilityId));
					// 例外が出る可能性があるので、closeは最後に呼ぶ
					is.close();
				}
			};
		}
	}

	/**
	 * エージェント側でアップデートに失敗した場合に呼びます。
	 * 
	 * @param agentInfo エージェントの情報。
	 * @param cause 失敗の理由。
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public void cancelUpdate(String cause, AgentInfo agentInfo)
			throws HinemosUnknown, FacilityNotFound, InvalidUserPass, InvalidRole {
		if (m_log.isDebugEnabled()) {
			m_log.debug("cancelUpdate: " + agentInfo.toString() + ", cause:" + cause);
		}

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

        ArrayList<String> facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);
		if (facilityIds.size() == 0) {
			throw new HinemosUnknown("Facility ID not specified.");
		}

		// 通知
		OutputBasicInfo info = new OutputBasicInfo();
		info.setPluginId(HinemosModuleConstant.PLATFORM_REPOSITORY);
		info.setPriority(PriorityConstant.TYPE_WARNING);
		info.setApplication(MessageConstant.AGENT.getMessage());
		info.setMessage(MessageConstant.MESSAGE_AGENT_UPDATE_FAILURE.getMessage());
		info.setMessageOrg(MessageConstant.MESSAGE_AGENT_UPDATE_FAILURE.getMessage() + "(" + cause + ")");
		info.setGenerationDate(HinemosTime.getDateInstance().getTime());
		info.setMonitorId("SYS"); // これ以外を指定すると監視設定を参照しようとしてしまう
		info.setFacilityId(""); // 後でセット
		info.setScopeText(""); // 後でセット
		info.setRunInstructionInfo(null);
		try {
			AgentConnectUtil.sendMessageLocal(info, facilityIds);
		} finally {
			// 更新中リストから当該ノードを除去する。
			Singletons.get(AgentUpdateList.class).release(facilityIds);
		}
	}
	
	/**
	 * [Update] マネージャが保持している、エージェントのライブラリファイルの一覧を取得します。
	 * ver.6.xからのエージェントはエージェントアップデート対象外なので基本的に呼ばれることは無い。
	 * <p>
	 * Java情報が送信されてきている場合、 HinemosJava を含む一覧、
	 * それ以外に対しては空の一覧を返します。<br/>
	 *
	 * @param agentInfo エージェントの情報。
	 * @return ライブラリファイルの一覧。
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<String> getAgentLibMap(AgentInfo agentInfo) throws HinemosUnknown, InvalidRole, InvalidUserPass {
		if (m_log.isDebugEnabled()) {
			m_log.debug("getAgentLibMap: " + agentInfo.toString());
		}

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// ファシリティIDを解決
        List<String> facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);

		// 更新中エージェントからの要求でない場合は拒否する
		AgentUpdateList uplist = Singletons.get(AgentUpdateList.class);
		if (!uplist.isUpdating(facilityIds)) {
			throw new HinemosUnknown("Not marked as updating.");
		}

		// ライブラリファイル一覧を取得
		AgentLibraryManager libMgr = Singletons.get(AgentLibraryManager.class);
		AgentLibMd5s libMd5s = libMgr.getAgentLibMd5s(facilityIds);

		// 更新中エージェントリストへ時刻を記録
		AgentUpdateList agentUpdateList = Singletons.get(AgentUpdateList.class);
		facilityIds.forEach(facilityId -> agentUpdateList.recordLibMapAccessTime(facilityId));
		
		// メソッドの I/F を変更しないように、ArrayList へキャストする。
		// 現在の convertToList は ArrayList を使用する実装であるため、これでも問題は起こらない。
		// convertToList の実装を修正したにもかかわらず、ここの修正を行わなかった場合、ここで ClassCastException が発生する。
		return (ArrayList<String>) WebServiceUtil.convertToList(libMd5s.asMap());
	}

	/**
	 * [Update] (ver.6.2先行版以前用)エージェント側のライブラリファイルの情報をマネージャへ登録します。
	 *
	 * @param filenameMd5 ライブラリファイルの一覧。
	 * @param agentInfo エージェントの情報。
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	// TODO: ver.6.2先行版以前のエージェントに接続する必要がなくなったら本メソッドは削除可能
	public void setAgentLibMd5(ArrayList<String> filenameMd5, AgentInfo agentInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

        List<String> facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);
		AgentLibMd5s libMd5s = new AgentLibMd5s(WebServiceUtil.convertToMap(filenameMd5));

		if (m_log.isDebugEnabled()) {
			m_log.debug(String.format("setAgentLibMd5: agentInfo=%s, facilityIds=[%s]", agentInfo.toString(),
					String.join(",", facilityIds)));
		}

		Singletons.get(AgentProfiles.class).registerProfile(facilityIds, new AgentProfile(libMd5s, null));
	}

	/**
	 * [Update] (ver.6.2正式版以降用)エージェント側のライブラリファイルとJavaの情報をマネージャへ登録します。
	 *
	 * @param filenameMd5 ライブラリファイルの一覧。
	 * @param javaInfo エージェントのJava環境情報。
	 * @param agentInfo エージェントの情報。
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void setAgentProfile(List<String> filenameMd5, AgentJavaInfo javaInfo, AgentInfo agentInfo)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

        List<String> facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);
		AgentLibMd5s libMd5s = new AgentLibMd5s(WebServiceUtil.convertToMap(filenameMd5));

		if (m_log.isDebugEnabled()) {
			m_log.debug(String.format("setAgentProfile: agentInfo=%s, facilityIds=[%s]", agentInfo.toString(),
					String.join(",", facilityIds)));
		}

		Singletons.get(AgentProfiles.class).registerProfile(facilityIds, new AgentProfile(libMd5s, javaInfo));
	}

	/**
	 * [Job] スクリプト情報を取得する
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param jonunitId
	 * @param jonId
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public List<String> getScript(String sessionId, String jobunitId, String jobId)
			throws HinemosUnknown, JobInfoNotFound,
			InvalidUserPass, InvalidRole {
		m_log.debug("getScript : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return new JobControllerBean().getJobScriptInfo(sessionId, jobunitId, jobId);
	}

	/**
	 * [Job] 監視ジョブの監視設定を取得
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param monitorTypeId
	 * @param agentInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public HashMapInfo getMonitorJobMap (String monitorTypeId, AgentInfo agentInfo)
			throws MonitorNotFound, HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("getMonitorJobMap : monitorTypeId=" + monitorTypeId + ", agentInfo=" + agentInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		ArrayList<String> facilityIdList = new ArrayList<String>();

		try {
            facilityIdList.addAll(AgentConnectUtil.getFacilityIds(agentInfo));
		} catch (Exception e) {
			m_log.warn(e,e);
			return null;
		}
		HashMapInfo info = new HashMapInfo();
		JobRunManagementBean bean = new JobRunManagementBean();
		for (String facilityId : facilityIdList) {
			info.getMap8().putAll(bean.getMonitorJobMap(monitorTypeId, facilityId));
		}
		return info;
	}


	/**
	 * [Job] コマンド実行情報を問い合わせられた場合にコールされるメソッドであり、問い合わせたエージェントが実行すべきコマンド実行情報を返す。
	 * ※監視ジョブ用
	 *
	 * HinemosAgentAccess権限が必要
	 *
	 * @param agentInfo メソッドをコールしたエージェント情報
	 * @return コマンド実行情報の一覧
	 * @throws CustomInvalid コマンド監視設定に不整合が見つかった場合にスローされる例外
	 * @throws HinemosUnknown 予期せぬ内部エラーによりスローされる例外
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<CommandExecuteDTO> getCommandExecuteDTOForMonitorJob(AgentInfo agentInfo)
			throws CustomInvalid, HinemosUnknown,
			InvalidUserPass, InvalidRole
			{
		m_log.debug("getCommandExecuteDTOForMonitorJob :  agentInfo=" + agentInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// Local Variables
		ArrayList<CommandExecuteDTO> dtos = null;
		ArrayList<String> facilityIds = null;

		// MAIN
        facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);
		dtos = new ArrayList<CommandExecuteDTO>();
		JobRunManagementBean jobRunManagement = new JobRunManagementBean();
		for (String facilityId : facilityIds) {
			dtos.addAll(jobRunManagement.getCommandExecuteDTOForMonitorJob(facilityId));
		}
		return dtos;
	}

}
