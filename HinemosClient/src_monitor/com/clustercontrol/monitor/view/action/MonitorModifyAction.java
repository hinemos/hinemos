/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.MonitorNumericValueInfoResponse;

import com.clustercontrol.analytics.dialog.CorrelationCreateDialog;
import com.clustercontrol.analytics.dialog.IntegrationCreateDialog;
import com.clustercontrol.analytics.dialog.LogcountCreateDialog;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.binary.dialog.MonitorBinaryDialog;
import com.clustercontrol.binary.dialog.MonitorPacketCaptureDialog;
import com.clustercontrol.custom.dialog.MonitorCustomDialog;
import com.clustercontrol.custom.dialog.MonitorStringCustomDialog;
import com.clustercontrol.customtrap.dialog.MonitorCustomTrapDialog;
import com.clustercontrol.customtrap.dialog.MonitorCustomTrapStringDialog;
import com.clustercontrol.hinemosagent.dialog.AgentCreateDialog;
import com.clustercontrol.http.dialog.HttpNumericCreateDialog;
import com.clustercontrol.http.dialog.HttpScenarioCreateDialog;
import com.clustercontrol.http.dialog.HttpStringCreateDialog;
import com.clustercontrol.jmx.dialog.JmxCreateDialog;
import com.clustercontrol.logfile.dialog.LogfileStringCreateDialog;
import com.clustercontrol.monitor.composite.MonitorListComposite;
import com.clustercontrol.monitor.plugin.IMonitorPlugin;
import com.clustercontrol.monitor.plugin.LoadMonitorPlugin;
import com.clustercontrol.monitor.run.action.GetMonitorListTableDefine;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.monitor.view.MonitorListView;
import com.clustercontrol.performance.monitor.dialog.PerformanceCreateDialog;
import com.clustercontrol.ping.dialog.PingCreateDialog;
import com.clustercontrol.port.dialog.PortCreateDialog;
import com.clustercontrol.process.dialog.ProcessCreateDialog;
import com.clustercontrol.sdml.util.SdmlClientUtil;
import com.clustercontrol.rpa.monitor.dialog.RpaLogfileStringCreateDialog;
import com.clustercontrol.rpa.monitor.dialog.RpaManagementToolServiceCreateDialog;
import com.clustercontrol.snmp.dialog.SnmpNumericCreateDialog;
import com.clustercontrol.snmp.dialog.SnmpStringCreateDialog;
import com.clustercontrol.snmptrap.dialog.SnmpTrapCreateDialog;
import com.clustercontrol.sql.dialog.SqlNumericCreateDialog;
import com.clustercontrol.sql.dialog.SqlStringCreateDialog;
import com.clustercontrol.systemlog.dialog.SystemlogStringCreateDialog;
import com.clustercontrol.winevent.dialog.WinEventDialog;
import com.clustercontrol.winservice.dialog.WinServiceCreateDialog;

/**
 * 監視[一覧]ビューの編集アクションクラス<BR>
 *
 * @version 6.1.0 バイナリ監視の追加
 * @since 4.0.0
 */
public class MonitorModifyAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( MonitorModifyAction.class );

	/** アクションID */
	public static final String ID = MonitorModifyAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;
	
	/** グラフから閾値を変更した場合の情報 */
	private List<MonitorNumericValueInfoResponse> m_MonitorNumericValueInfoList = null;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	public int dialogOpen(Shell shell, String managerName, String pluginId, String monitorId) {
		CommonMonitorDialog dialog = null;
		boolean updateFlg = true;
		if (monitorId == null) {
			updateFlg = false;
		}
		if (SdmlClientUtil.isSdmlPluginId(managerName, pluginId)) {
			// SDMLのプラグインIDをテーブルに表示していた場合は本来のプラグインIDに置き換える
			pluginId = SdmlClientUtil.getActualPluginId(managerName, monitorId);
		}
		if (pluginId.equals(HinemosModuleConstant.MONITOR_AGENT)) {
			dialog = new AgentCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_N)) {
				dialog = new HttpNumericCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_S)) {
				dialog = new HttpStringCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_SCENARIO)) {
			dialog = new HttpScenarioCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PERFORMANCE)) {
			dialog = new PerformanceCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PING)) {
			dialog = new PingCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PORT)) {
			dialog = new PortCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PROCESS)) {
			dialog = new ProcessCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMP_N)) {
				dialog = new SnmpNumericCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMP_S)) {
				dialog = new SnmpStringCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SQL_N)) {
				dialog = new SqlNumericCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if(pluginId.equals(HinemosModuleConstant.MONITOR_SQL_S)) {
				dialog = new SqlStringCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)) {
			dialog = new SystemlogStringCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
			dialog = new LogfileStringCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)) {
			dialog = new MonitorBinaryDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PCAP_BIN)) {
			dialog = new MonitorPacketCaptureDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOM_N)) {
			dialog = new MonitorCustomDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
			dialog = new MonitorStringCustomDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)) {
			dialog = new SnmpTrapCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_WINSERVICE)) {
			dialog = new WinServiceCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
			dialog = new WinEventDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_JMX)) {
			dialog = new JmxCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)) {
			dialog = new MonitorCustomTrapDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
			dialog = new MonitorCustomTrapStringDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_LOGCOUNT)) {
			dialog = new LogcountCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CORRELATION)) {
			dialog = new CorrelationCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_INTEGRATION)) {
			dialog = new IntegrationCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_RPA_LOGFILE)) {
			dialog = new RpaLogfileStringCreateDialog(shell, managerName, monitorId, updateFlg);
		} else if (pluginId.equals(HinemosModuleConstant.MONITOR_RPA_MGMT_TOOL_SERVICE)) {
			dialog = new RpaManagementToolServiceCreateDialog(shell, managerName, monitorId, updateFlg);
		} else {
			for(IMonitorPlugin extensionMonitor: LoadMonitorPlugin.getExtensionMonitorList()){
				if(pluginId.equals(extensionMonitor.getMonitorPluginId())){
					return extensionMonitor.create(shell, managerName, monitorId, updateFlg);
				}
			}

			m_log.warn("unknown pluginId " + pluginId);
			return -1;
		}
		if (m_MonitorNumericValueInfoList != null) {
			// グラフから監視設定画面を開く場合は、閾値情報を渡す
			dialog.setGraphMonitorNumericValueInfo(m_MonitorNumericValueInfoList);
		}
		return dialog.open();
	}
	
	/**
	 * グラフ画面から閾値情報を変えた場合に設定する。
	 * @param monitorNumericValueInfo
	 */
	public void setGraphMonitorNumericValueInfo(List<MonitorNumericValueInfoResponse> monitorNumericValueInfo) {
		this.m_MonitorNumericValueInfoList = monitorNumericValueInfo;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		MonitorListView view = null;
		try {
			view = (MonitorListView) this.viewPart.getAdapter(MonitorListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		MonitorListComposite composite = (MonitorListComposite) view.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		if (list == null || list.size() == 0)
			throw new InternalError("select element is not find");

		String managerName = (String)list.get(GetMonitorListTableDefine.MANAGER_NAME);
		String pluginId = (String) list.get(GetMonitorListTableDefine.MONITOR_TYPE_ID);
		String monitorId = (String) list.get(GetMonitorListTableDefine.MONITOR_ID);

		dialogOpen(this.viewPart.getSite().getShell(), managerName, pluginId, monitorId);

		// ビューの更新
		view.update();
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if(part instanceof MonitorListView){
					// Enable button when 1 item is selected
					MonitorListView view = (MonitorListView)part;

					if(view.getSelectedNum() == 1) {
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}

}
