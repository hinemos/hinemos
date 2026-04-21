/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.view.JobDetailView;

/*
 * ジョブ履歴[ジョブ詳細]ビューで選択項目によるフィルタ表示の切り替え
 */
public class JobDetailSelectedViewAction extends AbstractHandler implements IElementUpdater {
	/* ログ */
	private static Log log = LogFactory.getLog(JobDetailSelectedViewAction.class);
	
	/* アクションID */
	public static final String ID = JobDetailSelectedViewAction.class.getName();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// ジョブ履歴[ジョブ詳細]ビューのイベントか判定
		IWorkbenchPart wp = HandlerUtil.getActivePart(event);
		JobDetailView view = (JobDetailView) wp.getAdapter(JobDetailView.class);
		if (view == null) {
			log.info("The Event did not originate from a JobDetailView."); 
			return null;
		}
		
		Event e = (Event)event.getTrigger();
		
		// ツールバーからの切り替えか？
		if (e.widget instanceof ToolItem) {
			view.getComposite().getJobDetailViewModel().setSelectedView(((ToolItem)e.widget).getSelection());
		// ポップアップメニューからの切り替えか？
		} else if (e.widget instanceof MenuItem) {
			view.getComposite().getJobDetailViewModel().setSelectedView(((MenuItem)e.widget).getSelection());
			
			// ツールバー上のボタンを更新
			ICommandService cs = (ICommandService)HandlerUtil.getActiveWorkbenchWindow(event).getService(ICommandService.class);
			cs.refreshElements(ID, null);
		}
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		// ジョブ履歴[ジョブ詳細]ビューで、ジョブが選択されているならこのアクションを有効にする
		IWorkbenchPartSite site = (IWorkbenchPartSite)element.getServiceLocator().getService(IWorkbenchPartSite.class);
		if (site.getPart() instanceof JobDetailView) {
			element.setChecked(((JobDetailView)site.getPart()).getComposite().getJobDetailViewModel().isSelectedView());
		}
	}

	@Override
	public void dispose() {
	}
}
