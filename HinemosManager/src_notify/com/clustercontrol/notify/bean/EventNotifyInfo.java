/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;


import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.notify.util.NotifyUtil;



/**
 * イベント通知情報を保持するクラス<BR>
 *
 */
@XmlType(namespace = "http://notify.ws.clustercontrol.com")
public class EventNotifyInfo implements java.io.Serializable, Cloneable {
	
	private static final long serialVersionUID = -8071515648593803189L;
	/**
	 * 監視項目ID
	 */
	private String m_monitorId;

	/**
	 * 監視詳細
	 */
	private String m_monitorDetail;
	
	/**
	 * プラグインID
	 */
	private String m_pluginId;

	/**
	 * 出力日時
	 */
	private Long m_generationDate;
	
	/**
	 * ファシリティID
	 */
	private String m_facilityId;
	
	/**
	 * スコープ
	 */
	private String m_scopeText;

	/**
	 * アプリケーション
	 */
	private String m_application;

	/**
	 * メッセージ
	 */
	private String m_message;

	/**
	 * オリジナルメッセージ
	 */
	private String m_messageOrg;
	
	/**
	 * 重要度。
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	private int m_priority;

	/**
	 * 確認。
	 * @see com.clustercontrol.monitor.bean.ConfirmConstant
	 */
	private int m_confirmFlg;
	
	/**
	 * オーナーロールID
	 */
	private String m_ownerRoleId;
	
	/**
	 * ユーザ項目01
	 */
	private String m_userItem01;
	
	/**
	 * ユーザ項目02
	 */
	private String m_userItem02;
	
	/**
	 * ユーザ項目03
	 */
	private String m_userItem03;
	
	/**
	 * ユーザ項目04
	 */
	private String m_userItem04;
	
	/**
	 * ユーザ項目05
	 */
	private String m_userItem05;
	
	/**
	 * ユーザ項目06
	 */
	private String m_userItem06;
	
	/**
	 * ユーザ項目07
	 */
	private String m_userItem07;
	
	/**
	 * ユーザ項目08
	 */
	private String m_userItem08;
	
	/**
	 * ユーザ項目09
	 */
	private String m_userItem09;
	
	/**
	 * ユーザ項目10
	 */
	private String m_userItem10;
	
	/**
	 * ユーザ項目11
	 */
	private String m_userItem11;
	
	/**
	 * ユーザ項目12
	 */
	private String m_userItem12;
	
	/**
	 * ユーザ項目13
	 */
	private String m_userItem13;
	
	/**
	 * ユーザ項目14
	 */
	private String m_userItem14;
	
	/**
	 * ユーザ項目15
	 */
	private String m_userItem15;
	
	/**
	 * ユーザ項目16
	 */
	private String m_userItem16;
	
	/**
	 * ユーザ項目17
	 */
	private String m_userItem17;
	
	/**
	 * ユーザ項目18
	 */
	private String m_userItem18;
	
	/**
	 * ユーザ項目19
	 */
	private String m_userItem19;
	
	/**
	 * ユーザ項目20
	 */
	private String m_userItem20;
	
	/**
	 * ユーザ項目21
	 */
	private String m_userItem21;
	
	/**
	 * ユーザ項目22
	 */
	private String m_userItem22;
	
	/**
	 * ユーザ項目23
	 */
	private String m_userItem23;
	
	/**
	 * ユーザ項目24
	 */
	private String m_userItem24;
	
	/**
	 * ユーザ項目25
	 */
	private String m_userItem25;
	
	/**
	 * ユーザ項目26
	 */
	private String m_userItem26;
	
	/**
	 * ユーザ項目27
	 */
	private String m_userItem27;
	
	/**
	 * ユーザ項目28
	 */
	private String m_userItem28;
	
	/**
	 * ユーザ項目29
	 */
	private String m_userItem29;
	
	/**
	 * ユーザ項目30
	 */
	private String m_userItem30;
	
	/**
	 * ユーザ項目31
	 */
	private String m_userItem31;
	
	/**
	 * ユーザ項目32
	 */
	private String m_userItem32;
	
	/**
	 * ユーザ項目33
	 */
	private String m_userItem33;
	
	/**
	 * ユーザ項目34
	 */
	private String m_userItem34;
	
	/**
	 * ユーザ項目35
	 */
	private String m_userItem35;
	
	/**
	 * ユーザ項目36
	 */
	private String m_userItem36;
	
	/**
	 * ユーザ項目37
	 */
	private String m_userItem37;
	
	/**
	 * ユーザ項目38
	 */
	private String m_userItem38;
	
	/**
	 * ユーザ項目39
	 */
	private String m_userItem39;
	
	/**
	 * ユーザ項目40
	 */
	private String m_userItem40;
	
	
	public EventNotifyInfo() {}

	/**
	 * アプリケーションを返します。
	 * 
	 * @return アプリケーション
	 */
	public String getApplication() {
		return m_application;
	}
	/**
	 * アプリケーションを設定します。
	 * 
	 * @param application アプリケーション
	 */
	public void setApplication(String application) {
		this.m_application = application;
	}
	/**
	 * ファシリティIDを返します。
	 * 
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return m_facilityId;
	}
	/**
	 * ファシリティIDを設定します。
	 * 
	 * @param id ファシリティID
	 */
	public void setFacilityId(String id) {
		m_facilityId = id;
	}
	/**
	 * 出力日時を返します。
	 * 
	 * @return 出力日時
	 */
	public Long getGenerationDate() {
		return m_generationDate;
	}
	/**
	 * 出力日時を設定します。
	 * 
	 * @param date 出力日時
	 */
	public void setGenerationDate(Long date) {
		m_generationDate = date;
	}
	/**
	 * メッセージを返します。
	 * 
	 * @return メッセージ
	 */
	public String getMessage() {
		return m_message;
	}
	/**
	 * メッセージを設定します。
	 * 
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
		this.m_message = message;
	}
	/**
	 * オリジナルメッセージを返します。
	 * 
	 * @return オリジナルメッセージ
	 */
	public String getMessageOrg() {
		return m_messageOrg;
	}
	/**
	 * オリジナルメッセージを設定します。
	 * 
	 * @param org オリジナルメッセージ
	 */
	public void setMessageOrg(String org) {
		m_messageOrg = org;
	}
	/**
	 * 監視項目IDを返します。
	 * 
	 * @return 監視項目ID
	 */
	public String getMonitorId() {
		return m_monitorId;
	}
	/**
	 * 監視項目IDを設定します。
	 * 
	 * @param id 監視項目ID
	 */
	public void setMonitorId(String id) {
		m_monitorId = id;
	}
	/**
	 * 監視詳細を返します。
	 * 
	 * @return 監視詳細
	 */
	public String getMonitorDetail() {
		return m_monitorDetail;
	}
	/**
	 * 監視詳細を設定します。
	 * 
	 * @param monitorDetail 監視詳細
	 */
	public void setMonitorDetail(String monitorDetail) {
		m_monitorDetail = monitorDetail;
	}
	/**
	 * プラグインIDを返します。
	 * 
	 * @return プラグインID
	 */
	public String getPluginId() {
		return m_pluginId;
	}
	/**
	 * プラグインIDを設定します。
	 * 
	 * @param id プラグインID
	 */
	public void setPluginId(String id) {
		m_pluginId = id;
	}
	/**
	 * 重要度を返します。
	 * 
	 * @return 重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public int getPriority() {
		return m_priority;
	}
	/**
	 * 重要度を設定します。
	 * 
	 * @param priority 重要度
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public void setPriority(int priority) {
		this.m_priority = priority;
	}
	/**
	 * スコープを返します。
	 * 
	 * @return スコープ
	 */
	public String getScopeText() {
		return m_scopeText;
	}
	/**
	 * スコープを設定します。
	 * 
	 * @param text スコープ
	 */
	public void setScopeText(String text) {
		m_scopeText = text;
	}

	/**
	 * 確認を返します。
	 * 
	 * @return 確認
	 */
	public int getConfirmFlg() {
		return m_confirmFlg;
	}

	/**
	 * 確認を設定します。
	 * 
	 * @param confirmFlg 確認
	 */
	public void setConfirmFlg(int confirmFlg) {
		m_confirmFlg = confirmFlg;
	}
	
	/**
	 * オーナーロールIDを返します。
	 * 
	 * @return オーナーロールID
	 */
	public String getOwnerRoleId() {
		return m_ownerRoleId;
	}

	/**
	 * オーナーロールIDを設定します。
	 * 
	 * @param userItem01 ユーザ項目01
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		m_ownerRoleId = ownerRoleId;
	}
	
	/**
	 * ユーザ項目01を返します。
	 * 
	 * @return ユーザ項目01
	 */
	public String getUserItem01() {
		return m_userItem01;
	}

	/**
	 * ユーザ項目01を設定します。
	 * 
	 * @param userItem01 ユーザ項目01
	 */
	public void setUserItem01(String userItem01) {
		m_userItem01 = userItem01;
	}

	
	/**
	 * ユーザ項目02を返します。
	 * 
	 * @return ユーザ項目02
	 */
	public String getUserItem02() {
		return m_userItem02;
	}

	/**
	 * ユーザ項目02を設定します。
	 * 
	 * @param userItem02 ユーザ項目02
	 */
	public void setUserItem02(String userItem02) {
		m_userItem02 = userItem02;
	}

	
	/**
	 * ユーザ項目03を返します。
	 * 
	 * @return ユーザ項目03
	 */
	public String getUserItem03() {
		return m_userItem03;
	}

	/**
	 * ユーザ項目03を設定します。
	 * 
	 * @param userItem03 ユーザ項目03
	 */
	public void setUserItem03(String userItem03) {
		m_userItem03 = userItem03;
	}

	
	/**
	 * ユーザ項目04を返します。
	 * 
	 * @return ユーザ項目04
	 */
	public String getUserItem04() {
		return m_userItem04;
	}

	/**
	 * ユーザ項目04を設定します。
	 * 
	 * @param userItem04 ユーザ項目04
	 */
	public void setUserItem04(String userItem04) {
		m_userItem04 = userItem04;
	}

	
	/**
	 * ユーザ項目05を返します。
	 * 
	 * @return ユーザ項目05
	 */
	public String getUserItem05() {
		return m_userItem05;
	}

	/**
	 * ユーザ項目05を設定します。
	 * 
	 * @param userItem05 ユーザ項目05
	 */
	public void setUserItem05(String userItem05) {
		m_userItem05 = userItem05;
	}

	
	/**
	 * ユーザ項目06を返します。
	 * 
	 * @return ユーザ項目06
	 */
	public String getUserItem06() {
		return m_userItem06;
	}

	/**
	 * ユーザ項目06を設定します。
	 * 
	 * @param userItem06 ユーザ項目06
	 */
	public void setUserItem06(String userItem06) {
		m_userItem06 = userItem06;
	}

	
	/**
	 * ユーザ項目07を返します。
	 * 
	 * @return ユーザ項目07
	 */
	public String getUserItem07() {
		return m_userItem07;
	}

	/**
	 * ユーザ項目07を設定します。
	 * 
	 * @param userItem07 ユーザ項目07
	 */
	public void setUserItem07(String userItem07) {
		m_userItem07 = userItem07;
	}

	
	/**
	 * ユーザ項目08を返します。
	 * 
	 * @return ユーザ項目08
	 */
	public String getUserItem08() {
		return m_userItem08;
	}

	/**
	 * ユーザ項目08を設定します。
	 * 
	 * @param userItem08 ユーザ項目08
	 */
	public void setUserItem08(String userItem08) {
		m_userItem08 = userItem08;
	}

	
	/**
	 * ユーザ項目09を返します。
	 * 
	 * @return ユーザ項目09
	 */
	public String getUserItem09() {
		return m_userItem09;
	}

	/**
	 * ユーザ項目09を設定します。
	 * 
	 * @param userItem09 ユーザ項目09
	 */
	public void setUserItem09(String userItem09) {
		m_userItem09 = userItem09;
	}

	
	/**
	 * ユーザ項目10を返します。
	 * 
	 * @return ユーザ項目10
	 */
	public String getUserItem10() {
		return m_userItem10;
	}

	/**
	 * ユーザ項目10を設定します。
	 * 
	 * @param userItem10 ユーザ項目10
	 */
	public void setUserItem10(String userItem10) {
		m_userItem10 = userItem10;
	}

	
	/**
	 * ユーザ項目11を返します。
	 * 
	 * @return ユーザ項目11
	 */
	public String getUserItem11() {
		return m_userItem11;
	}

	/**
	 * ユーザ項目11を設定します。
	 * 
	 * @param userItem11 ユーザ項目11
	 */
	public void setUserItem11(String userItem11) {
		m_userItem11 = userItem11;
	}

	
	/**
	 * ユーザ項目12を返します。
	 * 
	 * @return ユーザ項目12
	 */
	public String getUserItem12() {
		return m_userItem12;
	}

	/**
	 * ユーザ項目12を設定します。
	 * 
	 * @param userItem12 ユーザ項目12
	 */
	public void setUserItem12(String userItem12) {
		m_userItem12 = userItem12;
	}

	
	/**
	 * ユーザ項目13を返します。
	 * 
	 * @return ユーザ項目13
	 */
	public String getUserItem13() {
		return m_userItem13;
	}

	/**
	 * ユーザ項目13を設定します。
	 * 
	 * @param userItem13 ユーザ項目13
	 */
	public void setUserItem13(String userItem13) {
		m_userItem13 = userItem13;
	}

	
	/**
	 * ユーザ項目14を返します。
	 * 
	 * @return ユーザ項目14
	 */
	public String getUserItem14() {
		return m_userItem14;
	}

	/**
	 * ユーザ項目14を設定します。
	 * 
	 * @param userItem14 ユーザ項目14
	 */
	public void setUserItem14(String userItem14) {
		m_userItem14 = userItem14;
	}

	
	/**
	 * ユーザ項目15を返します。
	 * 
	 * @return ユーザ項目15
	 */
	public String getUserItem15() {
		return m_userItem15;
	}

	/**
	 * ユーザ項目15を設定します。
	 * 
	 * @param userItem15 ユーザ項目15
	 */
	public void setUserItem15(String userItem15) {
		m_userItem15 = userItem15;
	}

	
	/**
	 * ユーザ項目16を返します。
	 * 
	 * @return ユーザ項目16
	 */
	public String getUserItem16() {
		return m_userItem16;
	}

	/**
	 * ユーザ項目16を設定します。
	 * 
	 * @param userItem16 ユーザ項目16
	 */
	public void setUserItem16(String userItem16) {
		m_userItem16 = userItem16;
	}

	
	/**
	 * ユーザ項目17を返します。
	 * 
	 * @return ユーザ項目17
	 */
	public String getUserItem17() {
		return m_userItem17;
	}

	/**
	 * ユーザ項目17を設定します。
	 * 
	 * @param userItem17 ユーザ項目17
	 */
	public void setUserItem17(String userItem17) {
		m_userItem17 = userItem17;
	}

	
	/**
	 * ユーザ項目18を返します。
	 * 
	 * @return ユーザ項目18
	 */
	public String getUserItem18() {
		return m_userItem18;
	}

	/**
	 * ユーザ項目18を設定します。
	 * 
	 * @param userItem18 ユーザ項目18
	 */
	public void setUserItem18(String userItem18) {
		m_userItem18 = userItem18;
	}

	
	/**
	 * ユーザ項目19を返します。
	 * 
	 * @return ユーザ項目19
	 */
	public String getUserItem19() {
		return m_userItem19;
	}

	/**
	 * ユーザ項目19を設定します。
	 * 
	 * @param userItem19 ユーザ項目19
	 */
	public void setUserItem19(String userItem19) {
		m_userItem19 = userItem19;
	}

	
	/**
	 * ユーザ項目20を返します。
	 * 
	 * @return ユーザ項目20
	 */
	public String getUserItem20() {
		return m_userItem20;
	}

	/**
	 * ユーザ項目20を設定します。
	 * 
	 * @param userItem20 ユーザ項目20
	 */
	public void setUserItem20(String userItem20) {
		m_userItem20 = userItem20;
	}

	
	/**
	 * ユーザ項目21を返します。
	 * 
	 * @return ユーザ項目21
	 */
	public String getUserItem21() {
		return m_userItem21;
	}

	/**
	 * ユーザ項目21を設定します。
	 * 
	 * @param userItem21 ユーザ項目21
	 */
	public void setUserItem21(String userItem21) {
		m_userItem21 = userItem21;
	}

	
	/**
	 * ユーザ項目22を返します。
	 * 
	 * @return ユーザ項目22
	 */
	public String getUserItem22() {
		return m_userItem22;
	}

	/**
	 * ユーザ項目22を設定します。
	 * 
	 * @param userItem22 ユーザ項目22
	 */
	public void setUserItem22(String userItem22) {
		m_userItem22 = userItem22;
	}

	
	/**
	 * ユーザ項目23を返します。
	 * 
	 * @return ユーザ項目23
	 */
	public String getUserItem23() {
		return m_userItem23;
	}

	/**
	 * ユーザ項目23を設定します。
	 * 
	 * @param userItem23 ユーザ項目23
	 */
	public void setUserItem23(String userItem23) {
		m_userItem23 = userItem23;
	}

	
	/**
	 * ユーザ項目24を返します。
	 * 
	 * @return ユーザ項目24
	 */
	public String getUserItem24() {
		return m_userItem24;
	}

	/**
	 * ユーザ項目24を設定します。
	 * 
	 * @param userItem24 ユーザ項目24
	 */
	public void setUserItem24(String userItem24) {
		m_userItem24 = userItem24;
	}

	
	/**
	 * ユーザ項目25を返します。
	 * 
	 * @return ユーザ項目25
	 */
	public String getUserItem25() {
		return m_userItem25;
	}

	/**
	 * ユーザ項目25を設定します。
	 * 
	 * @param userItem25 ユーザ項目25
	 */
	public void setUserItem25(String userItem25) {
		m_userItem25 = userItem25;
	}

	
	/**
	 * ユーザ項目26を返します。
	 * 
	 * @return ユーザ項目26
	 */
	public String getUserItem26() {
		return m_userItem26;
	}

	/**
	 * ユーザ項目26を設定します。
	 * 
	 * @param userItem26 ユーザ項目26
	 */
	public void setUserItem26(String userItem26) {
		m_userItem26 = userItem26;
	}

	
	/**
	 * ユーザ項目27を返します。
	 * 
	 * @return ユーザ項目27
	 */
	public String getUserItem27() {
		return m_userItem27;
	}

	/**
	 * ユーザ項目27を設定します。
	 * 
	 * @param userItem27 ユーザ項目27
	 */
	public void setUserItem27(String userItem27) {
		m_userItem27 = userItem27;
	}

	
	/**
	 * ユーザ項目28を返します。
	 * 
	 * @return ユーザ項目28
	 */
	public String getUserItem28() {
		return m_userItem28;
	}

	/**
	 * ユーザ項目28を設定します。
	 * 
	 * @param userItem28 ユーザ項目28
	 */
	public void setUserItem28(String userItem28) {
		m_userItem28 = userItem28;
	}

	
	/**
	 * ユーザ項目29を返します。
	 * 
	 * @return ユーザ項目29
	 */
	public String getUserItem29() {
		return m_userItem29;
	}

	/**
	 * ユーザ項目29を設定します。
	 * 
	 * @param userItem29 ユーザ項目29
	 */
	public void setUserItem29(String userItem29) {
		m_userItem29 = userItem29;
	}

	
	/**
	 * ユーザ項目30を返します。
	 * 
	 * @return ユーザ項目30
	 */
	public String getUserItem30() {
		return m_userItem30;
	}

	/**
	 * ユーザ項目30を設定します。
	 * 
	 * @param userItem30 ユーザ項目30
	 */
	public void setUserItem30(String userItem30) {
		m_userItem30 = userItem30;
	}

	
	/**
	 * ユーザ項目31を返します。
	 * 
	 * @return ユーザ項目31
	 */
	public String getUserItem31() {
		return m_userItem31;
	}

	/**
	 * ユーザ項目31を設定します。
	 * 
	 * @param userItem31 ユーザ項目31
	 */
	public void setUserItem31(String userItem31) {
		m_userItem31 = userItem31;
	}

	
	/**
	 * ユーザ項目32を返します。
	 * 
	 * @return ユーザ項目32
	 */
	public String getUserItem32() {
		return m_userItem32;
	}

	/**
	 * ユーザ項目32を設定します。
	 * 
	 * @param userItem32 ユーザ項目32
	 */
	public void setUserItem32(String userItem32) {
		m_userItem32 = userItem32;
	}

	
	/**
	 * ユーザ項目33を返します。
	 * 
	 * @return ユーザ項目33
	 */
	public String getUserItem33() {
		return m_userItem33;
	}

	/**
	 * ユーザ項目33を設定します。
	 * 
	 * @param userItem33 ユーザ項目33
	 */
	public void setUserItem33(String userItem33) {
		m_userItem33 = userItem33;
	}

	
	/**
	 * ユーザ項目34を返します。
	 * 
	 * @return ユーザ項目34
	 */
	public String getUserItem34() {
		return m_userItem34;
	}

	/**
	 * ユーザ項目34を設定します。
	 * 
	 * @param userItem34 ユーザ項目34
	 */
	public void setUserItem34(String userItem34) {
		m_userItem34 = userItem34;
	}

	
	/**
	 * ユーザ項目35を返します。
	 * 
	 * @return ユーザ項目35
	 */
	public String getUserItem35() {
		return m_userItem35;
	}

	/**
	 * ユーザ項目35を設定します。
	 * 
	 * @param userItem35 ユーザ項目35
	 */
	public void setUserItem35(String userItem35) {
		m_userItem35 = userItem35;
	}

	
	/**
	 * ユーザ項目36を返します。
	 * 
	 * @return ユーザ項目36
	 */
	public String getUserItem36() {
		return m_userItem36;
	}

	/**
	 * ユーザ項目36を設定します。
	 * 
	 * @param userItem36 ユーザ項目36
	 */
	public void setUserItem36(String userItem36) {
		m_userItem36 = userItem36;
	}

	
	/**
	 * ユーザ項目37を返します。
	 * 
	 * @return ユーザ項目37
	 */
	public String getUserItem37() {
		return m_userItem37;
	}

	/**
	 * ユーザ項目37を設定します。
	 * 
	 * @param userItem37 ユーザ項目37
	 */
	public void setUserItem37(String userItem37) {
		m_userItem37 = userItem37;
	}

	
	/**
	 * ユーザ項目38を返します。
	 * 
	 * @return ユーザ項目38
	 */
	public String getUserItem38() {
		return m_userItem38;
	}

	/**
	 * ユーザ項目38を設定します。
	 * 
	 * @param userItem38 ユーザ項目38
	 */
	public void setUserItem38(String userItem38) {
		m_userItem38 = userItem38;
	}

	
	/**
	 * ユーザ項目39を返します。
	 * 
	 * @return ユーザ項目39
	 */
	public String getUserItem39() {
		return m_userItem39;
	}

	/**
	 * ユーザ項目39を設定します。
	 * 
	 * @param userItem39 ユーザ項目39
	 */
	public void setUserItem39(String userItem39) {
		m_userItem39 = userItem39;
	}

	
	/**
	 * ユーザ項目40を返します。
	 * 
	 * @return ユーザ項目40
	 */
	public String getUserItem40() {
		return m_userItem40;
	}

	/**
	 * ユーザ項目40を設定します。
	 * 
	 * @param userItem40 ユーザ項目40
	 */
	public void setUserItem40(String userItem40) {
		m_userItem40 = userItem40;
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder("EventNotifyInfo : "
				+ "MonitorId = " + m_monitorId
				+ ", MonitorDetail = " + m_monitorDetail
				+ ", PluginId = " + m_pluginId 
				+ ", GenerationDate = " + m_generationDate
				+ ", FacilityId = " + m_facilityId
				+ ", ScopeText = " + m_scopeText
				+ ", Application = " + m_application
				+ ", Message = " + m_message
				+ ", MessageOrg = " + m_messageOrg
				+ ", Priority = " + m_priority
				+ ", ConfirmFlg = " + m_confirmFlg
				+ ", OwnerRoleId = " + m_ownerRoleId
				);
		
		final String userItemStr = ", UserItem%02d = %s";
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			sb.append(String.format(userItemStr, i, NotifyUtil.getUserItemValue(this, i)));
		}
		return sb.toString();
	}
	
	@Override
	public EventNotifyInfo clone(){
		EventNotifyInfo clonedInfo = null;
		try {
			clonedInfo = (EventNotifyInfo) super.clone();
			clonedInfo.setMonitorId(m_monitorId);
			clonedInfo.setMonitorDetail(m_monitorDetail);
			clonedInfo.setPluginId(m_pluginId);
			clonedInfo.setGenerationDate(m_generationDate);
			clonedInfo.setFacilityId(m_facilityId);
			clonedInfo.setScopeText(m_scopeText);
			clonedInfo.setApplication(m_application);
			clonedInfo.setMessage(m_message);
			clonedInfo.setMessageOrg(m_messageOrg);
			clonedInfo.setPriority(m_priority);
			clonedInfo.setConfirmFlg(m_confirmFlg);
			clonedInfo.setOwnerRoleId(m_ownerRoleId);
			for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
				NotifyUtil.setUserItemValue(clonedInfo, i, NotifyUtil.getUserItemValue(this, i));
			}
			
		} catch (CloneNotSupportedException e) {
			// do nothing
		}
		return clonedInfo;
	}
	
	public OutputBasicInfo toOutputBasicInfo(){
		OutputBasicInfo info = new OutputBasicInfo();
		info.setApplication(m_application);
		info.setFacilityId(m_facilityId);
		info.setGenerationDate(m_generationDate);
		info.setMessage(m_message);
		info.setMessageOrg(m_messageOrg);
		info.setMonitorId(m_monitorId);
		info.setPluginId(m_pluginId);
		info.setPriority(m_priority);
		info.setScopeText(m_scopeText);
		info.setSubKey(m_monitorDetail);
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			NotifyUtil.setUserItemValue(info, i, NotifyUtil.getUserItemValue(this, i));
		}
		return info;
	}
}
