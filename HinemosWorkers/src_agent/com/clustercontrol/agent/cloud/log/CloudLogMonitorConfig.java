/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log;

import java.util.ArrayList;

import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

import com.clustercontrol.agent.util.RestAgentBeanUtil;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.xcloud.bean.CloudConstant;

/**
 * クラウドログ監視の設定を保持するクラス<BR>
 */
public class CloudLogMonitorConfig {

	AgtMonitorInfoResponse monInfo = null;
	AgtRunInstructionInfoResponse runInfo = null;
	AgtRunInstructionInfoRequest runInfoReq = null;
	String monitorId = "";
	String platform = "";
	String access = "";
	String secret = "";
	String logGroup = "";
	String filePath = "";
	String resourceGroup = "";
	String workspaceName = "";
	String table = "";
	String col = "";
	String location = "";
	String retCode = "";
	int interval;
	long lastFireTime;
	boolean isPrefix = false;
	ArrayList<String> logStreams = new ArrayList<String>();
	int offset;

	public AgtRunInstructionInfoRequest getRunInfoReq() {
		return runInfoReq;
	}

	public void setRunInfoReq(AgtRunInstructionInfoRequest runInfoReq) {
		this.runInfoReq = runInfoReq;
	}

	public AgtRunInstructionInfoResponse getRunInfo() {
		return runInfo;
	}

	public void setRunInfo(AgtRunInstructionInfoResponse runInfo) {
		this.runInfo = runInfo;
		if (runInfo != null) {
			runInfoReq = new AgtRunInstructionInfoRequest();
			try {
				RestAgentBeanUtil.convertBean(this.runInfo, this.runInfoReq);
			} catch (HinemosUnknown e) {

			}
		}
	}

	public AgtMonitorInfoResponse getMonInfo() {
		return monInfo;
	}

	public void setMonInfo(AgtMonitorInfoResponse monInfo) {
		this.monInfo = monInfo;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	/*
	 * Return codeのみファイル監視に渡すものと、 クラウドログの取得時に使用する設定がことなるので 独自でセット。
	 * 他はファイル監視設定の物がそのまま使用される。
	 */
	public void setReturnCode(String retCode) {
		this.retCode = retCode;
	}

	public String getReturnCode() {
		return retCode;
	}

	public String getPatternHead() {
		return monInfo.getLogfileCheckInfo().getPatternHead();
	}

	public String getPatternTail() {
		return monInfo.getLogfileCheckInfo().getPatternTail();
	}

	public int getMaxBytes() {
		return monInfo.getLogfileCheckInfo().getMaxBytes();
	}

	public String getResourceGroup() {
		return resourceGroup;
	}

	public void setResourceGroup(String resourceGroup) {
		this.resourceGroup = resourceGroup;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public CloudLogMonitorConfig() {

	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getLogGroup() {
		return logGroup;
	}

	public void setLogGroup(String logGroup) {
		this.logGroup = logGroup;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public long getLastFireTime() {
		return lastFireTime;
	}

	public void setLastFireTime(long lastFireTime) {
		this.lastFireTime = lastFireTime;
	}

	public ArrayList<String> getLogSreams() {
		return logStreams;
	}

	public void setLogSreams(ArrayList<String> logSreams) {
		this.logStreams = logSreams;
	}

	public boolean isPrefix() {
		return isPrefix;
	}

	public void setPrefix(boolean isPrefix) {
		this.isPrefix = isPrefix;
	}

	public String getCol() {
		return col;
	}

	public void setCol(String col) {
		this.col = col;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}
	
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public boolean compare(CloudLogMonitorConfig other) {

		if (!this.platform.equals(other.getPlatform())) {
			return false;
		} else if (this.interval != other.getInterval()) {
			return false;
		}
		if (this.platform.equals(CloudConstant.platform_AWS)) {
			if (!this.location.equals(other.getLocation())) {
				return false;
			} else if (!this.logGroup.equals(other.getLogGroup())) {
				return false;
			} else if (!this.logStreams.equals(other.getLogSreams())) {
				return false;
			}
		} else {
			if (!this.resourceGroup.equals(other.getResourceGroup())) {
				return false;
			} else if (!this.workspaceName.equals(other.getWorkspaceName())) {
				return false;
			} else if (!this.table.equals(other.getTable())) {
				return false;
			} else if (!this.col.equals(other.getCol())) {
				return false;
			}
		}

		return true;

	}

}