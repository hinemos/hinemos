/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.view;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.repository.composite.NodeAttributeComposite;
import com.clustercontrol.repository.composite.NodeListComposite;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ノード属性ビュークラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class NodeAttributeView extends CommonViewPart {
	public static final String ID = NodeAttributeView.class.getName();

	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeAttributeView.class);

	// ----- instance フィールド ----- //

	/** ノード属性一覧コンポジット */
	private NodeAttributeComposite composite = null;

	private String orgViewName = null;
	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 */
	public NodeAttributeView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	// ----- instance メソッド ----- //

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		this.composite = new NodeAttributeComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, composite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.composite.setLayoutData(gridData);

		orgViewName = this.getPartName();
		this.update();
	}

	/**
	 * compositeを返します。
	 *
	 * @return composite
	 */
	public NodeAttributeComposite getComposite() {
		return this.composite;
	}

	/**
	 * ノード一覧にて選択されているノードの内容を表示します。
	 */
	public void update() {
		IWorkbench workbench = ClusterControlPlugin.getDefault().getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();

		NodeListView view = null;
		if (page != null) {
			IViewPart viewPart = page.findView(NodeListView.ID);
			if (viewPart != null){
				view = (NodeListView)viewPart.getAdapter(NodeListView.class);
			}
		}

		String managerName = null;
		String facilityId = null;
		if (view != null) {
			NodeListComposite composite = view.getComposite();
			CommonTableViewer viewer = composite.getTableViewer();
			StructuredSelection selection = (StructuredSelection) viewer
					.getSelection();

			ArrayList<?> list = (ArrayList<?>) selection.getFirstElement();
			if (list != null) {
				facilityId = (String) list.get(viewer
						.getTableColumnIndex(TableColumnInfo.FACILITY_ID));
				managerName = (String) list.get(viewer
						.getTableColumnIndex(TableColumnInfo.MANAGER_NAME));
				if(managerName != null && !managerName.isEmpty()) {
					String viewName = orgViewName + "(" + managerName + ")";
					setPartName(viewName);
				}
			}
		} else {
			m_log.info("update: view is null");
		}
		this.composite.update(managerName, facilityId);
	}
}
