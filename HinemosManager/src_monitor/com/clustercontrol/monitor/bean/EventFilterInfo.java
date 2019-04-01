/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * Hinemosのイベント情報の検索条件を格納するクラスです。<BR>
 * DTOクラスとしてマネージャ、クライアント間の通信で利用します。
 *
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class EventFilterInfo implements Serializable {

	private static final long serialVersionUID = -8348543802703964223L;
	private Boolean allSearch = null; // キャッシュを使わずにSQL検索をする
	private Long outputDateFrom = null;		//受信日時（自）
	private Long outputDateTo = null;		//受信日時（至）
	private Long generationDateFrom = null;	//出力日時（自）
	private Long generationDateTo = null;	//出力日時（至）
	private String monitorId = null;		//監視項目ID
	private String monitorDetailId = null;	//監視詳細
	private Integer facilityType = null;		//対象ファシリティ種別
	private String application = null;		//アプリケーション
	private String message = null;			//メッセージ
	private Integer[] confirmFlgTypeList = null;	//確認リスト
	private Long outputDate = null;			//受信日時
	private Long generationDate = null;		//出力日時
	private String confirmedUser = null;	//確認ユーザ
	private String comment = null;	//コメント
	private Long commentDate = null;	//コメント更新日時
	private String commentUser = null;	//コメント更新ユーザ
	private String ownerRoleId = null;	//オーナーロールID
	private Boolean collectGraphFlg = null;	//性能グラフ用フラグ
	private Integer[] priorityList = null;		//重要度リスト
	private Long positionFrom = null;	//イベント番号（自）
	private Long positionTo = null;	//イベント番号（至）
	private String userItem01 ; //ユーザ項目01
	private String userItem02 ; //ユーザ項目02
	private String userItem03 ; //ユーザ項目03
	private String userItem04 ; //ユーザ項目04
	private String userItem05 ; //ユーザ項目05
	private String userItem06 ; //ユーザ項目06
	private String userItem07 ; //ユーザ項目07
	private String userItem08 ; //ユーザ項目08
	private String userItem09 ; //ユーザ項目09
	private String userItem10 ; //ユーザ項目10
	private String userItem11 ; //ユーザ項目11
	private String userItem12 ; //ユーザ項目12
	private String userItem13 ; //ユーザ項目13
	private String userItem14 ; //ユーザ項目14
	private String userItem15 ; //ユーザ項目15
	private String userItem16 ; //ユーザ項目16
	private String userItem17 ; //ユーザ項目17
	private String userItem18 ; //ユーザ項目18
	private String userItem19 ; //ユーザ項目19
	private String userItem20 ; //ユーザ項目20
	private String userItem21 ; //ユーザ項目21
	private String userItem22 ; //ユーザ項目22
	private String userItem23 ; //ユーザ項目23
	private String userItem24 ; //ユーザ項目24
	private String userItem25 ; //ユーザ項目25
	private String userItem26 ; //ユーザ項目26
	private String userItem27 ; //ユーザ項目27
	private String userItem28 ; //ユーザ項目28
	private String userItem29 ; //ユーザ項目29
	private String userItem30 ; //ユーザ項目30
	private String userItem31 ; //ユーザ項目31
	private String userItem32 ; //ユーザ項目32
	private String userItem33 ; //ユーザ項目33
	private String userItem34 ; //ユーザ項目34
	private String userItem35 ; //ユーザ項目35
	private String userItem36 ; //ユーザ項目36
	private String userItem37 ; //ユーザ項目37
	private String userItem38 ; //ユーザ項目38
	private String userItem39 ; //ユーザ項目39
	private String userItem40 ; //ユーザ項目40
	private List<EventDataInfo> selectedEventList;//選択イベント（ＣＳＶ出力の場合のみ使用）
	
	public Boolean getAllSearch() {
		return allSearch;
	}
	public void setAllSearch(Boolean allSearch) {
		this.allSearch = allSearch;
	}
	public void setOutputDateFrom(Long outputDateFrom) {
		this.outputDateFrom = outputDateFrom;
	}
	public Long getOutputDateFrom() {
		return outputDateFrom;
	}
	public void setOutputDateTo(Long outputDateTo) {
		this.outputDateTo = outputDateTo;
	}
	public Long getOutputDateTo() {
		return outputDateTo;
	}
	public void setGenerationDateFrom(Long generationDateFrom) {
		this.generationDateFrom = generationDateFrom;
	}
	public Long getGenerationDateFrom() {
		return generationDateFrom;
	}
	public void setGenerationDateTo(Long generationDateTo) {
		this.generationDateTo = generationDateTo;
	}
	public Long getGenerationDateTo() {
		return generationDateTo;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}
	public String getMonitorDetailId() {
		return monitorDetailId;
	}
	public void setFacilityType(Integer facilityType) {
		this.facilityType = facilityType;
	}
	public Integer getFacilityType() {
		return facilityType;
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
	public void setConfirmFlgTypeList(Integer[] confirmFlgTypeList) {
		this.confirmFlgTypeList = confirmFlgTypeList;
	}
	public Integer[] getConfirmFlgTypeList() {
		return confirmFlgTypeList;
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
	public void setConfirmedUser(String confirmedUser) {
		this.confirmedUser = confirmedUser;
	}
	public String getConfirmedUser() {
		return confirmedUser;
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
	public void setOwnerRoleId(String ownerRoleId){
		this.ownerRoleId = ownerRoleId;
	}
	public String getOwnerRoleId(){
		return ownerRoleId;
	}
	public void setPriorityList(Integer[] priorityList) {
		this.priorityList = priorityList;
	}
	public Integer[] getPriorityList() {
		return priorityList;
	}
	public Boolean getCollectGraphFlg() {
		return collectGraphFlg;
	}
	public void setCollectGraphFlg(Boolean collectGraphFlg) {
		this.collectGraphFlg = collectGraphFlg;
	}
	public Long getPositionFrom() {
		return positionFrom;
	}
	public void setPositionFrom(Long positionFrom) {
		this.positionFrom = positionFrom;
	}
	public Long getPositionTo() {
		return positionTo;
	}
	public void setPositionTo(Long positionTo) {
		this.positionTo = positionTo;
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
	public List<EventDataInfo> getSelectedEventList() {
		return selectedEventList;
	}
	public void setSelectedEventList(List<EventDataInfo> selectedEventList) {
		this.selectedEventList = selectedEventList;
	}
	
}
