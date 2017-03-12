/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
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
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.composite.DetailComposite;
import com.clustercontrol.jobmanagement.composite.HistoryComposite;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.view.JobDetailView;
import com.clustercontrol.jobmanagement.view.JobHistoryView;
import com.clustercontrol.monitor.action.GetEventListTableDefine;
import com.clustercontrol.monitor.composite.EventListComposite;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.view.ScopeListBaseView;
import com.clustercontrol.ws.jobmanagement.JobInfoNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ履歴パースペクティブを開くクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class EventOpenJobHistoryAction extends AbstractHandler implements IElementUpdater {
	private IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

	/** アクションID */
	public static final String ID = EventOpenJobHistoryAction.class.getName();

	/** ログ */
	private static Log m_log = LogFactory.getLog(EventOpenJobHistoryAction.class);

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

	private JobTreeItem getChild(JobTreeItem item) {
		JobTreeItem child = item.getChildren().size() >0 ? item.getChildren().get(0) : null;

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
		if (desc == null){
			return null;
		}
		
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		ScopeListBaseView eventView = null;
		try {
			eventView = (EventView) this.viewPart.getAdapter(EventView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (eventView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		EventListComposite composite = (EventListComposite) eventView.getListComposite();
		StructuredSelection  selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (ArrayList<?>) selection.getFirstElement();

		if (list == null){
			return null;
		}

		JobTreeItem item;

		//セッションID（監視ID）を取得
		String monitorId = (String) list.get(GetEventListTableDefine.MONITOR_ID);
		String managerName = (String) list.get(GetEventListTableDefine.MANAGER_NAME);
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			item = wrapper.getJobDetailList(monitorId);
		} catch (JobInfoNotFound_Exception e) {
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
		historyCmp.setSessionId(monitorId);
		DetailComposite detailCmp = detailView.getComposite();
		JobTreeItem child = getChild(item);

		child.getData().getType().equals(JobConstant.TYPE_JOB);
		String jobId = child.getData().getId();
		String jobunitId = child.getData().getJobunitId();
		detailCmp.setJobId(jobId);
		detailCmp.setSessionId(monitorId);

		//該当のレコードを選択
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
				if(part instanceof EventView){
					// Enable button when 1 item is selected
					EventView view = (EventView)part;
					EventListComposite composite = (EventListComposite) view.getListComposite();
					StructuredSelection  selection = (StructuredSelection) composite.getTableViewer().getSelection();

					List<?> list = (ArrayList<?>) selection.getFirstElement();

					if(HinemosModuleConstant.JOB.equals(view.getPluginId()) &&
							!HinemosModuleConstant.SYSYTEM.equals(list.get(GetEventListTableDefine.MONITOR_ID)) ) {
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
