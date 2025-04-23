/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.session.CheckFacility;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NodeConfigSettingDuplicate;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.NodeHistoryNotFound;
import com.clustercontrol.fault.NodeHistoryRegistered;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.notify.util.NotifyRelationCacheRefreshCallback;
import com.clustercontrol.repository.bean.NodeConfigRunCollectInfo;
import com.clustercontrol.repository.bean.NodeConfigSetting;
import com.clustercontrol.repository.factory.NodeConfigRegister;
import com.clustercontrol.repository.factory.NodeConfigRunCollectManager;
import com.clustercontrol.repository.factory.NodeConfigSettingModifier;
import com.clustercontrol.repository.factory.NodeConfigSettingSelector;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfo;
import com.clustercontrol.repository.model.NodeHistory;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.util.FacilityUtil;
import com.clustercontrol.repository.util.NodeConfigCollectReferenceTimeUtil;
import com.clustercontrol.repository.util.NodeConfigRunCollectCallback;
import com.clustercontrol.repository.util.NodeConfigRunCollectEndCallback;
import com.clustercontrol.repository.util.NodeConfigSettingChangedNotificationCallback;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.repository.util.RepositoryValidator;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

import jakarta.persistence.EntityExistsException;

/**
 * 構成情報取得関連の管理を行う Session Bean <BR>
 * 他機能からの Entity Bean へのアクセスは、Session Bean を介して行う.
 * 
 * @version 6.2.0
 * @since 6.2.0
 * 
 */
public class NodeConfigSettingControllerBean implements CheckFacility {

	// ログ出力用インスタンス
	private static Log m_log = LogFactory.getLog(NodeConfigSettingControllerBean.class);

	/**
	 * 対象構成情報を新規に追加します。<BR>
	 *
	 * @param info 追加する対象構成情報
	 * @param isImport true:設定インポートエクスポートから実行、false:それ以外
	 * @return
	 * @throws NodeConfigSettingDuplicate
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public NodeConfigSettingInfo addNodeConfigSettingInfo(NodeConfigSettingInfo info, boolean isImport)
			throws NodeConfigSettingDuplicate, InvalidSetting, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 入力チェック
			RepositoryValidator.validateNodeConfigSettingInfo(info, true);

			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(info.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			// 対象構成情報登録
			NodeConfigSettingModifier.addNodeConfigSettingInfo(
					info,
					(String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.addCallback(new NodeConfigSettingChangedNotificationCallback());
			// コールバックメソッド設定
			if (!isImport) {
				addImportNodeConfigSettingInfoCallback(jtm);
			}

			jtm.commit();

			return new NodeConfigSettingSelector().getNodeConfigSettingInfo(info.getSettingId());
		} catch (EntityExistsException e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw new NodeConfigSettingDuplicate(e.getMessage(), e);
		} catch (InvalidSetting e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("addNodeConfigSettingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * 対象構成情報を更新します。<BR>
	 *
	 * @param info　更新する対象構成情報
	 * @param isImport true:設定インポートエクスポートから実行、false:それ以外
	 * @return
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws NodeConfigSettingNotFound
	 * @throws HinemosUnknown
	 */
	public NodeConfigSettingInfo modifyNodeConfigSettingInfo(NodeConfigSettingInfo info, boolean isImport)
			throws InvalidSetting, InvalidRole, NodeConfigSettingNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// クライアントからはオーナーロールIDが来ないので最新の情報から取得して設定(カレンダIdのバリデートで必要)
			NodeConfigSettingInfo nodeConfigSettingInfoPK = QueryUtil.getNodeConfigSettingInfoPK(info.getSettingId(), ObjectPrivilegeMode.READ);
			info.setOwnerRoleId(nodeConfigSettingInfoPK.getOwnerRoleId());

			// 入力チェック
			RepositoryValidator.validateNodeConfigSettingInfo(info, false);

			// 対象構成情報更新
			NodeConfigSettingModifier.modifyNodeConfigSettingInfo(
					info,
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.addCallback(new NodeConfigSettingChangedNotificationCallback());
			// コールバックメソッド設定
			if (!isImport) {
				addImportNodeConfigSettingInfoCallback(jtm);
			}

			jtm.commit();

			return new NodeConfigSettingSelector().getNodeConfigSettingInfo(info.getSettingId());
		} catch (InvalidSetting | InvalidRole | NodeConfigSettingNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyNodeConfigSettingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null){
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * 対象構成情報の新規登録／変更時に呼び出すコールバックメソッドを設定
	 * 
	 * 設定インポートエクスポートでCommit後に呼び出すものだけ定義
	 * 
	 * @param jtm JpaTransactionManager
	 */
	public void addImportNodeConfigSettingInfoCallback(JpaTransactionManager jtm) {
		// 通知リレーション情報のキャッシュ更新
		jtm.addCallback(new NotifyRelationCacheRefreshCallback());
	}

	/**
	 * 対象構成情報を削除します。<BR>
	 *
	 * @param settingIds settingIdの配列
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<NodeConfigSettingInfo> deleteNodeConfigSettingInfo(String[] settingIds) throws InvalidRole, NodeConfigSettingNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<NodeConfigSettingInfo> retList = new ArrayList<>();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (String settingId : settingIds) {
				NodeConfigSettingInfo ret = new NodeConfigSettingSelector().getNodeConfigSettingInfo(settingId);
				retList.add(ret);

				// 対象構成情報削除
				NodeConfigSettingModifier.deleteNodeConfigSettingInfo(
						settingId, 
						(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
			}
			jtm.addCallback(new NodeConfigSettingChangedNotificationCallback());
			// コミット後にリフレッシュする
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());

			jtm.commit();

		} catch (InvalidRole | NodeConfigSettingNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteNodeConfigSettingInfo() : "
					+ e.getClass().getSimpleName() +", " + e.getMessage(), e);
			if (jtm != null){
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return retList;
	}

	/**
	 * 対象構成情報の収集を有効化/無効化します。<BR>
	 *
	 * @param settingId　対象構成情報
	 * @throws HinemosException 
	 */
	public NodeConfigSettingInfo setStatusNodeConfigSetting(String settingId, boolean validFlag)
			throws InvalidSetting, InvalidRole, NodeConfigSettingNotFound, HinemosUnknown {
		// null check
		if(settingId == null || "".equals(settingId)){
			HinemosUnknown e = new HinemosUnknown("target settingId is null or empty.");
			m_log.info("setStatusNodeConfigSetting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		NodeConfigSettingInfo retInfo = null;
		NodeConfigSettingInfo info;
		try{
			// 通知情報を取得するためControllerBeanのメソッド利用して取得.
			info = this.getNodeConfigSettingInfo(settingId);
			// 更新権限チェック.
			QueryUtil.getNodeConfigSettingInfoPK(settingId, ObjectPrivilegeMode.MODIFY);
			if(validFlag){
				if(!info.getValidFlg()){
					info.setValidFlg(true);
					retInfo = modifyNodeConfigSettingInfo(info, false);
				}
			} else{
				if(info.getValidFlg()){
					info.setValidFlg(false);
					retInfo = modifyNodeConfigSettingInfo(info, false);
				}
			}
		} catch (InvalidSetting | InvalidRole | NodeConfigSettingNotFound e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("setStatusNodeConfigSetting() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return retInfo;
	}

	/**
	 * 複数の対象構成情報の収集を有効化/無効化します。<BR>
	 * @param settingIds 対象構成情報
	 * @param validFlag 有効/無効フラグ
	 * @return
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws NodeConfigSettingNotFound
	 * @throws HinemosUnknown
	 */
	public List<NodeConfigSettingInfo> setStatusNodeConfigSetting(List<String> settingIds, boolean validFlag)
			throws InvalidSetting, InvalidRole, NodeConfigSettingNotFound, HinemosUnknown {

		List<NodeConfigSettingInfo> retList = new ArrayList<>();
		for (String settingId : settingIds) {
			retList.add(setStatusNodeConfigSetting(settingId, validFlag));
		}
		return retList;
	}

	/**
	 * settingIDを条件としてNodeConfigSettingInfo を取得します。
	 *
	 * @param settingId settingId
	 * @return NodeConfigSettingInfo
	 * @throws NodeConfigSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public NodeConfigSettingInfo getNodeConfigSettingInfo(String settingId)
			throws NodeConfigSettingNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		NodeConfigSettingInfo entity = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 取得処理
			entity = new NodeConfigSettingSelector().getNodeConfigSettingInfo(settingId);

			jtm.commit();
		} catch (NodeConfigSettingNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getNodeConfigSettingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return entity;
	}

	/**
	 * ファシリティIDリストを条件としてNodeConfigSetting を取得します。
	 *
	 * @param facilityIds ファシリティIDリスト
	 * @return 対象構成情報リスト
	 * @throws FacilityNotFound
	 * @throws NodeConfigSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<NodeConfigSetting> getNodeConfigSettingListByFacilityIds(List<String> facilityIds)
			throws FacilityNotFound, NodeConfigSettingNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<NodeConfigSetting> list = new ArrayList<>();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// データ未存在の場合は空で返す
			if (facilityIds == null || facilityIds.size() <= 0) {
				return list;
			}
			String referenceTime = NodeConfigCollectReferenceTimeUtil.getReferenceTime();
			Long loadDstrbRange = NodeConfigCollectReferenceTimeUtil.getLoadDstrbRange();

			// ファシリティIDに対応した対象構成情報を取得
			for (String facilityId : facilityIds) {
				List<NodeConfigSettingInfo> infoList 
				= new NodeConfigSettingSelector().getNodeConfigSettingListByFacilityId(facilityId);
				if (infoList == null) {
					continue;
				}
				for (NodeConfigSettingInfo settingInfo : infoList) {
					if (settingInfo == null) {
						continue;
					}
					CalendarInfo calInfo = null;
					if (settingInfo.getCalendarId() != null && !settingInfo.getCalendarId().equals("")) {
						calInfo = new CalendarControllerBean().getCalendarFull(settingInfo.getCalendarId());
					}
					List<String> itemInfo = new ArrayList<>();
					if (settingInfo.getNodeConfigSettingItemList() != null) {
						for (NodeConfigSettingItemInfo item : settingInfo.getNodeConfigSettingItemList()) {
							itemInfo.add(item.getSettingItemId());
						}
					}
					NodeConfigSetting info = new NodeConfigSetting (
							settingInfo.getSettingId(),
							settingInfo.getSettingName(),
							facilityId,
							settingInfo.getRunInterval() * 1000,
							calInfo,
							itemInfo, 
							settingInfo.getNodeConfigCustomList(), 
							referenceTime, 
							loadDstrbRange);
					list.add(info);
				}
			}

			jtm.commit();
		} catch (NodeConfigSettingNotFound | FacilityNotFound | InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getNodeConfigSettingListByFacilityIds() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * 対象構成情報一覧取得
	 * 
	 * @return 対象構成情報取得
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<NodeConfigSettingInfo> getNodeConfigSettingList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 対象構成情報一覧を取得
		List<NodeConfigSettingInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = new NodeConfigSettingSelector().getAllNodeConfigSettingList();
			jtm.commit();
		} catch (InvalidRole e) {
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
			m_log.warn("getNodeConfigSettingList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * 構成情報登録
	 * 
	 * @param registerDatetime 構成情報送信日時
	 * @param nodeInfo 登録する構成情報
	 * @throws InvalidSetting
	 * @throws FacilityNotFound
	 * @throws NodeConfigSettingNotFound
	 * @throws NodeConfigSettingDuplicate
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void registerNodeConfigInfo(Long registerDatetime, NodeInfo nodeInfo)
			throws InvalidSetting, FacilityNotFound, NodeConfigSettingNotFound, NodeConfigSettingDuplicate, InvalidRole, NodeHistoryRegistered, HinemosUnknown {

		JpaTransactionManager jtm = null;

		// 対象構成情報
		NodeConfigSettingInfo settingInfo = null;

		try{

			// トランザクションがすでに開始されている場合は処理終了
			jtm = new JpaTransactionManager();
			jtm.begin(true);

			/** 対象構成情報設定ID、ノード情報、構成情報送信日時、対象構成情報チェック */
			// 構成情報送信日時チェック
			if (registerDatetime == null) {
				InvalidSetting e = new InvalidSetting("registerDatetime is empty.");
				throw e;
			}

			// ノード情報チェック
			// ノード情報Nullチェック
			if(nodeInfo == null){
				InvalidSetting e = new InvalidSetting("nodeInfo is empty.");
				throw e;
			}

			// 対象構成構成情報ID Nullチェック
			if (nodeInfo.getNodeConfigSettingId() == null || "".equals(nodeInfo.getNodeConfigSettingId())) {
				InvalidSetting e = new InvalidSetting("nodeConfigSettingId is empty.");
				throw e;
			}

			// ファシリティID Nullチェック
			if(nodeInfo.getFacilityId() == null || "".equals(nodeInfo.getFacilityId())){
				InvalidSetting e = new InvalidSetting("facilityId is empty.");
				throw e;
			}

			// 存在確認、権限確認
			FacilityInfo facilityInfo = QueryUtil.getFacilityPK(nodeInfo.getFacilityId(), ObjectPrivilegeMode.MODIFY);

			// ノードチェック
			if (!FacilityUtil.isNode(facilityInfo)) {
				InvalidSetting e = new InvalidSetting("facilityId is not node. "
						+ ": facilityId=" + nodeInfo.getFacilityId());
				throw e;
			}

			// 対象構成情報設定を取得する
			settingInfo = new NodeConfigSettingSelector().getNodeConfigSettingInfoByFacilityId(
					nodeInfo.getNodeConfigSettingId(), nodeInfo.getFacilityId(), nodeInfo.getNodeConfigAcquireOnce().booleanValue());
			// 当該対象構成情報設定が存在しない場合エラー
			if (settingInfo == null) {
				InvalidSetting e = new InvalidSetting("There is no valid Node Config Setting Info. "
						+ ": settingId=" + nodeInfo.getNodeConfigSettingId()
						+ ", facilityId=" + nodeInfo.getFacilityId());
				throw e;
			}

			// 同時刻に取得した情報が既に登録済の場合はエラー(SocketTimeOutによる再送などで発生).
			NodeHistory registeredHistory = null;
			try{
				registeredHistory = QueryUtil.getNodeHistoryPk(nodeInfo.getFacilityId(), registerDatetime);
			} catch(NodeHistoryNotFound e){
				m_log.debug("This is the first regstration");
			}
			if (registeredHistory != null) {
				NodeHistoryRegistered e = new NodeHistoryRegistered("The history at the same time is already registered."
						+ " It may be caused by java.net.SocketTimeoutException."
						+ " Check 'Agent.log' and if the exception has occured,"
						+ " please review the setting value of 'connect.timeout' in Agent.properties."
						+ " registerDatetime(msec)=[" + registerDatetime + "]"
						+ ", registerDatetime(date)=[" + new Date(registerDatetime).toString() + "]");
				throw e;
			}

			// 構成情報登録処理
			List<OutputBasicInfo> notifyInfoList = new NodeConfigRegister(
					registerDatetime, 
					(String) HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					nodeInfo, 
					settingInfo).exec();

			// 通知処理
			jtm.addCallback(new NotifyCallback(notifyInfoList));
			if(nodeInfo.getNodeConfigAcquireOnce().booleanValue()){
				jtm.addCallback(new NodeConfigRunCollectEndCallback(nodeInfo.getFacilityId(), settingInfo.getSettingId()));
				m_log.debug("registerNodeConfigInfo() : added NodeConfigRunCollectEndCallback.");
			}

			// ノード情報変更時に呼び出すコールバックメソッド
			new RepositoryControllerBean().addModifyNodeCallback(nodeInfo, true);

			jtm.commit();

		} catch (Exception e) {
			m_log.warn("registerNodeConfigInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());

			// NodeHistoryRegisteredは既に登録済みの情報と同じ情報が来ているので、「構成情報の取得に失敗したエラーではない」ためそのまま返却
			if (e instanceof NodeHistoryRegistered) {
				throw e;
			}

			String[] messageDetail = {};
			AplLogger.put(InternalIdCommon.NODE_CONFIG_SETTING_SYS_002, messageDetail, e.getMessage());

			if (e instanceof EntityExistsException) {
				if (jtm != null) {
					jtm.rollback();
				}
				throw new NodeConfigSettingDuplicate(e.getMessage(), e);
			} else {
				if (jtm != null) {
					jtm.rollback();
				}
				throw e;
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * ファシリティが利用されているか確認する。
	 * 
	 * @throws UsedFacility
	 * @throws InvalidRole
	 */
	@Override
	public void isUseFacilityId(String facilityId) throws UsedFacility {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			List<NodeConfigSettingInfo> infoCollection
			= QueryUtil.getNodeConfigSettingInfoByFacilityId_NONE(facilityId);
			if (infoCollection != null && infoCollection.size() > 0) {
				// ID名を取得する
				StringBuilder sb = new StringBuilder();
				sb.append(MessageConstant.NODE_CONFIG_SETTING.getMessage() + " : ");
				for (NodeConfigSettingInfo entity : infoCollection) {
					sb.append(entity.getSettingId());
					sb.append(", ");
				}
				UsedFacility e = new UsedFacility(sb.toString());
				m_log.info("isUseFacilityId() : " + e.getClass().getSimpleName() +
						", " + facilityId + ", " + e.getMessage());
				throw e;
			}
			jtm.commit();
		} catch (UsedFacility e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("isUseFacilityId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 引数で指定された設定の構成情報取得を即時実行します。<BR>
	 * 
	 * @param setting
	 *            構成情報収集設定
	 * @return 即時実行の負荷分散間隔
	 * @throws HinemosUnknown
	 * 
	 */
	public Long runCollectNodeConfig(String settingId) throws FacilityNotFound, HinemosUnknown, NodeConfigSettingNotFound, InvalidRole {

		Long instructedTime = Long.valueOf(HinemosTime.currentTimeMillis());
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			NodeConfigSettingInfo setting = new NodeConfigSettingControllerBean().getNodeConfigSettingInfo(settingId);
			RepositoryControllerBean controller = new RepositoryControllerBean();
			ArrayList<String> allFacilityIdList = controller.getExecTargetFacilityIdList(setting.getFacilityId(), setting.getOwnerRoleId());
			List<String> runFacilityIdList = new ArrayList<String>();
			for (String facilityId : allFacilityIdList) {
				// オブジェクト権限を持っているFacilityIDのみに絞り込む.
				try {
					QueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.EXEC);
				} catch (InvalidRole e) {
					m_log.debug("runCollectNodeConfig() : skipped because of InvalidRole. facilityID=" + facilityId);
					continue;
				} catch (FacilityNotFound e) {
					m_log.debug(
							"runCollectNodeConfig() : skipped because of FacilityNotFound. facilityID=" + facilityId);
					continue;
				}
				runFacilityIdList.add(facilityId);
			}

			// 絞込み結果、実質的な実行対象が存在するかチェック.
			if (runFacilityIdList.isEmpty()) {
				FacilityNotFound e = new FacilityNotFound("existed no valid FacilityID.");
				m_log.info("runCollectNodeConfig() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			// キャッシュへの登録.
			NodeConfigRunCollectManager.initialize();
			runFacilityIdList = NodeConfigRunCollectManager.addRunCollectMap(runFacilityIdList, instructedTime,
					setting.getSettingId());
			jtm.addCallback(new NodeConfigRunCollectCallback(runFacilityIdList));
			jtm.commit();
		} catch (FacilityNotFound | NodeConfigSettingNotFound | InvalidRole e1) {
			if (jtm != null) {
				jtm.close();
			}
			throw e1;
		} catch (Exception e) {
			m_log.warn("runCollectNodeConfig() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.close();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return NodeConfigRunCollectManager.getDistributionTime();
	}

	/**
	 * ファシリティIDリストを条件として即時実行に必要な情報を取得します。
	 *
	 * @param facilityIds
	 *            ファシリティIDリスト
	 * @return 対象構成情報リスト
	 * @throws HinemosUnknown
	 */
	public NodeConfigRunCollectInfo getNodeConfigRunCollectInfo(List<String> facilityIds) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		NodeConfigRunCollectInfo result = new NodeConfigRunCollectInfo();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// データない場合はnullで返す.
			if (facilityIds == null || facilityIds.size() <= 0) {
				m_log.warn("getNodeConfigRunCollectInfo() : skipped because of no facilityIds.");
				return null;
			}

			// ファシリティID毎に必要な情報を取得.
			for (String facilityId : facilityIds) {
				// 即時実行情報を取得.
				HashMap<NodeConfigSetting, Long> runCollectMap = null;
				runCollectMap = NodeConfigRunCollectManager.getRunCollectMap(facilityId);
				if (runCollectMap == null || runCollectMap.isEmpty()) {
					continue;
				}

				// 取得できていたら返却用のDTOにセット.
				result.getInstructedInfoMap().putAll(runCollectMap);
				m_log.debug("getNodeConfigRunCollectInfo() : succeeded to add NodeConfigRunCollectInstructedInfo."
						+ " facilityID=" + facilityId);
			}
			// 負荷分散間隔.
			Long loadDistributionTime = NodeConfigRunCollectManager.getDistributionTime();
			result.setLoadDistributionTime(loadDistributionTime);

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getNodeConfigRunCollectInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return result;
	}

	/**
	 * ファシリティIDリストを条件として即時実行に必要な情報を取得します。
	 *
	 * @param facilityIds
	 *            ファシリティIDリスト
	 * @return 対象構成情報リスト
	 * @throws FacilityNotFound
	 * @throws NodeConfigSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void stopNodeConfigRunCollect(List<String> facilityIds) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// データ未存在の場合は終了.
			if (facilityIds == null || facilityIds.size() <= 0) {
				m_log.warn("stopNodeConfigRunCollect() : skipped because of no facilityIds.");
				return;
			}

			// ファシリティID毎に停止させる.
			for (String facilityId : facilityIds) {
				NodeConfigRunCollectManager.stopRunCollect(facilityId);
			}

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("stopNodeConfigRunCollect() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return;
	}
}
