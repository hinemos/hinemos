/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.session.CheckFacility;
import com.clustercontrol.commons.util.AsyncTaskPersistentConfig;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MultiSmtpServerUtil;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.jobmanagement.factory.FullJob;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventUserExtensionItemInfo;
import com.clustercontrol.monitor.factory.SelectEventHinemosProperty;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.EventUtil;
import com.clustercontrol.notify.bean.EventNotifyInfo;
import com.clustercontrol.notify.bean.NotifyCheckIdResultInfo;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.factory.ModifyNotify;
import com.clustercontrol.notify.factory.ModifyNotifyRelation;
import com.clustercontrol.notify.factory.NotifyDispatcher;
import com.clustercontrol.notify.factory.SelectNotify;
import com.clustercontrol.notify.factory.SelectNotifyRelation;
import com.clustercontrol.notify.model.NotifyCloudInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfraInfo;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.notify.model.NotifyLogEscalateInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.util.MonitorStatusCacheRemoveByFacilityIdCallback;
import com.clustercontrol.notify.util.MonitorStatusCacheRemoveCallback;
import com.clustercontrol.notify.util.NotifyCacheRefreshCallback;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.notify.util.NotifyRelationCacheRefreshCallback;
import com.clustercontrol.notify.util.NotifyValidator;
import com.clustercontrol.notify.util.OutputEvent;
import com.clustercontrol.notify.util.OutputStatus;
import com.clustercontrol.notify.util.QueryUtil;
import com.clustercontrol.notify.util.RunJob;
import com.clustercontrol.notify.util.SendMail;
import com.clustercontrol.notify.util.SendSyslog;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;

/**
 * 通知機能の管理を行う Session Bean です。<BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 */
public class NotifyControllerBean implements CheckFacility {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( NotifyControllerBean.class );

	private static SendMail m_reportingSendMail = null;

	private static ConcurrentHashMap<String, Object> m_lockObjectMap = new ConcurrentHashMap<String, Object>();

	/**
	 * 通知情報を作成します。
	 *
	 * @param info 作成対象の通知情報
	 * @return NotifyInfo 作成に成功した通知情報
	 * @throws NotifyDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.factory.AddNotify#add(NotifyInfo)
	 */
	public NotifyInfo addNotify(NotifyInfo info)
			throws NotifyDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		return addNotify(info, false);
	}

	/**
	 * 通知情報を作成します。
	 *
	 * @param info 作成対象の通知情報
	 * @param isImport true:設定インポートエクスポートから実行、false:それ以外
	 * @return NotifyInfo 作成に成功した通知情報
	 * @throws NotifyDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.factory.AddNotify#add(NotifyInfo)
	 */
	public NotifyInfo addNotify(NotifyInfo info, boolean isImport)
			throws NotifyDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		NotifyInfo ret = null;

		// 通知情報を登録
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			NotifyValidator.validateNotifyInfo(info, true);

			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(info.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			ModifyNotify notify = new ModifyNotify();
			notify.add(info,(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			// コミット後にキャッシュクリアを行うため、コールバックを追加する
			jtm.addCallback(new NotifyCacheRefreshCallback());

			// コールバックメソッド設定
			if (!isImport) {
				addImportNotifyCallback(jtm);
			}

			jtm.commit();

			SelectNotify selectNotify = new SelectNotify();
			ret = selectNotify.getNotify(info.getNotifyId());
		} catch (NotifyDuplicate | InvalidSetting | HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("addNotify() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * 通知情報を変更します。
	 *
	 * @param info 変更対象の通知情報
	 * @return NotifyInfo 変更に成功した通知情報
	 * @throws NotifyDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.notify.factory.ModifyNotify#modify(NotifyInfo)
	 */
	public NotifyInfo modifyNotify(NotifyInfo info)
			throws NotifyDuplicate, InvalidRole, HinemosUnknown, InvalidSetting, NotifyNotFound {
		return modifyNotify(info, false);
	}

	/**
	 * 通知情報を変更します。
	 *
	 * @param info 変更対象の通知情報
	 * @param isImport true:設定インポートエクスポートから実行、false:それ以外
	 * @return NotifyInfo 変更に成功した通知情報
	 * @throws NotifyDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.notify.factory.ModifyNotify#modify(NotifyInfo)
	 */
	public NotifyInfo modifyNotify(NotifyInfo info, boolean isImport)
			throws NotifyDuplicate, InvalidRole, HinemosUnknown, InvalidSetting, NotifyNotFound {
		JpaTransactionManager jtm = null;
		NotifyInfo ret = null;

		// 通知情報を更新
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// クライアントからはオーナーロールIDが来ないので最新の情報から取得して設定(カレンダIdのバリデートで必要)
			NotifyInfo notifyInfo = QueryUtil.getNotifyInfoPK(info.getNotifyId(), ObjectPrivilegeMode.READ);
			info.setOwnerRoleId(notifyInfo.getOwnerRoleId());

			//入力チェック
			NotifyValidator.validateNotifyInfo(info, false);

			ModifyNotify notify = new ModifyNotify();
			notify.modify(info,(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			// コミット後にキャッシュクリアを行うため、コールバックを追加する
			jtm.addCallback(new NotifyCacheRefreshCallback());
			// コールバックメソッド設定
			if (!isImport) {
				addImportNotifyCallback(jtm);
			}

			jtm.commit();

			SelectNotify selectNotify = new SelectNotify();
			ret = selectNotify.getNotify(info.getNotifyId());
		} catch (NotifyDuplicate | InvalidSetting | HinemosUnknown | InvalidRole | NotifyNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("modifyNotify() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * 通知情報の新規登録／変更時に呼び出すコールバックメソッドを設定
	 * 
	 * 設定インポートエクスポートでCommit後に呼び出すものだけ定義
	 * 
	 * @param jtm JpaTransactionManager
	 */
	public void addImportNotifyCallback(JpaTransactionManager jtm) {
		// 通知リレーション情報のキャッシュ更新
		jtm.addCallback(new NotifyRelationCacheRefreshCallback());
	}

	/**
	 * 通知情報を削除します。
	 *
	 * @param notifyIds 削除対象の通知IDリスト
	 * @return List<NotifyInfo> 削除に成功した通知情報
	 * @throws NotifyDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.notify.factory.DeleteNotify#delete(String)
	 */
	public List<NotifyInfo> deleteNotify(String[] notifyIds) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		List<NotifyInfo> retList = new ArrayList<>(); 

		// 通知情報を削除
		ModifyNotify notify = new ModifyNotify();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectNotify selectNotify = new SelectNotify();
			for (String notifyId : notifyIds) {
				retList.add(selectNotify.getNotify(notifyId));
				notify.delete(notifyId);
			}

			// コミット後にキャッシュクリアを行うため、コールバックを追加する
			jtm.addCallback(new NotifyCacheRefreshCallback());
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

			// ジョブ定義のキャッシュも合わせて更新
			FullJob.updateCacheForNotifyId(notifyIds);

		} catch(NotifyNotFound | HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("deleteNotify() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return retList;
	}

	/**
	 * 引数で指定された通知情報を返します。
	 *
	 * @param notifyId 取得対象の通知ID
	 * @param notifyType 取得対象の通知種別
	 * @return 通知情報
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.factory.SelectNotify#getNotify(String)
	 */
	public NotifyInfo getNotify(String notifyId, Integer notifyType) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知情報を取得
		SelectNotify notify = new SelectNotify();
		NotifyInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			info = notify.getNotify(notifyId, notifyType);
			jtm.commit();
		} catch (NotifyNotFound | HinemosUnknown | InvalidRole e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNotify() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return info;
	}

	/**
	 * 通知情報一覧を返します。
	 *
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.notify.factory.SelectNotify#getNotifyList()
	 */
	public ArrayList<NotifyInfo> getNotifyList() throws NotifyNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知一覧を取得
		SelectNotify notify = new SelectNotify();
		ArrayList<NotifyInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = notify.getNotifyList();
			jtm.commit();
		} catch (NotifyNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNotifyList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}

	/**
	 * 指定種別の通知情報一覧を返します。
	 *
	 * @param notifyType 通知種別
	 * @return 通知情報一覧
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public ArrayList<NotifyInfo> getNotifyList(Integer notifyType) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知一覧を取得
		SelectNotify notify = new SelectNotify();
		ArrayList<NotifyInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = notify.getNotifyList(notifyType);
			jtm.commit();
		} catch (NotifyNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNotifyList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}
	
	/**
	 * オーナーロールIDを指定して通知情報一覧を返します。
	 *
	 * @param ownerRoleId オーナーロールID
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public ArrayList<NotifyInfo> getNotifyListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知一覧を取得
		SelectNotify notify = new SelectNotify();
		ArrayList<NotifyInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = notify.getNotifyListByOwnerRole(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNotifyListByOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}

	/**
	 * オーナーロールIDを指定して指定種別の通知情報一覧を返します。
	 *
	 * @param ownerRoleId オーナーロールID
	 * @param notifyType 通知種別
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<NotifyInfo> getNotifyListByOwnerRole(String ownerRoleId, Integer notifyType) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知一覧を取得
		SelectNotify notify = new SelectNotify();
		ArrayList<NotifyInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = notify.getNotifyListByOwnerRole(ownerRoleId, notifyType);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getNotifyListByOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}

	/**
	 * 指定された通知種別の通知情報一覧を返します。
	 *
	 * @param notifyType 指定必須
	 * @param ownerRoleId 絞り込み条件として不要な場合はnullでよい
	 * @return 通知情報一覧
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.notify.factory.SelectNotify#getNotifyListByNotifyType
	 */
	public ArrayList<NotifyInfo> getNotifyListByNotifyType(Integer notifyType ,String ownerRoleId)  throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		// 通知一覧を取得
		SelectNotify notify = new SelectNotify();
		ArrayList<NotifyInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = notify.getNotifyListByNotifyType(notifyType, ownerRoleId);
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getNotifyListByNotifyType() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}

	/**
	 * 通知グループに対応する通知を取得します。
	 *
	 * @param notifyGroupId  通知グループID
	 * @return 通知
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public  ArrayList<NotifyRelationInfo> getNotifyRelation(String notifyGroupId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		SelectNotifyRelation notify = new SelectNotifyRelation();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			ArrayList<NotifyRelationInfo> info = notify.getNotifyRelation(notifyGroupId);
			jtm.commit();

			return info;
		} catch (Exception e) {
			m_log.warn("getNotifyRelation(notifyGroupId) : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}


	/**
	 * 通知グループを変更します。
	 *
	 * @param info 通知のセット
	 * @param notifyGroupId 通知グループID
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public boolean modifyNotifyRelation(Collection<NotifyRelationInfo> info, String notifyGroupId)
			throws NotifyNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		// システム通知情報を更新
		ModifyNotifyRelation notify = new ModifyNotifyRelation();
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			flag = notify.modify(info, notifyGroupId);
			jtm.commit();
		} catch (NotifyNotFound | HinemosUnknown e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("modifyNotifyRelation() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return flag;
	}

	/**
	 * 通知グループを変更します。
	 *
	 * @param info 通知のセット
	 * @param notifyGroupId 通知グループID
	 * @param ownerRoleId オーナーロールID
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public boolean modifyNotifyRelation(Collection<NotifyRelationInfo> info, String notifyGroupId, String ownerRoleId)
			throws NotifyNotFound, InvalidRole, HinemosUnknown {

		JpaTransactionManager jtm = null;

		// システム通知情報を更新
		ModifyNotifyRelation notify = new ModifyNotifyRelation();
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			flag = notify.modify(info, notifyGroupId, ownerRoleId);
			jtm.commit();
		} catch (NotifyNotFound | HinemosUnknown e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("modifyNotifyRelation() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return flag;
	}

	/**
	 * 通知グループを削除します。
	 *
	 * @param notifyGroupId 通知グループID
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 */
	public boolean deleteNotifyRelation(String notifyGroupId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// システム通知情報を削除
		ModifyNotifyRelation notify = new ModifyNotifyRelation();
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			flag = notify.delete(notifyGroupId);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (HinemosUnknown e){
			jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("deleteNotifyRelation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return flag;
	}

	/**
	 * 通知グループを作成します。
	 *
	 * @param info 通知グループ
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean addNotifyRelation(Collection<NotifyRelationInfo> info) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// システム通知情報を登録
		if(info != null){
			ModifyNotifyRelation notify = new ModifyNotifyRelation();
			boolean flag;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				flag = notify.add(info);

				jtm.commit();
			} catch (ObjectPrivilege_InvalidRole e) {
				if (jtm != null)
					jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (HinemosUnknown e){
				jtm.rollback();
				throw e;
			} catch (Exception e){
				m_log.warn("addNotifyRelation() : " + e.getClass().getSimpleName() +
						", " + e.getMessage(), e);
				if (jtm != null)
					jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (jtm != null)
					jtm.close();
			}
			return flag;
		}else{
			return true;
		}
	}

	/**
	 * 通知グループを作成します。
	 *
	 * @param info 通知グループ
	 * @param ownerRoleId オーナーロールID
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean addNotifyRelation(Collection<NotifyRelationInfo> info, String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// システム通知情報を登録
		if(info != null){
			ModifyNotifyRelation notify = new ModifyNotifyRelation();
			boolean flag;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				flag = notify.add(info, ownerRoleId);

				jtm.commit();
			} catch (ObjectPrivilege_InvalidRole e) {
				if (jtm != null)
					jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (HinemosUnknown e){
				jtm.rollback();
				throw e;
			} catch (Exception e){
				m_log.warn("addNotifyRelation() : " + e.getClass().getSimpleName() +
						", " + e.getMessage(), e);
				if (jtm != null)
					jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (jtm != null)
					jtm.close();
			}
			return flag;
		}else{
			return true;
		}
	}

	/**
	 * 通知履歴情報を削除します。
	 *
	 * @param pluginId プラグインID
	 * @param monitorId 通知設定先ID
	 * @throws HinemosUnknown
	 */
	public void deleteNotifyHistory(String pluginId, String monitorId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// コミット後に、設定の結果状態を削除する
			jtm.addCallback(new MonitorStatusCacheRemoveCallback(pluginId, monitorId));

			// 通知履歴を削除する
			QueryUtil.deleteNotifyHistoryByPluginIdAndMonitorId(pluginId, monitorId);

			jtm.commit();
		} catch (Exception e){
			m_log.warn("deleteNotifyHistory() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 通知履歴情報を削除します。
	 *
	 * @param facilityId ファシリティID
	 * @throws HinemosUnknown
	 */
	public void deleteNotifyHistoryByFacilityId(String facilityId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 通知履歴を削除する
			QueryUtil.deleteNotifyHistoryByFacilityId(facilityId);

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("deleteNotifyHistoryByFacilityId() : "
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
	 * ステータス情報情報を削除します。
	 *
	 * @param facilityId ファシリティID
	 * @throws HinemosUnknown
	 */
	public void deleteMonitorStatus(String facilityId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// コミット後に、ステータス情報のキャッシュを削除する
			jtm.addCallback(new MonitorStatusCacheRemoveByFacilityIdCallback(facilityId));

			// ステータス情報を削除する
			QueryUtil.deleteMonitorStatusByFacilityId(facilityId);

			jtm.commit();
		} catch (Exception e){
			m_log.warn("deleteMonitorStatus() : "
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
	 *　引数で指定した通知IDを利用している通知グループIDを取得する。
	 *
	 * @param notifyIds
	 * @return　通知グループIDのリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<NotifyCheckIdResultInfo> checkNotifyId(String[] notifyIds) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		ArrayList<NotifyCheckIdResultInfo> ret = new ArrayList<NotifyCheckIdResultInfo>();
		SelectNotifyRelation notify = new SelectNotifyRelation();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (int i = 0; i < notifyIds.length; i++) {
				String notifyId = notifyIds[i];
				NotifyCheckIdResultInfo result = new NotifyCheckIdResultInfo();
				result.setNotifyId(notifyId);
				result.setNotifyGroupIdList(notify.getNotifyGroupIdBaseOnNotifyId(notifyId));
				ret.add(result);
			}
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("checkNotifyId() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 *　指定した通知IDを有効化/無効化する。
	 *
	 * @param notifyIds
	 * @return List<NotifyInfo>
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws NotifyDuplicate
	 * @throws InvalidRole
	 */
	public List<NotifyInfo> setNotifyValid(List<String> notifyIds, boolean validFlag) throws HinemosUnknown, NotifyNotFound, NotifyDuplicate, InvalidRole {
		JpaTransactionManager jtm = null;
		List<NotifyInfo> ret = new ArrayList<>();

		// null check
		if(notifyIds == null || notifyIds.size() == 0){
			HinemosUnknown e = new HinemosUnknown("target notifyId is null or empty.");
			m_log.info("setNotifyValid() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			SelectNotify notify = new SelectNotify();
			for (String notifyId : notifyIds) {
				NotifyInfo info = notify.getNotify(notifyId);
				if (validFlag) {
					// 通知設定の有効化
					info.setValidFlg(true);
					modifyNotify(info);
				} else {
					// 通知設定の無効化
					info.setValidFlg(false);
					modifyNotify(info);
				}
			}

			jtm.addCallback(new NotifyCacheRefreshCallback());
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

			for (String notifyId : notifyIds) {
				ret.add(notify.getNotify(notifyId));
			}
		} catch (NotifyNotFound | NotifyDuplicate | HinemosUnknown | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (InvalidSetting e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("setNotifyValid() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}

	/**
	 * 指定したファシリティIDが利用されているか確認する
	 *
	 *
	 */
	@Override
	public void isUseFacilityId(String facilityId) throws UsedFacility {
		JpaTransactionManager jtm = null;

		try {
			String message = "";
			jtm = new JpaTransactionManager();
			jtm.begin();

			List<NotifyJobInfo> notifyJobInfoEntityList = QueryUtil.getNotifyJobInfoByJobExecFacilityId(facilityId);
			if (notifyJobInfoEntityList != null
					&& notifyJobInfoEntityList.size() > 0) {
				// ID名を取得する
				StringBuilder sb = new StringBuilder();
				for (NotifyJobInfo entity : notifyJobInfoEntityList) {
					sb.append(entity.getNotifyId());
					sb.append(", ");
				}
				message += sb.toString();
			}
			List<NotifyLogEscalateInfo> notifyLogEscalateInfoEntityList
			= QueryUtil.getNotifyLogEscalateInfoByEscalateFacilityId(facilityId);
			if (notifyLogEscalateInfoEntityList != null
					&& notifyLogEscalateInfoEntityList.size() > 0) {
				// ID名を取得する
				// findbugs対応 文字列の連結方式をStringBuilderを利用する方法に変更
				StringBuilder listID = new StringBuilder();
				for (NotifyLogEscalateInfo entity : notifyLogEscalateInfoEntityList) {
					listID.append(entity.getNotifyId() + ", ");
				}
				message += listID.toString();
			}
			List<NotifyInfraInfo> notifyInfraInfoEntityList
			= QueryUtil.getNotifyInfraInfoByInfraExecFacilityId(facilityId);
			if (notifyInfraInfoEntityList != null
					&& notifyInfraInfoEntityList.size() > 0) {
				// ID名を取得する
				// findbugs対応 文字列の連結方式をStringBuilderを利用する方法に変更
				StringBuilder listID = new StringBuilder();
				for (NotifyInfraInfo entity : notifyInfraInfoEntityList) {
					listID.append(entity.getNotifyId() + ", ");
				}
				message += listID.toString();
			}
			List<NotifyCloudInfo> notifyCloudInfoEntityList
			= QueryUtil.getNotifyCloudInfoByCloudExecFacilityId(facilityId);
			if (notifyCloudInfoEntityList != null
					&& notifyCloudInfoEntityList.size() > 0) {
				// ID名を取得する
				// findbugs対応 文字列の連結方式をStringBuilderを利用する方法に変更
				StringBuilder listID = new StringBuilder();
				for (NotifyCloudInfo entity : notifyCloudInfoEntityList) {
					listID.append(entity.getNotifyId() + ", ");
				}
				message += listID.toString();
			}
			
			if (message.trim().length() > 0) {
				UsedFacility e = new UsedFacility(MessageConstant.NOTIFY.getMessage() + " : " + message);
				m_log.info("isUseFacilityId() : " + e.getClass().getSimpleName() +
						", " + facilityId + ", " + e.getMessage());
				throw e;
			}
			jtm.commit();
		} catch (UsedFacility e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("isUseFacilityId() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * イベント通知
	 *
	 * トランザクションは引き継がれたものを使用
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public void insertEventLog(OutputBasicInfo output, int confirmState) throws InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			new OutputEvent().insertEventLog(output, confirmState);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("insertEventLog() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * ステータス通知
	 *
	 * トランザクションは引き継がれたものを使用
	 *
	 * @param output
	 * @throws HinemosUnknown
	 */
	public void updateStatusLog(OutputBasicInfo output) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			new OutputStatus().updateStatus(output);

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("insertEventLog() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	/**
	 * Syslog通知
	 *
	 * トランザクションは引き継がれたものを使用
	 *
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void sendAfterConvertHostname(String ipAddress, int port, String facility, String severity, String facilityId, String message, String timeStamp) throws InvalidRole, HinemosUnknown{
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			new SendSyslog().sendAfterConvertHostname(ipAddress, port, facility, severity, facilityId, message, timeStamp);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("sendAfterConvertHostname() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	private static Object lock = new Object();

	private static void initReportMail () throws HinemosUnknown {
		synchronized (lock) {
			if(m_reportingSendMail == null) {
				String sendMailClass = "com.clustercontrol.notify.util.ReportingSendMail";
				try {
					@SuppressWarnings("unchecked")
					Class<? extends SendMail> clazz = (Class<? extends SendMail>) Class.forName(sendMailClass);
					m_reportingSendMail = clazz.newInstance();
					m_log.info("load " + sendMailClass + ".");
				} catch (Exception e) {
					throw new HinemosUnknown(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * メール通知
	 *
	 * トランザクションは引き継がれたものを使用
	 *
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void sendMail(String[] address, OutputBasicInfo outputBasicInfo) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SendMail sendMail;

			// レポーティングオプション用添付ファイル対応
			if(outputBasicInfo.getPluginId().equals(HinemosModuleConstant.REPORTING)
					&& outputBasicInfo.getSubKey() != null) {
				initReportMail();
				m_log.debug("m_reportingSendMail.notify");
				sendMail = m_reportingSendMail;

			} else {
				m_log.debug("sendMail.notify");
				sendMail = new SendMail();
			}

			String mailSubject = sendMail.getSubject(outputBasicInfo, null);
			String mailBody = sendMail.getContentWithMessageOrg(outputBasicInfo);

			if("INTERNAL".equals(outputBasicInfo.getFacilityId())) {
				List<Integer> list = MultiSmtpServerUtil.getRoleServerList(outputBasicInfo.getFacilityId());
				Boolean sendAll = MultiSmtpServerUtil.isSendAll();
				for(int i : list) {
					sendMail.sendMail(address, mailSubject, mailBody, i);
					if (sendAll == false) {
						break;
					}
				}
			} else {
				sendMail.sendMail(address, mailSubject, mailBody);
			}

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("sendMail() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * ジョブ通知 (ジョブ連携メッセージ送信)
	 *
	 * トランザクションは引き継がれたものを使用
	 *
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void sendJobLinkMessage(OutputBasicInfo outputBasicInfo, List<JobLinkExpInfo> expList) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			new RunJob().exectuteJob(outputBasicInfo, null, expList);

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("sendJobLinkMessage() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 外部から直接通知処理を実行します。
	 *
	 * @param pluginId プラグインID
	 * @param monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param subKey 抑制用のサブキー（任意の文字列）
	 * @param generationDate 出力日時（エポック秒）
	 * @param priority 重要度
	 * @param application アプリケーション
	 * @param message メッセージ
	 * @param messageOrg オリジナルメッセージ
	 * @param notifyIdList 通知IDのリスト
	 * @param srcId 送信元を特定するためのID
	 * @throws FacilityNotFound
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public void notify(
			String pluginId,
			String monitorId,
			String facilityId,
			String subKey,
			long generationDate,
			int priority,
			String application,
			String message,
			String messageOrg,
			ArrayList<String> notifyIdList,
			String srcId) throws FacilityNotFound, HinemosUnknown, NotifyNotFound, InvalidRole {
		m_log.info("notify() "
				+ "pluginId = " + pluginId
				+ ", monitorId = " + monitorId
				+ ", facilityId = " + facilityId
				+ ", subKey = " + subKey
				+ ", generationDate = " + generationDate
				+ ", priority = " + priority
				+ ", application = " + application
				+ ", message = " + message
				+ ", messageOrg = " + messageOrg
				+ ", srcId = " + srcId
				);

		JpaTransactionManager jtm = null;

		// パラメータのnullチェック
		if(pluginId == null){
			m_log.info("notify() Invalid argument. pluginId is null.");
			return;
		} else if(monitorId == null){
			m_log.info("notify() Invalid argument. monitorId is null.");
			return;
		} else if(facilityId == null){
			m_log.info("notify() Invalid argument. facilityId is null.");
			return;
		} else if(application == null){
			m_log.info("notify() Invalid argument. application is null.");
			return;
		} else if(message == null){
			m_log.info("notify() Invalid argument. message is null.");
			return;
		} else if(messageOrg == null){
			m_log.info("notify() Invalid argument. messageOrg is null.");
			return;
		} else if(notifyIdList == null){
			m_log.info("notify() Invalid argument. notifyIdList is null.");
			return;
		}

		if(subKey == null){
			// エラーとして扱わず空文字を設定する。
			subKey = "";
		}

		// 通知情報
		OutputBasicInfo output = new OutputBasicInfo();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 指定のファシリティIDが存在するか確認
			new RepositoryControllerBean().getFacilityEntityByPK(facilityId);

			// 指定の通知設定が存在するか確認
			for(String notifyId : notifyIdList){
				QueryUtil.getNotifyInfoPK(notifyId);
			}

			output.setApplication(application);
			output.setFacilityId(facilityId);
			// NotifyGroupIdを取得するためにダミーのmonitorInfoを作成
			MonitorInfo monitorInfo = new MonitorInfo();
			monitorInfo.setMonitorId(monitorId);
			monitorInfo.setMonitorTypeId(pluginId);
			// NotifyGroupIdを取得
			String notifyGroupId = NotifyGroupIdGenerator.generate(monitorInfo);
			// 通知IDのリストをチェック
			List<String> checkNotifyIdList = NotifyRelationCache.getNotifyIdList(notifyGroupId);
			if (checkNotifyIdList != null && checkNotifyIdList.size() > 0) {
				// 実在する監視項目IDを元にNotifyGroupIdを取得できたならセット
				output.setNotifyGroupId(notifyGroupId);
				// ジョブ連携メッセージID
				output.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MONITOR, pluginId, monitorId));
			} else {
				// 存在しない場合はエラーとする
				throw new HinemosUnknown("Could Not find " + monitorId + ". Please check monitorId or pluginId.");
			}
			
			try {
				String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
				output.setScopeText(facilityPath);
			} catch (ObjectPrivilege_InvalidRole e) {
				throw new InvalidRole(e.getMessage(), e);
			} catch (HinemosUnknown e) {
				// ファシリティIDをファシリティパスとする
				output.setScopeText(facilityId);
			} catch (Exception e) {
				m_log.warn("notify() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				// ファシリティIDをファシリティパスとする
				output.setScopeText(facilityId);
			}
			output.setGenerationDate(generationDate);
			output.setMessage(message);
			output.setMessageOrg(messageOrg);
			output.setMonitorId(monitorId);
			output.setMultiId(srcId);
			output.setPluginId(pluginId);
			output.setPriority(priority);
			output.setSubKey(subKey);

			output.setPriorityChangeJudgmentType(monitorInfo.getPriorityChangeJudgmentType());
			output.setPriorityChangeFailureType(monitorInfo.getPriorityChangeFailureType());

			// 通知設定
			jtm.addCallback(new NotifyCallback(output));

			jtm.commit();
		} catch (NotifyNotFound | InvalidRole | FacilityNotFound | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("notify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 外部から直接通知処理を実行します。
	 *
	 * @param notifyInfoList 通知情報リスト
	 */
	public static void notify(List<OutputBasicInfo> notifyInfoList) {

		if (notifyInfoList == null || notifyInfoList.isEmpty()) {
			return;
		}
		m_log.debug("notify(notifyInfoList=" + Arrays.toString(notifyInfoList.toArray()) + ")");

		for (OutputBasicInfo notifyInfo : notifyInfoList) {
			JpaTransactionManager jtm =  new JpaTransactionManager();
			try {
				jtm.begin(true);
				if (notifyInfo == null) {
					m_log.debug("notify() notifyInfo is NULL, so skip.");
					continue;
				}
				m_log.debug("notify() notifyInfo=" + notifyInfo);

				// 監視設定から通知IDのリストを取得する
				List<String> notifyIdList = NotifyRelationCache.getNotifyIdList(notifyInfo.getNotifyGroupId());
				if (notifyIdList == null || notifyIdList.isEmpty()) {
					// 該当の通知IDのリストがない場合はスキップする
					m_log.debug("notify() notifyIdList is NULL or empty, so skip.");
					continue;
				}
				m_log.debug("notify() made notifyIdList: " + Arrays.toString(notifyIdList.toArray()));

				if (notifyInfo.getPluginId() == null) {
					m_log.debug("notify() notifyInfo PluginId is NULL, so skip.");
					continue;
				}
				String lockKey = notifyInfo.getPluginId();
				m_lockObjectMap.putIfAbsent(lockKey, new Object());
				m_log.debug("notify() try to get notifyLock. lockKey=" + lockKey + ", notifyInfo=" + notifyInfo);
				long startTime = System.currentTimeMillis();
				synchronized (m_lockObjectMap.get(lockKey)) {
					if (m_log.isDebugEnabled()) {
						long duration = System.currentTimeMillis() - startTime;
						String logStr = "notify() got notifyLock. lockKey=" + lockKey + ", duration=" + duration + ", notifyInfo=" + notifyInfo;
						if (duration < 100) {
							m_log.debug(logStr);
						} else {
							m_log.warn(logStr + ", long_wait");
						}
					}

					// 通知処理を行う
					new NotifyControllerBean().notify(notifyInfo, notifyIdList);
				}
				m_log.debug("notify() release notifyLock. lockKey=" + lockKey + ", duration=" + (System.currentTimeMillis() - startTime) + ", notifyInfo=" + notifyInfo);
				m_log.debug("notify() start commit.");
				long commitStartTime = System.currentTimeMillis();
				jtm.commit();
				m_log.debug("notify() end commit, commit_duration=" + (System.currentTimeMillis() - commitStartTime));
			} catch (HinemosUnknown e) {
				m_log.warn("notify() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				if (jtm != null) {
					jtm.rollback();
				}
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
		}
	}

	protected void notify(OutputBasicInfo notifyInfo, List<String> notifyIdList) throws HinemosUnknown {
		// 機能毎に設定されているJMSの永続化モードを取得
		boolean persist = AsyncTaskPersistentConfig.isPersisted(notifyInfo.getPluginId());

		// 通知キューへの登録処理を実行
		NotifyDispatcher.notifyAction(notifyInfo, notifyIdList, persist);
	}
	
	/**
	 * 外部から直接イベント通知処理を実行します。
	 *
	 * @param eventNotifyInfo イベント通知情報
	 * 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static EventDataInfo notifyUserExtentionEvent(EventNotifyInfo eventNotifyInfo) 
			throws FacilityNotFound, HinemosUnknown, InvalidRole, InvalidSetting {
		
		if (eventNotifyInfo == null) {
			throw new HinemosUnknown("eventNotifyInfo is null");
		}
		
		JpaTransactionManager jtm = null;
		EventDataInfo info = null;
		
		//Hinemosプロパティを取得
		Map<Integer, EventUserExtensionItemInfo> userItemInfoMap = SelectEventHinemosProperty.getEventUserExtensionItemInfo();
		
		//validation
		NotifyValidator.validateEventNotify(eventNotifyInfo, userItemInfoMap);

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			EventLogEntity event = new OutputEvent().insertEventLog(
					eventNotifyInfo.toOutputBasicInfo(), 
					eventNotifyInfo.getConfirmFlg(),
					null,
					userItemInfoMap,
					eventNotifyInfo.getOwnerRoleId()
					);
			
			jtm.commit();
			
			info = new EventDataInfo(); 
			EventUtil.copyEventLogEntityToEventDataInfo(event, info);
			
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("notifyUserExtentionEvent() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return info;
	}

	
}
