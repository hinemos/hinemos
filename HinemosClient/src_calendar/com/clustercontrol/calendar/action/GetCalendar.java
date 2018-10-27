/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.calendar.util.CalendarEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.calendar.CalendarInfo;
import com.clustercontrol.ws.calendar.CalendarPatternInfo;

/**
 * カレンダ情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 2.1.0
 * @since 2.0.0
 */
public class GetCalendar {

	// ログ
	private static Log m_log = LogFactory.getLog( GetCalendar.class );

	/**
	 * カレンダ情報を返します。
	 *
	 * @param managerName マネージャ名
	 * @param calendarId カレンダID
	 * @return カレンダ情報
	 */
	public CalendarInfo getCalendar(String managerName, String calendarId) {

		CalendarInfo info = null;
		try {
			CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(managerName);
			info = wrapper.getCalendar(calendarId);
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getCalendar(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		return info;
	}
	/**
	 * カレンダ[カレンダパターン]情報を返します。<BR>
	 * @param managerName
	 * @param id
	 * @return
	 */
	public CalendarPatternInfo getCalendarPattern(String managerName, String id) {
		CalendarPatternInfo info = null;
		try {
			CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(managerName);
			info = wrapper.getCalendarPattern(id);
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getCalendarPattern(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return info;
	}
}
