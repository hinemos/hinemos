/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
 * イベント情報を保持するDTOです。<BR>
 *
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class EventDataInfo implements Serializable {

	private static final long serialVersionUID = -7703536781116618466L;
	private Integer priority = null;			//重要度
	private Long outputDate = null;				//受信日時
	private Long generationDate = null;			//出力日時
	private Long predictGenerationDate = null;	//将来予測出力日時(性能グラフで使用)
	private String pluginId = null;				//プラグインID
	private String monitorId = null;			//監視項目ID
	private String monitorDetailId = null;			//監視詳細
	private String parentMonitorDetailId = null;	//変化量、将来予測の親の監視詳細(性能グラフで使用)
	private String facilityId = null;			//ファシリティID
	private String scopeText = null;			//スコープ
	private String application = null;			//アプリケーション
	private String message = null;				//メッセージ
	private String messageOrg = null;			//オリジナルメッセージ
	private Integer confirmed = null;			//確認
	private Long confirmDate = null;			//確認済み日時
	private String confirmUser = null;			//確認ユーザ
	private Integer duplicationCount = null;	//重複カウンタ
	private String comment = null;				//コメント
	private Long commentDate = null;			//コメント確認日時
	private String commentUser = null;			//コメント確認ユーザ
	private Boolean collectGraphFlg = null;	//蓄積グラフ用フラグ
	private String ownerRoleId = null;			//オーナーロールID
	private String managerName = null;			//マネージャ名

	/**
	 * コンストラクタ。
	 */
	public EventDataInfo() {
		super();
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setOutputDate(Long outputDate) {
		this.outputDate = outputDate;
	}
	public Long getOutputDate() {
		return outputDate;
	}
	public void setGenerationDate(Long generationDate) {
		this.generationDate = generationDate;
	}
	public Long getGenerationDate() {
		return generationDate;
	}
	public void setPredictGenerationDate(Long predictGenerationDate) {
		this.predictGenerationDate = predictGenerationDate;
	}
	public Long getPredictGenerationDate() {
		return predictGenerationDate;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}
	public String getPluginId() {
		return pluginId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public String getMonitorId() {
		return monitorId;
	}
	public String getMonitorDetailId() {
		return monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}
	public String getParentMonitorDetailId() {
		return parentMonitorDetailId;
	}
	public void setParentMonitorDetailId(String parentMonitorDetailId) {
		this.parentMonitorDetailId = parentMonitorDetailId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public String getFacilityId() {
		return facilityId;
	}
	public void setScopeText(String scopeText) {
		this.scopeText = scopeText;
	}
	public String getScopeText() {
		return scopeText;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getApplication() {
		return application;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	public void setMessageOrg(String messageOrg) {
		this.messageOrg = messageOrg;
	}
	public String getMessageOrg() {
		return messageOrg;
	}
	public void setConfirmed(Integer confirmed) {
		this.confirmed = confirmed;
	}
	public Integer getConfirmed() {
		return confirmed;
	}
	public void setConfirmDate(Long confirmDate) {
		this.confirmDate = confirmDate;
	}
	public Long getConfirmDate() {
		return confirmDate;
	}
	public void setConfirmUser(String confirmUser) {
		this.confirmUser = confirmUser;
	}
	public String getConfirmUser() {
		return confirmUser;
	}
	public void setDuplicationCount(Integer duplicationCount) {
		this.duplicationCount = duplicationCount;
	}
	public Integer getDuplicationCount() {
		return duplicationCount;
	}
	public void setComment(String comment){
		this.comment = comment;
	}
	public String getComment(){
		return comment;
	}
	public void setCommentDate(Long commentDate){
		this.commentDate = commentDate;
	}
	public Long getCommentDate(){
		return commentDate;
	}
	public void setCommentUser(String commentUser){
		this.commentUser = commentUser;
	}
	public String getCommentUser(){
		return commentUser;
	}
	public void setCollectGraphFlg(Boolean collectGraphFlg) {
		this.collectGraphFlg = collectGraphFlg;
	}
	public Boolean getCollectGraphFlg() {
		return collectGraphFlg;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	public String getManagerName() {
		return managerName;
	}
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

}
