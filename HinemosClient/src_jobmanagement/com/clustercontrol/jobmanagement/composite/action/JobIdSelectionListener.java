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

package com.clustercontrol.jobmanagement.composite.action;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.dialog.JobTreeDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブID参照ボタンSelectionAdapterクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class JobIdSelectionListener extends SelectionAdapter {
	//シェルを取得
	private Shell shell;

	// Manager
	private ManagerListComposite managerListComposite;

	/** ジョブユニットID */
	private Text m_textJobunitId = null;

	/** ジョブID */
	private Text m_textJobId = null;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_ownerRoleId = null;

	public JobIdSelectionListener(Shell shell, ManagerListComposite managerListComposite, Text jobunitId, Text jobId, RoleIdListComposite ownerRoleId) {
		super();
		this.managerListComposite = managerListComposite;
		this.m_textJobunitId = jobunitId;
		this.m_textJobId = jobId;
		this.m_ownerRoleId = ownerRoleId;
		this.shell = shell;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {

		String managerName = managerListComposite.getText();
		
		if( null != managerName ){
			// ジョブツリーダイアログ表示
			JobTreeDialog dialog = new JobTreeDialog(shell, managerName, this.m_ownerRoleId.getText(), false);
			if (dialog.open() == IDialogConstants.OK_ID) {
				JobTreeItem selectItem = dialog.getSelectItem().get(0);
				if (selectItem.getData().getType() != JobConstant.TYPE_COMPOSITE) {
					m_textJobId.setText(selectItem.getData().getId());
					m_textJobunitId.setText(selectItem.getData().getJobunitId());
				} else {
					m_textJobId.setText("");
					m_textJobunitId.setText("");
				}
			}
		}else{
			MessageDialog.openInformation( shell, Messages.getString("message"), Messages.getString("multimanager.managername.required.message") );
		}
	}
}
