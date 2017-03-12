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
import com.clustercontrol.ws.calendar.CalendarDuplicate_Exception;
import com.clustercontrol.ws.calendar.CalendarInfo;
import com.clustercontrol.ws.calendar.CalendarPatternInfo;
import com.clustercontrol.ws.calendar.InvalidRole_Exception;

/**
 * カレンダ情報を登録するクライアント側アクションクラス<BR>
 * カレンダ[一覧]情報
 * カレンダ[カレンダパターン]情報
 *
 * @version 4.1.0
 * @since 2.0.0
 */
public class AddCalendar {

	/**
	 * カレンダ情報を追加します。<BR>
	 *
	 * @param managerName マネージャ名
	 * @param info カレンダ情報
	 * @return 登録に成功した場合、true
	 */
	public boolean add(String managerName, CalendarInfo info) {
		boolean result = false;
		String[] args = { info.getCalendarId() };
		try {
			CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(managerName);
			wrapper.addCalendar(info);
			result = true;
			args[0] = managerName;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.calendar.1", args));

		} catch (CalendarDuplicate_Exception e) {
			// カレンダIDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.calendar.27", args));

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
					Messages.getString("message.calendar.2", args) + errMessage);
		}

		return result;
	}
	/**
	 * カレンダ[カレンダパターン]情報を追加します。<BR>
	 * @param managerName
	 * @param info
	 * @return
	 */
	public boolean addCalendarPatternInfo(String managerName, CalendarPatternInfo info){
		boolean result = false;
		String[] args = { info.getCalPatternId() };
		try {
			CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(managerName);
			wrapper.addCalendarPattern(info);
			result = true;
			args[0] = managerName;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.calendar.34", args));

		} catch (CalendarDuplicate_Exception e) {
			// カレンダパターンIDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.calendar.40", args));

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
					Messages.getString("message.calendar.35", args) + errMessage);
		}
		return result;
	}
}
