/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.view.action;

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
import com.clustercontrol.notify.restaccess.action.GetRestAccessInfoListTableDefine;
import com.clustercontrol.notify.restaccess.composite.RestAccessInfoListComposite;
import com.clustercontrol.notify.restaccess.dialog.RestAccessInfoCreateDialog;
import com.clustercontrol.notify.restaccess.view.RestAccessInfoListView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * RESTアクセス情報[一覧]ビューの編集アクションクラス<BR>
 *
 */
public class RestAccessInfoModifyAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(RestAccessInfoModifyAction.class);

	/** アクションID */
	public static final String ID = RestAccessInfoModifyAction.class.getName();

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
		RestAccessInfoListView view = null;
		try {
			view = (RestAccessInfoListView) this.viewPart.getAdapter(RestAccessInfoListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		RestAccessInfoListComposite composite = (RestAccessInfoListComposite) view.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String RestAccessInfoId = null;
		if(list != null && list.size() > 0){
			managerName = (String) list.get(GetRestAccessInfoListTableDefine.MANAGER_NAME);
			RestAccessInfoId = (String) list.get(GetRestAccessInfoListTableDefine.REST_ACCESS_ID);
		}

		Table table = composite.getTableViewer().getTable();

		// 選択アイテムがある場合に、編集ダイアログを表示する
		if(RestAccessInfoId != null){
			RestAccessInfoCreateDialog dialog = new RestAccessInfoCreateDialog(view
					.getListComposite().getShell(), managerName,
					RestAccessInfoId, PropertyDefineConstant.MODE_MODIFY);
			if (dialog.open() == IDialogConstants.OK_ID) {
				int selectIndex = table.getSelectionIndex();
				composite.update();
				table.setSelection(selectIndex);
			}
		}
		else{
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.restaccess.8"));
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
				if(part instanceof RestAccessInfoListView){
					// Enable button when 1 item is selected
					RestAccessInfoListView view = (RestAccessInfoListView)part;

					if(view.getSelectedNum() == 1) {
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
