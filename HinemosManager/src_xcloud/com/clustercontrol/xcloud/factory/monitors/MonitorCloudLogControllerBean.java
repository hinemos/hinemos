/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.xcloud.factory.monitors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.hinemosagent.util.AgentVersionManager;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.MonitorOnAgentUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.endpoint.agent.dto.AgtMonitorInfoResponse;
import com.clustercontrol.rest.endpoint.agent.dto.AgtMonitorPluginStringInfoResponse;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.HinemosCredential;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.common.CloudMessageConstant;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.model.AccessKeyCredentialEntity;
import com.clustercontrol.xcloud.model.CloudLogMonitorRunStatusEntity;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CredentialBaseEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.util.FacilityIdUtil;

/**
 * クラウドログ監視の管理を行う Session Bean <BR>
 * 
 */
public class MonitorCloudLogControllerBean {
	public static final String monitorTypeId = HinemosModuleConstant.MONITOR_CLOUD_LOG;
	public static final int monitorType = MonitorTypeConstant.TYPE_STRING;
	public static final String STRING_CLOUDSERVICE_LOG_MONITOR = CloudMessageConstant.CLOUDSERVICE_LOG_MONITOR
			.getMessage();

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(MonitorCloudLogControllerBean.class);

	private static final ILock _lock;

	private static ConcurrentHashMap<String, String> currentRunningMap = new ConcurrentHashMap<String, String>();
	private static AtomicBoolean agentChangedFlg = new AtomicBoolean(false);
	private static ScheduledExecutorService _scheduler;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(MonitorCloudLogControllerBean.class.getName());

		try {
			_lock.writeLock();

			ArrayList<MonitorInfo> cache = getCache();
			if (cache == null) { // not null when clustered
				refreshCache();
			}
		} finally {
			_lock.writeUnlock();
			m_log.info("Static Initialization [Thread : " + Thread.currentThread() + ", User : "
					+ (String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID) + "]");
		}
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<MonitorInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_XCLOUD_LOGFILE);
		if (m_log.isDebugEnabled()) {
			m_log.debug("get cache " + AbstractCacheManager.KEY_XCLOUD_LOGFILE + " : " + cache);
		}

		if (cache == null) {
			return null;
		} else {
			return (ArrayList<MonitorInfo>) cache;
		}

	}

	private static void storeCache(ArrayList<MonitorInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) {
			m_log.debug("store cache " + AbstractCacheManager.KEY_XCLOUD_LOGFILE + " : " + newCache);
		}
		cm.store(AbstractCacheManager.KEY_XCLOUD_LOGFILE, newCache);
	}

	/**
	 * クラウドログ監視一覧リストを返します。
	 * 
	 * 
	 * @return Objectの2次元配列
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<MonitorInfo> getCloudLogfileList() throws MonitorNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;
		ArrayList<MonitorInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectMonitor().getMonitorListObjectPrivilegeModeNONE(HinemosModuleConstant.MONITOR_CLOUD_LOG);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCloudLogList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return list;
	}

	public static void refreshCache() {
		m_log.info("refreshCache()");

		long startTime = HinemosTime.currentTimeMillis();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_lock.writeLock();

			em.clear();
			ArrayList<MonitorInfo> cloudLogCache = new MonitorCloudLogControllerBean().getCloudLogfileList();
			storeCache(cloudLogCache);

			// 実行MAPの更新
			ArrayList<String> presentMonIdList = new ArrayList<String>();
			for (MonitorInfo mon : cloudLogCache) {
				presentMonIdList.add(mon.getMonitorId());
			}

			if (currentRunningMap.keySet().retainAll(presentMonIdList)) {
				m_log.debug("refreshCache(): currentRunningMap changed");
			}

			m_log.info("refresh cloudLogCache " + (HinemosTime.currentTimeMillis() - startTime) + "ms. size="
					+ cloudLogCache.size());
		} catch (Exception e) {
			m_log.warn("failed refreshing cache.", e);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * 
	 * <注意！> このメソッドはAgentユーザ以外で呼び出さないこと！
	 * <注意！> キャッシュの都合上、Agentユーザ以外から呼び出すと、正常に動作しません。
	 * 
	 * facilityIDごとのログファイル監視一覧リストを返します。
	 * withCalendarをtrueにするとMonitorInfoのcalendarDTOに値が入ります。
	 * 
	 * 
	 * @return Objectの2次元配列
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * 
	 */
	public ArrayList<MonitorInfo> getCloudLogListForFacilityId(String facilityId, boolean withCalendar)
			throws MonitorNotFound, HinemosUnknown {
		ArrayList<MonitorInfo> ret = new ArrayList<MonitorInfo>();
		JpaTransactionManager jtm = null;

		m_log.debug("getCloudLogListForFacilityId(): start");

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			ArrayList<MonitorInfo> monitorList = getCache();
			initAgentMonitorThread(monitorList);
			
			for (MonitorInfo monitorInfo : monitorList) {
				m_log.debug("getCloudLogListForFacilityId(): search for MonitorId=" + monitorInfo.getMonitorId());

				// 実行対象となるエージェントのファシリティIDを取得
				String targetFacility = getTargetFacilityId(monitorInfo);
				m_log.debug("getCloudLogListForFacilityId(): targetFacility=" + targetFacility);

				// 情報を要求したエージェントが実行対象であるかを判断
				if (facilityId.equals(targetFacility)) {
					// カレンダが設定済みの場合はカレンダ設定をMonitorInfoに格納
					if (withCalendar) {
						String calendarId = monitorInfo.getCalendarId();
						try {
							CalendarInfo calendar = new CalendarControllerBean().getCalendarFull(calendarId);
							monitorInfo.setCalendar(calendar);
						} catch (Exception e) {
							m_log.warn("getLogfileList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
							throw new HinemosUnknown(e.getMessage(), e);
						}
					}

					// 監視、収集ともに無効な場合は、エージェントに送信しない
					if (!monitorInfo.getMonitorFlg() && !monitorInfo.getCollectorFlg()) {
						m_log.debug("getCloudLogListForFacilityID: Monitor is disabled. MonitorID: "
								+ monitorInfo.getMonitorId());
						continue;
					}

					ret.add(monitorInfo);

					/*
					 * 実行対象のエージェントの管理 エージェントで初めて監視を始めるとき、
					 * 実行対象のエージェントが変更になったときにINTERNALを出力
					 */
					RepositoryControllerBean rcb = new RepositoryControllerBean();
					if (currentRunningMap.containsKey(monitorInfo.getMonitorId())) {
						if (currentRunningMap.get(monitorInfo.getMonitorId()).equals(facilityId)) {
							// 実行エージェントに変更が無い場合何もしない
						} else {
							// 実行エージェントが変更になったのでINTERNALを出力
							String oldFacilityId = currentRunningMap.get(monitorInfo.getMonitorId());
							String oldFacilityName = "";
							String newFacilityName = "";
							try {
								oldFacilityName = rcb.getNode(oldFacilityId).getFacilityName();
							} catch (FacilityNotFound e) {
								// oldFacilityIdが削除済みの場合にここに来る
								// ファシリティ名は取得できないが、
								// 処理的には問題ないのでログに出力するだけ
								m_log.info("getCloudLogListForFacilityId(): " + e.getMessage());
							}
							// こっちでFacilityNotFoundが起こるのは何か問題が発生しているので、
							// 上位でキャッチ
							newFacilityName = rcb.getNode(facilityId).getFacilityName();
							String[] msgArgs = { monitorInfo.getMonitorId(), oldFacilityName, oldFacilityId,
									newFacilityName, facilityId };
							AplLogger.put(InternalIdCommon.MON_CLOUDLOG_S_SYS_002, msgArgs);
							currentRunningMap.replace(monitorInfo.getMonitorId(), facilityId);
							// 実行エージェントの変更を記録
							agentChangedFlg.set(true);
						}
					} else {
						currentRunningMap.put(monitorInfo.getMonitorId(), facilityId);
						if (!rcb.isNode(getTargetScope(monitorInfo))) {
							// スコープ指定で初回実行時にはINTERNAL通知
							String[] msgArgs = { rcb.getNode(facilityId).getFacilityName(), facilityId,
									monitorInfo.getMonitorId() };
							AplLogger.put(InternalIdCommon.MON_CLOUDLOG_S_SYS_001, msgArgs);
						}

					}
				}
			}

			jtm.commit();

		} catch (HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getLogfileListForFacilityId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		return ret;
	}

	/**
	 * クラウドログ監視の実行を行うエージェントのファシリティIDを取得するメソッド
	 * 
	 * @param monitorInfo
	 * @return
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	private String getTargetFacilityId(MonitorInfo monitorInfo) throws FacilityNotFound, HinemosUnknown {
		String scope = getTargetScope(monitorInfo);
		ArrayList<String> activeFacilityList = new ArrayList<String>();

		// スコープ配下のファシリティIDを取得
		for (String f : new RepositoryControllerBean().getNodeFacilityIdList(scope, monitorInfo.getOwnerRoleId(), 0,
				true, true)) {
			if (AgentConnectUtil.isValidAgent(f)) {
				// ver.7.0以下のエージェントが存在しないかチェック
				if (!AgentVersionManager.checkVersion(AgentConnectUtil.getAgentInfo(f).getVersion(),
						AgentVersionManager.VERSION_7_0)) {
					m_log.info("getTargetFacilityId: Ignore Unsupported Version Agent. Facility ID: " + f);
				} else {
					activeFacilityList.add(f);
				}
			}
		}

		String targetFacility = "";
		int max = 0;
		// 最も優先度が高いノードを取得
		for (String s : activeFacilityList) {
			NodeInfo tmp = new RepositoryControllerBean().getNode(s);
			if (tmp.getCloudLogPriority() > max) {
				max = tmp.getCloudLogPriority();
				targetFacility = tmp.getFacilityId();
			}
		}

		return targetFacility;
	}

	/**
	 * 監視実行エージェントスコープを取得
	 * 
	 * @param info
	 * @return
	 */
	private String getTargetScope(MonitorInfo info) {
		for (MonitorPluginStringInfo i : info.getPluginCheckInfo().getMonitorPluginStringInfoList()) {
			if (i.getKey().equals(CloudConstant.cloudLog_targetScopeFacilityId)) {
				m_log.debug("getTargetScope(): found FacilityId" + i.getValue());
				return i.getValue();
			}
		}
		m_log.debug("getTargetScope(): facility Not Found");
		return "";
	}

	/**
	 * クラウドログ監視固有設定をAgtMonitorInfoResponseに反映します。
	 * 
	 * @param monitorInfo
	 * @throws CloudManagerException
	 * @throws HinemosUnknown
	 */
	public void reflectCloudLogInfo(AgtMonitorInfoResponse monitorInfo) throws CloudManagerException, HinemosUnknown {
		// 監視対象のクラウドスコープに紐づく認証情報を取得
		AccessKeyCredentialEntity tmp = null;
		String location = "";
		try (SessionScope sessionScope = SessionScope.open()) {
			// ログインユーザの取得
			CloudLoginUserEntity user = getCoudLoginUserForFacility(monitorInfo.getFacilityId());
			// 認証情報を取得
			tmp = getAuthInfo(user);
			// ファシリティIDからロケーションを抽出
			location = getLocation(user, monitorInfo.getFacilityId(), monitorInfo.getOwnerRoleId());
		}

		JpaTransactionManager jtm = null;

		long lastFireTime = 0;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();
			CloudLogMonitorRunStatusEntity statusEntity = null;
			statusEntity = em.find(CloudLogMonitorRunStatusEntity.class, monitorInfo.getMonitorId(),
					ObjectPrivilegeMode.NONE);
			if (statusEntity != null) {
				lastFireTime = statusEntity.getLastNotifiedDay();
				m_log.debug("setAuthInfo: LastFire time:" + lastFireTime);

			} else {
				m_log.debug("setAuthInfo: No lastFire time found");
			}
		} catch (Exception e) {
			m_log.warn("setAuthInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		// 最終実行日時を詰める
		AgtMonitorPluginStringInfoResponse pInfo = new AgtMonitorPluginStringInfoResponse();

		pInfo.setKey(CloudConstant.cloudLog_LastFireTime);
		pInfo.setValue(String.valueOf(lastFireTime));
		monitorInfo.getPluginCheckInfo().getMonitorPluginStringInfoList().add(pInfo);

		pInfo = new AgtMonitorPluginStringInfoResponse();
		pInfo.setKey(CloudConstant.cloudLog_accessKey);
		pInfo.setValue(tmp.getAccessKey());
		monitorInfo.getPluginCheckInfo().getMonitorPluginStringInfoList().add(pInfo);

		pInfo = new AgtMonitorPluginStringInfoResponse();
		pInfo.setKey(CloudConstant.cloudLog_secretKey);
		pInfo.setValue(tmp.getSecretKey());
		monitorInfo.getPluginCheckInfo().getMonitorPluginStringInfoList().add(pInfo);

		pInfo = new AgtMonitorPluginStringInfoResponse();
		pInfo.setKey(CloudConstant.cloudLog_Location);
		pInfo.setValue(location);
		monitorInfo.getPluginCheckInfo().getMonitorPluginStringInfoList().add(pInfo);

	}

	/**
	 * クラウドログ監視結果を通知するメソッド
	 * 
	 * @param results
	 *            クラウドログ監視結果のリスト
	 * @throws HinemosUnknown
	 */
	public void run(String facilityId, List<CloudLogResultDTO> results) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<OutputBasicInfo> notifyInfoList = new ArrayList<>();
		List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			if (results != null) {
				for (CloudLogResultDTO result : results) {
					// 現時点でAgentが実行対象であるかを確認
					try {
						/*
						 * ノードやスコープの変更により実行対象のエージェントが変更になった直後に ここに到達する可能性あり
						 * 監視ジョブの場合は監視ジョブの実行ターゲットスコープに所属しているかだけ確認する
						 */
						if (result.getRunInstructionInfo() == null
								&& !getTargetFacilityId(result.monitorInfo).equals(facilityId)) {
							m_log.debug("run(): skip to run because facilityId is not highest priority. FacilityID: "
									+ facilityId);
							continue;
						} else if (result.getRunInstructionInfo() != null) {
							// 監視ジョブ
							if (!MonitorOnAgentUtil.checkFacilityId(facilityId, result.getRunInstructionInfo(),
									result.getMonitorInfo())) {
								m_log.debug(
										"run(): skip to run because facilityId is not job target facility. FacilityID: "
												+ facilityId);
								continue;
							}
						}
					} catch (FacilityNotFound e) {
						m_log.debug("run(): skip to run because facilityId does not exists");
						continue;
					}

					// 監視ジョブ以外の場合のみ、最終実行日時を記録
					if (result.runInstructionInfo == null || result.runInstructionInfo.getJobunitId() == null
							|| result.runInstructionInfo.getJobunitId().isEmpty()) {
						// 最後にlogを取得した日時を記録
						HinemosEntityManager em = jtm.getEntityManager();

						CloudLogMonitorRunStatusEntity statusEntity = null;

						statusEntity = em.find(CloudLogMonitorRunStatusEntity.class, result.monitorInfo.getMonitorId(),
								ObjectPrivilegeMode.NONE);

						if (statusEntity != null) {
							statusEntity.setLastNotifiedDay(result.msgInfo.getGenerationDate());
						} else {
							// 監視設定が無効化された直後に到達する可能性あり。
							// 最終実行日時は無視する。
							// 通知や収集はログファイル監視と合わせ、この時点で無効化されていたとしても処理は行う。
							m_log.info("run(): Ignore lastFireTime since StatusEntity not exists for Monitor ID: "
									+ result.monitorInfo.getMonitorId());
						}

					} else {
						m_log.info("run(): MonitorJob result. Do not record last fire time");
					}
					
					// 通知対象のメッセージが存在しない場合は通知しない
					if (result.message == null) {
						m_log.debug("run(): only contains lastfiretime. skip notification");
						continue;
					}

					RunMonitorCloudLogString runMonitorCloudLogString = new RunMonitorCloudLogString();
					notifyInfoList.addAll(runMonitorCloudLogString.run(result.monitorInfo.getFacilityId(), result));
					monitorJobEndNodeList.addAll(runMonitorCloudLogString.getMonitorJobEndNodeList());
				}
			}

			// 通知設定
			jtm.addCallback(new NotifyCallback(notifyInfoList));

			jtm.commit();
		} catch (HinemosUnknown e) {
			m_log.warn("failed storeing result.", e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} finally {
			if (jtm != null) {
				jtm.close(this.getClass().getName());
			}
		}

		// 監視ジョブEndNode処理
		try {
			if (monitorJobEndNodeList != null && monitorJobEndNodeList.size() > 0) {
				for (MonitorJobEndNode monitorJobEndNode : monitorJobEndNodeList) {
					MonitorJobWorker.endMonitorJob(monitorJobEndNode.getRunInstructionInfo(),
							monitorJobEndNode.getMonitorTypeId(), monitorJobEndNode.getMessage(),
							monitorJobEndNode.getErrorMessage(), monitorJobEndNode.getStatus(),
							monitorJobEndNode.getEndValue());
				}
			}
		} catch (Exception e) {
			m_log.warn(
					"run() MonitorJobWorker.endMonitorJob() : " + e.getClass().getSimpleName() + ", " + e.getMessage(),
					e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * 引数で指定した監視設定の最終実行日時が存在しない場合に作成します。
	 * 
	 * @param monitorId
	 * @throws HinemosUnknown
	 */
	public void addRunStatusEntityIfNotExists(String monitorId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			HinemosEntityManager em = jtm.getEntityManager();
			CloudLogMonitorRunStatusEntity statusEntity = null;

			statusEntity = em.find(CloudLogMonitorRunStatusEntity.class, monitorId, ObjectPrivilegeMode.NONE);

			if (statusEntity != null) {
				m_log.debug("addRunStatusEntityIfNotExists(): entity exists for Monitor ID: " + monitorId);
			} else {
				statusEntity = new CloudLogMonitorRunStatusEntity();
				statusEntity.setMonitorId(monitorId);
				statusEntity.setLastNotifiedDay(0L);
				em.persist(statusEntity);
				m_log.info("addRunStatusEntityIfNotExists(): added entity for Monitor ID: " + monitorId);
			}

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("addRunStatusEntityIfNotExists: failed adding.", e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

	}

	/**
	 * 引数で指定した監視設定の最終実行日時を削除します
	 * 
	 * @param monitorId
	 * @throws HinemosUnknown
	 */
	public void deleteRunStatusEntity(String monitorId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			HinemosEntityManager em = jtm.getEntityManager();
			CloudLogMonitorRunStatusEntity statusEntity = null;
			statusEntity = em.find(CloudLogMonitorRunStatusEntity.class, monitorId, ObjectPrivilegeMode.NONE);
			if (statusEntity != null) {
				em.remove(statusEntity);
			}
			jtm.commit();
			m_log.info("deleteRunStatusEntity(): deleted entity for Monitor ID: " + monitorId);
		} catch (Exception e) {
			m_log.warn("deleteRunStatusEntity: failed deleting.", e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

	}

	/**
	 * 選択したクラウドスコープのロケーションを返却します。
	 * 
	 * @param facilityId
	 * @param ownerRoleId
	 * @return
	 * @throws CloudManagerException
	 * @throws HinemosUnknown
	 */
	private String getLocation(CloudLoginUserEntity user, String facilityId, String ownerRoleId) throws CloudManagerException, HinemosUnknown {

		String locationName = "";

		for (LocationEntity location : user.getCloudScope().getLocations()) {
			if (FacilityIdUtil.getLocationScopeId(user.getCloudScopeId(), location).equals(facilityId)) {
				locationName = location.getLocationId();
				m_log.debug("getLocation(): location id:" + locationName);
			}
		}

		if (locationName.isEmpty()) {
			throw new InternalManagerError(String.format("Location invalid. facilityId=%s", facilityId));
		}

		return locationName;
	}

	/**
	 * 選択したクラウドスコープの認証情報を返却します。
	 * 
	 * @param facilityId
	 * @param ownerRoleId
	 * @return
	 * @throws CloudManagerException
	 * @throws HinemosUnknown
	 */
	public AccessKeyCredentialEntity getAuthInfo(CloudLoginUserEntity user)
			throws CloudManagerException, HinemosUnknown {
		// ログインユーザ情報から認証情報を抽出
		AccessKeyCredentialEntity credential = user.getCredential()
				.transform(new CredentialBaseEntity.Transformer<AccessKeyCredentialEntity>() {
					@Override
					public AccessKeyCredentialEntity transform(AccessKeyCredentialEntity credential)
							throws CloudManagerException {
						return credential;
					}
				});
		m_log.debug("getAuthInfo(): credential: " + credential.getAccessKey());

		return credential;
	}

	/**
	 * ファシリティIDから参照可能なクラウドログインユーザを取得するメソッド
	 * 
	 * @param facilityId
	 * @return
	 * @throws HinemosUnknown
	 * @throws CloudManagerException
	 */
	private CloudLoginUserEntity getCoudLoginUserForFacility(String facilityId)
			throws HinemosUnknown, CloudManagerException {

		m_log.debug("getCloudScopeId: start facility id : " + facilityId);

		// ログインユーザの取得処理
		// その他クラウド系の監視に合わせ、処理を実施する際の内部的なユーザは
		// ADMINISTRATORSのユーザ（デフォルトhinemosユーザ）とする
		// そのため、通知設定のロールにメイン以外のログインユーザが割り当てられていても、
		// メインのログインユーザが使用される
		String userId = HinemosPropertyCommon.xcloud_internal_thread_admin_user.getStringValue();
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, userId);
		Session.current().setHinemosCredential(new HinemosCredential(userId));
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);

		// ファシリティIDからlocationID、cloudScopeIdを取り出す。
		String prefixRemoved = "";
		String cloudScopeId = "";
		try {
			prefixRemoved = facilityId.replaceFirst("_[A-Z]+_[A-Z]+_", "");
			cloudScopeId = prefixRemoved.substring(0, prefixRemoved.lastIndexOf("_"));
		} catch (Exception e) {
			// この時点でバリデーションされているので、上記の文字列操作でExceptionになることはないはずだが念のため。
			m_log.error("getCoudLoginUserForFacility(): CloudScope Id not found for facilityid: " + facilityId, e);
			throw new CloudManagerException("CloudScope Id not found for facilityid: " + facilityId);
		}

		if (m_log.isDebugEnabled()) {
			m_log.debug("getCoudLoginUserForFacility(): FacilityID: " + facilityId + " CloudScopeID: " + cloudScopeId);
		}

		// ログインユーザの取得
		final CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
				.getPrimaryCloudLoginUserByCurrent(cloudScopeId);

		// ログインユーザが見つからなかった場合は、エラー
		if (user == null) {
			throw new InternalManagerError(String.format("LoginUser not found. facilityId=%s", facilityId));
		}

		return user;

	}
	
	/**
	 * エージェント疎通確認用スケジューラを起動します
	 * 監視設定がなくなった場合には、スケジューラを停止します。
	 * @param monitorList
	 */
	private void initAgentMonitorThread(ArrayList<MonitorInfo> monitorList) {
		// intervalが0未満の場合、エージェント疎通確認は行わない
		long interval = HinemosPropertyCommon.xcloud_cloudlog_agent_monitor_interval.getNumericValue();

		synchronized (this) {
			if (_scheduler == null && monitorList != null && !monitorList.isEmpty() && interval > 0) {
				m_log.info("initAgentMonitorThread(): initialize and start Agent Monitor.");
				_scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "AgentMonitorForCloudLog");
					}
				});
				_scheduler.scheduleWithFixedDelay(new AgentMonitorForCloudLog(), 0, interval, TimeUnit.MILLISECONDS);
			} else {
				if (_scheduler != null && (monitorList == null || monitorList.isEmpty() || interval <= 0)) {
					m_log.info("initAgentMonitorThread(): stop Agent Monitor.");
					_scheduler.shutdownNow();
					_scheduler = null;
				}
			}
		}
	}
	
	/**
	 * エージェント疎通確認を行うワーカークラスです。
	 */
	private class AgentMonitorForCloudLog implements Runnable {
		private Log m_log = LogFactory.getLog(AgentMonitorForCloudLog.class);
		private HashSet<String> prevFacilityIds = new HashSet<String>();

		@Override
		public void run() {

			// 実行エージェントが変更になったことを通知
			if (agentChangedFlg.get()) {
				m_log.info("run(): Running Agent changed.");
				SettingUpdateInfo.getInstance().setCloudLogMonitorUpdateTime(HinemosTime.currentTimeMillis());
				CloudLogManagerUtil.broadcastConfiguredFlowControl();
				agentChangedFlg.set(false);
				prevFacilityIds.clear();
				return;
			}

			// 実行中のエージェントが無効になったことを通知
			for (Entry<String, String> set : currentRunningMap.entrySet()) {
				if (!AgentConnectUtil.isValidAgent(set.getValue())) {
					// 一度無効を通知した後は、再通知しない
					// 実行エージェントが変更するか、対象のエージェントと疎通可能になったら
					// クリアされる
					if (prevFacilityIds.contains((set.getValue()))) {
						m_log.debug("run(): Already notified invalid. FacilityID: " + set.getValue());
						continue;
					}

					m_log.info("run(): Running Agent become invalid. FacilityID: " + set.getValue());
					SettingUpdateInfo.getInstance().setCloudLogMonitorUpdateTime(HinemosTime.currentTimeMillis());
					CloudLogManagerUtil.broadcastConfiguredFlowControl();
					prevFacilityIds.add(set.getValue());
					return;
				} else {
					prevFacilityIds.remove((set.getValue()));
				}
			}
		}
	}
}
