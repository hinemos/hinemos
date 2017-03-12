/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;
import java.util.regex.Pattern;

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
import com.clustercontrol.jobmanagement.action.EnvVariableProperty;
import com.clustercontrol.jobmanagement.action.GetEnvVariableTableDefine;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.PropertySheet;

/**
 * 環境変数ダイアログクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class EnvVariableDialog extends CommonDialog {
	/** プロパティシート */
	private PropertySheet m_viewer = null;
	/** シェル */
	private Shell m_shell = null;
	/** 環境変数情報 */
	private ArrayList<Object> m_envVariable = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親シェル
	 */
	public EnvVariableDialog(Shell parent) {
		super(parent);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see com.clustercontrol.monitor.action.GetEventFilterProperty#getProperty()
	 * @see com.clustercontrol.bean.JobParamTypeConstant
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();

		parent.getShell().setText(Messages.getString("job.environment.variable"));

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

		Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, tree);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		tree.setLayoutData(gridData);

		m_viewer = new PropertySheet(tree);
		m_viewer.setSize(100, 150);
		
		m_viewer.setInput(new EnvVariableProperty().getProperty());
		m_viewer.expandAll();

		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = m_shell.getDisplay();
		m_shell.setLocation(
				(display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);

		//開始条件反映
		reflectEnvVariable();
		m_shell.pack();
		m_shell.setSize(new Point(m_shell.getSize().x, 400));

		m_viewer.expandAll();
	}

	/**
	 * 環境変数をプロパティシートに反映します。
	 */
	private void reflectEnvVariable() {
		Property property = null;

		if (m_envVariable != null) {
				property = new EnvVariableProperty().getProperty();

				//環境変数ID
				Object[] p = property.getChildren();
				String envVariableId = (String) m_envVariable.get(GetEnvVariableTableDefine.ENV_VARIABLE_ID);
				((Property)p[0]).setValue(envVariableId);

				//値
				String value = (String) m_envVariable.get(GetEnvVariableTableDefine.VALUE);
				((Property)p[1]).setValue(value);

				//説明
				String description = (String) m_envVariable.get(GetEnvVariableTableDefine.DESCRIPTION);
				((Property)p[2]).setValue(description);
				
				m_viewer.setInput(property);

			//ビュー更新
			m_viewer.refresh();
		}
	}

	/**
	 * プロパティシートの情報から、環境変数情報を作成します。
	 *
	 * @return 入力値の検証結果
	 */
	private ValidateResult createEnvVariable() {
		ValidateResult result = null;

		m_envVariable = new ArrayList<Object>();

		Property property = (Property) m_viewer.getInput();
		Object[] p = property.getChildren();
		
			//環境変数ID
			String envVariableId = ((Property)p[0]).getValueText();
			if (envVariableId == null || envVariableId.length() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.19"));
				return result;
			}
			else{
				//アルファベット・数字・'_'・'-'以外は許容しない
				if (!Pattern.matches("[a-zA-Z0-9_-]*", envVariableId)) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.60"));
					return result;
				}
			}
			m_envVariable.add(envVariableId);

			//値
			String value = ((Property)p[1]).getValueText();
			if (value == null || value.length() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.17"));
				return result;
			}
			m_envVariable.add(value);

			//説明
			String description = ((Property)p[2]).getValueText();
			m_envVariable.add(description);

		return null;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(350, 400);
	}

	/**
	 * 環境変数情報を設定します。
	 *
	 * @param list 環境変数情報
	 */
	public void setInputData(ArrayList<Object> list) {
		m_envVariable = list;
	}

	/**
	 * 環境変数情報を返します。
	 *
	 * @return 環境変数情報
	 */
	public ArrayList<?> getInputData() {
		return m_envVariable;
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

		result = createEnvVariable();
		if (result != null) {
			return result;
		}

		return null;
	}
}
