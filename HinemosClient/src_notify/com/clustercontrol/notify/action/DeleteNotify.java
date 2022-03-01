/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.action;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.openapitools.client.model.NotifyCheckIdResultInfoResponse;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.HinemosModuleMessage;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.infra.util.InfraConstants;
import com.clustercontrol.monitor.run.bean.CollectMonitorNotifyConstant;
import com.clustercontrol.notify.util.NotifyRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.ui.dialog.MessageDialogWithScroll;

/**
 * 通知情報を削除するクライアント側アクションクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class DeleteNotify {

	// ログ
	private static Log m_log = LogFactory.getLog( DeleteNotify.class );

	/**
	 * 通知情報を削除します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @param managerName マネージャ名
	 * @param notifyIdList 削除対象の通知IDリスト
	 * @return 削除に成功した場合、<code> true </code>
	 *
	 */
	public boolean delete(String managerName, List<String> notifyIdList) {

		boolean result = false;
		String msg = null;
		String[] args = new String[2];

		if (notifyIdList.isEmpty()) {
			return result;
		}

		if (notifyIdList.size() == 1) {
			args[0] = notifyIdList.get(0);
			args[1] = managerName;
			msg = "message.notify.5";
		} else {
			args[0] = Integer.toString(notifyIdList.size());
			msg = "message.notify.52";
		}

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			wrapper.deleteNotify(String.join(",", notifyIdList));
			result = true;

			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString(msg, args));

		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16") + "(" + managerName + ")");

		} catch (Exception e) {
			m_log.warn("delete(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + "(" + managerName + "), " + HinemosMessage.replace(e.getMessage()));
		}
		return result;
	}

	public int useCheck (String managerName, List<String> notifyIds){

		String id = this.toString();
		int result = Window.OK;
		List<NotifyCheckIdResultInfoResponse> retList = null;
		List<String> notifyGroupIdList = null;

		if (notifyIds == null || notifyIds.isEmpty()) {
			return result;
		}

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			retList = wrapper.checkNotifyId(String.join(",", notifyIds));

			for (NotifyCheckIdResultInfoResponse checkResult : retList) {
				notifyGroupIdList = checkResult.getNotifyGroupIdList();
				
				TreeMap<String, TreeSet<String>> notifyGroupMap = createNotifyGroupMap(notifyGroupIdList);

				if(notifyGroupMap.size() == 0){
					continue;
				}

				String[] args = { checkResult.getNotifyId() };
				MultiStatus mStatus = new MultiStatus(this.toString(), IStatus.OK, Messages.getString("message.notify.26", args), null);

				for (Map.Entry<String, TreeSet<String>> entry : notifyGroupMap.entrySet()) {
					
					mStatus.add(new Status(IStatus.INFO, id, IStatus.OK, "", null));

					String moduleDisplayName = "";
					moduleDisplayName = HinemosModuleMessage.nameToString(entry.getKey());
					mStatus.add(new Status(IStatus.INFO, id, IStatus.OK, "[" + moduleDisplayName + "]", null));

					for (String settingId : entry.getValue()) {
						//	        			message = "  " + strings[1];
						mStatus.add(new Status(IStatus.INFO, id, IStatus.OK, "  " + settingId, null));
					}
				}

				// どの監視機能で使用されているかをダイアログで表示する
				result = ErrorDialog.openError(
						null,
						 Messages.getString("info"),
						 null,
						 mStatus);
				continue;
			}
		} catch (RuntimeException | RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass e) {
			m_log.warn("useCheck(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		return result;
	}

	/**
	 * 削除対象の通知情報が他設定で使用されているかをダイアログ表示する
	 * 
	 * @param managerName マネージャ名
	 * @param notifyIds 通知IDリスト
	 * @return true：OK(削除可)、false：Cancel(削除不可)
	 */
	public boolean useCheckForUtility (String managerName, List<String> notifyIds){
		//FIXME メジャーバージョンでuseCheck()と共通の処理を統合等行う

		boolean result = true;
		List<NotifyCheckIdResultInfoResponse> retList = null;
		List<String> notifyGroupIdList = null;
		StringBuilder sbMessage = null;

		try {
			NotifyRestClientWrapper wrapper = NotifyRestClientWrapper.getWrapper(managerName);
			retList = wrapper.checkNotifyId(String.join(",", notifyIds));

			for (NotifyCheckIdResultInfoResponse checkResult : retList) {
				notifyGroupIdList = checkResult.getNotifyGroupIdList();

				TreeMap<String, TreeSet<String>> notifyGroupMap = createNotifyGroupMap(notifyGroupIdList);

				if(notifyGroupMap.size() == 0){
					continue;
				}

				sbMessage = new StringBuilder();
				String[] args = { checkResult.getNotifyId() };
				sbMessage.append(Messages.getString("message.notify.41", args));

				for (Map.Entry<String, TreeSet<String>> entry : notifyGroupMap.entrySet()) {

					String moduleDisplayName = "";
					moduleDisplayName = HinemosModuleMessage.nameToString(entry.getKey());
					sbMessage.append( "\n[" + moduleDisplayName + "]");

					for (String settingId : entry.getValue()) {
						sbMessage.append("\n  " + settingId);
					}
				}

				// どの監視機能で使用されているかをダイアログで表示する
				MessageDialogWithScroll messageDialogWithScroll = new MessageDialogWithScroll(
						Messages.getString(sbMessage.toString(), args));
				if (!messageDialogWithScroll.openQuestion()) {
					return false;
				}
			}
		} catch (RuntimeException | RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass e) {
			m_log.warn("useCheckForUtility(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			return false;
		}
		return result;
	}

	/**
	 * 通知グループIDから、通知が使用されている監視設定等の設定を洗い出す
	 * 
	 * @param notifyGroupIdList 通知グループIDリスト
	 * @return 通知が使用されている先を示すマップ
	 */
	private TreeMap<String, TreeSet<String>> createNotifyGroupMap(List<String> notifyGroupIdList) {
		TreeMap<String, TreeSet<String>> notifyGroupMap = new TreeMap<>();
		
		if (notifyGroupIdList == null || notifyGroupIdList.size() == 0) {
			return notifyGroupMap;
		}

		String notifyGroupId = null;
		String[] strings = null;

		Iterator<String> itr = notifyGroupIdList.iterator();

		while(itr.hasNext()){
			notifyGroupId = itr.next();
			strings = notifyGroupId.split("-");

			if (strings[0].startsWith(CollectMonitorNotifyConstant.PREDICTION_NOTIFY_GROUPID_PREFIX)) {
				// 将来予測監視
				strings[0] = strings[0].substring(CollectMonitorNotifyConstant.PREDICTION_NOTIFY_GROUPID_PREFIX.length());
			}
			if (strings[0].startsWith(CollectMonitorNotifyConstant.CHANGE_NOTIFY_GROUPID_PREFIX)) {
				// 変化量監視
				strings[0] = strings[0].substring(CollectMonitorNotifyConstant.CHANGE_NOTIFY_GROUPID_PREFIX.length());
			}
			if (strings[0].startsWith(InfraConstants.notifyGroupIdPrefix)) {
				// 環境構築
				String infraId = strings[0].substring(InfraConstants.notifyGroupIdPrefix.length());
				strings = new String[2];
				strings[0] = HinemosModuleConstant.INFRA;
				strings[1] = infraId;
			}

			if(strings[0].equals(HinemosModuleConstant.JOB_SESSION)){
				// JobSessionIDを除外
				continue;
			}

			if(!(HinemosModuleConstant.isExist(strings[0]))){
				// 収集対象を除外
				continue;
			}

			// 設定IDを出力
			StringBuffer sbId = new StringBuffer();
			sbId.append(strings[1]);
			for(int i = 2; i < strings.length - 1 ; i++){	// stringsの最後にはindexが含まれているため除外
				sbId.append("-" + strings[i]);
			}

			if (!notifyGroupMap.containsKey(strings[0])) {
				notifyGroupMap.put(strings[0], new TreeSet<>());
			}
			notifyGroupMap.get(strings[0]).add(sbId.toString());
		}
		return notifyGroupMap;
	}

}
