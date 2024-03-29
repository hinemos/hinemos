/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.view.JobListView;

/**
 * ジョブコピーするクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class CopyJobAction extends AbstractHandler implements IElementUpdater{
	/** アクションID */
	public static final String ID = CopyJobAction.class.getName();
	/** ログ */
	private static Log m_log = LogFactory.getLog(CopyJobAction.class);
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
	 * ジョブ[一覧]ビューにて選択されたジョブツリーアイテムを取得し、<BR>
	 * ジョブ[一覧]ビューにコピー元のジョブツリーアイテムとして設定します。
	 *
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (m_log.isTraceEnabled()) {
			m_log.trace("execute() start");
		}
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		if (viewPart instanceof JobListView) {
			JobListView view = (JobListView)viewPart;

			JobTreeItemWrapper copyItem = view.getSelectJobTreeItemList().get(0);
			view.setCopyJobTreeItem(copyItem);
		}
		if (m_log.isTraceEnabled()) {
			m_log.trace("execute() end");
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

				boolean editEnable = true;
				if(part instanceof JobListView){
					// Enable button when 1 item is selected
					JobListView view = (JobListView)part;
					int size = view.getJobTreeComposite().getSelectItemList().size();
					if(size != 1 || view.getDataType() == JobInfoWrapper.TypeEnum.COMPOSITE || view.getDataType() == JobInfoWrapper.TypeEnum.MANAGER){
						editEnable = false;
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
