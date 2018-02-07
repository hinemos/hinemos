/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.action.GetNodeListTableDefine;
import com.clustercontrol.repository.preference.RepositoryPreferencePage;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.repository.view.NodeListView;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandCreator.PlatformType;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.repository.InvalidRole_Exception;

/**
 * プログラム実行を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class ProgramExecutionAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( ProgramExecutionAction.class );

	public static final String ID = ProgramExecutionAction.class.getName();

	//	 ----- instance フィールド ----- //

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);

		// ノード一覧より、選択されているノードのファシリティIDを取得
		NodeListView view = null;
		try {
			view = (NodeListView) this.viewPart
					.getAdapter(NodeListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		StructuredSelection selection = (StructuredSelection) view
				.getComposite().getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();

		if (list == null) {
			return null;
		}

		String managerName = (String) list.get(GetNodeListTableDefine.MANAGER_NAME);
		String facilityId = (String) list.get(GetNodeListTableDefine.FACILITY_ID);

		if (facilityId == null) {
			return null;
		}

		// プレファレンスページよりプログラム実行文字列を取得
		String execProg = ClusterControlPlugin.getDefault().getPreferenceStore().getString(
				RepositoryPreferencePage.P_PROGRAM_EXECUTION);

		// プログラム実行文字列の空チェック
		if (execProg.equals("")) {
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.repository.49"));
			return null;
		}

		// 置換対象文字列が含まれているか「#」の有無で確認する
		if (execProg.indexOf("#") != -1) {
			try {
				// マネージャへ文字列を送り置換したもので置き換える
				RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
				execProg = wrapper.replaceNodeVariable(facilityId, execProg);
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("run(), " + e.getMessage(), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}

		// 実行コマンドを実行できるように変形
		String[] args = null;
		try {
			String user = System.getProperty("user.name");
			args = CommandCreator.createCommand(user, execProg, PlatformType.WINDOWS, false);
		} catch (HinemosUnknown e) {
			m_log.warn("run(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		StringBuffer message = new StringBuffer();
		for (String arg : args) {
			message.append(arg + " ");
		}

		String messageStr = message.toString();
		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.repository.46", new String[]{messageStr}))) {

			// 実行する
			m_log.debug("program execution start : " + messageStr);
			try {
				new CommandExecutor(args).execute();
			} catch (Exception e) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.repository.48") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page && ClusterControlPlugin.isRAP() == false){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if(part instanceof NodeListView){
					// Enable button when 1 item is selected
					NodeListView view = (NodeListView)part;

					if(view.getSelectedNum() > 0) {
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
