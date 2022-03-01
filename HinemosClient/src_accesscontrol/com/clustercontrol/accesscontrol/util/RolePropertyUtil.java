/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.ArrayList;
import java.util.Locale;

import org.openapitools.client.model.RoleInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;

/**
 * オブジェクト権限情報のDTOとロールごとのオブジェクト権限をまとめた Hashlist を相互変換するためのユーティリティクラスです。
 * 
 * @version 4.0.0
 */
public class RolePropertyUtil {
	/** ロールID */
	public static final String ROLE_ID = "roleId";
	/** 名前 */
	public static final String NAME = "namae";
	/** 説明 */
	public static final String DESCRIPTION = "description";
	/** 作成日時 */
	public static final String CREATE_TIME = "createTimestamp";
	/** 新規作成ユーザ */
	public static final String CREATOR_NAME = "creatorName";
	/** 最終更新ユーザ */
	public static final String MODIFIER_NAME = "modifierName";
	/** 最終更新日時 */
	public static final String MODIFY_TIME = "modifyTime";

	public static RoleInfoResponse property2dto(Property property){
		RoleInfoResponse info = new RoleInfoResponse();
		info.setCreateDate("");
		info.setModifyDate("");

		ArrayList<?> object = null;

		//ロールID
		object = PropertyUtil.getPropertyValue(property, RolePropertyUtil.ROLE_ID);
		if (object.size() > 0) {
			info.setRoleId((String)object.get(0));
		}
		//名前
		object = PropertyUtil.getPropertyValue(property, RolePropertyUtil.NAME);
		if (object.size() > 0) {
			info.setRoleName((String)object.get(0));
		}
		//説明
		object = PropertyUtil.getPropertyValue(property, RolePropertyUtil.DESCRIPTION);
		if (object.size() > 0) {
			info.setDescription((String)object.get(0));
		}
		//登録者
		object = PropertyUtil.getPropertyValue(property, RolePropertyUtil.CREATOR_NAME);
		if (object.size() > 0) {
			info.setCreateUserId((String)object.get(0));
		}
		//登録日時
		object = PropertyUtil.getPropertyValue(property, RolePropertyUtil.CREATE_TIME);
		if (object.size() > 0 && object.get(0) != null && !object.get(0).toString().equals("")) {
			info.setCreateDate( (String) object.get(0) );
		}
		//更新者
		object = PropertyUtil.getPropertyValue(property, RolePropertyUtil.MODIFIER_NAME);
		if (object.size() > 0) {
			info.setModifyUserId((String)object.get(0));
		}
		//更新日時
		object = PropertyUtil.getPropertyValue(property, RolePropertyUtil.MODIFY_TIME);
		if (object.size() > 0 && object.get(0) != null && !object.get(0).toString().equals("")) {
			info.setModifyDate((String) object.get(0));
		}
		//		//所属Role
		//		object = PropertyUtil.getProperty(property, RoleConstant.USER);
		//		if (object.size() > 0) {
		//			Property userProperty = (Property)object.get(0);
		//			for(String user : getUserList().getRoles()){
		//				ArrayList roleProperties = PropertyUtil.getProperty(accessProperty, role);
		//				if(roleProperties.size() > 0 && (Boolean)((Property)roleProperties.get(0)).getValue()){
		//					List<String> userList = info.getUserList();
		//					userList.add(user);
		//				}
		//			}
		//		}

		return info;
	}

	public static Property dto2property(RoleInfoResponse info, int mode, Locale locale){
		Property property = getProperty(mode, locale);
		ArrayList<Property> propertyList = null;

		if(info != null){
			//ロールID
			propertyList = PropertyUtil.getProperty(property, RolePropertyUtil.ROLE_ID);
			((Property)propertyList.get(0)).setValue(info.getRoleId());
			//名前
			propertyList = PropertyUtil.getProperty(property, RolePropertyUtil.NAME);
			if(info.getRoleName() != null && info.getRoleName().compareTo("") != 0){
				((Property)propertyList.get(0)).setValue(info.getRoleName());
			}
			//説明
			propertyList = PropertyUtil.getProperty(property, RolePropertyUtil.DESCRIPTION);
			if(info.getDescription() != null && info.getDescription().compareTo("") != 0){
				((Property)propertyList.get(0)).setValue(info.getDescription());
			}
			//登録者
			propertyList = PropertyUtil.getProperty(property, RolePropertyUtil.CREATOR_NAME);
			if(info.getCreateUserId() != null && info.getCreateUserId().compareTo("") != 0){
				((Property)propertyList.get(0)).setValue(info.getCreateUserId());
			}
			//登録日時
			propertyList = PropertyUtil.getProperty(property, RolePropertyUtil.CREATE_TIME);
			if(info.getCreateDate() != null){
				((Property)propertyList.get(0)).setValue( info.getCreateDate());
			}
			//更新者
			propertyList = PropertyUtil.getProperty(property, RolePropertyUtil.MODIFIER_NAME);
			if(info.getModifyUserId() != null && info.getModifyUserId().compareTo("") != 0){
				((Property)propertyList.get(0)).setValue(info.getModifyUserId());
			}
			//更新日時
			propertyList = PropertyUtil.getProperty(property, RolePropertyUtil.MODIFY_TIME);
			if(info.getModifyDate() != null){
				((Property)propertyList.get(0)).setValue( info.getModifyDate()  );
			}
			//			//所属Role
			//			for (String user : info.getUserList()) {
			//				((Property)propertyList.get(0)).setValue( new Date(info.getModifyDate()) );
			//			}

		}
		return property;
	}

	/**
	 * ロール用プロパティを返却する
	 * 
	 * @param mode PropertyConstant.MODE_ADDまたはMODE_MODIFY
	 * @param locale
	 * @return
	 */
	private static Property getProperty(int mode, Locale locale) {
		//ロールID
		Property roleId =
				new Property(RolePropertyUtil.ROLE_ID, Messages.getString("role.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//名前
		Property name =
				new Property(RolePropertyUtil.NAME, Messages.getString("name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//説明
		Property description =
				new Property(RolePropertyUtil.DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//登録日時
		Property createTime =
				new Property(RolePropertyUtil.CREATE_TIME, Messages.getString("create.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//登録者
		Property creatorName =
				new Property(RolePropertyUtil.CREATOR_NAME, Messages.getString("creator.name", locale), PropertyDefineConstant.EDITOR_TEXT);
		//更新日時
		Property modifyTime =
				new Property(RolePropertyUtil.MODIFY_TIME, Messages.getString("update.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//更新者
		Property modifierName =
				new Property(RolePropertyUtil.MODIFIER_NAME, Messages.getString("modifier.name", locale), PropertyDefineConstant.EDITOR_TEXT);

		//値を初期化
		roleId.setValue("");
		name.setValue("");
		description.setValue("");
		createTime.setValue("");
		creatorName.setValue("");
		modifyTime.setValue("");
		modifierName.setValue("");

		//モードにより、変更可及びコピー可を設定
		if(mode == PropertyDefineConstant.MODE_ADD){
			roleId.setModify(PropertyDefineConstant.MODIFY_OK);
			name.setModify(PropertyDefineConstant.MODIFY_OK);
			description.setModify(PropertyDefineConstant.MODIFY_OK);
		}
		else if(mode == PropertyDefineConstant.MODE_MODIFY){
			name.setModify(PropertyDefineConstant.MODIFY_OK);
			description.setModify(PropertyDefineConstant.MODIFY_OK);
		}

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(roleId);
		property.addChildren(name);
		property.addChildren(description);
		property.addChildren(createTime);
		property.addChildren(creatorName);
		property.addChildren(modifyTime);
		property.addChildren(modifierName);

		return property;
	}
}
