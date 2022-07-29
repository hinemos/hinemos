/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;
import org.openapitools.client.model.MonitorNumericValueInfoResponse.MonitorNumericTypeEnum;

import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 数値監視の判定情報（重要度）コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>値取得の成功時（重要度毎に配置）</dd>
 *  <dd>　判定項目１ テキストボックス</dd>
 *  <dd>　判定項目１ 判定基準文字列ラベル（例：以上）</dd>
 *  <dd>　判定項目２ テキストボックス</dd>
 *  <dd>　判定項目２ 判定基準文字列ラベル（例：以下）</dd>
 *  <dd>値取得の失敗時</dd>
 *  <dd>　「重要度」 コンボボックス</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class NumericValueInfoComposite extends Composite {

	/** 入力値の検証タイプ（整数）。 */
	public static final int INPUT_VERIFICATION_INTEGER_NUMBER = 0;
	/** 入力値の検証タイプ（実数）。 */
	public static final int INPUT_VERIFICATION_REAL_NUMBER = 1;
	/** 入力値の検証タイプ（正の整数）。 */
	public static final int INPUT_VERIFICATION_POSITIVE_INTEGER = 2;
	/** 入力値の検証タイプ（正の実数）。 */
	public static final int INPUT_VERIFICATION_POSITIVE_REAL = 3;

	/** 項目名（判定項目１）。 */
	protected String m_itemName1 = null;

	/** 項目名（判定項目２）。 */
	protected String m_itemName2 = null;

	/** ラベル（判定項目１） **/
	private Label m_textItemName1 = null;

	/** ラベル（判定項目２） **/
	private Label m_textItemName2 = null;

	/** 判定基準文字列（判定項目１）。 */
	private String m_criterion1 = Messages.getString("greater");

	/** 判定基準文字列（判定項目２）。 */
	private String m_criterion2 = Messages.getString("less");

	/** 入力値の検証タイプ（判定項目１）。 */
	protected int m_inputVerifyType1 = INPUT_VERIFICATION_INTEGER_NUMBER;

	/** 入力値の検証タイプ（判定項目２）。 */
	protected int m_inputVerifyType2 = INPUT_VERIFICATION_INTEGER_NUMBER;

	/** 判定値（判定項目１：通知） テキストボックス。 */
	protected Text m_textValue1Info = null;

	/** 判定値（判定項目２：通知） テキストボックス。 */
	protected Text m_textValue2Info = null;

	/** 判定値（判定項目１：警告） テキストボックス。 */
	protected Text m_textValue1Warn = null;

	/** 判定値（判定項目２：警告） テキストボックス。 */
	protected Text m_textValue2Warn = null;

	/** 数値監視モード  */
	private MonitorNumericTypeEnum m_monitorNumericType = MonitorNumericTypeEnum.BASIC;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 * 判定基準文字列，入力値の検証タイプ 及び 入力値の初期値はデフォルト値が使用されます。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param rangeFlg 範囲検証フラグ
	 * @param item1 項目名（判定項目１）
	 * @param item2 項目名（判定項目２）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NumericValueInfoComposite(Composite parent, int style, boolean rangeFlg, String item1, String item2) {
		super(parent, style);

		m_itemName1 = item1;
		m_itemName2 = item2;

		this.initialize();
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。<BR>
	 * 引数で指定された入力値の検証タイプで、全ての入力値テキストボックスの入力検証を行います。
	 * 判定基準文字列 及び 入力値の初期値はデフォルト値が使用されます。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param rangeFlg 範囲検証フラグ
	 * @param item1 項目名（判定項目１）
	 * @param item2 項目名（判定項目２）
	 * @param inputVerifyType 入力値の検証タイプ（判定項目１,２）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see #INPUT_VERIFICATION_INTEGER_NUMBER
	 * @see #INPUT_VERIFICATION_POSITIVE_INTEGER
	 * @see #INPUT_VERIFICATION_POSITIVE_REAL
	 * @see #INPUT_VERIFICATION_REAL_NUMBER
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NumericValueInfoComposite(Composite parent,
			int style,
			boolean rangeFlg,
			String item1,
			String item2,
			int inputVerifyType) {

		super(parent, style);

		m_itemName1 = item1;
		m_itemName2 = item2;
		m_inputVerifyType1 = inputVerifyType;
		m_inputVerifyType2 = inputVerifyType;

		this.initialize();
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。<BR>
	 * 各判定項目ごとに引数で指定された入力検証タイプで、各入力値テキストボックスの入力検証を行います。
	 * 判定基準文字列 及び 入力値の初期値はデフォルト値が使用されます。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param rangeFlg 範囲検証フラグ
	 * @param item1 項目名（判定項目１）
	 * @param item2 項目名（判定項目２）
	 * @param inputVerifyType1 入力値の検証タイプ（判定項目１）
	 * @param inputVerifyType2 入力値の検証タイプ（判定項目２）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see #INPUT_VERIFICATION_INTEGER_NUMBER
	 * @see #INPUT_VERIFICATION_POSITIVE_INTEGER
	 * @see #INPUT_VERIFICATION_POSITIVE_REAL
	 * @see #INPUT_VERIFICATION_REAL_NUMBER
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NumericValueInfoComposite(Composite parent,
			int style,
			boolean rangeFlg,
			String item1,
			String item2,
			int inputVerifyType1,
			int inputVerifyType2) {

		super(parent, style);

		m_itemName1 = item1;
		m_itemName2 = item2;
		m_inputVerifyType1 = inputVerifyType1;
		m_inputVerifyType2 = inputVerifyType2;

		this.initialize();
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。<BR>
	 * 引数で指定された入力値の検証タイプで、全ての入力値テキストボックスの入力検証を行います。
	 * 判定基準文字列 及び 入力値の初期値はデフォルト値が使用されます。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param rangeFlg 範囲検証フラグ
	 * @param item1 項目名（判定項目１）
	 * @param item2 項目名（判定項目２）
	 * @param criterion1 判定基準文字列（判定項目１）
	 * @param criterion2 判定基準文字列（判定項目２）
	 * @param inputVerifyType 入力値の検証タイプ（判定項目１,２）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see #INPUT_VERIFICATION_INTEGER_NUMBER
	 * @see #INPUT_VERIFICATION_POSITIVE_INTEGER
	 * @see #INPUT_VERIFICATION_POSITIVE_REAL
	 * @see #INPUT_VERIFICATION_REAL_NUMBER
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NumericValueInfoComposite(Composite parent,
			int style,
			boolean rangeFlg,
			String item1,
			String item2,
			String criterion1,
			String criterion2,
			int inputVerifyType) {

		super(parent, style);

		m_itemName1 = item1;
		m_itemName2 = item2;
		m_criterion1 = criterion1;
		m_criterion2 = criterion2;
		m_inputVerifyType1 = inputVerifyType;
		m_inputVerifyType2 = inputVerifyType;

		this.initialize();
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。<BR>
	 * 引数で指定された入力値の検証タイプで、全ての入力値テキストボックスの入力検証を行います。
	 * 判定基準文字列 及び 入力値の初期値はデフォルト値が使用されます。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param rangeFlg 範囲検証フラグ
	 * @param item1 項目名（判定項目１）
	 * @param item2 項目名（判定項目２）
	 * @param criterion1 判定基準文字列（判定項目１）
	 * @param criterion2 判定基準文字列（判定項目２）
	 * @param inputVerifyType 入力値の検証タイプ（判定項目１,２）
	 * @param monitorNumericType 数値監視種別
	 *
	 * @see org.eclipse.swt.SWT
	 * @see #INPUT_VERIFICATION_INTEGER_NUMBER
	 * @see #INPUT_VERIFICATION_POSITIVE_INTEGER
	 * @see #INPUT_VERIFICATION_POSITIVE_REAL
	 * @see #INPUT_VERIFICATION_REAL_NUMBER
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NumericValueInfoComposite(Composite parent,
			int style,
			boolean rangeFlg,
			String item1,
			String item2,
			String criterion1,
			String criterion2,
			int inputVerifyType,
			MonitorNumericTypeEnum monitorNumericType) {

		super(parent, style);

		m_itemName1 = item1;
		m_itemName2 = item2;
		m_criterion1 = criterion1;
		m_criterion2 = criterion2;
		m_inputVerifyType1 = inputVerifyType;
		m_inputVerifyType2 = inputVerifyType;
		m_monitorNumericType = monitorNumericType;
		
		this.initialize();
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。<BR>
	 * 各判定項目ごとに引数で指定された入力検証タイプで、各入力値テキストボックスの入力検証を行います。
	 * 入力値の初期値は、デフォルト値が使用されます。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param rangeFlg 範囲検証フラグ
	 * @param item1 項目名（判定項目１）
	 * @param item2 項目名（判定項目２）
	 * @param criterion1 判定基準文字列（判定項目１）
	 * @param criterion2 判定基準文字列（判定項目２）
	 * @param inputVerifyType1 入力値の検証タイプ（判定項目１）
	 * @param inputVerifyType2 入力値の検証タイプ（判定項目２）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see #INPUT_VERIFICATION_INTEGER_NUMBER
	 * @see #INPUT_VERIFICATION_POSITIVE_INTEGER
	 * @see #INPUT_VERIFICATION_POSITIVE_REAL
	 * @see #INPUT_VERIFICATION_REAL_NUMBER
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NumericValueInfoComposite(Composite parent,
			int style,
			boolean rangeFlg,
			String item1,
			String item2,
			String criterion1,
			String criterion2,
			int inputVerifyType1,
			int inputVerifyType2) {

		super(parent, style);

		m_itemName1 = item1;
		m_itemName2 = item2;
		m_criterion1 = criterion1;
		m_criterion2 = criterion2;
		m_inputVerifyType1 = inputVerifyType1;
		m_inputVerifyType2 = inputVerifyType2;

		this.initialize();
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。<BR>
	 * 引数で指定された入力値の検証タイプで、全ての入力値テキストボックスの入力検証を行います。
	 * 判定基準文字列は、デフォルト値が使用されます。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param rangeFlg 範囲検証フラグ
	 * @param item1 項目名（判定項目１）
	 * @param item2 項目名（判定項目２）
	 * @param inputVerifyType 入力値の検証タイプ（判定項目１,２）
	 * @param inputValue1 入力値の初期値（判定項目１）
	 * @param inputValue2 入力値の初期値（判定項目２）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see #INPUT_VERIFICATION_INTEGER_NUMBER
	 * @see #INPUT_VERIFICATION_POSITIVE_INTEGER
	 * @see #INPUT_VERIFICATION_POSITIVE_REAL
	 * @see #INPUT_VERIFICATION_REAL_NUMBER
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NumericValueInfoComposite(Composite parent,
			int style,
			boolean rangeFlg,
			String item1,
			String item2,
			int inputVerifyType,
			Double inputValue1,
			Double inputValue2) {

		super(parent, style);

		m_itemName1 = item1;
		m_itemName2 = item2;
		m_inputVerifyType1 = inputVerifyType;
		m_inputVerifyType2 = inputVerifyType;

		this.initialize();
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。<BR>
	 * 各判定項目ごとに引数で指定された入力検証タイプで、各入力値テキストボックスの入力検証を行います。
	 * 判定基準文字列は、デフォルト値が使用されます。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param rangeFlg 範囲検証フラグ
	 * @param item1 項目名（判定項目１）
	 * @param item2 項目名（判定項目２）
	 * @param inputVerifyType1 入力値の検証タイプ（判定項目１）
	 * @param inputVerifyType2 入力値の検証タイプ（判定項目２）
	 * @param inputValue1Info 入力値の初期値（判定項目１：通知）
	 * @param inputValue2Info 入力値の初期値（判定項目２：通知）
	 * @param inputValue1Warn 入力値の初期値（判定項目１：警告）
	 * @param inputValue2Warn 入力値の初期値（判定項目２：警告）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see #INPUT_VERIFICATION_INTEGER_NUMBER
	 * @see #INPUT_VERIFICATION_POSITIVE_INTEGER
	 * @see #INPUT_VERIFICATION_POSITIVE_REAL
	 * @see #INPUT_VERIFICATION_REAL_NUMBER
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NumericValueInfoComposite(Composite parent,
			int style,
			boolean rangeFlg,
			String item1,
			String item2,
			int inputVerifyType1,
			int inputVerifyType2,
			Double inputValue1Info,
			Double inputValue2Info,
			Double inputValue1Warn,
			Double inputValue2Warn) {

		super(parent, style);

		m_itemName1 = item1;
		m_itemName2 = item2;
		m_inputVerifyType1 = inputVerifyType1;
		m_inputVerifyType2 = inputVerifyType2;

		this.initialize();
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。<BR>
	 * 各判定項目ごとに引数で指定された入力検証タイプで、各入力値テキストボックスの入力検証を行います。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param rangeFlg 範囲検証フラグ
	 * @param item1 項目名（判定項目１）
	 * @param item2 項目名（判定項目２）
	 * @param criterion1 判定基準文字列（判定項目１）
	 * @param criterion2 判定基準文字列（判定項目２）
	 * @param inputVerifyType1 入力値の検証タイプ（判定項目１）
	 * @param inputVerifyType2 入力値の検証タイプ（判定項目２）
	 * @param inputValue1Info 入力値の初期値（判定項目１：通知）
	 * @param inputValue2Info 入力値の初期値（判定項目２：通知）
	 * @param inputValue1Warn 入力値の初期値（判定項目１：警告）
	 * @param inputValue2Warn 入力値の初期値（判定項目２：警告）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see #INPUT_VERIFICATION_INTEGER_NUMBER
	 * @see #INPUT_VERIFICATION_POSITIVE_INTEGER
	 * @see #INPUT_VERIFICATION_POSITIVE_REAL
	 * @see #INPUT_VERIFICATION_REAL_NUMBER
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NumericValueInfoComposite(Composite parent,
			int style,
			boolean rangeFlg,
			String item1,
			String item2,
			String criterion1,
			String criterion2,
			int inputVerifyType1,
			int inputVerifyType2,
			Double inputValue1Info,
			Double inputValue2Info,
			Double inputValue1Warn,
			Double inputValue2Warn) {

		super(parent, style);

		m_itemName1 = item1;
		m_itemName2 = item2;
		m_criterion1 = criterion1;
		m_criterion2 = criterion2;
		m_inputVerifyType1 = inputVerifyType1;
		m_inputVerifyType2 = inputVerifyType2;

		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 閾値の下限
		m_textItemName1 = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "itemname1", m_textItemName1);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_textItemName1.setLayoutData(gridData);
		m_textItemName1.setText(m_itemName1);

		// 閾値の上限
		m_textItemName2 = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "", m_textItemName2);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_textItemName2.setLayoutData(gridData);
		m_textItemName2.setText(m_itemName2);

		// 重要度：通知
		label = this.getLabelPriority(this, Messages.getString("info"),PriorityColorConstant.COLOR_INFO);

		// 閾値の下限
		this.m_textValue1Info = new Text(this, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "textvalue1", m_textValue1Info);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textValue1Info.setLayoutData(gridData);
		this.m_textValue1Info.setText("0");
		this.m_textValue1Info.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 以上
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "morethan", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(m_criterion1);

		// 閾値の上限
		this.m_textValue2Info = new Text(this, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "textvalue2", m_textValue2Info);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textValue2Info.setLayoutData(gridData);
		this.m_textValue2Info.setText("0");
		this.m_textValue2Info.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 以下
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "lessthan", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(m_criterion2);

		// 重要度：警告
		label = this.getLabelPriority(this, Messages.getString("warning"),PriorityColorConstant.COLOR_WARNING);

		// 閾値の下限
		this.m_textValue1Warn = new Text(this, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "valu1warn", m_textValue1Warn);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textValue1Warn.setLayoutData(gridData);
		this.m_textValue1Warn.setText("0");
		this.m_textValue1Warn.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 以上
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "morethan2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(m_criterion1);

		// 閾値の上限
		this.m_textValue2Warn = new Text(this, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "valu2warn", m_textValue2Warn);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textValue2Warn.setLayoutData(gridData);
		this.m_textValue2Warn.setText("0");
		this.m_textValue2Warn.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 以下
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "lessthan2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(m_criterion2);

		// 重要度：異常
		label = this.getLabelPriority(this, Messages.getString("critical"),PriorityColorConstant.COLOR_CRITICAL);

		// （通知・警告以外）
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "otherpriority", label);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("other.priority"));
	}

	/**
	 * 更新処理
	 *
	 */
	@Override
	public void update(){
		// 必須項目が未入力であることを明示
		if(this.m_textValue1Info.getEnabled() && "".equals(this.m_textValue1Info.getText())){
			this.m_textValue1Info.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textValue1Info.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_textValue2Info.getEnabled() && "".equals(this.m_textValue2Info.getText())){
			this.m_textValue2Info.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textValue2Info.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_textValue1Warn.getEnabled() && "".equals(this.m_textValue1Warn.getText())){
			this.m_textValue1Warn.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textValue1Warn.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_textValue2Warn.getEnabled() && "".equals(this.m_textValue2Warn.getText())){
			this.m_textValue2Warn.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textValue2Warn.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる監視情報
	 */
	public void setInputData(MonitorInfoResponse info) {

		if(info != null){

			List<MonitorNumericValueInfoResponse> list = info.getNumericValueInfo();
			if(list != null){
				for(int index=0; index<list.size(); index++){
					MonitorNumericValueInfoResponse numericValueInfo = list.get(index);
					if (numericValueInfo == null || !numericValueInfo.getMonitorNumericType().equals(m_monitorNumericType)) {
						continue;
					}
					if(MonitorNumericValueInfoResponse.PriorityEnum.INFO ==  numericValueInfo.getPriority()){
						String lower = "";
						String upper = "";
						if(m_inputVerifyType1  == INPUT_VERIFICATION_INTEGER_NUMBER ||
								m_inputVerifyType1  == INPUT_VERIFICATION_POSITIVE_INTEGER){
							lower = Long.toString(numericValueInfo.getThresholdLowerLimit().longValue());
						}
						else{
							lower = Double.toString(numericValueInfo.getThresholdLowerLimit());
						}
						if(m_inputVerifyType2  == INPUT_VERIFICATION_INTEGER_NUMBER ||
								m_inputVerifyType2  == INPUT_VERIFICATION_POSITIVE_INTEGER){
							upper = Long.toString(numericValueInfo.getThresholdUpperLimit().longValue());
						}
						else{
							upper = Double.toString(numericValueInfo.getThresholdUpperLimit());
						}
						this.m_textValue1Info.setText(lower);
						this.m_textValue2Info.setText(upper);
					} else if(MonitorNumericValueInfoResponse.PriorityEnum.WARNING ==  numericValueInfo.getPriority()){
						String lower = "";
						String upper = "";
						if(m_inputVerifyType1  == INPUT_VERIFICATION_INTEGER_NUMBER ||
								m_inputVerifyType1  == INPUT_VERIFICATION_POSITIVE_INTEGER){
							lower = Long.toString(numericValueInfo.getThresholdLowerLimit().longValue());
						}
						else{
							lower = Double.toString(numericValueInfo.getThresholdLowerLimit());
						}
						if(m_inputVerifyType2  == INPUT_VERIFICATION_INTEGER_NUMBER ||
								m_inputVerifyType2  == INPUT_VERIFICATION_POSITIVE_INTEGER){
							upper = Long.toString(numericValueInfo.getThresholdUpperLimit().longValue());
						}
						else{
							upper = Double.toString(numericValueInfo.getThresholdUpperLimit());
						}
						this.m_textValue1Warn.setText(lower);
						this.m_textValue2Warn.setText(upper);
					}
				}
			}

			// 必須項目が未入力であることを明示
			this.update();
		}
	}

	/**
	 * 引数で指定された監視情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。
	 * 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param info 入力値を設定する監視情報
	 * @return 検証結果
	 *
	 * @see #setValidateResult(String, String)
	 */
	public ValidateResult createInputData(MonitorInfoResponse info) {

		List<MonitorNumericValueInfoResponse> valueList = new ArrayList<>();

		String lowerText = null;
		String upperText = null;
		Double lower = null;
		Double upper = null;

		// 重要度：情報
		MonitorNumericValueInfoResponse valueInfo = getDefaultValueInfo(info, MonitorNumericValueInfoResponse.PriorityEnum.INFO);

		lowerText = this.m_textValue1Info.getText();
		upperText = this.m_textValue2Info.getText();

		if (lowerText != null && !"".equals(lowerText.trim())) {
			try{
				lower = Double.valueOf(lowerText);
				valueInfo.setThresholdLowerLimit(lower);
			}
			catch(NumberFormatException e){
				String[] args = { m_itemName1 };
				return setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.3", args));
			}
		}
		if (upperText != null && !"".equals(upperText.trim())) {
			try{
				upper = Double.valueOf(upperText);
				valueInfo.setThresholdUpperLimit(upper);
			}
			catch(NumberFormatException e){
				String[] args = { m_itemName2 };
				return setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.4", args));
			}
		}
		valueList.add(valueInfo);

		// 重要度：警告
		MonitorNumericValueInfoResponse valueWarn = getDefaultValueInfo(info, MonitorNumericValueInfoResponse.PriorityEnum.WARNING);

		lowerText = this.m_textValue1Warn.getText();
		upperText = this.m_textValue2Warn.getText();

		if (lowerText != null
				&& !"".equals(lowerText.trim())) {
			try{
				lower = Double.valueOf(lowerText);
				valueWarn.setThresholdLowerLimit(lower);
			}
			catch(NumberFormatException e){
				String[] args = { m_itemName1 };
				return setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.5", args));
			}
		}
		if (this.m_textValue2Warn.getText() != null
				&& !"".equals((this.m_textValue2Warn.getText()).trim())) {
			try{
				upper = Double.valueOf(upperText);
				valueWarn.setThresholdUpperLimit(upper);
			}
			catch(NumberFormatException e){
				String[] args = { m_itemName2 };
				return setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.6", args));
			}
		}
		valueList.add(valueWarn);

		// 重要度：危険
		MonitorNumericValueInfoResponse valueCritical = getDefaultValueInfo(info, MonitorNumericValueInfoResponse.PriorityEnum.CRITICAL);
		valueCritical.setThresholdLowerLimit(Double.valueOf(0));
		valueCritical.setThresholdUpperLimit(Double.valueOf(0));
		valueList.add(valueCritical);

		// 重要度：不明
		MonitorNumericValueInfoResponse valueUnknown = getDefaultValueInfo(info, MonitorNumericValueInfoResponse.PriorityEnum.UNKNOWN);
		valueUnknown.setThresholdLowerLimit(Double.valueOf(0));
		valueUnknown.setThresholdUpperLimit(Double.valueOf(0));
		valueList.add(valueUnknown);

		List<MonitorNumericValueInfoResponse> valueInfoList = info.getNumericValueInfo();
		valueInfoList.addAll(valueList);

		return null;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_textValue1Info.setEnabled(enabled);
		this.m_textValue2Info.setEnabled(enabled);
		this.m_textValue1Warn.setEnabled(enabled);
		this.m_textValue2Warn.setEnabled(enabled);

		this.update();
	}

	/**
	 * 重要度のラベルを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param text ラベルに表示するテキスト
	 * @param background ラベルの背景色
	 * @return 生成されたラベル
	 */
	private Label getLabelPriority(Composite parent,
			String text,
			Color background
			) {

		// ラベル（重要度）
		Label label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority", label);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(text + " : ");
		label.setBackground(background);

		return label;
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 * @return 認証結果
	 */
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	/**
	 * 初期値を設定した数値監視の判定情報を返します。<BR>
	 * 監視対象ID，監視項目ID 及び 重要度をセットした数値監視の判定情報を返します。
	 *
	 * @param info 監視情報
	 * @param priority 重要度
	 * @return 数値監視の判定情報
	 */
	private MonitorNumericValueInfoResponse getDefaultValueInfo(MonitorInfoResponse info, MonitorNumericValueInfoResponse.PriorityEnum priority) {

		MonitorNumericValueInfoResponse value = new MonitorNumericValueInfoResponse();
		value.setMonitorNumericType(m_monitorNumericType);
		value.setPriority(priority);

		return value;
	}

	/**
	 * 判定項目１のラベルを設定します。
	 *
	 * @param item1
	 */
	public void setTextItem1(String item1){
		m_textItemName1.setText(item1);
	}

	/**
	 * 判定項目２のラベルを設定します。
	 *
	 * @param item2
	 */
	public void setTextItem2(String item2){
		m_textItemName2.setText(item2);
	}

	public void setInfoWarnText(String info1, String info2, String warn1, String warn2) {
		this.m_textValue1Info.setText(info1);
		this.m_textValue2Info.setText(info2);
		this.m_textValue1Warn.setText(warn1);
		this.m_textValue2Warn.setText(warn2);

	}

}
