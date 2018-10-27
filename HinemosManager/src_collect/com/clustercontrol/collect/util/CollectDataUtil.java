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

package com.clustercontrol.collect.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.bean.PerfData;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectDataPK;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JdbcBatchExecutor;
import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.HinemosTime;

/**
 * 性能情報を登録するユーティティクラス<BR>
 *
 * @version 5.1.0
 * @since 5.1.0
 */
public class CollectDataUtil {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(CollectDataUtil.class);

	/** エポック秒を時間に直す際使用するタイムゾーン **/
	private static final long TIMEZONE = HinemosTime.getTimeZoneOffset();

	private static Integer maxId = null;
	private static Object maxLock = new Object();
	
	private static Integer getId(String itemName, String displayName, String monitorId, String facilityId, JpaTransactionManager jtm) {
		CollectKeyInfo collectKeyInfo = null;
		try {
			// collectKeyInfo(のcollectorid)を取ってくることができるか確認
			collectKeyInfo= QueryUtil.getCollectKeyPK(new CollectKeyInfoPK(itemName, displayName, monitorId, facilityId));
			if(collectKeyInfo == null){
				throw new CollectKeyNotFound();
			}
			return collectKeyInfo.getCollectorid();
		} catch (CollectKeyNotFound e) {
			
			// collecoridが存在しなかった場合は新たに作る
			synchronized(maxLock) {
				if (maxId == null) {
					maxId = QueryUtil.getMaxId();
					if (maxId == null) {
						maxId = -1;
					}
				}
				maxId++;
				try {
					HinemosEntityManager em = jtm.getEntityManager();
					collectKeyInfo = new CollectKeyInfo(itemName, displayName, monitorId, facilityId, maxId);
					em.persist(collectKeyInfo);
					em.flush();
					return collectKeyInfo.getCollectorid();
				} catch (Exception e1) {
					m_log.warn("put() : " + e1.getClass().getSimpleName() + ", " + e1.getMessage(), e1);
					if (jtm != null) {
						jtm.rollback();
					}
				}
			}
		}
		m_log.warn("getId : error");
		return null;
	}

	/**
	 * 性能情報を登録するために Queue に put する
	 *
	 * @param sample
	 *            性能情報
	 * @throws HinemosUnknown
	 * @throws Exception
	 */
	public static void put(List<Sample> sampleList) {
		m_log.debug("put() start");

		List<CollectData> collectdata_entities = new ArrayList<CollectData>();
		List<SummaryHour> summaryhour_entities = new ArrayList<SummaryHour>();
		List<SummaryDay> summaryday_entities = new ArrayList<SummaryDay>();
		List<SummaryMonth> summarymonth_entities = new ArrayList<SummaryMonth>();
		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.begin();

		for (Sample sample : sampleList) {
			// for debug
			if (m_log.isDebugEnabled()) {
				m_log.debug("put() dateTime = " + sample.getDateTime());
				ArrayList<PerfData> list = sample.getPerfDataList();
				for (PerfData data : list) {
					m_log.info("put() list facilityId = " + data.getFacilityId() + ", value = " + data.getValue());
				}
			}
			
			ArrayList<PerfData> list = sample.getPerfDataList();
			String monitorId = sample.getMonitorId();
			
			Long time = HinemosTime.currentTimeMillis();
			if (sample.getDateTime() != null) {
				time = sample.getDateTime().getTime();
			}
			for (PerfData data : list) {
				m_log.debug("persist itemCode = " + data.getItemName());
				String itemName = data.getItemName();
				String facilityId = data.getFacilityId();
				String displayName = data.getDisplayName();
				CollectData collectData = null;
				SummaryHour summaryHour_c = null;
				SummaryDay summaryDay_c = null;
				SummaryMonth summaryMonth_c = null;

				Integer collectorid = getId(itemName, displayName, monitorId, facilityId, jtm);
				CollectDataPK pk = new CollectDataPK(collectorid, time);
				Float value = null;
				if(data.getValue() != null){
					value = Float.parseFloat(data.getValue().toString());
				}
				collectData = new CollectData(pk, value);
				collectdata_entities.add(collectData);

				// Summary関連の計算を行う
				// エポック秒を時間で切り捨てて再計算
				Long hour = (time + TIMEZONE) / 1000 / 3600 * 3600 * 1000 - TIMEZONE;
				// エポック秒を日単位で切り捨てて計算
				Long day = (time + TIMEZONE) / 1000 / 3600 / 24 * 24 * 3600 * 1000 - TIMEZONE;
				// エポック秒から年月を取得
				Calendar calendar = HinemosTime.getCalendarInstance();
				calendar.setTimeInMillis(time);
				int y = calendar.get(Calendar.YEAR);
				int m = calendar.get(Calendar.MONTH);
				calendar.clear();
				calendar.set(y, m, 1);
				Long month = calendar.getTimeInMillis();
				
				// SummaryHour
				CollectDataPK pk_h = new CollectDataPK(collectorid, hour);
				try {
					SummaryHour summaryHour = QueryUtil.getSummaryHour(pk_h);
					summaryHour_c = summaryHour.clone();
					Integer h_count = summaryHour_c.getCount();
					if(value != null && !Float.isNaN(value)){
						summaryHour_c.setAvg((value + summaryHour_c.getAvg() * h_count) / (h_count + 1));
						summaryHour_c.setMin(summaryHour_c.getMin() < value ? summaryHour_c.getMin() : value);
						summaryHour_c.setMax(summaryHour_c.getMax() > value ? summaryHour_c.getMax() : value);
						summaryHour_c.setCount(h_count + 1);
					}
				} catch (CollectKeyNotFound e) {
					if(value != null && !Float.isNaN(value)) {
						summaryHour_c = new SummaryHour(pk_h, value, value, value, 1);
					}
				}

				// SummaryDay
				CollectDataPK pk_d = new CollectDataPK(collectorid, day);
				try {
					SummaryDay summaryDay = QueryUtil.getSummaryDay(pk_d);
					summaryDay_c = summaryDay.clone();
					Integer d_count = summaryDay_c.getCount();
					if(value != null && !Float.isNaN(value)){
						summaryDay_c.setAvg((value + summaryDay_c.getAvg() * d_count) / (d_count + 1));
						summaryDay_c.setMin(summaryDay_c.getMin() < value ? summaryDay_c.getMin() : value);
						summaryDay_c.setMax(summaryDay_c.getMax() > value ? summaryDay_c.getMax() : value);
						summaryDay_c.setCount(d_count + 1);
					}
				} catch (CollectKeyNotFound e) {
					if(value != null && !Float.isNaN(value)) {
						summaryDay_c = new SummaryDay(pk_d, value, value, value, 1);
					}
				}

				// SummaryMonth
				CollectDataPK pk_m = new CollectDataPK(collectorid, month);
				try {
					SummaryMonth summaryMonth = QueryUtil.getSummaryMonth(pk_m);
					summaryMonth_c = summaryMonth.clone();
					Integer m_count = summaryMonth_c.getCount();
					if(value != null && !Float.isNaN(value)){
						summaryMonth_c.setAvg((value + summaryMonth_c.getAvg() * m_count) / (m_count + 1));
						summaryMonth_c.setMin(summaryMonth_c.getMin() < value ? summaryMonth_c.getMin() : value);
						summaryMonth_c.setMax(summaryMonth_c.getMax() > value ? summaryMonth_c.getMax() : value);
						summaryMonth_c.setCount(m_count + 1);
					}
				} catch (CollectKeyNotFound e) {
					if(value != null && !Float.isNaN(value)) {
						summaryMonth_c = new SummaryMonth(pk_m, value, value, value, 1);
					}
				}

				// 各Summaryについて、追加するデータの精査を行う
				if (value != null && !Float.isNaN(value)) {
					summaryhour_entities.add(summaryHour_c);
					summaryday_entities.add(summaryDay_c);
					summarymonth_entities.add(summaryMonth_c);
				}
			}
			m_log.debug(
					"insert() end : dateTime = " + sample.getDateTime());
		}
		jtm.commit();
		List<JdbcBatchQuery> query = new ArrayList<JdbcBatchQuery>();
		// データを更新(挿入)する
		if(!collectdata_entities.isEmpty()){
			query.add(new CollectDataJdbcBatchInsert(collectdata_entities));
		}
		if(!summaryhour_entities.isEmpty()){
			query.add(new SummaryHourJdbcBatchUpsert(summaryhour_entities));
		}
		if(!summaryday_entities.isEmpty()){
			query.add(new SummaryDayJdbcBatchUpsert(summaryday_entities));
		}
		if(!summarymonth_entities.isEmpty()){
			query.add(new SummaryMonthJdbcBatchUpsert(summarymonth_entities));
		}
		JdbcBatchExecutor.execute(query);
		jtm.close();
		m_log.debug("put() end");
	}
}