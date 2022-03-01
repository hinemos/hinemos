/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.jobutil.dialog;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openapitools.client.model.JobInfoResponse;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.jobutil.util.JobStringUtil;
import com.clustercontrol.utility.settings.ui.constant.XMLConstant;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.ui.settings.composite.UtilityUploadComponent;
import com.clustercontrol.utility.util.ClientPathUtil;
import com.clustercontrol.utility.util.FileUtil;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.ZipUtil;

/**
 * ジョブのインポートダイアログ
 * 
 * @version 6.1.0
 * @since 6.1.0
 * 
 */
public class JobImportDialog extends CommonDialog {
	public JobImportDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 270);
	}

	/** ログ出力用 */
	private static Log log = LogFactory.getLog(JobImportDialog.class);
	/** アップロード用コンポジット */
	private UtilityUploadComponent uploadComponent;
	
	protected Shell shell;
	protected Button btnScope;
	protected Button btnNotify;
	private boolean isScope;
	private boolean isNotify;
	private String fileName = MultiManagerPathUtil.getDirectoryPathTemporary(SettingToolsXMLPreferencePage.KEY_XML) +
			File.separator +
			MultiManagerPathUtil.getXMLFileName(XMLConstant.DEFAULT_XML_JOB_MST); 
	private JobInfoWrapper item;
	
	@Override
	protected void customizeDialog(Composite parent) {
		shell = parent.getShell();
		shell.setText(Messages.getString("dialog.job.import"));
		
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
		grpJob.setText(Messages.getString("dialog.job.import.confirm",
				new String[]{ JobStringUtil.toJobTypeStringForEnum(item.getType()) }));
		
		Label lblJobId = new Label(grpJob, SWT.NONE);
		lblJobId.setText(item.getType().equals(JobInfoResponse.TypeEnum.MANAGER ) ?
				com.clustercontrol.util.Messages.getString("facility.managername") + " : " :
				com.clustercontrol.util.Messages.getString("job.id") + " : ");
		lblJobId.setLayoutData(new GridData(GridData.FILL, SWT.CENTER, false, false, 1, 1));
		
		Text txtJobId = new Text(grpJob, SWT.BORDER);
		txtJobId.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, false, 2, 1));
		txtJobId.setText(item.getId() != null ? item.getId() : "---");
		txtJobId.setEditable(false);
		
		if (!item.getType().equals(JobInfoResponse.TypeEnum.MANAGER)) {
			Label lblJobName = new Label(grpJob, SWT.NONE);
			lblJobName.setText(com.clustercontrol.util.Messages.getString("job.name") + " : ");
			lblJobName.setLayoutData(new GridData(GridData.FILL, SWT.CENTER, false, false, 1, 1));

			Text txtJobName = new Text(grpJob, SWT.BORDER);
			txtJobName.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, false, 2, 1));
			txtJobName.setText(item.getName() != null ? item.getName() : "---");
			txtJobName.setEditable(false);
		}
		
		btnScope = new Button(infoComposite, SWT.CHECK);
		btnScope.setText(Messages.getString("dialog.job.scope.setting"));
		btnScope.setLayoutData(new GridData(GridData.FILL, SWT.FILL, false, false, 3, 1));

		btnNotify = new Button(infoComposite, SWT.CHECK);
		btnNotify.setText(Messages.getString("dialog.job.notify.setting"));
		btnNotify.setLayoutData(new GridData(GridData.FILL, SWT.FILL, false, false, 3, 1));
		
		uploadComponent = new UtilityUploadComponent( infoComposite, Messages.getString("string.import") + com.clustercontrol.util.Messages.getString("file.name") + " : ", 1, 2);
		
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
		if (uploadComponent.getFilePath().isEmpty() && uploadComponent.getFileName().isEmpty())
			return false;
		
		try {
			String filePath;
			String parentPath = MultiManagerPathUtil.getDirectoryPathTemporary(SettingToolsXMLPreferencePage.KEY_XML);
			ClientPathUtil pathUtil = ClientPathUtil.getInstance();
			if(pathUtil.lock(parentPath)) {
				filePath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
			} else {
				//現在のロック機構ではOOME発生などでunlockが呼ばれない場合、ロック取得に失敗し続けるので、
				//ロックが取得できない際は解放して取り直す
				pathUtil.unlock(parentPath);
				pathUtil.lock(parentPath);
				filePath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
			}
			if(uploadComponent.getFileName().endsWith(".zip") || uploadComponent.getFileName().endsWith(".ZIP") || uploadComponent.getFileName().endsWith(".Zip")){
				ZipUtil.decompress(new File(uploadComponent.getFilePath()), filePath);
				fileName = filePath + File.separator + MultiManagerPathUtil.getXMLFileName(XMLConstant.DEFAULT_XML_JOB_MST);
			} else {
				fileName = filePath + File.separator + uploadComponent.getFileName();
				FileUtil.moveFile2OtherDir(uploadComponent.getFilePath(), fileName);
			}
			File tmpFile = new File(filePath);
			
			File[] tmpFiles = tmpFile.listFiles();
			if (tmpFiles != null) {
				for (File file : tmpFiles) {
					if (file != null && file.isDirectory() && 
							!file.getName().equals(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_INFRA)) && 
							!file.getName().equals(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_JOBMAP_IMAGE_FOLDER)) && 
							!file.getName().equals(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_NODEMAP_BG_FOLDER)) && 
							!file.getName().equals(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_NODEMAP_ICON_FOLDER))) {
						FileUtil.moveAllFiles2OtherDir(file.getAbsolutePath(), tmpFile.getAbsolutePath());
						if (!file.delete())
							log.warn(String.format("Fail to delete file. %s", file.getAbsolutePath()));
					}
				}
			}
			
			return true;
		} catch (Exception e) {
		}
		return false;
	}
	
	public Boolean isScope() {
		return isScope;
	}
	
	public Boolean isNotify() {
		return isNotify;
	}
	
	public String getFileName() {
		return fileName;
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

	public void setSelectJob(JobTreeItemWrapper item) {
		this.item = item.getData();
		UtilityManagerUtil.setCurrentManagerName(JobTreeItemUtil.getManager(item).getData().getName());
	}
	
	/**
	 * * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		if ("".equals(uploadComponent.getFileName())) {
			return createValidateResult(Messages.getString("message.hinemos.1"),
					Messages.getString("message.infra.specify.item",
							new Object[]{Messages.getString("string.import") + Messages.getString("file.name")}));
		}
		if (!uploadComponent.isReady()) {
			return createValidateResult(Messages.getString("upload"), Messages.getString("upload.busy.message"));
		}

		return super.validate();
	}

	/**
	 * 無効な入力値の情報を設定します
	 *
	 */
	protected ValidateResult createValidateResult(String id, String message) {
		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}
	
}