/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmap.view.action.CreateJobLinkRcvJobAction;
import com.clustercontrol.jobmap.view.action.CreateJobLinkSendJobAction;
import com.clustercontrol.jobmap.view.action.CreateApprovalJobAction;
import com.clustercontrol.jobmap.view.action.CreateFileCheckJobAction;
import com.clustercontrol.jobmap.view.action.CreateMonitorJobAction;
import com.clustercontrol.jobmap.view.action.CreateFileJobAction;
import com.clustercontrol.jobmap.view.action.CreateJobAction;
import com.clustercontrol.jobmap.view.action.CreateJobNetAction;
import com.clustercontrol.jobmap.view.action.CreateJobUnitAction;
import com.clustercontrol.jobmap.view.action.CreateMonitorJobAction;
import com.clustercontrol.jobmap.view.action.CreateReferJobAction;
import com.clustercontrol.jobmap.view.action.CreateResourceJobAction;
import com.clustercontrol.jobmap.view.action.CreateRpaJobAction;
import com.clustercontrol.jobmap.view.action.DeleteJobAction;
import com.clustercontrol.jobmap.view.action.EditModeAction;
import com.clustercontrol.jobmap.view.action.JobObjectPrivilegeAction;
import com.clustercontrol.jobmap.view.action.ModifyJobAction;
import com.clustercontrol.jobmap.view.action.RunJobAction;
import com.clustercontrol.utility.jobutil.ui.views.commands.ExportJobCommand;
import com.clustercontrol.utility.jobutil.ui.views.commands.ImportJobCommand;

/**
 * ノードマップビューを描画するためのクラス。
 * コントロール部分はMapViewController
 */
public class JobMapEditorView extends JobMapView {

	// ログ
	private static Log m_log = LogFactory.getLog( JobMapEditorView.class );

	public static final String ID = JobMapEditorView.class.getName();

	public JobMapEditorView(){
		m_log.debug("JobMapEditorView constructor");
	}

	@Override
	public void applySetting() {
		m_canvasComposite.applySetting();
		updateNotManagerAccess();
		this.stopAutoReload();
	}

	/**
	 * ビューのアクションの有効/無効を設定します。
	 * 
	 * @param type ジョブ種別
	 * @param selection ボタン（アクション）を有効にするための情報
	 * 
	 * @see com.clustercontrol.bean.JobConstant
	 */
	public void setEnabledAction(JobTreeItemWrapper jobTreeItem) {
		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(CreateJobUnitAction.ID, null);
			service.refreshElements(CreateJobNetAction.ID, null);
			service.refreshElements(CreateJobAction.ID, null);
			service.refreshElements(CreateFileJobAction.ID, null);
			service.refreshElements(CreateReferJobAction.ID, null);
			service.refreshElements(CreateApprovalJobAction.ID, null);
			service.refreshElements(CreateMonitorJobAction.ID, null);
			service.refreshElements(CreateFileCheckJobAction.ID, null);
			service.refreshElements(CreateJobLinkSendJobAction.ID, null);
			service.refreshElements(CreateJobLinkRcvJobAction.ID, null);
			service.refreshElements(CreateResourceJobAction.ID, null);
			service.refreshElements(CreateRpaJobAction.ID, null);
			service.refreshElements(DeleteJobAction.ID, null);
			service.refreshElements(ModifyJobAction.ID, null);
			service.refreshElements(JobObjectPrivilegeAction.ID, null);
			service.refreshElements(RunJobAction.ID, null);
			service.refreshElements(EditModeAction.ID, null);
			service.refreshElements(ImportJobCommand.ID, null);
			service.refreshElements(ExportJobCommand.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}
}
