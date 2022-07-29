/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.CalendarInfoResponse;
import org.openapitools.client.model.JobKickFilterInfoRequest;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.calendar.util.CalendarRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.jobmanagement.bean.JobKickFilterConstant;
import com.clustercontrol.jobmanagement.bean.JobKickTypeMessage;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;

/**
 * 実行契機フィルタの検索条件ユーティリティクラス
 *
 * @version 5.1.0
 */
public class JobKickFilterPropertyUtil {
	// ログ
	private static Log m_log = LogFactory.getLog( JobKickFilterPropertyUtil.class );

	/**
	 * プロパティをジョブ実行契機フィルタDTOに変換するメソッドです。
	 *
	 * @param property
	 * @return
	 */
	public static JobKickFilterInfoRequest property2dto(Property property){
		JobKickFilterInfoRequest info = new JobKickFilterInfoRequest();
		ArrayList<?> values = null;
		
		// 実行契機ID
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.JOBKICK_ID);
		if (!"".equals(values.get(0))) {
			info.setJobkickId((String) values.get(0));
		}
		// 実行契機名
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.JOBKICK_NAME);
		if (!"".equals(values.get(0))) {
			info.setJobkickName((String) values.get(0));
		}
		// 実行契機種別
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.JOBKICK_TYPE);
		if(!"".equals(values.get(0))){
			JobKickFilterInfoRequest.JobkickTypeEnum TypeNum = JobKickFilterInfoRequest.JobkickTypeEnum
					.fromValue(JobKickTypeMessage.stringToTypeEnumValue((String)values.get(0)));
			info.setJobkickType(TypeNum);
		}
		// ジョブユニットID
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.JOBUNIT_ID);
		if (!"".equals(values.get(0))) {
			info.setJobunitId((String) values.get(0));
		}
		// ジョブID
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.JOB_ID);
		if (!"".equals(values.get(0))) {
			info.setJobId((String) values.get(0));
		}
		// カレンダID
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.CALENDAR_ID);
		if (!"".equals(values.get(0))) {
			info.setCalendarId((String) values.get(0));
		}
		// 有効フラグ
		Boolean validFlg = null;
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.VALID_FLG);
		if (!"".equals(values.get(0))) {
			if(ValidMessage.STRING_VALID.equals(values.get(0))){
				validFlg = true;
			}else{
				validFlg = false;
			}
		}
		info.setValidFlg(validFlg);

		//新規作成者
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.REG_USER);
		if (!"".equals(values.get(0))) {
			info.setRegUser((String) values.get(0));
		}

		//作成日時(From)
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.REG_FROM_DATE);
		if (values.get(0) instanceof Date) {
			info.setRegFromDate(TimezoneUtil.getSimpleDateFormat().format((Date)values.get(0)));
		}

		//作成日時(To)
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.REG_TO_DATE);
		if (values.get(0) instanceof Date) {
			info.setRegToDate(TimezoneUtil.getSimpleDateFormat().format((Date)values.get(0)));
		}

		//最終変更者
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.UPDATE_USER);
		if (!"".equals(values.get(0))) {
			info.setUpdateUser((String) values.get(0));
		}
		//最終変更日時(From)
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.UPDATE_FROM_DATE);
		if (values.get(0) instanceof Date) {
			info.setUpdateFromDate(TimezoneUtil.getSimpleDateFormat().format((Date)values.get(0)));
		}

		//最終変更日時(To)
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.UPDATE_TO_DATE);
		if (values.get(0) instanceof Date) {
			info.setUpdateToDate(TimezoneUtil.getSimpleDateFormat().format((Date)values.get(0)));
		}
		
		//オーナーロールID
		values = PropertyUtil.getPropertyValue(property,
				JobKickFilterConstant.OWNER_ROLE_ID);
		if (!"".equals(values.get(0))) {
			info.setOwnerRoleId((String) values.get(0));
		}
		return info;
	}

	/**
	 * ジョブ実行契機用フィルタプロパティを取得します。<BR>
	 *
	 * @param locale
	 * @return フィルタプロパティ
	 */
	public static Property getProperty(Locale locale) {

		// マネージャ
		Property manager =
				new Property(JobKickFilterConstant.MANAGER, Messages.getString("facility.manager", locale), PropertyDefineConstant.EDITOR_SELECT);

		// 実行契機ID
		Property jobkickId =
				new Property(JobKickFilterConstant.JOBKICK_ID, Messages.getString("jobkick.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		// 実行契機名
		Property jobkickName =
				new Property(JobKickFilterConstant.JOBKICK_NAME, Messages.getString("jobkick.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		// 実行契機種別
		Property jobkickType =
				new Property(JobKickFilterConstant.JOBKICK_TYPE, Messages.getString("jobkick.type", locale), PropertyDefineConstant.EDITOR_SELECT);

		// ジョブユニットID
		Property jobunitId =
				new Property(JobKickFilterConstant.JOBUNIT_ID, Messages.getString("jobunit.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		// ジョブID
		Property jobId =
				new Property(JobKickFilterConstant.JOB_ID, Messages.getString("job.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		// カレンダID
		Property calendarId =
				new Property(JobKickFilterConstant.CALENDAR_ID, Messages.getString("calendar", locale), PropertyDefineConstant.EDITOR_SELECT);

		// 有効フラグ
		Property validFlg =
				new Property(JobKickFilterConstant.VALID_FLG, Messages.getString("valid", locale), PropertyDefineConstant.EDITOR_SELECT);

		//新規作成者
		Property regUser =
				new Property(JobKickFilterConstant.REG_USER, Messages.getString("creator.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//作成日時
		Property regDate =
				new Property(JobKickFilterConstant.REG_DATE, Messages.getString("create.time", locale), PropertyDefineConstant.EDITOR_TEXT);

		//最終変更者
		Property updateUser =
				new Property(JobKickFilterConstant.UPDATE_USER, Messages.getString("modifier.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		//最終変更日時
		Property updateDate =
				new Property(JobKickFilterConstant.UPDATE_DATE, Messages.getString("update.time", locale), PropertyDefineConstant.EDITOR_TEXT);

		//作成日時(START)
		Property regFromDate =
				new Property(JobKickFilterConstant.REG_FROM_DATE, Messages.getString("start", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//作成日時(END)
		Property regToDate =
				new Property(JobKickFilterConstant.REG_TO_DATE, Messages.getString("end", locale), PropertyDefineConstant.EDITOR_DATETIME);

		//最終変更日時(START)
		Property updateFromDate =
				new Property(JobKickFilterConstant.UPDATE_FROM_DATE, Messages.getString("start", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//最終変更日時(END)
		Property updateToDate =
				new Property(JobKickFilterConstant.UPDATE_TO_DATE, Messages.getString("end", locale), PropertyDefineConstant.EDITOR_DATETIME);

		//オーナーロールID
		Property ownerRoleId =
				new Property(JobKickFilterConstant.OWNER_ROLE_ID, Messages.getString("owner.role.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

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
		jobkickId.setValue("");
		jobkickName.setValue("");

		Object jobkickTypeValues[][] = {
				{"", JobKickTypeMessage.STRING_MANUAL, JobKickTypeMessage.STRING_FILECHECK, JobKickTypeMessage.STRING_SCHEDULE, JobKickTypeMessage.STRING_JOBLINKRCV},
				{"", JobKickTypeMessage.STRING_MANUAL, JobKickTypeMessage.STRING_FILECHECK, JobKickTypeMessage.STRING_SCHEDULE, JobKickTypeMessage.STRING_JOBLINKRCV}};
		jobkickType.setSelectValues(jobkickTypeValues);
		jobkickType.setValue("");

		jobunitId.setValue("");
		jobId.setValue("");

		calendarId.setSelectValues(getCalendarIdList());
		calendarId.setValue("");

		Object validFlgValues[][] = {
				{ "", ValidMessage.STRING_VALID, ValidMessage.STRING_INVALID},
				{ "", ValidMessage.STRING_VALID, ValidMessage.STRING_INVALID}};
		validFlg.setSelectValues(validFlgValues);
		validFlg.setValue(ValidMessage.STRING_VALID);

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
		jobkickId.setModify(PropertyDefineConstant.MODIFY_OK);
		jobkickName.setModify(PropertyDefineConstant.MODIFY_OK);
		jobkickType.setModify(PropertyDefineConstant.MODIFY_OK);
		jobunitId.setModify(PropertyDefineConstant.MODIFY_OK);
		jobId.setModify(PropertyDefineConstant.MODIFY_OK);
		calendarId.setModify(PropertyDefineConstant.MODIFY_OK);
		validFlg.setModify(PropertyDefineConstant.MODIFY_OK);
		regUser.setModify(PropertyDefineConstant.MODIFY_OK);
		regDate.setModify(PropertyDefineConstant.MODIFY_NG);
		updateUser.setModify(PropertyDefineConstant.MODIFY_OK);
		updateDate.setModify(PropertyDefineConstant.MODIFY_NG);

		regFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		regToDate.setModify(PropertyDefineConstant.MODIFY_OK);
		updateFromDate.setModify(PropertyDefineConstant.MODIFY_OK);
		updateToDate.setModify(PropertyDefineConstant.MODIFY_OK);

		ownerRoleId.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(manager);
		property.addChildren(jobkickId);
		property.addChildren(jobkickName);
		property.addChildren(jobkickType);
		property.addChildren(jobunitId);
		property.addChildren(jobId);
		property.addChildren(calendarId);
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

	/**
	 * カレンダIDのリストを配列で返却する
	 * 
	 * @return カレンダIDのリスト
	 */
	private static Object[][] getCalendarIdList() {

		List<CalendarInfoResponse> calList = new ArrayList<CalendarInfoResponse>();
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
		} catch (HinemosUnknown e) {
			m_log.warn("getCalendarIdList(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch ( RestConnectFailed e) {
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
