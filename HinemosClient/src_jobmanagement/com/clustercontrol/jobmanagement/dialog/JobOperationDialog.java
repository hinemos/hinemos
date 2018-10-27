/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.bean.Property;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.bean.JobOperationConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.PropertySheet;

/**
 * ジョブ[開始]及びジョブ[停止]のダイアログクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobOperationDialog extends CommonDialog {
	/** プロパティ */
	private Property m_property = null;
	/** プロパティシート */
	private PropertySheet m_propertySheet = null;
	/** ダイアログのタイトル */
	private String m_title = "";

	/**
	 * コンストラクタ
	 *
	 * @param parent 親シェル
	 */
	public JobOperationDialog(Shell parent) {
		super(parent);
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 *
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		shell.setText(m_title);

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		// プロパティシート
		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, tree);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		tree.setLayoutData(gridData);
		m_propertySheet = new PropertySheet(tree);
		m_propertySheet.setInput(m_property);
		m_propertySheet.expandAll();

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * ジョブ操作用プロパティを返します。
	 *
	 * @return ジョブ操作用プロパティ
	 */
	public Property getProperty() {
		return m_property;
	}

	/**
	 * ジョブ操作用プロパティを設定します。
	 *
	 * @param property ジョブ操作用プロパティ
	 */
	public void setProperty(Property property) {
		m_property = property;
	}

	/**
	 * ダイアログタイトルを返します。
	 *
	 * @return ダイアログタイトル
	 */
	public String getTitleText() {
		return m_title;
	}

	/**
	 * ダイアログタイトルを設定します。
	 *
	 * @param title ダイアログタイトル
	 */
	public void setTitleText(String title) {
		m_title = title;
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
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
		ValidateResult result = null;

		//制御取得
		ArrayList<?> values = PropertyUtil.getPropertyValue(m_property, JobOperationConstant.CONTROL);
		Integer control = null;
		if(values.get(0) instanceof String){
			String controlString = (String)values.get(0);
			control = Integer.valueOf(OperationMessage.stringToType(controlString));
		}

		if (control == null)
			throw new InternalError("control is null , controlString : " + values.get(0));
		
		//終了値取得
		values = PropertyUtil.getPropertyValue(m_property, JobOperationConstant.END_VALUE);
		Integer endValue = null;
		if(values.size() > 0 && values.get(0) instanceof Integer)
			endValue = (Integer)values.get(0);

		if(control == -1) {
			result = new ValidateResult();
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.115"));
			result.setValid(false);
			return result;
		}

		//制御が停止[状態変更]または停止[強制]の場合、終了値をチェック
		if(control == OperationConstant.TYPE_STOP_MAINTENANCE ||
				control == OperationConstant.TYPE_STOP_FORCE){
			if(endValue == null){
				result = new ValidateResult();
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.35"));
				result.setValid(false);
				return result;
			}
		}

		return result;
	}
}
