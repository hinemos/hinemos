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
import java.util.HashMap;
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
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetParameterTableDefine;
import com.clustercontrol.jobmanagement.action.ParameterProperty;
import com.clustercontrol.jobmanagement.bean.JobParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobParamTypeMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.PropertySheet;

/**
 * ジョブ変数ダイアログクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class ParameterDialog extends CommonDialog {
	/** プロパティシート */
	private PropertySheet m_viewer = null;
	/** シェル */
	private Shell m_shell = null;
	/** ジョブ変数情報 */
	private ArrayList<Object> m_parameter = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親シェル
	 */
	public ParameterDialog(Shell parent) {
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

		parent.getShell().setText(Messages.getString("job.parameter"));

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

		int type = JobParamTypeConstant.TYPE_USER;
		if (m_parameter != null) {
			type = (Integer) m_parameter.get(GetParameterTableDefine.TYPE);
		}
		m_viewer.setInput(new ParameterProperty().getProperty(type));
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
		reflectParameter();
		m_shell.pack();
		m_shell.setSize(new Point(m_shell.getSize().x, 400));

		m_viewer.expandAll();
	}

	/**
	 * ジョブ変数情報をプロパティシートに反映します。
	 *
	 * @see com.clustercontrol.jobmanagement.action.ParameterProperty
	 * @see com.clustercontrol.jobmanagement.action.ParameterProperty#getProperty(int)
	 * @see com.clustercontrol.bean.JobParamTypeConstant
	 */
	private void reflectParameter() {
		Property property = null;

		if (m_parameter != null) {
			//パラメータを設定
			Integer type = (Integer) m_parameter.get(
					GetParameterTableDefine.TYPE);

			if (type == JobParamTypeConstant.TYPE_USER) {
				//種別がユーザの場合
				property = new ParameterProperty().getProperty(JobParamTypeConstant.TYPE_USER);
				ArrayList<?> propertyList = PropertyUtil.getProperty(property,
						ParameterProperty.ID_TYPE);
				Property paramType = (Property) propertyList.get(0);
				Object values[][] = paramType.getSelectValues();
				paramType.setValue(JobParamTypeMessage.STRING_USER);

				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) values[PropertyDefineConstant.SELECT_VALUE][0];
				ArrayList<?> list = (ArrayList<?>) map.get("property");

				//パラメータIDを設定
				String paramId = (String) m_parameter.get(GetParameterTableDefine.PARAM_ID);
				((Property) list.get(0)).setValue(paramId);

				//値
				String value = (String) m_parameter.get(GetParameterTableDefine.VALUE);
				((Property) list.get(1)).setValue(value);

				//説明
				String description = (String) m_parameter.get(GetParameterTableDefine.DESCRIPTION);
				((Property) list.get(2)).setValue(description);
				m_viewer.setInput(property);
			}

			//ビュー更新
			m_viewer.refresh();
		}
	}

	/**
	 * プロパティシートの情報から、ジョブ変数情報を作成します。
	 *
	 * @return 入力値の検証結果
	 *
	 * @see com.clustercontrol.jobmanagement.action.ParameterProperty
	 * @see com.clustercontrol.jobmanagement.action.ParameterProperty#getProperty(int)
	 * @see com.clustercontrol.bean.JobParamTypeConstant
	 */
	private ValidateResult createParameter() {
		ValidateResult result = null;

		m_parameter = new ArrayList<Object>();

		Property property = (Property) m_viewer.getInput();

		//種別を取得
		ArrayList<?> values = PropertyUtil.getPropertyValue(property,
				ParameterProperty.ID_TYPE);
		String type = (String) values.get(0);

		if (type.equals(JobParamTypeMessage.STRING_USER)) {
			//パラメータID
			values = PropertyUtil.getPropertyValue(property,
					ParameterProperty.ID_USER_PARAM_ID);
			String paramId = (String) values.get(0);
			if (paramId == null || paramId.length() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.19"));
				return result;
			}
			else{
				//アルファベット・数字・'_'・'-'以外は許容しない
				if (!Pattern.matches("[a-zA-Z0-9_-]*", paramId)) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.60"));
					return result;
				}
			}
			m_parameter.add(paramId);

			//種別
			m_parameter.add(JobParamTypeConstant.TYPE_USER);

			//値
			values = PropertyUtil.getPropertyValue(property,
					ParameterProperty.ID_VALUE);
			String value = (String) values.get(0);
			if (value == null || value.length() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.17"));
				return result;
			}

			m_parameter.add(value);

			//説明
			values = PropertyUtil.getPropertyValue(property,
					ParameterProperty.ID_DESCRIPTION);
			String description = (String) values.get(0);
			m_parameter.add(description);
		}

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
	 * ジョブ変数情報を設定します。
	 *
	 * @param list ジョブ変数情報
	 */
	public void setInputData(ArrayList<Object> list) {
		m_parameter = list;
	}

	/**
	 * ジョブ変数情報を返します。
	 *
	 * @return ジョブ変数情報
	 */
	public ArrayList<?> getInputData() {
		return m_parameter;
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 * @since 2.1.0
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 * @since 2.1.0
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

		result = createParameter();
		if (result != null) {
			return result;
		}

		return null;
	}
}
