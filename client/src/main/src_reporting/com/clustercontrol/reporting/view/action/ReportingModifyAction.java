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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.reporting.view.action.ReportingModifyAction;
import com.clustercontrol.reporting.action.GetReportingScheduleTableDefine;
import com.clustercontrol.reporting.composite.ReportingScheduleListComposite;
import com.clustercontrol.reporting.dialog.ReportingScheduleDialog;
import com.clustercontrol.reporting.view.ReportingScheduleView;
import com.clustercontrol.util.Messages;

/**
 * レポーティング[スケジュール]ビューの編集アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReportingModifyAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog(ReportingModifyAction.class);

	/** アクションID */
	public static final String ID = ReportingModifyAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		ReportingScheduleView view = (ReportingScheduleView) this.viewPart
				.getAdapter(ReportingScheduleView.class);
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		ReportingScheduleListComposite composite = (ReportingScheduleListComposite) view
				.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite
				.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String scheduleId = null;
		if (list != null && list.size() > 0) {
			managerName = (String) list.get(GetReportingScheduleTableDefine.MANAGER_NAME);
			scheduleId = (String) list.get(GetReportingScheduleTableDefine.REPORT_SCHEDULE_ID);
		}
		Table table = composite.getTableViewer().getTable();

		// 選択アイテムがある場合に、編集ダイアログを表示する
		if (scheduleId != null) {

			try {
				// ダイアログを生成
				ReportingScheduleDialog dialog = new ReportingScheduleDialog(
						this.viewPart.getSite().getShell(), managerName, scheduleId,
						PropertyDefineConstant.MODE_MODIFY);

				// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
				if (dialog.open() == IDialogConstants.OK_ID) {
					int selectIndex = table.getSelectionIndex();
					view.update();
					table.setSelection(selectIndex);
				}
			} catch (Exception e1) {
				m_log.warn("run(), " + e1.getMessage(), e1);
			}

		} else {
			MessageDialog.openWarning(null, 
					Messages.getString("warning"),
					Messages.getString("message.reporting.8"));
		}
		return null;
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
