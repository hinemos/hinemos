/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RestAccessNotFound;
import com.clustercontrol.notify.restaccess.model.RestAccessAuthHttpHeader;
import com.clustercontrol.notify.restaccess.model.RestAccessInfo;
import com.clustercontrol.notify.restaccess.model.RestAccessSendHttpHeader;
import com.clustercontrol.notify.restaccess.util.RestAccessQueryUtil;

public class SelectRestAccessInfo {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectRestAccessInfo.class );

	/**
	 * RESTアクセス情報を返します。
	 * 
	 * @param RestAccessId 取得対象のRESTアクセスID
	 * @return RESTアクセス情報
	 * @throws RestAccessNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.notify.ejb.entity.RestAccessInfoBean
	 */
	public RestAccessInfo getRestAccessInfo(String RestAccessId) throws RestAccessNotFound, InvalidRole {
		// RESTアクセス情報を取得
		RestAccessInfo entity = null;
		entity = RestAccessQueryUtil.getRestAccessInfoPK(RestAccessId);
		sortHeaders(entity);
		return entity;
	}

	/**
	 * RESTアクセス情報一覧を返します。
	 * <p>
	 * 
	 * @return RESTアクセス情報一覧(ArrayList)
	 * @throws RestAccessNotFound
	 * 
	 * @see #collectionToArray(Collection)
	 */
	public ArrayList<RestAccessInfo> getRestAccessList() {
		m_log.debug("getRestAccessList() : start");
		ArrayList<RestAccessInfo> list = new ArrayList<RestAccessInfo>();

		// RESTアクセス情報一覧を取得
		List<RestAccessInfo> ct = RestAccessQueryUtil.getAllRestAccessInfoOrderByRestAccessId();

		for(RestAccessInfo entity : ct){
			sortHeaders(entity);
			list.add(entity);
			// for debug
			if(m_log.isDebugEnabled()){
				m_log.debug("getRestAccessList() : " + entity.toString());
			}
		}
		return list;
	}

	/**
	 * オーナーロールIDを条件としてRESTアクセス情報一覧を返します。
	 * 
	 * @param ownerRoleId
	 * @return RESTアクセス情報一覧(ArrayList)
	 * @throws RestAccessNotFound
	 * 
	 */
	public ArrayList<RestAccessInfo> getRestAccessListByOwnerRole(String ownerRoleId) {
		m_log.debug("getRestAccessListByOwnerRole() : start");
		ArrayList<RestAccessInfo> list = new ArrayList<RestAccessInfo>();

		// RESTアクセス情報一覧を取得
		List<RestAccessInfo> ct = RestAccessQueryUtil.getAllRestAccessInfoOrderByRestAccessId_OR(ownerRoleId);
		for(RestAccessInfo entity : ct){
			sortHeaders(entity);
			list.add(entity);
			// for debug
			if(m_log.isDebugEnabled()){
				m_log.debug("getRestAccessListByOwnerRole() : " + entity.toString());
			}
		}
		return list;
	}
	
	/**
	 * RESTアクセス情報内のhttpヘッダー情報をソートします
	 */
	private static void sortHeaders(RestAccessInfo target ){
		if( target.getSendHttpHeaders() != null ){
			ArrayList<RestAccessSendHttpHeader> sorted = new ArrayList<RestAccessSendHttpHeader>(
					target.getSendHttpHeaders());
			Collections.sort(sorted, new Comparator<RestAccessSendHttpHeader>() {
				public int compare(RestAccessSendHttpHeader c1, RestAccessSendHttpHeader c2) {
					if (c1.getId().getHeaderOrderNo() < c2.getId().getHeaderOrderNo()) {
						return -1;
					} else if (c1.getId().getHeaderOrderNo() > c2.getId().getHeaderOrderNo()) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			target.setSendHttpHeaders(sorted);
		}
		if( target.getAuthHttpHeaders() != null ){
			ArrayList<RestAccessAuthHttpHeader> sorted = new ArrayList<RestAccessAuthHttpHeader>(
					target.getAuthHttpHeaders());
			Collections.sort(sorted, new Comparator<RestAccessAuthHttpHeader>() {
				public int compare(RestAccessAuthHttpHeader c1, RestAccessAuthHttpHeader c2) {
					if (c1.getId().getHeaderOrderNo() < c2.getId().getHeaderOrderNo()) {
						return -1;
					} else if (c1.getId().getHeaderOrderNo() > c2.getId().getHeaderOrderNo()) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			target.setAuthHttpHeaders(sorted);
		}
		
	}

}
