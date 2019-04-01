/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.monitor.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * イベント表示の設定情報を保持するDTOです。<BR>
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class EventCustomCommandInfo implements Serializable {

	private static final long serialVersionUID = -7178781596940538332L;
	
	private String encode;
	private Boolean enable;
	private String displayName;
	private String description;
	private String command;
	private Long warnRc;
	private Long errorRc;
	private Long maxEventSize;
	private Long thread;
	private Long queue;
	private String dateFormat;
	private String user;
	private Long timeout;
	private Long buffer;
	private String mode;
	private Boolean login;
	private Long resultPollingKeeptime;
	private Long resultPollingDelay;
	private Long resultPollingInterval;
	
	public String getEncode() {
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}
	public Boolean getEnable() {
		return enable;
	}
	public void setEnable(Boolean enable) {
		this.enable = enable;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public Long getWarnRc() {
		return warnRc;
	}
	public void setWarnRc(Long warnRc) {
		this.warnRc = warnRc;
	}
	public Long getErrorRc() {
		return errorRc;
	}
	public void setErrorRc(Long errorRc) {
		this.errorRc = errorRc;
	}
	public Long getMaxEventSize() {
		return maxEventSize;
	}
	public void setMaxEventSize(Long maxEventSize) {
		this.maxEventSize = maxEventSize;
	}
	public Long getThread() {
		return thread;
	}
	public void setThread(Long thread) {
		this.thread = thread;
	}
	public Long getQueue() {
		return queue;
	}
	public void setQueue(Long queue) {
		this.queue = queue;
	}
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public Long getTimeout() {
		return timeout;
	}
	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}
	public Long getBuffer() {
		return buffer;
	}
	public void setBuffer(Long buffer) {
		this.buffer = buffer;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public Boolean getLogin() {
		return login;
	}
	public void setLogin(Boolean login) {
		this.login = login;
	}
	public Long getResultPollingKeeptime() {
		return resultPollingKeeptime;
	}
	public void setResultPollingKeeptime(Long resultPollingKeeptime) {
		this.resultPollingKeeptime = resultPollingKeeptime;
	}
	public Long getResultPollingDelay() {
		return resultPollingDelay;
	}
	public void setResultPollingDelay(Long resultPollingDelay) {
		this.resultPollingDelay = resultPollingDelay;
	}
	public Long getResultPollingInterval() {
		return resultPollingInterval;
	}
	public void setResultPollingInterval(Long resultPollingInterval) {
		this.resultPollingInterval = resultPollingInterval;
	}
}
