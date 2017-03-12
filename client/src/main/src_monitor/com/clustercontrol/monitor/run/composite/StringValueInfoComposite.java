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

package com.clustercontrol.monitor.run.composite;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.monitor.run.dialog.StringValueInfoCreateDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * 文字列監視の判定情報（重要度）コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>値取得の成功時</dd>
 *  <dd>　判定情報一覧コンポジット</dd>
 *  <dd>　「追加」ボタン</dd>
 *  <dd>　「変更」ボタン</dd>
 *  <dd>　「削除」ボタン</dd>
 *  <dd>　「上へ」ボタン</dd>
 *  <dd>　「下へ」ボタン</dd>
 *  <dd>値取得の失敗時</dd>
 *  <dd>　「重要度」 コンボボックス</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class StringValueInfoComposite extends Composite {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = 5;

	/** カラム数（値）。 */
	public static final int WIDTH_VALUE = 2;

	/** 判定情報一覧 コンポジット。 */
	private StringValueListComposite m_infoList = null;

	/** 追加 ボタン。 */
	private Button m_buttonAdd = null;

	/** 変更 ボタン。 */
	private Button m_buttonModify = null;

	/** 削除 ボタン。 */
	private Button m_buttonDelete = null;

	/** コピー ボタン。 */
	private Button m_buttonCopy = null;

	/** 上へ ボタン。 */
	private Button m_buttonUp = null;

	/** 下へ ボタン。 */
	private Button m_buttonDown = null;

	/** メッセージにデフォルトを入れるフラグ(#[LOG_LINE]) */
	private boolean logLineFlag = false;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param tableDefine 文字列監視の判定情報一覧のテーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String, int, int, int)
	 * @see com.clustercontrol.monitor.run.action.GetStringFilterTableDefine
	 * @see #initialize(ArrayList)
	 */
	public StringValueInfoComposite(Composite parent, int style, ArrayList<TableColumnInfo> tableDefine, boolean logLineFlag) {
		super(parent, style);

		this.initialize(tableDefine);
		this.logLineFlag = logLineFlag;
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize(ArrayList<TableColumnInfo> tableDefine) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = CommonMonitorDialog.BASIC_UNIT;
		this.setLayout(layout);

		/*
		 * 文字列監視判定情報一覧
		 */
		this.m_infoList = new StringValueListComposite(this, SWT.BORDER, tableDefine);
		WidgetTestUtil.setTestId(this, "stringvaluelist", m_infoList);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = CommonMonitorDialog.LONG_UNIT;
		this.m_infoList.setLayoutData(gridData);

		/*
		 * 操作ボタン
		 */
		Composite composite = new Composite(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "button", composite);
		layout = new GridLayout(1, true);
		layout.numColumns = 1;
		composite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = CommonMonitorDialog.SHORT_UNIT;
		composite.setLayoutData(gridData);

		// 追加ボタン
		this.m_buttonAdd = this.createButton(composite, Messages.getString("add"));
		WidgetTestUtil.setTestId(this, "add", m_buttonAdd);
		this.m_buttonAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// シェルを取得
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				StringValueInfoCreateDialog dialog = new StringValueInfoCreateDialog(shell, logLineFlag);
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_infoList.getMonitorStringValueInfoList().add(dialog.getInputData());
					m_infoList.update();
				}
			}
		});

		// 変更ボタン
		this.m_buttonModify = this.createButton(composite, Messages.getString("modify"));
		WidgetTestUtil.setTestId(this, "modify", m_buttonModify);
		this.m_buttonModify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_infoList.getTableViewer().getTable().getSelectionIndex();
				if (order >= 0) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					StringValueInfoCreateDialog dialog = new StringValueInfoCreateDialog(shell, m_infoList.getMonitorStringValueInfoList().get(order));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_infoList.getMonitorStringValueInfoList().remove(order);
						m_infoList.getMonitorStringValueInfoList().add(order, dialog.getInputData());
						m_infoList.setSelection();
						m_infoList.update();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 削除ボタン
		this.m_buttonDelete = this.createButton(composite, Messages.getString("delete"));
		WidgetTestUtil.setTestId(this, "delete", m_buttonDelete);
		this.m_buttonDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_infoList.getTableViewer().getTable().getSelectionIndex();

				if (order >= 0) {
					String detail = m_infoList.getFilterItem().getDescription();
					if (detail == null) {
						detail = "";
					}

					String[] args = { detail };
					if (MessageDialog.openConfirm(
							null,
							Messages.getString("confirmed"),
							Messages.getString("message.monitor.31", args))) {
						m_infoList.getMonitorStringValueInfoList().remove(order);
						m_infoList.update();
					}
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// コピーボタン
		this.m_buttonCopy = this.createButton(composite, Messages.getString("copy"));
		WidgetTestUtil.setTestId(this, "copy", m_buttonCopy);
		this.m_buttonCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_infoList.getTableViewer().getTable().getSelectionIndex();
				if (order >= 0) {

					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					StringValueInfoCreateDialog dialog = new StringValueInfoCreateDialog(shell, m_infoList.getMonitorStringValueInfoList().get(order));
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = m_infoList.getTableViewer().getTable();
						WidgetTestUtil.setTestId(this, "modify", table);
						int selectIndex = table.getSelectionIndex();
						m_infoList.getMonitorStringValueInfoList().add(dialog.getInputData());
						m_infoList.update();
						table.setSelection(selectIndex);
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 上へボタン
		Label label = new Label(composite, SWT.NONE);	// ダミー
		WidgetTestUtil.setTestId(this, "up", label);
		this.m_buttonUp = this.createButton(composite, Messages.getString("up"));
		WidgetTestUtil.setTestId(this, "up", m_buttonUp);
		this.m_buttonUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_infoList.getTableViewer().getTable().getSelectionIndex();
				if (order >= 0) {
					m_infoList.up();
					m_infoList.update();
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});

		// 下へボタン
		this.m_buttonDown = this.createButton(composite, Messages.getString("down"));
		WidgetTestUtil.setTestId(this, "down", m_buttonDown);
		this.m_buttonDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int order = m_infoList.getTableViewer().getTable().getSelectionIndex();
				if (order >= 0) {
					m_infoList.down();
					m_infoList.update();
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.monitor.30"));
				}
			}
		});
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる監視情報
	 */
	public void setInputData(MonitorInfo info) {

		if(info != null){
			this.m_infoList.setInputData(info);
		}
		// 必須項目を明示
		this.update();
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
	 * @see com.clustercontrol.monitor.run.composite.StringValueListComposite#createInputData(MonitorInfo)
	 */
	public ValidateResult createInputData(MonitorInfo info) {

		// 文字列監視判定情報
		ValidateResult validateResult = m_infoList.createInputData(info);
		if(validateResult != null){
			return validateResult;
		}

		return null;
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_infoList.setEnabled(enabled);
		this.m_buttonAdd.setEnabled(enabled);
		this.m_buttonModify.setEnabled(enabled);
		this.m_buttonDelete.setEnabled(enabled);
		this.m_buttonCopy.setEnabled(enabled);
		this.m_buttonUp.setEnabled(enabled);
		this.m_buttonDown.setEnabled(enabled);
	}

	/**
	 * ボタンを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param label ボタンに表示するテキスト
	 * @return ボタン
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.NONE);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		button.setText(label);

		return button;
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
