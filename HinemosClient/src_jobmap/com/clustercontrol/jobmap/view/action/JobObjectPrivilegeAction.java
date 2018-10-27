/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.accesscontrol.dialog.ObjectPrivilegeEditDialog;
import com.clustercontrol.accesscontrol.dialog.ObjectPrivilegeListDialog;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmap.figure.JobFigure;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.view.action.ObjectPrivilegeAction;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブの作成・変更ダイアログによる、ジョブのオブジェクト権限設定を行うクライアント側アクションクラス<BR>
 * 
 * @version 4.1.0
 * @since 4.1.0
 */
public class JobObjectPrivilegeAction extends ObjectPrivilegeAction {
	private final static Log m_log = LogFactory.getLog( JobMapActionUtil.class );
	public static final String ID = BaseAction.ActionIdBase + JobObjectPrivilegeAction.class.getSimpleName();

	/**
	 * Handler execution
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		JobTreeView view = JobMapActionUtil.getJobTreeView();

		List<ObjectBean> objectBeans = view.getSelectedObjectBeans();
		if (objectBeans != null && objectBeans.size() > 0) {
			if (objectBeans.size() == 1) {
				// ダイアログを生成
				ObjectPrivilegeListDialog dialog = new ObjectPrivilegeListDialog(
						this.viewPart.getSite().getShell(),
						objectBeans.get(0).getManagerName(),
						objectBeans.get(0).getObjectId(),
						objectBeans.get(0).getObjectType(),
						view.getSelectedOwnerRoleId());
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
					// ジョブの場合は、登録されていない場合があるためupdate()を実行しない。
					// view.update();
				}
			}
		} else {
			m_log.debug("objectBeans is null");
		}
		
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}
		viewPart = page.getActivePart();
		
		JobTreeItem jobTreeItem = null;
		if (viewPart instanceof JobMapEditorView) {
			JobMapEditorView view = (JobMapEditorView) viewPart;
			JobFigure figure = (JobFigure) view.getCanvasComposite()
					.getSelection();
			if (figure == null) {
				return;
			}
			
			jobTreeItem =  figure.getJobTreeItem();
		} else if (viewPart instanceof JobTreeView) {
			JobTreeView view = (JobTreeView) viewPart;
			jobTreeItem = view.getSelectJobTreeItem();
		}
		
		if (jobTreeItem == null) {
			this.setBaseEnabled(false);
			return;
		}
		
		this.setBaseEnabled(jobTreeItem.getData().getType() == JobConstant.TYPE_JOBUNIT);
	}
}