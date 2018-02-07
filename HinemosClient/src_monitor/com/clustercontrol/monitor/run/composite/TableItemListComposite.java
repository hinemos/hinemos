/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.composite;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.monitor.run.viewer.TableItemTableViewer;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 文字列監視の判定情報一覧コンポジットクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class TableItemListComposite<T> extends Composite {

	/** テーブルビューアー。 */
	protected TableItemTableViewer m_tableViewer = null;

	/** テーブル定義情報。 */
	protected ITableItemCompositeDefine<T> m_define = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param tableDefine 判定情報一覧のテーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String, int, int, int)
	 * @see com.clustercontrol.monitor.run.action.GetStringFilterTableDefine
	 * @see #initialize()
	 */
	public TableItemListComposite(Composite parent, int style, ITableItemCompositeDefine<T> define) {
		super(parent, style);

		this.m_define = define;

		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	protected void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table table = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.m_tableViewer = new TableItemTableViewer(table, this.m_define.getLabelProvider());
		this.m_tableViewer.createTableColumn(this.m_define.getTableDefine());
		this.m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				T item = getTableItem();
				if (item != null) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					CommonDialog dialog = m_define.createDialog(shell, item);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = getTableViewer().getTable();
						WidgetTestUtil.setTestId(this, "doubleclick", table);
						int selectIndex = table.getSelectionIndex();
						m_define.getTableItemInfoManager().modify(item, m_define.getCurrentCreatedItem());
						table.setSelection(selectIndex);
						update();
					}
				}
			}
		});
	}

	/**
	 * このコンポジットが利用するテーブルビューアーを返します。
	 *
	 * @return テーブルビューアー
	 */
	public TableItemTableViewer getTableViewer() {
		return this.m_tableViewer;
	}

	/**
	 * 現在選択されているアイテムを返します。
	 * <p>
	 * 選択されていない場合は、<code>null</code>を返します。
	 *
	 * @return 選択アイテム
	 */
	@SuppressWarnings("unchecked")
	public T getTableItem() {
		StructuredSelection selection = (StructuredSelection) this.m_tableViewer.getSelection();

		if (selection == null) {
			return null;
		} else {
			return (T) selection.getFirstElement();
		}
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる監視情報
	 */
	public void setInputData(List<T> items) {

		if(items != null){
			// 文字列監視判定情報設定
			m_define.getTableItemInfoManager().initialize(items);
			// データ取得
			Object[] list = m_define.getTableItemInfoManager().get();

			// テーブル更新
			this.m_tableViewer.setInput(list);
		}
	}

	/**
	 * コンポジットを更新します。<BR>
	 * 判定情報一覧を取得し、テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.monitor.run.util.StringValueInfoManager#get()
	 * @see com.com.clustercontrol.monitor.run.viewer.TableItemTableViewer
	 */
	@Override
	public void update() {
		// データ取得
		Object[] list = m_define.getTableItemInfoManager().get();

		// テーブル更新
		this.m_tableViewer.setInput(list);
	}

	/**
	 * 引数で指定された監視情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。
	 * 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param monitorInfo 入力値を設定する監視情報
	 * @return 検証結果
	 *
	 * @see #setValidateResult(String, String)
	 */
	public List<T> getTableItemData() {
		return this.m_define.getTableItemInfoManager().getTableItemInfoList();
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_tableViewer.getTable().setEnabled(enabled);
	}
}
