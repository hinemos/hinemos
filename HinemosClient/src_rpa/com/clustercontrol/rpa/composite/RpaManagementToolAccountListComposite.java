/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.openapitools.client.model.RpaManagementToolAccountResponse;
import org.openapitools.client.model.RpaManagementToolResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rpa.composite.action.RpaManagementToolAccountDoubleClickListener;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TableViewerSorter;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * RPA管理ツールアカウント一覧コンポジットクラス
 *
 */
public class RpaManagementToolAccountListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( RpaManagementToolAccountListComposite.class );
	/** テーブルビューア */
	private TableViewer tableViewer = null;
	/** テーブル */
	private Table accountListTable = null;
	/** ラベル */
	private Label m_labelCount = null;

	/**
	 * このコンポジットが利用するテーブルビューアを取得します。
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return tableViewer;
	}
	/**
	 * このコンポジットが利用するテーブルを取得します。
	 * @return テーブル
	 */
	public Table getTable() {
		return tableViewer.getTable();
	}
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 */
	public RpaManagementToolAccountListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * 初期化処理
	 *
	 */
	private void initialize() {
		
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//RPA管理アカウント一覧テーブル作成
		Composite composite_1 = new Composite(this, SWT.NONE);
		TableColumnLayout tcl_composite_1 = new TableColumnLayout();
		composite_1.setLayout(tcl_composite_1);
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		composite_1.setLayoutData(gridData);

		m_labelCount = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "count", m_labelCount);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		tableViewer = new TableViewer(composite_1, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		accountListTable = tableViewer.getTable();
		accountListTable.setHeaderVisible(true);
		accountListTable.setLinesVisible(true);
		
		for(final ViewColumn column: ViewColumn.values()){
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tableColumn = tableViewerColumn.getColumn();
			tcl_composite_1.setColumnData(tableColumn, column.getPixelData());
			tableColumn.setText(column.getLabel());
			tableViewerColumn.setLabelProvider(column.getProvider());
			tableColumn.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					tableViewer.setSorter(new TableViewerSorter(tableViewer, column.getProvider()));
				}
			});
		}


		tableViewer.setContentProvider(new ArrayContentProvider());
		// Sorting by Manager > Scope name
		tableViewer.setComparator(new ViewerComparator(){
			// Set sorting key by element type
			private String getSortingKey(Object element){
				if(element instanceof RpaManagementToolAccountViewColumn){
					RpaManagementToolAccountViewColumn castedElem = (RpaManagementToolAccountViewColumn)element;
					return castedElem.getManagerName()
							+ castedElem.getRpaManagementToolAccount().getRpaScopeName();
				} else {
					return "";
				}
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return getSortingKey(e1).compareTo(getSortingKey(e2));
			}
		});
		
		tableViewer.addDoubleClickListener(new RpaManagementToolAccountDoubleClickListener(this));
		
		for (int i = 0; i < accountListTable.getColumnCount(); i++){
			accountListTable.getColumn(i).setMoveable(true);
		}
		
	}

	/**
	 * 更新処理
	 */
	@Override
	public void update() {
		//RPA管理ツールアカウント一覧情報取得
		Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
		List<RpaManagementToolAccountViewColumn> listInput = new ArrayList<RpaManagementToolAccountViewColumn>();
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			List<RpaManagementToolAccountResponse> list = new ArrayList<>();
			List<RpaManagementToolResponse> toolMst = new ArrayList<>();
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
			try {
				list = wrapper.getRpaManagementToolAccountList();
				toolMst = wrapper.getRpaManagementTool();				
			} catch (InvalidRole e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				// 上記以外の例外
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("update(), " + errMessage, e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
			}
			if(list == null){
				list = new ArrayList<RpaManagementToolAccountResponse>();
			}
			
			for (RpaManagementToolAccountResponse data : list) {
				// マネージャ名、管理ツール名をセット
				RpaManagementToolAccountViewColumn inputData = new RpaManagementToolAccountViewColumn(data);
				inputData.setManagerName(managerName);
				inputData.setRpaManagementToolName(toolMst.stream()
						.filter(tool -> tool.getRpaManagementToolId().equals(inputData.getRpaManagementToolAccount().getRpaManagementToolId()))
						.findAny()
						.map(RpaManagementToolResponse::getRpaManagementToolName)
						.orElse(""));
				listInput.add(inputData);
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		tableViewer.setInput(listInput);
		tableViewer.refresh();

		Object[] args = { Integer.valueOf(listInput.size()) };
		m_labelCount.setText(Messages.getString("records", args));
	}

	private enum ViewColumn{
		manager(
			Messages.getString("MANAGER_NAME"),
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((RpaManagementToolAccountViewColumn)element).getManagerName();
				}
			}
		),
		rpa_management_tool(
			Messages.getString("rpa.management.tool"),
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((RpaManagementToolAccountViewColumn)element).getRpaManagementToolName();
				}
			}
		),
		rpa_scope(
			Messages.getString("rpa.scope") + " (" + Messages.getString("RPA_SCOPE_ID") + ")",
			new ColumnPixelData(200, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element){ 
					return ((RpaManagementToolAccountViewColumn)element).getRpaManagementToolAccount().getRpaScopeName() + 
							" (" + ((RpaManagementToolAccountViewColumn)element).getRpaManagementToolAccount().getRpaScopeId() + ")";
				}
			}
		),
		account_name(
			Messages.getString("word.account") + " (" + Messages.getString("word.account.id") + ")",
			new ColumnPixelData(200, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((RpaManagementToolAccountViewColumn)element).getRpaManagementToolAccount().getDisplayName() + 
							" (" + ((RpaManagementToolAccountViewColumn)element).getRpaManagementToolAccount().getAccountId() + ")";
				}
			}
		),
		description(
				Messages.getString("description"),
			new ColumnPixelData(200, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((RpaManagementToolAccountViewColumn)element).getRpaManagementToolAccount().getDescription();
				}
			}
		),
		reg_user(
			Messages.getString("creator.name"),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((RpaManagementToolAccountViewColumn)element).getRpaManagementToolAccount().getRegUser();
				}
			}
		),
		reg_date(
			Messages.getString("create.time"),
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((RpaManagementToolAccountViewColumn)element).getRpaManagementToolAccount().getRegDate();
				}
			}
		),
		update_user(
			Messages.getString("modifier.name"),
			new ColumnPixelData(100, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((RpaManagementToolAccountViewColumn)element).getRpaManagementToolAccount().getUpdateUser();
				}
			}
		),
		update_date(
			Messages.getString("update.time"),
			new ColumnPixelData(150, true, true),
			new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					return ((RpaManagementToolAccountViewColumn)element).getRpaManagementToolAccount().getUpdateDate();
				}
			}
		);

		private String label;
		private ColumnLabelProvider provider;
		private ColumnPixelData pixelData;
		
		ViewColumn(String label, ColumnPixelData pixelData, ColumnLabelProvider provider){
			this.label = label;
			this.pixelData = pixelData;
			this.provider = provider;
		}

		public String getLabel() {
			return label;
		}

		public ColumnPixelData getPixelData() {
			return pixelData;
		}

		public ColumnLabelProvider getProvider() {
			return provider;
		}
	}

	// ViewColumn用のbean
	public static class RpaManagementToolAccountViewColumn {
		private String managerName;
		private String rpaManagementToolName;
		private RpaManagementToolAccountResponse rpaManagementToolAccount;
		
		public RpaManagementToolAccountViewColumn(RpaManagementToolAccountResponse rpaManagementToolAccount) {
			this.setRpaManagementToolAccount(rpaManagementToolAccount);
		}

		public String getManagerName() {
			return managerName;
		}

		public void setManagerName(String managerName) {
			this.managerName = managerName;
		}

		public String getRpaManagementToolName() {
			return rpaManagementToolName;
		}

		public void setRpaManagementToolName(String rpaManagementToolName) {
			this.rpaManagementToolName = rpaManagementToolName;
		}


		public RpaManagementToolAccountResponse getRpaManagementToolAccount() {
			return rpaManagementToolAccount;
		}

		public void setRpaManagementToolAccount(RpaManagementToolAccountResponse rpaManagementToolAccount) {
			this.rpaManagementToolAccount = rpaManagementToolAccount;
		}
	}
	
}
