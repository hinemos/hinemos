/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.JobTreeItem;
import org.openapitools.client.model.JobTreeItemResponseP2;
import org.openapitools.client.model.RegisterJobunitRequest;
import org.openapitools.client.model.ReplaceJobunitRequest;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobWaitRuleUtil;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[一覧]ビューの「登録」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class RegisterJobAction extends AbstractHandler {

	// ログ
	private static Log m_log = LogFactory.getLog( RegisterJobAction.class );

	/** ビュー */
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * ジョブ[一覧]ビューの「登録」が押された場合に、ジョブを登録します。
	 * <p>
	 * <ol>
	 * <li>登録の確認ダイアログを表示します。</li>
	 * <li>ジョブ[一覧]ビューからジョブツリーアイテムを取得します。</li>
	 * <li>ジョブツリーアイテムを登録します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.JobListView
	 * @see com.clustercontrol.jobmanagement.composite.JobTreeComposite
	 * @see com.clustercontrol.jobmanagement.action.RegisterJob#registerJob(JobTreeItem)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if (null == this.window || !isEnabled()) {
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		JobListView jobListView = null;
		try {
			jobListView = (JobListView) viewPart.getAdapter(JobListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (jobListView == null) {
			m_log.info("execute: view is null");
			return null;
		}

		// 確認ダイアログを生成
		if (MessageDialog.openQuestion(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.job.31"))) {

			// Exceptionが発生したかどうかのフラグ
			boolean error = false;
			// ジョブユニットごとのメッセージのリスト(成功、失敗の結果の詳細が入る）
			HashMap<String, String> resultList = new HashMap<String, String>();

			JobTreeItemWrapper jobTreeItem = JobEditStateUtil.getJobTreeItem();
			if (jobTreeItem == null
					|| jobTreeItem.getChildren() == null
					|| jobTreeItem.getChildren().size() == 0
					|| jobTreeItem.getChildren().get(0).getChildren() == null) {
				// ジョブのキャッシュ情報が最新でないため処理終了
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.46"));
				return null;
			}		
			for (JobTreeItemWrapper mgrTree : jobTreeItem.getChildren().get(0).getChildren() ) {

				// 成功して編集モードから解除されたのジョブユニットのリスト
				ArrayList<JobTreeItemWrapper> releaseList = new ArrayList<JobTreeItemWrapper>();

				String managerName = mgrTree.getData().getId();
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);

				// ジョブ登録の開始
				m_log.debug("registerJob start (managerName=" + managerName +")" + new Date());
				Long allStart = System.currentTimeMillis();
				Long deleteStart = System.currentTimeMillis();

				// ジョブユニットの削除
				m_log.debug("deleteJobunit start");
				JobEditState jobEditState = JobEditStateUtil.getJobEditState(managerName);
				for (JobTreeItemWrapper jobunit : jobEditState.getDeletedJobunitList()) {
					try {
						String jobunitId = jobunit.getData().getJobunitId();
						m_log.debug("delete " + jobunitId);
						wrapper.checkEditLock(jobunitId, jobEditState.getEditSession(jobunit.getData()));
						wrapper.deleteJobunit(jobunitId);
						if (!jobEditState.getEditedJobunitList().contains(jobunit)) {
							// 削除のみの場合は、編集ロック開放リストに追加する
							releaseList.add(jobunit);
							jobEditState.removeJobunitUpdateTime(jobunitId);
						}
						Object[] arg = { managerName };
						resultList.put(jobunitId, Messages.getString("message.job.75", arg));
					} catch (InvalidRole e) {
						// システム権限エラーの場合は次のジョブユニットを処理する
						m_log.error("run() delete: " + e.getMessage());
						error = true;
						String jobunitId = jobunit.getData().getJobunitId();
						resultList.put(jobunitId, HinemosMessage.replace(e.getMessage()) + " ("	+ managerName + ")");
					} catch (Exception e) {
						m_log.error("run() delete: " + e.getMessage(), e);
						error = true;
						String jobunitId = jobunit.getData().getJobunitId();
						resultList.put(jobunitId, Messages.getString("message.job.76") + "[" + HinemosMessage.replace(e.getMessage()) + "]" + " (" + managerName + ")");
					}
				}
				long t_end = System.currentTimeMillis();
				m_log.debug("delete: " + (t_end - deleteStart) + " ms");
				Long registerStart = System.currentTimeMillis();
				// ジョブユニットの登録
				m_log.debug("registerJobunit start");
				for (JobInfoWrapper info : jobEditState.getLockedJobunitList()) {
					try {
						JobTreeItemWrapper jobunit = null; // ループで一致すると判定された場合にはjobunitに値が設定される

						// 削除したジョブユニットかチェックする
						for (JobTreeItemWrapper item : releaseList) {
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
						for (JobTreeItemWrapper item : jobEditState.getEditedJobunitList()) {
							if (item.getData().equals(info)) {
								jobunit = item;
								break;
							}
						}

						if (jobunit == null) {
							// 編集していないジョブユニットは、ロックを開放する
							wrapper.releaseEditLock(info.getJobunitId(),jobEditState.getEditSession(info));
							jobEditState.removeLockedJobunit(info);
						} else {
							// 編集したジョブユニットはマネージャに登録する
							JobTreeItemResponseP2 updateRes =null;
							m_log.debug("register " + jobunit.getData().getJobunitId());
							String jobunitId = jobunit.getData().getJobunitId();
							wrapper.checkEditLock(jobunitId, jobEditState.getEditSession(jobunit.getData()));
							if (jobEditState.getLockedJobunitBackup(jobunit.getData()) == null) {
								m_log.debug("add jobunit " + jobunit.getData().getJobunitId());
								RegisterJobunitRequest request = new RegisterJobunitRequest();
								request.setJobTreeItem(JobTreeItemUtil.getRequestFromItem(jobunit));
								updateRes = wrapper.registerJobunit(request);
							}else{
								m_log.debug("mod jobunit " + jobunit.getData().getJobunitId());
								
								// ジョブの待ち条件等の変更に従い、他ジョブの後続ジョブ実行設定を更新する
								updateNextJobOrderInfo(jobunit.getChildren());
								
								ReplaceJobunitRequest request = new ReplaceJobunitRequest();
								request.setJobTreeItem(JobTreeItemUtil.getRequestFromItem(jobunit));
								updateRes = wrapper.replaceJobunit(jobunit.getData().getJobunitId(), request);
							}
							// 編集ロック開放リストに追加する
							releaseList.add(jobunit);

							jobEditState.putJobunitUpdateTime(jobunit.getData().getJobunitId(), JobTreeItemUtil.convertDtStringtoLong(updateRes.getData().getUpdateTime()));
							Object[] arg = { managerName };
							resultList.put(jobunitId, Messages.getString("message.job.79", arg));
						}
					} catch (InvalidRole e) {
						// システム権限エラーの場合は次のジョブユニットを処理する
						m_log.error("run() register: " + e.getMessage());
						error = true;
						resultList.put(info.getJobunitId(), info.getJobunitId() +
								" (" + Messages.getString("message.accesscontrol.16") + ")" + " (" + managerName + ")");
					} catch (Exception e) {
						m_log.error("run() register: " + e.getMessage(), e);
						error = true;
						resultList.put(info.getJobunitId(), Messages.getString("message.job.80") + "[" + HinemosMessage.replace(e.getMessage()) + "]" + " (" + managerName + ")");
					}
				}
				t_end = System.currentTimeMillis();
				m_log.debug("register : " + (t_end - registerStart) + " ms");

				Long releaseStart = System.currentTimeMillis();

				// 編集ロックを解除したジョブユニットのpropertyFullをまとめてクリアする
				for (JobTreeItemWrapper item : releaseList) {
					try {
						// 編集ロックの解除(マネージャ側のロックはマネージャ側編集操作の完了時に併せて実施されている前提で不要 )
						m_log.debug("release " + item.getData().getJobunitId());
						jobEditState.exitEditMode(item);

						// 更新時刻の最新化のため、propertyFullをクリアする
						String jobunitId = item.getData().getJobunitId();
						JobPropertyUtil.clearPropertyFull(mgrTree, jobunitId);
					} catch (Exception e) {
						m_log.warn("run() : " + e.getMessage());
					}
				}

				t_end = System.currentTimeMillis();
				m_log.debug("release : " + (t_end - releaseStart) + " ms");

				Long end = System.currentTimeMillis();
				m_log.debug("delete+register+release : " + (end - allStart) + "ms");

			}

			// 登録結果のメッセージの作成
			StringBuffer message = new StringBuffer();

			ArrayList<String> entries = new ArrayList<String>(resultList.keySet());
			Collections.sort(entries);

			for (String key : entries) {
				message.append(key + ":" + resultList.get(key) + "\n");
			}

			String messageStr = message.toString();
			String[] args = { messageStr };

			if (error) {
				m_log.info("run() : register job failure " + messageStr);
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

			m_log.debug("registerJob end   " + new Date());

			jobListView.getJobTreeComposite().refresh();
			jobListView.getJobTreeComposite().getTreeViewer().setSelection(
					new StructuredSelection(jobListView.getJobTreeComposite().getSelectItemList()), true);
		}
		return null;
	}

	/**
	 * ジョブの待ち条件等の変更に従い、他ジョブの後続ジョブ実行設定を更新します
	 * @param treeItemList
	 */
	private void updateNextJobOrderInfo(List<JobTreeItemWrapper> treeItemList) {
		if (treeItemList == null || treeItemList.size() == 0) {
			return;
		}
		for (JobTreeItemWrapper treeItem : treeItemList) {
			JobInfoWrapper jobInfo = treeItem.getData();
			if (jobInfo.getWaitRule() != null && jobInfo.getWaitRule().getExclusiveBranch().booleanValue()) {
				JobWaitRuleUtil.updateNextJobOrderInfo(treeItem, null);
			}
			// 再帰処理
			if (treeItem.getChildren() != null && treeItem.getChildren().size() > 0) {
				updateNextJobOrderInfo(treeItem.getChildren());
			}
		}
	}
}
