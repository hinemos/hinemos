/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.util.HinemosTime;

public class NodeConfigRunCollectCallback implements JpaTransactionCallback {

	private static Log m_log = LogFactory.getLog(NodeConfigRunCollectCallback.class);

	public final List<String> facilityIdList;

	public NodeConfigRunCollectCallback(List<String> facilityIdList) {
		this.facilityIdList = facilityIdList;
	}

	@Override
	public void preFlush() {
	}

	@Override
	public void postFlush() {
	}

	@Override
	public void preCommit() {
	}

	@Override
	public void postCommit() {

		// 更新時刻リフレッシュ(AgentのReceiveTopicで更新チェックする).
		SettingUpdateInfo.getInstance().setNodeConfigRunCollectUpdateTime(HinemosTime.currentTimeMillis());

		// 即時実行のAgent通知.
		TopicInfo topicInfo = new TopicInfo();
		topicInfo.setNodeConfigRunInstructed(true);

		// Manager負荷軽減のためにAgent実行タイミングをずらす.
		Long sleep = HinemosPropertyCommon.repository_node_config_run_sleep.getNumericValue();
		for (String facilityId : this.facilityIdList) {
			m_log.info("runCollectNodeConfig() : setTopic(" + facilityId + ")");
			AgentConnectUtil.setTopic(facilityId, topicInfo);
			try {
				Thread.sleep(sleep.longValue());
			} catch (InterruptedException e) {
				m_log.info("runCollectNodeConfig : " + e.getMessage());
			}
		}
	}

	@Override
	public void preRollback() {
	}

	@Override
	public void postRollback() {
	}

	@Override
	public void preClose() {
	}

	@Override
	public void postClose() {
	}

	@Override
	public int hashCode() {
		int h = 1;
		if (this.facilityIdList == null || this.facilityIdList.isEmpty()) {
			h = h * 31 + 0;
		} else {
			for (String facilityId : this.facilityIdList) {
				h = h * 31 + facilityId.hashCode();
			}
		}
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof NodeConfigRunCollectCallback) {
			NodeConfigRunCollectCallback cast = (NodeConfigRunCollectCallback) obj;
			if (this.facilityIdList == null && cast.facilityIdList == null) {
				return true;
			}
			if (this.facilityIdList == null) {
				return false;
			}
			if (cast.facilityIdList == null) {
				return false;
			}
			if (this.facilityIdList.size() != cast.facilityIdList.size()) {
				return false;
			}
			if (this.facilityIdList.containsAll(cast.facilityIdList)) {
				return true;
			}
		}
		return false;
	}

}
