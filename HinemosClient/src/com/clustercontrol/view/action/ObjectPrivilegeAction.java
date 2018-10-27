/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.view.action;

import java.util.List;
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

import com.clustercontrol.accesscontrol.dialog.ObjectPrivilegeEditDialog;
import com.clustercontrol.accesscontrol.dialog.ObjectPrivilegeListDialog;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.calendar.view.CalendarListView;
import com.clustercontrol.calendar.view.CalendarPatternView;
import com.clustercontrol.infra.view.InfraFileManagerView;
import com.clustercontrol.infra.view.InfraManagementView;
import com.clustercontrol.jobmanagement.view.JobKickListView;
import com.clustercontrol.maintenance.view.MaintenanceListView;
import com.clustercontrol.monitor.view.MonitorListView;
import com.clustercontrol.notify.mail.view.MailTemplateListView;
import com.clustercontrol.notify.view.NotifyListView;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * オブジェクト権限設定を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class ObjectPrivilegeAction extends AbstractHandler implements IElementUpdater {

	public static final String ID = ObjectPrivilegeAction.class.getName();

	private static Log m_log = LogFactory.getLog(ObjectPrivilegeAction.class);

	protected IWorkbenchWindow window;
	protected IWorkbenchPart viewPart;

	protected Class<? extends ObjectPrivilegeTargetListView> listViewClass = ObjectPrivilegeTargetListView.class;

	/**
	 * Handler execution
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		ObjectPrivilegeTargetListView view = null; 
		try { 
			view = (ObjectPrivilegeTargetListView) this.viewPart.getAdapter(ObjectPrivilegeTargetListView.class); 
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		List<ObjectBean> objectBeans = view.getSelectedObjectBeans();
		if (objectBeans != null && objectBeans.size() > 0) {
			if (objectBeans.size() == 1) {
				// ダイアログを生成
				ObjectPrivilegeListDialog dialog = new ObjectPrivilegeListDialog(
						this.viewPart.getSite().getShell(),objectBeans.get(0).getManagerName(),
						objectBeans.get(0).getObjectId(), objectBeans.get(0).getObjectType(), view.getSelectedOwnerRoleId());
				// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
				if (dialog.open() == IDialogConstants.OK_ID) {
					view.update();
				}
			} else {
				// ダイアログを生成
				ObjectPrivilegeEditDialog dialog = new ObjectPrivilegeEditDialog(
						this.viewPart.getSite().getShell(),
						objectBeans,
						null,
						null);
				// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
				if (dialog.open() == IDialogConstants.OK_ID) {
					view.update();
				}
			}
		}
		return null;
	}
	/**
	 * Dispose
	 */
	@Override
	public void dispose(){
		this.window = null;
		this.viewPart = null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				if( part instanceof CalendarListView  ){
					this.setBaseEnabled( 0 < ((CalendarListView) part).getSelectedNum() );
				}else if( part instanceof CalendarPatternView ){
					this.setBaseEnabled( 0 < ((CalendarPatternView) part).getSelectedNum() );
				}else if( part instanceof JobKickListView ){
					this.setBaseEnabled( 0 < ((JobKickListView) part).getSelectedNum() );
				}else if( part instanceof NotifyListView ){
					this.setBaseEnabled( 0 < ((NotifyListView) part).getSelectedNum() );
				}else if( part instanceof MonitorListView ){
					this.setBaseEnabled( 0 < ((MonitorListView) part).getSelectedNum() );
				}else if( part instanceof MailTemplateListView ){
					this.setBaseEnabled( 0 < ((MailTemplateListView) part).getSelectedNum() );
				}else if( part instanceof MaintenanceListView ){
					this.setBaseEnabled( 0 < ((MaintenanceListView) part).getSelectedNum() );
				}else if( part instanceof InfraManagementView ){
					this.setBaseEnabled( 0 < ((InfraManagementView) part).getSelectedNum() );
				}else if( part instanceof InfraFileManagerView ){
					this.setBaseEnabled( 0 < ((InfraFileManagerView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}

}
