/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.scheduler;

import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.bean.Schedule;
import com.clustercontrol.jobmanagement.bean.SessionPremakeScheduleType;

public class QuartzUtil {
	/**
	 * スケジュール情報を基にcron文字列を作成します。
	 * メンテナンス機能用
	 * 
	 * @param schedule スケジュール
	 * 
	 * @see com.clustercontrol.bean.ScheduleConstant
	 */
	public static String getCronString(Schedule schedule){
		//cron形式でスケジュールを作成する
		StringBuffer cron = new StringBuffer();

		int second = 0;
		if (schedule.getType() == ScheduleConstant.TYPE_DAY) {
			if (schedule.getMinute() == null) {

			} else if (schedule.getHour() == null){
				cron.append(second);
				cron.append(" ");
				cron.append(schedule.getMinute());
				cron.append(" * * * ? *");
			} else if (schedule.getDay() == null) {
				cron.append(second);
				cron.append(" ");
				cron.append(schedule.getMinute());
				cron.append(" ");
				cron.append(schedule.getHour());
				cron.append(" * * ? *");
			} else if (schedule.getMonth() == null) {
				cron.append(second);
				cron.append(" ");
				cron.append(schedule.getMinute());
				cron.append(" ");
				cron.append(schedule.getHour());
				cron.append(" ");
				cron.append(schedule.getDay());
				cron.append(" * ? *");
			} else {
				cron.append(second);
				cron.append(" ");
				cron.append(schedule.getMinute());
				cron.append(" ");
				cron.append(schedule.getHour());
				cron.append(" ");
				cron.append(schedule.getDay());
				cron.append(" ");
				cron.append(schedule.getMonth());
				cron.append(" ? *");
			}
		} else {
			cron.append(second);
			cron.append(" ");
			cron.append(schedule.getMinute());
			cron.append(" ");
			if (schedule.getHour() == null) {
				cron.append("*");
			} else {
				cron.append(schedule.getHour());
			}
			cron.append(" ? * ");
			cron.append(schedule.getWeek());
			cron.append(" *");
		}
		return cron.toString();
	}
	/**
	 * ジョブ[実行契機]のスケジュール情報をCron表記に変換する
	 * @param type
	 * @param month
	 * @param day
	 * @param week
	 * @param hour
	 * @param minute
	 * @param fromXminutes
	 * @param everyXminutes
	 * @return
	 */
	public static String getCronString(int type, Integer week,
			Integer hour, Integer minute, Integer fromXminutes, Integer everyXminutes){

		/**
		 * cronは48時対応していない
		 * 以下対応フォーマット
		 * －秒		0-59			, - * /
		 * －分		0-59			, - * /
		 * －時		0-23			, - * /
		 * －日		1-31			, - * / ? L W
		 * －月		1-12 or JAN - DEC	, - * /
		 * －曜日	1-7 or SUN - SAT	, - * / ? L #
		 * －(年)	empty,1970 - 2099	, - * /
		 * 
		 * 「時」には、24以上が入りえないため、次の処理を行う
		 * ・23時以下になるまで元の数値から24を引く
		 * ・スケジュールの設定が曜日で上記の処理が行われた場合、曜日を進める
		 */
		if(hour != null){
			while(hour >= 24){
				hour= hour - 24;
				if(type == ScheduleConstant.TYPE_WEEK){
					if(week >= 7){
						week = 1;
					}
					else {
						week++;
					}
				}
			}
		}
		//cron形式でスケジュールを作成する
		StringBuffer cron = new StringBuffer();

		int second = 0;
		if (type == ScheduleConstant.TYPE_DAY) {
			if (minute == null) {

			} else if (hour == null){
				cron.append(second);
				cron.append(" ");
				cron.append(minute);
				cron.append(" * * * ? *");
			} else  {
				cron.append(second);
				cron.append(" ");
				cron.append(minute);
				cron.append(" ");
				cron.append(hour);
				cron.append(" * * ? *");
			}
		} else if(type == ScheduleConstant.TYPE_WEEK){
			cron.append(second);
			cron.append(" ");
			cron.append(minute);
			cron.append(" ");
			if (hour == null) {
				cron.append("*");
			} else {
				cron.append(hour);
			}
			cron.append(" ? * ");
			cron.append(week);
			cron.append(" *");
		}
		//type == ScheduleConstant.TYPE_REPEAT
		else {
			if (fromXminutes == null && everyXminutes == null){
			}
			else {
				cron.append(second);
				cron.append(" ");
				cron.append(fromXminutes + "/" + everyXminutes);
				cron.append(" * * * ? *");
			}
		}
		return cron.toString();
	}

	/**
	 * ジョブセッション事前生成のスケジュール情報をCron表記に変換する
	 * @param type
	 * @param month
	 * @param day
	 * @param week
	 * @param hour
	 * @param minute
	 * @param fromXminutes
	 * @param everyXminutes
	 * @return
	 */
	public static String getCronStringJobpremake(int sessionPremakeScheduleType, Integer week,
			Integer hour, Integer minute, Integer everyXHour){

		/**
		 * cronは48時対応していない
		 * 以下対応フォーマット
		 * －秒		0-59			, - * /
		 * －分		0-59			, - * /
		 * －時		0-23			, - * /
		 * －日		1-31			, - * / ? L W
		 * －月		1-12 or JAN - DEC	, - * /
		 * －曜日	1-7 or SUN - SAT	, - * / ? L #
		 * －(年)	empty,1970 - 2099	, - * /
		 * 
		 * 「時」には、24以上が入りえないため、次の処理を行う
		 * ・23時以下になるまで元の数値から24を引く
		 * ・スケジュールの設定が曜日で上記の処理が行われた場合、曜日を進める
		 */
		if(hour != null){
			while(hour >= 24){
				hour= hour - 24;
				if(sessionPremakeScheduleType == SessionPremakeScheduleType.TYPE_EVERY_WEEK){
					if(week >= 7){
						week = 1;
					}
					else {
						week++;
					}
				}
			}
		}
		//cron形式でスケジュールを作成する
		StringBuffer cron = new StringBuffer();

		int second = 0;
		if(sessionPremakeScheduleType == SessionPremakeScheduleType.TYPE_EVERY_DAY){
			cron.append(second);
			cron.append(" ");
			cron.append(minute);
			cron.append(" ");
			cron.append(hour);
			cron.append(" * * ? *");
		} else if(sessionPremakeScheduleType == SessionPremakeScheduleType.TYPE_EVERY_WEEK){
			cron.append(second);
			cron.append(" ");
			cron.append(minute);
			cron.append(" ");
			cron.append(hour);
			cron.append(" ? * ");
			cron.append(week);
			cron.append(" *");
		} else if(sessionPremakeScheduleType == SessionPremakeScheduleType.TYPE_TIME){
			Integer startHour = hour % everyXHour;
			cron.append(second);
			cron.append(" ");
			cron.append(minute);
			cron.append(" ");
			cron.append(startHour + "-23/" + everyXHour);
			cron.append(" * * ? *");
		}
		return cron.toString();
	}
}
