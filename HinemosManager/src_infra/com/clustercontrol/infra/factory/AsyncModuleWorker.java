/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.infra.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InfraModuleNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.SessionNotFound;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.ModuleNodeResult;
import com.clustercontrol.infra.bean.ModuleResult;
import com.clustercontrol.infra.bean.ModuleTypeConstant;
import com.clustercontrol.infra.bean.OkNgConstant;
import com.clustercontrol.infra.bean.RunCheckTypeConstant;
import com.clustercontrol.infra.model.CommandModuleInfo;
import com.clustercontrol.infra.model.FileTransferModuleInfo;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.jobmanagement.factory.CreateSessionId;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * 環境構築機能の実行およびチェックを行う管理を行う
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class AsyncModuleWorker {
	
	private static Log m_log = LogFactory.getLog( AsyncModuleWorker.class );

	private static ExecutorService executor;
	
	static {
		int threadSize = HinemosPropertyUtil.getHinemosPropertyNum("infra.run.thread", Long.valueOf(16)).intValue();
		int queueSize = threadSize * 5;
		executor = new MonitoredThreadPoolExecutor(
			threadSize, // coreSize
			threadSize, // maxSize
			0L, // keepAliveTime
			TimeUnit.MICROSECONDS,
			new LinkedBlockingQueue<Runnable>(queueSize),
			new ThreadFactory() {
				private volatile int _count = 0;
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "Infra-" + _count++);
				}
			},
			new RejectedExecutionHandler() {
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					m_log.warn("InfraModule task is rejected. " + r.toString());
				}
			}
		);
	}

	/**
	 * 環境構築タスクの基底クラス。
	 * 通知メッセージ用の API を実装。
	 */
	private static abstract class AbstarctInfraTask implements Callable<ModuleNodeResult> {
		protected InfraManagementInfo management;
		protected NodeInfo node;
		protected AccessInfo access;

		public AbstarctInfraTask(InfraManagementInfo management, NodeInfo node, AccessInfo access) {
			this.management = management;
			this.node = node;
			this.access = access;
		}
		
		public abstract ModuleNodeResult invoke() throws Exception;

		public abstract void unexpectedError(Exception e) throws Exception;

		@Override
		public ModuleNodeResult call() throws Exception {
			ModuleNodeResult ret = null;
			try {
				ret = invoke();
			} catch (HinemosUnknown e) {
				m_log.warn("call() : " + e.getMessage(), e);
				unexpectedError(e);
				throw e;
			} catch (Exception e) {
				m_log.warn("call() : " + e.getMessage(), e);
				unexpectedError(e);
				throw e;
			}
			return ret;
		}

		protected void notifyManagementMessageWithSubKey(String subkey, int priority, String msg, String msgOrg) {
			notifyMessage(management.getManagementId(), subkey, node.getFacilityId(), priority, msg, msgOrg);
		}
		
		// 開始の通知
		protected void notifyModuleCheckStart(InfraModuleInfo<?> module) {
			String msg = null;
			msg = MessageConstant.MESSAGE_MODULE_CHECK_BEGINS.getMessage();
			notifyManagementMessageWithSubKey(module.getModuleId(), management.getStartPriority(), msg, msg);
		}
		
		// 終了の通知
		protected void notifyModuleCheckResult(InfraModuleInfo<?> module, ModuleNodeResult result) {
			int priority = 0;
			String msg = null;
			switch (result.getResult()) {
			case OkNgConstant.TYPE_OK:
				priority = management.getNormalPriorityCheck();
				msg = MessageConstant.MESSAGE_MODULE_CHECK_IS_COMPLETED_SUCCESSFULLY.getMessage();
				break;
			case OkNgConstant.TYPE_NG:
			default:
				priority = management.getAbnormalPriorityCheck();
				msg = MessageConstant.MESSAGE_MODULE_CHECK_FAILED.getMessage();
				break;
			}
			notifyManagementMessageWithSubKey(module.getModuleId(), priority, msg, result.getMessage());
		}
		
		// 開始の通知
		protected void notifyModuleRunStart(InfraModuleInfo<?> module) {
			String msg = null;
			msg = MessageConstant.MESSAGE_MODULE_EXECUTION_BEGINS.getMessage();
			notifyManagementMessageWithSubKey(module.getModuleId(), management.getStartPriority(), msg, msg);
		}
		
		// 終了の通知
		protected void notifyModuleRunResult(InfraModuleInfo<?> module, ModuleNodeResult result) {
			int priority = 0;
			String msg = null;
			switch (result.getResult()) {
			case OkNgConstant.TYPE_OK:
				priority = management.getNormalPriorityRun();
				msg = MessageConstant.MESSAGE_MODULE_EXECUTION_IS_COMPLETED_SUCCESSFULLY.getMessage();
				break;
			case OkNgConstant.TYPE_NG:
			default:
				priority = management.getAbnormalPriorityRun();
				msg = MessageConstant.MESSAGE_MODULE_EXECUTION_FAILED.getMessage();
				break;
			}
			notifyManagementMessageWithSubKey(module.getModuleId(), priority, msg, result.getMessage());
		}
		
		public void notifyMessage(
			String monitorId,
			String subKey,
			String facilityId,
			int priority,
			String msg,
			String msgOrg
			) {

			OutputBasicInfo info = createOutputBasicInfo(
				HinemosModuleConstant.INFRA,
				management.getManagementId(),
				subKey,
				facilityId,
				null,
				"", // application
				priority,
				msg,
				msgOrg,
				HinemosTime.getDateInstance().getTime()
				);

			try {
				new NotifyControllerBean().notify(info, management.getNotifyGroupId());
			} catch (HinemosUnknown e) { 
				m_log.warn("notifyMessage : HinemosUnknown " + e.getMessage());
			}
		}
		
		private OutputBasicInfo createOutputBasicInfo(
			String pluginId,
			String monitorId,
			String subKey,
			String facilityId,
			String facilityPath,
			String application,
			int priority,
			String message,
			String messageOrg,
			Long generationDate) {
			OutputBasicInfo output = new OutputBasicInfo();

			// 通知情報を設定
			output.setPluginId(pluginId);
			output.setMonitorId(monitorId);
			output.setApplication(application);

			// 通知抑制用のサブキーを設定。
			output.setSubKey(subKey);

			output.setFacilityId(facilityId);

			if (facilityPath == null) {
				try {
					facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
				}
				catch (Exception e) {
					Logger.getLogger(this.getClass()).error(e.getMessage(), e);
				}
			}
			output.setScopeText(facilityPath);

			output.setPriority(priority);
			output.setMessage(message);
			output.setMessageOrg(messageOrg);
			output.setGenerationDate(generationDate);

			return output;
		}
	}
	

	// 環境構築モジュールに対して実行
	private static class ModuleRunTask extends AbstarctInfraTask {
		private InfraModuleInfo<?> module;
		private String sessionId;
		
		public ModuleRunTask(InfraManagementInfo management, InfraModuleInfo<?> module, NodeInfo node, AccessInfo access, String sessionId) {
			super(management, node, access);
			this.module = module;
			this.sessionId = sessionId;
		}
		
		@Override
		public ModuleNodeResult invoke() throws Exception {
			m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "start", "run", management.getManagementId(), module.getModuleId()));

			if (!management.getValidFlg() || !module.getValidFlg()) {
				m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "disable", "run", management.getManagementId(), module.getModuleId()));
				ModuleNodeResult r = new ModuleNodeResult(node.getFacilityId(), OkNgConstant.TYPE_NG, -1, "invalid");
				r.setRunCheckType(RunCheckTypeConstant.TYPE_RUN);
				return r;
			}

			ModuleNodeResult result = null;
			if (module.getPrecheckFlg()) {
				result = module.check(management, node, access, sessionId, false);
				result.setRunCheckType(RunCheckTypeConstant.TYPE_PRECHECK);
				if (result.getResult() == OkNgConstant.TYPE_OK) {
					return result;
				}
			}
			notifyModuleRunStart(module);
			result = module.run(management, node, access, sessionId);
			result.setRunCheckType(RunCheckTypeConstant.TYPE_RUN);
			notifyModuleRunResult(module, result);
			
			if (m_log.isDebugEnabled()) {
				switch (result.getResult()) {
				case OkNgConstant.TYPE_OK:
					m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "success", "run", management.getManagementId(), module.getModuleId()));
					break;
				case OkNgConstant.TYPE_NG:
				default:
					m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "fail", "run", management.getManagementId(), module.getModuleId()));
					break;
				}
			}

			m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "end", "run", management.getManagementId(), module.getModuleId()));
			return result;
		}
		
		@Override
		public void unexpectedError(Exception e) throws Exception {
			notifyManagementMessageWithSubKey(module.getModuleId(), management.getAbnormalPriorityRun(), MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED.getMessage(), e.getMessage());
		}
	}

	// 環境構築モジュールに対してチェック
	private static class ModuleCheckTask extends AbstarctInfraTask {
		private InfraModuleInfo<?> module;
		boolean verbose;
		String sessionId;
		
		public ModuleCheckTask(InfraManagementInfo management, InfraModuleInfo<?> module, NodeInfo node, AccessInfo access, String sessionId, boolean verbose) {
			super(management, node, access);
			this.module = module;
			this.verbose = verbose;
			this.sessionId = sessionId;
		}
		
		@Override
		public ModuleNodeResult invoke() throws Exception {
			m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "start", "check", management.getManagementId(), module.getModuleId()));
			
			if (!management.getValidFlg() || !module.getValidFlg()) {
				m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "disable", "check", management.getManagementId(), module.getModuleId()));
				ModuleNodeResult r = new ModuleNodeResult(node.getFacilityId(), OkNgConstant.TYPE_NG, -1, "invalid");
				r.setRunCheckType(RunCheckTypeConstant.TYPE_CHECK);
				return r;
			}

			notifyModuleCheckStart(module);
			ModuleNodeResult result = module.check(management, node, access, sessionId, verbose);
			result.setRunCheckType(RunCheckTypeConstant.TYPE_CHECK);
			notifyModuleCheckResult(module, result);
			
			if (m_log.isDebugEnabled()) {
				switch (result.getResult()) {
				case OkNgConstant.TYPE_OK:
					m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "success", "check", management.getManagementId(), module.getModuleId()));
					break;
				case OkNgConstant.TYPE_NG:
				default:
					m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "fail", "check", management.getManagementId(), module.getModuleId()));
					break;
				}
			}

			m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "end", "check", management.getManagementId(), module.getModuleId()));
			return result;
		}
		
		@Override
		public void unexpectedError(Exception e) throws Exception {
			notifyManagementMessageWithSubKey(module.getModuleId(), management.getAbnormalPriorityCheck(),  MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED.getMessage(), e.getMessage());
		}
	}
	
	private static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

	private static class Session{
		InfraManagementInfo management = null;
		List<InfraModuleInfo<?>> moduleList = new ArrayList<>();
		List<AccessInfo> accessList = null;

		/*
		 * 「実行コマンドのリターンコードが0以外の場合、後続モジュールを実行しない。」にチェックを入れており、
		 * 実行コマンドのリターンコードが0以外の場合は skipNodeList に追加される。
		 * skipNodeListに追加された場合、後続モジュールすべてを実行しない。
		 */
		List<String> skipNodeList = new ArrayList<>();

		private Session (String managementId, List<String> moduleIdList, List<AccessInfo> accessList)
				throws InfraManagementNotFound, InfraModuleNotFound, InvalidRole, HinemosUnknown {
			management = new SelectInfraManagement().get(managementId, ObjectPrivilegeMode.EXEC);
			for (String moduleId : moduleIdList) {
				InfraModuleInfo<?> module = null;
				boolean flag = true;
				for (InfraModuleInfo<?> m: management.getModuleList()) {
					if (m.getModuleId().equals(moduleId)) {
						module = m;
						flag = false;
						break;
					}
				}
				if (flag) {
					throw new InfraModuleNotFound(managementId, moduleId);
				}
				if (module.getValidFlg()) {
					moduleList.add(module);
				}
			}
			if (moduleList.size() == 0) {
				throw new InfraModuleNotFound(managementId, "");
			}
			this.accessList = accessList;
		}
	}

	public static String createSession(String managementId, List<String> moduleIdList, List<AccessInfo> accessList)
			throws InfraManagementNotFound, InfraModuleNotFound, InvalidRole, HinemosUnknown, FacilityNotFound, InvalidSetting {
		
		InfraManagementInfo management = new SelectInfraManagement().get(managementId, ObjectPrivilegeMode.EXEC);
		String roleId = management.getOwnerRoleId();
		
		for (AccessInfo access : accessList) {
			String facilityId = access.getFacilityId();
			
			// 参照権限のチェック
			FacilityTreeCache.validateFacilityId(facilityId, roleId, true);
			
			NodeInfo node = NodeProperty.getProperty(facilityId);
			// accessがfacilityIdしか持っていない場合は、ノードプロパティを参照する
			if (access.getSshUser() == null &&
				access.getSshPassword() == null &&
				access.getSshPrivateKeyFilepath() == null &&
				access.getSshPrivateKeyPassphrase() == null &&
				access.getWinRmUser() == null &&
				access.getWinRmPassword() == null
				) {
				access.setSshUser(node.getSshUser());
				access.setSshPassword(node.getSshUserPassword());
				access.setSshPrivateKeyFilepath(node.getSshPrivateKeyFilepath());
				access.setSshPrivateKeyPassphrase(node.getSshPrivateKeyPassphrase());
				access.setWinRmUser(node.getWinrmUser());
				access.setWinRmPassword(node.getWinrmUserPassword());
			}
			access.setSshPort(node.getSshPort());
			access.setSshTimeout(node.getSshTimeout());
			access.setWinRmPort(node.getWinrmPort());
			access.setWinRmTimeout(node.getWinrmTimeout());
		}
		
		String sessionId = "INFRA_" + CreateSessionId.create();
		sessionMap.put(sessionId, new Session(managementId, moduleIdList, accessList));
		if (sessionMap.size() > 10) {
			m_log.warn("size of sessionMap is too large " + sessionMap.size());
		}
		return sessionId;
	}

	/**
	 * 削除できたら(削除対象のsessionが存在したら)、trueを返す。
	 * true : 削除成功(Session途中の場合に呼び出すと、このステータスとなる。)
	 * false : 削除失敗(Sessionが最後まで到達している場合は、削除済みとなっているため、削除失敗のステータスとなる)
	 * @param sessionId
	 * @return
	 * @throws InfraManagementNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static boolean deleteSession(String sessionId) throws InfraManagementNotFound, InvalidRole, HinemosUnknown {
		
		Session session = sessionMap.get(sessionId);
		if (session == null) {
			return false;
		}
		
		// このセッションを触ってよいかチェック。
		new SelectInfraManagement().get(session.management.getManagementId(), ObjectPrivilegeMode.EXEC);
		
		sessionMap.remove(sessionId);
		return true;
	}
	
	public static ModuleResult runInfraModule(String sessionId)
			throws HinemosUnknown, SessionNotFound, InfraManagementNotFound, InvalidRole {
		// セッションの取得
		Session session = sessionMap.get(sessionId);
		if (session == null) {
			throw new SessionNotFound(sessionId);
		}
		
		List<AccessInfo> accessList = session.accessList;
		InfraManagementInfo management = session.management;
		List<InfraModuleInfo<?>> moduleList = session.moduleList;
		List<String> skipNodeList = session.skipNodeList;
		InfraModuleInfo<?> module = moduleList.get(0);
		
		// このセッションを触ってよいかチェック。
		new SelectInfraManagement().get(management.getManagementId(), ObjectPrivilegeMode.EXEC);

		moduleList.remove(0);
		
		// 返り値
		ModuleResult ret = new ModuleResult(sessionId, module.getModuleId());
		if (moduleList.size() == 0) {
			ret.setHasNext(false);
			deleteSession(sessionId);
		} else {
			ret.setHasNext(true);
		}
		
		module.beforeRun(sessionId);

		try {
			// 実行
			RepositoryControllerBean repository = new RepositoryControllerBean();
			List<Future<ModuleNodeResult>> futureList = new ArrayList<Future<ModuleNodeResult>>();
			List<String> runList = new ArrayList<>();
			for (AccessInfo access: accessList) {
				String facilityId = access.getFacilityId();
				if (skipNodeList.contains(facilityId)) {
					ModuleNodeResult result = new ModuleNodeResult(facilityId, OkNgConstant.TYPE_SKIP, -1, "");
					result.setRunCheckType(RunCheckTypeConstant.TYPE_RUN);
					ret.getModuleNodeResultList().add(result);
					continue;
				}
				NodeInfo node;
				try {
					node = repository.getNode(facilityId);
					Future<ModuleNodeResult> future = executor.submit(new ModuleRunTask(management, module, node, access, sessionId));
					futureList.add(future);
					runList.add(facilityId);
				}
				catch (FacilityNotFound e) {
					m_log.debug(e.getMessage(), e);
				}
			}
	
			// 実行結果の取得
			long timeout = HinemosPropertyUtil.getHinemosPropertyNum("infra.run.timeout", Long.valueOf(5 * 60 * 1000));
			int i = 0;
			for (Future<ModuleNodeResult> future : futureList) {
				String facilityId = runList.get(i);
				ModuleNodeResult result = null;
				String message = null;
				try {
					result = future.get(timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException | ExecutionException e) {
					message = e.getCause().getMessage();
					if (message == null) {
						message = e.getClass().getName() + ":" + e.getMessage();
					}
					m_log.warn("runInfraModule : " + e.getMessage(), e);
				} catch (Exception e) {
					message = e.getMessage();
					if (message == null) {
						message = e.getClass().getName();
					}
				}
				if (message != null) {
					m_log.warn("runInfraModule : " + message);
					result = new ModuleNodeResult(facilityId, OkNgConstant.TYPE_NG, -1, message);
					result.setRunCheckType(RunCheckTypeConstant.TYPE_RUN);
				}
				if (result == null) {
					result = new ModuleNodeResult(facilityId, OkNgConstant.TYPE_NG, -1, "unknownException 523"); // ここは通らないはず
					result.setRunCheckType(RunCheckTypeConstant.TYPE_RUN);
				}
				// 失敗したら以降はスキップする
				if (module.getStopIfFailFlg()) {
					if (result.getResult() == OkNgConstant.TYPE_NG) {
						skipNodeList.add(facilityId);
					}
				}
				ret.getModuleNodeResultList().add(result);
				i++;
			}
		} finally {
			module.afterRun(sessionId);
		}
		
		if (module instanceof CommandModuleInfo) {
			ret.setModuleType(ModuleTypeConstant.TYPE_COMMAND);
		} else if (module instanceof FileTransferModuleInfo) {
			ret.setModuleType(ModuleTypeConstant.TYPE_FILETRANSFER);
		}
		return ret;
	}

	public static ModuleResult checkInfraModule(String sessionId, boolean verbose)
			throws HinemosUnknown, SessionNotFound, InfraManagementNotFound, InvalidRole {
		// セッションの取得
		Session session = sessionMap.get(sessionId);
		if (session == null) {
			throw new SessionNotFound(sessionId);
		}
		List<AccessInfo> accessList = session.accessList;
		InfraManagementInfo management = session.management;
		List<InfraModuleInfo<?>> moduleList = session.moduleList;
		InfraModuleInfo<?> module = moduleList.get(0);
		List<String> skipNodeList = session.skipNodeList;

		// このセッションを触ってよいかチェック。
		new SelectInfraManagement().get(management.getManagementId(), ObjectPrivilegeMode.EXEC);
		
		moduleList.remove(0);

		// 返り値
		ModuleResult ret = new ModuleResult(sessionId, module.getModuleId());
		if (moduleList.size() == 0) {
			ret.setHasNext(false);
			deleteSession(sessionId);
		} else {
			ret.setHasNext(true);
		}
		
		module.beforeRun(sessionId);
		
		try {
			// チェックの実行
			RepositoryControllerBean repository = new RepositoryControllerBean();
			List<Future<ModuleNodeResult>> futureList = new ArrayList<Future<ModuleNodeResult>>();
			List<String> runList = new ArrayList<>();
			for (AccessInfo access : accessList) {
				NodeInfo node;
				String facilityId = access.getFacilityId();
				if (skipNodeList.contains(facilityId)) {
					ModuleNodeResult result = new ModuleNodeResult(facilityId, OkNgConstant.TYPE_SKIP, -1, "");
					result.setRunCheckType(RunCheckTypeConstant.TYPE_CHECK);
					ret.getModuleNodeResultList().add(result);
					continue;
				}
				try {
					node = repository.getNode(facilityId);
					Future<ModuleNodeResult> future = executor.submit(new ModuleCheckTask(management, module, node, access, sessionId, verbose));
					futureList.add(future);
					runList.add(facilityId);
				}
				catch (FacilityNotFound e) {
					m_log.debug(e.getMessage(), e);
				}
			}
	
			// チェックの実行結果の取得
			long timeout = HinemosPropertyUtil.getHinemosPropertyNum("infra.check.timeout", Long.valueOf(50 * 1000));
			int i = 0;
			for (Future<ModuleNodeResult> future : futureList) {
				String facilityId = runList.get(i);
				ModuleNodeResult result = null;
				String message = null;
				try {
					result = future.get(timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException | ExecutionException e) {
					message = e.getCause().getMessage();
					if (message == null) {
						message = e.getClass().getName() + ":" + e.getMessage();
					}
					m_log.warn("runInfraModule : " + e.getMessage(), e);
				} catch (Exception e) {
					message = e.getMessage();
					if (message == null) {
						message = e.getClass().getName();
					}
				}
				if (message != null) {
					m_log.warn("checkInfraModule : " + message);
					result = new ModuleNodeResult(facilityId, OkNgConstant.TYPE_NG, -1, message);
					result.setRunCheckType(RunCheckTypeConstant.TYPE_CHECK);
				}
				ret.getModuleNodeResultList().add(result);
				i++;
			}
			
			UpdateInfraCheckResult.update(management.getManagementId(), module.getModuleId(), ret.getModuleNodeResultList());
		
		} finally {
			module.afterRun(sessionId);
		}
		
		if (module instanceof CommandModuleInfo) {
			ret.setModuleType(ModuleTypeConstant.TYPE_COMMAND);
		} else if (module instanceof FileTransferModuleInfo) {
			ret.setModuleType(ModuleTypeConstant.TYPE_FILETRANSFER);
		}
		return ret;
	}
}
