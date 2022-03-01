/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.JobHistoryFilterBaseRequest;
import org.openapitools.client.model.JobHistoryFilterConditionRequest;
import org.openapitools.client.model.JobTreeItemResponseP4;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.composite.DetailComposite;
import com.clustercontrol.jobmanagement.composite.HistoryComposite;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.view.JobDetailView;
import com.clustercontrol.jobmanagement.view.JobHistoryView;
import com.clustercontrol.jobmanagement.view.action.HistoryFilterAction;
import com.clustercontrol.monitor.action.GetStatusListTableDefine;
import com.clustercontrol.monitor.composite.StatusListComposite;
import com.clustercontrol.monitor.view.StatusView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.view.ScopeListBaseView;

/**
 * ジョブ履歴パースペクティブを開くクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class StatusOpenJobHistoryAction extends AbstractHandler implements IElementUpdater {
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

	/** アクションID */
	public static final String ID = StatusOpenJobHistoryAction.class.getName();

	/** ログ */
	private static Log m_log = LogFactory.getLog(EventOpenJobHistoryAction.class);

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.window = null;
	}

	private JobTreeItemWrapper getChild(JobTreeItemWrapper item) {
		JobTreeItemWrapper child = item.getChildren().size() >0 ? item.getChildren().get(0) : null;

		if (child != null) {
			child = getChild(child);
		} else {
			return item;
		}
		return child;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		IPerspectiveRegistry reg = PlatformUI.getWorkbench()
				.getPerspectiveRegistry();

		PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IPerspectiveDescriptor desc = reg.findPerspectiveWithId("com.clustercontrol.jobmanagement.ui.JobHistoryPerspective");
		if (desc == null) {
			return null;
		}

		// 選択アイテムの取得
		IWorkbenchPart viewPart = HandlerUtil.getActivePart(event);
		ScopeListBaseView statusView = null;
		try {
			statusView = (StatusView) viewPart.getAdapter(StatusView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (statusView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		StatusListComposite composite = (StatusListComposite) statusView.getListComposite();
		StructuredSelection  selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (ArrayList<?>) selection.getFirstElement();

		if (list == null) {
			return null;
		}

		JobTreeItemWrapper item;

		//セッションID（監視ID）を取得
		String monitorId = (String) list.get(GetStatusListTableDefine.MONITOR_ID);
		String managerName = (String) list.get(GetStatusListTableDefine.MANAGER_NAME);
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			JobTreeItemResponseP4 detail = wrapper.getJobDetailList(monitorId);
			item = JobTreeItemUtil.getItemFromP4(detail);
		} catch (JobInfoNotFound e) {
			ClientSession.occupyDialog();
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.job.122"));
			ClientSession.freeDialog();
			return null;
		} catch (Exception e) {
			m_log.warn("run() getJobDetailList, " + e.getMessage(), e);
			if(ClientSession.isDialogFree()){
				ClientSession.occupyDialog();
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				ClientSession.freeDialog();
			}
			return null;
		}

		IWorkbenchPage page = window.getActivePage();

		//パースペクティブを開く
		page.setPerspective(desc);

		//viewとcompositeを取得
		JobHistoryView historyView = (JobHistoryView)page.findView(JobHistoryView.ID);
		if (historyView == null)
			throw new InternalError("historyView is null.");

		JobDetailView detailView = (JobDetailView)page.findView(JobDetailView.ID);
		if (detailView == null)
			throw new InternalError("detailView is null.");

		HistoryComposite historyCmp = historyView.getComposite();

		//ジョブID、ジョブユニットIDを取得
		ArrayList<?> objList = (ArrayList<?>)historyCmp.getTableViewer().getInput();
		if (objList == null || objList.size() == 0) {
			return null;
		}

		//セッションID、ジョブIDをセット
		historyCmp.setManagerName(managerName);
		historyCmp.setSessionId(monitorId);
		DetailComposite detailCmp = detailView.getComposite();
		JobTreeItemWrapper child = getChild(item);
		String jobId = child.getData().getId();
		String jobunitId = child.getData().getJobunitId();
		detailCmp.setJobId(jobId);
		detailCmp.setSessionId(monitorId);

		// フィルタボタン
		ICommandService commandService = (ICommandService)window.getService(ICommandService.class);
		Command command = commandService.getCommand(HistoryFilterAction.ID);
		boolean isChecked = !HandlerUtil.toggleCommandState(command);
		if (!isChecked) {
			// チェック外れる場合はもう一度呼出し、チェックを入れる
			HandlerUtil.toggleCommandState(command);
		}

		// セッションIDによるフィルタリング
		JobHistoryFilterBaseRequest sessionIdFilter = new JobHistoryFilterBaseRequest();
		JobHistoryFilterConditionRequest filterCondition = new JobHistoryFilterConditionRequest();
		filterCondition.setSessionId(monitorId);
		sessionIdFilter.addConditionsItem(filterCondition);
		historyView.setFilter(managerName, sessionIdFilter);
		historyView.setFilterEnabled(true);

		// 画面更新、該当のジョブ詳細を選択
		historyView.update(false);
		detailCmp.setItem(managerName, monitorId, jobunitId, item);

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
				if(part instanceof StatusView){
					// Enable button when 1 item is selected
					StatusView view = (StatusView)part;

					if(view.getRowNum() == 1 &&
							HinemosModuleConstant.JOB.equals(view.getPluginId())){
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
