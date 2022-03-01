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
import org.openapitools.client.model.CalendarInfoResponse;
import org.openapitools.client.model.CalendarPatternInfoResponse;
import com.clustercontrol.calendar.util.CalendarRestClientWrapper;
import com.clustercontrol.util.Messages;

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
	public CalendarInfoResponse getCalendar(String managerName, String calendarId) {

		CalendarInfoResponse info = null;
		try {
			CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(managerName);
			info = wrapper.getCalendarInfo(calendarId);
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getCalendar(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					e.getMessage());
		}

		return info;
	}
	/**
	 * カレンダ[カレンダパターン]情報を返します。<BR>
	 * @param managerName
	 * @param id
	 * @return
	 */
	public CalendarPatternInfoResponse getCalendarPattern(String managerName, String id) {
		CalendarPatternInfoResponse info = null;
		try {
			CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(managerName);
			info = wrapper.getCalendarPattern(id);
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("getCalendarPattern(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					e.getMessage());
		}
		return info;
	}
}
