/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementInvalid;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InfraModuleNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.SessionNotFound;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.InfraParameterConstant;
import com.clustercontrol.infra.bean.ModuleNodeResult;
import com.clustercontrol.infra.bean.ModuleResult;
import com.clustercontrol.infra.bean.ModuleTypeConstant;
import com.clustercontrol.infra.bean.OkNgConstant;
import com.clustercontrol.infra.bean.RunCheckTypeConstant;
import com.clustercontrol.infra.model.CommandModuleInfo;
import com.clustercontrol.infra.model.FileTransferModuleInfo;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.infra.model.ReferManagementModuleInfo;
import com.clustercontrol.infra.util.InfraParameterUtil;
import com.clustercontrol.jobmanagement.factory.CreateSessionId;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
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

	/** セッションIDのPRFIX **/
	private static final String SESSION_PREFIX = "INFRA_";

	static {
		int threadSize = HinemosPropertyCommon.infra_run_thread.getIntegerValue();
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
		
		// 開始の通知
		protected void notifyModuleCheckStart(InfraModuleInfo<?> module) {
			String msg = null;
			msg = MessageConstant.MESSAGE_MODULE_CHECK_BEGINS.getMessage();
			notifyMessage(module, management.getStartPriority(), msg, msg);
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
			notifyMessage(module, priority, msg, result.getMessage());
		}
		
		// 開始の通知
		protected void notifyModuleRunStart(InfraModuleInfo<?> module) {
			String msg = null;
			msg = MessageConstant.MESSAGE_MODULE_EXECUTION_BEGINS.getMessage();
			notifyMessage(module, management.getStartPriority(), msg, msg);
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
			notifyMessage(module, priority, msg, result.getMessage());
		}

		/**
		 * 通知実行
		 * @param module モジュール（参照環境構築モジュールの場合は、参照している環境構築のモジュール）
		 * @param priority 重要度
		 * @param msg メッセージ
		 * @param msgOrg オリジナルメッセージ
		 */
		protected void notifyMessage(
				InfraModuleInfo<?> module,
				int priority,
				String msg,
				String msgOrg) {

			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				InfraManagementInfo infraManagementInfo = null;
				if (module instanceof ReferManagementModuleInfo) {
					infraManagementInfo = module.getInfraManagementInfoEntity();
				} else {
					infraManagementInfo = management;
				}
				OutputBasicInfo info = createOutputBasicInfo(
						HinemosModuleConstant.INFRA,
						infraManagementInfo.getManagementId(),
						module.getModuleId(),
						node.getFacilityId(),
						null,
						"", // application
						priority,
						msg,
						msgOrg,
						HinemosTime.getDateInstance().getTime()
						);
				info.setNotifyGroupId(infraManagementInfo.getNotifyGroupId());

				// 通知設定
				jtm.addCallback(new NotifyCallback(info));
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
		private Map<String, String> paramMap;
		
		public ModuleRunTask(InfraManagementInfo management, InfraModuleInfo<?> module, NodeInfo node, AccessInfo access, String sessionId, Map<String, String> paramMap) {
			super(management, node, access);
			this.module = module;
			this.sessionId = sessionId;
			this.paramMap = paramMap;
		}
		
		@Override
		public ModuleNodeResult invoke() throws Exception {
			m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "start", "run", management.getManagementId(), module.getModuleId()));
			JpaTransactionManager jtm = null;

			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				if (!management.getValidFlg() || !module.getValidFlg()) {
					m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "disable", "run", management.getManagementId(), module.getModuleId()));
					ModuleNodeResult r = new ModuleNodeResult(node.getFacilityId(), OkNgConstant.TYPE_NG, -1, "invalid");
					r.setRunCheckType(RunCheckTypeConstant.TYPE_RUN);
					return r;
				}
	
				ModuleNodeResult result = null;
				if (module.getPrecheckFlg()) {
					result = module.check(management, node, access, sessionId, paramMap, false);
					result.setRunCheckType(RunCheckTypeConstant.TYPE_PRECHECK);
					if (result.getResult() == OkNgConstant.TYPE_OK) {
						return result;
					}
				}
				notifyModuleRunStart(module);
				result = module.run(management, node, access, sessionId, paramMap);
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

				jtm.commit();

				return result;

			} catch (Exception e) {
				m_log.warn("invoke() : Exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
				if (jtm != null)
					jtm.rollback();
				throw e;
			} finally {
				if (jtm != null)
					jtm.close();
			}
		}
		
		@Override
		public void unexpectedError(Exception e) throws Exception {
			notifyMessage(module, management.getAbnormalPriorityRun(), MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED.getMessage(), e.getMessage());
		}
	}

	// 環境構築モジュールに対してチェック
	private static class ModuleCheckTask extends AbstarctInfraTask {
		private InfraModuleInfo<?> module;
		boolean verbose;
		String sessionId;
		Map<String, String> paramMap;
		
		public ModuleCheckTask(InfraManagementInfo management, InfraModuleInfo<?> module, NodeInfo node, AccessInfo access, String sessionId, Map<String, String> paramMap, boolean verbose) {
			super(management, node, access);
			this.module = module;
			this.verbose = verbose;
			this.sessionId = sessionId;
			this.paramMap = paramMap;
		}
		
		@Override
		public ModuleNodeResult invoke() throws Exception {
			m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "start", "check", management.getManagementId(), module.getModuleId()));
			JpaTransactionManager jtm = null;

			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				if (!management.getValidFlg() || !module.getValidFlg()) {
					m_log.debug(String.format("%s %s, manegementId = %s, moduleId = %s", "disable", "check", management.getManagementId(), module.getModuleId()));
					ModuleNodeResult r = new ModuleNodeResult(node.getFacilityId(), OkNgConstant.TYPE_NG, -1, "invalid");
					r.setRunCheckType(RunCheckTypeConstant.TYPE_CHECK);
					return r;
				}
	
				notifyModuleCheckStart(module);
				ModuleNodeResult result = module.check(management, node, access, sessionId, paramMap, verbose);
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

				jtm.commit();
				return result;

			} catch (Exception e) {
				m_log.warn("invoke() : Exception=" + e.getClass().getSimpleName() + ", message=" + e.getMessage(), e);
				if (jtm != null)
					jtm.rollback();
				throw e;
			} finally {
				if (jtm != null)
					jtm.close();
			}
		}
		
		@Override
		public void unexpectedError(Exception e) throws Exception {
			notifyMessage(module, management.getAbnormalPriorityCheck(),  MessageConstant.MESSAGE_UNEXPECTED_ERR_OCCURRED.getMessage(), e.getMessage());
		}
	}
	
	private static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

	private static class Session{
		/*
		 * セッションID
		 * サブセッション（参照環境構築モジュールの参照先）の場合は
		 * 「セッションID:モジュールID:モジュールID:...」
		 */
		String sessionId = null;
		InfraManagementInfo management = null;
		List<InfraModuleInfo<?>> moduleList = new ArrayList<>();
		List<AccessInfo> accessList = new ArrayList<>();
		Map<String, String> paramMap = new HashMap<>();
		Map<String, Session> subSessionMap = new HashMap<>();
		boolean isFirst = true;			// true:参照環境構築モジュールの配下のモジュールへの初回アクセス

		/*
		 * 「実行コマンドのリターンコードが0以外の場合、後続モジュールを実行しない。」にチェックを入れており、
		 * 実行コマンドのリターンコードが0以外の場合は skipNodeList に追加される。
		 * skipNodeListに追加された場合、後続モジュールすべてを実行しない。
		 */
		List<String> skipNodeList = new ArrayList<>();

		/**
		 * サブセッション用コンストラクタ
		 * @param sessionId セッションID
		 */
		private Session (String sessionId) {
			this.sessionId = sessionId;
		}

		/**
		 * セッション用コンストラクタ
		 * @param managementId 環境構築ID
		 * @param moduleIdList モジュールID一覧(モジュールを指定して実行時使用)
		 * @param paramAccessList ログイン情報リスト
		 * @param sessionId セッションID
		 */
		private Session (String managementId, List<String> moduleIdList, List<AccessInfo> paramAccessList, String sessionId)
				throws InfraManagementNotFound, InfraModuleNotFound, InvalidRole, HinemosUnknown {
			this.management = new SelectInfraManagement().get(managementId, null, ObjectPrivilegeMode.EXEC);
			this.sessionId = sessionId;
			Map<String, List<AccessInfo>> accessListMap = new HashMap<>();
			for (AccessInfo accessInfo : paramAccessList) {
				// サブセッションごとにログイン情報マップに設定
				String subSessionId = null;
				if (accessInfo.getModuleId().equals("")) {
					subSessionId = this.sessionId;
				} else {
					subSessionId = String.format("%s" + AccessInfo.MODULEID_DELIMITER + "%s", this.sessionId, accessInfo.getModuleId());
				}
				if (!accessListMap.containsKey(subSessionId)) {
					accessListMap.put(subSessionId, new ArrayList<>());
				}
				accessInfo.setModuleId(null);
				accessListMap.get(subSessionId).add(accessInfo);
			}
			accessList = accessListMap.get(this.sessionId);
			paramMap.putAll(InfraParameterUtil.createInfraParameter(managementId));
			for (InfraModuleInfo<?> module : management.getModuleList()) {
				if (moduleIdList != null && moduleIdList.size() > 0 && !moduleIdList.contains(module.getModuleId())) {
					continue;
				}
				if (module.getValidFlg()) {
					if (module instanceof ReferManagementModuleInfo) {
						String subSessionId = String.format("%s" + AccessInfo.MODULEID_DELIMITER + "%s", this.sessionId, module.getModuleId());
						Session subSession = createSubSession(((ReferManagementModuleInfo)module).getReferManagementId(), subSessionId, accessListMap, paramMap);
						if (subSession == null) {
							continue;
						}
						subSessionMap.put(subSessionId, subSession);
					}
					moduleList.add(module);
				}
			}
			if (moduleList.size() == 0) {
				throw new InfraModuleNotFound(managementId, "");
			}
		}
		
		/**
		 * サブセッション作成
		 * @param referManagementId 参照環境構築モジュールで参照している環境構築ID
		 * @param sessionId セッションID
		 * @param accessListMap ログイン情報マップ
		 * @param parentParamMap 環境構築変数マップ
		 * @return サブセッション
		 */
		private Session createSubSession(String referManagementId, String sessionId, Map<String, List<AccessInfo>> accessListMap, Map<String, String> parentParamMap) {
			if (referManagementId == null || referManagementId.isEmpty()) {
				return null;
			}
			Session session = new Session(sessionId);
			try {
				session.management = new SelectInfraManagement().get(referManagementId, null, ObjectPrivilegeMode.EXEC);
			} catch (Exception e) {
				m_log.warn("createSubSession() : referInfraManagement is not found. referManagementId=" + referManagementId);
				return null;
			}
			session.paramMap.putAll(parentParamMap);
			session.paramMap.putAll(InfraParameterUtil.createInfraParameter(referManagementId));
			for (InfraModuleInfo<?> module : session.management.getModuleList()) {
				if (module.getValidFlg()) {
					if (module instanceof ReferManagementModuleInfo) {
						String subSessionId = String.format("%s" + AccessInfo.MODULEID_DELIMITER + "%s", sessionId, module.getModuleId());
						Session subSession = createSubSession(((ReferManagementModuleInfo) module).getReferManagementId(), subSessionId, accessListMap, session.paramMap);
						session.subSessionMap.put(subSessionId, subSession);
					}
					session.moduleList.add(module);
				}
			}
			if (session.moduleList.size() == 0) {
				return null;
			}
			if (accessListMap.containsKey(sessionId)) {
				session.accessList = accessListMap.get(sessionId);
			} else {
				String accessKey = sessionId.substring(0, sessionId.indexOf(AccessInfo.MODULEID_DELIMITER));
				session.accessList = accessListMap.get(accessKey);
			}
			return session;
		}
	}

	public static String createSession(String managementId, List<String> moduleIdList, List<AccessInfo> accessList)
			throws InfraManagementNotFound, InfraModuleNotFound, InfraManagementInvalid, InvalidRole, HinemosUnknown, FacilityNotFound, InvalidSetting {
		
		InfraManagementInfo management = new SelectInfraManagement().get(managementId, null, ObjectPrivilegeMode.EXEC);
		String roleId = management.getOwnerRoleId();

		for (AccessInfo access : accessList) {
			String facilityId = access.getFacilityId();
			
			// 参照権限のチェック
			FacilityTreeCache.validateFacilityId(facilityId, roleId, true);
			
			NodeInfo node = NodeProperty.getProperty(facilityId);
			access.setSshPort(node.getSshPort());
			access.setSshTimeout(node.getSshTimeout());
			access.setWinRmPort(node.getWinrmPort());
			access.setWinRmTimeout(node.getWinrmTimeout());
		}
		
		String sessionId = SESSION_PREFIX + CreateSessionId.create();
		sessionMap.put(sessionId, new Session(managementId, moduleIdList, accessList, sessionId));
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
		new SelectInfraManagement().get(session.management.getManagementId(), null, ObjectPrivilegeMode.EXEC);
		
		sessionMap.remove(sessionId);
		return true;
	}

	/**
	 * 実行モジュール実行
	 * @param sessionId
	 * @return モジュール実行結果
	 * @throws HinemosUnknown
	 * @throws SessionNotFound
	 * @throws InfraManagementNotFound
	 * @throws InvalidRole
	 */
	public static ModuleResult runInfraModule(String sessionId)
			throws HinemosUnknown, SessionNotFound, InfraManagementNotFound, InvalidRole {
		// セッションの取得
		Session session = sessionMap.get(sessionId);
		if (session == null) {
			throw new SessionNotFound(sessionId);
		}
		ModuleResult ret = runInfraModule(session);
		return ret;
	}
	

	/**
	 * 実行モジュール実行
	 * サブモジュール（参照環境構築モジュールの参照先）の場合は、再帰呼び出し
	 * @param sessionId
	 * @return モジュール実行結果
	 * @throws HinemosUnknown
	 * @throws SessionNotFound
	 * @throws InfraManagementNotFound
	 * @throws InvalidRole
	 */
	private static ModuleResult runInfraModule(Session session)
			throws HinemosUnknown, SessionNotFound, InfraManagementNotFound, InvalidRole {
		// セッションのモジュール実行権限確認
		new SelectInfraManagement().get(session.management.getManagementId(), null, ObjectPrivilegeMode.EXEC);
		List<InfraModuleInfo<?>> moduleList = session.moduleList;
		List<AccessInfo> accessList = null;
		List<String> skipNodeList = null;
		InfraModuleInfo<?> module = null;
		String moduleId = moduleList.get(0).getModuleId();
		
		// 返り値
		ModuleResult ret = null;
		module = moduleList.get(0);
		if (moduleList.get(0) instanceof ReferManagementModuleInfo) {
			Session subSession = session.subSessionMap.get(
				String.format("%s" + AccessInfo.MODULEID_DELIMITER + "%s", session.sessionId, moduleId));
			if (subSession.isFirst) {
				for (Map.Entry<String, String> entry : session.paramMap.entrySet()) {
					String[] keyArgs = entry.getKey().split(InfraParameterConstant.PARAMETER_DELIMITER);
					if (keyArgs.length >= 2) {
						subSession.paramMap.put(entry.getKey(), entry.getValue());
					}
				}
				subSession.isFirst = false;
			}
			ret = runInfraModule(subSession);
			if (subSession.moduleList.size() == 0) {
				moduleList.remove(0);
			}
			if (moduleList.size() == 0) {
				ret.setHasNext(false);
				deleteSession(session.sessionId);
			} else {
				ret.setHasNext(true);
			}
			return ret;
		} else {
			accessList = session.accessList;
			skipNodeList = session.skipNodeList;
			moduleList.remove(0);
			ret = new ModuleResult(session.sessionId, module.getModuleId());
			if (moduleList.size() == 0) {
				ret.setHasNext(false);
				deleteSession(session.sessionId);
			} else {
				ret.setHasNext(true);
			}
		}
		
		module.beforeRun(session.sessionId);

		try {
			// 実行
			RepositoryControllerBean repository = new RepositoryControllerBean();
			List<Future<ModuleNodeResult>> futureList = new ArrayList<Future<ModuleNodeResult>>();
			List<String> runList = new ArrayList<>();
			if (accessList != null) {
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
						Future<ModuleNodeResult> future = executor.submit(new ModuleRunTask(session.management, module, node, access, session.sessionId, session.paramMap));
						futureList.add(future);
						runList.add(facilityId);
					}
					catch (FacilityNotFound e) {
						m_log.debug(e.getMessage(), e);
					}
				}
			}
	
			// 実行結果の取得
			long timeout = HinemosPropertyCommon.infra_run_timeout.getNumericValue();
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

				// 戻り値を設定
				if (module instanceof CommandModuleInfo) {
					// コマンドモジュール
					session.paramMap.put(
							String.format("%s:%s", module.getExecReturnParamName(), result.getFacilityId()), 
							String.valueOf(result.getStatusCode()));
				} else if (module instanceof FileTransferModuleInfo) {
					// ファイル配布モジュール
					if (result.getResult() == OkNgConstant.TYPE_OK) {
						Integer fileStatusCode = HinemosPropertyCommon.infra_management_file_module_return_success.getIntegerValue();
						session.paramMap.put(
								String.format("%s" + InfraParameterConstant.PARAMETER_DELIMITER + "%s", module.getExecReturnParamName(), result.getFacilityId()), 
								fileStatusCode.toString());
					} else if (result.getResult() == OkNgConstant.TYPE_NG) {
						Integer fileStatusCode = HinemosPropertyCommon.infra_management_file_module_return_failure.getIntegerValue();
						session.paramMap.put(
								String.format("%s" + InfraParameterConstant.PARAMETER_DELIMITER + "%s", module.getExecReturnParamName(), result.getFacilityId()), 
								fileStatusCode.toString());
					}
				}

				ret.getModuleNodeResultList().add(result);
				i++;
			}
		} finally {
			module.afterRun(session.sessionId);
		}
		
		if (module instanceof CommandModuleInfo) {
			ret.setModuleType(ModuleTypeConstant.TYPE_COMMAND);
		} else if (module instanceof FileTransferModuleInfo) {
			ret.setModuleType(ModuleTypeConstant.TYPE_FILETRANSFER);
		} else if (module instanceof ReferManagementModuleInfo) {
			ret.setModuleType(ModuleTypeConstant.TYPE_REFERMANAGEMENT);
		}
		return ret;
	}

	/**
	 * チェックモジュール実行
	 * @param sessionId
	 * @param verbose
	 * @return モジュール実行結果
	 * @throws HinemosUnknown
	 * @throws SessionNotFound
	 * @throws InfraManagementNotFound
	 * @throws InvalidRole
	 */
	public static ModuleResult checkInfraModule(String sessionId, boolean verbose)
			throws HinemosUnknown, SessionNotFound, InfraManagementNotFound, InvalidRole {
		// セッションの取得
		Session session = sessionMap.get(sessionId);
		if (session == null) {
			throw new SessionNotFound(sessionId);
		}
		ModuleResult ret = checkInfraModule(session, verbose);
		return ret;
	}

	/**
	 * チェックモジュール実行
	 * サブモジュール（参照環境構築モジュールの参照先）の場合は、再帰呼び出し
	 * @param sessionId
	 * @param verbose
	 * @return モジュール実行結果
	 * @throws HinemosUnknown
	 * @throws SessionNotFound
	 * @throws InfraManagementNotFound
	 * @throws InvalidRole
	 */
	private static ModuleResult checkInfraModule(Session session, boolean verbose)
			throws HinemosUnknown, SessionNotFound, InfraManagementNotFound, InvalidRole {
		// セッションのモジュール実行権限確認
		new SelectInfraManagement().get(session.management.getManagementId(), null, ObjectPrivilegeMode.EXEC);
		List<InfraModuleInfo<?>> moduleList = session.moduleList;
		List<AccessInfo> accessList = null;
		List<String> skipNodeList = null;
		InfraModuleInfo<?> module = null;
		String moduleId = moduleList.get(0).getModuleId();

		// 返り値
		ModuleResult ret = null;
		module = moduleList.get(0);
		if (moduleList.get(0) instanceof ReferManagementModuleInfo) {
			Session subSession = session.subSessionMap.get(
				String.format("%s" + AccessInfo.MODULEID_DELIMITER + "%s", session.sessionId, moduleId));
			ret = checkInfraModule(subSession, verbose);
			if (subSession.moduleList.size() == 0) {
				moduleList.remove(0);
			}
			if (moduleList.size() == 0) {
				ret.setHasNext(false);
				deleteSession(session.sessionId);
			} else {
				ret.setHasNext(true);
			}
			return ret;
		} else {
			accessList = session.accessList;
			skipNodeList = session.skipNodeList;
			moduleList.remove(0);
			ret = new ModuleResult(session.sessionId, module.getModuleId());
			if (moduleList.size() == 0) {
				ret.setHasNext(false);
				deleteSession(session.sessionId);
			} else {
				ret.setHasNext(true);
			}
		}
		
		module.beforeRun(session.sessionId);
		
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
					Future<ModuleNodeResult> future = executor.submit(new ModuleCheckTask(session.management, module, node, access, session.sessionId, session.paramMap, verbose));
					futureList.add(future);
					runList.add(facilityId);
				}
				catch (FacilityNotFound e) {
					m_log.debug(e.getMessage(), e);
				}
			}
	
			// チェックの実行結果の取得
			long timeout = HinemosPropertyCommon.infra_check_timeout.getNumericValue();
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
			
			UpdateInfraCheckResult.update(session.management.getManagementId(), module.getModuleId(), ret.getModuleNodeResultList());
		
		} finally {
			module.afterRun(session.sessionId);
		}
		
		if (module instanceof CommandModuleInfo) {
			ret.setModuleType(ModuleTypeConstant.TYPE_COMMAND);
		} else if (module instanceof FileTransferModuleInfo) {
			ret.setModuleType(ModuleTypeConstant.TYPE_FILETRANSFER);
		} else if (module instanceof ReferManagementModuleInfo) {
			ret.setModuleType(ModuleTypeConstant.TYPE_REFERMANAGEMENT);
		}
		return ret;
	}
}
