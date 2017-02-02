package com.clustercontrol.monitor.plugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.monitor.bean.MonitorTypeMstConstant;

public class LoadMonitorPlugin {
	private static List<IMonitorPlugin> extensionMonitorList = new ArrayList<IMonitorPlugin>();

	static {
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// 拡張ポイントを取得
		IExtensionPoint point = registry.getExtensionPoint(ClusterControlPlugin.getDefault().getBundle().getSymbolicName() + ".monitorPlugin");

		for(IExtension extension: point.getExtensions()){
			for(IConfigurationElement element: extension.getConfigurationElements()){
				IMonitorPlugin pluginMonitor = null;

				// 要素名がmonitorPluginだった場合、ExtensionTypeの情報を取得
				if(element.getName().equals("monitorPlugin")){
					try {
						pluginMonitor = (IMonitorPlugin) element.createExecutableExtension("plugin_monitor_class");
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}

				if(pluginMonitor != null){
					assert pluginMonitor.getMonitorPluginId() != null: "MonitorPluginId is null.";
					assert pluginMonitor.getMonitorName() != null: "MonitorName is null.";
					assert pluginMonitor.getDeleteMonitorClassObject() != null: "DeleteMonitorClassObject is null";

					//HinemosModuleConstant へ追加監視の情報を登録
					HinemosModuleConstant.ExtensionType extensionType = new HinemosModuleConstant.ExtensionType(pluginMonitor.getMonitorPluginId(), pluginMonitor.getMonitorCode(), pluginMonitor.getMonitorName());
					HinemosModuleConstant.addExtensionType(extensionType);

					extensionMonitorList.add(pluginMonitor);
				}
			}
		}

		//MonitorTypeMst へ追加監視の情報を登録
		ArrayList<ArrayList<Object>> monitorTypeMst = MonitorTypeMstConstant.getListAll();
		ArrayList<Object> list = null;

		for(IMonitorPlugin extensionMonitor: extensionMonitorList){
			list = new ArrayList<Object>();
			list.add(extensionMonitor.getMonitorPluginId());
			list.add(extensionMonitor.getMonitorType());
			monitorTypeMst.add(list);
		}
	}

	public static List<IMonitorPlugin> getExtensionMonitorList(){
		return new ArrayList<IMonitorPlugin>(extensionMonitorList);
	}

	public static IMonitorPlugin getExtensionMonitor(String monitorPluginId){
		for(IMonitorPlugin extensionMonitor: extensionMonitorList){
			if(extensionMonitor.getMonitorPluginId().equals(monitorPluginId)){
				return extensionMonitor;
			}
		}
		return null;
	}
}
