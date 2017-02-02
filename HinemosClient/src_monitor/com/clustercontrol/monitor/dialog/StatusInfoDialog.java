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

package com.clustercontrol.monitor.dialog;

import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.Property;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.monitor.util.StatusDataPropertyUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.viewer.PropertySheet;

/**
 * 監視[ステータスの詳細]ダイアログクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class StatusInfoDialog extends CommonDialog {

	/** 選択されたアイテム。 */
	private List<?> m_list = null;

	/** プロパティシート。 */
	private PropertySheet propertySheet = null;

	/** プロパティ  */
	private Property statusCommentProperty = null;

	/**
	 * インスタンスを返します。
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public StatusInfoDialog(Shell parent, List<?> list) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		m_list = list;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(550, 500);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.monitor.info.status"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		/*
		 * 属性プロパティシート
		 */

		// ラベル
		Label label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "attribute", label);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("attribute") + " : ");

		// プロパティシート
		Tree table = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, table);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		this.propertySheet = new PropertySheet(table);
		this.propertySheet.setSize(170, 280);

		// プロパティ取得及び設定
		statusCommentProperty = null;
		if (m_list != null) {
			statusCommentProperty = StatusDataPropertyUtil.dto2property(m_list, Locale.getDefault());
			this.propertySheet.setInput(statusCommentProperty);
		}

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
	 * 入力値を保持したプロパティを返します。<BR>
	 * プロパティシートよりプロパティを取得します。
	 *
	 * @return プロパティ
	 *
	 * @see com.clustercontrol.viewer.PropertySheet#getInput()
	 */
	public Property getInputData() {
		if(statusCommentProperty != null){
			Property copy = PropertyUtil.copy(statusCommentProperty);
			return copy;
		}
		else{
			return null;
		}
	}

	/**
	 * 入力値を保持したプロパティを設定します。
	 *
	 * @param property プロパティ
	 */
	public void setInputData(Property property) {
		propertySheet.setInput(property);
	}

	/**
	 * 閉じるボタンを作成します。
	 *
	 * @param parent 親のコンポジット（ボタンバー）
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// 閉じる(cancel)ボタン
		this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("cancel"), false);
	}
}
