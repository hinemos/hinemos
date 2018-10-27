/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.composite;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.utility.traputil.action.MibManager;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMasterInfo;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMibMasterData;


/**
 * MIB情報表示コンポジット
 * 
 * @version 6.1.0
 * @since 2.4.0
 *
 */
public class MibListComposite extends Composite{

	private SashForm mibViewSash = null;
	private MibNameComposite mibNameComposite = null;
	private MibDetailComposite mibDetailComposite = null;
	private ListViewer mibList = null;
	private TableViewer mibDetailTable = null;
	private boolean isSync;
	
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
	public MibListComposite(Composite parent, int style) {
			super(parent, style);
			createContents(parent);
			
			//MIB情報マネージャ
			MibManager.getInstance(false).initialize();
			update();
	}
	
	/**
 	* コンポジットを配置します。
 	*/
	public void createContents(Composite parent){
		
		// レイアウト設定
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);
		
		// サッシュフォーム作成及び設定
		mibViewSash = new SashForm(this, SWT.HORIZONTAL);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		mibViewSash.setLayoutData(gridData);
		
		mibNameComposite = new MibNameComposite(mibViewSash, SWT.NONE);
		mibDetailComposite = new MibDetailComposite(mibViewSash, SWT.NONE);
		
		//マネージャ同期モードの設定
		mibNameComposite.setSync(this.isSync);
		mibDetailComposite.setSync(this.isSync);

		
		// Sashの境界を調整 左部30% 右部70%
		mibViewSash.setWeights(new int[] { 30, 70 });
		
		
		mibList = mibNameComposite.getMibList();
		mibDetailTable = mibDetailComposite.getMibDetailTable();
		
		// MIBリスト選択時の動作
		mibList.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				doSelectMibList(selection.toList());
			}
		});
		
		// MIB詳細テーブルダブルクリック時の動作はView側で個別のリスナーを実装すること
		// (マスター編集とインポートで動作が異なるため)
		

	}
	
	
	@Override
	public void update() {
		super.update();
		mibNameComposite.update();
		mibDetailComposite.update();
	}
	
	private void doSelectMibList(List<SnmpTrapMibMasterData> list) {
		
		this.mibDetailComposite.clearSelectedMibMaster();
		
		for(int i = 0; i < list.size(); i++) {
			this.mibDetailComposite.addSelectedMibMaster((SnmpTrapMibMasterData)list.get(i));
			
		}
		
		mibDetailComposite.update();
		
	}
	
	/**
	 * 選択したMIB詳細情報のリストを返します
	 * 
	 * @return
	 */
	public ArrayList<SnmpTrapMasterInfo> getSelectDetails() {
		return this.mibDetailComposite.getSelectDetails();
	}
	
	public MibDetailComposite getMibDetailComposite() {
		return mibDetailComposite;
	}

	public MibNameComposite getMibNameComposite() {
		return mibNameComposite;
	}

	public void setSyncMode(boolean isSync) {
		this.isSync = isSync;
		this.mibNameComposite.setSync(isSync);
		this.mibDetailComposite.setSync(isSync);
	}

	public TableViewer getMibDetailTable() {
		return mibDetailTable;
	}

	public ListViewer getMibList() {
		return mibList;
	}
}
