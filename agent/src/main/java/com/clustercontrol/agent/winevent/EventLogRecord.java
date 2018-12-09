/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.winevent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.clustercontrol.winevent.bean.WinEventConstant;

public class EventLogRecord {
	
	private String message;
	private List<String> data;
	private int id;
	private int level = WinEventConstant.UNDEFINED;
	private int task = WinEventConstant.UNDEFINED;
	private int opcode = WinEventConstant.UNDEFINED;
	private long keywords;
	private long recordId;
	private String providerName;
	private int providerId;
	private String logName;
	private int processId;
	private int threadId;
	private String machineName;
	private String userId;
	private Date timeCreated;
	private String levelDisplayName;
	private String opcodeDisplayName;
	private String taskDisplayName;
	private String keywordsDisplayNames;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<String> getData() {
		if(data == null){
			data = new ArrayList<String>();
		}
		return data;
	}
	public void setData(List<String> data) {
		this.data = data;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getTask() {
		return task;
	}
	public void setTask(int task) {
		this.task = task;
	}
	public int getOpcode() {
		return opcode;
	}
	public void setOpcode(int opcode) {
		this.opcode = opcode;
	}
	public long getKeywords() {
		return keywords;
	}
	public void setKeywords(long keywords) {
		this.keywords = keywords;
	}
	public long getRecordId() {
		return recordId;
	}
	public void setRecordId(long recordId) {
		this.recordId = recordId;
	}
	public String getProviderName() {
		return providerName;
	}
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}
	public int getProviderId() {
		return providerId;
	}
	public void setProviderId(int providerId) {
		this.providerId = providerId;
	}
	public String getLogName() {
		return logName;
	}
	public void setLogName(String logName) {
		this.logName = logName;
	}
	public int getProcessId() {
		return processId;
	}
	public void setProcessId(int processId) {
		this.processId = processId;
	}
	public int getThreadId() {
		return threadId;
	}
	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Date getTimeCreated() {
		return timeCreated;
	}
	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}
	public String getLevelDisplayName() {
		return levelDisplayName;
	}
	public void setLevelDisplayName(String levelDisplayName) {
		this.levelDisplayName = levelDisplayName;
	}
	public String getOpcodeDisplayName() {
		return opcodeDisplayName;
	}
	public void setOpcodeDisplayName(String opcodeDisplayName) {
		this.opcodeDisplayName = opcodeDisplayName;
	}
	public String getTaskDisplayName() {
		return taskDisplayName;
	}
	public void setTaskDisplayName(String taskDisplayName) {
		this.taskDisplayName = taskDisplayName;
	}
	public String getKeywordsDisplayNames() {
		return keywordsDisplayNames;
	}
	public void setKeywordsDisplayNames(String keywordsDisplayNames) {
		this.keywordsDisplayNames = keywordsDisplayNames;
	}

	// TODO 設定ファイルで指定できるようにする？
	public String toString(){
		return  "<" + this.logName + ";" + this.providerName + ";" + level2char(this.level) + this.id + ";>" + (this.message!=null? this.message:this.data);
	}
	
	private String level2char(int level){
		switch(level){
		case 0:		// 情報(Informational)
			return "I";
		case 1:		// 重大(Critical)
			return "C";
		case 2:		// エラー(Error)
			return "E";
		case 3:		// 警告(Warning)
			return "W";
		case 4:		// 情報(Informational)
			return "I";
		case 5:		// 詳細(Verbose
			return "V";
		default:	// その他(Unknown)	
			return "U";
		}
	}
}
