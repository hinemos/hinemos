/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.composite;

import java.util.ArrayList;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.binary.dialog.BinaryPatternInfoCreateDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.action.GetStringFilterTableDefine;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.monitor.BinaryPatternInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * 文字列監視の判定情報一覧コンポジットクラス<BR>
 *
 * @version 6.1.0 バイナリ監視用フィルタ追加対応
 * @since 6.1.0
 */
public class BinaryListComposite extends Composite {

	/** テーブルビューアー。 */
	private CommonTableViewer m_tableViewer = null;

	/** テーブル定義情報。 */
	private ArrayList<TableColumnInfo> m_tableDefine = null;

	/** バイナリ検索条件情報(一覧としては非表示) */
	private ArrayList<BinaryPatternInfo> m_binaryPatternInfoList = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
	 * @see #initialize()
	 */
	public BinaryListComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 * @param tableDefine
	 *            判定情報一覧のテーブル定義情報（
	 *            {@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int
	 *      style)
	 * @see com.clustercontrol.bean.TableColumnInfo#TableColumnInfo(java.lang.String,
	 *      int, int, int)
	 * @see com.clustercontrol.monitor.run.action.GetStringFilterTableDefine
	 * @see #initialize()
	 */
	public BinaryListComposite(Composite parent, int style, ArrayList<TableColumnInfo> tableDefine) {
		super(parent, style);

		this.m_tableDefine = tableDefine;

		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
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
		this.m_tableViewer = new CommonTableViewer(table);
		this.m_tableViewer.createTableColumn(m_tableDefine, GetStringFilterTableDefine.ORDER_NO,
				GetStringFilterTableDefine.SORT_ORDER);

		IDoubleClickListener dclickListner = null;

		// バイナリ監視の場合のリスナー.
		dclickListner = new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				BinaryPatternInfo binaryInfo = getFilterItemBinary();
				if (binaryInfo != null) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					BinaryPatternInfoCreateDialog dialog = new BinaryPatternInfoCreateDialog(shell, binaryInfo);
					// OKボタン押下後処理.
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = getTableViewer().getTable();
						WidgetTestUtil.setTestId(this, null, table);
						int selectIndex = table.getSelectionIndex();
						m_binaryPatternInfoList.set(selectIndex, dialog.getBinaryInputData());
						update();
						table.setSelection(selectIndex);
					}
				}
			}
		};

		this.m_tableViewer.addDoubleClickListener(dclickListner);
	}

	/**
	 * このコンポジットが利用するテーブルビューアーを返します。
	 *
	 * @return テーブルビューアー
	 */
	public CommonTableViewer getTableViewer() {
		return this.m_tableViewer;
	}

	/**
	 * 現在選択されているアイテムに紐づくバイナリ検索条件情報を取得.
	 * <p>
	 * 選択されていない場合は、<code>null</code>を返します。
	 *
	 * @return 選択アイテムに紐づくバイナリ検索条件情報
	 */
	public BinaryPatternInfo getFilterItemBinary() {
		StructuredSelection selection = (StructuredSelection) this.m_tableViewer.getSelection();

		if (selection == null) {
			return null;
		} else {
			ArrayList<?> list = (ArrayList<?>) selection.getFirstElement();
			return (BinaryPatternInfo) m_binaryPatternInfoList.get((Integer) list.get(0) - 1);
		}
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info
	 *            設定値として用いる監視情報
	 */

	public void setInputData(MonitorInfo info) {

		if (info != null) {
			// バイナリ監視の場合はバイナリ検索条件も設定.
			m_binaryPatternInfoList = new ArrayList<BinaryPatternInfo>(info.getBinaryPatternInfo());

			// テーブル更新
			update();
		}
	}

	/**
	 * コンポジットを更新します。<BR>
	 * 判定情報一覧を取得し、テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.monitor.run.util.StringValueInfoManager#get()
	 * @see com.clustercontrol.monitor.run.viewer.StringValueListTableViewer
	 */
	@Override
	public void update() {
		// テーブル更新
		ArrayList<Object> listAll = new ArrayList<Object>();
		int i = 1;
		for (BinaryPatternInfo info : m_binaryPatternInfoList) {
			ArrayList<Object> list = new ArrayList<Object>();

			// 順序
			list.add(i);

			// 処理
			list.add(info.isProcessType());

			// 重要度
			if (!info.isProcessType()) {
				// 処理しないの場合は空欄
				list.add(PriorityConstant.TYPE_NONE);
			} else {
				list.add(info.getPriority());
			}

			// パターンマッチ表現
			list.add(info.getGrepString());

			// 説明
			list.add(info.getDescription());

			// 有効/無効
			list.add(info.isValidFlg());

			listAll.add(list);
			++i;
		}

		m_tableViewer.setInput(listAll);
	}

	/**
	 * 引数で指定された監視情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param monitorInfo
	 *            入力値を設定する監視情報
	 * @return 検証結果
	 *
	 * @see #setValidateResult(String, String)
	 */
	public ValidateResult createInputData(MonitorInfo monitorInfo) {

		if (m_binaryPatternInfoList != null && m_binaryPatternInfoList.size() > 0) {
			String MonitorId = monitorInfo.getMonitorId();

			for (int index = 0; index < m_binaryPatternInfoList.size(); index++) {
				BinaryPatternInfo info = m_binaryPatternInfoList.get(index);
				info.setMonitorId(MonitorId);
			}
			List<BinaryPatternInfo> binaryPatternInfoList = monitorInfo.getBinaryPatternInfo();
			binaryPatternInfoList.clear();
			binaryPatternInfoList.addAll(m_binaryPatternInfoList);
		}

		return null;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_tableViewer.getTable().setEnabled(enabled);
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id
	 *            ID
	 * @param message
	 *            メッセージ
	 * @return 認証結果
	 */
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	/**
	 * 選択したテーブル行番号を返す。
	 *
	 */
	public Integer getSelection() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();
		if (selection.getFirstElement() instanceof ArrayList) {
			ArrayList<?> list = (ArrayList<?>) selection.getFirstElement();
			if (list.get(0) instanceof Integer) {
				return (Integer) list.get(0);
			}
		}
		return null;
	}

	public void setSelection() {
		Table calDetailListSetSelectionTable = m_tableViewer.getTable();
		WidgetTestUtil.setTestId(this, null, calDetailListSetSelectionTable);
		int selectIndex = calDetailListSetSelectionTable.getSelectionIndex();
		update();
		calDetailListSetSelectionTable.setSelection(selectIndex);
	}

	/**
	 * 引数で指定された判定情報の行を選択状態にします。
	 *
	 * @param identifier
	 *            識別キー
	 */
	private void selectItem(Integer order) {
		Table stringValueListSelectItemTable = m_tableViewer.getTable();
		WidgetTestUtil.setTestId(this, null, stringValueListSelectItemTable);
		TableItem[] items = stringValueListSelectItemTable.getItems();

		if (items == null || order == null) {
			return;
		}
		stringValueListSelectItemTable.select(order);
		return;
	}

	/**
	 * テーブル選択項目の優先度を上げる
	 */
	public void up() {
		// 選択したテーブル行番号を取得
		Integer order = getSelection();

		// 行番号は1から始まるので、-1する
		--order;

		if (order > 0) {
			BinaryPatternInfo aa = m_binaryPatternInfoList.get(order);
			BinaryPatternInfo bb = m_binaryPatternInfoList.get(order - 1);
			m_binaryPatternInfoList.set(order, bb);
			m_binaryPatternInfoList.set(order - 1, aa);
		}
		update();
		// 更新後に再度選択項目にフォーカスをあてる
		selectItem(order - 1);
	}

	/**
	 * テーブル選択項目の優先度を下げる
	 */
	public void down() {
		// 選択したテーブル行番号を取得
		Integer order = getSelection();

		// 行番号は1から始まるので、-1する
		--order;

		if (order < m_binaryPatternInfoList.size() - 1) {
			BinaryPatternInfo aa = m_binaryPatternInfoList.get(order);
			BinaryPatternInfo bb = m_binaryPatternInfoList.get(order + 1);
			m_binaryPatternInfoList.set(order, bb);
			m_binaryPatternInfoList.set(order + 1, aa);
		}
		update();
		// 更新後に再度選択項目にフォーカスをあてる
		selectItem(order + 1);
	}

	public ArrayList<BinaryPatternInfo> getBinaryPatternInfoList() {
		return m_binaryPatternInfoList;
	}
}
