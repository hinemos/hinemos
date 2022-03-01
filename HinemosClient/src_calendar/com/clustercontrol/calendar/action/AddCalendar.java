/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.AddCalendarPatternRequest;
import org.openapitools.client.model.AddCalendarRequest;
import org.openapitools.client.model.CalendarInfoResponse;
import org.openapitools.client.model.CalendarPatternInfoResponse;

import com.clustercontrol.calendar.util.CalendarRestClientWrapper;
import com.clustercontrol.fault.CalendarDuplicate;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

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
	public boolean add(String managerName, CalendarInfoResponse info) {
		boolean result = false;
		String[] args = { info.getCalendarId() };
		try {
			CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(managerName);
			AddCalendarRequest dto = new AddCalendarRequest();
			RestClientBeanUtil.convertBean(info ,dto);
			wrapper.addCalendar(dto);
			result = true;
			args[0] = managerName;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.calendar.1", args));

		} catch (CalendarDuplicate e) {
			// カレンダIDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					e.getMessage());

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
	public boolean addCalendarPatternInfo(String managerName, CalendarPatternInfoResponse info){
		boolean result = false;
		String[] args = { info.getCalendarPatternId() };
		try {
			CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(managerName);
			AddCalendarPatternRequest dto = new AddCalendarPatternRequest();
			RestClientBeanUtil.convertBean(info, dto);
			wrapper.addCalendarPattern(dto);
			result = true;
			args[0] = managerName;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.calendar.34", args));

		} catch (CalendarDuplicate e) {
			// カレンダパターンIDが重複している場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					e.getMessage());

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
					Messages.getString("message.calendar.35", args) + errMessage);
		}
		return result;
	}
}
