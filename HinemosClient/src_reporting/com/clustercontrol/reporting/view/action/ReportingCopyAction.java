/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.view.action;

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
import com.clustercontrol.reporting.action.GetReportingScheduleTableDefine;
import com.clustercontrol.reporting.composite.ReportingScheduleListComposite;
import com.clustercontrol.reporting.dialog.ReportingScheduleDialog;
import com.clustercontrol.reporting.view.ReportingScheduleView;

/**
 * レポーティング[スケジュール]ビューのコピーアクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReportingCopyAction extends AbstractHandler implements IElementUpdater{
	/** ログ */
	private static Log m_log = LogFactory.getLog(ReportingCopyAction.class);
	
	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;
	
	/** アクションID */
	public static final String ID = ReportingCopyAction.class.getName();
	
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
		
		// スケジュール一覧より、選択されているスケジュールIDを取得
		
		ReportingScheduleView view = (ReportingScheduleView) this.viewPart
				.getAdapter(ReportingScheduleView.class);
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		ReportingScheduleListComposite composite = (ReportingScheduleListComposite) view.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();
		
		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String reportScheduleId = null;
		if(list != null && list.size() > 0) {
			managerName = (String) list.get(GetReportingScheduleTableDefine.MANAGER_NAME);
			reportScheduleId = (String) list.get(GetReportingScheduleTableDefine.REPORT_SCHEDULE_ID);
		}
		
		if (managerName != null && reportScheduleId != null) {
			// ダイアログを生成
			ReportingScheduleDialog dialog = new ReportingScheduleDialog(
					this.viewPart.getSite().getShell(), managerName, reportScheduleId, 
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

				if( part instanceof ReportingScheduleView ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 1 == ((ReportingScheduleView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
