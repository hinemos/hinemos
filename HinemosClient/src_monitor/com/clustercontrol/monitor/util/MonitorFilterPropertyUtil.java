/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.CalendarInfoResponse;
import org.openapitools.client.model.MonitorFilterInfoRequest;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.calendar.util.CalendarRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.bean.MonitorFilterConstant;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.RestConnectManager;

/**
 * 監視[一覧]のフィルタダイアログに関するutilityクラス
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class MonitorFilterPropertyUtil {

	// ログ
	private static Log m_log = LogFactory.getLog( MonitorFilterPropertyUtil.class );

	/**
	 * プロパティを監視設定フィルタDTOに変換するメソッドです。
	 *
	 * @param property
	 * @return
	 */
	public static MonitorFilterInfoRequest property2dto(Property property){
		MonitorFilterInfoRequest info = new MonitorFilterInfoRequest();

		String monitorId = null;	// 監視項目ID
		String monitorTypeId = null;// プラグインID
		String description = null;	// 説明
		String facilityId = null;	// ファシリティID
		String calendarId = null;	// カレンダ
		String regUser = null;		// 新規作成者
		Timestamp regFromDate = null;			// 作成日時(From)
		Timestamp regToDate = null;			// 作成日時(To)
		String updateUser = null;	// 最終変更者
		Timestamp updateFromDate = null;		// 最終変更日時(From)
		Timestamp updateToDate = null;		// 最終変更日時(To)
		Boolean monitorFlg = null;		// 監視有効フラグ
		Boolean collectorFlg = null;	// 収集有効フラグ
		String ownerRoleId = null;	// オーナーロールID

		ArrayList<?> values = null;

		//監視項目ID
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.MONITOR_ID);
		if (!"".equals(values.get(0))) {
			monitorId = (String) values.get(0);
			info.setMonitorId(monitorId);
		}

		//プラグインID
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.MONITOR_TYPE_ID);
		if (!"".equals(values.get(0))) {
			monitorTypeId = (String) values.get(0);
			info.setMonitorTypeId(monitorTypeId);
		}

		//説明
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.DESCRIPTION);
		if (!"".equals(values.get(0))) {
			description = (String) values.get(0);
			info.setDescription(description);
		}

		//ファシリティID
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.FACILITY_ID);
		if (!"".equals(values.get(0))) {
			FacilityTreeItemResponse item = (FacilityTreeItemResponse)values.get(0);
			facilityId = item.getData().getFacilityId();
			info.setFacilityId(facilityId);
		}

		//カレンダ
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.CALENDAR_ID);
		if (!"".equals(values.get(0))) {
			calendarId = (String) values.get(0);
			info.setCalendarId(calendarId);
		}

		//新規作成者
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.REG_USER);
		if (!"".equals(values.get(0))) {
			regUser = (String) values.get(0);
			info.setRegUser(regUser);
		}

		//作成日時(From)
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.REG_FROM_DATE);
		if (values.get(0) instanceof Date) {
			regFromDate = new Timestamp(((Date) values.get(0))
					.getTime());
			regFromDate.setNanos(999999999);
			info.setRegFromDate(TimezoneUtil.getSimpleDateFormat().format(regFromDate));
		}

		//作成日時(To)
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.REG_TO_DATE);
		if (values.get(0) instanceof Date) {
			regToDate = new Timestamp(((Date) values.get(0))
					.getTime());
			regToDate.setNanos(999999999);
			info.setRegToDate(TimezoneUtil.getSimpleDateFormat().format(regToDate));
		}

		//最終変更者
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.UPDATE_USER);
		if (!"".equals(values.get(0))) {
			updateUser = (String) values.get(0);
			info.setUpdateUser(updateUser);
		}
		//最終変更日時(From)
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.UPDATE_FROM_DATE);
		if (values.get(0) instanceof Date) {
			updateFromDate = new Timestamp(((Date) values.get(0))
					.getTime());
			updateFromDate.setNanos(999999999);
			info.setUpdateFromDate(TimezoneUtil.getSimpleDateFormat().format(updateFromDate));
		}

		//最終変更日時(To)
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.UPDATE_TO_DATE);
		if (values.get(0) instanceof Date) {
			updateToDate = new Timestamp(((Date) values.get(0))
					.getTime());
			updateToDate.setNanos(999999999);
			info.setUpdateToDate(TimezoneUtil.getSimpleDateFormat().format(updateToDate));
		}

		// 監視有効フラグ
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.MONITOR_FLG);
		if (!"".equals(values.get(0))) {
			if(ValidMessage.STRING_VALID.equals(values.get(0))){
				monitorFlg = true;
			}else{
				monitorFlg = false;
			}
		}
		info.setMonitorFlg(monitorFlg);

		// 収集有効フラグ
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.COLLECTOR_FLG);
		if (!"".equals(values.get(0))) {
			if(ValidMessage.STRING_VALID.equals(values.get(0))){
				collectorFlg = true;
			}else{
				collectorFlg = false;
			}
		}
		info.setCollectorFlg(collectorFlg);

		//オーナーロールID
		values = PropertyUtil.getPropertyValue(property,
				MonitorFilterConstant.OWNER_ROLE_ID);
		if (!"".equals(values.get(0))) {
			ownerRoleId = (String) values.get(0);
			info.setOwnerRoleId(ownerRoleId);
		}

		return info;
	}

	/**
	 * 監視設定用フィルタプロパティを取得します。<BR>
	 *
	 * @param locale
	 * @return
	 */
	public static Property getProperty(Locale locale) {

		//マネージャ
		Property manager =
				new Property(MonitorFilterConstant.MANAGER, Messages.getString("facility.manager", locale), PropertyDefineConstant.EDITOR_SELECT);

		//監視項目ID
		Property monitorId =
				new Property(MonitorFilterConstant.MONITOR_ID, Messages.getString("monitor.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//プラグインID
		Property monitorTypeId =
				new Property(MonitorFilterConstant.MONITOR_TYPE_ID, Messages.getString("plugin.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//説明
		Property description =
				new Property(MonitorFilterConstant.DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//ファシリティID
		Property facilityId =
				new Property(MonitorFilterConstant.FACILITY_ID, Messages.getString("facility.name", locale), PropertyDefineConstant.EDITOR_NODE, DataRangeConstant.VARCHAR_64);

		//カレンダ
		Property calendarId =
				new Property(MonitorFilterConstant.CALENDAR_ID, Messages.getString("calendar", locale), PropertyDefineConstant.EDITOR_SELECT);

		//新規作成者
		Property regUser =
				new Property(MonitorFilterConstant.REG_USER, Messages.getString("creator.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//作成日時
		Property regDate =
				new Property(MonitorFilterConstant.REG_DATE, Messages.getString("create.time", locale), PropertyDefineConstant.EDITOR_TEXT);

		//最終変更者
		Property updateUser =
				new Property(MonitorFilterConstant.UPDATE_USER, Messages.getString("modifier.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//最終変更日時
		Property updateDate =
				new Property(MonitorFilterConstant.UPDATE_DATE, Messages.getString("update.time", locale), PropertyDefineConstant.EDITOR_TEXT);

		//監視有効フラグ
		Property monitorFlg =
				new Property(MonitorFilterConstant.MONITOR_FLG, Messages.getString("monitor.valid.name", locale), PropertyDefineConstant.EDITOR_SELECT);

		//監視無効フラグ
		Property collectorFlg =
				new Property(MonitorFilterConstant.COLLECTOR_FLG, Messages.getString("collector.valid.name", locale), PropertyDefineConstant.EDITOR_SELECT);


		//作成日時(START)
		Property regFromDate =
				new Property(MonitorFilterConstant.REG_FROM_DATE, Messages.getString("start", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//作成日時(END)
		Property regToDate =
				new Property(MonitorFilterConstant.REG_TO_DATE, Messages.getString("end", locale), PropertyDefineConstant.EDITOR_DATETIME);

		//最終変更日時(START)
		Property updateFromDate =
				new Property(MonitorFilterConstant.UPDATE_FROM_DATE, Messages.getString("start", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//最終変更日時(END)
		Property updateToDate =
				new Property(MonitorFilterConstant.UPDATE_TO_DATE, Messages.getString("end", locale), PropertyDefineConstant.EDITOR_DATETIME);

		//オーナーロールID
		Property ownerRoleId =
				new Property(MonitorFilterConstant.OWNER_ROLE_ID, Messages.getString("owner.role.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		Object[] obj = RestConnectManager.getActiveManagerSet().toArray();
		Object[] val = new Object[obj.length + 1];
		val[0] = "";
		for(int i = 0; i<obj.length; i++) {
			val[i + 1] = obj[i];
		}

		Object[][] managerValues = {val, val};
		manager.setSelectValues(managerValues);
		manager.setValue("");

		//値を初期化
		Object monitorFlgValues[][] = {
				{ "", ValidMessage.STRING_VALID, ValidMessage.STRING_INVALID},
				{ "", ValidMessage.STRING_VALID, ValidMessage.STRING_INVALID}};
		Object collectorFlgValues[][] = {
				{ "", ValidMessage.STRING_VALID, ValidMessage.STRING_INVALID},
				{ "", ValidMessage.STRING_VALID, ValidMessage.STRING_INVALID}};


		monitorFlg.setSelectValues(monitorFlgValues);
		monitorFlg.setValue(ValidMessage.STRING_VALID);
		collectorFlg.setSelectValues(collectorFlgValues);
		collectorFlg.setValue(ValidMessage.STRING_VALID);

		monitorId.setValue("");
		monitorTypeId.setValue("");
		description.setValue("");
		facilityId.setValue("");

		calendarId.setSelectValues(getCalendarIdList());
		calendarId.setValue("");

		regUser.setValue("");
		regDate.setValue("");
		updateUser.setValue("");
		updateDate.setValue("");

		regFromDate.setValue("");
		regToDate.setValue("");
		updateFromDate.setValue("");
		updateToDate.setValue("");

		ownerRoleId.setValue("");

		//変更の可/不可を設定
		manager.setModify(PropertyDefineConstant.MODIFY_OK);
		monitorId.setModify(PropertyDefineConstant.MODIFY_OK);
		monitorTypeId.setModify(PropertyDefineConstant.MODIFY_OK);
		description.setModify(PropertyDefineConstant.MODIFY_OK);
		facilityId.setModify(PropertyDefineConstant.MODIFY_OK);
		calendarId.setModify(PropertyDefineConstant.MODIFY_OK);
		regUser.setModify(PropertyDefineConstant.MODIFY_OK);
		regDate.setModify(PropertyDefineConstant.MODIFY_NG);
		updateUser.setModify(PropertyDefineConstant.MODIFY_OK);
		updateDate.setModify(PropertyDefineConstant.MODIFY_NG);
		monitorFlg.setModify(PropertyDefineConstant.MODIFY_OK);
		collectorFlg.setModify(PropertyDefineConstant.MODIFY_OK);

		regFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		regToDate.setModify(PropertyDefineConstant.MODIFY_OK);
		updateFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		updateToDate.setModify(PropertyDefineConstant.MODIFY_OK);

		ownerRoleId.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(manager);
		property.addChildren(monitorId);
		property.addChildren(monitorTypeId);
		property.addChildren(description);
		property.addChildren(facilityId);
		property.addChildren(calendarId);
		property.addChildren(regUser);
		property.addChildren(regDate);
		property.addChildren(updateUser);
		property.addChildren(updateDate);
		property.addChildren(monitorFlg);
		property.addChildren(collectorFlg);
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

	/**
	 * カレンダIDのリストを配列で返却する
	 * @return
	 */
	private static Object[][] getCalendarIdList() {

		List<CalendarInfoResponse> calList = new ArrayList<>();
		Object retArray[][] = null;
		try{
			for(String managerName : RestConnectManager.getActiveManagerSet()) {
				CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(managerName);
				for(CalendarInfoResponse info : wrapper.getCalendarList(null)) {
					calList.add(info);
				}
			}
		} catch (InvalidUserPass e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (HinemosUnknown | RestConnectFailed e) {
			m_log.warn("getCalendarIdList(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		if(calList != null && calList.isEmpty() == false)
		{
			retArray = new Object[2][calList.size()+1];
			retArray[0][0] = "";
			retArray[1][0] = "";
			for (int i = 0; i < calList.size(); i++){
				retArray[0][i+1] = calList.get(i).getCalendarId();
				retArray[1][i+1] = calList.get(i).getCalendarId();
			}
		}
		else{
			Object nullArray[][] = {
					{ "" },
					{ "" }
			};
			retArray = nullArray;
		}
		return retArray;
	}
}
