/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.clustercontrol.calendar.composite.CalendarIdListComposite;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.repository.bean.NodeConfigRunInterval;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.repository.NodeConfigSettingInfo;

/**
 * 構成管理情報収集条件コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「実行間隔」 コンボボックス</dd>
 *  <dd>「カレンダID」 コンボボックス</dd>
 * </dl>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigCollectRuleComposite extends Composite {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = CommonMonitorDialog.WIDTH_TITLE;

	/** カラム数（値）。*/
	public static final int WIDTH_VALUE = CommonMonitorDialog.SHORT_UNIT;

	/** 空白のカラム数。 */
	public static final int WIDTH_WHITE_SPACE = CommonMonitorDialog.MIN_UNIT;

	/** 実行間隔 コンボボックス。 */
	private Combo m_comboRunInterval = null;

	/** カレンダID コンポジット。 */
	private CalendarIdListComposite m_calendarId = null;


	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NodeConfigCollectRuleComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 *
	 * @see com.clustercontrol.calendar.composite.CalendarIdListComposite#CalendarIdListComposite(Composite, int, boolean)
	 */
	private void initialize() {

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 25;
		this.setLayout(layout);

		/*
		 * 実行間隔（分）
		 */
		// ラベル
		label = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("run.interval") + " : ");
		// コンボボックス
		this.m_comboRunInterval = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboRunInterval.setLayoutData(gridData);
		for(NodeConfigRunInterval it : NodeConfigRunInterval.values()){
			this.m_comboRunInterval.add(it.toString());
		}

		// 空白
		label = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * カレンダ
		 */
		this.m_calendarId = new CalendarIdListComposite(this, SWT.NONE, true);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_calendarId.setLayoutData(gridData);
	}

	/**
	 * 引数で指定された構成情報収集設定の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる構成情報収集設定
	 *
	 * @see com.clustercontrol.calendar.composite.CalendarIdListComposite#setText(String)
	 */
	public void setInputData(NodeConfigSettingInfo info) {

		// 構成情報収集間隔
		int runInterval = 0;
		if (info == null || info.getRunInterval() == 0) {
			runInterval = NodeConfigRunInterval.TYPE_HOUR_6.toSec(); 
		} else {
			runInterval = info.getRunInterval();
		}
		this.m_comboRunInterval.setText(NodeConfigRunInterval.valueOf(runInterval).toString());

		// カレンダー
		if (info == null || info.getCalendarId() == null) {
			this.m_calendarId.setText("");
		} else {
			this.m_calendarId.setText(info.getCalendarId());
		}
	}

	/**
	 * 引数で指定された構成情報収集設定に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。
	 * 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param info 入力値を設定する構成情報収集設定
	 * @return 検証結果
	 *
	 * @see #setValidateResult(String, String)
	 * @see com.clustercontrol.calendar.composite.CalendarIdListComposite#getText()
	 */
	public ValidateResult createInputData(NodeConfigSettingInfo info) {

		if(info != null){
			if (this.m_comboRunInterval.getText() != null
					&& !"".equals((this.m_comboRunInterval.getText()).trim())) {
				if("0".equals(this.m_comboRunInterval.getText())){
					info.setRunInterval(0);
				}else{
					info.setRunInterval(NodeConfigRunInterval.stringToType(this.m_comboRunInterval.getText()).toSec());
				}
			}

			if (this.m_calendarId.getText() != null
					&& !"".equals((this.m_calendarId.getText()).trim())) {
				info.setCalendarId(this.m_calendarId.getText());
			}
		}
		return null;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_comboRunInterval.setEnabled(enabled);
		this.m_calendarId.setEnabled(enabled);
	}

	/**
	 * 構成情報収集設定の間隔のメニュ選択可否の設定
	 * @param enabled
	 */
	public void setRunIntervalEnabled(boolean enabled) {
		this.m_comboRunInterval.setEnabled(enabled);
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

	public CalendarIdListComposite getCalendarId() {
		return m_calendarId;
	}
	
	public int getRunInterval() {
		return NodeConfigRunInterval.values()[m_comboRunInterval.getSelectionIndex()].toSec();
	}
}
