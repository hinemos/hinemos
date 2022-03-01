/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.DownloadNodeConfigFileRequest;
import org.openapitools.client.model.GetNodeListRequest;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.nodemap.util.NodeMapRestClientWrapper;
import com.clustercontrol.nodemap.util.NodemapUtil;
import com.clustercontrol.nodemap.view.action.NodeListDownloadAction;
import com.clustercontrol.repository.bean.NodeConfigSettingConstant;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;

/**
 * 検索結果ノードより構成情報ファイルダウンロードを行うダイアログクラス<BR>
 *
 * @version 6.2.0
 */
public class NodeListDownloadDialog extends CommonDialog {

	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeListDownloadAction.class);

	// ----- instance フィールド ----- //

	/** ファシリティIDマップ（マネージャ名, ファシリティIDリスト） */
	private HashMap<String, List<String>> m_facilityIdMap = new HashMap<>();

	/** 検索条件 */
	private GetNodeListRequest m_nodeFilterInfo = null;

	/** ファイルパス */
	private String m_filePath = "";

	/** ファイル名 */
	private String m_fileName = "";

	/** 収集対象構成情報（OS） チェックボックス */
	private Button m_checkTargetConfigOs = null;
	/** 収集対象構成情報（HW-CPU情報） チェックボックス */
	private Button m_checkTargetConfigHwCpu = null;
	/** 収集対象構成情報（HW-メモリ情報） チェックボックス */
	private Button m_checkTargetConfigHwMemory = null;
	/** 収集対象構成情報（HW-NIC情報） チェックボックス */
	private Button m_checkTargetConfigHwNic = null;
	/** 収集対象構成情報（HW-ディスク情報） チェックボックス */
	private Button m_checkTargetConfigHwDisk = null;
	/** 収集対象構成情報（HW-ファイルシステム情報） チェックボックス */
	private Button m_checkTargetConfigHwFilesystem = null;
	/** 収集対象構成情報（ホスト名情報） チェックボックス */
	private Button m_checkTargetConfigHwHostname = null;
	/** 収集対象構成情報（ネットワーク接続） チェックボックス */
	private Button m_checkTargetConfigNetstat = null;
	/** 収集対象構成情報（プロセス） チェックボックス */
	private Button m_checkTargetConfigProcess = null;
	/** 収集対象構成情報（パッケージ） チェックボックス */
	private Button m_checkTargetConfigPackage = null;
	/** 収集対象構成情報（ノード変数情報） チェックボックス */
	private Button m_checkTargetConfigNodeVariable = null;
	/** 収集対象構成情報（個別導入製品情報） チェックボックス */
	private Button m_checkTargetConfigProduct = null;
	/** 収集対象構成情報（ライセンス情報） チェックボックス */
	private Button m_checkTargetConfigLicense = null;
	/** 収集対象構成情報（ユーザ任意情報） チェックボックス */
	private Button m_checkTargetConfigCustom = null;
	
	// ----- コンストラクタ ----- //

	/**
	 * 指定した形式のダイアログのインスタンスを返します。
	 *
	 * @param parent 親シェル
	 */
	public NodeListDownloadDialog(Shell parent, HashMap<String, List<String>> facilityIdMap, GetNodeListRequest nodeFilterInfo) {
		super(parent);
		this.m_facilityIdMap = facilityIdMap;
		this.m_nodeFilterInfo = nodeFilterInfo;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(530, 350);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.nodemap.download.node.config"));

		// レイアウト
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		// ラベル
		Label label = new Label(parent, SWT.LEFT);
		label.setText(Messages.getString("nodemap.download.node.config"));

		
		// 構成情報収集対象グループ
		Composite composite = new Group(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 2;
		composite.setLayout(layout);

		// OS情報
		this.m_checkTargetConfigOs = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigOs.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigOs.setText(NodeConfigSettingItem.OS.displayName());

		// HW-CPU情報
		this.m_checkTargetConfigHwCpu = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigHwCpu.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwCpu.setText(NodeConfigSettingItem.HW_CPU.displayName());

		// HW-メモリ情報
		this.m_checkTargetConfigHwMemory = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigHwMemory.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwMemory.setText(NodeConfigSettingItem.HW_MEMORY.displayName());

		// HW-NIC情報
		this.m_checkTargetConfigHwNic = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigHwNic.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwNic.setText(NodeConfigSettingItem.HW_NIC.displayName());

		// HW-ディスク情報
		this.m_checkTargetConfigHwDisk = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigHwDisk.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwDisk.setText(NodeConfigSettingItem.HW_DISK.displayName());

		// HW-ファイルシステム情報
		this.m_checkTargetConfigHwFilesystem = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigHwFilesystem.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwFilesystem.setText(NodeConfigSettingItem.HW_FILESYSTEM.displayName());

		// HW-ホスト名情報
		this.m_checkTargetConfigHwHostname = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigHwHostname.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwHostname.setText(NodeConfigSettingItem.HOSTNAME.displayName());

		// HW-ネットワーク接続
		this.m_checkTargetConfigNetstat = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigNetstat.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigNetstat.setText(NodeConfigSettingItem.NETSTAT.displayName());

		// プロセス情報
		this.m_checkTargetConfigProcess = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigProcess.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigProcess.setText(NodeConfigSettingItem.PROCESS.displayName());

		// パッケージ情報
		this.m_checkTargetConfigPackage = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigPackage.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigPackage.setText(NodeConfigSettingItem.PACKAGE.displayName());

		// ノード変数情報
		this.m_checkTargetConfigNodeVariable = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigNodeVariable.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigNodeVariable.setText(NodeConfigSettingItem.NODE_VARIABLE.displayName());

		// 個別導入製品情報
		this.m_checkTargetConfigProduct = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigProduct.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigProduct.setText(NodeConfigSettingItem.PRODUCT.displayName());

		// ライセンス情報
		this.m_checkTargetConfigLicense = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigLicense.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigLicense.setText(NodeConfigSettingItem.LICENSE.displayName());

		// ユーザ任意情報
		this.m_checkTargetConfigCustom = new Button(composite, SWT.CHECK);
		this.m_checkTargetConfigCustom.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigCustom.setText(NodeConfigSettingItem.CUSTOM.displayName());
	}

	/**
	 * 入力値チェックをして、ファイルに出力します。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult validateResult = null;
		if ( !this.output() ) {
			validateResult = new ValidateResult();
			validateResult.setValid(false);
			validateResult.setID(Messages.getString("message.hinemos.1"));
			validateResult.setMessage(Messages.getString("message.monitor.44"));
		}
		return validateResult;
	}

	/* (non-Javadoc)
	 * @see com.clustercontrol.dialog.CommonDialog#okPressed()
	 */
	@Override
	protected void okPressed() {

		if (!m_checkTargetConfigOs.getSelection()
				&& !m_checkTargetConfigHwCpu.getSelection()
				&& !m_checkTargetConfigHwMemory.getSelection()
				&& !m_checkTargetConfigHwNic.getSelection()
				&& !m_checkTargetConfigHwDisk.getSelection()
				&& !m_checkTargetConfigHwFilesystem.getSelection()
				&& !m_checkTargetConfigHwHostname.getSelection()
				&& !m_checkTargetConfigNetstat.getSelection()
				&& !m_checkTargetConfigProcess.getSelection()
				&& !m_checkTargetConfigPackage.getSelection()
				&& !m_checkTargetConfigNodeVariable.getSelection()
				&& !m_checkTargetConfigProduct.getSelection()
				&& !m_checkTargetConfigLicense.getSelection()
				&& !m_checkTargetConfigCustom.getSelection()) {
			// 構成情報が選択されていない場合
			MessageDialog.openWarning(
					null,
					Messages.getString("message.hinemos.1"),
					Messages.getString("message.node.config.10"));
			return;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		// クライアントで出力するファイル名の日時情報はクライアントのタイムゾーンの現在時刻とする(マネージャのタイムゾーン時刻に補正しない)
		String now = sdf.format(new Date());

		// ファイルダイアログを開く
		FileDialog  fileDialog = new FileDialog(this.getShell(), SWT.SAVE);
		String extension = ".csv";
		m_fileName = NodeConfigSettingConstant.NODE_CONFIG_FILE_PREFIX + now + extension;

		fileDialog.setFileName(m_fileName);
		fileDialog.setFilterExtensions(new String[] { "*" + extension });

		m_filePath = fileDialog.open();
		if (m_filePath != null && !"".equals(m_filePath.trim())) {
			// Get specified new filename on RCP
			if(! ClusterControlPlugin.isRAP()){
				m_fileName = fileDialog.getFileName();
			}
			// Validate
			super.okPressed();
		}
	}

	/**
	 * 構成情報ファイルを出力します。
	 *
	 * @return true：正常、false：異常
	 */
	protected boolean output() {
		m_log.debug("output() : start");
		long start = HinemosTime.currentTimeMillis();

		boolean flag = false;

		// 対象日時
		String targetDatetime = "";
		if (m_nodeFilterInfo != null) {
			if (m_nodeFilterInfo.getNodeConfigTargetDatetime() != null) {
				targetDatetime = m_nodeFilterInfo.getNodeConfigTargetDatetime();
			}
		}

		// 検索条件
		String conditionStr = NodemapUtil.createConditionString(m_nodeFilterInfo);

		// ダウンロード対象
		List<String> itemList = new ArrayList<>();
		// OS情報
		if (m_checkTargetConfigOs.getSelection()) {
			itemList.add(NodeConfigSettingItem.OS.name());
		}
		// CPU情報
		if (m_checkTargetConfigHwCpu.getSelection()) {
			itemList.add(NodeConfigSettingItem.HW_CPU.name());
		}
		// メモリ情報
		if (m_checkTargetConfigHwMemory.getSelection()) {
			itemList.add(NodeConfigSettingItem.HW_MEMORY.name());
		}
		// NIC情報
		if (m_checkTargetConfigHwNic.getSelection()) {
			itemList.add(NodeConfigSettingItem.HW_NIC.name());
		}
		// ディスク情報
		if (m_checkTargetConfigHwDisk.getSelection()) {
			itemList.add(NodeConfigSettingItem.HW_DISK.name());
		}
		// ファイルシステム情報
		if (m_checkTargetConfigHwFilesystem.getSelection()) {
			itemList.add(NodeConfigSettingItem.HW_FILESYSTEM.name());
		}
		// ホスト名情報
		if (m_checkTargetConfigHwHostname.getSelection()) {
			itemList.add(NodeConfigSettingItem.HOSTNAME.name());
		}
		// ネットワーク接続
		if (m_checkTargetConfigNetstat.getSelection()) {
			itemList.add(NodeConfigSettingItem.NETSTAT.name());
		}
		// プロセス
		if (m_checkTargetConfigProcess.getSelection()) {
			itemList.add(NodeConfigSettingItem.PROCESS.name());
		}
		// パッケージ
		if (m_checkTargetConfigPackage.getSelection()) {
			itemList.add(NodeConfigSettingItem.PACKAGE.name());
		}
		// ノード変数情報
		if (m_checkTargetConfigNodeVariable.getSelection()) {
			itemList.add(NodeConfigSettingItem.NODE_VARIABLE.name());
		}
		// 個別導入製品情報
		if (m_checkTargetConfigProduct.getSelection()) {
			itemList.add(NodeConfigSettingItem.PRODUCT.name());
		}
		// ライセンス情報
		if (m_checkTargetConfigLicense.getSelection()) {
			itemList.add(NodeConfigSettingItem.LICENSE.name());
		}
		// ユーザ任意情報
		if (m_checkTargetConfigCustom.getSelection()) {
			itemList.add(NodeConfigSettingItem.CUSTOM.name());
		}

		boolean isOutputHeader = false;

		File file = new File(m_filePath);
		FileOutputStream fOut = null;
		try {
			if (!file.createNewFile()) {
				m_log.warn("file is already exist.");
			}

			fOut = new FileOutputStream(file);

			String[] mapKeys = m_facilityIdMap.keySet().toArray(new String[]{});
			Arrays.sort(mapKeys);

			StringBuilder sbErrMsg = new StringBuilder();
			for (int i = 0; i < mapKeys.length; i++){
				String managerName = mapKeys[i];
				List<String> facilityIdList = m_facilityIdMap.get(managerName);
				Collections.sort(facilityIdList);
				try {
					NodeMapRestClientWrapper wrapper = NodeMapRestClientWrapper.getWrapper(managerName);
					CommonRestClientWrapper commonWrapper = CommonRestClientWrapper.getWrapper(managerName);

					// 構成情報一覧取得
					String language = Locale.getDefault().getLanguage();

					// 一度に取得する情報のノード数を取得
					String value = commonWrapper.getDownloadNodeConfigCount().getValue();
					int downloadNodeCount = Integer.parseInt(value);

					// データ出力
					DownloadNodeConfigFileRequest dtoReq = new DownloadNodeConfigFileRequest();
					dtoReq.setConditionStr(conditionStr);
					dtoReq.setTargetDatetime(targetDatetime);
					dtoReq.setLanguage(language);
					dtoReq.setManagerName(managerName);
					dtoReq.setItemList(itemList);
					dtoReq.setItemList(itemList);
					for (int j = 0; j < facilityIdList.size(); j = j + downloadNodeCount) {
						if ((j + downloadNodeCount) > facilityIdList.size()) {
							dtoReq.setFacilityIdList(facilityIdList.subList(j, facilityIdList.size()));
						} else {
							dtoReq.setFacilityIdList(facilityIdList.subList(j, j + downloadNodeCount));
						}
						if (!isOutputHeader) {
							// ヘッダー出力
							dtoReq.setNeedHeaderInfo(true);
							isOutputHeader = true;
						} else {
							dtoReq.setNeedHeaderInfo(false);
						}
						File downloadFile = wrapper.downloadNodeConfigFile(dtoReq);
						try (FileInputStream fIn = new FileInputStream(downloadFile)) {
							writeTo(fIn, fOut);
						}
					}

				} catch (Exception e) {
					if (sbErrMsg.length() != 0) {
						sbErrMsg.append("\n");
					}
					if (e instanceof RestConnectFailed) {
						m_log.debug("reload(), " + e.getMessage());
						sbErrMsg.append(Messages.getString("message.hinemos.failure.transfer") 
								+ ", " + e.getMessage());
					} else if (e instanceof InvalidSetting) {
						m_log.warn("reload(), " + e.getMessage(), e);
						sbErrMsg.append(HinemosMessage.replace(e.getMessage()));
					} else {
						m_log.warn("reload(), " + e.getMessage(), e);
						sbErrMsg.append(Messages.getString("message.hinemos.failure.unexpected") 
								+ ", " + HinemosMessage.replace(e.getMessage()));
					}
				}
			}
			// エラーが発生した場合は、まとめてメッセージを表示する。
			if (sbErrMsg.length() != 0) {
				MessageDialog.openInformation(null, Messages.getString("message"), sbErrMsg.toString());
			}

			if (m_log.isDebugEnabled()) {
				m_log.debug("output() " + (HinemosTime.currentTimeMillis() - start) + "ms.");
			}

			// Start download file
			if( ClusterControlPlugin.isRAP() ){
				FileDownloader.openBrowser(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					m_filePath,
					m_fileName
				);
			}
			flag = true;

		} catch (IOException e) {
			m_log.warn("output() downloadNodeConfigFile, " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected")
							+ ", " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("output() downloadNodeConfigFile, " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected")
							+ ", " + HinemosMessage.replace(e.getMessage()));
		} finally {
			try {
				if (fOut != null) {
					fOut.close();
				}
				if( ClusterControlPlugin.isRAP() ){
					// Clean up temporary file
					FileDownloader.cleanup(m_filePath);
				}
			} catch (IOException e) {
				m_log.warn("output() downloadNodeConfigFile, " + e.getMessage(), e);
			}
		}
		return flag;
	}

	/**
	 * 出力先パスを返します。
	 *
	 * @return 出力先パス
	 */
	public String getFilePath() {
		return this.m_filePath;
	}

	/**
	 * 出力先ファイル名を返します。
	 *
	 * @return 出力先ファイル名
	 */
	public String getFileName() {
		return this.m_fileName;
	}

	/**
	 * ＯＫボタンのテキストを返します。
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("download");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}
	
	private void writeTo(FileInputStream in, FileOutputStream out) throws IOException {
		try {
			byte[] buffer = new byte[1024];

			int length;
			while ((length = in.read(buffer)) > 0){
				out.write(buffer, 0, length);
			}
		} catch(IOException e) {
			m_log.warn("failed write nodeconfig download file.");
			throw e;
		}
	}
}
