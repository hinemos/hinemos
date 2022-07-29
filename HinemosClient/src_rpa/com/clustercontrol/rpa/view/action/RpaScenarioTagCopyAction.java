/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.rpa.action.GetRpaScenarioTagListTableDefine;
import com.clustercontrol.rpa.composite.RpaScenarioTagListComposite;
import com.clustercontrol.rpa.dialog.RpaScenarioTagDialog;
import com.clustercontrol.rpa.view.RpaScenarioTagView;

/**
 * RPA設定[シナリオタグ]ビューのコピーアクションクラス<BR>
 */
public class RpaScenarioTagCopyAction extends AbstractHandler implements IElementUpdater{
	
	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;

	/** ログ */
	private static Log log = LogFactory.getLog(RpaScenarioTagCopyAction.class);
	
	/** アクションID */
	public static final String ID = RpaScenarioTagCopyAction.class.getName();
	
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
		
		// テンプレートセット一覧より、選択されているスケジュールIDを取得
		
		RpaScenarioTagView view = (RpaScenarioTagView) this.viewPart
				.getAdapter(RpaScenarioTagView.class);
		if (view == null) {
			log.info("execute: view is null"); 
			return null;
		}
		RpaScenarioTagListComposite composite = (RpaScenarioTagListComposite) view.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();
		
		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String tagId = null;
		
		if(list != null && list.size() > 0) {
			managerName = (String) list.get(GetRpaScenarioTagListTableDefine.MANAGER_NAME);
			tagId = (String) list.get(GetRpaScenarioTagListTableDefine.TAG_ID);
		}
		
		if (managerName != null && tagId != null) {
			// ダイアログを生成
			RpaScenarioTagDialog dialog = new RpaScenarioTagDialog(
					this.viewPart.getSite().getShell(), managerName, tagId, 
					PropertyDefineConstant.MODE_COPY);
			
			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				view.update();
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

				if( part instanceof RpaScenarioTagView ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 1 == ((RpaScenarioTagView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
