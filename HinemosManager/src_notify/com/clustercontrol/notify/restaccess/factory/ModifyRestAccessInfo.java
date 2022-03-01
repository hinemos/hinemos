/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.factory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaPersistenceConfig;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RestAccessDuplicate;
import com.clustercontrol.fault.RestAccessNotFound;
import com.clustercontrol.fault.RestAccessUsed;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyRestInfo;
import com.clustercontrol.notify.restaccess.model.RestAccessAuthHttpHeader;
import com.clustercontrol.notify.restaccess.model.RestAccessAuthHttpHeaderPK;
import com.clustercontrol.notify.restaccess.model.RestAccessInfo;
import com.clustercontrol.notify.restaccess.model.RestAccessSendHttpHeader;
import com.clustercontrol.notify.restaccess.model.RestAccessSendHttpHeaderPK;
import com.clustercontrol.notify.restaccess.util.RestAccessQueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

import jakarta.persistence.EntityExistsException;

/**
 * RESTアクセス情報を変更するクラス<BR>
 *
 */
public class ModifyRestAccessInfo {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyRestAccessInfo.class );

	/**
	 * RESTアクセス情報を作成します。
	 * <p>
	 * <ol>
	 *  <li>RESTアクセス情報を作成します。</li>
	 * </ol>
	 * 
	 * @param info 作成対象のRESTアクセス情報
	 * @param name RESTアクセス情報を作成したユーザ名
	 * @throws RestAccessDuplicate
	 * 
	 */
	public void add(RestAccessInfo data, String name) throws RestAccessDuplicate {

		long now = HinemosTime.currentTimeMillis();

		//エンティティBeanを作る
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 重複チェック
			jtm.checkEntityExists(RestAccessInfo.class, data.getRestAccessId());
			data.setRegDate(now);
			data.setRegUser(name);
			data.setUpdateDate(now);
			data.setUpdateUser(name);
			
			em.persist(data);
		} catch (EntityExistsException e) {
			m_log.info("add() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new RestAccessDuplicate(e.getMessage(),e);
		}
	}
	
	/**
	 * RESTアクセス情報を変更します。
	 * <p>
	 * <ol>
	 *  <li>RESTアクセスIDより、RESTアクセス情報を取得し、
	 *      RESTアクセス情報を変更します。</li>
	 * </ol>
	 * 
	 * @param info 変更対象のRESTアクセス情報
	 * @param name 変更したユーザ名
	 * @throws RestAccessNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.RestAccessInfoBean
	 */
	public void modify(RestAccessInfo data, String name) throws RestAccessNotFound, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//RESTアクセス情報を取得
			RestAccessInfo restAccessInfo = RestAccessQueryUtil.getRestAccessInfoPK(data.getRestAccessId(), ObjectPrivilegeMode.MODIFY);
			
	
			//RESTアクセス情報を変更
			restAccessInfo.setDescription(data.getDescription());
			restAccessInfo.setHttpConnectTimeout(data.getHttpConnectTimeout());
			restAccessInfo.setHttpRequestTimeout(data.getHttpRequestTimeout());
			restAccessInfo.setHttpRetryNum(data.getHttpRetryNum());
			restAccessInfo.setUseWebProxy(data.getUseWebProxy());
			restAccessInfo.setWebProxyUrlString(data.getWebProxyUrlString());
			restAccessInfo.setWebProxyPort(data.getWebProxyPort());
			restAccessInfo.setWebProxyAuthUser(data.getWebProxyAuthUser());
			restAccessInfo.setWebProxyAuthPassword(data.getWebProxyAuthPassword());
			restAccessInfo.setSendHttpMethodType(data.getSendHttpMethodType());
			restAccessInfo.setSendUrlString(data.getSendUrlString());
			restAccessInfo.setSendHttpBody(data.getSendHttpBody());
			restAccessInfo.setAuthType(data.getAuthType());
			restAccessInfo.setAuthBasicUser(data.getAuthBasicUser());
			restAccessInfo.setAuthBasicPassword(data.getAuthBasicPassword());
			restAccessInfo.setAuthUrlMethodType(data.getAuthUrlMethodType());
			restAccessInfo.setAuthUrlString(data.getAuthUrlString());
			restAccessInfo.setAuthUrlBody(data.getAuthUrlBody());
			restAccessInfo.setAuthUrlGetRegex(data.getAuthUrlGetRegex());
			restAccessInfo.setAuthUrlValidTerm(data.getAuthUrlValidTerm());

			// SendHeader（配列）
			if(restAccessInfo.getSendHttpHeaders() != null && !(restAccessInfo.getSendHttpHeaders().isEmpty()) ){
				Set<RestAccessSendHttpHeaderPK> newSendHeaderPkSet = new HashSet<RestAccessSendHttpHeaderPK>();
				if (data.getSendHttpHeaders() != null) {
					for(RestAccessSendHttpHeader newRec: data.getSendHttpHeaders()){
						RestAccessSendHttpHeader target = em.find(RestAccessSendHttpHeader.class, newRec.getId(), JpaPersistenceConfig.JPA_EXISTS_CHECK_HINT_MAP, ObjectPrivilegeMode.NONE);
						if(target == null){
							//登録
							newRec.setRestAccessInfoEntity(restAccessInfo);
							em.persist(newRec);
							restAccessInfo.getSendHttpHeaders().add(newRec);
						}else{
							//変更
							target.setKey(newRec.getKey());
							target.setValue(newRec.getValue());
						}
						newSendHeaderPkSet.add(newRec.getId());
					}
				}
				Iterator<RestAccessSendHttpHeader> iter = restAccessInfo.getSendHttpHeaders().iterator();
				while (iter.hasNext()) {
					RestAccessSendHttpHeader entity = iter.next();
					if (!(newSendHeaderPkSet.contains(entity.getId()))) {
						// 差分削除
						iter.remove();
						em.remove(entity);
					}
				}
			}else{
				//空なら一括登録
				restAccessInfo.setSendHttpHeaders(data.getSendHttpHeaders());
			}

			// AuthHeader（配列）
			if(restAccessInfo.getAuthHttpHeaders() != null && !(restAccessInfo.getAuthHttpHeaders().isEmpty()) ){
				Set<RestAccessAuthHttpHeaderPK> newAuthHeaderPkSet = new HashSet<RestAccessAuthHttpHeaderPK>();
				if (data.getAuthHttpHeaders() != null) {
					for(RestAccessAuthHttpHeader newRec: data.getAuthHttpHeaders()){
						RestAccessAuthHttpHeader target = em.find(RestAccessAuthHttpHeader.class, newRec.getId(), JpaPersistenceConfig.JPA_EXISTS_CHECK_HINT_MAP, ObjectPrivilegeMode.NONE);
						if(target == null){
							//登録
							newRec.setRestAccessInfoEntity(restAccessInfo);
							em.persist(newRec);
							restAccessInfo.getAuthHttpHeaders().add(newRec);
						}else{
							//変更
							target.setKey(newRec.getKey());
							target.setValue(newRec.getValue());
						}
						newAuthHeaderPkSet.add(newRec.getId());
					}
				}
				Iterator<RestAccessAuthHttpHeader> iter = restAccessInfo.getAuthHttpHeaders().iterator();
				while (iter.hasNext()) {
					RestAccessAuthHttpHeader entity = iter.next();
					if (!(newAuthHeaderPkSet.contains(entity.getId()))) {
						// 差分削除
						iter.remove();
						em.remove(entity);
					}
				}
			}else{
				//空なら一括登録
				restAccessInfo.setAuthHttpHeaders(data.getAuthHttpHeaders());
			}

			// 自動変更項目
			restAccessInfo.setUpdateDate(HinemosTime.currentTimeMillis());
			restAccessInfo.setUpdateUser(name);
		}
	}
	
	/**
	 * RESTアクセス情報を削除します。<BR>
	 * <p>
	 * <ol>
	 *  <li>RESTアクセスIDより、RESTアクセス情報を取得し、
	 *      RESTアクセス情報を削除します。</li>
	 * </ol>
	 * 
	 * @param RestAccessId 削除対象のRESTアクセスID
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws RestAccessNotFound 
	 */
	public void delete(String restAccessId) throws InvalidRole, HinemosUnknown ,RestAccessUsed, RestAccessNotFound{

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// RESTアクセス情報を検索し取得
			RestAccessInfo entity = null;
			try {
				entity = RestAccessQueryUtil.getRestAccessInfoPK(restAccessId, ObjectPrivilegeMode.MODIFY);
			} catch (RestAccessNotFound e) {
				throw new RestAccessNotFound(e.getMessage(), e);
			}

			// 利用されているRESTアクセスか否かチェックする
			List<NotifyInfo> notifyList = com.clustercontrol.notify.util.QueryUtil.getAllNotifyInfo_NONE();
			for (NotifyInfo notify : notifyList) {
				NotifyRestInfo restInfo = notify.getNotifyRestInfo();
				if (restInfo == null ) {
					continue;
				}
				boolean isUsed = false;
				if (restAccessId.equals(restInfo.getInfoRestAccessId())) {
					isUsed = true;
				}else if(restAccessId.equals(restInfo.getWarnRestAccessId())) {
					isUsed = true;
				}else if(restAccessId.equals(restInfo.getCriticalRestAccessId())) {
					isUsed = true;
				}else if(restAccessId.equals(restInfo.getUnknownRestAccessId())) {
					isUsed = true;
				}
				if (isUsed) {
					String[] args ={restAccessId,notify.getNotifyId() }; 
					m_log.info("Failed to delete due to reference from a notify. RestAccessID=" + restAccessId
							+ ",NotifyID=" + notify.getNotifyId());
					throw new RestAccessUsed(MessageConstant.MESSAGE_DELETE_NG_NOTIFY_REFERENCE_TO_RESTACCESS.getMessage(args));
				}
			}
			
			//RESTアクセス情報を削除
			if(entity.getSendHttpHeaders() != null){
				for(RestAccessSendHttpHeader rec: entity.getSendHttpHeaders()){
					em.remove(rec);
				}
			}
			if(entity.getAuthHttpHeaders() != null){
				for(RestAccessAuthHttpHeader rec: entity.getAuthHttpHeaders()){
					em.remove(rec);
				}
			}
			em.remove(entity);

		}
	}	
}
