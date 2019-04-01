/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventLogHistoryTypeConstant;
import com.clustercontrol.monitor.bean.EventUserExtensionItemInfo;
import com.clustercontrol.monitor.run.util.EventCacheModifyCallback;
import com.clustercontrol.monitor.run.util.EventLogOperationHistoryUtil;
import com.clustercontrol.monitor.run.util.EventUtil;
import com.clustercontrol.monitor.util.EventHinemosPropertyUtil;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;


/**
 * イベント情報を更新するクラス<BR>
 * 更新対象の値は以下
 * ・確認
 * ・コメント
 * ・性能グラフ用フラグ
 * ・ユーザ項目01～40
 *
 */
public class ModifyEventInfo {

	private static final String CONFIRM_FLAG = "CONFIRM_FLAG";
	private static final String COMMENT = "COMMENT";
	private static final String COLLECT_GRAPH_FLAG = "COLLECT_GRAPH_FLAG";
	private static final String USER_ITEM_FORMAT = "USER_ITEM%02d";
	
	/**
	 * 引数で指定されたイベント情報を更新します。<BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 取得したイベント情報の確認フラグを更新します。確認フラグが済の場合は、確認済み日時も更新します。
	 * 
	 * @param info 更新するイベント情報
	 * @throws EventLogNotFound
	 * @throws InvalidRole
	 * 
	 */
	public void modifyEventInfo(EventDataInfo info) throws  EventLogNotFound, InvalidRole  {

		// イベントログ情報を取得
		EventLogEntity event = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			
			try {
				event = QueryUtil.getEventLogPK(
						info.getMonitorId(), info.getMonitorDetailId(), info.getPluginId(),
						info.getOutputDate(), info.getFacilityId(), ObjectPrivilegeMode.MODIFY);
			} catch (EventLogNotFound e) {
				throw e;
			} catch (InvalidRole e) {
				throw e;
			}
			
			List<String> changeItemList = getChangeItemList(event, info);
			if (changeItemList.size() == 0) {
				//値の変更が無いとき
				return;
			}
			
			long now = HinemosTime.currentTimeMillis();
			String user = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			
			if (changeItemList.contains(CONFIRM_FLAG)) {
				setConfirmFlgChange(jtm, event, info.getConfirmed(), now, user);
			}
			
			StringBuilder detail = new StringBuilder();
			
			if (changeItemList.contains(COMMENT)) {
				setCommentChange(event, info.getComment(), now, user, detail);
			}
			
			if (changeItemList.contains(COLLECT_GRAPH_FLAG)) {
				setCollectGraphFlagChange(event, info.getCollectGraphFlg(), detail);
			}
			
			setChangeUserItems(event, info, detail, changeItemList);
			
			if (detail.length() > 0) {
				EventLogOperationHistoryUtil.addEventLogOperationHistory(jtm,
						event, now, user, EventLogHistoryTypeConstant.TYPE_CHANGE_VALUE, detail.toString()
					);
			}
			
			jtm.addCallback(new EventCacheModifyCallback(false, event));
		}
	}
	
	public static boolean setConfirmFlgChange(
			JpaTransactionManager jtm, EventLogEntity event, 
			int confirmType, long confirmDate, String confirmUser) {
		
		Integer befConfirmFlg = event.getConfirmFlg();
		
		if (befConfirmFlg == confirmType) {
			return false;
		}
		
		// 確認を変更
		event.setConfirmFlg(confirmType);
		
		if(confirmType == ConfirmConstant.TYPE_CONFIRMED || 
			confirmType == ConfirmConstant.TYPE_CONFIRMING){
			event.setConfirmDate(confirmDate);
		}
		
		// 確認を実施したユーザを設定
		event.setConfirmUser(confirmUser);
		
		String detail = MessageConstant.MESSAGE_MONITOR_EVENT_CHANGE.getMessage(
				MessageConstant.CONFIRMED.getMessage(),
				MessageConstant.getMessageId(ConfirmConstant.typeToMessageCode(befConfirmFlg)),
						MessageConstant.getMessageId(ConfirmConstant.typeToMessageCode(confirmType))
				);
		
		int historyType = 0;
		
		switch (confirmType) {
		case ConfirmConstant.TYPE_UNCONFIRMED:
			historyType = EventLogHistoryTypeConstant.TYPE_CHANGE_UNCONFIRMED;
			break;
		case ConfirmConstant.TYPE_CONFIRMING:
			historyType = EventLogHistoryTypeConstant.TYPE_CHANGE_CONFIRMING;
			break;
		case ConfirmConstant.TYPE_CONFIRMED:
			historyType = EventLogHistoryTypeConstant.TYPE_CHANGE_CONFIRMED;
			break;
		default:
			break;
		}
		
		EventLogOperationHistoryUtil.addEventLogOperationHistory(
				jtm, event, confirmDate, confirmUser, historyType, detail);
		
		return true;
	}
	
	public static boolean setCommentChange(
			EventLogEntity event, String comment, long commentDate, String commentUser, StringBuilder changeDetail) {
		
		String befComment = event.getComment();
		
		if (comment.equals(befComment)){
			return false;
		}
		
		event.setComment(comment);
		event.setCommentUser(commentUser);
		event.setCommentDate(commentDate);
		
		if (changeDetail.length() != 0) {changeDetail.append("\n");}
		changeDetail.append(
				MessageConstant.MESSAGE_MONITOR_EVENT_CHANGE.getMessage(
						MessageConstant.COMMENT.getMessage(),
						String.valueOf(befComment),
						String.valueOf(comment)
						)
				);
		return true;
	}
	
	public static boolean setCollectGraphFlagChange(
			EventLogEntity event, Boolean collectGraphFlag, StringBuilder changeDetail) {
		
		Boolean befGraphCollectFlg = event.getCollectGraphFlg();
		
		if (collectGraphFlag.equals(befGraphCollectFlg)) {
			return false;
		}
		
		event.setCollectGraphFlg(collectGraphFlag);	
		
		if (changeDetail.length() != 0) {changeDetail.append("\n");}
		changeDetail.append(
				MessageConstant.MESSAGE_MONITOR_EVENT_CHANGE.getMessage(
						MessageConstant.COLLECT_GRAPH_FLG.getMessage(),
						String.valueOf(befGraphCollectFlg),
						String.valueOf(collectGraphFlag)
						)
				);
		return true;
	}
	
	/**
	 * 変更があった項目を抽出する
	 */
	private static List<String> getChangeItemList(EventLogEntity now, EventDataInfo change) {
		List<String> ret = new ArrayList<>();
		
		//nullは更新対象外と判断
		//文字列の場合の空文字は更新対象
		
		if (change.getConfirmed() != null 
				&& !change.getConfirmed().equals(now.getConfirmFlg())) {
			ret.add(CONFIRM_FLAG);
		}
		if (change.getCollectGraphFlg() != null 
				&& !change.getCollectGraphFlg().equals(now.getCollectGraphFlg())) {
			ret.add(COLLECT_GRAPH_FLAG);
		}
		if (change.getComment() != null
				&& !change.getComment().equals(now.getComment())) {
			ret.add(COMMENT);
		}
		
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			if (isChange(EventUtil.getUserItemValue(now , i), EventUtil.getUserItemValue(change , i))) {
				ret.add(String.format(USER_ITEM_FORMAT, i));
			}
		}
		
		return ret;
	}
	
	private static boolean isChange(String befValue, String changeValue) {
		if (changeValue == null) {
			//更新対象ではないので、チェックしない
			return false;
		}
		
		//nullと空文字は同じとみなす　→　初期値はnullとなるため、空文字が登録されることはない
		if ("".equals(befValue)) {
			befValue = null;
		}
		if ("".equals(changeValue)) {
			changeValue = null;
		}
		
		if (befValue == null && changeValue == null) {
			return false;
		}
		if (befValue == null || changeValue == null) {
			//直前のロジックでnullでないことをチェックしているのでいずれかがnullであれば変更あり
			return true;
		}
		return !befValue.equals(changeValue);
	}
	
	private void setChangeUserItems(EventLogEntity event, EventDataInfo info, StringBuilder changeDetail, List<String> changeItemList) {
		
		Map<Integer, EventUserExtensionItemInfo> userItemMap = SelectEventHinemosProperty.getEventUserExtensionItemInfo();
		
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			if (!changeItemList.contains(String.format(USER_ITEM_FORMAT, i))) {
				continue;
			}
			if (changeDetail.length() != 0) {changeDetail.append("\n");}
			
			changeDetail.append(MessageConstant.MESSAGE_MONITOR_EVENT_CHANGE.getMessage(
				EventHinemosPropertyUtil.getDisplayName(userItemMap.get(i).getDisplayName(), i), 
				nullToEmpty(EventUtil.getUserItemValue(event, i)),
				nullToEmpty(EventUtil.getUserItemValue(info, i))
			));
			
			EventUtil.setUserItemValue(event, i, EventUtil.getUserItemValue(info, i));
		}
	}
	
	private static String nullToEmpty(String value) {
		if (value == null) {
			return "";
		}
		return value;
	}
}

