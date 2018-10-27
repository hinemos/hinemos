/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.session.CheckFacility;
import com.clustercontrol.commons.util.AsyncTaskPersistentConfig;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.notify.bean.NotifyCheckIdResultInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.factory.ModifyNotify;
import com.clustercontrol.notify.factory.ModifyNotifyRelation;
import com.clustercontrol.notify.factory.NotifyDispatcher;
import com.clustercontrol.notify.factory.SelectNotify;
import com.clustercontrol.notify.factory.SelectNotifyRelation;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfraInfo;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.notify.model.NotifyLogEscalateInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.util.NotifyCache;
import com.clustercontrol.notify.util.NotifyCacheRefreshCallback;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.notify.util.NotifyRelationCacheRefreshCallback;
import com.clustercontrol.notify.util.NotifyValidator;
import com.clustercontrol.notify.util.OutputEvent;
import com.clustercontrol.notify.util.OutputStatus;
import com.clustercontrol.notify.util.QueryUtil;
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

	private static Object notifyLock = new Object();

	/**
	 * 通知情報を作成します。
	 *
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws NotifyDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.factory.AddNotify#add(NotifyInfo)
	 */
	public boolean addNotify(NotifyInfo info) throws NotifyDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知情報を登録
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			NotifyValidator.validateNotifyInfo(info);
			
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(info.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			ModifyNotify notify = new ModifyNotify();
			flag = notify.add(info,(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyCache.refresh();
			NotifyRelationCache.refresh();

		} catch (NotifyDuplicate | InvalidSetting | HinemosUnknown e) {
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

		return  flag;
	}

	/**
	 * 通知情報を変更します。
	 *
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws NotifyDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.notify.factory.ModifyNotify#modify(NotifyInfo)
	 */
	public boolean modifyNotify(NotifyInfo info) throws NotifyDuplicate, InvalidRole, HinemosUnknown, InvalidSetting {
		JpaTransactionManager jtm = null;

		// 通知情報を更新
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			NotifyValidator.validateNotifyInfo(info);

			ModifyNotify notify = new ModifyNotify();
			flag = notify.modify(info,(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.addCallback(new NotifyCacheRefreshCallback());
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

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
			m_log.warn("modifyNotify() : " + e.getClass().getSimpleName() +
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
	 * 通知情報を削除します。
	 *
	 * @param notifyIds 削除対象の通知IDリスト
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws NotifyDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 *
	 * @see com.clustercontrol.notify.factory.DeleteNotify#delete(String)
	 */
	public boolean deleteNotify(String[] notifyIds) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知情報を削除
		ModifyNotify notify = new ModifyNotify();
		boolean flag = true;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (String notifyId : notifyIds) {
				flag = flag && notify.delete(notifyId);
			}

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyCache.refresh();
			NotifyRelationCache.refresh();

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
		return flag;
	}

	/**
	 * 引数で指定された通知情報を返します。
	 *
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 *
	 * @see com.clustercontrol.notify.factory.SelectNotify#getNotify(String)
	 */
	public NotifyInfo getNotify(String notifyId) throws NotifyNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知情報を取得
		SelectNotify notify = new SelectNotify();
		NotifyInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			info = notify.getNotify(notifyId);
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
	 * @param notifyId
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws NotifyDuplicate
	 * @throws InvalidRole
	 */
	public void setNotifyStatus(String notifyId, boolean validFlag) throws HinemosUnknown, NotifyNotFound, NotifyDuplicate, InvalidRole {
		JpaTransactionManager jtm = null;

		// null check
		if(notifyId == null || "".equals(notifyId)){
			HinemosUnknown e = new HinemosUnknown("target notifyId is null or empty.");
			m_log.info("setNotifyStatus() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			NotifyInfo info = getNotify(notifyId);
			if (validFlag) {
				// 通知設定の有効化
				if(!info.getValidFlg().booleanValue()){
					info.setValidFlg(true);
					modifyNotify(info);
				}
			} else {
				// 通知設定の無効化
				if(info.getValidFlg().booleanValue()){
					info.setValidFlg(false);
					modifyNotify(info);
				}
			}

			jtm.addCallback(new NotifyCacheRefreshCallback());
			jtm.addCallback(new NotifyRelationCacheRefreshCallback());
			jtm.commit();

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
			m_log.warn("setNotifyStatus() : " + e.getClass().getSimpleName() +
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

			// 指定のファシリティIDが存在するか確認
			new RepositoryControllerBean().getFacilityEntityByPK(facilityId);

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
				String listID = "";
				for (NotifyLogEscalateInfo entity : notifyLogEscalateInfoEntityList) {
					listID += (entity.getNotifyId() + ", ");
				}
				message += listID;
			}
			List<NotifyInfraInfo> notifyInfraInfoEntityList
			= QueryUtil.getNotifyInfraInfoByInfraExecFacilityId(facilityId);
			if (notifyInfraInfoEntityList != null
					&& notifyInfraInfoEntityList.size() > 0) {
				// ID名を取得する
				String listID = "";
				for (NotifyInfraInfo entity : notifyInfraInfoEntityList) {
					listID += (entity.getNotifyId() + ", ");
				}
				message += listID;
			}
			
			if (message.trim().length() > 0) {
				UsedFacility e = new UsedFacility(MessageConstant.NOTIFY.getMessage() + " : " + message);
				m_log.info("isUseFacilityId() : " + e.getClass().getSimpleName() +
						", " + facilityId + ", " + e.getMessage());
				throw e;
			}
			jtm.commit();
		} catch (HinemosUnknown | FacilityNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
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
			String mailBody = sendMail.getContent(outputBasicInfo, null);
			sendMail.sendMail(address, mailSubject, mailBody);

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
			ArrayList<String> confirmedNotifyIdList = new ArrayList<String>();
			for(String notifyId : notifyIdList){
				QueryUtil.getNotifyInfoPK(notifyId);
				confirmedNotifyIdList.add(notifyId);
			}

			output.setApplication(application);
			output.setFacilityId(facilityId);
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

			// 通知設定
			jtm.addCallback(new NotifyCallback(output));

			jtm.commit();
		} catch (NotifyNotFound | InvalidRole e) {
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
	 * @param notifyGroupId
	 * @throws HinemosUnknown 
	 */
	public static void notify(List<OutputBasicInfo> notifyInfoList) {

		JpaTransactionManager jtm = null;

		if (notifyInfoList == null || notifyInfoList.isEmpty()) {
			return;
		}

		synchronized (notifyLock) {
			try {
				jtm = new JpaTransactionManager();
				jtm.begin(true);

				for (OutputBasicInfo notifyInfo : notifyInfoList) {
					if (notifyInfo == null) {
						continue;
					}
					// 監視設定から通知IDのリストを取得する
					List<String> notifyIdList = NotifyRelationCache.getNotifyIdList(notifyInfo.getNotifyGroupId());
					m_log.trace("notifyIdList.size=" + notifyIdList.size() + ", notifyGroupId=" + notifyInfo.getNotifyGroupId());
					// 該当の通知IDのリストがない場合は終了する
					if(notifyIdList == null || notifyIdList.size() <= 0){
						continue;
					}
	
					// 通知処理を行う
					new NotifyControllerBean().notify(notifyInfo, notifyIdList);
				}
				jtm.commit();
			} catch (Exception e) {
				m_log.warn("notify() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				if (jtm != null) {
					jtm.rollback();
				}
				throw new RuntimeException(e.getMessage(), e);
			} finally {
				if (jtm != null)
					jtm.close();
			}
		}
	}

	protected void notify(OutputBasicInfo notifyInfo, List<String> notifyIdList) throws HinemosUnknown {
		// 機能毎に設定されているJMSの永続化モードを取得
		boolean persist = AsyncTaskPersistentConfig.isPersisted(notifyInfo.getPluginId());

		// 通知キューへの登録処理を実行
		NotifyDispatcher.notifyAction(notifyInfo, notifyIdList, persist);
	}
}
