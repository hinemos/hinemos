/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.custom.action.DeleteCustomNumeric;
import com.clustercontrol.custom.action.DeleteCustomString;
import com.clustercontrol.customtrap.action.DeleteCustomTrapNumeric;
import com.clustercontrol.customtrap.action.DeleteCustomTrapString;
import com.clustercontrol.hinemosagent.action.DeleteAgent;
import com.clustercontrol.http.action.DeleteHttpNumeric;
import com.clustercontrol.http.action.DeleteHttpScenario;
import com.clustercontrol.http.action.DeleteHttpString;
import com.clustercontrol.jmx.action.DeleteJmx;
import com.clustercontrol.logfile.action.DeleteLogfile;
import com.clustercontrol.monitor.action.DeleteInterface;
import com.clustercontrol.monitor.composite.MonitorListComposite;
import com.clustercontrol.monitor.plugin.IMonitorPlugin;
import com.clustercontrol.monitor.plugin.LoadMonitorPlugin;
import com.clustercontrol.monitor.run.action.GetMonitorListTableDefine;
import com.clustercontrol.monitor.view.MonitorListView;
import com.clustercontrol.performance.monitor.action.DeletePerformance;
import com.clustercontrol.ping.action.DeletePing;
import com.clustercontrol.port.action.DeletePort;
import com.clustercontrol.process.action.DeleteProcess;
import com.clustercontrol.snmp.action.DeleteSnmpNumeric;
import com.clustercontrol.snmp.action.DeleteSnmpString;
import com.clustercontrol.snmptrap.action.DeleteSnmpTrap;
import com.clustercontrol.sql.action.DeleteSqlNumeric;
import com.clustercontrol.sql.action.DeleteSqlString;
import com.clustercontrol.systemlog.action.DeleteSystemlog;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.winevent.action.DeleteWinEvent;
import com.clustercontrol.winservice.action.DeleteWinService;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;

/**
 * 監視[一覧]ビューの削除アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class MonitorDeleteAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( MonitorDeleteAction.class );

	/** アクションID */
	public static final String ID = MonitorDeleteAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		MonitorListView monitorListView = null;
		try {
			monitorListView = (MonitorListView) this.viewPart.getAdapter(MonitorListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (monitorListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		MonitorListComposite composite = (MonitorListComposite) monitorListView.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();
		List<String[]> argsList = new ArrayList<String[]>();
		if(list != null && list.size() > 0){
			for (Object obj : list) {
				List<?> objList = (List<?>)obj;
				String[] args = new String[3];
				args[0] = (String) objList.get(GetMonitorListTableDefine.MANAGER_NAME);
				args[1] = (String) objList.get(GetMonitorListTableDefine.MONITOR_TYPE_ID);
				args[2] = (String) objList.get(GetMonitorListTableDefine.MONITOR_ID);
				argsList.add(args);
			}
		}

		// 選択アイテムがある場合に、削除処理を呼び出す
		if(argsList.isEmpty() ) {
			return null;
		}
		// 削除を実行してよいかの確認ダイアログの表示
		String msg = null;
		String[] msgArgs = new String[2];
		if(argsList.isEmpty() == false) {
			if (argsList.size() == 1) {
				msgArgs[0] = argsList.get(0)[2] + "(" + argsList.get(0)[0] + ")";
				msg = "message.monitor.39";
			} else {
				msgArgs[0] = Integer.toString(argsList.size());
				msg = "message.monitor.81";
			}
		}

		if (!MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString(msg, msgArgs))) {

			// OKが押されない場合は終了
			return null;
		}

		DeleteInterface deleteInterfaceAgent = null;
		DeleteInterface deleteInterfaceHttpN = null;
		DeleteInterface deleteInterfaceHttpS = null;
		DeleteInterface deleteInterfaceHttpScenario = null;
		DeleteInterface deleteInterfacePerformance = null;
		DeleteInterface deleteInterfacePing = null;
		DeleteInterface deleteInterfacePort = null;
		DeleteInterface deleteInterfaceProcess = null;
		DeleteInterface deleteInterfaceSnmpN = null;
		DeleteInterface deleteInterfaceSnmpS = null;
		DeleteInterface deleteInterfaceSqlN = null;
		DeleteInterface deleteInterfaceSqlS = null;
		DeleteInterface deleteInterfaceSystemLog = null;
		DeleteInterface deleteInterfaceLogfile = null;
		DeleteInterface deleteInterfaceCustomN = null;
		DeleteInterface deleteInterfaceCustomS = null;
		DeleteInterface deleteInterfaceSnmpTrap = null;
		DeleteInterface deleteInterfaceWinservice = null;
		DeleteInterface deleteInterfaceWinevent = null;
		DeleteInterface deleteInterfaceJmx = null;
		DeleteInterface deleteInterfaceExt = null;
		DeleteInterface deleteInterfaceCustomTrapN = null;		
		DeleteInterface deleteInterfaceCustomTrapS = null;		

		Map<String, List<String>> deleteMapAgent = null;
		Map<String, List<String>> deleteMapHttpN = null;
		Map<String, List<String>> deleteMapHttpS = null;
		Map<String, List<String>> deleteMapHttpScenario = null;
		Map<String, List<String>> deleteMapPerformance = null;
		Map<String, List<String>> deleteMapPing = null;
		Map<String, List<String>> deleteMapPort = null;
		Map<String, List<String>> deleteMapProcess = null;
		Map<String, List<String>> deleteMapSnmpN = null;
		Map<String, List<String>> deleteMapSnmpS = null;
		Map<String, List<String>> deleteMapSqlN = null;
		Map<String, List<String>> deleteMapSqlS = null;
		Map<String, List<String>> deleteMapSystemLog = null;
		Map<String, List<String>> deleteMapLogfile = null;
		Map<String, List<String>> deleteMapCustomN = null;
		Map<String, List<String>> deleteMapCustomS = null;
		Map<String, List<String>> deleteMapSnmpTrap = null;
		Map<String, List<String>> deleteMapWinservice = null;
		Map<String, List<String>> deleteMapWinevent = null;
		Map<String, List<String>> deleteMapJmx = null;
		Map<String, List<String>> deleteMapExt = null;
		Map<String, List<String>> deleteMapCustomTrapN = null;
		Map<String, List<String>> deleteMapCustomTrapS = null;

		for(String[] args : argsList) {
			String pluginId = args[1];
			String managerName = args[0];
			if (pluginId.equals(HinemosModuleConstant.MONITOR_AGENT)) {
				if(deleteMapAgent == null) {
					deleteMapAgent = new ConcurrentHashMap<String, List<String>>();
				}
				if(deleteMapAgent.get(managerName) == null) {
					deleteMapAgent.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_N)) {
				if(deleteMapHttpN == null) {
					deleteMapHttpN = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapHttpN.get(managerName) == null) {
					deleteMapHttpN.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_S)) {
				if(deleteMapHttpS == null) {
					deleteMapHttpS = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapHttpS.get(managerName) == null) {
					deleteMapHttpS.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_SCENARIO)) {
				if(deleteMapHttpScenario == null) {
					deleteMapHttpScenario = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapHttpScenario.get(managerName) == null) {
					deleteMapHttpScenario.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PERFORMANCE)) {
				if(deleteMapPerformance == null) {
					deleteMapPerformance = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapPerformance.get(managerName) == null) {
					deleteMapPerformance.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PING)) {
				if(deleteMapPing == null) {
					deleteMapPing = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapPing.get(managerName) == null) {
					deleteMapPing.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PORT)) {
				if(deleteMapPort == null) {
					deleteMapPort = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapPort.get(managerName) == null) {
					deleteMapPort.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PROCESS)) {
				if(deleteMapProcess == null) {
					deleteMapProcess = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapProcess.get(managerName) == null) {
					deleteMapProcess.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMP_N)) {
				if(deleteMapSnmpN == null) {
					deleteMapSnmpN = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapSnmpN.get(managerName) == null) {
					deleteMapSnmpN.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMP_S)) {
				if(deleteMapSnmpS == null) {
					deleteMapSnmpS = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapSnmpS.get(managerName) == null) {
					deleteMapSnmpS.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SQL_N)) {
				if(deleteMapSqlN == null) {
					deleteMapSqlN = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapSqlN.get(managerName) == null) {
					deleteMapSqlN.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SQL_S)) {
				if(deleteMapSqlS == null) {
					deleteMapSqlS = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapSqlS.get(managerName) == null) {
					deleteMapSqlS.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)) {
				if(deleteMapSystemLog == null) {
					deleteMapSystemLog = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapSystemLog.get(managerName) == null) {
					deleteMapSystemLog.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
				if(deleteMapLogfile == null) {
					deleteMapLogfile = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapLogfile.get(managerName) == null) {
					deleteMapLogfile.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOM_N)) {
				if(deleteMapCustomN == null) {
					deleteMapCustomN = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapCustomN.get(managerName) == null) {
					deleteMapCustomN.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
				if(deleteMapCustomS == null) {
					deleteMapCustomS = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapCustomS.get(managerName) == null) {
					deleteMapCustomS.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)) {
				if(deleteMapSnmpTrap == null) {
					deleteMapSnmpTrap = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapSnmpTrap.get(managerName) == null) {
					deleteMapSnmpTrap.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_WINSERVICE)) {
				if(deleteMapWinservice == null) {
					deleteMapWinservice = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapWinservice.get(managerName) == null) {
					deleteMapWinservice.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
				if(deleteMapWinevent == null) {
					deleteMapWinevent = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapWinevent.get(managerName) == null) {
					deleteMapWinevent.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_JMX)) {
				if(deleteMapJmx == null) {
					deleteMapJmx = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapJmx.get(managerName) == null) {
					deleteMapJmx.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)) {
				if(deleteMapCustomTrapN == null) {
					deleteMapCustomTrapN = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapCustomTrapN.get(managerName) == null) {
					deleteMapCustomTrapN.put(managerName, new ArrayList<String>());
				}
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
				if(deleteMapCustomTrapS == null) {
					deleteMapCustomTrapS = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapCustomTrapS.get(managerName) == null) {
					deleteMapCustomTrapS.put(managerName, new ArrayList<String>());
				}
			} else {
				if(deleteMapExt == null) {
					deleteMapExt = new ConcurrentHashMap<String, List<String>>();
				}
				if (deleteMapExt.get(managerName) == null) {
					deleteMapExt.put(managerName, new ArrayList<String>());
				}
			}
		}

		for (String[] args : argsList) {
			String managerName = args[0];
			String pluginId = args[1];
			String monitorId = args[2];

			if (pluginId.equals(HinemosModuleConstant.MONITOR_AGENT)) {
				if (deleteInterfaceAgent == null) {
					deleteInterfaceAgent = new DeleteAgent();
				}
				deleteMapAgent.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_N)) {
				if (deleteInterfaceHttpN == null) {
					deleteInterfaceHttpN = new DeleteHttpNumeric();
				}
				deleteMapHttpN.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_S)) {
				if (deleteInterfaceHttpS == null) {
					deleteInterfaceHttpS = new DeleteHttpString();
				}
				deleteMapHttpS.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_HTTP_SCENARIO)) {
				if (deleteInterfaceHttpScenario == null) {
					deleteInterfaceHttpScenario = new DeleteHttpScenario();
				}
				deleteMapHttpScenario.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PERFORMANCE)) {
				if (deleteInterfacePerformance == null) {
					deleteInterfacePerformance = new DeletePerformance();
				}
				deleteMapPerformance.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PING)) {
				if (deleteInterfacePing == null) {
					deleteInterfacePing = new DeletePing();
				}
				deleteMapPing.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PORT)) {
				if (deleteInterfacePort == null) {
					deleteInterfacePort = new DeletePort();
				}
				deleteMapPort.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_PROCESS)) {
				if (deleteInterfaceProcess == null) {
					deleteInterfaceProcess = new DeleteProcess();
				}
				deleteMapProcess.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMP_N)) {
				if (deleteInterfaceSnmpN == null) {
					deleteInterfaceSnmpN = new DeleteSnmpNumeric();
				}
				deleteMapSnmpN.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMP_S)) {
				if (deleteInterfaceSnmpS == null) {
					deleteInterfaceSnmpS = new DeleteSnmpString();
				}
				deleteMapSnmpS.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SQL_N)) {
				if (deleteInterfaceSqlN == null) {
					deleteInterfaceSqlN = new DeleteSqlNumeric();
				}
				deleteMapSqlN.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SQL_S)) {
				if (deleteInterfaceSqlS == null) {
					deleteInterfaceSqlS = new DeleteSqlString();
				}
				deleteMapSqlS.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)) {
				if (deleteInterfaceSystemLog == null) {
					deleteInterfaceSystemLog = new DeleteSystemlog();
				}
				deleteMapSystemLog.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
				if (deleteInterfaceLogfile == null) {
					deleteInterfaceLogfile = new DeleteLogfile();
				}
				deleteMapLogfile.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOM_N)) {
				if (deleteInterfaceCustomN == null) {
					deleteInterfaceCustomN = new DeleteCustomNumeric();
				}
				deleteMapCustomN.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
				if (deleteInterfaceCustomS == null) {
					deleteInterfaceCustomS = new DeleteCustomString();
				}
				deleteMapCustomS.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)) {
				if (deleteInterfaceSnmpTrap == null) {
					deleteInterfaceSnmpTrap = new DeleteSnmpTrap();
				}
				deleteMapSnmpTrap.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_WINSERVICE)) {
				if (deleteInterfaceWinservice == null) {
					deleteInterfaceWinservice = new DeleteWinService();
				}
				deleteMapWinservice.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
				if (deleteInterfaceWinevent == null) {
					deleteInterfaceWinevent = new DeleteWinEvent();
				}
				deleteMapWinevent.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_JMX)) {
				if (deleteInterfaceJmx == null) {
					deleteInterfaceJmx = new DeleteJmx();
				}
				deleteMapJmx.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)) {
				if (deleteInterfaceCustomTrapN == null) {
					deleteInterfaceCustomTrapN = new DeleteCustomTrapNumeric();
				}
				deleteMapCustomTrapN.get(managerName).add(monitorId);
			} else if (pluginId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
				if (deleteInterfaceCustomTrapS == null) {
					deleteInterfaceCustomTrapS = new DeleteCustomTrapString();
				}
				deleteMapCustomTrapS.get(managerName).add(monitorId);
			} else {
				int i = 0;
				for(IMonitorPlugin extensionMonitor: LoadMonitorPlugin.getExtensionMonitorList()){
					if(pluginId.equals(extensionMonitor.getMonitorPluginId())){
						i++;
						if (deleteInterfaceExt == null) {
							deleteInterfaceExt = extensionMonitor.getDeleteMonitorClassObject();
						}
						break;
					}
				}
				if(i == 0){
					m_log.warn("unknown pluginId " + pluginId);
					break;
				}
				deleteMapExt.get(managerName).add(monitorId);
			}
		}
		String errMessage = "";
		int errCount = 0;
		int successCount = 0;
		try {
			if (deleteInterfaceAgent != null) {
				for(Map.Entry<String, List<String>> map : deleteMapAgent.entrySet()) {
					try {
						deleteInterfaceAgent.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceHttpN != null) {
				for(Map.Entry<String, List<String>> map : deleteMapHttpN.entrySet()) {
					try {
						deleteInterfaceHttpN.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceHttpS != null) {
				for(Map.Entry<String, List<String>> map : deleteMapHttpS.entrySet()) {
					try {
						deleteInterfaceHttpS.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceHttpScenario != null) {
				for(Map.Entry<String, List<String>> map : deleteMapHttpScenario.entrySet()) {
					try {
						deleteInterfaceHttpScenario.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfacePerformance != null) {
				for(Map.Entry<String, List<String>> map : deleteMapPerformance.entrySet()) {
					try {
						deleteInterfacePerformance.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfacePing != null) {
				for(Map.Entry<String, List<String>> map : deleteMapPing.entrySet()) {
					try {
						deleteInterfacePing.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfacePort != null) {
				for(Map.Entry<String, List<String>> map : deleteMapPort.entrySet()) {
					try {
						deleteInterfacePort.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceProcess != null) {
				for(Map.Entry<String, List<String>> map : deleteMapProcess.entrySet()) {
					try {
						deleteInterfaceProcess.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceSnmpN != null) {
				for(Map.Entry<String, List<String>> map : deleteMapSnmpN.entrySet()) {
					try {
						deleteInterfaceSnmpN.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceSnmpS != null) {
				for(Map.Entry<String, List<String>> map : deleteMapSnmpS.entrySet()) {
					try {
						deleteInterfaceSnmpS.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceSqlN != null) {
				for(Map.Entry<String, List<String>> map : deleteMapSqlN.entrySet()) {
					try {
						deleteInterfaceSqlN.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceSqlS != null) {
				for(Map.Entry<String, List<String>> map : deleteMapSqlS.entrySet()) {
					try {
						deleteInterfaceSqlS.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceSystemLog != null) {
				for(Map.Entry<String, List<String>> map : deleteMapSystemLog.entrySet()) {
					try {
						deleteInterfaceSystemLog.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceLogfile != null) {
				for(Map.Entry<String, List<String>> map : deleteMapLogfile.entrySet()) {
					try {
						deleteInterfaceLogfile.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceCustomN != null) {
				for(Map.Entry<String, List<String>> map : deleteMapCustomN.entrySet()) {
					try {
						deleteInterfaceCustomN.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceCustomS != null) {
				for(Map.Entry<String, List<String>> map : deleteMapCustomS.entrySet()) {
					try {
						deleteInterfaceCustomS.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}			
			if (deleteInterfaceSnmpTrap != null) {
				for(Map.Entry<String, List<String>> map : deleteMapSnmpTrap.entrySet()) {
					try {
						deleteInterfaceSnmpTrap.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceWinservice != null) {
				for(Map.Entry<String, List<String>> map : deleteMapWinservice.entrySet()) {
					try {
						deleteInterfaceWinservice.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceWinevent != null) {
				for(Map.Entry<String, List<String>> map : deleteMapWinevent.entrySet()) {
					try {
						deleteInterfaceWinevent.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceJmx != null) {
				for(Map.Entry<String, List<String>> map : deleteMapJmx.entrySet()) {
					try {
						deleteInterfaceJmx.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceExt != null) {
				for(Map.Entry<String, List<String>> map : deleteMapExt.entrySet()) {
					try {
						deleteInterfaceExt.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
			if (deleteInterfaceCustomTrapN != null) {
				for(Map.Entry<String, List<String>> map : deleteMapCustomTrapN.entrySet()) {
					try {
						deleteInterfaceCustomTrapN.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}		
			if (deleteInterfaceCustomTrapS != null) {
				for(Map.Entry<String, List<String>> map : deleteMapCustomTrapS.entrySet()) {
					try {
						deleteInterfaceCustomTrapS.delete(map.getKey(), map.getValue());
						successCount = successCount + map.getValue().size();
					} catch(InvalidRole_Exception e) {
						throw e;
					} catch(Exception e) {
						errCount = errCount + map.getValue().size();
						errMessage = HinemosMessage.replace(e.getMessage());
					}
				}
			}
		} catch(Exception e) {
			if (e instanceof InvalidRole_Exception) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				return null;
			}
		}
		String message = null;
		if (errCount > 0) {
			if (errCount == 1) {
				msgArgs[1] = errMessage;
				message = Messages.getString("message.monitor.38", msgArgs);
			} else {
				message = Messages.getString("message.monitor.83", new String[]{Integer.toString(errCount), errMessage});
			}
			MessageDialog.openError(null, Messages.getString("failed"), message);
		}
		if (successCount > 0) {
			if (successCount == 1) {
				message = Messages.getString("message.monitor.37", msgArgs);
			} else {
				message = Messages.getString("message.monitor.82", new String[]{Integer.toString(successCount)});
			}
			MessageDialog.openInformation(null, Messages.getString("successful"), message);
			monitorListView.update();
		}
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

					if(view.getSelectedNum() > 0) {
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
