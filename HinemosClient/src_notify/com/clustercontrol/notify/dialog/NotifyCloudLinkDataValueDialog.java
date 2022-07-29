/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.dialog;

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

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.PropertySheet;

/**
 * クラウド通知の値追加用ダイアログクラスです。
 *
 */
public class NotifyCloudLinkDataValueDialog extends CommonDialog {
	/** プロパティシート */
	private PropertySheet m_viewer = null;
	/** シェル */
	private Shell m_shell = null;
	/** データ一覧 */
	private ArrayList<Object> m_linkInfoDataList = null;
	
	private static final int NAME = 0;
	private static final int VALUE = 1;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 *            親シェル
	 */
	public NotifyCloudLinkDataValueDialog(Shell parent) {
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
		m_shell = this.getShell();

		parent.getShell().setText(Messages.getString("dialog.notify.cloud.link.data.value"));

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		Label tableTitle = new Label(parent, SWT.NONE);
		tableTitle.setText(Messages.getString("attribute") + " : ");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		tableTitle.setLayoutData(gridData);

		Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		tree.setLayoutData(gridData);

		m_viewer = new PropertySheet(tree);
		m_viewer.setSize(100, 150);

		m_viewer.setInput(getProperty());
		m_viewer.expandAll();

		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = m_shell.getDisplay();
		m_shell.setLocation((display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);

		// 開始条件反映
		reflectEnvVariable();
		m_shell.pack();
		m_shell.setSize(new Point(m_shell.getSize().x, 400));

		m_viewer.expandAll();
	}

	/**
	 * 連携情報データをプロパティシートに反映します。
	 */
	private void reflectEnvVariable() {
		Property property = null;

		if (m_linkInfoDataList != null) {
			property = getProperty();

			// 名前
			Object[] p = property.getChildren();
			String name = (String) m_linkInfoDataList.get(NAME);
			((Property) p[NAME]).setValue(name);

			// 値
			String value = (String) m_linkInfoDataList.get(VALUE);
			((Property) p[VALUE]).setValue(value);


			m_viewer.setInput(property);

			// ビュー更新
			m_viewer.refresh();
		}
	}

	/**
	 * プロパティシートの情報から、データ詳細を作成します。
	 *
	 * @return 入力値の検証結果
	 */
	private ValidateResult createEnvVariable() {
		ValidateResult result = null;

		
		m_linkInfoDataList=new ArrayList<Object>();
		
		
		Property property = (Property) m_viewer.getInput();
		Object[] p = property.getChildren();

		// 名前
		String key = ((Property) p[NAME]).getValueText();
		if (key == null || key.length() == 0) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.notify.cloud.4"));
			return result;
		}
		//値
		String value = ((Property) p[VALUE]).getValueText();
		if (value == null || value.length() == 0) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.notify.cloud.5"));
			return result;
		}

		m_linkInfoDataList.add(key);
		m_linkInfoDataList.add(value);

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
	 * 連携情報データを設定します。
	 *
	 * @param list
	 *            連携情報データ
	 */
	public void setInputData(ArrayList<Object> list) {
		m_linkInfoDataList = list;
	}

	/**
	 * 連携情報データを返します。
	 *
	 * @return 連携情報データ
	 */
	public ArrayList<Object> getInputData() {
		return m_linkInfoDataList;
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

	private Property getProperty() {
		// プロパティ項目定義
		Property name = new Property("key", Messages.getString("name"), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.TEXT);
		Property value = new Property("value", Messages.getString("value"), PropertyDefineConstant.EDITOR_TEXT,
				DataRangeConstant.TEXT);
		// 値を初期化
		name.setValue("");
		value.setValue("");

		// 変更の可/不可を設定
		name.setModify(PropertyDefineConstant.MODIFY_OK);
		value.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, null);

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(name);
		property.addChildren(value);

		return property;
	}
}
