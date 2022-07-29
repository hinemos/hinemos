/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.composite;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.traputil.action.GetMasterTableDefine;
import com.clustercontrol.utility.traputil.action.MibManager;
import com.clustercontrol.utility.traputil.bean.MasterTableDefine;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMasterInfo;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMibMasterData;
import com.clustercontrol.viewer.CommonTableViewer;


/**
 * MIB詳細テーブル表示コンポジット
 * 
 * @version 6.1.0
 * @since 2.4.0
 * 
 */
public class MibDetailComposite extends Composite{

	private CommonTableViewer tableViewer = null;
	private Table table = null;
	private boolean isSync;
	private Label label = null;
	
	/** 合計ラベル */
	private Label totalLavel = null;
	
	private ArrayList<SnmpTrapMibMasterData> selectedMibMasterList = null;
	
	/**
 	* インスタンスを返します。
 	* 
 	* @param parent 親のコンポジット
 	* @param style スタイル
 	* 
 	* @see org.eclipse.swt.SWT
 	* @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
 	* @see #initialize()
 	*/
	public MibDetailComposite(Composite parent, int style) {
			super(parent, style);
			createContents(parent);
	}
	
	/**
 	* コンポジットを配置します。
 	* 
 	*/
	public void createContents(Composite parent){
		
		// レイアウト設定
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);

		
		label = new Label(this, SWT.NONE);
		label.setText(Messages.getString("mibdetail"));
		
		// テーブル作成
		table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.minimumHeight = 90;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		
		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetMasterTableDefine.get(),
				MasterTableDefine.SORT_COLUMN_INDEX,
				MasterTableDefine.SORT_ORDER);
		
		// 合計ラベルの作成
		this.totalLavel = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		this.totalLavel.setLayoutData(gridData);
		
		this.update();
	}
	
	/**
	 * 
	 */
	@Override
	public void update() {
		ArrayList<ArrayList<Object>> list = null;
		super.update();
		
		if(this.selectedMibMasterList != null) {
			list = new ArrayList<>();
			for(int i = 0;i < this.selectedMibMasterList.size(); i++) {
				ArrayList<SnmpTrapMasterInfo> detailList = null;
				try {
					detailList = MibManager.getInstance(this.isSync).getMibDetails(this.selectedMibMasterList.get(i));
				} catch (InvocationTargetException e) {
					MessageDialog.openError(
							null,
							com.clustercontrol.util.Messages.getString("failed"),
							com.clustercontrol.util.Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getCause().getMessage());
				}
				list.addAll(collectionToArray(detailList));
			}
		}
		
		this.tableViewer.setInput(list);
		
		
		// 合計欄更新
		if(list != null) {
			String[] args = {String.valueOf(list.size())};
			String message = Messages.getString("records", args);
			this.totalLavel.setText(message);
		}
		else {
			String[] args = {"0"};
			String message = Messages.getString("records", args);
			this.totalLavel.setText(message);
		}
		
	}
	
	/**
	 * マスタ情報をObjectの2次元配列に格納
	 * 
	 * @param detailList
	 * @return
	 */
	public List<ArrayList<Object>> collectionToArray(ArrayList<SnmpTrapMasterInfo> detailList) {
		
		List<ArrayList<Object>> list = new ArrayList<>();
		if(detailList != null){
			Iterator<SnmpTrapMasterInfo> itr = detailList.iterator();
			while(itr.hasNext())
			{
				SnmpTrapMasterInfo detail = (SnmpTrapMasterInfo)itr.next();
	
				ArrayList<Object> info = new ArrayList<>();
				info.add(detail.getMib());
				info.add(detail.getUei());
				info.add(detail.getTrapOid());
				info.add(Integer.valueOf(detail.getGenericId()));
				info.add(Integer.valueOf(detail.getSpecificId()));
				info.add(Integer.valueOf(detail.getPriority()));
				info.add(detail.getLogmsg());
				info.add(detail.getDescr());

				list.add(info);
			}
		}
		return list;
	}
	
	/**
	 * 選択したMIB詳細情報のリストを返します
	 * (完全なDTOを復元します)
	 * 
	 * @return
	 */
	public ArrayList<SnmpTrapMasterInfo> getSelectDetails() {
		
		ArrayList<SnmpTrapMasterInfo> data = new ArrayList<SnmpTrapMasterInfo>();
		SnmpTrapMasterInfo detail = null;
		
		//選択されたアイテムを取得
		StructuredSelection selection =
			(StructuredSelection)tableViewer.getSelection();
		@SuppressWarnings("unchecked")
		List<ArrayList<Object>> list = selection.toList();
		
		if (list != null) {
			for(int index = 0; index < list.size(); index++){
				
				ArrayList<Object> info = (ArrayList<Object>)list.get(index);
				if (info != null && info.size() > 0) {
					detail = new SnmpTrapMasterInfo();
					
					detail.setMib((String)info.get(MasterTableDefine.MIB));
					detail.setUei((String)info.get(MasterTableDefine.TRAP_NAME));
					detail.setTrapOid((String)info.get(MasterTableDefine.TRAP_OID));
					detail.setGenericId((Integer)info.get(MasterTableDefine.GENERIC_ID));
					detail.setSpecificId((Integer)info.get(MasterTableDefine.SPECIFIC_ID));
					detail.setPriority((Integer)info.get(MasterTableDefine.PRIORITY));
					detail.setLogmsg((String)info.get(MasterTableDefine.MESSAGE));
					detail.setDescr((String)info.get(MasterTableDefine.DESCR));
					
					data.add(detail);
				}
			}
		}
		
		return data;
	}

	
	public TableViewer getMibDetailTable() {
		return tableViewer;
	}

	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}

	public void addSelectedMibMaster(SnmpTrapMibMasterData master) {
		if(this.selectedMibMasterList == null) {
			this.selectedMibMasterList = new ArrayList<SnmpTrapMibMasterData>();
		}
		this.selectedMibMasterList.add(master);
	}
	
	public void clearSelectedMibMaster() {
		this.selectedMibMasterList = null;
	}

}
