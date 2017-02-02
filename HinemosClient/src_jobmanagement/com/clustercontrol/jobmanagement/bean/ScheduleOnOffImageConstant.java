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

package com.clustercontrol.jobmanagement.bean;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.TimezoneUtil;

/**
 * 予定イメージの定数クラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ScheduleOnOffImageConstant {
	private static Image past = null;

	private static Image now = null;

	private static Image future = null;

	/**
	 * 日付からImageに変換する
	 *
	 * @param date
	 * @return
	 */
	public static Image dateToImage(Date date) {
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();

		if (date != null) {

			Calendar workCalendar = Calendar.getInstance(TimezoneUtil.getTimeZone());
			workCalendar.setTime(date);
			workCalendar.set(Calendar.HOUR_OF_DAY, 0);
			workCalendar.set(Calendar.MINUTE, 0);
			workCalendar.set(Calendar.SECOND, 0);
			workCalendar.set(Calendar.MILLISECOND, 0);
			date = workCalendar.getTime();

			workCalendar.setTime(new Date());
			workCalendar.set(Calendar.HOUR_OF_DAY, 0);
			workCalendar.set(Calendar.MINUTE, 0);
			workCalendar.set(Calendar.SECOND, 0);
			workCalendar.set(Calendar.MILLISECOND, 0);
			Date checkDate = workCalendar.getTime();

			if (date.equals(checkDate)){
				if (now == null)
					now = registry.getDescriptor(
							ClusterControlPlugin.IMG_SCHEDULE_NOW)
							.createImage();
				return now;
			}
			else if (date.after(checkDate)){
				if (future == null)
					future = registry.getDescriptor(
							ClusterControlPlugin.IMG_SCHEDULE_FUTURE)
							.createImage();
				return future;
			}
			else if (date.before(checkDate)){
				if (past == null)
					past = registry.getDescriptor(
							ClusterControlPlugin.IMG_SCHEDULE_PAST)
							.createImage();
				return past;
			}
			else{
				return null;
			}
		} else {
			return null;
		}
	}
}
