/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.openapitools.client.model.SdmlControlSettingFilterInfoRequest;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;

public class SdmlControlSettingFilterPropertyUtil {

	/**
	 * プロパティをSDML制御設定フィルタDTOに変換するメソッドです。
	 *
	 * @param property
	 * @return
	 */
	public static SdmlControlSettingFilterInfoRequest property2dto(Property property) {
		SdmlControlSettingFilterInfoRequest info = new SdmlControlSettingFilterInfoRequest();
		ArrayList<?> values = null;

		// アプリケーションID
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.APPLICATION_ID);
		if (!"".equals(values.get(0))) {
			info.setApplicationId((String) values.get(0));
		}

		// 説明
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.DESCRIPTION);
		if (!"".equals(values.get(0))) {
			info.setDescription((String) values.get(0));
		}

		// ファシリティID
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.FACILITY_ID);
		if (!"".equals(values.get(0))) {
			FacilityTreeItemResponse item = (FacilityTreeItemResponse) values.get(0);
			String facilityId = item.getData().getFacilityId();
			info.setFacilityId(facilityId);
		}

		// 有効フラグ
		Boolean validFlg = null;
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.VALID_FLG);
		if (!"".equals(values.get(0))) {
			if (ValidMessage.STRING_VALID.equals(values.get(0))) {
				validFlg = true;
			} else {
				validFlg = false;
			}
		}
		info.setValidFlg(validFlg);

		// 新規作成者
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.REG_USER);
		if (!"".equals(values.get(0))) {
			info.setRegUser((String) values.get(0));
		}

		// 作成日時(From)
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.REG_FROM_DATE);
		if (values.get(0) instanceof Date) {
			info.setRegFromDate(TimezoneUtil.getSimpleDateFormat().format((Date) values.get(0)));
		}

		// 作成日時(To)
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.REG_TO_DATE);
		if (values.get(0) instanceof Date) {
			info.setRegToDate(TimezoneUtil.getSimpleDateFormat().format((Date) values.get(0)));
		}

		// 最終変更者
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.UPDATE_USER);
		if (!"".equals(values.get(0))) {
			info.setUpdateUser((String) values.get(0));
		}

		// 最終変更日時(From)
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.UPDATE_FROM_DATE);
		if (values.get(0) instanceof Date) {
			info.setUpdateFromDate(TimezoneUtil.getSimpleDateFormat().format((Date) values.get(0)));
		}

		// 最終変更日時(To)
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.UPDATE_TO_DATE);
		if (values.get(0) instanceof Date) {
			info.setUpdateToDate(TimezoneUtil.getSimpleDateFormat().format((Date) values.get(0)));
		}

		// オーナーロールID
		values = PropertyUtil.getPropertyValue(property, SdmlControlSettingFilterConstant.OWNER_ROLE_ID);
		if (!"".equals(values.get(0))) {
			info.setOwnerRoleId((String) values.get(0));
		}
		return info;
	}

	/**
	 * SDML制御設定用フィルタプロパティを取得します。<BR>
	 *
	 * @param locale
	 * @return フィルタプロパティ
	 */
	public static Property getProperty(Locale locale) {

		// マネージャ
		Property manager = new Property(SdmlControlSettingFilterConstant.MANAGER,
				Messages.getString("facility.manager", locale), PropertyDefineConstant.EDITOR_SELECT);

		// アプリケーションID
		Property applicationId = new Property(SdmlControlSettingFilterConstant.APPLICATION_ID,
				Messages.getString("application.id", locale), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.VARCHAR_64);

		// 説明
		Property description = new Property(SdmlControlSettingFilterConstant.DESCRIPTION,
				Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.VARCHAR_64);

		// ファシリティID
		Property facilityId = new Property(SdmlControlSettingFilterConstant.FACILITY_ID,
				Messages.getString("facility.name", locale), PropertyDefineConstant.EDITOR_FACILITY,
				DataRangeConstant.VARCHAR_64);

		// 有効フラグ
		Property validFlg = new Property(SdmlControlSettingFilterConstant.VALID_FLG,
				Messages.getString("valid", locale), PropertyDefineConstant.EDITOR_SELECT);

		// 新規作成ユーザ
		Property regUser = new Property(SdmlControlSettingFilterConstant.REG_USER,
				Messages.getString("creator.name", locale), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.VARCHAR_64);

		// 作成日時
		Property regDate = new Property(SdmlControlSettingFilterConstant.REG_DATE,
				Messages.getString("create.time", locale), PropertyDefineConstant.EDITOR_TEXT);

		// 作成日時(From)
		Property regFromDate = new Property(SdmlControlSettingFilterConstant.REG_FROM_DATE,
				Messages.getString("start", locale), PropertyDefineConstant.EDITOR_DATETIME);

		// 作成日時(To)
		Property regToDate = new Property(SdmlControlSettingFilterConstant.REG_TO_DATE,
				Messages.getString("end", locale), PropertyDefineConstant.EDITOR_DATETIME);

		// 最終変更ユーザ
		Property updateUser = new Property(SdmlControlSettingFilterConstant.UPDATE_USER,
				Messages.getString("modifier.name", locale), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.VARCHAR_64);

		// 最終変更日時
		Property updateDate = new Property(SdmlControlSettingFilterConstant.UPDATE_DATE,
				Messages.getString("update.time", locale), PropertyDefineConstant.EDITOR_TEXT);

		// 最終変更日時(From)
		Property updateFromDate = new Property(SdmlControlSettingFilterConstant.UPDATE_FROM_DATE,
				Messages.getString("start", locale), PropertyDefineConstant.EDITOR_DATETIME);

		// 最終変更日時(To)
		Property updateToDate = new Property(SdmlControlSettingFilterConstant.UPDATE_TO_DATE,
				Messages.getString("end", locale), PropertyDefineConstant.EDITOR_DATETIME);

		// オーナーロールID
		Property ownerRoleId = new Property(SdmlControlSettingFilterConstant.OWNER_ROLE_ID,
				Messages.getString("owner.role.id", locale), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.VARCHAR_64);

		Object[] obj = RestConnectManager.getActiveManagerSet().toArray();
		Object[] val = new Object[obj.length + 1];
		val[0] = "";
		for (int i = 0; i < obj.length; i++) {
			val[i + 1] = obj[i];
		}

		Object[][] managerValues = { val, val };
		manager.setSelectValues(managerValues);
		manager.setValue("");

		// 値を初期化
		applicationId.setValue("");
		description.setValue("");
		facilityId.setValue("");

		Object validFlgValues[][] = { { "", ValidMessage.STRING_VALID, ValidMessage.STRING_INVALID },
				{ "", ValidMessage.STRING_VALID, ValidMessage.STRING_INVALID } };
		validFlg.setSelectValues(validFlgValues);
		validFlg.setValue(ValidMessage.STRING_VALID);

		regUser.setValue("");
		regDate.setValue("");
		regFromDate.setValue("");
		regToDate.setValue("");

		updateUser.setValue("");
		updateDate.setValue("");
		updateFromDate.setValue("");
		updateToDate.setValue("");

		ownerRoleId.setValue("");

		// 変更の可/不可を設定
		manager.setModify(PropertyDefineConstant.MODIFY_OK);
		applicationId.setModify(PropertyDefineConstant.MODIFY_OK);
		description.setModify(PropertyDefineConstant.MODIFY_OK);
		facilityId.setModify(PropertyDefineConstant.MODIFY_OK);
		validFlg.setModify(PropertyDefineConstant.MODIFY_OK);

		regUser.setModify(PropertyDefineConstant.MODIFY_OK);
		regDate.setModify(PropertyDefineConstant.MODIFY_NG);
		regFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		regToDate.setModify(PropertyDefineConstant.MODIFY_OK);

		updateUser.setModify(PropertyDefineConstant.MODIFY_OK);
		updateDate.setModify(PropertyDefineConstant.MODIFY_NG);
		updateFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		updateToDate.setModify(PropertyDefineConstant.MODIFY_OK);

		ownerRoleId.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(manager);
		property.addChildren(applicationId);
		property.addChildren(description);
		property.addChildren(facilityId);
		property.addChildren(validFlg);
		property.addChildren(regUser);
		property.addChildren(regDate);
		property.addChildren(updateUser);
		property.addChildren(updateDate);
		property.addChildren(ownerRoleId);

		// 作成日時
		regDate.removeChildren();
		regDate.addChildren(regFromDate);
		regDate.addChildren(regToDate);

		// 最終変更日時
		updateDate.removeChildren();
		updateDate.addChildren(updateFromDate);
		updateDate.addChildren(updateToDate);

		return property;
	}
}
