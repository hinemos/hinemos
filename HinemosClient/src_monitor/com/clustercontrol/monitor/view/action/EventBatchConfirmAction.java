/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.view.action;

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

import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.dialog.EventBatchConfirmDialog;
import com.clustercontrol.monitor.util.EventBatchConfirmPropertyUtil;
import com.clustercontrol.monitor.util.MonitorEndpointWrapper;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.ws.monitor.EventBatchConfirmInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 監視[一括確認]ダイアログによる確認の更新処理を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class EventBatchConfirmAction extends AbstractHandler {

	// ログ
	private static Log m_log = LogFactory.getLog( EventBatchConfirmAction.class );

	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/** アクションID */
	public static final String ID = EventBatchConfirmAction.class.getName();

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * 監視[一括確認]ダイアログで指定された条件に一致するイベント情報の確認を更新します。
	 * <p>
	 * <ol>
	 * <li>監視[一括確認]ダイアログを表示します。</li>
	 * <li>ダイアログで指定された更新条件を取得します。</li>
	 * <li>スコープツリーで選択されているアイテムより、ファシリティIDを取得します。</li>
	 * <li>ファシリティIDと更新条件に一致するイベント情報の確認を一括更新します。 </li>
	 * <li>監視[イベント]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.monitor.dialog.EventBatchConfirmDialog
	 * @see com.clustercontrol.monitor.view.EventView#update()
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

		EventBatchConfirmDialog dialog = new EventBatchConfirmDialog(
				this.viewPart.getSite().getShell());

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
			facilityId = null;
			managerName = item.getData().getFacilityId();
		}else{
			facilityId = item.getData().getFacilityId();
			FacilityTreeItem manager = ScopePropertyUtil.getManager(item);
			managerName = manager.getData().getFacilityId();
		}

		if (dialog.open() == IDialogConstants.OK_ID) {
			Property condition = dialog.getInputData();
			PropertyUtil.deletePropertyDefine(condition);
			try {
				MonitorEndpointWrapper wrapper = MonitorEndpointWrapper.getWrapper(managerName);
				EventBatchConfirmInfo info = EventBatchConfirmPropertyUtil.property2dto(condition);
				wrapper.modifyBatchConfirm(ConfirmConstant.TYPE_CONFIRMED, facilityId, info);
				view.update(false);
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (HinemosUnknown_Exception e) {
				MessageDialog.openError(null, Messages.getString("message"),
						Messages.getString("message.monitor.60") + ", " + HinemosMessage.replace(e.getMessage()));
			} catch (Exception e) {
				m_log.warn("run() modifyBatchConfirm, " + e.getMessage(), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
		return null;
	}
}
