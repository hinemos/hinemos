/*

Copyright (C) 2014 NTT DATA Corporation

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
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.util.Messages;

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
 * @version 5.0.0
 * @since 5.0.0
 */
public class TableItemInfoComposite<T> extends Composite {

	/** 判定情報一覧 コンポジット。 */
	protected TableItemListComposite<T> m_infoList = null;

	protected ITableItemCompositeDefine<T> m_define = null;

	/** 追加 ボタン。 */
	protected Button m_buttonAdd = null;

	/** 変更 ボタン。 */
	protected Button m_buttonModify = null;

	/** 削除 ボタン。 */
	protected Button m_buttonDelete = null;

	/** コピー ボタン。 */
	protected Button m_buttonCopy = null;

	/** 上へ ボタン。 */
	protected Button m_buttonUp = null;

	/** 下へ ボタン。 */
	protected Button m_buttonDown = null;

//	/** メッセージにデフォルトを入れるフラグ(#[LOG_LINE]) */
//	private boolean logLineFlag = false;

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
	public TableItemInfoComposite(Composite parent, int style, ITableItemCompositeDefine<T> define) {
		this(parent, style, define, null);
	}

	public TableItemInfoComposite(Composite parent, int style, ITableItemCompositeDefine<T> define, List<T> items) {
		super(parent, style);

		this.m_define = define;
		if(items != null){
			this.m_define.initTableItemInfoManager(items);
		} else {
			this.m_define.initTableItemInfoManager();
		}
		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	protected void initialize() {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		/*
		 * 文字列監視判定情報一覧
		 */
		this.m_infoList = new TableItemListComposite<T>(this, SWT.BORDER, this.m_define);
		WidgetTestUtil.setTestId(this, "pagelist", m_infoList);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 11;
		this.m_infoList.setLayoutData(gridData);

		/*
		 * 操作ボタン
		 */
		Composite composite = new Composite(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "button", composite);
		layout = new GridLayout(1, true);
		if((this.m_define.getButtonOptions() & ITableItemCompositeDefine.MULTI) != 0){
			layout.numColumns = 2;
		}
		composite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 4;
		composite.setLayoutData(gridData);


		// 追加ボタン
		if((this.m_define.getButtonOptions() & ITableItemCompositeDefine.ADD) != 0){
			this.m_buttonAdd = this.createButton(composite, Messages.getString("add"));
			WidgetTestUtil.setTestId(this, "add", m_buttonAdd);
			this.m_buttonAdd.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					CommonDialog dialog = m_define.createDialog(shell);
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_define.getTableItemInfoManager().add(m_define.getCurrentCreatedItem());
						m_infoList.update();
					}
				}
			});
		}
		// 変更ボタン
		if((this.m_define.getButtonOptions() & ITableItemCompositeDefine.MODIFY) != 0){
			this.m_buttonModify = this.createButton(composite, Messages.getString("modify"));
			WidgetTestUtil.setTestId(this, "modify", m_buttonModify);
			this.m_buttonModify.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					T item = getSelectedItem();
					if (item != null) {

						// シェルを取得
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

						CommonDialog dialog = m_define.createDialog(shell, item);
						if (dialog.open() == IDialogConstants.OK_ID) {
							Table table = m_infoList.getTableViewer().getTable();
							WidgetTestUtil.setTestId(this, "modify", table);
							int selectIndex = table.getSelectionIndex();
							m_define.getTableItemInfoManager().modify(item, m_define.getCurrentCreatedItem());
							m_infoList.update();
							table.setSelection(selectIndex);
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
		}

		// 削除ボタン
		if((this.m_define.getButtonOptions() & ITableItemCompositeDefine.DELETE) != 0){
			this.m_buttonDelete = this.createButton(composite, Messages.getString("delete"));
			WidgetTestUtil.setTestId(this, "delete", m_buttonDelete);
			this.m_buttonDelete.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					T item = m_infoList.getTableItem();

					if (item != null) {
						String detail = m_define.getItemsIdentifier(item);

						String[] args = { detail };
						if (MessageDialog.openConfirm(
								null,
								Messages.getString("confirmed"),
								Messages.getString("message.monitor.31", args))) {

							m_define.getTableItemInfoManager().delete(getSelectedItem());
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
		}

		// コピーボタン
		if((this.m_define.getButtonOptions() & ITableItemCompositeDefine.COPY) != 0){
			this.m_buttonCopy = this.createButton(composite, Messages.getString("copy"));
			WidgetTestUtil.setTestId(this, "copy", m_buttonCopy);
			this.m_buttonCopy.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					T item = m_infoList.getTableItem();
					if (item != null) {

						// シェルを取得
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

						CommonDialog dialog = m_define.createDialog(shell, item);
						if (dialog.open() == IDialogConstants.OK_ID) {
							Table table = m_infoList.getTableViewer().getTable();
							WidgetTestUtil.setTestId(this, "modify", table);
							int selectIndex = table.getSelectionIndex();
							m_define.getTableItemInfoManager().add(m_define.getCurrentCreatedItem());
							m_infoList.update();
							table.setSelection(selectIndex);
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
		}

		// スペース
		if((this.m_define.getButtonOptions() & ITableItemCompositeDefine.SPACE) != 0){
			Label label = new Label(composite, SWT.NONE);	// ダミー
			WidgetTestUtil.setTestId(this, "space", label);
		}

		// 上へボタン
		if((this.m_define.getButtonOptions() & ITableItemCompositeDefine.UP) != 0){
			this.m_buttonUp = this.createButton(composite, Messages.getString("up"));
			WidgetTestUtil.setTestId(this, "up", m_buttonUp);
			this.m_buttonUp.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					T item = m_infoList.getTableItem();
					if (item != null) {
						if (m_define.getTableItemInfoManager().upOrder(item)) {
							m_infoList.update();
							selectItem(item);
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
		}

		// 下へボタン
		if((this.m_define.getButtonOptions() & ITableItemCompositeDefine.DOWN) != 0){
			this.m_buttonDown = this.createButton(composite, Messages.getString("down"));
			WidgetTestUtil.setTestId(this, "down", m_buttonDown);
			this.m_buttonDown.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					T item = m_infoList.getTableItem();
					if (item != null) {
						if (m_define.getTableItemInfoManager().downOrder(item)) {
							m_infoList.update();
							selectItem(item);
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
		}
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる監視情報
	 */
	public void setInputData(List<T> list) {

		if(list != null){
			this.m_infoList.setInputData(list);
		}
		// 必須項目を明示
		this.update();
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_infoList.setEnabled(enabled);
		if(this.m_buttonAdd != null){
			this.m_buttonAdd.setEnabled(enabled);
		}
		if(this.m_buttonModify != null){
			this.m_buttonModify.setEnabled(enabled);
		}
		if(this.m_buttonDelete != null){
			this.m_buttonDelete.setEnabled(enabled);
		}
		if(this.m_buttonCopy != null){
			this.m_buttonCopy.setEnabled(enabled);
		}
		if(this.m_buttonUp != null){
			this.m_buttonUp.setEnabled(enabled);
		}
		if(this.m_buttonDown != null){
			this.m_buttonDown.setEnabled(enabled);
		}
	}

	/**
	 * ボタンを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param label ボタンに表示するテキスト
	 * @return ボタン
	 */
	protected Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, button);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		button.setText(label);

		return button;
	}

	/**
	 * 引数で指定された判定情報の行を選択状態にします。
	 *
	 * @param identifier 識別キー
	 */
	protected void selectItem(T item) {
		Table table = this.m_infoList.getTableViewer().getTable();
		WidgetTestUtil.setTestId(this, null, table);
		TableItem[] items = table.getItems();

		if (items == null || item == null) {
			return;
		}

		for (int i = 0; i < items.length; i++) {
			@SuppressWarnings("unchecked")
			T tmpItem = (T) items[i].getData();
			WidgetTestUtil.setTestId(this, "items" + i, items[i]);
			if (item.equals(tmpItem)) {
				table.select(i);
				return;
			}
		}
	}

	/**
	 * 選択されている判定情報の識別キーを返します。
	 *
	 * @return 識別キー。選択されていない場合は、<code>null</code>。
	 */
	protected T getSelectedItem() {
		StructuredSelection selection = (StructuredSelection) this.m_infoList.getTableViewer().getSelection();

		@SuppressWarnings("unchecked")
		T tableItemInfo = (T) selection.getFirstElement();

		return tableItemInfo;
	}

	/**
	 * テーブルに登録されているアイテムを返します。
	 *
	 * @return List<T> テーブルアイテム。
	 */
	public List<T> getItems(){
		return this.m_infoList.getTableItemData();
	}
}
