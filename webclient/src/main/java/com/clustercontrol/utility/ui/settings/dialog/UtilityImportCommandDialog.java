/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.ui.settings.dialog;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.ui.settings.composite.UtilityUploadComponent;
import com.clustercontrol.utility.util.ClientPathUtil;
import com.clustercontrol.utility.util.FileUtil;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.ZipUtil;

public class UtilityImportCommandDialog extends CommonDialog {
	public UtilityImportCommandDialog(Shell parent) {
		super(parent);
	}

//	private static final int FILE_SIZE_LIMIT = 1024 * 1024 * 100;//100MB
	// ログ
	private static Log log = LogFactory.getLog( UtilityImportCommandDialog.class );
	/**
	 * ダイアログの最背面レイヤのカラム数
	 * 最背面のレイヤのカラム数のみを変更するとレイアウトがくずれるため、
	 * グループ化されているレイヤは全てこれにあわせる
	 */
	private final int DIALOG_WIDTH = 12;
	/** タイトルラベルのカラム数 */
	private final int TITLE_WIDTH = 4;
	/** テキストフォームのカラム数 */
	private final int FORM_WIDTH = 8;

	/** シェル */
	private Shell m_shell = null;

	/** アップロード用コンポジット */
	private UtilityUploadComponent uploadComponent;

	private String filePath;

	/**
	 * ダイアログエリアを生成します。
	 * <P>
	 *
	 *
	 * @param parent 親コンポジット
	 *
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();
		parent.getShell().setText(Messages.getString("string.import"));
		/**
		 * レイアウト設定
		 * ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		GridLayout baseLayout = new GridLayout(1, true);
		baseLayout.marginWidth = 10;
		baseLayout.marginHeight = 10;
		baseLayout.numColumns = DIALOG_WIDTH;
		//一番下のレイヤー
		parent.setLayout(baseLayout);

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = DIALOG_WIDTH;

		GridData gridData = null;

		Composite infoComposite = new Composite(parent, SWT.NONE);
		infoComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = DIALOG_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		infoComposite.setLayoutData(gridData);

		//マネージャ
		Label label = new Label(infoComposite, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", label);
		gridData = new GridData();
		gridData.horizontalSpan = TITLE_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("facility.manager") + " : ");
		Text text = new Text(infoComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = FORM_WIDTH;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		text.setLayoutData(gridData);
		text.setText(UtilityManagerUtil.getCurrentManagerName() + " ( " + EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getUrlListStr() + " )");

		uploadComponent = new UtilityUploadComponent( infoComposite, Messages.getString("string.import") + Messages.getString("file.name") + " : ", TITLE_WIDTH, FORM_WIDTH);

		m_shell.pack();
		m_shell.setSize(new Point(540, m_shell.getSize().y));

		// 画面中央に
		Display display = m_shell.getDisplay();
		m_shell.setLocation(
				(display.getBounds().width - m_shell.getSize().x) / 2, (display
						.getBounds().height - m_shell.getSize().y) / 2);
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 * @since 5.0.a
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 * @since 5.0.a
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 入力値チェックをします。
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

	@Override
	protected boolean action() {
		try {
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
			} else {
				FileUtil.moveFile2OtherDir(uploadComponent.getFilePath(), filePath + File.separator + uploadComponent.getFileName());
			}
			File tmpFile = new File(filePath);
			File[] tmpFiles = tmpFile.listFiles();
			if(tmpFiles != null){
				for(File file: tmpFiles){
					if(file != null && file.isDirectory() && 
							!file.getName().equals(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_INFRA)) && 
							!file.getName().equals(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_JOBMAP_IMAGE_FOLDER)) && 
							!file.getName().equals(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_NODEMAP_BG_FOLDER)) && 
							!file.getName().equals(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_NODEMAP_ICON_FOLDER))){
						FileUtil.moveAllFiles2OtherDir(file.getAbsolutePath(), tmpFile.getAbsolutePath());
						if (!file.delete())
							log.warn(String.format("Fail to delete Directory. %s", file.getAbsolutePath()));
					}
				}
			}
			return true;
		} catch (Exception e) {
			log.error("Unzip failed " + e.getMessage(),e);
			if(log.isDebugEnabled()){
				e.printStackTrace();
			}
		}
		return false;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}