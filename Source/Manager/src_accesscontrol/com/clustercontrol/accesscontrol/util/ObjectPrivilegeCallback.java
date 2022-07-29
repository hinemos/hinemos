/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeTargetBean;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfoPK;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;

/**
 * オブジェクト権限チェックのコールバックメソッド
 * Commit前に、更新対象、削除対象に対してチェックする。
 *
 */
public class ObjectPrivilegeCallback implements JpaTransactionCallback {

	private static Log m_log = LogFactory.getLog( ObjectPrivilegeCallback.class );

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() throws ObjectPrivilege_InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// オブジェクト権限削除リスト({objectType, objectId, isModifyCheck})
			List<Object[]> deleteList = new ArrayList<Object[]>();

			// SQLをDBに反映させるためflushする。
			jtm.flush();

			// オブジェクト権限対象 取得
			@SuppressWarnings("unchecked")
			List<ObjectPrivilegeTargetBean> targetList
			= (List<ObjectPrivilegeTargetBean>)HinemosSessionContext.instance().getProperty(HinemosSessionContext.OBJECT_PRIVILEGE_TARGET_LIST);

			if (targetList != null) {
				// ログインユーザ 取得
				String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
				Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);

				List<String> roleIdList = new ArrayList<String>();
				// ユーザ情報の取得
				if (loginUser != null && !"".equals(loginUser.trim())) {
					roleIdList = UserRoleCache.getRoleIdList(loginUser);
				}

				for (ObjectPrivilegeTargetBean bean : targetList) {
					HinemosObjectPrivilege hinemosObjectPrivilege = bean.getEntityClass().getAnnotation(HinemosObjectPrivilege.class);
					if (hinemosObjectPrivilege == null) {
						// HinemosObjectPrivilegeアノテーションが設定されていない場合はチェック対象外
						continue;
					}

					// ジョブの場合、jobunitId='_ROOT_'はチェック対象外
					if (hinemosObjectPrivilege.objectType().equals(HinemosModuleConstant.JOB_MST)
							&& bean.getObjectId().equals(CreateJobSession.TOP_JOBUNIT_ID)) {
						continue;
					}

					// ジョブエンティティの削除の場合、ModifyJob#deleteJobunit()で削除するため、チェック対象外
					if (hinemosObjectPrivilege.objectType().equals(HinemosModuleConstant.JOB)
							&& bean.isDeleteFlg()) {
						continue;
					}
					
					// オーナーロールスコープの削除の場合はチェック対象外
					if (hinemosObjectPrivilege.objectType().equals(HinemosModuleConstant.PLATFORM_REPOSITORY)
							&& bean.getObjectId().equals(bean.getOwnerRoleId())){
						continue;
					}

					// 削除するエンティティの場合、オブジェクト権限削除リストに追加する
					if (bean.isDeleteFlg()) {
						Object[] args = {hinemosObjectPrivilege.objectType(), bean.getObjectId(), hinemosObjectPrivilege.isModifyCheck()};
						deleteList.add(args);
					}

					// チェック対象外の場合はチェックしない
					if (bean.isUncheckFlg()) {
						m_log.debug("preCommit() isUncheckFlg entity targetClass = " + bean.getEntityClass().getSimpleName()
								+ ", objectId = " + bean.getObjectId()
								+ ", ownerRoleId = " + bean.getOwnerRoleId());
						continue;
					}

					// ユーザ情報が取得できない場合はオブジェクト権限チェックはしない
					if (loginUser != null && !"".equals(loginUser.trim())) {

						if (isAdministrator != null && isAdministrator) {
							// ADMINISTRATORSロールに所属している場合、オブジェクト権限チェックはしない
							continue;		// オブジェクト権限有り
						}

						// オーナーロールにユーザのロールが設定されている場合は変更可
						boolean existsflg = false;
						for (String roleId : roleIdList) {
							m_log.debug("preCommit() userRoleId = " + roleId);
							if (roleId.equals(bean.getOwnerRoleId())) {
								existsflg = true;
								break;
							}
						}
						if (existsflg) {
							continue;		// オブジェクト権限有り
						}

						// オブジェクト権限テーブルにデータが存在するかの確認
						String objectType = hinemosObjectPrivilege.objectType();
						for (String roleId : roleIdList) {
							// 所属ロールで設定されている場合は変更可
							ObjectPrivilegeInfoPK objectPrivilegeEntityPK 
								= new ObjectPrivilegeInfoPK(
										objectType, 
										bean.getObjectId(), 
										roleId, 
										ObjectPrivilegeMode.MODIFY.name());
							ObjectPrivilegeInfo objectPrivilegeEntity = em.find(ObjectPrivilegeInfo.class, objectPrivilegeEntityPK, ObjectPrivilegeMode.READ);
							if (objectPrivilegeEntity != null) {
								existsflg = true;		// オブジェクト権限有り
								break;
							}
						}
						if (!existsflg) {
							// オブジェクト権限エラー
							ObjectPrivilege_InvalidRole e = new ObjectPrivilege_InvalidRole(
									"targetClass = " + bean.getEntityClass().getSimpleName()
									+ ", objectId = " + bean.getObjectId()
									+ ", ownerRoleId = " + bean.getOwnerRoleId());
							m_log.warn("preCommit() object privilege error. : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}
				}

				// オブジェクト権限削除リストに格納されたオブジェクト種別、オブジェクトIDのレコードをオブジェクト権限テーブルから削除する。
				for (Object[] args : deleteList) {
					if ((Boolean)args[2]) {
						if (HinemosModuleConstant.JOB.equals((String)args[0])) {
							// JobMstEntityの場合は、ModifyJob#deleteJobunit()で削除する。
						} else {
							ObjectPrivilegeUtil.deleteObjectPrivilege((String)args[0], (String)args[1]);
						}
					}
				}
			}
		}
	}

	@Override
	public void postCommit() {
		// オブジェクト権限対象 初期化
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.OBJECT_PRIVILEGE_TARGET_LIST, null);
	}

	@Override
	public void preRollback() { }

	@Override
	public void postRollback() {
		// オブジェクト権限対象 初期化
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.OBJECT_PRIVILEGE_TARGET_LIST, null);
	}

	@Override
	public void preClose() { }

	@Override
	public void postClose() { }
	
	@Override
	public int hashCode() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			int h = 1;
			h = h * 31 + (em == null ? 0 : em.hashCode());
			return h;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ObjectPrivilegeCallback) {
			ObjectPrivilegeCallback cast = (ObjectPrivilegeCallback)obj;
			if (this.hashCode() == cast.hashCode()) {
				return true;
			}
		}
		return false;
	}
}
