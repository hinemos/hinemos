/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.agent.bean.TopicFlagConstant;

import com.clustercontrol.util.HinemosTime;

@XmlType(namespace = "http://agent.ws.clustercontrol.com")
public class TopicInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// job
	private RunInstructionInfo runInstructionInfo = null;

	// com.clustercontrol.agent.bean.TopicFlagConstant.java
	private long flag = 0;

	// none=0, restart=1, update=2
	private int agentCommand = 0;

	// トピック生成日時。古いトピックを無効とするための値。
	private long generateDate = 0;

	public TopicInfo () {
		generateDate = HinemosTime.currentTimeMillis();
	}

	// 古いトピックは無効とする。
	public boolean isValid() {
		// TODO hinemos.propertiesで与えたい。
		int topicValidPeriod = 3600;
		return (HinemosTime.currentTimeMillis() <
				generateDate + topicValidPeriod * 1000l);
	}

	public long getFlag() {
		return flag;
	}
	public void setFlag(long flag) {
		this.flag = flag;
	}

	private boolean isRepositoryChanged() {
		return (flag & TopicFlagConstant.REPOSITORY_CHANGED) != 0;
	}
	public void setRepositoryChanged(boolean changed) {
		if (changed) {
			flag = flag | TopicFlagConstant.REPOSITORY_CHANGED;
		} else {
			flag = flag - (flag & TopicFlagConstant.REPOSITORY_CHANGED);
		}
	}
	private boolean isCalendarChanged() {
		return (flag & TopicFlagConstant.CALENDAR_CHANGED) != 0;
	}
	public void setCalendarChanged(boolean changed) {
		if (changed) {
			flag = flag | TopicFlagConstant.CALENDAR_CHANGED;
		} else {
			flag = flag - (flag & TopicFlagConstant.CALENDAR_CHANGED);
		}
	}
	private boolean isNewFacilityFlag() {
		return (flag & TopicFlagConstant.NEW_FACILITY) != 0;
	}
	public void setNewFacilityFlag(boolean newFlag) {
		if (newFlag) {
			flag = flag | TopicFlagConstant.NEW_FACILITY;
		} else {
			flag = flag - (flag & TopicFlagConstant.NEW_FACILITY);
		}
	}
	private boolean isLogfileMonitorChanged() {
		return (flag & TopicFlagConstant.LOGFILE_CHANGED) != 0;
	}
	public void setLogfileMonitorChanged(boolean changed) {
		if (changed) {
			flag = flag | TopicFlagConstant.LOGFILE_CHANGED;
		} else {
			flag = flag - (flag & TopicFlagConstant.LOGFILE_CHANGED);
		}
	}
	private boolean isBinaryMonitorChanged() {
		return (flag & TopicFlagConstant.BINARY_CHANGED) != 0;
	}

	public void setBinaryMonitorChanged(boolean changed) {
		if (changed) {
			flag = flag | TopicFlagConstant.BINARY_CHANGED;
		} else {
			flag = flag - (flag & TopicFlagConstant.BINARY_CHANGED);
		}
	}
	private boolean isFileCheckChanged() {
		return (flag & TopicFlagConstant.FILECHECK_CHANGED) != 0;
	}
	public void setFileCheckChanged(boolean changed) {
		if (changed) {
			flag = flag | TopicFlagConstant.FILECHECK_CHANGED;
		} else {
			flag = flag - (flag & TopicFlagConstant.FILECHECK_CHANGED);
		}
	}
	private boolean isCustomMonitorChanged() {
		return (flag & TopicFlagConstant.CUSTOM_CHANGED) != 0;
	}
	public void setCustomMonitorChanged(boolean changed) {
		if (changed) {
			flag = flag | TopicFlagConstant.CUSTOM_CHANGED;
		} else {
			flag = flag - (flag & TopicFlagConstant.CUSTOM_CHANGED);
		}
	}
	private boolean isWinEventMonitorChanged() {
		return (flag & TopicFlagConstant.WINEVENT_CHANGED) != 0;
	}
	public void setWinEventMonitorChanged(boolean changed) {
		if (changed) {
			flag = flag | TopicFlagConstant.WINEVENT_CHANGED;
		} else {
			flag = flag - (flag & TopicFlagConstant.WINEVENT_CHANGED);
		}
	}
	public RunInstructionInfo getRunInstructionInfo() {
		return runInstructionInfo;
	}
	public void setRunInstructionInfo(RunInstructionInfo runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}
	public int getAgentCommand() {
		return agentCommand;
	}
	public void setAgentCommand(int agentCommand) {
		this.agentCommand = agentCommand;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + agentCommand;
		result = prime * result + (int) (flag ^ (flag >>> 32));
		result = prime * result + (int) (generateDate ^ (generateDate >>> 32));
		result = prime
				* result
				+ ((runInstructionInfo == null) ? 0 : runInstructionInfo
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object otherObject) {
		if (!(otherObject instanceof TopicInfo))
			return false;
		
		TopicInfo otherTopic = (TopicInfo)otherObject;
		if (otherTopic.getFlag() != this.getFlag())
			return false;
		
		if (otherTopic.runInstructionInfo == this.runInstructionInfo)
			return true;
		
		if (otherTopic.runInstructionInfo == null)
			return false;
		
		return otherTopic.runInstructionInfo.equals(this.runInstructionInfo);
	}
	
	@Override
	public String toString() {
		String ret = flag + "," + agentCommand;
		if (runInstructionInfo != null) {
			ret += "," + runInstructionInfo.getSessionId() + 
					"," + runInstructionInfo.getJobunitId() + 
					"," + runInstructionInfo.getJobId() +
					"," + isValid();
		}
		return ret;
	}

	public static void main (String args[]) {
		/*
		 * ビット演算のチェック
		 */
		TopicInfo info = new TopicInfo();
		System.out.println("flag=" + info.getFlag());

		boolean flag = true;
		flag = true;
		info.setRepositoryChanged(flag);
		System.out.println((flag ^ info.isRepositoryChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = false;
		info.setRepositoryChanged(flag);
		System.out.println((flag ^ info.isRepositoryChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = true;
		info.setRepositoryChanged(flag);
		System.out.println((flag ^ info.isRepositoryChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());

		flag = true;
		info.setNewFacilityFlag(flag);
		System.out.println((flag ^ info.isNewFacilityFlag() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = false;
		info.setNewFacilityFlag(flag);
		System.out.println((flag ^ info.isNewFacilityFlag() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = false;
		info.setNewFacilityFlag(flag);
		System.out.println((flag ^ info.isNewFacilityFlag() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = true;
		info.setNewFacilityFlag(flag);
		System.out.println((flag ^ info.isNewFacilityFlag() ? "NG" : "OK") + ", flag=" + info.getFlag());

		flag = false;
		info.setCalendarChanged(flag);
		System.out.println((flag ^ info.isCalendarChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = true;
		info.setCalendarChanged(flag);
		System.out.println((flag ^ info.isCalendarChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());

		flag = true;
		info.setCustomMonitorChanged(flag);
		System.out.println((flag ^ info.isCustomMonitorChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = false;
		info.setCustomMonitorChanged(flag);
		System.out.println((flag ^ info.isCustomMonitorChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = true;
		info.setCustomMonitorChanged(flag);
		System.out.println((flag ^ info.isCustomMonitorChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());

		flag = true;
		info.setWinEventMonitorChanged(flag);
		System.out.println((flag ^ info.isWinEventMonitorChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = false;
		info.setWinEventMonitorChanged(flag);
		System.out.println((flag ^ info.isWinEventMonitorChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = true;
		info.setWinEventMonitorChanged(flag);
		System.out.println((flag ^ info.isWinEventMonitorChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());

		flag = false;
		info.setLogfileMonitorChanged(flag);
		System.out.println((flag ^ info.isLogfileMonitorChanged() ? "NG" : "OK")  + ", flag=" + info.getFlag());
		flag = true;
		info.setLogfileMonitorChanged(flag);
		System.out.println((flag ^ info.isLogfileMonitorChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());

		flag = false;
		info.setBinaryMonitorChanged(flag);
		System.out.println((flag ^ info.isBinaryMonitorChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());
		flag = true;
		info.setBinaryMonitorChanged(flag);
		System.out.println((flag ^ info.isBinaryMonitorChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());

		flag = false;
		info.setFileCheckChanged(flag);
		System.out.println((flag ^ info.isFileCheckChanged() ? "NG" : "OK")  + ", flag=" + info.getFlag());
		flag = true;
		info.setFileCheckChanged(flag);
		System.out.println((flag ^ info.isFileCheckChanged() ? "NG" : "OK") + ", flag=" + info.getFlag());

	}
}
