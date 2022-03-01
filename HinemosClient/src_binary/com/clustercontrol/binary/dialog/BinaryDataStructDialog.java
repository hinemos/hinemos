/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.BinaryCheckInfoResponse;
import org.openapitools.client.model.BinaryCheckInfoResponse.LengthTypeEnum;
import org.openapitools.client.model.BinaryCheckInfoResponse.TsTypeEnum;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.CommonVerifyListener;
import com.clustercontrol.util.Messages;

/**
 * データ構造詳細設定ダイアログクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinaryDataStructDialog extends CommonDialog {

	// Widgets
	private Text m_fileHeadBytes;
	private Button m_recordFixed;
	private Button m_recordVariable;
	private Text m_recordSize;
	private Text m_rsPosition;
	private Text m_rsByteLength;
	private Text m_recordHeaderSize;
	private Button m_haveTimeStamp;
	private Text m_tsPostion;
	private Combo m_tsType;
	private Button m_littleEndian;

	// その他フィールド
	/** デフォルト値保持用フィールド */
	private BinaryCheckInfoResponse m_inputDefault = null;
	/** 入力値保持用フィールド */
	private BinaryCheckInfoResponse m_inputResult = null;
	/** 入力値チェック結果 */
	private ValidateResult m_validateResult = null;

	// 後でpackするためsizeXはダミーの値。
	private static final int sizeX = 460;
	private static final int sizeY = 400;

	/** プリセット表示名(ダイアログタイトル用) */
	private final String presetName;

	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public BinaryDataStructDialog(Shell parent, String fileType, BinaryCheckInfoResponse inputDefault) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.presetName = fileType;
		this.m_inputDefault = inputDefault;
	}

	public BinaryDataStructDialog(Shell parent) {
		this(parent, null, null);
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(sizeX, sizeY);
	}

	/**
	 * ラベルWidgetを追加します。
	 */
	private Label addLabel(final Composite parent, String msgKey) {
		Label widget = new Label(parent, SWT.NONE);
		widget.setText(Messages.getString(msgKey) + " : ");
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = false;
		widget.setLayoutData(gd);

		return widget;
	}

	/**
	 * ラベルWidgetを追加します。
	 */
	private void initGridData(Control widget) {
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.horizontalSpan = 2;
		widget.setLayoutData(gd);
	}

	/**
	 * ラベルWidgetを追加します。(左寄せ)
	 */
	private void initGridData(Control widget, boolean beginning) {
		if(beginning){
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.BEGINNING;
			gd.horizontalSpan = 2;
			widget.setLayoutData(gd);
		} else {
			initGridData(widget);
		}
	}

	/**
	 * ラベルWidgetを追加します。
	 */
	private Text addTextField(final Composite parent, String text, String tooltip) {
		Text widget = new Text(parent, SWT.BORDER);
		if (null != text) {
			widget.setText(text);
		}
		if (null != tooltip) {
			widget.setToolTipText(tooltip);
		}
		initGridData(widget);
		return widget;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 *
	 * @see com.clustercontrol.notify.composite.NotifyListComposite
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		if (this.presetName != null && !this.presetName.isEmpty()) {
			shell.setText(this.presetName + " : " + Messages.getString("binary.data.struct.details"));
		} else {
			shell.setText(Messages.getString("binary.data.struct.details"));
		}

		// レイアウト
		GridLayout layout = new GridLayout(3, false);
		parent.setLayout(layout);

		// Common instances
		GridData gridData = null;
		String expressionStr = null;

		// ファイルヘッダサイズ
		addLabel(parent, "file.header.size");
		this.m_fileHeadBytes = addTextField(parent, null, Messages.getString("tooltip.input.byte"));
		this.m_fileHeadBytes.addVerifyListener(CommonVerifyListener.onlyNumelicListener);

		// レコード.
		addLabel(parent, "length.type");
		// 固定長.
		this.m_recordFixed = new Button(parent, SWT.RADIO);
		this.m_recordFixed.setText(Messages.getString(BinaryConstant.LENGTH_TYPE_FIXED));
		this.m_recordFixed.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
				setEnableds();
			}
		});
		// 可変長.
		this.m_recordVariable = new Button(parent, SWT.RADIO);
		this.m_recordVariable.setText(Messages.getString(BinaryConstant.LENGTH_TYPE_VARIABLE));
		this.m_recordVariable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
				setEnableds();
			}
		});

		// 必須項目入力時のイベント.
		ModifyListener updateModified = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		};

		// レコードサイズ.
		addLabel(parent, "record.size");
		this.m_recordSize = addTextField(parent, null, Messages.getString("tooltip.input.byte"));
		this.m_recordSize.addVerifyListener(CommonVerifyListener.onlyNumelicListener);
		this.m_recordSize.addModifyListener(updateModified);

		// レコードヘッダサイズ.
		addLabel(parent, "record.header.size");
		this.m_recordHeaderSize = addTextField(parent, null, Messages.getString("tooltip.input.byte"));
		this.m_recordHeaderSize.addVerifyListener(CommonVerifyListener.onlyNumelicListener);

		// サイズ位置.
		addLabel(parent, "record.size.position");
		this.m_rsPosition = addTextField(parent, null, Messages.getString("tooltip.input.allocated.position"));
		this.m_rsPosition.addVerifyListener(CommonVerifyListener.onlyNumelicListener);
		this.m_rsPosition.addModifyListener(updateModified);

		// サイズ表現バイト長.
		addLabel(parent, "record.size.byte.length");
		this.m_rsByteLength = addTextField(parent, null, Messages.getString("tooltip.input.allocated.byte"));
		this.m_rsByteLength.addVerifyListener(CommonVerifyListener.onlyNumelicListener);
		this.m_rsByteLength.addModifyListener(updateModified);

		// タイムスタンプ
		addLabel(parent, "have.timestamp");
		this.m_haveTimeStamp = new Button(parent, SWT.CHECK);
		this.initGridData(this.m_haveTimeStamp, true);
		this.m_haveTimeStamp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
				setEnableds();
			}
		});

		// タイムスタンプ位置.
		addLabel(parent, "timestamp.position");
		this.m_tsPostion = addTextField(parent, null, Messages.getString("tooltip.input.allocated.position"));
		this.m_tsPostion.addVerifyListener(CommonVerifyListener.onlyNumelicListener);
		this.m_tsPostion.addModifyListener(updateModified);

		// タイムスタンプ種類
		addLabel(parent, "timestamp.type");
		this.m_tsType = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.initGridData(this.m_tsType);
		this.m_tsType.addModifyListener(updateModified);

		// リトルエンディアン方式
		addLabel(parent, "little.endian");
		this.m_littleEndian = new Button(parent, SWT.CHECK);
		this.initGridData(this.m_littleEndian, true);

		// コンボボックス表示文字列と別に多言語対応文字列をデータとしてセット.
		expressionStr = Messages.getString(BinaryConstant.TS_TYPE_ONLY_SEC);
		this.m_tsType.setData(expressionStr, TsTypeEnum.ONLY_SEC);
		this.m_tsType.add(expressionStr);
		expressionStr = Messages.getString(BinaryConstant.TS_TYPE_SEC_AND_USEC);
		this.m_tsType.setData(expressionStr, TsTypeEnum.SEC_AND_USEC);
		this.m_tsType.add(expressionStr);
		this.m_tsType.setText(Messages.getString(BinaryConstant.TS_TYPE_ONLY_SEC));

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		line.setLayoutData(gridData);

		// ダイアログのサイズ調整（pack:resize to be its preferred size）
		shell.pack();
		shell.setSize(new Point(sizeX, sizeY));

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		this.init(this.m_inputDefault);

	}

	/**
	 * 活性制御
	 */
	private void setEnableds() {

		boolean littleEndianEnabled = false;

		// レコード長タイプによる活性制御.
		if (this.m_recordFixed.getSelection()) {
			// 固定長.
			Text[] enableds = { m_recordSize };
			this.setEnabledTexts(enableds, true);
			Text[] disableds = { m_recordHeaderSize, m_rsPosition, m_rsByteLength };
			this.setEnabledTexts(disableds, false);
		}
		if (this.m_recordVariable.getSelection()) {
			// 可変長.
			Text[] enableds = { m_recordHeaderSize, m_rsPosition, m_rsByteLength };
			this.setEnabledTexts(enableds, true);
			Text[] disableds = { m_recordSize };
			this.setEnabledTexts(disableds, false);
			littleEndianEnabled = true;
		}

		// タイムスタンプ有無による活性制御.
		if (this.m_haveTimeStamp.getSelection()) {
			// タイムスタンプあり.
			Text[] enableds = { m_tsPostion };
			this.setEnabledTexts(enableds, true);
			this.m_tsType.setEnabled(true);
			littleEndianEnabled = true;
		} else {
			// タイムスタンプなし.
			Text[] disableds = { m_tsPostion };
			this.setEnabledTexts(disableds, false);
			this.m_tsType.setEnabled(false);
			this.m_tsType.setText("");
		}

		// リトルエンディアンは可変長またはタイムスタンプありの場合に活性.
		this.m_littleEndian.setEnabled(littleEndianEnabled);
		if (!littleEndianEnabled) {
			this.m_littleEndian.setSelection(false);
		}

	}

	/**
	 * テキストボックスの活性/非活性制御.
	 */
	private void setEnabledTexts(Text[] texts, boolean enabled) {
		for (Text text : texts) {
			text.setEnabled(enabled);
			if (!enabled) {
				// 非活性の場合は値をクリア.
				text.setText("");
			}
		}
	}

	/**
	 * 画面更新処理.<br>
	 * <br>
	 * 必須項目が未入力の場合赤反転.
	 */
	public void update() {

		// レコード長タイプに応じた必須項目.
		if (this.m_recordFixed.getSelection()) {
			// 固定長.
			Text[] texts = { m_recordSize };
			Text[] notNeddTexts = { m_recordHeaderSize, m_rsPosition, m_rsByteLength };
			this.updateNumericTexts(texts, notNeddTexts);

		}
		if (this.m_recordVariable.getSelection()) {
			// 可変長.
			Text[] texts = { m_recordHeaderSize, m_rsPosition, m_rsByteLength };
			Text[] notNeddTexts = { m_recordSize };
			this.updateNumericTexts(texts, notNeddTexts);
		}

		// タイムスタンプ有無に応じた必須項目.
		if (this.m_haveTimeStamp.getSelection()) {
			// タイムスタンプあり.
			Text[] texts = { m_tsPostion };
			this.updateNumericTexts(texts, null);
			Combo[] combos = { m_tsType };
			this.updateCombos(combos, null);
		} else {
			// タイムスタンプなし
			Text[] notNeddTexts = { m_tsPostion };
			this.updateNumericTexts(null, notNeddTexts);
			Combo[] combos = { m_tsType };
			this.updateCombos(null, combos);
		}

	}

	/**
	 * 数値型のTextの必須チェックをして背景色をセット.<br>
	 * <br>
	 * 空文字もしくは0の場合に赤.
	 * 
	 * @param needs
	 *            必須項目
	 * @param notNeeds
	 *            必須ではない項目(赤反転を通常の背景色に戻す)
	 */
	private void updateNumericTexts(Text[] needs, Text[] notNeeds) {
		// 数値(空文字もしくは0の場合に赤).
		if (needs != null) {
			for (Text text : needs) {
				if ("".equals(text.getText()) || "0".equals(text.getText())) {
					text.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				} else {
					text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}
		}
		if (notNeeds != null) {
			for (Text text : notNeeds) {
				text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
	}

	/**
	 * コンボボックスの必須チェックをして背景色をセット.<br>
	 * <br>
	 * 未選択(空文字)の場合に赤.
	 *
	 * @param needs
	 *            必須項目
	 * @param notNeeds
	 *            必須ではない項目(赤反転を通常の背景色に戻す)
	 */
	private void updateCombos(Combo[] needs, Combo[] notNeeds) {
		// コンボボックス(未選択の場合に赤)

		if (needs != null) {
			for (Combo combo : needs) {
				if ("".equals(combo.getText())) {
					combo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				} else {
					combo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}
		}
		if (notNeeds != null) {
			for (Combo combo : notNeeds) {
				combo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}
	}

	/**
	 * ダイアログの初期値をセット.
	 * 
	 * @param info
	 *            ダイアログ初期値.
	 */
	private void init(BinaryCheckInfoResponse info) {

		// 引数にnull値が設定されていた場合は固定のデフォルト値をセット.
		if (info == null) {
			info = new BinaryCheckInfoResponse();
			info.setLengthType(LengthTypeEnum.FIXED);
			info.setHaveTs(false);
			info.setFileHeadSize(0L);
			info.setRecordSize(0);
			info.setRecordHeadSize(0);
			info.setSizePosition(0);
			info.setSizeLength(0);
			info.setTsPosition(0);
			info.setTsType(null);
			info.setLittleEndian(true);
		}

		String expressionStr = null;

		// ファイルヘッダサイズ.
		if (info.getFileHeadSize() < 0) {
			info.setFileHeadSize(0L);
		}
		this.m_fileHeadBytes.setText(Long.toString(info.getFileHeadSize()));

		// レコード長指定方法.
		if (LengthTypeEnum.VARIABLE.equals(info.getLengthType())) {
			this.m_recordVariable.setSelection(true);
			this.m_recordFixed.setSelection(false);
		} else {
			this.m_recordVariable.setSelection(false);
			this.m_recordFixed.setSelection(true);
		}

		// 固定長レコードサイズ.
		if (info.getRecordSize() < 0) {
			info.setRecordSize(0);
		}
		this.m_recordSize.setText(Integer.toString(info.getRecordSize()));

		// レコードヘッダサイズ.
		if (info.getRecordHeadSize() < 0) {
			info.setRecordHeadSize(0);
		}
		this.m_recordHeaderSize.setText(Integer.toString(info.getRecordHeadSize()));

		// 可変長レコードサイズ位置.
		if (info.getSizePosition() < 0) {
			info.setSizePosition(0);
		}
		this.m_rsPosition.setText(Integer.toString(info.getSizePosition()));

		// 可変長レコードサイズ表現バイト長.
		if (info.getSizeLength() < 0) {
			info.setSizeLength(0);
		}
		this.m_rsByteLength.setText(Integer.toString(info.getSizeLength()));

		// タイムスタンプ.
		if (info.getHaveTs()) {
			this.m_haveTimeStamp.setSelection(true);
		} else {
			this.m_haveTimeStamp.setSelection(false);
		}

		// タイムスタンプ位置.
		if (info.getTsPosition() < 0) {
			info.setTsPosition(0);
		}
		this.m_tsPostion.setText(Integer.toString(info.getTsPosition()));

		// タイムスタンプ種類.
		if (info.getTsType() == null) {
			info.setTsType(TsTypeEnum.SEC_AND_USEC);
		}
		switch(info.getTsType()){
		case SEC_AND_USEC:
			expressionStr = Messages.getString(BinaryConstant.TS_TYPE_SEC_AND_USEC);
			break;
		case ONLY_SEC:
			expressionStr = Messages.getString(BinaryConstant.TS_TYPE_ONLY_SEC);
			break;
		}
		this.m_tsType.setText(expressionStr);

		// リトルエンディアン方式.
		if (info.getLittleEndian()) {
			this.m_littleEndian.setSelection(true);
		} else {
			this.m_littleEndian.setSelection(false);
		}

		// 設定元に活性・非活性制御と赤反転処理.
		this.update();
		this.setEnableds();

	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		this.update();
		this.m_inputResult = this.getCheckedInputData();
		if (this.m_inputResult != null) {
			return super.validate();
		} else {
			return this.m_validateResult;
		}
	}

	/**
	 * チェック済の入力値をBinaryCheckInfoにセットして返却.<br>
	 * <br>
	 * 入力値チェックを行い、不正な場合は<code>null</code>を返します。
	 *
	 * @return 入力値、チェック結果不正はnull.
	 */
	private BinaryCheckInfoResponse getCheckedInputData() {

		BinaryCheckInfoResponse info = new BinaryCheckInfoResponse();
		String[] args = null;

		long checkLong = 0;
		int checkInteger = 0;

		// ファイルヘッダサイズ.
		if (this.m_fileHeadBytes.getText() != null && !this.m_fileHeadBytes.getText().isEmpty()) {
			try {
				checkLong = Long.parseLong(this.m_fileHeadBytes.getText());
			} catch (NumberFormatException e) {
				// long値の最大値を超えて入力された場合.
				args = new String[] { Messages.getString("file.header.size"), Long.toString(Long.MAX_VALUE) };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.11", args));
				return null;
			}
			info.setFileHeadSize(checkLong);
		} else {
			info.setFileHeadSize(0L);
		}

		// レコード長タイプ.
		if (this.m_recordVariable.getSelection()) {
			info.setLengthType(LengthTypeEnum.VARIABLE);
		} else {
			info.setLengthType(LengthTypeEnum.FIXED);
		}

		// 固定長レコードサイズ.
		if (this.m_recordFixed.getSelection()) {
			if (this.m_recordSize.getText() != null && !this.m_recordSize.getText().isEmpty()) {
				try {
					checkInteger = Integer.parseInt(this.m_recordSize.getText());
				} catch (NumberFormatException e) {
					// int値の最大値を超えて入力された場合.
					args = new String[] { Messages.getString("record.size"), Integer.toString(Integer.MAX_VALUE) };
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.11", args));
					return null;
				}
				if (checkInteger == 0) {
					// 入力必須.
					args = new String[] { Messages.getString("record.size") };
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.1", args));
					return null;
				}
				info.setRecordSize(checkInteger);
			} else {
				// 入力必須.
				args = new String[] { Messages.getString("record.size") };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.1", args));
				return null;
			}
		} else {
			info.setRecordSize(0);
		}

		// 可変長レコードヘッダサイズ.
		if (this.m_recordVariable.getSelection()) {
			if (this.m_recordHeaderSize.getText() != null && !this.m_recordHeaderSize.getText().isEmpty()) {
				try {
					checkInteger = Integer.parseInt(this.m_recordHeaderSize.getText());
				} catch (NumberFormatException e) {
					// int値の最大値を超えて入力された場合.
					args = new String[] { Messages.getString("record.header.size"), Integer.toString(Integer.MAX_VALUE) };
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.11", args));
					return null;
				}
				info.setRecordHeadSize(checkInteger);
			} else {
				// 入力必須.
				args = new String[] { Messages.getString("record.header.size") };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.1", args));
				return null;
			}
		} else {
			info.setRecordHeadSize(0);
		}

		// 可変長レコードサイズ位置.
		if (this.m_recordVariable.getSelection()) {
			if (this.m_rsPosition.getText() != null && !this.m_rsPosition.getText().isEmpty()) {
				try {
					checkInteger = Integer.parseInt(this.m_rsPosition.getText());
				} catch (NumberFormatException e) {
					// int値の最大値を超えて入力された場合.
					args = new String[] { Messages.getString("record.size.position"),
							Integer.toString(Integer.MAX_VALUE) };
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.11", args));
					return null;
				}
				if (checkInteger == 0) {
					// 入力必須.
					args = new String[] { Messages.getString("record.size.position") };
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.1", args));
					return null;
				}
				info.setSizePosition(checkInteger);
			} else {
				// 入力必須.
				args = new String[] { Messages.getString("record.size.position") };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.1", args));
				return null;
			}
		} else {
			info.setSizePosition(0);
		}

		// 可変長レコードサイズ表現バイト長.
		if (this.m_recordVariable.getSelection()) {
			if (this.m_rsByteLength.getText() != null && !this.m_rsByteLength.getText().isEmpty()) {
				try {
					checkInteger = Integer.parseInt(this.m_rsByteLength.getText());
				} catch (NumberFormatException e) {
					// int値の最大値を超えて入力された場合.
					args = new String[] { Messages.getString("record.size.byte.length"),
							Integer.toString(Integer.MAX_VALUE) };
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.11", args));
					return null;
				}
				if (checkInteger == 0) {
					// 入力必須.
					args = new String[] { Messages.getString("record.size.byte.length") };
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.1", args));
					return null;
				}
				info.setSizeLength(checkInteger);
			} else {
				// 入力必須.
				args = new String[] { Messages.getString("record.size.byte.length") };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.1", args));
				return null;
			}
		} else {
			info.setSizeLength(0);
		}

		// タイムスタンプ有無.
		info.setHaveTs(this.m_haveTimeStamp.getSelection());

		// タイムスタンプ位置.
		if (this.m_haveTimeStamp.getSelection()) {
			if (this.m_tsPostion.getText() != null && !this.m_tsPostion.getText().isEmpty()) {
				try {
					checkInteger = Integer.parseInt(this.m_tsPostion.getText());
				} catch (NumberFormatException e) {
					// int値の最大値を超えて入力された場合.
					args = new String[] { Messages.getString("timestamp.position"),
							Integer.toString(Integer.MAX_VALUE) };
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.11", args));
					return null;
				}
				if (checkInteger == 0) {
					// 入力必須.
					args = new String[] { Messages.getString("timestamp.position") };
					this.setValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.common.1", args));
					return null;
				}
				info.setTsPosition(checkInteger);
			} else {
				// 入力必須.
				args = new String[] { Messages.getString("timestamp.position") };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.1", args));
				return null;
			}
		} else {
			info.setTsPosition(0);
		}

		// タイムスタンプ種類.
		if (this.m_haveTimeStamp.getSelection()) {
			if (this.m_tsType.getText() != null && !this.m_tsType.getText().isEmpty()
					&& this.m_tsType.getData(this.m_tsType.getText()) != null
					&& this.m_tsType.getData(this.m_tsType.getText()) instanceof TsTypeEnum) {
				info.setTsType((TsTypeEnum) this.m_tsType.getData(this.m_tsType.getText()));
			} else {
				// 入力必須.
				args = new String[] { Messages.getString("timestamp.type") };
				this.setValidateResult(Messages.getString("message.hinemos.1"),
						Messages.getString("message.common.1", args));
				return null;
			}
		} else {
			info.setTsType(null);
		}

		// リトルエンディアン.
		info.setLittleEndian(this.m_littleEndian.getSelection());

		return info;
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id
	 *            ID
	 * @param message
	 *            メッセージ
	 */
	protected void setValidateResult(String id, String message) {

		this.m_validateResult = new ValidateResult();
		this.m_validateResult.setValid(false);
		this.m_validateResult.setID(id);
		this.m_validateResult.setMessage(message);
	}

	/**
	 * 入力値をBinaryCheckInfoにセットして返却.
	 *
	 * @return 入力値.
	 */
	public BinaryCheckInfoResponse getInputResult() {
		return this.m_inputResult;
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

	/**
	 * キャンセルボタンのテキストを返します。
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

}
