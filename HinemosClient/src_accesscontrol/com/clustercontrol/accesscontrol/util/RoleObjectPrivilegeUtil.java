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

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.ObjectPrivilegeFilterInfo;
import com.clustercontrol.ws.access.ObjectPrivilegeInfo;

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
	public static List<ObjectPrivilegeInfo> beanList2dtoList(List<ObjectPrivilegeBean> beanList) {

		List<ObjectPrivilegeInfo> resultList = new ArrayList<ObjectPrivilegeInfo>();

		for(ObjectPrivilegeBean bean : beanList) {

			// 権限情報毎に ObjectPrivilegeInfo を作成する
			ObjectPrivilegeInfo info = null;
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
	public static List<ObjectPrivilegeInfo> beanMap2dtoList(HashMap<String, ObjectPrivilegeBean> beanMap) {

		List<ObjectPrivilegeInfo> resultList = new ArrayList<ObjectPrivilegeInfo>();
		ObjectPrivilegeBean bean = null;

		for(Map.Entry<String, ObjectPrivilegeBean> keyValue : beanMap.entrySet()) {

			bean = keyValue.getValue();

			// 権限情報毎に ObjectPrivilegeInfo を作成する
			// 参照権限
			if(bean.getReadPrivilege()){
				ObjectPrivilegeInfo info = getCommon(bean);
				info.setObjectPrivilege(ObjectPrivilegeMode.READ.toString());
				resultList.add(info);
			}
			// 更新権限
			if(bean.getWritePrivilege()){
				ObjectPrivilegeInfo info = getCommon(bean);
				info.setObjectPrivilege(ObjectPrivilegeMode.MODIFY.toString());
				resultList.add(info);
			}
			// 実行権限
			if(bean.getExecPrivilege()){
				ObjectPrivilegeInfo info = getCommon(bean);
				info.setObjectPrivilege(ObjectPrivilegeMode.EXEC.toString());
				resultList.add(info);
			}
		}

		return resultList;
	}

	// utilMap2dtoList() 内の共通操作を外だし
	private static ObjectPrivilegeInfo getCommon(ObjectPrivilegeBean bean) {
		ObjectPrivilegeInfo info = new ObjectPrivilegeInfo();

		// ロールID
		info.setRoleId(bean.getRoleId());

		return info;
	}

	/**
	 * DBのオブジェクト権限情報のリストをロール単位の情報(HashMap)に変換する
	 */
	public static HashMap<String, ObjectPrivilegeBean> dto2beanMap(List<ObjectPrivilegeInfo> infoList) {

		HashMap<String, ObjectPrivilegeBean> resultMap = new HashMap<String, ObjectPrivilegeBean>();
		ObjectPrivilegeBean bean = null;

		if(infoList == null){
			return null;
		}

		for(ObjectPrivilegeInfo info : infoList) {

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
		List<ObjectPrivilegeInfo> objectPrivilegeList = null;

		// フィルタ条件を設定
		ObjectPrivilegeFilterInfo filter = new ObjectPrivilegeFilterInfo();
		filter.setObjectId(objectId);
		filter.setObjectType(objectType);

		try {
			AccessEndpointWrapper wrapper = AccessEndpointWrapper.getWrapper(managerName);
			objectPrivilegeList = wrapper.getObjectPrivilegeInfoList(filter);
		}
		catch (InvalidRole_Exception e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getOwnUserList(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		return dto2beanMap(objectPrivilegeList);
	}

	//	/**
	//	 * ObjectPrivilegeUtil のリストから DB に投入
	//	 */
	//	public void utilMap2dto(HashMap<String, ObjectPrivilegeUtil> utilMap) {
	//
	//		// 既存のデータから削除するリスト
	//		List<ObjectPrivilegeUtil> delegeList = new ArrayList<ObjectPrivilegeUtil>();
	//		// 既存のデータに挿入するリスト
	//		List<ObjectPrivilegeUtil> insertList = new ArrayList<ObjectPrivilegeUtil>();
	//
	//
	//
	//	}
}
