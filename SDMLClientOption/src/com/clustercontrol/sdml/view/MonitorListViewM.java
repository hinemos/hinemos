/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.view;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;

import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.bean.MonitorFilterConstant;
import com.clustercontrol.monitor.dialog.MonitorFilterDialog;
import com.clustercontrol.monitor.run.action.GetMonitorListTableDefine;
import com.clustercontrol.monitor.util.MonitorFilterPropertyUtil;
import com.clustercontrol.monitor.view.MonitorListView;
import com.clustercontrol.monitor.view.action.MonitorFilterAction;
import com.clustercontrol.util.PropertyUtil;

/**
 * 初期表示時に自動作成された監視設定のフィルタを実現するために同じビューを作る
 *
 */
public class MonitorListViewM extends MonitorListView {
	private static Log logger = LogFactory.getLog(MonitorListViewM.class);
	/** ビューID */
	public static final String ID = MonitorListViewM.class.getName();
	/** 初期表示フィルタ用文字列 */
	private static final String DEFAULT_FILTER_MONITOR_TYPE_ID = "MON_SDML_%";

	@Override
	public void createPartControl(Composite parent) {
		// 親呼び出し前にフィルタ条件を設定
		Property property = MonitorFilterPropertyUtil.getProperty(Locale.getDefault());
		ArrayList<Property> monTypeProperty = PropertyUtil.getProperty(property, MonitorFilterConstant.MONITOR_TYPE_ID);
		if (monTypeProperty != null && monTypeProperty.size() == 1) {
			monTypeProperty.get(0).setValue(DEFAULT_FILTER_MONITOR_TYPE_ID);
		}
		// SDMLでは必ず監視・収集の両方が有効とは限らないので条件から除外する
		ArrayList<Property> monFlgProperty = PropertyUtil.getProperty(property, MonitorFilterConstant.MONITOR_FLG);
		if (monFlgProperty != null && monFlgProperty.size() == 1) {
			monFlgProperty.get(0).setValue("");
		}
		ArrayList<Property> colFlgProperty = PropertyUtil.getProperty(property, MonitorFilterConstant.COLLECTOR_FLG);
		if (colFlgProperty != null && colFlgProperty.size() == 1) {
			colFlgProperty.get(0).setValue("");
		}
		this.condition = property;
		// ボタンのトグルを設定
		try {
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);
			Command command = commandService.getCommand(MonitorFilterAction.ID);
			command.getState(RegistryToggleState.STATE_ID).setValue(true);
		} catch (Exception e) {
			logger.warn("createPartControl() : " + e.getMessage(), e);
		}
		// ダイアログのキャッシュに登録
		MonitorFilterDialog.setProperty(PropertyUtil.copy(property));

		// 親呼び出し
		super.createPartControl(parent);
	}

	@Override
	protected void addSelectionChangedListener() {
		this.composite.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(MonitorListViewM.ID);

				// 選択アイテムを取得
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				if (viewPart != null && selection != null) {
					String selectMonitorTypeId = null;
					for (Object obj : selection.toList()) {
						ArrayList<?> item = (ArrayList<?>) obj;
						selectMonitorTypeId = (String) item.get(GetMonitorListTableDefine.MONITOR_TYPE_ID);
					}
					MonitorListViewM view = (MonitorListViewM) viewPart.getAdapter(MonitorListViewM.class);

					if (view == null) {
						logger.info("selectionChanged() : view is null");
						return;
					}

					// ビューのボタン（アクション）の使用可/不可を設定する
					view.setEnabledAction(selection.size(), selectMonitorTypeId, event.getSelection());
				}
			}
		});
	}
}
