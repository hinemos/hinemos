/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.util.JobmapImageCacheUtil;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ[一覧]ビューの「登録」のクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class RegisterJobAction extends BaseAction {

	// ログ
	private static Log m_log = LogFactory.getLog( RegisterJobAction.class );

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		// 確認ダイアログを生成
		if (!MessageDialog.openQuestion(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.job.31"))) {
			return null;
		}


		JobTreeView view = JobMapActionUtil.getJobTreeView();
		JobMapTreeComposite tree = view.getJobMapTreeComposite();

		// Exceptionが発生したかどうかのフラグ
		boolean error = false;
		// ジョブユニットごとのメッセージのリスト(成功、失敗の結果の詳細が入る）
		HashMap<String, String> resultList = new HashMap<String, String>();

		
		
		// ジョブ登録の開始
		m_log.debug("registerJob start " + new Date());
		Long start = System.currentTimeMillis();
		Long t_start = System.currentTimeMillis();
		
		for (JobTreeItem mgrTree : JobEditStateUtil.getJobTreeItem().getChildren().get(0).getChildren() ) {
			
			// 成功して開放予定のジョブユニットのリスト
			ArrayList<JobTreeItem> releaseList = new ArrayList<JobTreeItem>();
		
			String managerName = mgrTree.getData().getId();
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
			
			// ジョブユニットの削除
			m_log.debug("deleteJobunit start");
			for (JobTreeItem jobunit : editState.getDeletedJobunitList()) {
				try {
					String jobunitId = jobunit.getData().getJobunitId();
					m_log.debug("delete " + jobunitId);
					wrapper.checkEditLock(jobunitId, editState.getEditSession(jobunit.getData()));
					wrapper.deleteJobunit(jobunitId);
					if (!editState.getEditedJobunitList().contains(jobunit)) {
						// 削除のみの場合は、編集ロック開放リストに追加する
						releaseList.add(jobunit);
						editState.removeJobunitUpdateTime(jobunitId);
					}
					Object[] arg = {managerName};
					resultList.put(jobunitId, Messages.getString("message.job.75", arg));
				} catch (InvalidRole_Exception e) {
					// システム権限エラーの場合は次のジョブユニットを処理する
					m_log.error("run() delete: " + HinemosMessage.replace(e.getMessage()));
					error = true;
					String jobunitId = jobunit.getData().getJobunitId();
					resultList.put(jobunitId, HinemosMessage.replace(e.getMessage()) + " (" + managerName +")");
				} catch (Exception e) {
					m_log.error("run() delete: " + HinemosMessage.replace(e.getMessage()), e);
					error = true;
					String jobunitId = jobunit.getData().getJobunitId();
					resultList.put(jobunitId, Messages.getString("message.job.76") + "[" + HinemosMessage.replace(e.getMessage()) + "]" + " (" + managerName +")");
				}
			}
			long t_end = System.currentTimeMillis();
			m_log.debug("delete: " + (t_end-t_start) +" ms");
			t_start = System.currentTimeMillis();
			
			// ジョブユニットの登録
			m_log.debug("registerJobunit start");
			for (JobInfo info : editState.getLockedJobunitList()) {
				try {
					JobTreeItem jobunit = null; // ループで一致すると判定された場合にはjobunitに値が設定される
					
					//削除したジョブユニットかチェックする
					for (JobTreeItem item : releaseList) {
						if (item.getData().equals(info)) {
							jobunit = item;
							break;
						}
					}
					if (jobunit != null) {
						// 削除したジョブユニットの場合は何もしない
						continue;
					}
					
					// 編集したジョブユニットかどうかをチェックする
					for (JobTreeItem item : editState.getEditedJobunitList()) {
						if (item.getData().equals(info)) {
							jobunit = item;
							break;
						}
					}
					
					if (jobunit == null) {
						// 編集していないジョブユニットは、ロックを開放する
						wrapper.releaseEditLock(editState.getEditSession(info));
						editState.removeLockedJobunit(info);
					} else {
						// 編集したジョブユニットはマネージャに登録する
						m_log.debug("register " + jobunit.getData().getJobunitId());
						String jobunitId = jobunit.getData().getJobunitId();
						wrapper.checkEditLock(jobunitId, editState.getEditSession(jobunit.getData()));
						Long updateTime = wrapper.registerJobunit(jobunit);
						
						// 編集ロック開放リストに追加する
						releaseList.add(jobunit);
						
						editState.putJobunitUpdateTime(jobunitId, updateTime);
						Object[] arg = {managerName};
						resultList.put(jobunitId, Messages.getString("message.job.79", arg));
					}
				} catch (InvalidRole_Exception e) {
					// システム権限エラーの場合は次のジョブユニットを処理する
					m_log.error("run() register: " + HinemosMessage.replace(e.getMessage()));
					error = true;
					resultList.put(info.getJobunitId(), info.getJobunitId() +
							" (" + Messages.getString("message.accesscontrol.16") + ")" + " (" + managerName +")");
				} catch (Exception e) {
					m_log.error("run() register: " + HinemosMessage.replace(e.getMessage()), e);
					error = true;
					resultList.put(info.getJobunitId(), Messages.getString("message.job.80") + "[" + HinemosMessage.replace(e.getMessage()) + "]" + " (" + managerName +")");
				}
			}
			t_end = System.currentTimeMillis();
			m_log.debug("register: " + (t_end-t_start) +" ms");
	
			for (JobTreeItem item : releaseList) {
				try {
					editState.exitEditMode(item);
					
					// 更新時刻の最新化のため、propertyFullをクリアする
					String jobunitId = item.getData().getJobunitId();
					JobPropertyUtil.clearPropertyFull(mgrTree, jobunitId);
				} catch (Exception e) {
					m_log.warn("run() : " + HinemosMessage.replace(e.getMessage()));
				}
			}
		}
		
		// 登録結果メッセージの作成
		String message = "";

		ArrayList<String> entries = new ArrayList<String>(resultList.keySet());
		Collections.sort(entries);

		for(String key : entries){
			message = message + key + ":" + resultList.get(key) + "\n";
		}
		
		String[] args = {message};
		
		if (error) {
			m_log.info("run() : register job failure " + message);
			// 登録に失敗したジョブユニットがある場合
			MessageDialog.openWarning(
					null,
					Messages.getString("message.hinemos.1"),
					Messages.getString("message.job.67", args));
		} else {
			// 登録に失敗したジョブユニットがない場合
			if (entries.isEmpty()) {
				// ジョブを編集してなかった場合など、登録に成功したジョブユニットが0件の場合
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.job.114"));
			} else {
				// 1件以上のジョブユニットを登録した場合
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.job.111", args));
			}
		}
		//定義情報更新時、アイコンキャッシュもリフレッシュする。
		JobmapImageCacheUtil iconCache = JobmapImageCacheUtil.getInstance();
		iconCache.refresh();

		tree.refresh();
		tree.getTreeViewer().setSelection(tree.getTreeViewer().getSelection(), true);
		tree.updateJobMapEditor(null);

		Long end = System.currentTimeMillis();
		m_log.debug("register() : " + (end - start) + "ms");
		m_log.debug("registerJob end   " + new Date());
		
		return null;
	}
}