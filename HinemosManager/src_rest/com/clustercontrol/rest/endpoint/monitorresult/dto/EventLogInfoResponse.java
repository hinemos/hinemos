/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class EventLogInfoResponse {
	private Integer priority = null;			//重要度
	@RestBeanConvertDatetime
	private String outputDate = null;				//受信日時
	@RestBeanConvertDatetime
	private String generationDate = null;			//出力日時
	@RestBeanConvertDatetime
	private String predictGenerationDate = null;	//将来予測出力日時(性能グラフで使用)
	private String pluginId = null;				//プラグインID
	private String monitorId = null;			//監視項目ID
	private String monitorDetailId = null;			//監視詳細
	private String parentMonitorDetailId = null;	//変化量、将来予測の親の監視詳細(性能グラフで使用)
	private String facilityId = null;			//ファシリティID
	@RestPartiallyTransrateTarget
	private String scopeText = null;			//スコープ
	@RestPartiallyTransrateTarget
	private String application = null;			//アプリケーション
	@RestPartiallyTransrateTarget
	private String message = null;				//メッセージ
	@RestPartiallyTransrateTarget
	private String messageOrg = null;			//オリジナルメッセージ
	private Integer confirmed = null;			//確認
	@RestBeanConvertDatetime
	private String confirmDate = null;			//確認済み日時
	private String confirmUser = null;			//確認ユーザ
	private Integer duplicationCount = null;	//重複カウンタ
	private String comment = null;				//コメント
	@RestBeanConvertDatetime
	private String commentDate = null;			//コメント確認日時
	private String commentUser = null;			//コメント確認ユーザ
	private Boolean collectGraphFlg = null;	//性能グラフ用フラグ
	private String ownerRoleId = null;			//オーナーロールID
	private Long position = null;			//イベント番号
	private String userItem01;//ユーザ項目01
	private String userItem02;//ユーザ項目02
	private String userItem03;//ユーザ項目03
	private String userItem04;//ユーザ項目04
	private String userItem05;//ユーザ項目05
	private String userItem06;//ユーザ項目06
	private String userItem07;//ユーザ項目07
	private String userItem08;//ユーザ項目08
	private String userItem09;//ユーザ項目09
	private String userItem10;//ユーザ項目10
	private String userItem11;//ユーザ項目11
	private String userItem12;//ユーザ項目12
	private String userItem13;//ユーザ項目13
	private String userItem14;//ユーザ項目14
	private String userItem15;//ユーザ項目15
	private String userItem16;//ユーザ項目16
	private String userItem17;//ユーザ項目17
	private String userItem18;//ユーザ項目18
	private String userItem19;//ユーザ項目19
	private String userItem20;//ユーザ項目20
	private String userItem21;//ユーザ項目21
	private String userItem22;//ユーザ項目22
	private String userItem23;//ユーザ項目23
	private String userItem24;//ユーザ項目24
	private String userItem25;//ユーザ項目25
	private String userItem26;//ユーザ項目26
	private String userItem27;//ユーザ項目27
	private String userItem28;//ユーザ項目28
	private String userItem29;//ユーザ項目29
	private String userItem30;//ユーザ項目30
	private String userItem31;//ユーザ項目31
	private String userItem32;//ユーザ項目32
	private String userItem33;//ユーザ項目33
	private String userItem34;//ユーザ項目34
	private String userItem35;//ユーザ項目35
	private String userItem36;//ユーザ項目36
	private String userItem37;//ユーザ項目37
	private String userItem38;//ユーザ項目38
	private String userItem39;//ユーザ項目39
	private String userItem40;//ユーザ項目40
	private String notifyUUID;//通知のuuid
	private String managerName = null;			//マネージャ名
	private List<EventLogOperationHistoryEntityResponse> eventLogHitory = null;
	
	/**
	 * コンストラクタ。
	 */
	public EventLogInfoResponse() {
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setOutputDate(String outputDate) {
		this.outputDate = outputDate;
	}
	public String getOutputDate() {
		return outputDate;
	}
	public void setGenerationDate(String generationDate) {
		this.generationDate = generationDate;
	}
	public String getGenerationDate() {
		return generationDate;
	}
	public void setPredictGenerationDate(String predictGenerationDate) {
		this.predictGenerationDate = predictGenerationDate;
	}
	public String getPredictGenerationDate() {
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
	public void setConfirmDate(String confirmDate) {
		this.confirmDate = confirmDate;
	}
	public String getConfirmDate() {
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
	public void setCommentDate(String commentDate){
		this.commentDate = commentDate;
	}
	public String getCommentDate(){
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
	public Long getPosition() {
		return position;
	}
	public void setPosition(Long position) {
		this.position = position;
	}
	
	public String getUserItem01() {
		return userItem01;
	}

	public void setUserItem01(String userItem01) {
		this.userItem01 = userItem01;
	}

	public String getUserItem02() {
		return userItem02;
	}

	public void setUserItem02(String userItem02) {
		this.userItem02 = userItem02;
	}

	public String getUserItem03() {
		return userItem03;
	}

	public void setUserItem03(String userItem03) {
		this.userItem03 = userItem03;
	}

	public String getUserItem04() {
		return userItem04;
	}

	public void setUserItem04(String userItem04) {
		this.userItem04 = userItem04;
	}

	public String getUserItem05() {
		return userItem05;
	}

	public void setUserItem05(String userItem05) {
		this.userItem05 = userItem05;
	}

	public String getUserItem06() {
		return userItem06;
	}

	public void setUserItem06(String userItem06) {
		this.userItem06 = userItem06;
	}

	public String getUserItem07() {
		return userItem07;
	}

	public void setUserItem07(String userItem07) {
		this.userItem07 = userItem07;
	}

	public String getUserItem08() {
		return userItem08;
	}

	public void setUserItem08(String userItem08) {
		this.userItem08 = userItem08;
	}

	public String getUserItem09() {
		return userItem09;
	}

	public void setUserItem09(String userItem09) {
		this.userItem09 = userItem09;
	}

	public String getUserItem10() {
		return userItem10;
	}

	public void setUserItem10(String userItem10) {
		this.userItem10 = userItem10;
	}

	public String getUserItem11() {
		return userItem11;
	}

	public void setUserItem11(String userItem11) {
		this.userItem11 = userItem11;
	}

	public String getUserItem12() {
		return userItem12;
	}

	public void setUserItem12(String userItem12) {
		this.userItem12 = userItem12;
	}

	public String getUserItem13() {
		return userItem13;
	}

	public void setUserItem13(String userItem13) {
		this.userItem13 = userItem13;
	}

	public String getUserItem14() {
		return userItem14;
	}

	public void setUserItem14(String userItem14) {
		this.userItem14 = userItem14;
	}

	public String getUserItem15() {
		return userItem15;
	}

	public void setUserItem15(String userItem15) {
		this.userItem15 = userItem15;
	}

	public String getUserItem16() {
		return userItem16;
	}

	public void setUserItem16(String userItem16) {
		this.userItem16 = userItem16;
	}

	public String getUserItem17() {
		return userItem17;
	}

	public void setUserItem17(String userItem17) {
		this.userItem17 = userItem17;
	}

	public String getUserItem18() {
		return userItem18;
	}

	public void setUserItem18(String userItem18) {
		this.userItem18 = userItem18;
	}

	public String getUserItem19() {
		return userItem19;
	}

	public void setUserItem19(String userItem19) {
		this.userItem19 = userItem19;
	}

	public String getUserItem20() {
		return userItem20;
	}

	public void setUserItem20(String userItem20) {
		this.userItem20 = userItem20;
	}

	public String getUserItem21() {
		return userItem21;
	}

	public void setUserItem21(String userItem21) {
		this.userItem21 = userItem21;
	}

	public String getUserItem22() {
		return userItem22;
	}

	public void setUserItem22(String userItem22) {
		this.userItem22 = userItem22;
	}

	public String getUserItem23() {
		return userItem23;
	}

	public void setUserItem23(String userItem23) {
		this.userItem23 = userItem23;
	}

	public String getUserItem24() {
		return userItem24;
	}

	public void setUserItem24(String userItem24) {
		this.userItem24 = userItem24;
	}

	public String getUserItem25() {
		return userItem25;
	}

	public void setUserItem25(String userItem25) {
		this.userItem25 = userItem25;
	}

	public String getUserItem26() {
		return userItem26;
	}

	public void setUserItem26(String userItem26) {
		this.userItem26 = userItem26;
	}

	public String getUserItem27() {
		return userItem27;
	}

	public void setUserItem27(String userItem27) {
		this.userItem27 = userItem27;
	}

	public String getUserItem28() {
		return userItem28;
	}

	public void setUserItem28(String userItem28) {
		this.userItem28 = userItem28;
	}

	public String getUserItem29() {
		return userItem29;
	}

	public void setUserItem29(String userItem29) {
		this.userItem29 = userItem29;
	}

	public String getUserItem30() {
		return userItem30;
	}

	public void setUserItem30(String userItem30) {
		this.userItem30 = userItem30;
	}

	public String getUserItem31() {
		return userItem31;
	}

	public void setUserItem31(String userItem31) {
		this.userItem31 = userItem31;
	}

	public String getUserItem32() {
		return userItem32;
	}

	public void setUserItem32(String userItem32) {
		this.userItem32 = userItem32;
	}

	public String getUserItem33() {
		return userItem33;
	}

	public void setUserItem33(String userItem33) {
		this.userItem33 = userItem33;
	}

	public String getUserItem34() {
		return userItem34;
	}

	public void setUserItem34(String userItem34) {
		this.userItem34 = userItem34;
	}

	public String getUserItem35() {
		return userItem35;
	}

	public void setUserItem35(String userItem35) {
		this.userItem35 = userItem35;
	}

	public String getUserItem36() {
		return userItem36;
	}

	public void setUserItem36(String userItem36) {
		this.userItem36 = userItem36;
	}

	public String getUserItem37() {
		return userItem37;
	}

	public void setUserItem37(String userItem37) {
		this.userItem37 = userItem37;
	}

	public String getUserItem38() {
		return userItem38;
	}

	public void setUserItem38(String userItem38) {
		this.userItem38 = userItem38;
	}

	public String getUserItem39() {
		return userItem39;
	}

	public void setUserItem39(String userItem39) {
		this.userItem39 = userItem39;
	}

	public String getUserItem40() {
		return userItem40;
	}

	public void setUserItem40(String userItem40) {
		this.userItem40 = userItem40;
	}

	public String getNotifyUUID() {
		return notifyUUID;
	}

	public void setNotifyUUID(String notifyUUID) {
		this.notifyUUID = notifyUUID;
	}

	public List<EventLogOperationHistoryEntityResponse> getEventLogHitory() {
		return eventLogHitory;
	}

	public void setEventLogHitory(List<EventLogOperationHistoryEntityResponse> eventLogHitory) {
		this.eventLogHitory = eventLogHitory;
	}

	public String getManagerName() {
		return managerName;
	}
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}
}
