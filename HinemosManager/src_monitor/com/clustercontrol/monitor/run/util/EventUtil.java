/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * イベントに関するUtilityクラス<br/>
 *
 */
public class EventUtil {

	/** イベント情報キー */
	private static final String _KEY_PRIORITY = "PRIORITY";
	private static final String _KEY_PRIORITY_NUM = "PRIORITY_NUM";
	private static final String _KEY_PRIORITY_JP = "PRIORITY_JP";
	private static final String _KEY_PRIORITY_EN = "PRIORITY_EN";
	private static final String _KEY_PLUGIN_ID = "PLUGIN_ID";
	private static final String _KEY_PLUGIN_NAME = "PLUGIN_NAME";
	private static final String _KEY_MONITOR_ID = "MONITOR_ID";
	private static final String _KEY_MONITOR_DETAIL_ID = "MONITOR_DETAIL_ID";
	private static final String _KEY_MONITOR_OWNER_ROLE_ID = "MONITOR_OWNER_ROLE_ID";
	private static final String _KEY_FACILITY_ID = "FACILITY_ID";
	private static final String _KEY_SCOPE = "SCOPE";
	private static final String _KEY_GENERATION_DATE = "GENERATION_DATE";
	private static final String _KEY_APPLICATION = "APPLICATION";
	private static final String _KEY_MESSAGE = "MESSAGE";
	private static final String _KEY_ORG_MESSAGE = "ORG_MESSAGE";
	private static final String _KEY_EVENT_NO = "EVENT_NO";
	private static final String _KEY_CONFIRM = "CONFIRM";
	private static final String _KEY_CONFIRM_NUM = "CONFIRM_NUM";
	private static final String _KEY_CONFIRM_JP = "CONFIRM_JP";
	private static final String _KEY_CONFIRM_EN = "CONFIRM_EN";
	private static final String _KEY_CONFIRM_DATE = "CONFIRM_DATE";
	private static final String _KEY_CONFIRM_USER = "CONFIRM_USER";
	private static final String _KEY_OUTPUT_DATE = "OUTPUT_DATE";
	private static final String _KEY_DUPLICATETION_COUNT = "DUPLICATETION_COUNT";
	private static final String _KEY_COMMENT = "COMMENT";
	private static final String _KEY_COMMENT_DATE = "COMMENT_DATE";
	private static final String _KEY_COMMENT_USER = "COMMENT_USER";
	private static final String _KEY_COLLECT_GRAPH_FLG = "COLLECT_GRAPH_FLG";
	private static final String _KEY_USER_ITEM_FORMAT = "USER_ITEM%02d";
	private static final String _KEY_NOTIFY_UUID = "NOTIFY_UUID";
	

	/**
	 * イベント情報をハッシュとして返す。
	 * @param outputInfo 通知情報
	 * @return 通知情報のハッシュ
	 */
	public static Map<String, String> createParameter(EventLogEntity event, SimpleDateFormat sdf) {
		Map<String, String> ret = new HashMap<>();
		addParameter(ret, event, sdf);
		return ret;
	}
	
	/**
	 * 引数のハッシュにイベント情報の項目を追加する
	 * 
	 * @param base
	 * @param event
	 * @param sdf
	 */
	public static void addParameter(Map<String, String> base, EventLogEntity event, SimpleDateFormat sdf) {
		if (event == null) {
			return;
		}
		Locale locale = Locale.getDefault();
		
		base.put(_KEY_PRIORITY_NUM, objectToString(event.getPriority()));
		String priorityMessageCode = PriorityConstant.typeToMessageCode(event.getPriority());
		
		base.put(_KEY_PRIORITY, Messages.getString(priorityMessageCode, locale));
		base.put(_KEY_PRIORITY_JP, Messages.getString(priorityMessageCode, Locale.JAPANESE));
		base.put(_KEY_PRIORITY_EN, Messages.getString(priorityMessageCode, Locale.ENGLISH));
		
		base.put(_KEY_PLUGIN_ID, event.getId().getPluginId());
		base.put(_KEY_PLUGIN_NAME, Messages.getString(HinemosModuleConstant.nameToMessageCode(event.getId().getPluginId()), locale));
		
		base.put(_KEY_MONITOR_ID, event.getId().getMonitorId());
		base.put(_KEY_MONITOR_DETAIL_ID, event.getId().getMonitorDetailId());
		base.put(_KEY_MONITOR_OWNER_ROLE_ID, event.getOwnerRoleId());
		base.put(_KEY_FACILITY_ID, event.getId().getFacilityId());
		base.put(_KEY_SCOPE, event.getScopeText());
		base.put(_KEY_GENERATION_DATE, dateToString(event.getGenerationDate(), sdf)); 
		
		base.put(_KEY_APPLICATION, HinemosMessage.replace(event.getApplication(), locale));
		base.put(_KEY_MESSAGE, HinemosMessage.replace(event.getMessage(), locale));
		base.put(_KEY_ORG_MESSAGE, HinemosMessage.replace(event.getMessageOrg(), locale));
		
		base.put(_KEY_EVENT_NO, objectToString(event.getPosition()));
		
		base.put(_KEY_CONFIRM_NUM, objectToString(event.getConfirmFlg()));
		String confirmMessageCode = ConfirmConstant.typeToMessageCode(event.getConfirmFlg());
		base.put(_KEY_CONFIRM, Messages.getString(confirmMessageCode, locale));
		base.put(_KEY_CONFIRM_JP, Messages.getString(confirmMessageCode, Locale.JAPANESE));
		base.put(_KEY_CONFIRM_EN, Messages.getString(confirmMessageCode, Locale.ENGLISH));
		
		base.put(_KEY_CONFIRM_DATE, dateToString(event.getConfirmDate(), sdf));
		base.put(_KEY_CONFIRM_USER, event.getConfirmUser());
		base.put(_KEY_OUTPUT_DATE, dateToString(event.getId().getOutputDate(), sdf));
		base.put(_KEY_DUPLICATETION_COUNT, objectToString(event.getDuplicationCount()));
		base.put(_KEY_COMMENT, event.getComment());
		base.put(_KEY_COMMENT_DATE, dateToString(event.getCommentDate(), sdf));
		base.put(_KEY_COMMENT_USER, event.getCommentUser());
		base.put(_KEY_COLLECT_GRAPH_FLG, objectToString(event.getCollectGraphFlg()));
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			base.put(String.format(_KEY_USER_ITEM_FORMAT, i), getUserItemValue(event, i));
		}
		base.put(_KEY_NOTIFY_UUID, event.getNotifyUUID());
	}
	
	public static void copyEventLogEntityToEventDataInfo(EventLogEntity event, EventDataInfo eventInfo) {
		eventInfo.setPriority(event.getPriority());
		if (event.getId().getOutputDate() != null) {
			eventInfo.setOutputDate(event.getId().getOutputDate());
		}
		if (event.getGenerationDate() != null) {
			eventInfo.setGenerationDate(event.getGenerationDate());
			eventInfo.setPredictGenerationDate(event.getGenerationDate());
		}
		eventInfo.setPluginId(event.getId().getPluginId());
		eventInfo.setMonitorId(event.getId().getMonitorId());
		eventInfo.setMonitorDetailId(event.getId().getMonitorDetailId());
		eventInfo.setParentMonitorDetailId(event.getId().getMonitorDetailId());
		eventInfo.setFacilityId(event.getId().getFacilityId());
		eventInfo.setScopeText(event.getScopeText());
		eventInfo.setApplication(event.getApplication());
		eventInfo.setMessage(event.getMessage());
		eventInfo.setMessageOrg(event.getMessageOrg());
		eventInfo.setConfirmed(event.getConfirmFlg());
		eventInfo.setConfirmUser(event.getConfirmUser());
		eventInfo.setConfirmDate(event.getConfirmDate());
		eventInfo.setComment(event.getComment());
		if (event.getCommentDate() != null ) {
			eventInfo.setCommentDate(event.getCommentDate());
		}
		eventInfo.setCommentUser(event.getCommentUser());
		eventInfo.setCollectGraphFlg(event.getCollectGraphFlg());
		eventInfo.setOwnerRoleId(event.getOwnerRoleId());
		eventInfo.setPosition(event.getPosition());
		eventInfo.setNotifyUUID(event.getNotifyUUID());
		
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			EventUtil.setUserItemValue(eventInfo, i, EventUtil.getUserItemValue(event, i));
		}
	}
	
	private static String dateToString(Long value, SimpleDateFormat sdf) {
		if (value == null) {
			return null;
		}
		return sdf.format(value);
	}
	
	private static String objectToString(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		}
		return String.valueOf(value);
	}
	
	public static void setUserItemValue(EventLogEntity event, int index, String value) {
		switch (index) {
		case 1:
			event.setUserItem01(value);
			break;
		
		case 2:
			event.setUserItem02(value);
			break;
		
		case 3:
			event.setUserItem03(value);
			break;
		
		case 4:
			event.setUserItem04(value);
			break;
		
		case 5:
			event.setUserItem05(value);
			break;
		
		case 6:
			event.setUserItem06(value);
			break;
		
		case 7:
			event.setUserItem07(value);
			break;
		
		case 8:
			event.setUserItem08(value);
			break;
		
		case 9:
			event.setUserItem09(value);
			break;
		
		case 10:
			event.setUserItem10(value);
			break;
		
		case 11:
			event.setUserItem11(value);
			break;
		
		case 12:
			event.setUserItem12(value);
			break;
		
		case 13:
			event.setUserItem13(value);
			break;
		
		case 14:
			event.setUserItem14(value);
			break;
		
		case 15:
			event.setUserItem15(value);
			break;
		
		case 16:
			event.setUserItem16(value);
			break;
		
		case 17:
			event.setUserItem17(value);
			break;
		
		case 18:
			event.setUserItem18(value);
			break;
		
		case 19:
			event.setUserItem19(value);
			break;
		
		case 20:
			event.setUserItem20(value);
			break;
		
		case 21:
			event.setUserItem21(value);
			break;
		
		case 22:
			event.setUserItem22(value);
			break;
		
		case 23:
			event.setUserItem23(value);
			break;
		
		case 24:
			event.setUserItem24(value);
			break;
		
		case 25:
			event.setUserItem25(value);
			break;
		
		case 26:
			event.setUserItem26(value);
			break;
		
		case 27:
			event.setUserItem27(value);
			break;
		
		case 28:
			event.setUserItem28(value);
			break;
		
		case 29:
			event.setUserItem29(value);
			break;
		
		case 30:
			event.setUserItem30(value);
			break;
		
		case 31:
			event.setUserItem31(value);
			break;
		
		case 32:
			event.setUserItem32(value);
			break;
		
		case 33:
			event.setUserItem33(value);
			break;
		
		case 34:
			event.setUserItem34(value);
			break;
		
		case 35:
			event.setUserItem35(value);
			break;
		
		case 36:
			event.setUserItem36(value);
			break;
		
		case 37:
			event.setUserItem37(value);
			break;
		
		case 38:
			event.setUserItem38(value);
			break;
		
		case 39:
			event.setUserItem39(value);
			break;
		
		case 40:
			event.setUserItem40(value);
			break;
			
		default:
			break;
		}
	}
	
	public static String getUserItemValue(EventLogEntity event, int index) {
		switch (index) {
		case 1:
			return event.getUserItem01();
		case 2:
			return event.getUserItem02();
		case 3:
			return event.getUserItem03();
		case 4:
			return event.getUserItem04();
		case 5:
			return event.getUserItem05();
		case 6:
			return event.getUserItem06();
		case 7:
			return event.getUserItem07();
		case 8:
			return event.getUserItem08();
		case 9:
			return event.getUserItem09();
		case 10:
			return event.getUserItem10();
		case 11:
			return event.getUserItem11();
		case 12:
			return event.getUserItem12();
		case 13:
			return event.getUserItem13();
		case 14:
			return event.getUserItem14();
		case 15:
			return event.getUserItem15();
		case 16:
			return event.getUserItem16();
		case 17:
			return event.getUserItem17();
		case 18:
			return event.getUserItem18();
		case 19:
			return event.getUserItem19();
		case 20:
			return event.getUserItem20();
		case 21:
			return event.getUserItem21();
		case 22:
			return event.getUserItem22();
		case 23:
			return event.getUserItem23();
		case 24:
			return event.getUserItem24();
		case 25:
			return event.getUserItem25();
		case 26:
			return event.getUserItem26();
		case 27:
			return event.getUserItem27();
		case 28:
			return event.getUserItem28();
		case 29:
			return event.getUserItem29();
		case 30:
			return event.getUserItem30();
		case 31:
			return event.getUserItem31();
		case 32:
			return event.getUserItem32();
		case 33:
			return event.getUserItem33();
		case 34:
			return event.getUserItem34();
		case 35:
			return event.getUserItem35();
		case 36:
			return event.getUserItem36();
		case 37:
			return event.getUserItem37();
		case 38:
			return event.getUserItem38();
		case 39:
			return event.getUserItem39();
		case 40:
			return event.getUserItem40();
		default:
			break;
		}
		return null;
	}
	
	public static void setUserItemValue(EventDataInfo event, int index, String value) {
		switch (index) {
		case 1:
			event.setUserItem01(value);
			break;
		
		case 2:
			event.setUserItem02(value);
			break;
		
		case 3:
			event.setUserItem03(value);
			break;
		
		case 4:
			event.setUserItem04(value);
			break;
		
		case 5:
			event.setUserItem05(value);
			break;
		
		case 6:
			event.setUserItem06(value);
			break;
		
		case 7:
			event.setUserItem07(value);
			break;
		
		case 8:
			event.setUserItem08(value);
			break;
		
		case 9:
			event.setUserItem09(value);
			break;
		
		case 10:
			event.setUserItem10(value);
			break;
		
		case 11:
			event.setUserItem11(value);
			break;
		
		case 12:
			event.setUserItem12(value);
			break;
		
		case 13:
			event.setUserItem13(value);
			break;
		
		case 14:
			event.setUserItem14(value);
			break;
		
		case 15:
			event.setUserItem15(value);
			break;
		
		case 16:
			event.setUserItem16(value);
			break;
		
		case 17:
			event.setUserItem17(value);
			break;
		
		case 18:
			event.setUserItem18(value);
			break;
		
		case 19:
			event.setUserItem19(value);
			break;
		
		case 20:
			event.setUserItem20(value);
			break;
		
		case 21:
			event.setUserItem21(value);
			break;
		
		case 22:
			event.setUserItem22(value);
			break;
		
		case 23:
			event.setUserItem23(value);
			break;
		
		case 24:
			event.setUserItem24(value);
			break;
		
		case 25:
			event.setUserItem25(value);
			break;
		
		case 26:
			event.setUserItem26(value);
			break;
		
		case 27:
			event.setUserItem27(value);
			break;
		
		case 28:
			event.setUserItem28(value);
			break;
		
		case 29:
			event.setUserItem29(value);
			break;
		
		case 30:
			event.setUserItem30(value);
			break;
		
		case 31:
			event.setUserItem31(value);
			break;
		
		case 32:
			event.setUserItem32(value);
			break;
		
		case 33:
			event.setUserItem33(value);
			break;
		
		case 34:
			event.setUserItem34(value);
			break;
		
		case 35:
			event.setUserItem35(value);
			break;
		
		case 36:
			event.setUserItem36(value);
			break;
		
		case 37:
			event.setUserItem37(value);
			break;
		
		case 38:
			event.setUserItem38(value);
			break;
		
		case 39:
			event.setUserItem39(value);
			break;
		
		case 40:
			event.setUserItem40(value);
			break;
			
		default:
			break;
		}
	}
	
	public static String getUserItemValue(EventDataInfo event, int index) {
		switch (index) {
		case 1:
			return event.getUserItem01();
		case 2:
			return event.getUserItem02();
		case 3:
			return event.getUserItem03();
		case 4:
			return event.getUserItem04();
		case 5:
			return event.getUserItem05();
		case 6:
			return event.getUserItem06();
		case 7:
			return event.getUserItem07();
		case 8:
			return event.getUserItem08();
		case 9:
			return event.getUserItem09();
		case 10:
			return event.getUserItem10();
		case 11:
			return event.getUserItem11();
		case 12:
			return event.getUserItem12();
		case 13:
			return event.getUserItem13();
		case 14:
			return event.getUserItem14();
		case 15:
			return event.getUserItem15();
		case 16:
			return event.getUserItem16();
		case 17:
			return event.getUserItem17();
		case 18:
			return event.getUserItem18();
		case 19:
			return event.getUserItem19();
		case 20:
			return event.getUserItem20();
		case 21:
			return event.getUserItem21();
		case 22:
			return event.getUserItem22();
		case 23:
			return event.getUserItem23();
		case 24:
			return event.getUserItem24();
		case 25:
			return event.getUserItem25();
		case 26:
			return event.getUserItem26();
		case 27:
			return event.getUserItem27();
		case 28:
			return event.getUserItem28();
		case 29:
			return event.getUserItem29();
		case 30:
			return event.getUserItem30();
		case 31:
			return event.getUserItem31();
		case 32:
			return event.getUserItem32();
		case 33:
			return event.getUserItem33();
		case 34:
			return event.getUserItem34();
		case 35:
			return event.getUserItem35();
		case 36:
			return event.getUserItem36();
		case 37:
			return event.getUserItem37();
		case 38:
			return event.getUserItem38();
		case 39:
			return event.getUserItem39();
		case 40:
			return event.getUserItem40();
		default:
			break;
		}
		return null;
	}
}