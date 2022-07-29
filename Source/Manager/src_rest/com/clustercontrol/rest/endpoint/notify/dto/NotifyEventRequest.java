/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.EventNormalStateEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyPriorityEnum;
import com.clustercontrol.util.MessageConstant;

public class NotifyEventRequest implements RequestDto {

	@RestItemName(value = MessageConstant.MONITOR_ID)
	private String monitorId;

	private String monitorDetail;

	@RestItemName(value = MessageConstant.PLUGIN_ID)
	private String pluginId;

	@RestBeanConvertDatetime
	private String generationDate;

	@RestItemName(value = MessageConstant.FACILITY_ID)
	private String facilityId;

	@RestItemName(value = MessageConstant.SCOPE)
	private String scopeText;

	@RestItemName(value = MessageConstant.APPLICATION)
	private String application;

	@RestItemName(value = MessageConstant.MESSAGE)
	private String message;

	@RestItemName(value = MessageConstant.MESSAGE_ORG)
	private String messageOrg;

	@RestItemName(value = MessageConstant.PRIORITY)
	@RestBeanConvertEnum
	private NotifyPriorityEnum priority;

	@RestBeanConvertEnum
	private EventNormalStateEnum confirmFlg;

	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	private String ownerRoleId;

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

	public NotifyEventRequest() {
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorDetail() {
		return monitorDetail;
	}

	public void setMonitorDetail(String monitorDetail) {
		this.monitorDetail = monitorDetail;
	}

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(String generationDate) {
		this.generationDate = generationDate;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getScopeText() {
		return scopeText;
	}

	public void setScopeText(String scopeText) {
		this.scopeText = scopeText;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageOrg() {
		return messageOrg;
	}

	public void setMessageOrg(String messageOrg) {
		this.messageOrg = messageOrg;
	}

	public NotifyPriorityEnum getPriority() {
		return priority;
	}

	public void setPriority(NotifyPriorityEnum priority) {
		this.priority = priority;
	}

	public EventNormalStateEnum getConfirmFlg() {
		return confirmFlg;
	}

	public void setConfirmFlg(EventNormalStateEnum confirmFlg) {
		this.confirmFlg = confirmFlg;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
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
	public void correlationCheck() throws InvalidSetting {
	}
}
