/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.hub.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JdbcBatchExecutor;
import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.bean.ValueType;
import com.clustercontrol.hub.model.CollectDataTag;
import com.clustercontrol.hub.model.CollectDataTagPK;
import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.hub.model.CollectStringDataPK;
import com.clustercontrol.hub.model.CollectStringKeyInfo;
import com.clustercontrol.hub.model.CollectStringKeyInfoPK;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 性能情報を登録するユーティティクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class CollectStringDataUtil {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(CollectStringDataUtil.class);

	private static Long maxId = null;
	private static Object maxLock = new Object();

	private static Long getCollectStringKeyInfoPK(String monitorId, String facilityId, String targetName, JpaTransactionManager jtm) {
		CollectStringKeyInfo collectKeyInfo = null;
		try {
			// collectKeyInfo(のcollectorid)を取ってくることができるか確認
			m_log.debug("getCollectStringKeyInfoPK() : " + monitorId + ", " + facilityId + "," + targetName);
			collectKeyInfo= QueryUtil.getCollectStringKeyPK(new CollectStringKeyInfoPK(monitorId, facilityId, targetName));
			if(collectKeyInfo == null){
				throw new CollectKeyNotFound();
			}
			return collectKeyInfo.getCollectId();
		} catch (CollectKeyNotFound e) {
			m_log.debug("getCollectStringKeyInfoPK() : CollectKeyNotFound");
			// collectIdが存在しなかった場合は新たに作る
			synchronized(maxLock) {
				if (maxId == null) {
					maxId = QueryUtil.getMaxId();//文字列収集用のID生成ロジック
					if (maxId == null) {
						maxId = (long) -1;
					}
				}
				maxId++;
				try {
					HinemosEntityManager em = jtm.getEntityManager();
					collectKeyInfo = new CollectStringKeyInfo(monitorId, facilityId, targetName, maxId);
					em.persist(collectKeyInfo);
					em.flush();
					return collectKeyInfo.getCollectId();
				} catch (Exception e1) {
					m_log.warn("store() : " + e1.getClass().getSimpleName() + ", " + e1.getMessage(), e1);
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
	 * 
	 * @param sampleList
	 */
	public static void store(List<StringSample> sampleList) {
		m_log.debug("store() start");

		List<CollectStringData> collectdata_entities = new ArrayList<CollectStringData>();
		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.begin();
		
		Map<String, MonitorInfo> monitorInfoMap = new HashMap<>();
		Map<String, CollectStringDataParser> parserMap = new HashMap<>();
		for (StringSample sample : sampleList) {
			// for debug
			if (m_log.isDebugEnabled()) {
				m_log.debug("store() facilityId = " + sample.getFacilityId() + ", dateTime = " + sample.getDateTime());
				m_log.debug("store() value = " + sample.getValue());
			}

			m_log.debug("persist targetName = " + sample.getTargetName());

			String monitorId = sample.getMonitorId();
			String facilityId = sample.getFacilityId();
			// スキーマの内容を考慮し、512 文字で制限する。
			String targetName = sample.getTargetName().length() > 512 ? sample.getTargetName().substring(0, 512): sample.getTargetName();
			
			CollectStringData collectData = null;
			Long collectId = getCollectStringKeyInfoPK(monitorId, facilityId, targetName, jtm);
			
			Long dataId = StringDataIdGenerator.getNext();
			if (dataId == StringDataIdGenerator.getMax() / 2) {
				AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.HUB_TRANSFER, MessageConstant.MESSAGE_HUB_COLLECT_NUMBERING_OVER_INTERMEDIATE, new String[]{},
						String.format("current=%d, max=%d", dataId, StringDataIdGenerator.getMax()));
			}
			
			CollectStringDataPK pk = new CollectStringDataPK(collectId, dataId);
			
			String value = sample.getValue() != null? sample.getValue() : "";
			collectData = new CollectStringData(pk, HinemosTime.currentTimeMillis(), value);
			
			if (!sample.getTagList().isEmpty()) {
				Map<String, CollectDataTag> tagMap = new HashMap<>();
				for (StringSampleTag tag : sample.getTagList()) {
					tagMap.put(tag.getKey(), new CollectDataTag(new CollectDataTagPK(collectId, dataId, tag.getKey()), tag.getType(), tag.getValue()));
				}
				collectData.getTagList().addAll(tagMap.values());
			}
			
			// タグ抽出
			MonitorSettingControllerBean bean = new MonitorSettingControllerBean();
			try {
				MonitorInfo mi = monitorInfoMap.get(monitorId);
				if (mi == null) {
					mi = bean.getMonitor(monitorId);
					monitorInfoMap.put(monitorId, mi);
				}
				if (mi.getLogFormatId() != null) {
					CollectStringDataParser parser = parserMap.get(mi.getLogFormatId());
					if (parser == null) {
						parser = new CollectStringDataParser(
								new HubControllerBean().getLogFormat(mi.getLogFormatId()));
						parserMap.put(mi.getLogFormatId(), parser);
					}
					parser.parse(collectData);
					
					collectData.setLogformatId(mi.getLogFormatId());
				}
				
				// KEY_TIMESTAMP_IN_LOG があれば、ログの時刻を受信日時から切り替えます。
				CollectDataTag timestamp = null;
				for (CollectDataTag tag: collectData.getTagList()) {
					if (tag.getKey().equals(CollectStringDataParser.KEY_TIMESTAMP_IN_LOG)) {
						timestamp = tag;
						break;
					}
				}
				if (timestamp != null) {
					try {
						Long time = collectData.getTime();
						collectData.setTime(Long.valueOf(timestamp.getValue()));
						collectData.getTagList().add(new CollectDataTag(new CollectDataTagPK(collectData.getCollectId(), collectData.getDataId(), CollectStringDataParser.KEY_TIMESTAMP_RECIEVED), ValueType.number, time.toString()));
					} catch(Exception e) {
						m_log.warn("store() : fail to change to timestamp of log. time=" + timestamp.getValue(), e);
					}
				}
			} catch (MonitorNotFound | HinemosUnknown | InvalidRole e) {
				m_log.warn(String.format("failed to get a MonitorInfo : %s", sample.getMonitorId()));
			}
			
			collectdata_entities.add(collectData);
			
			m_log.debug("store() : " + collectData);
		}
		
		jtm.commit();
		
		List<JdbcBatchQuery> query = new ArrayList<JdbcBatchQuery>();
		// データを更新(挿入)する
		if(!collectdata_entities.isEmpty()){
			query.add(new CollectStringDataJdbcBatchInsert(collectdata_entities));
			for (CollectStringData data : collectdata_entities) {
				query.add(new CollectStringTagJdbcBatchInsert(data.getTagList()));
			}
		}
		JdbcBatchExecutor.execute(query);
		jtm.close();
		m_log.debug("store() end");
	}

	public static void main(String[] args){
		System.out.println("START");
		List<StringSample> list = new ArrayList<>();
		StringSample sample = new StringSample(new Date(HinemosTime.currentTimeMillis()), "MON_SYS_0001");
		sample.set("ver510logdev", "hoge", "<34>Oct 11 22:14:15 mymachine su: 'su root' failed for lonvick on /dev/pts/8");
		list.add(sample);
		store(list);
		System.out.println("END");
	}
}