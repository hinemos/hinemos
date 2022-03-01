/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JdbcBatchExecutor;
import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.bean.StringSampleData;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.model.CollectDataTag;
import com.clustercontrol.hub.model.CollectDataTagPK;
import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.hub.model.CollectStringDataPK;
import com.clustercontrol.hub.util.CollectStringDataJdbcBatchInsert;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.hub.util.CollectStringTagJdbcBatchInsert;
import com.clustercontrol.hub.util.StringDataIdGenerator;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.v1.bean.SdmlControlLogDTO;
import com.clustercontrol.sdml.v1.constant.SdmlCollectStringTag;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * SDML制御ログに関するユーティリティクラス
 *
 */
public class ControlLogUtil {
	private static Log logger = LogFactory.getLog(ControlLogUtil.class);

	/**
	 * SDML制御ログの収集処理
	 * 
	 * @param controlSetting
	 * @param facilityId
	 * @param logDto
	 */
	public static void collectControlLog(SdmlControlSettingInfo controlSetting, String facilityId, SdmlControlLogDTO logDto) {
		if (controlSetting == null || facilityId == null || logDto == null) {
			return;
		}
		logger.debug("collectControlLog() : start."); 
		StringSample stringSample = new StringSample(HinemosTime.getDateInstance(), controlSetting.getApplicationId());

		String targetName = new File(new File(controlSetting.getControlLogDirectory()), controlSetting.getControlLogFilename()).getPath();

		// タグ抽出
		List<StringSampleTag> tags = new ArrayList<>();
		// 制御設定で指定されたファイル名を設定（正規表現が不可であることが前提）
		tags.add(new StringSampleTag(SdmlCollectStringTag.FileName.name(), SdmlCollectStringTag.FileName.valueType(), controlSetting.getControlLogFilename()));
		tags.add(new StringSampleTag(SdmlCollectStringTag.TIMESTAMP_IN_LOG.name(), SdmlCollectStringTag.TIMESTAMP_IN_LOG.valueType(), Long.toString(logDto.getTime())));
		tags.add(new StringSampleTag(SdmlCollectStringTag.Hostname.name(), SdmlCollectStringTag.Hostname.valueType(), logDto.getHostname()));
		tags.add(new StringSampleTag(SdmlCollectStringTag.ApplicationID.name(), SdmlCollectStringTag.ApplicationID.valueType(), logDto.getApplicationId()));
		tags.add(new StringSampleTag(SdmlCollectStringTag.PID.name(), SdmlCollectStringTag.PID.valueType(), logDto.getPid()));
		tags.add(new StringSampleTag(SdmlCollectStringTag.SDMLControlCode.name(), SdmlCollectStringTag.SDMLControlCode.valueType(), logDto.getControlCode()));

		stringSample.set(facilityId, targetName, logDto.getOrgLogLine(), tags);
		// データ登録
		store(stringSample);
	}

	/**
	 * SDML制御ログの収集データ登録<br>
	 * ※ログフォーマットに関する処理は不要のため独自に実装する<br>
	 * 
	 * @see com.clustercontrol.hub.util.CollectStringDataUtil
	 */
	private static void store(StringSample sample) {
		logger.debug("store() : start");

		List<CollectStringData> collectdata_entities = new ArrayList<CollectStringData>();
		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.begin();

		for (StringSampleData data: sample.getStringSampleList()) {
			if (logger.isDebugEnabled()) {
				logger.debug("store() : facilityId=" + data.getFacilityId()
						+ ", dateTime=" + sample.getDateTime()
						+ ", targetName=" + data.getTargetName()
						+ ", value=" + sample.getDateTime());
			}

			// スキーマの内容を考慮し、512 文字で制限する。
			String targetName = data.getTargetName();
			if (targetName.length() > 512) {
				targetName = targetName.substring(0, 512);
			}

			Long collectId = CollectStringDataUtil.getCollectStringKeyInfoPK(sample.getMonitorId(), data.getFacilityId(), targetName, jtm);
			Long dataId = StringDataIdGenerator.getNext();
			if (dataId == StringDataIdGenerator.getMax() / 2) {
				AplLogger.put(InternalIdCommon.HUB_TRF_SYS_001, new String[]{},
						String.format("current=%d, max=%d", dataId, StringDataIdGenerator.getMax()));
			}

			CollectStringDataPK pk = new CollectStringDataPK(collectId, dataId);

			String value = data.getValue();
			if (value == null) {
				value = "";
			}
			CollectStringData collectData = new CollectStringData(pk, HinemosTime.currentTimeMillis(), value);

			if (!data.getTagList().isEmpty()) {
				Map<String, CollectDataTag> tagMap = new HashMap<>();
				for (StringSampleTag tag : data.getTagList()) {
					tagMap.put(tag.getKey(), new CollectDataTag(new CollectDataTagPK(collectId, dataId, tag.getKey()), tag.getType(), tag.getValue()));
				}
				collectData.getTagList().addAll(tagMap.values());
			}

			collectdata_entities.add(collectData);
			logger.debug("store() : " + collectData);
		}

		jtm.commit();

		List<JdbcBatchQuery> query = new ArrayList<JdbcBatchQuery>();
		// データを更新(挿入)する
		if(!collectdata_entities.isEmpty()) {
			query.add(new CollectStringDataJdbcBatchInsert(collectdata_entities));
			for (CollectStringData data : collectdata_entities) {
				query.add(new CollectStringTagJdbcBatchInsert(data.getTagList()));
			}
		}
		JdbcBatchExecutor.execute(query);
		jtm.close();

		logger.debug("store() : end");
	}

}
