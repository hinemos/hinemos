/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.CalendarInfoResponse;
import org.openapitools.client.model.CalendarPatternInfoResponse;
import org.openapitools.client.model.ModifyCalendarPatternRequest;
import org.openapitools.client.model.ModifyCalendarRequest;

import com.clustercontrol.calendar.util.CalendarRestClientWrapper;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * カレンダ情報を変更するクライアント側アクションクラス<BR>
 * カレンダ[一覧]情報
 * カレンダ[例外日設定]情報
 *
 * @version 4.1.0
 * @since 2.0.0
 *
 */
public class ModifyCalendar {

	/**
	 * カレンダ情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param info 変更対象のカレンダ情報
	 * @return 変更に成功した場合、<code> true </code>
	 */
	public boolean modify(String managerName, CalendarInfoResponse info ,String calendarId){

		boolean result = false;
		String[] args = { calendarId };
		try {
			CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(managerName);
			ModifyCalendarRequest dto = new ModifyCalendarRequest();
			RestClientBeanUtil.convertBean(info, dto);
			wrapper.modifyCalendar(calendarId, dto);
			result = true;
			args[0] = managerName;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.calendar.3", args));

		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						e.getMessage());
			} else {
				errMessage = ", " + e.getMessage();
			}
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.calendar.4", args) + errMessage);
		}

		return result;
	}
	/**
	 * カレンダ[カレンダパターン]情報を変更します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 * @param managerName
	 * @param info
	 * @return
	 */
	public boolean modifyPatternInfo(String managerName, CalendarPatternInfoResponse info, String  calendarPatternId ){
		boolean result = false;
		String[] args = { calendarPatternId };
		try {
			CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(managerName);
			ModifyCalendarPatternRequest dto = new ModifyCalendarPatternRequest();
			RestClientBeanUtil.convertBean(info, dto);
			wrapper.modifyCalendarPattern(calendarPatternId, dto);
			result = true;
			args[0] = managerName;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.calendar.36", args));

		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole) {
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} else {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.calendar.37", args) + errMessage);
		}
		return result;
	}
}
