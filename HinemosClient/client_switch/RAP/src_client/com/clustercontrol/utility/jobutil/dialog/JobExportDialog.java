/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.jobutil.dialog;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.jobutil.util.JobStringUtil;
import com.clustercontrol.utility.settings.ui.constant.XMLConstant;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.util.ClientPathUtil;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブのエクスポートダイアログ
 * 
 * @version 6.1.0
 * @since 6.1.0
 * 
 */
public class JobExportDialog extends CommonDialog {
	public JobExportDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 240);
	}

	protected Shell shell;
	protected Button btnScope;
	protected Button btnNotify;
	protected Text txtFileName;
	private Boolean isNotify;
	private Boolean isScope;
	private File file = new File(MultiManagerPathUtil.getDirectoryPathTemporary(SettingToolsXMLPreferencePage.KEY_XML) +
			File.separator +
			MultiManagerPathUtil.getXMLFileName(XMLConstant.DEFAULT_XML_JOB_MST)); 
	private JobInfo item;
	
	@Override
	protected void customizeDialog(Composite parent) {
		shell = parent.getShell();
		shell.setText(Messages.getString("dialog.job.export"));
		
		// レイアウト
		GridLayout baseLayout = new GridLayout(3, false);
		baseLayout.marginWidth = 10;
		baseLayout.marginHeight = 10;
		//一番下のレイヤー
		parent.setLayout(baseLayout);

		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 5;
		layout.marginHeight = 5;

		Composite infoComposite = new Composite(parent, SWT.NONE);
		infoComposite.setLayout(layout);
		infoComposite.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, true));
		
		Group grpJob = new Group(infoComposite, SWT.NONE);
		grpJob.setLayout(new GridLayout(3, false));
		grpJob.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, false, 3, 1));
		grpJob.setText(Messages.getString("dialog.job.export.confirm",
				new String[]{ JobStringUtil.toJobTypeString(item.getType()) }));

		Label lblJobId = new Label(grpJob, SWT.NONE);
		lblJobId.setText(com.clustercontrol.util.Messages.getString("job.id") + " : ");
		lblJobId.setLayoutData(new GridData(GridData.FILL, SWT.FILL, false, false, 1, 1));
		
		Text txtJobId = new Text(grpJob, SWT.BORDER);
		txtJobId.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, false, 2, 1));
		txtJobId.setText(item.getId() != null ? item.getId() : "");
		txtJobId.setEditable(false);
		
		Label lblJobName = new Label(grpJob, SWT.NONE);
		lblJobName.setText(com.clustercontrol.util.Messages.getString("job.name") + " : ");
		lblJobName.setLayoutData(new GridData(GridData.FILL, SWT.FILL, false, false, 1, 1));
		
		Text txtJobName = new Text(grpJob, SWT.BORDER);
		txtJobName.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, false, 2, 1));
		txtJobName.setText(item.getName() != null ? item.getName() : "");
		txtJobName.setEditable(false);
		
		btnScope = new Button(infoComposite, SWT.CHECK);
		btnScope.setText(Messages.getString("dialog.job.scope.setting"));
		btnScope.setLayoutData(new GridData(GridData.FILL, SWT.FILL, false, false, 3, 1));

		btnNotify = new Button(infoComposite, SWT.CHECK);
		btnNotify.setText(Messages.getString("dialog.job.notify.setting"));
		btnNotify.setLayoutData(new GridData(GridData.FILL, SWT.FILL, false, false, 3, 1));
		
		Label lblExport = new Label(infoComposite, SWT.NONE);
		lblExport.setText(Messages.getString("string.export") + com.clustercontrol.util.Messages.getString("file.name") + " : ");
		lblExport.setLayoutData(new GridData(GridData.FILL, SWT.FILL, false, false, 1, 1));
		
		txtFileName = new Text(infoComposite, SWT.BORDER);
		txtFileName.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, false, 1, 1));

		if (file != null) {
			txtFileName.setText(file.getName());
		}

		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(
				shell.getSize().x > getInitialSize().x ? shell.getSize().x : getInitialSize().x,
				shell.getSize().y));
		
		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}
	
	/**
	 * ＯＫボタンのテキストを返します。
	 * 
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}
	
	
	@Override
	protected boolean action() {
		isScope = btnScope.getSelection();
		isNotify = btnNotify.getSelection();
		if (txtFileName.getText().isEmpty()) {
			return false;
		}

		String filePath;
		String parentPath = MultiManagerPathUtil.getDirectoryPathTemporary(SettingToolsXMLPreferencePage.KEY_XML);
		ClientPathUtil pathUtil = ClientPathUtil.getInstance();
		if (pathUtil.lock(parentPath)) {
			filePath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		} else {
			// ロックが取得できない場合、アンロック後にロック
			pathUtil.unlock(parentPath);
			pathUtil.lock(parentPath);
			filePath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		}
		
		file = new File(filePath + File.separator + txtFileName.getText());
		return true;
	}
	
	public Boolean isScope() {
		return isScope;
	}
	
	public Boolean isNotify() {
		return isNotify;
	}
	
	public String getFileName() {
		return file.getAbsolutePath();
	}
	
	/**
	 * キャンセルボタンのテキストを返します。
	 * 
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("close");
	}

	public void setSelectJob(JobTreeItem item) {
		this.item = item.getData();
		UtilityManagerUtil.setCurrentManagerName(JobTreeItemUtil.getManager(item).getData().getName());
		file = new File(MultiManagerPathUtil.getDirectoryPathTemporary(SettingToolsXMLPreferencePage.KEY_XML) +
				File.separator +
				MultiManagerPathUtil.getXMLFileName(XMLConstant.DEFAULT_XML_JOB_MST)); 
	}
}