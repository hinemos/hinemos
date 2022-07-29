/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.ObjectPrivilegeInfoResponse;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.Messages;

/**
 * ロール情報のDTOとプロパティを相互変換するためのユーティリティクラスです。
 *
 * @version 4.0.0
 */
public class RoleObjectPrivilegeUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(RoleObjectPrivilegeUtil.class);
	/** オブジェクトタイプ 	*/
	public static final String OBJECT_TYPE = "objectType";
	/** オブジェクトID */
	public static final String OBJECT_ID = "objectId";
	/** ロールID */
	public static final String ROLE_ID = "roleId";
	/** オブジェクト権限 */
	public static final String OBJECT_PRIVILEGE = "objectPrivilege";
	/** 作成日時 */
	public static final String CREATE_TIME = "createTime";
	/** 新規作成ユーザ */
	public static final String CREATE_USER = "createUser";
	/** 最終更新ユーザ */
	public static final String MODIFY_USER = "modifyUSER";
	/** 最終更新日時 */
	public static final String MODIFY_TIME = "modifyTime";

	/**
	 * ロールごとのオブジェクト権限をDB格納用の情報のリストに変換する(不要かも)
	 */
	public static List<ObjectPrivilegeInfoResponse> beanList2dtoList(List<ObjectPrivilegeBean> beanList) {

		List<ObjectPrivilegeInfoResponse> resultList = new ArrayList<ObjectPrivilegeInfoResponse>();

		for(ObjectPrivilegeBean bean : beanList) {

			// 権限情報毎に ObjectPrivilegeInfo を作成する
			ObjectPrivilegeInfoResponse info = null;
			// 参照権限が与えられている、もしくはオーナである場合
			if(bean.getReadPrivilege() || bean.getOwnerFlag()){
				info = getCommon(bean);
				info.setObjectPrivilege(ObjectPrivilegeMode.READ.toString());
				resultList.add(info);
			}
			// 更新権限
			if(bean.getWritePrivilege()){
				info = getCommon(bean);
				info.setObjectPrivilege(ObjectPrivilegeMode.MODIFY.toString());
				resultList.add(info);
			}

			// 実行権限
			if(bean.getExecPrivilege()){
				info = getCommon(bean);
				info.setObjectPrivilege(ObjectPrivilegeMode.EXEC.toString());
				resultList.add(info);
			}
		}

		return resultList;
	}

	/**
	 * ロールごとのオブジェクト権限の情報をDB格納用の情報のリストに変換する
	 */
	public static List<ObjectPrivilegeInfoResponse> beanMap2dtoList(HashMap<String, ObjectPrivilegeBean> beanMap) {

		List<ObjectPrivilegeInfoResponse> resultList = new ArrayList<ObjectPrivilegeInfoResponse>();
		ObjectPrivilegeBean bean = null;

		for(Map.Entry<String, ObjectPrivilegeBean> keyValue : beanMap.entrySet()) {

			bean = keyValue.getValue();

			// 権限情報毎に ObjectPrivilegeInfo を作成する
			// 参照権限
			if(bean.getReadPrivilege()){
				ObjectPrivilegeInfoResponse info = getCommon(bean);
				info.setObjectPrivilege(ObjectPrivilegeMode.READ.toString());
				resultList.add(info);
			}
			// 更新権限
			if(bean.getWritePrivilege()){
				ObjectPrivilegeInfoResponse info = getCommon(bean);
				info.setObjectPrivilege(ObjectPrivilegeMode.MODIFY.toString());
				resultList.add(info);
			}
			// 実行権限
			if(bean.getExecPrivilege()){
				ObjectPrivilegeInfoResponse info = getCommon(bean);
				info.setObjectPrivilege(ObjectPrivilegeMode.EXEC.toString());
				resultList.add(info);
			}
		}

		return resultList;
	}

	// utilMap2dtoList() 内の共通操作を外だし
	private static ObjectPrivilegeInfoResponse getCommon(ObjectPrivilegeBean bean) {
		ObjectPrivilegeInfoResponse info = new ObjectPrivilegeInfoResponse();

		// ロールID
		info.setRoleId(bean.getRoleId());

		return info;
	}

	/**
	 * DBのオブジェクト権限情報のリストをロール単位の情報(HashMap)に変換する
	 */
	public static HashMap<String, ObjectPrivilegeBean> dto2beanMap(List<ObjectPrivilegeInfoResponse> infoList) {

		HashMap<String, ObjectPrivilegeBean> resultMap = new HashMap<String, ObjectPrivilegeBean>();
		ObjectPrivilegeBean bean = null;

		if(infoList == null){
			return null;
		}

		for(ObjectPrivilegeInfoResponse info : infoList) {

			// ロールIDが初めて登場した場合の処理
			if(resultMap.get(info.getRoleId()) == null) {
				bean = new ObjectPrivilegeBean();
				bean.setRoleId(info.getRoleId());
				resultMap.put(info.getRoleId(), bean);
			}
			else
				bean = resultMap.get(info.getRoleId());

			if(info.getObjectPrivilege().equals(ObjectPrivilegeMode.READ.toString())) {
				bean.setReadPrivilege(true);
			}
			else if(info.getObjectPrivilege().equals(ObjectPrivilegeMode.MODIFY.toString())) {
				bean.setWritePrivilege(true);
			}
			else if(info.getObjectPrivilege().equals(ObjectPrivilegeMode.EXEC.toString())) {
				bean.setExecPrivilege(true);
			}

		}

		return resultMap;
	}


	/**
	 * オブジェクトID、オブジェクトタイプで、それひもづくオブジェクト権限を取得する
	 */
	public static HashMap<String, ObjectPrivilegeBean> dto2beanMap(String managerName, String objectId, String objectType) {

		// オブジェクト権限一覧をマネージャから取得
		List<ObjectPrivilegeInfoResponse> objectPrivilegeList = null;

		try {
			AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(managerName);
			objectPrivilegeList = wrapper.getObjectPrivilegeInfoList(objectType, objectId, null, null);
		}
		catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getOwnUserList(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
		}

		return dto2beanMap(objectPrivilegeList);
	}
}
