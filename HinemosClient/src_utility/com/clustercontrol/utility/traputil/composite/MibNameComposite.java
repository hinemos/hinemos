/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.composite;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.traputil.action.MibManager;

/**
 * MIB名リスト表示コンポジット
 * 
 * @version 6.1.0
 * @since 2.4.0
 *
 */
public class MibNameComposite extends Composite{

	private ListViewer mibList = null;

	private Label label = null;

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
	public MibNameComposite(Composite parent, int style) {
			super(parent, style);
			createContents(parent);
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

		
		label = new Label(this, SWT.NONE);
		label.setText(Messages.getString("miblist"));
		
		mibList = new ListViewer(this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI |SWT.BORDER);
		GridData gdata = new GridData(GridData.FILL_BOTH);
		mibList.getList().setLayoutData(gdata);

		//MIB名リストのラベルプロバイダー
		mibList.setLabelProvider(new MibNameListLabelProvider());
		
		//リストのコンテンツプロバイダーは使う必要が無さそうなため未実装
		
		//MIB名リストのソーター
		//MIB名でソートするだけなのでViewerSorterをそのまま利用
		mibList.setSorter(new ViewerSorter());
	}

	@Override
	public void update() {
		this.clear();
		try {
			mibList.add(MibManager.getInstance(isSync).getMibMasters().toArray());
		} catch (InvocationTargetException e) {
			MessageDialog.openError(
					null,
					com.clustercontrol.util.Messages.getString("failed"),
					com.clustercontrol.util.Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getCause().getMessage());
		}
	}
	
	public void clear() {
		mibList.refresh();
		
	}

	public ListViewer getMibList() {
		return mibList;
	}

	public void setMibList(ListViewer mibList) {
		this.mibList = mibList;
	}

	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}


}
