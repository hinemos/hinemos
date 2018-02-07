/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.composite;


import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.composite.TableItemListComposite;
import com.clustercontrol.monitor.run.viewer.TableItemTableViewer;
import com.clustercontrol.ws.monitor.TrapValueInfo;

public class TrapDefineTableComposite extends TableItemListComposite<TrapValueInfo> {

	public TrapDefineTableComposite(Composite parent, int style, ITableItemCompositeDefine<TrapValueInfo> define) {
		super(parent, style, define);
	}


	/**
	 * コンポジットを配置します。
	 */
	protected void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.m_tableViewer = new TableItemTableViewer(table, this.m_define.getLabelProvider());
		this.m_tableViewer.createTableColumn(this.m_define.getTableDefine());
		this.m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				TrapDefineListComposite parent = (TrapDefineListComposite)getParent();
				parent.modifyEvent();
			}
		});
	}
}
