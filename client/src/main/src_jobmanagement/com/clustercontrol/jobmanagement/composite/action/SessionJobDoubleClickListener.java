/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.jobmanagement.action.GetHistoryTableDefine;
import com.clustercontrol.jobmanagement.action.GetPlanTableDefine;
import com.clustercontrol.jobmanagement.composite.DetailComposite;
import com.clustercontrol.jobmanagement.composite.HistoryComposite;
import com.clustercontrol.jobmanagement.composite.JobPlanComposite;
import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ[履歴]ビューまたはジョブ[ジョブ詳細]ビューのテーブルビューア用のDoubleClickListenerです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class SessionJobDoubleClickListener implements IDoubleClickListener {

	// ログ
	private static Log m_log = LogFactory.getLog( SessionJobDoubleClickListener.class );

	/** ジョブ[履歴]ビュー、ジョブ[ジョブ詳細]ビュー、ジョブ[スケジュール予定]用のコンポジット */
	private Composite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite ジョブ[履歴]ビュー、ジョブ[ジョブ詳細]ビュー、ジョブ[スケジュール予定]用のコンポジット
	 */
	public SessionJobDoubleClickListener(Composite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * ジョブ[履歴]ビュー、
	 * ジョブ[ジョブ詳細]ビュー、
	 * ジョブ[スケジュール予定]ビュー
	 * のテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からセッションIDとジョブIDを取得します。</li>
	 * <li>セッションIDとジョブIDからジョブ情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		String managerName = null;
		String sessionId = null;
		String jobunitId = null;
		String jobId = null;

		if(m_composite instanceof HistoryComposite){
			//セッションIDとジョブIDを取得
			if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
				ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
						.getSelection()).getFirstElement();
				managerName = (String)info.get(GetHistoryTableDefine.MANAGER_NAME);
				sessionId = (String)info.get(GetHistoryTableDefine.SESSION_ID);
				jobunitId = (String)info.get(GetHistoryTableDefine.JOBUNIT_ID);
				jobId = (String)info.get(GetHistoryTableDefine.JOB_ID);
			}
		}
		else if(m_composite instanceof DetailComposite){
			//セッションIDとジョブIDを取得
			if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
				JobTreeItem item = (JobTreeItem) ((StructuredSelection) event
						.getSelection()).getFirstElement();
				JobInfo info = item.getData();
				managerName = ((DetailComposite)m_composite).getManagerName();
				sessionId = ((DetailComposite)m_composite).getSessionId();
				jobunitId = ((DetailComposite)m_composite).getJobunitId();
				jobId = info.getId();
			}
		}
		//ジョブ[スケジュール予定]ビューダブルクリックの処理 (ジョブ[一覧]のツリーの該当ノードをフォーカス)
		else if(m_composite instanceof JobPlanComposite){
			if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
				ArrayList<?> plan = (ArrayList<?>) ((StructuredSelection) event
						.getSelection()).getFirstElement();
				managerName = (String)plan.get(GetPlanTableDefine.MANAGER_NAME);
				jobunitId = (String)plan.get(GetPlanTableDefine.JOBUNIT_ID);
				jobId = (String)plan.get(GetPlanTableDefine.JOB_ID);
				//アクティブページを手に入れる
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(JobListView.ID);
				if (viewPart != null) {
					JobListView view = (JobListView) viewPart
							.getAdapter(JobListView.class);
					if (view == null) {
						m_log.info("double click: view is null");
						return;
					}
					view.setFocus(managerName, jobunitId, jobId);
				}
			}
		}

		if(sessionId != null && sessionId.length() > 0 &&
				jobunitId != null && jobunitId.length() > 0 &&
				jobId != null && jobId.length() > 0){

			JobTreeItem item = null;
			try {
				JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
				item = wrapper.getSessionJobInfo(sessionId, jobunitId, jobId);
			} catch (InvalidRole_Exception e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("doubleClick() getSessionJobInfo, " + e.getMessage(), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}

			if(item != null){
				JobDialog dialog = new JobDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						managerName, true, true);

				dialog.setJobTreeItem(item);
				dialog.open();
			}
		}
	}

}
