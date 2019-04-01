/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.repository.action.GetNodeCustomTableDefine;
import com.clustercontrol.repository.dialog.NodeCustomInfoCreateDialog;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.repository.NodeConfigCustomInfo;
import com.clustercontrol.ws.repository.NodeConfigSettingInfo;

/**
 * ユーザ任意情報一覧テーブル<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeCustomListComposite extends Composite {

	/** テーブルビューアー。 */
	private CommonTableViewer m_tableViewer = null;

	/** テーブル定義情報。 */
	private ArrayList<TableColumnInfo> m_tableDefine = null;

	/** ユーザ任意情報{@literal <ユーザ任意情報ID, ユーザ任意情報>}} */
	private HashMap<String, NodeConfigCustomInfo> m_customInfoMap = null;

	/** DB登録済ユーザ任意情報IDリスト */
	private ArrayList<String> registeredCustomIdList = null;

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
	public NodeCustomListComposite(Composite parent, int style) {
		super(parent, style);
		ArrayList<TableColumnInfo> tableDefine = GetNodeCustomTableDefine.get();
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
		this.m_tableViewer.createTableColumn(this.m_tableDefine, GetNodeCustomTableDefine.SETTING_CUSTOM_ID,
				GetNodeCustomTableDefine.SORT_ORDER);
		this.m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				NodeConfigCustomInfo info = getFilterItem();
				if (info != null) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					NodeCustomInfoCreateDialog dialog = new NodeCustomInfoCreateDialog(shell, info, getCustomIdList(),
							false);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = getTableViewer().getTable();
						int selectIndex = table.getSelectionIndex();
						m_customInfoMap.put(dialog.getCustomId(), dialog.getInputData());
						update();
						table.setSelection(selectIndex);
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
	public CommonTableViewer getTableViewer() {
		return this.m_tableViewer;
	}

	/**
	 * 現在選択されているアイテムを返します。
	 * <p>
	 * 選択されていない場合は、<code>null</code>を返します。
	 *
	 * @return 選択アイテム
	 */
	public NodeConfigCustomInfo getFilterItem() {
		StructuredSelection selection = (StructuredSelection) this.m_tableViewer.getSelection();
		if (selection == null) {
			return null;
		} else {
			ArrayList<?> list = (ArrayList<?>) selection.getFirstElement();
			String displayId = (String) list.get(0);
			return this.m_customInfoMap.get(displayId);
		}
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info
	 *            設定値として用いる監視情報
	 */

	public void setInputData(NodeConfigSettingInfo info) {

		if (info != null && info.getNodeConfigCustomList() != null && !info.getNodeConfigCustomList().isEmpty()) {
			// 設定
			this.m_customInfoMap = new HashMap<String, NodeConfigCustomInfo>();
			this.registeredCustomIdList = new ArrayList<String>();
			for (NodeConfigCustomInfo customInfo : info.getNodeConfigCustomList()) {
				this.m_customInfoMap.put(customInfo.getSettingCustomId(), customInfo);
				this.registeredCustomIdList.add(customInfo.getSettingCustomId());
			}

			// テーブル更新
			update();
		} else {
			this.m_customInfoMap = new HashMap<String, NodeConfigCustomInfo>();
			this.registeredCustomIdList = null;
		}
	}

	/**
	 * コンポジットを更新します。<BR>
	 */
	@Override
	public void update() {
		// テーブル更新
		ArrayList<Object> listAll = new ArrayList<Object>();
		for (NodeConfigCustomInfo info : this.m_customInfoMap.values()) {
			ArrayList<Object> list = new ArrayList<Object>();

			// ユーザ任意情報ID.
			list.add(info.getSettingCustomId());

			// ユーザ任意情報名.
			list.add(info.getDisplayName());

			// コマンド.
			list.add(info.getCommand());

			// 説明
			list.add(info.getDescription());

			// 実行ユーザ.
			if (info.isSpecifyUser()) {
				list.add(info.getEffectiveUser());
			} else {
				list.add(Messages.getString("agent.user"));
			}

			// 有効/無効
			list.add(info.isValidFlg());
			listAll.add(list);
		}

		this.m_tableViewer.setInput(listAll);
	}

	/**
	 * 引数で指定された設定情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param monitorInfo
	 *            入力値を設定する監視情報
	 * @return 検証結果
	 *
	 * @see #setValidateResult(String, String)
	 */
	public ValidateResult createInputData(NodeConfigSettingInfo nodeConfigInfo) {

		if (this.m_customInfoMap != null && !this.m_customInfoMap.isEmpty()) {
			String settingId = nodeConfigInfo.getSettingId();

			for (NodeConfigCustomInfo info : this.m_customInfoMap.values()) {
				info.setSettingCustomId(settingId);
			}
			List<NodeConfigCustomInfo> customInfoList = nodeConfigInfo.getNodeConfigCustomList();
			customInfoList.clear();
			customInfoList.addAll(this.m_customInfoMap.values());
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
		int selectIndex = calDetailListSetSelectionTable.getSelectionIndex();
		update();
		calDetailListSetSelectionTable.setSelection(selectIndex);
	}

	public HashMap<String, NodeConfigCustomInfo> getNodeConfigCustomInfoMap() {
		return this.m_customInfoMap;
	}

	public ArrayList<String> getCustomIdList() {
		ArrayList<String> customIdList = new ArrayList<String>();
		if (this.m_customInfoMap == null || this.m_customInfoMap.isEmpty()) {
			return customIdList;
		}

		customIdList.addAll(this.m_customInfoMap.keySet());
		return customIdList;
	}
}
