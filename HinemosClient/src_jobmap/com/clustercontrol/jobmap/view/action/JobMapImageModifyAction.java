/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmap.dialog.JobMapImageDialog;
import com.clustercontrol.jobmap.view.JobMapImageListView;
import com.clustercontrol.util.HinemosMessage;

/**
 * ジョブマップ用イメージファイル変更を行うクライアント側アクションクラス<BR>
 *
 * @version 6.0.a
 */
public class JobMapImageModifyAction extends AbstractHandler implements IElementUpdater{
	private static Log m_log = LogFactory.getLog(JobMapImageModifyAction.class);
	
	/** CommandId */
	public static final String ID = BaseAction.ActionIdBase + JobMapImageModifyAction.class.getSimpleName();

	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;

	/**
	 * Handler execution
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);

		// カレンダ一覧より、選択されているカレンダIDを取得
		JobMapImageListView listView = null; 
		try { 
			listView = (JobMapImageListView) this.viewPart.getAdapter(JobMapImageListView.class); 
		} catch (Exception e) { 
			m_log.info("execute " + HinemosMessage.replace(e.getMessage())); 
			return null; 
		}
		String managerName = listView.getManagerName();
		String id = listView.getSelectedIdList().get(0);

		if (id != null) {
			// ダイアログを生成
			JobMapImageDialog dialog = new JobMapImageDialog(this.viewPart.getSite()
					.getShell(), managerName, id,
					PropertyDefineConstant.MODE_MODIFY);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				listView.update();
			}
		}

		return null;
	}

	/**
	 * Dispose
	 */
	@Override
	public void dispose(){
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * Update handler status
	 */
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				if( part instanceof JobMapImageListView  ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 1 == ((JobMapImageListView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}

}
