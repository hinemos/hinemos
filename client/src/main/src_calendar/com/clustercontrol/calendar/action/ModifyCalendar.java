/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.calendar.action;

import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.calendar.util.CalendarEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.calendar.CalendarInfo;
import com.clustercontrol.ws.calendar.CalendarPatternInfo;
import com.clustercontrol.ws.calendar.InvalidRole_Exception;

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
	public boolean modify(String managerName, CalendarInfo info){

		boolean result = false;
		String[] args = { info.getCalendarId() };
		try {
			CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(managerName);
			wrapper.modifyCalendar(info);
			result = true;
			args[0] = managerName;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.calendar.3", args));

		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
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
	public boolean modifyPatternInfo(String managerName, CalendarPatternInfo info){
		boolean result = false;
		String[] args = { info.getCalPatternId() };
		try {
			CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(managerName);
			wrapper.modifyCalendarPattern(info);
			result = true;
			args[0] = managerName;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.calendar.36", args));

		} catch (Exception e) {
			String errMessage = "";
			if (e instanceof InvalidRole_Exception) {
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
