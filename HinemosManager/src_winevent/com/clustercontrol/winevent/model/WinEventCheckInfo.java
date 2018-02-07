/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;


/**
 * The persistent class for the cc_monitor_winevent_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_winevent_info", schema="setting")
@Cacheable(true)
public class WinEventCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private boolean levelCritical;
	private boolean levelWarning;
	private boolean levelVerbose;
	private boolean levelError;
	private boolean levelInformational;
	
	private List<String> logNameList = new ArrayList<>();
	private List<String> sourceList = new ArrayList<>();
	private List<Integer> eventIdList = new ArrayList<>();
	private List<Integer> categoryList = new ArrayList<>();
	private List<Long> keywordList = new ArrayList<>();
	
	private List<MonitorWinEventLogInfoEntity> monitorWinEventLogInfoEntities = new ArrayList<>();
	private List<MonitorWinEventSourceInfoEntity> monitorWinEventSourceInfoEntities = new ArrayList<>();
	private List<MonitorWinEventIdInfoEntity> monitorWinEventIdInfoEntities = new ArrayList<>();
	private List<MonitorWinEventCategoryInfoEntity> monitorWinEventCategoryInfoEntities = new ArrayList<>();
	private List<MonitorWinEventKeywordInfoEntity> monitorWinEventKeywordInfoEntities = new ArrayList<>();
	
	private MonitorInfo monitorInfo;
	
	@Deprecated
	public WinEventCheckInfo() {
	}

	@Id
	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}


	@Column(name="level_critical")
	public boolean isLevelCritical() {
		return levelCritical;
	}


	public void setLevelCritical(boolean levelCritical) {
		this.levelCritical = levelCritical;
	}


	@Column(name="level_warning")
	public boolean isLevelWarning() {
		return levelWarning;
	}


	public void setLevelWarning(boolean levelWarning) {
		this.levelWarning = levelWarning;
	}


	@Column(name="level_verbose")
	public boolean isLevelVerbose() {
		return levelVerbose;
	}


	public void setLevelVerbose(boolean levelVerbose) {
		this.levelVerbose = levelVerbose;
	}


	@Column(name="level_error")
	public boolean isLevelError() {
		return levelError;
	}


	public void setLevelError(boolean levelError) {
		this.levelError = levelError;
	}


	@Column(name="level_informational")
	public boolean isLevelInformational() {
		return levelInformational;
	}


	public void setLevelInformational(boolean levelInformational) {
		this.levelInformational = levelInformational;
	}


	//bi-directional one-to-one association to MonitorInfo
	@XmlTransient
	@OneToOne(fetch=FetchType.LAZY, optional=false)
	@PrimaryKeyJoinColumn(name="monitor_id")
	public MonitorInfo getMonitorInfo() {
		return this.monitorInfo;
	}

	@Deprecated
	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}
	
	/*
	 * jax-wsの仕様が変更された。マネージャがDTOを受け取ったときは、
	 * JDK1.7の時：setHoge(List)を呼ぶ
	 * JDK1.8の時：getHogeの返り値のlistにaddAllする
	 * 
	 * となった。
	 * JDK1.8の場合は、マネージャがDTOを受け取った直後にrefrectメソッドを呼び出す必要あり。
	 * 
	 * FIXME あまり良くない実装であるため、後で修正すること
	 */
	public void reflect() {
		setLogName(logNameList);
		setSource(sourceList);
		setEventId(eventIdList);
		setCategory(categoryList);
		setKeywords(keywordList);
	}
	
	@Transient
	public List<String> getLogName(){
		logNameList = new ArrayList<>();
		for (MonitorWinEventLogInfoEntity entity: getMonitorWinEventLogInfoEntities()) {
			logNameList.add(entity.getId().getLogName());
		}
		return logNameList;
	}
	public void setLogName(List<String> logName){
		for(MonitorWinEventLogInfoEntity info: getMonitorWinEventLogInfoEntities()){
			info.relateToMonitorWinEventInfoEntity(null);
		}

		List<MonitorWinEventLogInfoEntity> logs = new ArrayList<MonitorWinEventLogInfoEntity>();
		for(String l : logName){
			logs.add(new MonitorWinEventLogInfoEntity(new MonitorWinEventLogInfoEntityPK(getMonitorId(), l)));
		}
		setMonitorWinEventLogInfoEntities(logs);
	}

	@Transient
	public List<String> getSource(){
		sourceList = new ArrayList<>();
		for (MonitorWinEventSourceInfoEntity entity: getMonitorWinEventSourceInfoEntities()) {
			sourceList.add(entity.getId().getSource());
		}
		return sourceList;
	}
	public void setSource(List<String> source){
		for(MonitorWinEventSourceInfoEntity info: getMonitorWinEventSourceInfoEntities()){
			info.relateToMonitorWinEventInfoEntity(null);
		}

		List<MonitorWinEventSourceInfoEntity> sources = new ArrayList<MonitorWinEventSourceInfoEntity>();
		for(String l : source){
			sources.add(new MonitorWinEventSourceInfoEntity(new MonitorWinEventSourceInfoEntityPK(getMonitorId(), l)));
		}
		setMonitorWinEventSourceInfoEntities(sources);
	}

	@Transient
	public List<Integer> getEventId() {
		eventIdList = new ArrayList<>();
		for (MonitorWinEventIdInfoEntity entity: getMonitorWinEventIdInfoEntities()) {
			eventIdList.add(entity.getId().getEventId());
		}
		return eventIdList;
	}
	public void setEventId(List<Integer> eventId){
		for(MonitorWinEventIdInfoEntity info: getMonitorWinEventIdInfoEntities()){
			info.relateToMonitorWinEventInfoEntity(null);
		}

		List<MonitorWinEventIdInfoEntity> eventIds = new ArrayList<MonitorWinEventIdInfoEntity>();
		for(Integer l : eventId){
			eventIds.add(new MonitorWinEventIdInfoEntity(new MonitorWinEventIdInfoEntityPK(getMonitorId(), l)));
		}
		setMonitorWinEventIdInfoEntities(eventIds);
	}

	@Transient
	public List<Integer> getCategory(){
		categoryList = new ArrayList<>();
		for (MonitorWinEventCategoryInfoEntity entity: getMonitorWinEventCategoryInfoEntities()) {
			categoryList.add(entity.getId().getCategory());
		}
		return categoryList;
	}
	public void setCategory(List<Integer> category){
		for(MonitorWinEventCategoryInfoEntity info: getMonitorWinEventCategoryInfoEntities()){
			info.relateToMonitorWinEventInfoEntity(null);
		}

		List<MonitorWinEventCategoryInfoEntity> categories = new ArrayList<MonitorWinEventCategoryInfoEntity>();
		for(Integer l : category){
			categories.add(new MonitorWinEventCategoryInfoEntity(new MonitorWinEventCategoryInfoEntityPK(getMonitorId(), l)));
		}
		setMonitorWinEventCategoryInfoEntities(categories);
	}

	@Transient
	public List<Long> getKeywords() {
		keywordList = new ArrayList<>();
		for (MonitorWinEventKeywordInfoEntity entity: getMonitorWinEventKeywordInfoEntities()) {
			keywordList.add(entity.getId().getKeyword());
		}
		return keywordList;
	}
	public void setKeywords(List<Long> keywords) {
		for(MonitorWinEventKeywordInfoEntity info: getMonitorWinEventKeywordInfoEntities()){
			info.relateToMonitorWinEventInfoEntity(null);
		}
		setMonitorWinEventKeywordInfoEntities(new ArrayList<MonitorWinEventKeywordInfoEntity>());

		List<MonitorWinEventKeywordInfoEntity> keyword = new ArrayList<MonitorWinEventKeywordInfoEntity>();
		for(Long l : keywords){
			MonitorWinEventKeywordInfoEntity k = new MonitorWinEventKeywordInfoEntity(new MonitorWinEventKeywordInfoEntityPK(getMonitorId(), l));
			keyword.add(k);
		}
		setMonitorWinEventKeywordInfoEntities(keyword);
	}
	
	
	//bi-directional many-to-one association to MonitorWinEventLogInfoEntity
	@XmlTransient
	@OneToMany(mappedBy="monitorWinEventInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorWinEventLogInfoEntity> getMonitorWinEventLogInfoEntities() {
		return this.monitorWinEventLogInfoEntities;
	}

	public void setMonitorWinEventLogInfoEntities(List<MonitorWinEventLogInfoEntity> monitorWinEventLogInfoEntities) {
		this.monitorWinEventLogInfoEntities = monitorWinEventLogInfoEntities;
	}

	//bi-directional many-to-one association to MonitorWinEventSourceInfoEntity
	@XmlTransient
	@OneToMany(mappedBy="monitorWinEventInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorWinEventSourceInfoEntity> getMonitorWinEventSourceInfoEntities() {
		return this.monitorWinEventSourceInfoEntities;
	}

	public void setMonitorWinEventSourceInfoEntities(List<MonitorWinEventSourceInfoEntity> monitorWinEventSourceInfoEntities) {
		this.monitorWinEventSourceInfoEntities = monitorWinEventSourceInfoEntities;
	}

	//bi-directional many-to-one association to MonitorWinEventIdInfoEntity
	@XmlTransient
	@OneToMany(mappedBy="monitorWinEventInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorWinEventIdInfoEntity> getMonitorWinEventIdInfoEntities() {
		return this.monitorWinEventIdInfoEntities;
	}

	public void setMonitorWinEventIdInfoEntities(List<MonitorWinEventIdInfoEntity> monitorWinEventIdInfoEntities) {
		this.monitorWinEventIdInfoEntities = monitorWinEventIdInfoEntities;
	}

	//bi-directional many-to-one association to MonitorWinEventCategoryInfoEntity
	@XmlTransient
	@OneToMany(mappedBy="monitorWinEventInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorWinEventCategoryInfoEntity> getMonitorWinEventCategoryInfoEntities() {
		return this.monitorWinEventCategoryInfoEntities;
	}

	public void setMonitorWinEventCategoryInfoEntities(List<MonitorWinEventCategoryInfoEntity> monitorWinEventCategoryInfoEntities) {
		this.monitorWinEventCategoryInfoEntities = monitorWinEventCategoryInfoEntities;
	}

	//bi-directional many-to-one association to MonitorWinEventKeywordInfoEntity
	@XmlTransient
	@OneToMany(mappedBy="monitorWinEventInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorWinEventKeywordInfoEntity> getMonitorWinEventKeywordInfoEntities() {
		return this.monitorWinEventKeywordInfoEntities;
	}

	public void setMonitorWinEventKeywordInfoEntities(List<MonitorWinEventKeywordInfoEntity> monitorWinEventKeywordInfoEntities) {
		this.monitorWinEventKeywordInfoEntities = monitorWinEventKeywordInfoEntities;
	}

	/**
	 * MonitorInfoオブジェクト参照設定<BR>
	 * 
	 * MonitorInfo設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToMonitorInfo(MonitorInfo monitorInfo) {
		this.setMonitorInfo(monitorInfo);
		if (monitorInfo != null) {
			monitorInfo.setWinEventCheckInfo(this);
		}
	}

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// MonitorInfo
		if (this.monitorInfo != null) {
			this.monitorInfo.setWinEventCheckInfo(null);
		}
	}
}