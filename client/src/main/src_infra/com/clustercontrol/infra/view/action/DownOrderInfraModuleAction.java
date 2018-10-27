/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.view.action;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.infra.action.GetInfraModuleTableDefine;
import com.clustercontrol.infra.view.InfraModuleView;

public class DownOrderInfraModuleAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(DownOrderInfraModuleAction.class);
	
	/** アクションID */
	public static final String ID = DownOrderInfraModuleAction.class.getName();

	/** dispose*/
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		if (viewPart instanceof InfraModuleView) {
			InfraModuleView view = null;
			try {
				view = (InfraModuleView) viewPart.getAdapter(InfraModuleView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}
			
			if (view == null) {
				m_log.info("execute: view is null"); 
				return null;
			}
			
			view.moveDown();
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		boolean enable = false;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				if(part instanceof InfraModuleView){
					InfraModuleView view = (InfraModuleView) part.getAdapter(InfraModuleView.class);
					if (view == null) {
						m_log.info("execute: view is null");
						return;
					}
					// Enable button when 1 item is selected
					StructuredSelection selection = null;
					if(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
						selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
					}
					if(selection != null && selection.size() == 1
							&& view.getComposite().getTable().getItemCount() > 1
							// error: incompatible types: CAP#1 cannot be converted to int
							//&& (int)((ArrayList<?>)selection.getFirstElement()).get(GetInfraModuleTableDefine.ORDER_NO) < view.getComposite().getTable().getItemCount()){
							&& (Integer)((ArrayList<?>)selection.getFirstElement()).get(GetInfraModuleTableDefine.ORDER_NO) < view.getComposite().getTable().getItemCount()){
						enable = true;
					}
				}
				this.setBaseEnabled(enable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}

	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

}
