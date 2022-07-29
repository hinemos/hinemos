/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoPK;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfo;

public class MonitorJudgementInfoCacheRefreshCallback implements JpaTransactionCallback {

	public final String monitorId;

	public final Integer monitorType;

	public final List<MonitorStringValueInfo> monitorStringValueList;

	public final List<MonitorTruthValueInfo> monitorTruthValueList;

	public final List<MonitorNumericValueInfo> monitorNumericValueList;
	
	public boolean isCommit = false;

	public MonitorJudgementInfoCacheRefreshCallback(String monitorId, Integer monitorType,
			List<MonitorStringValueInfo> monitorStringValueList, 
			List<MonitorTruthValueInfo> monitorTruthValueList, 
			List<MonitorNumericValueInfo> monitorNumericValueList) {
		this.monitorId = monitorId;
		this.monitorType = monitorType;
		if (monitorStringValueList != null) {
			this.monitorStringValueList = new ArrayList<>();
			for (MonitorStringValueInfo info : monitorStringValueList) {
				MonitorStringValueInfo valueInfo 
					= new MonitorStringValueInfo(monitorId, info.getOrderNo());
				valueInfo.setMessage(info.getMessage());
				valueInfo.setCaseSensitivityFlg(info.getCaseSensitivityFlg());
				valueInfo.setDescription(info.getDescription());
				valueInfo.setPattern(info.getPattern());
				valueInfo.setPriority(info.getPriority());
				valueInfo.setProcessType(info.getProcessType());
				valueInfo.setValidFlg(info.getValidFlg());
				this.monitorStringValueList.add(valueInfo);
			}
		} else {
			this.monitorStringValueList = null;
		}
		if (monitorTruthValueList != null) {
			this.monitorTruthValueList = new ArrayList<>();
			for (MonitorTruthValueInfo info : monitorTruthValueList) {
				MonitorTruthValueInfo valueInfo 
					= new MonitorTruthValueInfo(
							monitorId, info.getPriority(), info.getTruthValue());
				valueInfo.setMessage(info.getMessage());
				this.monitorTruthValueList.add(valueInfo);
			}
		} else {
			this.monitorTruthValueList = null;
		}
		if (monitorNumericValueList != null) {
			this.monitorNumericValueList = new ArrayList<>();
			for (MonitorNumericValueInfo info : monitorNumericValueList) {
				MonitorNumericValueInfo valueInfo 
					= new MonitorNumericValueInfo(
							new MonitorNumericValueInfoPK(
									monitorId, info.getMonitorNumericType(), info.getPriority()));
				valueInfo.setMessage(info.getMessage());
				valueInfo.setThresholdLowerLimit(info.getThresholdLowerLimit());
				valueInfo.setThresholdUpperLimit(info.getThresholdUpperLimit());
				this.monitorNumericValueList.add(valueInfo);
			}
		} else {
			this.monitorNumericValueList = null;
		}
	}

	@Override
	public boolean isTransaction() {
		return false;
	}

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		isCommit = true;
	}

	@Override
	public void preRollback() { }

	@Override
	public void postRollback() { }

	@Override
	public void preClose() { }

	@Override
	public void postClose() {
		if (isCommit) {
			if (monitorType.equals(MonitorTypeConstant.TYPE_STRING)) {
				MonitorJudgementInfoCache.updateString(monitorId, monitorStringValueList);
			} else if (monitorType.equals(MonitorTypeConstant.TYPE_TRUTH)) {
				MonitorJudgementInfoCache.updateTruth(monitorId, monitorTruthValueList);
			} else if (monitorType.equals(MonitorTypeConstant.TYPE_NUMERIC)) {
				MonitorJudgementInfoCache.updateNumeric(monitorId, monitorNumericValueList);
			} else {
				// 何も処理をしない
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((monitorId == null) ? 0 : monitorId.hashCode());
		result = prime * result + ((monitorNumericValueList == null) ? 0 : monitorNumericValueList.hashCode());
		result = prime * result + ((monitorStringValueList == null) ? 0 : monitorStringValueList.hashCode());
		result = prime * result + ((monitorTruthValueList == null) ? 0 : monitorTruthValueList.hashCode());
		result = prime * result + ((monitorType == null) ? 0 : monitorType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonitorJudgementInfoCacheRefreshCallback other = (MonitorJudgementInfoCacheRefreshCallback) obj;
		if (monitorId == null) {
			if (other.monitorId != null)
				return false;
		} else if (!monitorId.equals(other.monitorId))
			return false;
		if (monitorNumericValueList == null) {
			if (other.monitorNumericValueList != null)
				return false;
		} else if (!monitorNumericValueList.equals(other.monitorNumericValueList))
			return false;
		if (monitorStringValueList == null) {
			if (other.monitorStringValueList != null)
				return false;
		} else if (!monitorStringValueList.equals(other.monitorStringValueList))
			return false;
		if (monitorTruthValueList == null) {
			if (other.monitorTruthValueList != null)
				return false;
		} else if (!monitorTruthValueList.equals(other.monitorTruthValueList))
			return false;
		if (monitorType == null) {
			if (other.monitorType != null)
				return false;
		} else if (!monitorType.equals(other.monitorType))
			return false;
		return true;
	}

}
