/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.jobmanagement.factory;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.calendar.factory.SelectCalendar;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.util.CalendarUtil;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.util.HinemosTime;


/**
 * ジョブ[スケジュール予定]を作成するクラス
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobPlanSchedule implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7702306326951556363L;

	private static Log m_log = LogFactory.getLog( JobPlanSchedule.class );
	/**
	 * 0=数字,1=*,2=?
	 * minuteTypeのみ、3=p/q ：p分からq分毎に
	 * 2はday,weekのみ入る可能性あり。
	 * dayかweekの片方だけ?となる。(制約条件)
	 */
	private int secondType = 1;
	private int minuteType = 1;
	private int hourType = 1;
	private int dayType = 1;
	private int monthType = 1;
	private int weekType = 1;

	private String calendarId = null;

	/**
	 * 値が-1のままならば、エラー
	 */
	private int second = -1;
	private int minute = -1;
	private int hour = -1;
	private int day = -1;
	private int month = -1;
	private int week = -1; // cronの値
	private int weekCalendar = -1; // Calendarクラスの値。(cronとは異なる。)
	private int year = -1;

	private Long startTime = 0L;

	private int fromMinutes = 0;
	private int everyMinutes = 0;

	private boolean firstFlg = false;

	/**
	 * 
	 * @param plan
	 */
	public JobPlanSchedule (String plan,Long startTime, String calendarId) {
		this.startTime = startTime;
		setThisTime(startTime);
		createPlan(plan);
		this.calendarId = calendarId;
		this.firstFlg = true;
	}
	/**
	 * 現時刻から未来のスケジュール予定をCronから求める
	 * 
	 * @param cron
	 */
	private void createPlan(String cron){

		/*
		 * cron定義例
		 * 秒 分 時 日 月 曜 年
		 */

		// * 0 15 * * ? *			毎日15時00分
		// * 0 23 ? * 1 *			毎週日曜日23時00分
		// * */10 * * * ? * 		毎時10分ごと

		String[] planSplit = cron.split(" ");
		String aster = "*";
		String question = "?";
		String slash = "/";
		if (!aster.equals(planSplit[0])) {
			secondType = 0;
			second = Integer.parseInt(planSplit[0]);
		}
		if (!aster.equals(planSplit[1])) {
			String[] slashSplit = planSplit[1].split(slash);
			int num = 0;
			for(String str : slashSplit){
				m_log.trace("slashSplit[ "+ num + " ] =" + str);
				num++;
			}
			//p分からq分毎に繰り返し実行の場合
			if(slashSplit.length == 2){
				minuteType = 3;
				minute =Integer.parseInt(slashSplit[0]);
				fromMinutes = Integer.parseInt(slashSplit[0]);
				everyMinutes = Integer.parseInt(slashSplit[1]);
			}else {
				minuteType = 0;
				minute = Integer.parseInt(planSplit[1]);
				m_log.debug("minuteType=" + minuteType);
				m_log.debug("minute=" + minute);
			}
		}
		if (!aster.equals(planSplit[2])) {
			hourType = 0;
			hour = Integer.parseInt(planSplit[2]);
		}
		if (question.equals(planSplit[3])) {
			dayType = 2;
		} else if (!aster.equals(planSplit[3])) {
			dayType = 0;
			day = Integer.parseInt(planSplit[3]);
		}
		if (!aster.equals(planSplit[4])) {
			monthType = 0;
			month = Integer.parseInt(planSplit[4]);
		}
		if (question.equals(planSplit[5])) {
			weekType = 2;
		} else if (!aster.equals(planSplit[5])) {
			weekType = 0;
			week = Integer.parseInt(planSplit[5]);
		}
		switch (week) {
		case 1:
			weekCalendar = Calendar.SUNDAY;
			break;
		case 2:
			weekCalendar = Calendar.MONDAY;
			break;
		case 3:
			weekCalendar = Calendar.TUESDAY;
			break;
		case 4:
			weekCalendar = Calendar.WEDNESDAY;
			break;
		case 5:
			weekCalendar = Calendar.THURSDAY;
			break;
		case 6:
			weekCalendar = Calendar.FRIDAY;
			break;
		case 7:
			weekCalendar = Calendar.SATURDAY;
			break;
		default:
			weekCalendar = Calendar.SUNDAY;
		}
		// weekTypeが0（つまり特定の曜日固定）の場合、今日が該当の曜日かチェック。
		// 違えば直近の次の該当日を探した上で、時刻が「*」指定の場合は時刻を0に戻す 
		if (weekType == 0 && isFireDayOfWeek() == false) {
			nextFireDay();
			if (hourType == 1) {
				hour = 0;
			}
		}
	}

	/**
	 * 次のスケジュール予定を返す。
	 * MAX_YEARまで進んでもスケジュール予定が見つからない場合は、nullを返す。
	 * 
	 * @param calendarId
	 * @return
	 */
	public Long getNextPlan() {
		// 初回にnext()を実行させないようにするため、firstFlgを設定。
		while (firstFlg || next()) {
			// 初回処理時にfirstFlgをfalseにする。
			if(firstFlg) {
				firstFlg = false;
			}

			SelectCalendar select = new SelectCalendar();
			m_log.debug(year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			sdf.setTimeZone(HinemosTime.getTimeZone());
			Date date = null;
			try {
				date = sdf.parse(year + "/" + month + "/" + day + " " + hour + ":" + minute + ":" + second);
			} catch (ParseException e1) {
				m_log.warn("ParseException : " + e1.getMessage(), e1);
				return null;
			}
			m_log.debug("Date : " + date);

			if(calendarId == null){
				if(date.getTime() > startTime){
					return date.getTime();
				}
			} else {
				CalendarInfo calInfo = null;
				try {
					calInfo = select.getCalendarFull(calendarId);
					if (calInfo.getValidTimeTo() < date.getTime()) {
						return null;
					}
					if(CalendarUtil.isRun(calInfo, date) && isDate(year,month,day)){
						if(date.getTime() > startTime){
							return date.getTime();
						}
					}
				} catch (CalendarNotFound e) {
					m_log.warn("getNextPlan : " + e.getClass().getName() + ", " + e.getMessage());
				} catch (InvalidRole e) {
					m_log.warn("getNextPlan : " + e.getClass().getName() + ", " + e.getMessage());
				}
			}
		}
		return null;
	}
	/**
	 * 月を進めるメソッド
	 * 同じ処理が複数存在したためメソッド化
	 * 
	 */
	private void nextMonth(){
		month ++;
		if (month > 12) {
			month = 1;
			year++;
		}
	}

	/**
	 * Cron定義をもとに
	 * 日、月、年を進める
	 */
	private boolean next() {
		int maxYear = HinemosPropertyUtil.getHinemosPropertyNum("job.schedule.plan.max", Long.valueOf(2030)).intValue();
		if (maxYear < year) {
			return false;
		}
		/**
		 * Cron定義を元に
		 * 時、分、秒を進める
		 */
		if (secondType == 1) {
			second ++;
			if (second < 60) {
				return true;
			}
			second = 0;
		}
		if (minuteType == 1) {
			minute ++;
			if (minute < 60) {
				return true;
			}
			minute = 0;
		}
		//p分からq分毎に繰り返し実行
		if (minuteType == 3) {
			minute = minute + everyMinutes;
			if (minute < 60) {
				return true;
			}
			minute = fromMinutes;
		}
		if (hourType == 1) {
			hour ++;
			if (hour < 24) {
				return true;
			}
			hour = 0;
		}
		return nextFireDay();
	}
	
	/**
	 * 現在保持している年月日の次にキックすべき日に移動する
	 * @return 通常はtrue。CRON定義が異常な場合のみfalse
	 */
	private boolean nextFireDay() {
		/**
		 * weekTypeが?の場合、
		 * dayTypeは、0 か 1
		 */
		if(weekType == 2){
			//dayTypeが 1 の場合、1日周期
			if(dayType == 1){
				day++;
				if(!isDate(year, month, day)){
					day = 1;
					nextMonth();
				}
			}
			//dayTypeが 0 の場合、day固定
			if(dayType == 0){
				if(monthType == 1){
					nextMonth();
				}
				else {
					year++;
				}
			}
			if (isDate(year, month, day)) {
				return true;
			}
		}
		/**
		 * dayTypeが?の場合
		 */
		else if (dayType == 2) {
			while (true) {
				day ++;
				if (!isDate(year, month, day)) {
					day = 1;
					if (monthType == 1) {
						nextMonth();
					} else {
						year ++;
					}
				}
				if (isFireDayOfWeek() == true) {
					break;
				}
			}
		} else {
			m_log.error("Cron Definition ERROR.");
			return false;
		}
		return true;
	}
	
	/**
	 * 現在保持している年月日が、キックすべき曜日か否かをチェックする関数（曜日指定の場合に使用）
	 * @return キックすべき日であればtrue そうでなければfalse
	 */
	private boolean isFireDayOfWeek() {
		Calendar calendar = HinemosTime.getCalendarInstance();
		calendar.set(year, month - 1, day);
		if (calendar.get(Calendar.DAY_OF_WEEK) == weekCalendar) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 存在する日付であるかチェック。
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	private boolean isDate(int year, int month, int day) {
		boolean ret = false;
		Calendar cal = HinemosTime.getCalendarInstance();
		cal.setLenient( true );
		cal.set(year, month - 1, day);
		ret = cal.get(Calendar.MONTH) == (month - 1) % 12;
		m_log.trace("year=" + year + ",month=" + month + ",day=" + day + ",ret=" + ret);
		return ret;
	}

	/**
	 * 現在の日時をセット
	 * ジョブ[スケジュール予定]ビュー一覧には、
	 * この日時以降のスケジュールが予定が表示される
	 * @param now
	 */
	private void setThisTime(Long now){
		Date date = new Date(now);
		m_log.trace("date= " + date);
		SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
		sdfYear.setTimeZone(HinemosTime.getTimeZone());
		String strYear = sdfYear.format(date);
		this.year = Integer.parseInt(strYear);

		SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
		sdfMonth.setTimeZone(HinemosTime.getTimeZone());
		String strMonth = sdfMonth.format(date);
		this.month = Integer.parseInt(strMonth);

		SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
		sdfDay.setTimeZone(HinemosTime.getTimeZone());
		String strDay = sdfDay.format(date);
		this.day = Integer.parseInt(strDay);

		SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
		sdfHour.setTimeZone(HinemosTime.getTimeZone());
		String strHour = sdfHour.format(date);
		this.hour = Integer.parseInt(strHour);

		SimpleDateFormat sdfMinute = new SimpleDateFormat("mm");
		sdfMinute.setTimeZone(HinemosTime.getTimeZone());
		String strMinute = sdfMinute.format(date);
		this.minute = Integer.parseInt(strMinute);
		m_log.trace("year=" + year + ",month=" + month + ",day=" + day);
	}
}
