/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.dialog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddBinaryfileMonitorRequest;
import org.openapitools.client.model.BinaryCheckInfoResponse;
import org.openapitools.client.model.BinaryCheckInfoResponse.CollectTypeEnum;
import org.openapitools.client.model.BinaryCheckInfoResponse.CutTypeEnum;
import org.openapitools.client.model.BinaryPatternInfoRequest;
import org.openapitools.client.model.ModifyBinaryfileMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * バイナリファイルの監視設定ダイアログクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class MonitorBinaryDialog extends CommonMonitorBinaryDialog {

	// ログ
	private static Log m_log = LogFactory.getLog(MonitorBinaryDialog.class);

	// ----- instance フィールド ----- //
	// 入力項目.
	/** 収集方式_ファイル全体 */
	protected Button m_collectType_single = null;
	/** 収集方式_増分のみ */
	protected Button m_collectType_continuous = null;
	/** データ構造 */
	protected Combo m_dataArchitecture = null;
	/** 詳細設定(データ構造) */
	protected Button m_details = null;
	/** ディレクトリ */
	protected Text m_directory = null;
	/** ファイル名 */
	protected Text m_fileName = null;

	// 隠しパラメータ.
	/** 詳細設定ダイアログの入力値. */
	protected BinaryCheckInfoResponse m_inputDetails = null;
	/** 詳細設定ダイアログOKボタン押下時に選択していたデータ構造. */
	protected String m_selectedDataArch = null;

	// ----- コンストラクタ ----- //
	/**
	 * 作成用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public MonitorBinaryDialog(Shell parent) {
		super(parent, null);
		super.logLineFlag = true;
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param managerName
	 *            マネージャ名
	 * @param monitorId
	 *            変更する監視項目ID
	 * @param updateFlg
	 *            更新するか否か（true:変更、false:新規登録）
	 */
	public MonitorBinaryDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

		logLineFlag = true;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		super.customizeDialog(parent);

		// タイトル
		shell.setText(Messages.getString("dialog.binary.file.create.modify"));

		// バイナリファイル監視独自の画面項目の設定.
		this.setInputFields();

		// ダイアログを調整
		super.adjustDialog();

		// 静的な非活性項目の設定.
		super.confirmCollectValid.removeSelectionListener(super.collectSelectedListner);

		// 初期表示値の設定.
		MonitorInfoResponse info = null;
		if (this.monitorId == null) {
			// 作成の場合
			info = new MonitorInfoResponse();
			super.setInfoInitialValue(info);
			super.m_monitorRule.setInputData(info);
			this.setInputData(info);
		} else {
			// 変更の場合、情報取得
			try {
				MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(super.managerName);
				info = wrapper.getMonitor(monitorId);
				this.setInputData(info);
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("customizeDialog() getMonitor, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", "
								+ HinemosMessage.replace(e.getMessage()));
			}
		}
	}

	/**
	 * 画面項目の設定.
	 */
	private void setInputFields() {
		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// 条件グループの子グループとしてまとめる
		Group groupFileInfo = new Group(super.groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "fileInfo", groupFileInfo);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = BASIC_MARGIN;
		layout.marginHeight = BASIC_MARGIN;
		layout.numColumns = BASIC_UNIT;
		groupFileInfo.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupFileInfo.setLayoutData(gridData);
		groupFileInfo.setText(Messages.getString("file.info"));

		// --収集方式.
		// ラベル
		label = new Label(groupFileInfo, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("binary.collect.type") + " : ");

		// ラジオボタン
		Composite fileTypeComposite = new Composite(groupFileInfo, SWT.NULL);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileTypeComposite.setLayoutData(gridData);
		fileTypeComposite.setLayout(new RowLayout());

		// 収集方式選択時のイベント.
		SelectionAdapter collectSelection = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled();
			}
		};

		this.m_collectType_single = new Button(fileTypeComposite, SWT.RADIO);
		this.m_collectType_single.setText(Messages.getString(BinaryConstant.COLLECT_TYPE_WHOLE_FILE));
		this.m_collectType_single.addSelectionListener(collectSelection);
		this.m_collectType_continuous = new Button(fileTypeComposite, SWT.RADIO);
		this.m_collectType_continuous.setText(Messages.getString(BinaryConstant.COLLECT_TYPE_ONLY_INCREMENTS));
		this.m_collectType_continuous.addSelectionListener(collectSelection);

		// --データ構造
		// ラベル
		label = new Label(groupFileInfo, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("binary.data.struct") + " : ");

		Composite presetComposite = new Composite(groupFileInfo, SWT.NULL);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		presetComposite.setLayoutData(gridData);
		presetComposite.setLayout(new RowLayout());

		// コンボボックス
		this.m_dataArchitecture = new Combo(presetComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.setDataArchtecture();
		this.m_dataArchitecture.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BinaryConstant.DataArchType dataArchType = getDataArchType();
				setEnabledByDataArch(dataArchType);
				inputFromPreset(dataArchType);
			}
		});

		// ボタン
		this.m_details = new Button(presetComposite, SWT.PUSH);
		this.m_details.setText(Messages.getString("binary.data.struct.details"));
		this.m_details.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedDetails();
				update();
			}
		});

		// --ディレクトリ
		// ラベル
		label = new Label(groupFileInfo, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("directory") + " : ");
		// テキスト
		this.m_directory = new Text(groupFileInfo, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_directory.setLayoutData(gridData);
		this.m_directory.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		this.m_directory.setToolTipText(Messages.getString("tooltip.input.directory"));

		// --ファイル名
		// ラベル
		label = new Label(groupFileInfo, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("file.name") + "(" + Messages.getString("regex") + ") : ");
		// テキスト
		this.m_fileName = new Text(groupFileInfo, SWT.BORDER | SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = LONG_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_fileName.setLayoutData(gridData);
		this.m_fileName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		this.m_fileName.setToolTipText(Messages.getString("dialog.logfile.pattern"));
	}

	/**
	 * データ構造プルダウンリストの中身をプリセットから取得してセット.
	 * 
	 * <pre>
	 * プルダウンのdataには下記をセット
	 * 
	 *   汎用(空欄):String(DB上tagTypeに設定される値)
	 *   時間区切り:String(DB上cutTypeに設定される値)
	 *   プリセット:BinaryCheckInfo(プリセットの設定値)
	 * </pre>
	 */
	private void setDataArchtecture() {

		String expressionStr = null;

		// 汎用(プリセットなし).
		expressionStr = Messages.getString(BinaryConstant.TAG_TYPE_UNIVERSAL);
		this.m_dataArchitecture.setData(expressionStr, BinaryConstant.TAG_TYPE_UNIVERSAL);
		this.m_dataArchitecture.add(expressionStr);
		this.m_dataArchitecture.setText(expressionStr); // デフォルト選択値.

		// 時間区切り.
		expressionStr = Messages.getString(BinaryConstant.CUT_TYPE_INTERVAL);
		this.m_dataArchitecture.setData(expressionStr, BinaryConstant.CUT_TYPE_INTERVAL);
		this.m_dataArchitecture.add(expressionStr);

		// プリセットをマネージャーから取得.
		List<BinaryCheckInfoResponse> presetList = null;
		String errMessage = null;
		String[] args = null;
		try {
			String managerName = super.getManagerName();
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(managerName);
			presetList = wrapper.getBinaryPresetList();
		} catch (Exception e) {
			args = new String[] { Messages.getString("binary.data.struct"), Messages.getString("preset") };
			errMessage = HinemosMessage.replace(e.getMessage());
			if (!errMessage.isEmpty()) {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}
			MessageDialog.openError(null, Messages.getString("error"),
					Messages.getString("message.binary.6", args) + errMessage);
		}
		// プリセット取得できなかったら終了.
		if (presetList == null || presetList.isEmpty()) {
			return;
		}

		// 取得したプリセットをプルダウンにセット.
		for (BinaryCheckInfoResponse preset : presetList) {
			if ("".equals(preset.getTagType())) {
				// プルダウンリスト表示名の取得に失敗したファイルが存在する場合はエラーメッセージを表示する(プルダウン表示不可).
				String presetFile = Messages.getString("preset") + String.format("[%s]", preset.getFileName());
				args = new String[] { Messages.getString("binary.data.struct"), presetFile };
				errMessage = HinemosMessage.replace(preset.getErrMsg());
				if (!errMessage.isEmpty()) {
					errMessage = "\n" + HinemosMessage.replace(preset.getErrMsg());
				}
				MessageDialog.openError(null, Messages.getString("error"),
						Messages.getString("message.binary.4", args) + errMessage);
			} else {
				if (preset.getErrMsg() != null && !preset.getErrMsg().isEmpty()) {
					// 設定の取得に失敗したプロパティが存在する場合は情報メッセージを表示する(プルダウン表示可・詳細設定ダイアログで修正).
					String dialog = Messages.getString("binary.data.struct.details") + Messages.getString("dialog");
					args = new String[] { Messages.getString("binary.data.struct"), Messages.getString("preset"),
							dialog };
					errMessage = HinemosMessage.replace(preset.getErrMsg());
					if (!errMessage.isEmpty()) {
						errMessage = ", " + HinemosMessage.replace(preset.getErrMsg());
					}
					MessageDialog.openError(null, Messages.getString("info"),
							Messages.getString("message.binary.8", args) + errMessage);
				}
				expressionStr = preset.getTagType();
				// ダイアログ引継ぎ用のオブジェクトをセット.
				this.m_dataArchitecture.setData(expressionStr, preset);
				this.m_dataArchitecture.add(expressionStr);
			}

		}

	}

	/**
	 * 全体の活性制御.
	 */
	private void setEnabled() {
		this.setDataArchEnabled();
		BinaryConstant.DataArchType dataArchType = this.getDataArchType();
		this.setEnabledByDataArch(dataArchType);
	}

	/**
	 * データ構造プルダウンリストの活性制御.
	 */
	private void setDataArchEnabled() {

		// 収集方式_ファイル全体が選択されている場合は非活性.
		if (this.m_collectType_single.getSelection()) {
			// 非活性にしてプリセットなし選択.
			this.m_dataArchitecture.setEnabled(false);
			this.m_dataArchitecture.setText(Messages.getString(BinaryConstant.TAG_TYPE_UNIVERSAL));
		} else {
			this.m_dataArchitecture.setEnabled(true);
		}

	}

	/**
	 * データ構造選択値の種別を取得.
	 */
	private BinaryConstant.DataArchType getDataArchType() {
		// ファイル全体の場合.
		if (this.m_collectType_single.getSelection()) {
			return BinaryConstant.DataArchType.NONE;
		}

		// プルダウンにセットしたデータを取得.
		Object obj = this.m_dataArchitecture.getData(this.m_dataArchitecture.getText());
		if (obj == null) {
			// 取得できなかった場合.
			return BinaryConstant.DataArchType.ERROR;
		}

		// プルダウンにセットしたデータが文字列の場合はデフォルトで存在する選択肢.
		if (obj instanceof String) {
			String dataArch = (String) obj;
			if (BinaryConstant.CUT_TYPE_INTERVAL.equals(dataArch)) {
				// 時間区切り.
				return BinaryConstant.DataArchType.INTERVAL;
			}

			if (BinaryConstant.TAG_TYPE_UNIVERSAL.equals(dataArch)) {
				// 空欄(詳細設定ダイアログで全入力)
				return BinaryConstant.DataArchType.CUSTOMIZE;
			}
		}

		// プルダウンにセットしたデータがプリセットファイルから取得した設定値の場合.
		if (obj instanceof BinaryCheckInfoResponse) {
			return BinaryConstant.DataArchType.PRESET;
		}

		// プルダウンのデータが正常に取得できなかった場合.
		return BinaryConstant.DataArchType.ERROR;
	}

	/**
	 * データ構造選択値による活性制御.
	 */
	private void setEnabledByDataArch(BinaryConstant.DataArchType dataArchType) {
		this.setRunIntervalEnabled(dataArchType);
		this.setDetailEnabled(dataArchType);
	}

	/**
	 * 監視間隔の活性・非活性制御.
	 */
	private void setRunIntervalEnabled(BinaryConstant.DataArchType dataArchType) {
		if (dataArchType != BinaryConstant.DataArchType.INTERVAL) {
			super.m_monitorRule.setRunIntervalEnabled(false);
			return;
		}
		// データ構造：時間区切りが選択されている場合は活性.
		super.m_monitorRule.setRunIntervalEnabled(true);
	}

	/**
	 * 詳細設定ボタンの活性制御.
	 */
	private void setDetailEnabled(BinaryConstant.DataArchType dataArchType) {

		// プリセットファイルからの入力もしくは詳細設定ダイアログでの入力の場合は活性.
		if (dataArchType == BinaryConstant.DataArchType.PRESET
				|| dataArchType == BinaryConstant.DataArchType.CUSTOMIZE) {
			this.m_details.setEnabled(true);
			return;
		}

		// 上記以外は非活性.
		this.m_details.setEnabled(false);
		this.m_inputDetails = null;
		this.m_selectedDataArch = null;
		return;
	}

	/**
	 * プリセットファイルの設定値を入力値として設定.
	 */
	private void inputFromPreset(BinaryConstant.DataArchType dataArchType) {
		if (dataArchType != BinaryConstant.DataArchType.PRESET) {
			return;
		}
		Object obj = this.m_dataArchitecture.getData(this.m_dataArchitecture.getText());
		if (obj == null || !(obj instanceof BinaryCheckInfoResponse)) {
			return;
		}

		if (m_inputDetails != null && m_selectedDataArch != null
				&& m_selectedDataArch.equals(m_dataArchitecture.getText())) {
			// 前回入力(登録)時と同じデータ構造の場合は入力(登録)値を優先する.
			return;
		}

		BinaryCheckInfoResponse presetInfo = (BinaryCheckInfoResponse) obj;
		this.m_inputDetails = presetInfo;
		this.m_selectedDataArch = this.m_dataArchitecture.getText();
	}

	/**
	 * 詳細設定ボタン押下時イベント.
	 */
	private void selectedDetails() {

		// ダイアログデフォルト値をセット.
		BinaryCheckInfoResponse detailInfo = null;
		if (m_inputDetails != null && m_selectedDataArch != null
				&& m_selectedDataArch.equals(m_dataArchitecture.getText())) {
			// 前回入力時と同じデータ構造を選択している場合は前回の入力値をセット.
			detailInfo = m_inputDetails;
		} else {
			// データ構造があらためて選択されている場合はプリセットファイルの設定値を設定.
			Object obj = m_dataArchitecture.getData(m_dataArchitecture.getText());
			if (obj != null && obj instanceof BinaryCheckInfoResponse) {
				detailInfo = (BinaryCheckInfoResponse) obj;
			} else {
				// プリセット取得不可の場合はダイアログデフォルト値.
				detailInfo = null;
			}
		}
		BinaryDataStructDialog detailsDialog = new BinaryDataStructDialog(shell, m_dataArchitecture.getText(),
				detailInfo);

		// ダイアログ表示及び終了処理.
		if (IDialogConstants.OK_ID == detailsDialog.open()) {
			// OKの場合は入力値を取得.
			m_inputDetails = detailsDialog.getInputResult();
			m_selectedDataArch = m_dataArchitecture.getText();
		}
	}

	/**
	 * 更新処理.<br>
	 * <br>
	 * 入力状態を元に必須項目を明示.
	 *
	 */
	@Override
	public void update() {
		super.update();

		// 文字列(空文字の場合に赤).
		Text[] texts = { m_directory, m_fileName };
		for (Text text : texts) {
			if ("".equals(text.getText())) {
				text.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
	}

	/**
	 * 各項目に入力値を設定します。
	 *
	 * @param monitor
	 *            設定値として用いる監視情報
	 */
	@Override
	protected void setInputData(MonitorInfoResponse monitor) {

		// 間隔が0の場合は、デフォルトの5分で表示する(変更のみ)
		if (monitor.getBinaryCheckInfo() != null) {
			if (!CollectTypeEnum.ONLY_INCREMENTS.equals(monitor.getBinaryCheckInfo().getCollectType()) ||
					CutTypeEnum.LENGTH.equals(monitor.getBinaryCheckInfo().getCutType())) {
				monitor.setRunInterval(MonitorInfoResponse.RunIntervalEnum.MIN_05);
			}
		}
		
		// とりあえずフィールドにセット
		super.setInputData(monitor);
		super.inputData = monitor;

		// 通知設定の取得.
		List<NotifyRelationInfoResponse> c = monitor.getNotifyRelationList();
		if (c != null) {
			for (NotifyRelationInfoResponse i : c) {
				m_log.debug("notifyId : " + i.getNotifyId());
			}
		}

		// バイナリ監視情報の設定.
		BinaryCheckInfoResponse binaryfileInfo = monitor.getBinaryCheckInfo();
		if (binaryfileInfo == null) {
			binaryfileInfo = new BinaryCheckInfoResponse();
			this.m_inputDetails = null;
		} else {
			// 登録値をデフォルト値としてセット.
			this.m_inputDetails = monitor.getBinaryCheckInfo();
		}

		// 収集方式.
		if (binaryfileInfo.getCollectType() == null
				|| CollectTypeEnum.WHOLE_FILE.equals(binaryfileInfo.getCollectType())) {
			// デフォルトもしくは引継ぎで"ファイル全体"選択.
			this.m_collectType_single.setSelection(true);
		} else {
			this.m_collectType_continuous.setSelection(true);
		}

		String expressionStr = null;

		// レコード分割方法・タグタイプ→データ構造
		if (binaryfileInfo.getCutType() != null) {
			if (CutTypeEnum.INTERVAL.equals(binaryfileInfo.getCutType())) {
				// 時間区切り.
				expressionStr = Messages.getString(BinaryConstant.CUT_TYPE_INTERVAL);
				this.m_dataArchitecture.setText(expressionStr);
			} else {
				// レコード長指定.
				if (binaryfileInfo.getTagType() != null && !binaryfileInfo.getTagType().isEmpty()) {
					// 登録されているプリセット名(汎用含む).
					this.m_dataArchitecture.setText(binaryfileInfo.getTagType());
				} else {
					// デフォルト汎用(プリセットなし).
					expressionStr = Messages.getString(BinaryConstant.TAG_TYPE_UNIVERSAL);
					this.m_dataArchitecture.setText(expressionStr);
				}
			}
		} else {
			// デフォルト汎用(プリセットなし).
			expressionStr = Messages.getString(BinaryConstant.TAG_TYPE_UNIVERSAL);
			this.m_dataArchitecture.setText(expressionStr);
		}

		// 登録値が存在する場合はセットデータを前回の選択値として保持.
		if (this.m_inputDetails != null) {
			this.m_selectedDataArch = this.m_dataArchitecture.getText();
		}

		// ディレクトリ.
		if (binaryfileInfo.getDirectory() != null) {
			this.m_directory.setText(binaryfileInfo.getDirectory());
		}

		// ファイル名.
		if (binaryfileInfo.getFileName() != null) {
			this.m_fileName.setText(binaryfileInfo.getFileName());
		}

		// 必須項目の赤反転処理.
		this.update();
		this.setEnabled();

		super.m_binaryPatternInfo.setInputData(monitor);

	}

	/**
	 * 入力値を用いて通知情報を生成します。
	 *
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfoResponse createInputData() {
		super.createInputData();
		if (super.validateResult != null) {
			return null;
		}

		// バイナリファイル監視（バイナリ）固有情報をManager送信用infoに設定.
		BinaryCheckInfoResponse binaryInfo = new BinaryCheckInfoResponse();

		// 収集方式.
		if (this.m_collectType_single.getSelection()) {
			binaryInfo.setCollectType(CollectTypeEnum.WHOLE_FILE);
		}
		if (this.m_collectType_continuous.getSelection()) {
			binaryInfo.setCollectType(CollectTypeEnum.ONLY_INCREMENTS);
		}

		// データ構造 → レコード分割方法・プリセット名(タグ種別).
		if (this.m_dataArchitecture.getText() != null) {
			BinaryConstant.DataArchType dataArchType = this.getDataArchType();

			switch (dataArchType) {

			case INTERVAL:
				binaryInfo.setCutType(CutTypeEnum.INTERVAL);
				break;

			case CUSTOMIZE:
				binaryInfo.setCutType(CutTypeEnum.LENGTH);
				binaryInfo.setTagType(BinaryConstant.TAG_TYPE_UNIVERSAL);
				break;

			case PRESET:
				binaryInfo.setCutType(CutTypeEnum.LENGTH);
				if (!this.m_dataArchitecture.getText().isEmpty()) {
					binaryInfo.setTagType(this.m_dataArchitecture.getText());
				}
				break;

			case NONE:
			case ERROR:
			default:
				break;

			}
		}

		// 詳細設定ダイアログ入力値をマッピング.
		if (this.m_inputDetails != null) {
			binaryInfo.setFileHeadSize(this.m_inputDetails.getFileHeadSize());
			binaryInfo.setLengthType(this.m_inputDetails.getLengthType());
			binaryInfo.setRecordSize(this.m_inputDetails.getRecordSize());
			binaryInfo.setRecordHeadSize(this.m_inputDetails.getRecordHeadSize());
			binaryInfo.setSizePosition(this.m_inputDetails.getSizePosition());
			binaryInfo.setSizeLength(this.m_inputDetails.getSizeLength());
			binaryInfo.setHaveTs(this.m_inputDetails.getHaveTs());
			binaryInfo.setTsPosition(this.m_inputDetails.getTsPosition());
			binaryInfo.setTsType(this.m_inputDetails.getTsType());
			binaryInfo.setLittleEndian(this.m_inputDetails.getLittleEndian());
		}

		// ディレクトリ.
		if (this.m_directory.getText() != null && !"".equals(this.m_directory.getText())) {
			binaryInfo.setDirectory(this.m_directory.getText());
		}

		// ファイル名.
		if (this.m_fileName.getText() != null && !"".equals(this.m_fileName.getText())) {
			binaryInfo.setFileName(this.m_fileName.getText());
		}

		// 設定した項目を監視情報としてセット.
		super.monitorInfo.setBinaryCheckInfo(binaryInfo);

		// 結果判定の定義
		super.validateResult = super.m_binaryPatternInfo.createInputData(super.monitorInfo);
		if (validateResult != null) {
			return null;
		}

		// 通知関連情報とアプリケーションの設定
		super.validateResult = super.m_notifyInfo.createInputData(super.monitorInfo);
		if (super.validateResult != null) {
			if (super.validateResult.getID() == null) { // 通知ID警告用出力
				if (!displayQuestion(super.validateResult)) {
					super.validateResult = null;
					return null;
				}
			} else { // アプリケーション未入力チェック
				return null;
			}
		}

		return super.monitorInfo;
	}

	/**
	 * 入力値をマネージャに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		if (super.inputData != null) {
			String[] args = { this.inputData.getMonitorId(), getManagerName() };
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(getManagerName());
			if (!this.updateFlg) {
				// 作成の場合
				try {
					AddBinaryfileMonitorRequest info = new AddBinaryfileMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddBinaryfileMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getBinaryPatternInfo() != null) {
						for (int i = 0; i < this.inputData.getBinaryPatternInfo().size(); i++) {
							info.getBinaryPatternInfo().get(i).setPriority(
									BinaryPatternInfoRequest.PriorityEnum.fromValue(
											this.inputData.getBinaryPatternInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.addBinaryfileMonitor(info);
					MessageDialog.openInformation(null, Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
					result = true;
				} catch (MonitorDuplicate e) {
					// 監視項目IDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.monitor.53", args));
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.monitor.34", args) + errMessage);
				}
			} else {
				// 変更の場合
				try {
					ModifyBinaryfileMonitorRequest info = new ModifyBinaryfileMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyBinaryfileMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getBinaryPatternInfo() != null) {
						for (int i = 0; i < this.inputData.getBinaryPatternInfo().size(); i++) {
							info.getBinaryPatternInfo().get(i).setPriority(
									BinaryPatternInfoRequest.PriorityEnum.fromValue(
											this.inputData.getBinaryPatternInfo().get(i).getPriority().getValue()));
						}
					}
					wrapper.modifyBinaryfileMonitor(this.inputData.getMonitorId(), info);
					MessageDialog.openInformation(null, Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
					result = true;
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
						// アクセス権なしの場合、エラーダイアログを表示する
						MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.monitor.36", args) + errMessage);
				}
			}
		}

		return result;
	}
}
