package com.clustercontrol.snmptrap.composite;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.dialog.CommonDialog;
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
				TrapValueInfo item = getTableItem();
				if (item != null) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					CommonDialog dialog = m_define.createDialog(shell, item);
					if (dialog.open() == IDialogConstants.OK_ID) {
						Table table = getTableViewer().getTable();
						WidgetTestUtil.setTestId(this, null, table);
						int selectIndex = table.getSelectionIndex();
						m_define.getTableItemInfoManager().modify(item, m_define.getCurrentCreatedItem());
						table.setSelection(selectIndex);
						update();
					}
				}
			}
		});
	}
}
