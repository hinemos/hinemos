/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.CommandTemplateResponse;

import com.clustercontrol.notify.action.GetCommandTemplate;
import com.clustercontrol.notify.action.GetCommandTemplateTableDefine;
import com.clustercontrol.notify.composite.CommandTemplateListComposite;
import com.clustercontrol.notify.dialog.CommandTemplateShowDialog;
import com.clustercontrol.notify.view.CommandTemplateListView;

/**
 * 通知[コマンド通知テンプレート]ビューの参照アクションクラス<BR>
 */
public class CommandTemplateShowAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CommandTemplateShowAction.class);

	/** アクションID */
	public static final String ID = CommandTemplateShowAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	@Override
	public void dispose() {
		viewPart = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		viewPart = HandlerUtil.getActivePart(event);
		CommandTemplateListView view = null;
		try {
			view = (CommandTemplateListView) viewPart.getAdapter(CommandTemplateListView.class);
		} catch (Exception e) {
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		CommandTemplateListComposite composite = (CommandTemplateListComposite) view.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = (String) list.get(GetCommandTemplateTableDefine.MANAGER_NAME);
		String templateId = (String) list.get(GetCommandTemplateTableDefine.COMMAND_TEMPLATE_ID);
		CommandTemplateResponse commandTemplate = new GetCommandTemplate().getCommandTemplate(managerName, templateId);

		CommandTemplateShowDialog dialog = new CommandTemplateShowDialog(
				view.getListComposite().getShell(), managerName, commandTemplate);

		dialog.open();
		return null;
	}

	/**
	 * 指定されたコマンド通知テンプレートIDをもとに参照ダイアログを表示する
	 * @param shell
	 * @param managerName
	 * @param templateId
	 */
	public void openDialog(Shell shell, String managerName, String templateId) {
		CommandTemplateShowDialog dialog = null;
		CommandTemplateResponse commandTemplate = new GetCommandTemplate().getCommandTemplate(managerName, templateId);

		dialog = new CommandTemplateShowDialog(shell, managerName, commandTemplate);
		dialog.open();
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			boolean editEnable = false;
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();
				if(part instanceof CommandTemplateListView){
					// Enable button when 1 item is selected
					CommandTemplateListView view = (CommandTemplateListView) part;
					if (view.getSelectedNum() == 1) {
						editEnable = true;
					}
				}
				setBaseEnabled(editEnable);
			} else {
				setBaseEnabled(editEnable);
			}
		}
	}
}
