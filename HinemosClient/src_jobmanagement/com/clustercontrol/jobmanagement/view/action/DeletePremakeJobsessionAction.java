/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.JobKickFilterInfoRequest.JobkickTypeEnum;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.action.GetJobKickTableDefine;
import com.clustercontrol.jobmanagement.composite.JobKickListComposite;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.JobKickListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[実行契機]ビューの「事前生成したジョブセッションの削除」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class DeletePremakeJobsessionAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( DeletePremakeJobsessionAction.class );

	/** アクションID */
	public static final String ID = DeletePremakeJobsessionAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * ジョブ[実行契機]ビューの「事前生成したジョブセッションの削除」が押された場合に、
	 * ジョブスケジュールで事前生成したジョブセッションを削除します。
	 * 
	 *
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		JobKickListView view = null;
		try {
			view = (JobKickListView) viewPart.getAdapter(JobKickListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		JobKickListComposite composite = (JobKickListComposite) view.getComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		if (list == null || list.size() == 0) {
			throw new InternalError("select element is not find");
		}

		String managerName = (String)list.get(GetJobKickTableDefine.MANAGER_NAME);
		String jobKickId = (String) list.get(GetJobKickTableDefine.JOBKICK_ID);

		String[] args = { jobKickId, managerName };
		// 確認ダイアログ
		if (!MessageDialog.openQuestion(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.job.169", args))) {
			return null;
		}
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			wrapper.deletePremakeJobsession(jobKickId);
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.job.170", args));
		} catch (InvalidRole e) {
			// 権限が無い場合はエラーメッセージを表示する
			MessageDialog.openInformation( 
					null, 
					Messages.getString("message"), 
					Messages.getString("message.accesscontrol.16")); 
		} catch (Exception e) { 
			m_log.warn("run(), " + e.getMessage(), e); 
			MessageDialog.openError( 
					null, 
					Messages.getString("failed"), 
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage())); 
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if(part instanceof JobKickListView){
					// Enable button when 1 item is selected
					JobKickListView view = (JobKickListView)part;
					if(Objects.equals(view.getSelectType(), JobkickTypeEnum.SCHEDULE)
						&& view.getSelectedIdList().size() == 1) {
						// ジョブスケジュールのみ1件選択されている場合はTrue
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
