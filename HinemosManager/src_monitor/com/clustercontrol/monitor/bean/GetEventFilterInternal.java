/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.run.util.EventUtil;
import com.clustercontrol.repository.bean.FacilityTargetConstant;

/**
 * マネージャ内部で使用するイベントの取得用フィルタ条件
 *
 */
public class GetEventFilterInternal extends EventFilterInternal<EventFilterInfo> {
	
	private List<Integer> confirmFlgList;
	private String confirmUser;
	private String ownerRoleId;
	private Long positionFrom;
	private Long positionTo;
	private String userItem01;
	private String userItem02;
	private String userItem03;
	private String userItem04;
	private String userItem05;
	private String userItem06;
	private String userItem07;
	private String userItem08;
	private String userItem09;
	private String userItem10;
	private String userItem11;
	private String userItem12;
	private String userItem13;
	private String userItem14;
	private String userItem15;
	private String userItem16;
	private String userItem17;
	private String userItem18;
	private String userItem19;
	private String userItem20;
	private String userItem21;
	private String userItem22;
	private String userItem23;
	private String userItem24;
	private String userItem25;
	private String userItem26;
	private String userItem27;
	private String userItem28;
	private String userItem29;
	private String userItem30;
	private String userItem31;
	private String userItem32;
	private String userItem33;
	private String userItem34;
	private String userItem35;
	private String userItem36;
	private String userItem37;
	private String userItem38;
	private String userItem39;
	private String userItem40;
	
	
	public GetEventFilterInternal() {
	}
	
	public void setFilterDefaultGetEventList(String facilityId) throws HinemosUnknown {
		
		this.setFacilityType(FacilityTargetConstant.TYPE_ALL);
		List<Integer> confirmList = new ArrayList<>();
		confirmList.add(ConfirmConstant.TYPE_UNCONFIRMED);
		confirmList.add(ConfirmConstant.TYPE_CONFIRMING);
		this.setConfirmFlgList(confirmList);
		this.setCollectGraphFlg(CollectGraphFlgConstant.TYPE_ALL);
		
		// ファシリティID 設定
		setFacilityIdList(getFacilityIds(facilityId, this.getFacilityType()));
	}
	
	public void setFilterDefaultGetEventFile(String facilityId) throws HinemosUnknown {
		
		this.setFacilityType(FacilityTargetConstant.TYPE_BENEATH);
		this.setCollectGraphFlg(CollectGraphFlgConstant.TYPE_ALL);
		this.setCollectGraphFlg(CollectGraphFlgConstant.TYPE_ALL);
		
		// ファシリティID 設定
		setFacilityIdList(getFacilityIds(facilityId, this.getFacilityType()));
	}
	
	@Override
	public void setFilter(String facilityId, EventFilterInfo filter) throws HinemosUnknown {
		
		setFilterCommon(filter);
		setFilterGetEvent(filter);
		
		// ファシリティID 設定
		setFacilityIdList(getFacilityIds(facilityId, this.getFacilityType()));
		
	}
	
	private void setFilterCommon(EventFilterInfo filter) throws HinemosUnknown {
		// UpdateEventFilterInternalとの共通部分 - START
		// 本来は同一ロジックとすべきだが、EventFilterInfoとEventBatchConfirmInfoで
		// 一部の変数名(OutputFromDate、OutputDateFromなど)が異なるため、
		// 別ロジックとする
		
		//重要度　設定
		if (filter.getPriorityList() != null && filter.getPriorityList().length > 0) {
			setPriorityList(Arrays.asList(filter.getPriorityList()));
		} else {
			setPriorityList(null);
		}
		
		//更新日時（自）　設定
		setOutputFromDate(convFromDate(filter.getOutputDateFrom()));
		
		//更新日時（至）　設定
		setOutputToDate(convToDate(filter.getOutputDateTo()));

		//出力日時（自）　設定
		setGenerationFromDate(convFromDate(filter.getGenerationDateFrom()));

		//出力日時（至）　設定
		setGenerationToDate(convToDate(filter.getGenerationDateTo()));

		//監視項目ID　設定
		setMonitorId(convEmptyToNull(filter.getMonitorId()));
		
		//監視詳細　設定
		setMonitorDetailId(convEmptyToNull(filter.getMonitorDetailId()));
		
		//対象ファシリティ種別　設定
		setFacilityType(filter.getFacilityType());
		
		//アプリケーション　設定
		setApplication(convEmptyToNull(filter.getApplication()));
		
		//メッセージ　設定
		setMessage(convEmptyToNull(filter.getMessage()));
		
		// コメント　設定
		setComment(convEmptyToNull(filter.getComment()));
		// コメントユーザ 設定
		setCommentUser(convEmptyToNull(filter.getCommentUser()));
		
		// 性能グラフ用フラグ　設定
		setCollectGraphFlg(filter.getCollectGraphFlg());
		
		// UpdateEventFilterInternalとの共通部分 - END
	}
	
	private void setFilterGetEvent(EventFilterInfo filter) {
		
		//確認状態　設定
		if (filter.getConfirmFlgTypeList() != null && filter.getConfirmFlgTypeList().length > 0 ) {
			setConfirmFlgList(Arrays.asList(filter.getConfirmFlgTypeList()));
		} else {
			setConfirmFlgList(null);
		}
		
		//確認ユーザ　設定
		setConfirmUser(convEmptyToNull(filter.getConfirmedUser()));
		// オーナーロールID
		setOwnerRoleId(convEmptyToNull(filter.getOwnerRoleId()));
		// イベント番号（自）
		setPositionFrom(filter.getPositionFrom());
		// イベント番号（至）
		setPositionTo(filter.getPositionTo());
		
		//ユーザ項目
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			String value = convEmptyToNull(EventUtil.getUserItemValue(filter, i));
			EventUtil.setUserItemValue(this, i, value);
		}
	}
	
	public List<Integer> getConfirmFlgList() {
		return confirmFlgList;
	}
	public void setConfirmFlgList(List<Integer> confirmFlgList) {
		this.confirmFlgList = confirmFlgList;
	}
	public String getConfirmUser() {
		return confirmUser;
	}
	public void setConfirmUser(String confirmUser) {
		this.confirmUser = confirmUser;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((confirmFlgList == null) ? 0 : confirmFlgList.hashCode());
		result = prime * result + ((confirmUser == null) ? 0 : confirmUser.hashCode());
		result = prime * result + ((ownerRoleId == null) ? 0 : ownerRoleId.hashCode());
		result = prime * result + ((positionFrom == null) ? 0 : positionFrom.hashCode());
		result = prime * result + ((positionTo == null) ? 0 : positionTo.hashCode());
		for (int i = 1 ; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			String value = EventUtil.getUserItemValue(this, i);
			result = prime * result + ((value == null) ? 0 : value.hashCode());
		}
		
		return result;
	}
	
	@Override
	public boolean equals(Object obj) { 
		if (!super.equals(obj)) {return false;}
		GetEventFilterInternal other = (GetEventFilterInternal) obj;
		if (!isEquals(confirmFlgList, other.confirmFlgList)) {return false;}
		if (!isEquals(confirmUser, other.confirmUser)) {return false;}
		if (!isEquals(ownerRoleId, other.ownerRoleId)) {return false;}
		if (!isEquals(positionFrom, other.positionFrom)) {return false;}
		if (!isEquals(positionTo, other.positionTo)) {return false;}
		for (int i = 1 ; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			String thisValue = EventUtil.getUserItemValue(this, i);
			String otherValue = EventUtil.getUserItemValue(other, i);
			if (!isEquals(thisValue, otherValue)) {
				return false;
			}
		}
		
		return true;
	}
}
