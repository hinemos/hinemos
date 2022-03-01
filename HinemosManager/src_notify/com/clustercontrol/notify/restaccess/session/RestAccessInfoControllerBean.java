/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.session;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.RestAccessDuplicate;
import com.clustercontrol.fault.RestAccessNotFound;
import com.clustercontrol.fault.RestAccessUsed;
import com.clustercontrol.notify.restaccess.factory.ModifyRestAccessInfo;
import com.clustercontrol.notify.restaccess.factory.SelectRestAccessInfo;
import com.clustercontrol.notify.restaccess.model.RestAccessInfo;
import com.clustercontrol.notify.restaccess.util.RestAccesccTokenCacheRemoveCallback;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;

public class RestAccessInfoControllerBean {
	private static Log m_log = LogFactory.getLog( RestAccessInfoControllerBean.class );

	/**
	 * RESTアクセス情報をマネージャに登録します。<BR>
	 * 
	 * 値の入力チェックは呼び出し元で実施済みの前提です<BR>
	 * 
	 * @param RestAccessInfo
	 * @return RestAccessInfo
	 * @throws HinemosUnknown
	 * @throws RestAccessDuplicate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.factory.AddRestAccess#add(RestAccessInfoData)
	 */
	public RestAccessInfo addRestAccess(RestAccessInfo data) throws HinemosUnknown, RestAccessDuplicate, InvalidSetting, InvalidRole {
		RestAccessInfo ret = null;
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			//オーナーロールID 存在チェック
			CommonValidator.validateOwnerRoleId(data.getOwnerRoleId(), true, data.getRestAccessId(),
					HinemosModuleConstant.PLATFORM_REST_ACCESS);
			
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(data.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			ModifyRestAccessInfo RestAccess = new ModifyRestAccessInfo();
			RestAccess.add(data, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();
			ret = new SelectRestAccessInfo().getRestAccessInfo(data.getRestAccessId());
		} catch (RestAccessDuplicate | InvalidSetting e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addRestAccess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * マネージャ上のRESTアクセス情報を変更します。<BR>
	 * 
	 * 値の入力チェックは呼び出し元で実施済みの前提です<BR>
	 * 
	 * @param RestAccessInfo
	 * @return RestAccessInfo
	 * @throws HinemosUnknown
	 * @throws RestAccessNotFound
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.restaccess.factory.ModifyRestAccessInfo#modify(RestAccessInfo)
	 * @see com.clustercontrol.notify.restaccess.model.RestAccessInfo
	 */
	public RestAccessInfo modifyRestAccess(RestAccessInfo data) throws HinemosUnknown, RestAccessNotFound,InvalidSetting, InvalidRole {
		RestAccessInfo ret = null;
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();


			ModifyRestAccessInfo RestAccess = new ModifyRestAccessInfo();
			RestAccess.modify(data, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));
			//設定変更時、取得方法が変わっている可能性があるので、アクセストークンのキャッシュはクリア 
			jtm.addCallback(new RestAccesccTokenCacheRemoveCallback(data.getRestAccessId()));

			jtm.commit();

			ret = new SelectRestAccessInfo().getRestAccessInfo(data.getRestAccessId());
		} catch (RestAccessNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyRestAccess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * RESTアクセス情報をマネージャから削除します。<BR>
	 * 
	 * @param RestAccessId 削除対象のRESTアクセスID
	 * @return List<RestAccessInfo>
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws RestAccessNotFound 
	 * 
	 * @see com.clustercontrol.notify.restaccess.factory.ModifyRestAccessInfo#delete(String)
	 */
	public List<RestAccessInfo> deleteRestAccess(List<String> RestAccessIdList) throws InvalidRole, HinemosUnknown, RestAccessNotFound, RestAccessUsed {
		m_log.debug("deleteRestAccess");

		JpaTransactionManager jtm = null;
		List<RestAccessInfo> retList =  new ArrayList<>();

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// RESTアクセス情報を削除
			ModifyRestAccessInfo restAccess = new ModifyRestAccessInfo();
			for(String restAccessId : RestAccessIdList) {
				retList.add(new SelectRestAccessInfo().getRestAccessInfo(restAccessId));
				restAccess.delete(restAccessId);
				//設定削除時、不要なので、アクセストークンのキャッシュはクリア 
				jtm.addCallback(new RestAccesccTokenCacheRemoveCallback(restAccessId));
			}
			jtm.commit();
		} catch (InvalidRole | HinemosUnknown e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (RestAccessNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (RestAccessUsed e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteRestAccess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * 引数で指定されたRESTアクセス情報を返します。
	 * 
	 * @param RestAccessId 取得対象のRESTアクセスID
	 * @return RestAccessInfo
	 * @throws HinemosUnknown
	 * @throws RestAccessNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.restaccess.factory.SelectRestAccessInfo#getRestAccessInfo(String)
	 */
	public RestAccessInfo getRestAccessInfo(String RestAccessId) throws RestAccessNotFound, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		RestAccessInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// RESTアクセス情報を取得
			SelectRestAccessInfo RestAccess = new SelectRestAccessInfo();
			info = RestAccess.getRestAccessInfo(RestAccessId);
			// 更新対象外なのでdetachしておく
			jtm.getEntityManager().detach(info);
			jtm.commit();
		} catch (InvalidRole | RestAccessNotFound e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRestAccessInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
	 * オーナーロールIDを指定してRESTアクセス情報一覧を取得します。<BR>
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return RESTアクセス情報一覧
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * 
	 * @see com.clustercontrol.notify.restaccess.factory.SelectRestAccessInfo#getRestAccessListByOwnerRole()
	 */
	public ArrayList<RestAccessInfo> getRestAccessListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<RestAccessInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// RESTアクセス一覧を取得
			SelectRestAccessInfo RestAccess = new SelectRestAccessInfo();
			if (ownerRoleId != null) {
				list = RestAccess.getRestAccessListByOwnerRole(ownerRoleId);
			} else {
				list = RestAccess.getRestAccessList();
			}
			// 更新対象外なのでdetachしておく
			for(RestAccessInfo rec:list){
				jtm.getEntityManager().detach(rec);
			}

			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getRestAccessListByOwnerRole() : "
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

}
