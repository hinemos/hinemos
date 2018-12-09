/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view.action;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.dialog.EventReportDialog;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 監視[イベントのダウンロード]ダイアログによるイベントの帳票出力処理を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.1.0
 */
public class EventReportAction extends AbstractHandler {

	// ログ
	private static Log m_log = LogFactory.getLog( EventReportAction.class );

	/** アクションID */
	public static final String ID = EventReportAction.class.getName();

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
	 * 監視[イベントのダウンロード処理]ダイアログで指定された条件に一致するイベントを取得し、
	 * 帳票を出力します。
	 * <p>
	 * <ol>
	 * <li>監視[イベントのダウンロード処理]ダイアログを表示します。</li>
	 * <li>ダイアログで指定された出力情報、検索条件を取得します。</li>
	 * <li>スコープツリーで選択されているアイテムより、ファシリティIDを取得します。</li>
	 * <li>ファシリティIDと検索条件に一致するイベント情報一覧を取得します。 </li>
	 * <li>帳票を出力します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * @see com.clustercontrol.monitor.dialog.EventReportDialog
	 * @see com.clustercontrol.monitor.util.EventReportGenerator#run(String, Property, ArrayList, int, String)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		EventView view = null;
		try {
			view = (EventView) this.viewPart.getAdapter(EventView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		FacilityTreeItem item = view.getScopeTreeComposite().getSelectItem();
		if( null == item || item.getData().getFacilityType() == FacilityConstant.TYPE_COMPOSITE ){
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.monitor.47"));
			return null;
		}

		String managerName;
		String facilityId;
		if( item.getData().getFacilityType() == FacilityConstant.TYPE_MANAGER ){
			facilityId = RoleSettingTreeConstant.ROOT_ID;
			managerName = item.getData().getFacilityId();
		}else{
			facilityId = item.getData().getFacilityId();
			FacilityTreeItem manager = ScopePropertyUtil.getManager(item);
			managerName = manager.getData().getFacilityId();
		}

		// ダイアログを生成
		EventReportDialog dialog = new EventReportDialog( this.viewPart.getSite().getShell(), managerName, facilityId );

		// ダイアログにて出力が選択された場合、帳票出力
		int btnId = dialog.open();
		if( btnId == IDialogConstants.OK_ID ) {
			m_log.debug( dialog.getFilePath() + " exported" );
			if( !ClusterControlPlugin.isRAP() ){
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.monitor.45", new String[]{ dialog.getFileName(), managerName }));
			}
		} else if( btnId == IDialogConstants.CANCEL_ID ) {
			// Do nothing
		} else {
			MessageDialog.openError(
				null,
				Messages.getString("failed"),
				Messages.getString("message.monitor.46"));
		}
		return null;
	}
}
