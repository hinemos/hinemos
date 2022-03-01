/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.SdmlControlSettingDuplicate;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.notify.util.NotifyRelationCacheRefreshCallback;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.sdml.bean.SdmlControlSettingFilterInfo;
import com.clustercontrol.sdml.factory.ModifySdmlControl;
import com.clustercontrol.sdml.factory.SelectSdmlControl;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.model.SdmlMonitorTypeMasterInfo;
import com.clustercontrol.sdml.util.QueryUtil;
import com.clustercontrol.sdml.v1.SdmlController;
import com.clustercontrol.sdml.v1.bean.SdmlControlLogDTO;
import com.clustercontrol.sdml.v1.util.SdmlControlSettingCallback;
import com.clustercontrol.util.HinemosTime;

/**
 * SDML機能の管理を行う Session Bean です。<BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 * 
 */
public class SdmlControllerBean {
	private static Log logger = LogFactory.getLog(SdmlControllerBean.class);

	private static final ILock _lock;

	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(SdmlControllerBean.class.getName());

		try {
			_lock.writeLock();

			ArrayList<SdmlControlSettingInfo> cache = getCache();
			if (cache == null) { // not null when clustered
				refreshCache();
			}
		} finally {
			_lock.writeUnlock();
			logger.info("Static Initialization [Thread : " + Thread.currentThread() + ", User : "
					+ (String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID) + "]");
		}
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<SdmlControlSettingInfo> getCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_SDML_CONTROL_SETTING);
		if (logger.isDebugEnabled()) {
			logger.debug("get cache " + AbstractCacheManager.KEY_SDML_CONTROL_SETTING + " : " + cache);
		}
		if (cache == null) {
			return null;
		}
		return (ArrayList<SdmlControlSettingInfo>) cache;
	}

	private static void storeCache(ArrayList<SdmlControlSettingInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (logger.isDebugEnabled()) {
			logger.debug("store cache " + AbstractCacheManager.KEY_SDML_CONTROL_SETTING + " : " + newCache);
		}
		cm.store(AbstractCacheManager.KEY_SDML_CONTROL_SETTING, newCache);
	}

	/**
	 * キャッシュ更新
	 */
	public static void refreshCache() {
		logger.info("refreshCache()");

		long startTime = HinemosTime.currentTimeMillis();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			_lock.writeLock();

			em.clear();
			ArrayList<SdmlControlSettingInfo> logfileCache = new ArrayList<>(
					new SelectSdmlControl().getAllSdmlControlSettingInfoList());
			storeCache(logfileCache);

			logger.info("refresh cache " + (HinemosTime.currentTimeMillis() - startTime) + "ms. size="
					+ logfileCache.size());
		} catch (Exception e) {
			logger.warn("failed refreshing cache.", e);
		} finally {
			_lock.writeUnlock();
		}
	}

	/**
	 * SDML制御設定を新規に作成します。
	 * 
	 * @param info
	 * @return
	 * @throws SdmlControlSettingDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public SdmlControlSettingInfo addSdmlControlSetting(SdmlControlSettingInfo info)
			throws SdmlControlSettingDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		logger.debug("addSdmlControlSetting() : start");
		JpaTransactionManager jtm = null;
		SdmlControlSettingInfo ret = null;

		// SDML制御設定情報を登録
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(info.getOwnerRoleId(),
					(String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean) HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			ModifySdmlControl modifier = new ModifySdmlControl();
			modifier.addControlSetting(info,
					(String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.addCallback(new SdmlControlSettingCallback());
			// コミット後にリフレッシュする
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

			ret = new SelectSdmlControl().getSdmlControlSettingInfo(info.getApplicationId());
		} catch (SdmlControlSettingDuplicate | InvalidSetting | InvalidRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			logger.warn("addSdmlControlSetting() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * SDML制御設定を変更します。
	 * 
	 * @param info
	 * @return
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws SdmlControlSettingNotFound
	 * @throws HinemosUnknown
	 */
	public SdmlControlSettingInfo modifySdmlControlSetting(SdmlControlSettingInfo info)
			throws InvalidRole, SdmlControlSettingNotFound, HinemosUnknown {
		logger.debug("modifySdmlControlSetting() : start");
		JpaTransactionManager jtm = null;
		SdmlControlSettingInfo ret = null;

		// SDML制御設定情報を更新
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			ModifySdmlControl modifier = new ModifySdmlControl();
			modifier.modifyControlSetting(info,
					(String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.addCallback(new SdmlControlSettingCallback());
			// コミット後にリフレッシュする
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

			ret = new SelectSdmlControl().getSdmlControlSettingInfo(info.getApplicationId());
		} catch (InvalidRole | SdmlControlSettingNotFound | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			logger.warn("modifySdmlControlSetting() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * 引数のアプリケーションIDに対するSDML制御設定を削除します。
	 * 
	 * @param applicationIds
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<SdmlControlSettingInfo> deleteSdmlControlSetting(String[] applicationIds)
			throws SdmlControlSettingNotFound, InvalidRole, HinemosUnknown {
		logger.debug("deleteSdmlControlSetting() : start");
		JpaTransactionManager jtm = null;
		List<SdmlControlSettingInfo> retList = new ArrayList<>();

		// SDML制御設定情報を削除
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectSdmlControl selector = new SelectSdmlControl();
			ModifySdmlControl modifier = new ModifySdmlControl();
			for (String applicationId : applicationIds) {
				retList.add(selector.getSdmlControlSettingInfo(applicationId));
				modifier.deleteControlSetting(applicationId);
			}

			jtm.addCallback(new SdmlControlSettingCallback());
			// コミット後にリフレッシュする
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

		} catch (SdmlControlSettingNotFound | InvalidRole | HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			logger.warn("deleteSdmlControlSetting() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return retList;
	}

	/**
	 * 引数で指定したアプリケーションIDに対応するSDML制御設定情報を取得します。
	 * 
	 * @param applicationId
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public SdmlControlSettingInfo getSdmlControlSetting(String applicationId)
			throws SdmlControlSettingNotFound, InvalidRole, HinemosUnknown {
		logger.debug("getSdmlControlSetting() : start");
		JpaTransactionManager jtm = null;
		SdmlControlSettingInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			info = new SelectSdmlControl().getSdmlControlSettingInfo(applicationId);

			jtm.commit();
		} catch (SdmlControlSettingNotFound | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			logger.warn("getSdmlControlSetting() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return info;
	}

	/**
	 * SDML制御設定の一覧を取得します。<br>
	 * 引数でバージョンが指定されている場合はそのバージョンの設定一覧を取得します。<br>
	 * 引数でオーナーロールIDが指定されている場合はそのオーナーロールで参照できる一覧を取得します。
	 * 
	 * @param version
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<SdmlControlSettingInfo> getSdmlControlSettingList(String version, String ownerRoleId)
			throws HinemosUnknown {
		logger.debug("getSdmlControlSettingList() : start");
		JpaTransactionManager jtm = null;
		List<SdmlControlSettingInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			if (version == null || version.isEmpty()) {
				if (ownerRoleId == null || ownerRoleId.isEmpty()) {
					list = new SelectSdmlControl().getAllSdmlControlSettingInfoList();
				} else {
					list = new SelectSdmlControl().getAllSdmlControlSettingInfoList(ownerRoleId);
				}
			} else {
				if (ownerRoleId == null || ownerRoleId.isEmpty()) {
					list = new SelectSdmlControl().getSdmlControlSettingInfoListByVersion(version);
				} else {
					list = new SelectSdmlControl().getSdmlControlSettingInfoListByVersion(version, ownerRoleId);
				}
			}

			jtm.commit();
		} catch (HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			logger.warn("getSdmlControlSettingList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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

	/**
	 * SDML制御設定の一覧を取得します。<br>
	 * 引数で指定された条件に一致する設定の一覧を取得します。
	 * 
	 * @param condition
	 * @param version
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<SdmlControlSettingInfo> getSdmlControlSettingList(SdmlControlSettingFilterInfo condition,
			String version) throws HinemosUnknown {
		logger.debug("getSdmlControlSettingList(condition) : start");
		long start = HinemosTime.currentTimeMillis();
		JpaTransactionManager jtm = null;
		List<SdmlControlSettingInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = new SelectSdmlControl().getSdmlControlSettingInfoList(condition, version);

			jtm.commit();
		} catch (HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			logger.warn(
					"getSdmlControlSettingList(condition) : " + e.getClass().getSimpleName() + ", " + e.getMessage(),
					e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		if (logger.isDebugEnabled()) {
			long end = HinemosTime.currentTimeMillis();
			logger.debug("getSdmlControlSettingList(condition) : end time=" + (end - start));
		}
		return list;
	}

	/**
	 * SDML制御設定を有効化/無効化します。
	 * 
	 * @param applicationIdList
	 * @param validFlag
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<SdmlControlSettingInfo> setSdmlControlSettingStatus(List<String> applicationIdList, boolean validFlag)
			throws SdmlControlSettingNotFound, InvalidRole, HinemosUnknown {
		logger.debug("setSdmlControlSettingStatus() : start");
		List<SdmlControlSettingInfo> retList = new ArrayList<>();

		// null check
		if (applicationIdList == null || applicationIdList.isEmpty()) {
			HinemosUnknown e = new HinemosUnknown("target applicationId is null or empty.");
			logger.info("setSdmlControlSettingStatus() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		for (String applicationId : applicationIdList) {
			if (applicationId == null || applicationId.isEmpty()) {
				continue;
			}
			// 通知情報を取得するためControllerBeanのメソッド利用して取得.
			SdmlControlSettingInfo info = this.getSdmlControlSetting(applicationId);
			// 更新権限チェック.
			QueryUtil.getSdmlControlSettingInfoPK(applicationId, ObjectPrivilegeMode.MODIFY);

			if (validFlag) {
				if (!info.getValidFlg()) {
					info.setValidFlg(true);
					modifySdmlControlSetting(info);
				}
			} else {
				if (info.getValidFlg()) {
					info.setValidFlg(false);
					modifySdmlControlSetting(info);
				}
			}
			retList.add(getSdmlControlSetting(applicationId));
		}

		return retList;
	}

	/**
	 * SDML制御設定のSDML制御ログの収集を有効化/無効化します。
	 * 
	 * @param applicationIdList
	 * @param validFlag
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<SdmlControlSettingInfo> setSdmlControlSettingLogCollector(List<String> applicationIdList, boolean validFlag)
			throws SdmlControlSettingNotFound, InvalidRole, HinemosUnknown {
		logger.debug("setSdmlControlSettingLogCollector() : start");
		List<SdmlControlSettingInfo> retList = new ArrayList<>();

		// null check
		if (applicationIdList == null || applicationIdList.isEmpty()) {
			HinemosUnknown e = new HinemosUnknown("target applicationId is null or empty.");
			logger.info("setSdmlControlSettingLogCollector() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		for (String applicationId : applicationIdList) {
			if (applicationId == null || applicationId.isEmpty()) {
				continue;
			}
			// 通知情報を取得するためControllerBeanのメソッド利用して取得.
			SdmlControlSettingInfo info = this.getSdmlControlSetting(applicationId);
			// 更新権限チェック.
			QueryUtil.getSdmlControlSettingInfoPK(applicationId, ObjectPrivilegeMode.MODIFY);

			if (validFlag) {
				if (!info.getControlLogCollectFlg()) {
					info.setControlLogCollectFlg(true);
					modifySdmlControlSetting(info);
				}
			} else {
				if (info.getControlLogCollectFlg()) {
					info.setControlLogCollectFlg(false);
					modifySdmlControlSetting(info);
				}
			}
			retList.add(getSdmlControlSetting(applicationId));
		}

		return retList;
	}

	/**
	 * SDML監視種別の一覧を取得します。
	 * 
	 * @param version
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<SdmlMonitorTypeMasterInfo> getSdmlMonitorTypeMstList() throws HinemosUnknown {
		logger.debug("getSdmlMonitorTypeMstList() : start");
		JpaTransactionManager jtm = null;
		List<SdmlMonitorTypeMasterInfo> list = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = QueryUtil.getAllSdmlMonitorTypeMst();

			jtm.commit();
		} catch (Exception e) {
			logger.warn("getSdmlMonitorTypeMstList() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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

	/**
	 * 
	 * <注意！> このメソッドはAgentユーザ以外で呼び出さないこと！
	 * <注意！> キャッシュの都合上、Agentユーザ以外から呼び出すと、正常に動作しません。
	 * 
	 * facilityIDごとのSDML制御設定一覧リストを返します。
	 * 
	 * @param facilityId
	 * @param version
	 * @return
	 * @throws HinemosUnknown
	 * @throws SdmlControlSettingNotFound
	 * 
	 */
	public List<SdmlControlSettingInfo> getSdmlControlSettingListForFacilityId(String facilityId, String version)
			throws SdmlControlSettingNotFound, HinemosUnknown {
		List<SdmlControlSettingInfo> ret = new ArrayList<SdmlControlSettingInfo>();
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 並列してキャッシュ更新処理が実行されている場合、更新処理完了を待機しない（更新前・後のどちらが取得されるか保証されない）
			// (部分書き換えでなく全置換えのキャッシュ更新特性、ロックに伴う処理コストの観点から参照ロックは意図的に取得しない)
			List<SdmlControlSettingInfo> controlSettingList = getCache();

			for (SdmlControlSettingInfo controlSettingInfo : controlSettingList) {
				if (version != null && !version.isEmpty()) {
					// バージョンの指定がある場合は該当バージョンのみ
					if (!version.equals(controlSettingInfo.getVersion())) {
						continue;
					}
				}
				String scope = controlSettingInfo.getFacilityId();
				if (new RepositoryControllerBean().containsFacilityIdWithoutList(scope, facilityId,
						controlSettingInfo.getOwnerRoleId())) {
					ret.add(controlSettingInfo);
				}
			}

			jtm.commit();
		} catch (Exception e) {
			logger.warn("getSdmlControlSettingListForFacilityId() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * 受け取ったSDML制御ログに応じて自動制御を実行します。
	 * 
	 * @param facilityId
	 * @param logList
	 * @throws HinemosUnknown
	 */
	public void run(String facilityId, List<SdmlControlLogDTO> logList) throws HinemosUnknown {
		logger.debug("run() : start");
		if (logList == null) {
			logger.debug("run() : logList is null.");
			return;
		}

		JpaTransactionManager jtm = null;
		for (SdmlControlLogDTO log : logList) {
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				// ファシリティIDのチェックはアプリケーションIDからSDML制御設定を取得後に行うためここでは実施しない

				// メイン処理
				SdmlController controller = new SdmlController();
				List<OutputBasicInfo> notifyInfoList = controller.run(facilityId, log);

				// 通知
				jtm.addCallback(new NotifyCallback(notifyInfoList));
				jtm.commit();
			} catch (HinemosUnknown e) {
				logger.warn("run() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				if (jtm != null) {
					jtm.rollback();
				}
				throw e;
			} catch (Exception e) {
				logger.error("run() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	}

	/**
	 * SDMLログリーダから受け取った通知情報をSDML制御設定の通知に対して実行します。
	 * 
	 * @param outputBasicInfo
	 * @param facilityIdList
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 */
	public void sendMessage(OutputBasicInfo outputBasicInfo, ArrayList<String> facilityIdList)
			throws HinemosUnknown, FacilityNotFound {
		logger.debug("sendMessage() : start");
		// SDML制御設定情報を取得
		List<String> facilityList = null;
		SdmlControlSettingInfo controlSetting = null;
		try {
			controlSetting = getSdmlControlSetting(outputBasicInfo.getMonitorId());
			facilityList = FacilitySelector.getFacilityIdList(controlSetting.getFacilityId(),
					controlSetting.getOwnerRoleId(), 0, false, false);
		} catch (SdmlControlSettingNotFound | InvalidRole e) {
			logger.warn("sendMessage() : cannnot get control setting. id=" + outputBasicInfo.getMonitorId() + ", "
					+ e.getMessage(), e);
		}
		List<OutputBasicInfo> notifyList = new ArrayList<>();
		for (String facilityId : facilityIdList) {
			if (facilityList != null && !facilityList.contains(facilityId)) {
				logger.debug("sendMessage() : not match facilityId(" + facilityId + ")");
				continue;
			}
			String scopeText = "";
			try {
				NodeInfo nodeInfo = new RepositoryControllerBean().getNode(facilityId);
				scopeText = nodeInfo.getFacilityName();
			} catch (FacilityNotFound e) {
				throw e;
			}
			// 通知出力情報をディープコピーする
			OutputBasicInfo clonedInfo = outputBasicInfo.clone();
			clonedInfo.setFacilityId(facilityId);
			clonedInfo.setScopeText(scopeText);
			clonedInfo.setNotifyGroupId(controlSetting.getNotifyGroupId());
			notifyList.add(clonedInfo);
		}
		// 通知
		NotifyControllerBean.notify(notifyList);
	}
}
