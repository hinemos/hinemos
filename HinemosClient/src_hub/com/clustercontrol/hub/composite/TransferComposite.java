/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.hub.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.hub.action.GetTransferTableDefine;
import com.clustercontrol.hub.dialog.TransferInfoDialog;
import com.clustercontrol.hub.util.HubEndpointWrapper;
import com.clustercontrol.hub.view.TransferView;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.hub.TransferInfo;
import com.clustercontrol.ws.hub.TransferType;

/**
 * 収集蓄積[転送]コンポジットクラス<BR>
 *
 */
public class TransferComposite extends Composite {
	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** テーブル */
	private Table m_transferListTable = null;
	/** ラベル */
	private Label m_labelCount = null;
	/** ID */
	private String m_transferId = null;

	/**
	 * このコンポジットが利用するテーブルビューアを取得します。<BR>
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return m_viewer;
	}
	/**
	 * このコンポジットが利用するテーブルを取得します。<BR>
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return m_viewer.getTable();
	}
	/**
	 * ID
	 * @return m_transferId
	 */
	public String getTransferId() {
		return m_transferId;
	}

	/**
	 * ID
	 * @param transferId
	 */
	public void setTransferId(String transferId) {
		m_transferId = transferId;
	}
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 */
	public TransferComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * 初期化処理<BR>
	 *
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//収集蓄積[転送]一覧テーブル作成
		m_transferListTable = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, m_transferListTable);
		m_transferListTable.setHeaderVisible(true);
		m_transferListTable.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		m_transferListTable.setLayoutData(gridData);

		m_labelCount = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "count", m_labelCount);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(m_transferListTable);
				
		m_viewer.createTableColumn(GetTransferTableDefine.get(),
				GetTransferTableDefine.SORT_COLUMN_INDEX1,
				GetTransferTableDefine.SORT_COLUMN_INDEX2,
				GetTransferTableDefine.SORT_ORDER);

		for (int i = 0; i < m_transferListTable.getColumnCount(); i++){
			m_transferListTable.getColumn(i).setMoveable(true);
		}
		m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			/**
			 * 選択変更時に呼び出されます。<BR>
			 * 各ビューのテーブルビューアを選択した際に、<BR>
			 * 選択した行の内容でビューのアクションの有効・無効を設定します。
			 * 
			 * @param event 選択変更イベント
			 * 
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				String id = null;
				
				//収集蓄積[転送]ビューのインスタンスを取得
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				
				IViewPart viewPart = page.findView(TransferView.ID);

				//選択アイテムを取得
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				if ( viewPart != null && selection != null) {
					if ( selection.getFirstElement() != null) {
						ArrayList<?> info = (ArrayList<?>) selection.getFirstElement();
						id = (String) info.get(GetTransferTableDefine.TRANSFER_ID);
						//IDを設定
						TransferComposite.this.setTransferId(id);
					}
					TransferView view = (TransferView) viewPart.getAdapter(TransferView.class);
					//ビューのボタン（アクション）の使用可/不可を設定する
					view.setEnabledAction(selection.size(), event.getSelection());
				}
			}
		});
		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			/**
			 * 
			 * @param event
			 */
			@Override
			public void doubleClick(DoubleClickEvent event) {
				String managerName = "";
				String exportId = "";

				if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
					ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event.getSelection()).getFirstElement();

					managerName = (String)info.get(GetTransferTableDefine.MANAGER_NAME);
					exportId = (String) info.get(GetTransferTableDefine.TRANSFER_ID);
				}

				if(exportId != null){
					// ダイアログ名を取得
					TransferInfoDialog dialog = new TransferInfoDialog(
							TransferComposite.this.getShell(),managerName, exportId, PropertyDefineConstant.MODE_MODIFY);
					
					// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
					if (dialog.open() == IDialogConstants.OK_ID) {
						TransferComposite.this.update();
					}
				}
			}
		});
	}

	/**
	 * 更新処理<BR>
	 *
	 */
	@Override
	public void update() {
		List<TransferInfo> list = null;

		//収集蓄積[転送]一覧情報取得
		Map<String, List<TransferInfo>> dispDataMap= new ConcurrentHashMap<String, List<TransferInfo>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for(String managerName : EndpointManager.getActiveManagerSet()) {
			HubEndpointWrapper wrapper = HubEndpointWrapper.getWrapper(managerName);
			try {
				list = wrapper.getTransferInfoList();
			} catch (Exception e) {
				// 上記以外の例外
				Logger.getLogger(this.getClass()).warn("update(), " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
			}
			//一覧が空の場合
			if (list == null) {
				list = Collections.emptyList();
			}
			dispDataMap.put(managerName, list);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			//TODO アクセス権限がないときのエラーハンドリング追加
			UIManager.showMessageBox(errorMsgs, true);
		}

		List<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<TransferInfo>> map: dispDataMap.entrySet()) {
			for (TransferInfo export : map.getValue()) {
				List<Object> obj = new ArrayList<Object>();
				obj.add(map.getKey());
				obj.add(export.getTransferId());
				obj.add(export.getDescription());
				obj.add(Messages.getString(TransferDataTypeConstant.typeToString(export.getDataType())));
				obj.add(export.getDestTypeId());
				obj.add(Messages.getString(TransferTransTypeConstant.typeToString(export.getTransType())));
				String str  = "";
				if (export.getTransType() != TransferType.REALTIME && export.getInterval() != null) {
					str = Messages.getString(TransferTransIntervalConstant.typeToString(export.getInterval()));
				}
				obj.add(str);
				obj.add(export.getCalendarId());
				obj.add(Messages.getString(export.isValidFlg()? "valid":"invalid"));
				obj.add(export.getOwnerRoleId());
				obj.add(export.getRegUser());
				obj.add(new Date(export.getRegDate()));
				obj.add(export.getUpdateUser());
				obj.add(new Date(export.getUpdateDate()));
				obj.add(null);				

				listInput.add(obj);
			}
		}
		m_viewer.setInput(listInput);

		Object[] args = { Integer.valueOf(listInput.size()) };
		m_labelCount.setText(Messages.getString("records", args));
	}
}