/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.commons;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.clustercontrol.util.Messages;

/**
 * ログ検索の期間を設定するクラス
 * ここに期間のcalendarを設定するだけで追加される。
 */
public class LogSearchPeriodConstants {
	public static final String ALL = Messages.getString("view.hub.log.search.condition.period.all");
	public static final String TODAY = Messages.getString("view.hub.log.search.condition.period.today");
	public static final String YESTERDAY = Messages.getString("view.hub.log.search.condition.period.yesterday");
	public static final String LAST_WEEK = Messages.getString("view.hub.log.search.condition.period.lastWeek");
	public static final String LAST_MONTH = Messages.getString("view.hub.log.search.condition.period.lastMonth");
	public static final String PAST_7_DAYS = Messages.getString("view.hub.log.search.condition.period.past7days");
	public static final String PAST_30_DAYS = Messages.getString("view.hub.log.search.condition.period.past30days");

	/**
	 * 指定された文字列を条件に、期間のfrom と to を返す。
	 * List<Calendar>.get(0) に  from
	 * List<Calendar>.get(1) に to
	 * @param str
	 * @return
	 */
	public static List<Calendar> getPeriod(String str){
		if (str.equals(ALL)) return null;
		else if (str.equals(TODAY)) return today();
		else if (str.equals(YESTERDAY)) return yesterday();
		else if (str.equals(LAST_WEEK)) return lastWeek();
		else if (str.equals(LAST_MONTH)) return lastMonth();
		else if (str.equals(PAST_7_DAYS)) return past7days();
		else if (str.equals(PAST_30_DAYS)) return past30days();
		else return null;
	}

	/**
	 * プルダウン表示用のlistを返す
	 * @return
	 */
	public static List<String> getPeriodStrList(){
		List<String> list = new ArrayList<String>();
		list.add(ALL);
		list.add(TODAY);
		list.add(YESTERDAY);
		list.add(LAST_WEEK);
		list.add(LAST_MONTH);
		list.add(PAST_7_DAYS);
		list.add(PAST_30_DAYS);
		return list;
	}

	/**
	 * from の時刻を、00:00:00
	 * to の時刻を、23:59:59
	 * 
	 * @param from
	 * @param to
	 */
	private static void setHMS(Calendar from, Calendar to){
		from.set(Calendar.HOUR_OF_DAY, 0);
		from.set(Calendar.MINUTE, 0);
		from.set(Calendar.SECOND, 0);
		to.set(Calendar.HOUR_OF_DAY, 23);
		to.set(Calendar.MINUTE, 59);
		to.set(Calendar.SECOND, 59);
	}

	/**
	 * 今日
	 * @return
	 */
	private static List<Calendar> today(){
		List<Calendar> list = new ArrayList<Calendar>();
		
		//今日
		Calendar from = Calendar.getInstance();
		from.add(Calendar.DATE, 0);

		Calendar to = Calendar.getInstance();
		setHMS(from, to);
		
		list.add(from);
		list.add(to);
		
		return list;
	}

	/**
	 * 昨日
	 * @return
	 */
	private static List<Calendar> yesterday(){
		List<Calendar> list = new ArrayList<Calendar>();
		
		//昨日
		Calendar from = Calendar.getInstance();
		from.add(Calendar.DATE, -1);
		
		Calendar to = Calendar.getInstance();
		to.add(Calendar.DATE, -1);
		
		setHMS(from, to);
		
		list.add(from);
		list.add(to);
		
		return list;
	}

	/**
	 * 先週
	 * @return
	 */
	private static List<Calendar> lastWeek(){
		List<Calendar> list = new ArrayList<Calendar>();
		Calendar now = Calendar.getInstance();
		
		//前週
		int diff = (now.get(Calendar.DAY_OF_WEEK) + 6) % 7 + 7;
		
		Calendar from = Calendar.getInstance();
		from.add(Calendar.DATE, -diff);
		
		Calendar to = Calendar.getInstance();
		to.setTime(from.getTime());
		to.add(Calendar.DATE, 6);
		
		setHMS(from, to);
		
		list.add(from);
		list.add(to);
		
		return list;
	}

	/**
	 * 先月
	 * @return
	 */
	private static List<Calendar> lastMonth(){
		List<Calendar> list = new ArrayList<Calendar>();
		
		Calendar now = Calendar.getInstance();

		//先月
		int diff = (now.get(Calendar.DATE)) - 1;
		
		Calendar from = Calendar.getInstance();
		from.setTime(now.getTime());
		from.add(Calendar.MONTH, -1);
		from.add(Calendar.DATE, - diff);
		
		Calendar to = Calendar.getInstance();
		to.setTime(from.getTime());
		to.add(Calendar.MONTH, 1);
		to.add(Calendar.DATE, -1);
		setHMS(from, to);
		
		list.add(from);
		list.add(to);
		
		return list;
	}
	/**
	 * 過去7日間
	 * @return
	 */
	private static List<Calendar> past7days(){
		List<Calendar> list = new ArrayList<Calendar>();
		
		//過去7日間
		Calendar from = Calendar.getInstance();
		from.add(Calendar.DATE, -7);
		
		Calendar to = Calendar.getInstance();
		to.add(Calendar.DATE, -1);
		setHMS(from, to);
		
		list.add(from);
		list.add(to);
		
		return list;
	}

	/**
	 * 過去30日間
	 * @return
	 */
	private static List<Calendar> past30days(){
		List<Calendar> list = new ArrayList<Calendar>();
		
		//過去30日
		Calendar from = Calendar.getInstance();
		from.add(Calendar.DATE, -30);
		
		Calendar to = Calendar.getInstance();
		to.add(Calendar.DATE, -1);
		
		setHMS(from, to);
		
		list.add(from);
		list.add(to);
		
		return list;
	}
}
