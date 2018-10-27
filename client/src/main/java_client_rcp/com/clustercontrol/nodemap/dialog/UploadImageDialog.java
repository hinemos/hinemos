/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.dialog;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.nodemap.util.ImageManager;
import com.clustercontrol.nodemap.util.NodeMapEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.nodemap.InvalidRole_Exception;

/**
 * アップロード用ダイアログ
 * @since 1.0.0
 */
public class UploadImageDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( UploadImageDialog.class );

	// ----- instance フィールド ----- //

	//Composite用オブジェクト
	private String fileParent  = null;
	private Text fileName  = null;
	private Button radioIcon = null;
	private Button radioBg = null;
	private Button buttonHost = null; // 検索ボタン
	private String m_managerName = null;

	// ----- コンストラクタ ----- //
	/**
	 * 指定したファシリティIDをホストノードとして仮想ノードを検索、登録するダイアログのインスタンスを返す。
	 * @param parent 親のシェルオブジェクト
	 * @param facilityId 初期表示するノードのファシリティID
	 */
	public UploadImageDialog(Shell parent, String managerName) {
		super(parent);
		m_managerName = managerName;
	}

	// ----- instance メソッド ----- //
	/**
	 * ダイアログエリアを生成します
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		//共通変数
		GridData gridData = null; // GridDataは毎回必ず作成すること
		GridLayout layout = null;

		////////////////////////////////////////
		// ダイアログ全体の設定
		////////////////////////////////////////

		Shell shell = this.getShell();

		// タイトル
		shell.setText(com.clustercontrol.nodemap.messages.Messages.getString("file.upload"));

		layout = new GridLayout(1,true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 1;
		parent.setLayout(layout);

		Group group = new Group(parent, SWT.SHADOW_NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;

		layout = new GridLayout(2, false);
		group.setLayout(layout);
		group.setLayoutData(gridData);

		// 1-1
		this.radioIcon = new Button(group, SWT.RADIO);
		this.radioIcon.setLayoutData(gridData);
		this.radioIcon.setText(com.clustercontrol.nodemap.messages.Messages.getString("file.upload.icon"));
		// 1-2 dummy
		new Label(group,SWT.NONE);//ダミーラベル

		// 2-1
		this.radioBg = new Button(group, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.radioBg.setLayoutData(gridData);
		this.radioBg.setText(com.clustercontrol.nodemap.messages.Messages.getString("file.upload.bg"));
		// 2-2 dummy
		new Label(group,SWT.NONE);//ダミーラベル

		// 3-1
		this.fileName = new Text(group, SWT.BORDER | SWT.SINGLE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.minimumWidth = 80;
		this.fileName.setLayoutData(gridData);
		this.fileName.setEnabled(true); // 編集可

		// 3-2
		this.buttonHost = new Button(group, SWT.NONE);
		gridData = new GridData();
		this.buttonHost.setLayoutData(gridData);
		this.buttonHost.setText(Messages.getString("refer"));
		this.buttonHost.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				// ファイルダイアログを開く
				FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);

				String defaultFileName = "";
				if(defaultFileName != null && !"".equals(defaultFileName.trim())){
					dialog.setFileName(defaultFileName.trim());
				}
				dialog.setFilterExtensions(new String[]{"*.gif;*.png;*.jpg;*.bmp", "*.*"});

				String path = dialog.open();
				if(path != null && !"".equals(path)){
					File file = new File(path);
					fileParent = file.getParent();

					String[] fileNameArray = dialog.getFileNames();

					m_log.debug("filepath =" + path);
					m_log.debug("filename =" + fileName);
					if(fileNameArray != null && fileNameArray.length > 0){
						String fileNameStr = "";
						for (String str : fileNameArray) {
							fileNameStr += "\"" + str + "\" ";
						}
						fileName.setText(fileNameStr);
					}
				}
			}
		});
	}

	/**
	 * OKボタンのテキストを返します。
	 * 
	 * @return OKボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("close");
	}

	/**
	 * 入力値チェックをします。
	 * 
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {

		ValidateResult result = null;

		// TODO

		return result;
	}

	/**
	 * OKボタンのメイン処理
	 */
	@Override
	protected void okPressed() {
		/*
		 * filenameは  "file1.gif" "file2.gif" "file3.gif" という内容。
		 * " " でsplitして配列に戻す。
		 */
		for (String filename : fileName.getText().split("\" *\"")) {
			// 先頭のファイルは頭に「"」がつくので消す。
			filename = filename.replaceAll("\"", "");
			// 念のため空白も削除しておく。
			filename = filename.trim();
			String filepath = fileParent + "\\" + filename;
			m_log.debug("filepath = " + filepath);
			try {
				NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(m_managerName);
				if (radioBg.getSelection()) {
					boolean okFlag = false;
					if (wrapper.isBgImage(filename)) {
						// 上書きになります。
						if (MessageDialog.openQuestion(
								null, com.clustercontrol.util.Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("file.overwrite.question") +
								" [" + filepath + "]")) {
							okFlag = true;
						}
					} else {
						// 新規になります。
						okFlag = true;
					}
					if (okFlag) {
						ImageManager.bgUpload(m_managerName, filepath, filename);
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								com.clustercontrol.nodemap.messages.Messages.getString("file.upload.success") +
								" [" + filepath + "]");
					}
				} else if (radioIcon.getSelection()) {
					boolean okFlag = false;
					if (wrapper.isIconImage(filename)) {
						// 上書きになります。
						if (MessageDialog.openQuestion(
								null,
								com.clustercontrol.util.Messages.getString("confirmed"),
								com.clustercontrol.nodemap.messages.Messages.getString("file.overwrite.question") +
								" [" + filepath + "]")) {
							okFlag = true;
						}
					} else {
						// 新規になります。
						okFlag = true;
					}
					if (okFlag) {
						ImageManager.iconUpload(m_managerName, filepath, filename);
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								com.clustercontrol.nodemap.messages.Messages.getString("file.upload.success") +
								" [" + filepath + "]");
					}
				} else {
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							com.clustercontrol.nodemap.messages.Messages.getString("file.radio.select"));
				}
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						e.getMessage() + " [" + filepath + "]");
			}
		}
		super.okPressed();
	}
}
