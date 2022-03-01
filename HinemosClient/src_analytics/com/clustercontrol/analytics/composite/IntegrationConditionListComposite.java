/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.composite;

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
import org.openapitools.client.model.IntegrationCheckInfoResponse;
import org.openapitools.client.model.IntegrationConditionInfoResponse;

import com.clustercontrol.analytics.action.GetIntegrationConditionTableDefine;
import com.clustercontrol.analytics.dialog.IntegrationConditionCreateDialog;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * 収集値統合監視の判定条件一覧コンポジットクラス<BR>
 *
 * @version 6.1.0
 */
public class IntegrationConditionListComposite extends Composite {

	/** テーブルビューアー。 */
	private CommonTableViewer m_tableViewer = null;

	/** テーブル定義情報。 */
	private ArrayList<TableColumnInfo> m_tableDefine = null;

	/** 判定条件情報 */
	private ArrayList<IntegrationConditionInfoResponse> m_conditionList = null;

	/** マネージャ名 */
	private String m_managerName = null;

	/** オーナーロールID */
	private String m_ownerRoleId = null;

	/** 監視設定のファシリティID */
	private String m_monitorFacilityId = null;

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
	 */
	public IntegrationConditionListComposite(Composite parent, int style, ArrayList<TableColumnInfo> tableDefine) {
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
		gridData.widthHint = 400;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.m_tableViewer = new CommonTableViewer(table);
		this.m_tableViewer.createTableColumn(m_tableDefine, GetIntegrationConditionTableDefine.ORDER_NO,
				GetIntegrationConditionTableDefine.SORT_ORDER);
		this.m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IntegrationConditionInfoResponse info = getItem();
				if (info != null) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					IntegrationConditionCreateDialog dialog = new IntegrationConditionCreateDialog(shell, m_managerName,
							m_ownerRoleId, m_monitorFacilityId, info);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = getTableViewer().getTable();
						int selectIndex = (Integer) ((ArrayList<?>) table.getSelection()[0].getData()).get(0) - 1;
						m_conditionList.set(selectIndex, dialog.getInputData());
						update();
						table.setSelection(table.getSelectionIndex());
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
	public IntegrationConditionInfoResponse getItem() {
		StructuredSelection selection = (StructuredSelection) this.m_tableViewer.getSelection();

		if (selection == null) {
			return null;
		} else {
			ArrayList<?> list = (ArrayList<?>)selection.getFirstElement();
			return (IntegrationConditionInfoResponse) m_conditionList.get((Integer)list.get(0)-1);
		}
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info
	 *            設定値として用いる監視情報
	 */
	public void setInputData(IntegrationCheckInfoResponse checkInfo) {
		if (checkInfo.getConditionList() != null) {
			// 文字列監視判定情報設定
			m_conditionList = new ArrayList<>(checkInfo.getConditionList());
		} else {
			m_conditionList = new ArrayList<>();
		}
		// テーブル更新
		update();
	}

	/**
	 * コンポジットを更新します。<BR>
	 * 判定情報一覧を取得し、テーブルビューアーにセットします。
	 */
	@Override
	public void update() {
		// テーブル更新
		ArrayList<Object> listAll = new ArrayList<Object>();
		int i = 1;
		for (IntegrationConditionInfoResponse info : m_conditionList) {
			ArrayList<Object> list = new ArrayList<Object>();
			// 順序
			list.add(i);
			// 対象ノード
			String targetNode = "";
			try {
				RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(this.m_managerName);
				if (info.getMonitorNode()) {
					targetNode = wrapper.getFacilityPath(m_monitorFacilityId, null).getFacilityPath();
				} else {
					targetNode = wrapper.getFacilityPath(info.getTargetFacilityId(), null).getFacilityPath();
				}
			} catch (HinemosUnknown | InvalidRole | InvalidUserPass | RestConnectFailed e) {
				// エラー時は何もしない
			}
			list.add(HinemosMessage.replace(targetNode));
			// 収集値種別
			String monitorType = "";
			if (info.getTargetMonitorType() != null) {
				if (info.getTargetMonitorType() == IntegrationConditionInfoResponse.TargetMonitorTypeEnum.NUMERIC) {
					monitorType = Messages.getString("numeric");
				} else if (info.getTargetMonitorType() == IntegrationConditionInfoResponse.TargetMonitorTypeEnum.STRING) {
					monitorType = Messages.getString("string");
				}
			}
			list.add(monitorType);
			// 収集項目名
			list.add(HinemosMessage.replace(info.getTargetItemDisplayName()));
			// 比較方法
			list.add(info.getComparisonMethod());
			// 比較値
			list.add(info.getComparisonValue());
			// 説明
			list.add(info.getDescription());
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
	 */
	public ValidateResult createInputData(IntegrationCheckInfoResponse checkInfo) {
		if (checkInfo != null) {
			List<IntegrationConditionInfoResponse> conditionList = checkInfo.getConditionList();
			conditionList.clear();
			if (m_conditionList != null && m_conditionList.size() > 0) {
				conditionList.addAll(m_conditionList);
			}
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
		Table integrationConditionListSelectItemTable = m_tableViewer.getTable();
		TableItem[] items = integrationConditionListSelectItemTable.getItems();

		if (items == null || order == null) {
			return;
		}
		integrationConditionListSelectItemTable.select(order);
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
			IntegrationConditionInfoResponse a = m_conditionList.get(order);
			IntegrationConditionInfoResponse b = m_conditionList.get(order-1);
			m_conditionList.set(order, b);
			m_conditionList.set(order - 1, a);
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

		if (order < m_conditionList.size() - 1) {
			IntegrationConditionInfoResponse a = m_conditionList.get(order);
			IntegrationConditionInfoResponse b = m_conditionList.get(order + 1);
			m_conditionList.set(order, b);
			m_conditionList.set(order + 1, a);
		}
		update();
		// 更新後に再度選択項目にフォーカスをあてる
		selectItem(order + 1);
	}

	public ArrayList<IntegrationConditionInfoResponse> getIntegrationConditionList() {
		return m_conditionList;
	}

	/**
	 * マネージャ名設定
	 * 
	 * @param managerName
	 *            マネージャ名
	 */
	public void setManagerName(String managerName) {
		m_managerName = managerName;
	}

	/**
	 * オーナーロールID設定
	 * 
	 * @param ownerRoleId
	 *            オーナーロールID
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		m_ownerRoleId = ownerRoleId;
	}

	/**
	 * 監視設定のファシリティID設定
	 * 
	 * @param monitorFacilityId
	 *            監視設定のファシリティID
	 */
	public void setMonitorFacilityId(String monitorFacilityId) {
		m_monitorFacilityId = monitorFacilityId;
	}
}
