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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.bean.TruthConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorTruthValueInfo;

/**
 * 真偽値監視の判定情報（重要度）コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>値取得の成功時（真偽値毎に配置）</dd>
 *  <dd>　「重要度」 コンボボックス</dd>
 *  <dd>値取得の失敗時</dd>
 *  <dd>　「重要度」 コンボボックス</dd>
 * </dl>
 * @version 2.0.0
 * @since 2.0.0
 */
public class TruthValueInfoComposite extends Composite {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 4;

	/** カラム数（値）。 */
	public static final int WIDTH_VALUE = 2;

	/** 項目名（真）。 */
	private String m_itemTrue = null;

	/** 項目名（偽）。 */
	private String m_itemFalse = null;

	/** 値取得の成功時（真） コンボボックス。 */
	private Combo m_comboTrue = null;

	/** 値取得の成功時（偽） コンボボックス。 */
	private Combo m_comboFalse = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param rangeFlg 範囲判定フラグ
	 * @param itemTrue 項目名（真）
	 * @param itemFalse 項目名（偽）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	/**
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param range 範囲判定フラグ
	 * @param item1 判定項目名
	 * @param item2 判定項目名
	 */
	public TruthValueInfoComposite(Composite parent, int style, boolean rangeFlg, String itemTrue, String itemFalse) {
		super(parent, style);

		m_itemTrue = itemTrue;
		m_itemFalse = itemFalse;

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

		// ラベル
		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space1", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// 重要度
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "priority", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("priority"));

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 値がTrueの場合
		 */
		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space3", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(m_itemTrue + " : ");

		// 重要度
		this.m_comboTrue = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "true", m_comboTrue);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboTrue.setLayoutData(gridData);
		this.m_comboTrue.add(PriorityMessage.STRING_CRITICAL);
		this.m_comboTrue.add(PriorityMessage.STRING_WARNING);
		this.m_comboTrue.add(PriorityMessage.STRING_INFO);
		this.m_comboTrue.add(PriorityMessage.STRING_UNKNOWN);
		this.m_comboTrue.setText(PriorityMessage.STRING_INFO);

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space4", label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 値がFalseの場合
		 */
		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space5", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(m_itemFalse + " : ");

		// 重要度
		this.m_comboFalse = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "false", m_comboTrue);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboFalse.setLayoutData(gridData);
		this.m_comboFalse.add(PriorityMessage.STRING_CRITICAL);
		this.m_comboFalse.add(PriorityMessage.STRING_WARNING);
		this.m_comboFalse.add(PriorityMessage.STRING_INFO);
		this.m_comboFalse.add(PriorityMessage.STRING_UNKNOWN);
		this.m_comboFalse.setText(PriorityMessage.STRING_CRITICAL);

		// 空白
		label = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "space6", label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる監視情報
	 */
	public void setInputData(MonitorInfo info) {

		if(info != null){

			List<MonitorTruthValueInfo> list = info.getTruthValueInfo();
			if(list != null){
				for(int index=0; index<list.size(); index++){
					MonitorTruthValueInfo truthValueInfo = list.get(index);
					if(truthValueInfo != null){
						if(truthValueInfo.getTruthValue() == TruthConstant.TYPE_TRUE){
							this.m_comboTrue.setText(PriorityMessage.typeToString(truthValueInfo.getPriority()));
						}
						else if(truthValueInfo.getTruthValue() == TruthConstant.TYPE_FALSE){
							this.m_comboFalse.setText(PriorityMessage.typeToString(truthValueInfo.getPriority()));
						}
					}
				}
			}
		}
	}

	/**
	 * 引数で指定された監視情報に、入力値を設定します。
	 *
	 * @param info 入力値を設定する監視情報
	 * @return 検証結果
	 */
	public ValidateResult createInputData(MonitorInfo info) {

		ArrayList<MonitorTruthValueInfo> valueList = new ArrayList<MonitorTruthValueInfo>();

		// 値がTrueの場合
		MonitorTruthValueInfo valueInfo = new MonitorTruthValueInfo();
		valueInfo.setMonitorId(info.getMonitorId());
		valueInfo.setTruthValue(TruthConstant.TYPE_TRUE);
		valueInfo.setPriority(PriorityMessage.stringToType(this.m_comboTrue.getText()));
		valueList.add(valueInfo);

		valueInfo = new MonitorTruthValueInfo();
		valueInfo.setMonitorId(info.getMonitorId());
		valueInfo.setTruthValue(TruthConstant.TYPE_FALSE);
		valueInfo.setPriority(PriorityMessage.stringToType(this.m_comboFalse.getText()));
		valueList.add(valueInfo);

		List<MonitorTruthValueInfo> monitorTruthValueInfoList = info.getTruthValueInfo();
		monitorTruthValueInfoList.clear();
		monitorTruthValueInfoList.addAll(valueList);

		return null;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_comboTrue.setEnabled(enabled);
		this.m_comboFalse.setEnabled(enabled);
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
}
