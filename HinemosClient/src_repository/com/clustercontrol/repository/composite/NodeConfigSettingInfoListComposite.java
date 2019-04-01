/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.bean.HistoryFilterPropertyConstant;
import com.clustercontrol.repository.action.GetNodeConfigSettingListTableDefine;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.repository.action.GetNodeConfigSettingList;
import com.clustercontrol.repository.composite.action.NodeConfigSettingDoubleClickListener;
import com.clustercontrol.repository.view.NodeConfigSettingListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.repository.NodeConfigSettingInfo;
import com.clustercontrol.ws.repository.NodeConfigSettingItemInfo;

/**
 * 構成情報取得一覧コンポジットクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigSettingInfoListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( NodeConfigSettingInfoListComposite.class );
	
	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	/** 表示内容ラベル */
	private Label statuslabel = null;

	/** 合計ラベル */
	private Label totalLabel = null;

	/** 検索条件 */
	private Property condition = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param style
	 *            スタイル
	 */
	public NodeConfigSettingInfoListComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	// ----- instance メソッド ----- //

	/**
	 * コンポジットを生成・構築します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.statuslabel = new Label(this, SWT.LEFT);
		this.statuslabel.setText("");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.statuslabel.setLayoutData(gridData);

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetNodeConfigSettingListTableDefine.get(),
				GetNodeConfigSettingListTableDefine.SORT_COLUMN_INDEX1,
				GetNodeConfigSettingListTableDefine.SORT_COLUMN_INDEX2,
				GetNodeConfigSettingListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		this.tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(NodeConfigSettingListView.ID);

				//選択アイテムを取得
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				if ( viewPart != null && selection != null) {
					NodeConfigSettingListView view = (NodeConfigSettingListView) viewPart.getAdapter(NodeConfigSettingListView.class);

					if (view == null) {
						return;
					}

					//ビューのボタン（アクション）の使用可/不可を設定する
					view.setEnabledAction(selection.size(), event.getSelection());
				}
			}
		});
		// ダブルクリックリスナの追加
		this.tableViewer.addDoubleClickListener(new NodeConfigSettingDoubleClickListener(this));

		this.totalLabel = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);

	}

	/**
	 * tableViewerを返します。
	 *
	 * @return tableViewer
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}

	/**
	 * コンポジットを更新します。
	 * <p>
	 *
	 * 検索条件が事前に設定されている場合、その条件にヒットする構成情報収集設定一覧を 表示します <br>
	 * 検索条件が設定されていない場合は、全構成情報収集設定を表示します。
	 */
	@Override
	public void update() {
		// データ取得
		Map<String, List<NodeConfigSettingInfo>> dispDataMap = null;
		ArrayList<Object> listInput = new ArrayList<Object>();

		if (this.condition == null) {
			this.statuslabel.setText("");
			dispDataMap = new GetNodeConfigSettingList().getAll();
		} else {
			this.statuslabel.setText(Messages.getString("filtered.list"));

			String conditionManager = null;
			ArrayList<?> values = PropertyUtil.getPropertyValue(this.condition, HistoryFilterPropertyConstant.MANAGER);
			if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
				conditionManager = (String)values.get(0);
			}

			if(conditionManager == null || conditionManager.equals("")) {
				dispDataMap = new GetNodeConfigSettingList().getAll();
			} else {
				List<NodeConfigSettingInfo> list = new GetNodeConfigSettingList().getAll(conditionManager);
				dispDataMap = new ConcurrentHashMap<String, List<NodeConfigSettingInfo>>();
				dispDataMap.put(conditionManager, list);
			}
		}

		int cnt = 0;
		for(Map.Entry<String, List<NodeConfigSettingInfo>> entrySet : dispDataMap.entrySet()) {
			List<NodeConfigSettingInfo> list = entrySet.getValue();

			if(list == null){
				list = new ArrayList<NodeConfigSettingInfo>();
			}

			for (NodeConfigSettingInfo config : list) {

				//有効になっている収集項目の文字列を生成
				String targets = "";
				boolean isStart = true;
				for (NodeConfigSettingItemInfo info : config.getNodeConfigSettingItemList()) {
					if (!isStart) {
						targets += ", ";
					} else {
						isStart = false;
					}

					String str = info.getSettingItemId().toString();
					
					if (str.equals(NodeConfigSettingItem.HW_NIC.name())) {
						targets += NodeConfigSettingItem.HW_NIC.displayName();
					} else if (str.equals(NodeConfigSettingItem.OS.name())) {
						targets += NodeConfigSettingItem.OS.displayName();
					} else if (str.equals(NodeConfigSettingItem.PACKAGE.name())) {
						targets += NodeConfigSettingItem.PACKAGE.displayName();
					} else if (str.equals(NodeConfigSettingItem.PROCESS.name())) {
						targets += NodeConfigSettingItem.PROCESS.displayName();
					} else if (str.equals(NodeConfigSettingItem.HW_CPU.name())) {
						targets += NodeConfigSettingItem.HW_CPU.displayName();
					} else if (str.equals(NodeConfigSettingItem.HW_DISK.name())) {
						targets += NodeConfigSettingItem.HW_DISK.displayName();
					} else if (str.equals(NodeConfigSettingItem.HW_FILESYSTEM.name())) {
						targets += NodeConfigSettingItem.HW_FILESYSTEM.displayName();
					} else if (str.equals(NodeConfigSettingItem.HOSTNAME.name())) {
						targets += NodeConfigSettingItem.HOSTNAME.displayName();
					} else if (str.equals(NodeConfigSettingItem.HW_MEMORY.name())) {
						targets += NodeConfigSettingItem.HW_MEMORY.displayName();
					} else if (str.equals(NodeConfigSettingItem.NETSTAT.name())) {
						targets += NodeConfigSettingItem.NETSTAT.displayName();
					} else if (str.equals(NodeConfigSettingItem.CUSTOM.name())) {
						targets += NodeConfigSettingItem.CUSTOM.displayName();
					} else {
						m_log.warn("update(): invalid settingItemId. settingItemId: " + str);
					}
				}
				
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(entrySet.getKey());
				a.add(config.getSettingId());
				a.add(config.getSettingName());
				a.add(config.getDescription());
				a.add(targets);
				a.add(config.getRunInterval());
				a.add(config.getFacilityId());
				a.add(HinemosMessage.replace(config.getScope()));
				a.add(config.getCalendarId());
				a.add(config.isValidFlg());
				a.add(config.getOwnerRoleId());
				a.add(config.getRegUser());
				if (config.getRegDate() == null) {
					a.add(null);
				} else {
					a.add(new Date(config.getRegDate()));
				}
				a.add(config.getUpdateUser());
				if (config.getUpdateDate() == null) {
					a.add(null);
				} else {
					a.add(new Date(config.getUpdateDate()));
				}
				a.add(null);
				listInput.add(a);
				cnt++;
			}
		}
		// テーブル更新
		this.tableViewer.setInput(listInput);

		// 合計欄更新
		String[] args = { Integer.toString(cnt) };
		String message = null;
		if (this.condition == null) {
			message = Messages.getString("records", args);
		} else {
			message = Messages.getString("filtered.records", args);
		}
		this.totalLabel.setText(message);
	}

	/**
	 * 検索条件にヒットした構成情報収集設定一覧を表示します。
	 * <p>
	 *
	 * conditionがnullの場合、全構成情報収集設定を表示します。
	 *
	 * @param condition
	 *            検索条件
	 */
	public void update(Property condition) {
		this.condition = condition;

		this.update();
	}
}
