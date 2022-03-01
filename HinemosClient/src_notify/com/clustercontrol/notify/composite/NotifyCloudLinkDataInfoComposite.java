/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.notify.action.GetNotifyTableDefineNoCheckBox;
import com.clustercontrol.notify.composite.action.CloudLinkInfoDataSelectionChangedListener;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * クラウド通知連携情報の値追加用コンポジットクラス<BR>
 */
public class NotifyCloudLinkDataInfoComposite extends Composite {
	
	/** テーブルビューアー。 */
	private CommonTableViewer tableViewer = null;


	/** 合計ラベル */
	private Label totalLabel = null;


	private Map<String, String> dataListMap;

	private List<?> selectItem=new ArrayList<Object>();
	
	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public NotifyCloudLinkDataInfoComposite(Composite parent, int style) {
		super(parent, style);
		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 *
	 * @see com.clustercontrol.notify.action.GetNotifyTableDefineCheckBox#get()
	 * @see #update()
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		final Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();
		tableDefine.add(0,
				new TableColumnInfo(Messages.getString("name", locale), TableColumnInfo.NONE, 170, SWT.LEFT));
		tableDefine.add(1,
				new TableColumnInfo(Messages.getString("value", locale), TableColumnInfo.NONE, 180, SWT.LEFT));


		this.tableViewer.createTableColumn(tableDefine, GetNotifyTableDefineNoCheckBox.SORT_COLUMN_INDEX1,
				GetNotifyTableDefineNoCheckBox.SORT_COLUMN_INDEX2, GetNotifyTableDefineNoCheckBox.SORT_ORDER);

		// 合計ラベルの作成
		this.totalLabel = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLabel.setLayoutData(gridData);
		
		this.tableViewer.addSelectionChangedListener(
				new CloudLinkInfoDataSelectionChangedListener(this));

	}
	
	/**
	 * このコンポジットが利用するテーブルビューアーを返します。
	 *
	 * @return テーブルビューアー
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
	 * コンポジットを更新します。<BR>
	 * 通知一覧情報を取得し、テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.notify.action.GetNotify#getNotifyList()
	 */
	@Override
	public void update() {
		//map list変換
		if(dataListMap==null){
			return;
		}
		
		ArrayList<ArrayList<Object>> listInput = new ArrayList<ArrayList<Object>>();
		
		for(Entry<String, String> entrySet: dataListMap.entrySet()){
			ArrayList<Object>tmp = new ArrayList<Object>();
			tmp.add(entrySet.getKey());
			tmp.add(entrySet.getValue());
			
			listInput.add(tmp);
		}
		
		this.tableViewer.setInput(listInput);
		
	}


	/**
	 * 連携情報のディテール/データをセットします
	 * @param dataList
	 */
	public void setInfoLinkDataList(Map<String,String>dataList){
		this.dataListMap = new ConcurrentHashMap<>(dataList);
	}


	public void setSelectItem(List<?> selectedInfo) {
		selectItem= selectedInfo;
		
	}
	
	public List<?> getSelectedItem(){
		return selectItem;
	}

}
