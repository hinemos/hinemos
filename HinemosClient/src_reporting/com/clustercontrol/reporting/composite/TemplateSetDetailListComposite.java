/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.composite;

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

import com.clustercontrol.reporting.action.GetTemplateSetDetailTableDefine;
import com.clustercontrol.reporting.dialog.TemplateSetDetailDialog;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.reporting.TemplateSetDetailInfo;

/**
 * テンプレートセット詳細情報一覧コンポジットクラス<BR>
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class TemplateSetDetailListComposite extends Composite {

	/** テーブルビューアー。 */
	private CommonTableViewer m_tableViewer = null;
	/** テンプレートセット詳細情報一覧 */
	private ArrayList<TemplateSetDetailInfo> detailList = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** マネージャ名 */
	private String m_managerName = null;

	/**
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}
	
	/**
	 * 
	 * @param managerName
	 */
	public void setManagerName(String managerName) {
		m_managerName = managerName;
		detailList = new ArrayList<TemplateSetDetailInfo>();
		update();
	}

	/**
	 *
	 * @return
	 */
	public ArrayList<TemplateSetDetailInfo> getDetailList(){
		return this.detailList;
	}
	/**
	 *
	 * @return
	 */
	public String getOwnerRoleId() {
		return m_ownerRoleId;
	}
	/**
	 *
	 * @param ownerRoleId
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param managerName マネージャ名
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public TemplateSetDetailListComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		this.initialize();
	}

	/**
	 * テーブル選択項目のの優先度を上げる
	 */
	public void up() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();//.firstElement;
		ArrayList<?> list =  (ArrayList<?>) selection.getFirstElement();
		//選択したテーブル行番号を取得
		Integer order = (Integer) list.get(0);
		List<TemplateSetDetailInfo> detailList = this.detailList;

		//orderは、テーブルカラム番号のため、1 ～ n listから値を取得する際は、 order - 1
		order = order-1;
		if(order > 0){
			TemplateSetDetailInfo a = detailList.get(order);
			TemplateSetDetailInfo b = detailList.get(order-1);
			detailList.set(order, b);
			detailList.set(order-1, a);
		}
		update();
		//更新後に再度選択項目にフォーカスをあてる
		selectItem(order - 1);
	}
	/**
	 * テーブル選択項目の優先度を下げる
	 */
	public void down() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();//.firstElement;
		ArrayList<?> list =  (ArrayList<?>) selection.getFirstElement();
		//選択したテーブル行番号を取得
		Integer order = (Integer) list.get(0);
		List<TemplateSetDetailInfo> detailList = this.detailList;
		//list内 order+1 の値を取得するため、
		if(order < detailList.size()){
			//orderは、テーブルカラム番号のため、1 ～ n listから値を取得する際は、 order - 1
			order = order - 1;
			TemplateSetDetailInfo a = detailList.get(order);
			TemplateSetDetailInfo b = detailList.get(order + 1);
			detailList.set(order, b);
			detailList.set(order+1, a);
		}
		update();
		//更新後に再度選択項目にフォーカスをあてる
		selectItem(order + 1);
	}
	/**
	 * 引数で指定された判定情報の行を選択状態にします。
	 *
	 * @param identifier 識別キー
	 */
	private void selectItem(Integer order) {
		Table templateSetDetailListSelectItemTable = m_tableViewer.getTable();
		TableItem[] items = templateSetDetailListSelectItemTable.getItems();

		if (items == null || order == null) {
			return;
		}
		templateSetDetailListSelectItemTable.select(order);
		return;
	}
	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		/*
		 * テンプレートセット詳細初期化
		 */
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table templateSetDetialListTable = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, templateSetDetialListTable);
		templateSetDetialListTable.setHeaderVisible(true);
		templateSetDetialListTable.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		templateSetDetialListTable.setLayoutData(gridData);

		// テーブルビューアの作成
		m_tableViewer = new CommonTableViewer(templateSetDetialListTable);
		m_tableViewer.createTableColumn(GetTemplateSetDetailTableDefine.get(),
				GetTemplateSetDetailTableDefine.SORT_COLUMN_INDEX,
				GetTemplateSetDetailTableDefine.SORT_ORDER);
		m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Integer order = getSelection();
				List<TemplateSetDetailInfo> detailList = getDetailList();
				if (order != null) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					TemplateSetDetailDialog dialog = new TemplateSetDetailDialog(shell, m_managerName, detailList.get(order - 1), m_ownerRoleId);
					if (dialog.open() == IDialogConstants.OK_ID) {
						detailList.remove(order - 1);
						detailList.add(order - 1,dialog.getInputData());
						setSelection();
					}
				}
			}
		});
	}
	/**
	 * 選択したテーブル行番号を返す。
	 *
	 */
	public Integer getSelection() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();
		if (selection.getFirstElement() instanceof ArrayList) {
			ArrayList<?> list = (ArrayList<?>)selection.getFirstElement();
			if (list.get(0) instanceof Integer) {
				return (Integer)list.get(0);
			}
		}
		return null;
	}

	public void setSelection() {
		Table templateSetDetailListSetSelectionTable = m_tableViewer.getTable();
		WidgetTestUtil.setTestId(this, null, templateSetDetailListSetSelectionTable);
		int selectIndex = templateSetDetailListSetSelectionTable.getSelectionIndex();
		update();
		templateSetDetailListSetSelectionTable.setSelection(selectIndex);
	}
	/**
	 * 現在選択されているアイテムを返します。
	 * <p>
	 * 選択されていない場合は、<code>null</code>を返します。
	 *
	 * @return 選択アイテム
	 */
	public TemplateSetDetailInfo getFilterItem() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();

		if (selection == null) {
			return null;
		} else {
			return (TemplateSetDetailInfo) selection.getFirstElement();
		}
	}

	/**
	 * 引数で指定されたテンプレートセット詳細情報をコンポジット内リストに反映させる
	 * @param detailList
	 */
	public void setDetailList(ArrayList<TemplateSetDetailInfo> detailList){
		if (detailList != null) {
			this.detailList = detailList;
			this.update();
		}
	}
	/**
	 * コンポジットを更新します。<BR>
	 * テンプレートセット詳細情報一覧を取得し、テーブルビューアーにセットします。
	 */
	@Override
	public void update() {
		// テーブル更新
		ArrayList<Object> listAll = new ArrayList<Object>();
		int i = 1;
		for (TemplateSetDetailInfo detail : getDetailList()) {
			ArrayList<Object> list = new ArrayList<Object>();
			
			//順序
			list.add(i);
			//テンプレートID）
			list.add(detail.getTemplateId());
			//説明
			list.add(HinemosMessage.replace(detail.getDescription()));
			listAll.add(list);
			i++;
		}
		m_tableViewer.setInput(listAll);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_tableViewer.getTable().setEnabled(enabled);
	}
}
