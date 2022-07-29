/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.bean.Property;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetRpaDirectParameterTableDefine;
import com.clustercontrol.jobmanagement.action.RpaDirectParameterProperty;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.PropertySheet;

/**
 * RPAシナリオ 直接実行 実行パラメータ設定ダイアログ用のコンポジットクラスです
 */
public class RpaDirectParameterDialog extends CommonDialog {
	/** プロパティシート */
	private PropertySheet m_viewer = null;
	/** 実行パラメータ */
	private List<Object> m_parameter = null;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親コンポジット
	 */
	public RpaDirectParameterDialog(Shell parent) {
		super(parent);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		// ダイアログタイトル
		parent.getShell().setText(Messages.getString("dialog.job.create.modify.rpa.job.parameter"));

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		Label tableTitle = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "tabletitle", tableTitle);
		tableTitle.setText(Messages.getString("attribute") + " : ");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		tableTitle.setLayoutData(gridData);

		// 実行パラメータ入力テーブル
		Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, tree);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		tree.setLayoutData(gridData);

		m_viewer = new PropertySheet(tree);

		Property property = new RpaDirectParameterProperty().getProperty();
		m_viewer.setInput(property);

		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 変更の場合に値を反映
		if (m_parameter != null) {
			List<Property> propertyList = PropertyUtil.getProperty(property,
					RpaDirectParameterProperty.ID_PARAMETER);
			Property parameter = propertyList.get(0);
			parameter.setValue(m_parameter.get(GetRpaDirectParameterTableDefine.PARAMETER));
			propertyList = PropertyUtil.getProperty(property,
					RpaDirectParameterProperty.ID_DESCRIPTION);
			Property description = propertyList.get(0);
			description.setValue(m_parameter.get(GetRpaDirectParameterTableDefine.DESCRIPTION));
			m_viewer.setInput(property);
			m_viewer.refresh();
		}
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

	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		m_parameter = new ArrayList<Object>();
		Property property = (Property) m_viewer.getInput();
		ArrayList<?> values = PropertyUtil.getPropertyValue(property,
				RpaDirectParameterProperty.ID_PARAMETER);
		String parameter = (String) values.get(0);
		if (parameter == null || parameter.length() == 0) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.rpa.5"));
			return result;
		}
		m_parameter.add(parameter);
		values = PropertyUtil.getPropertyValue(property, RpaDirectParameterProperty.ID_DESCRIPTION);
		String description = (String) values.get(0);
		m_parameter.add(description);

		return result;
	}

	/**
	 * 入力値を返します。
	 *
	 * @return 判定対象情報
	 */
	public List<Object> getInputData() {
		return m_parameter;
	}

	/**
	 * 入力値を設定します。
	 *
	 * @return 判定対象情報
	 */
	public void setInputData(List<Object> parameter) {
		m_parameter = parameter;
	}
}
