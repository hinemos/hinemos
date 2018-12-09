/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.clustercontrol.ClusterControlPlugin;

public final class CloudViewPluginExtension {
	
	private static CloudViewPluginExtension singleton;
	
	private List<PluginView> views = new ArrayList<>();

	private CloudViewPluginExtension(){
		List<PluginView> views = new LinkedList<PluginView>();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// 拡張ポイントを取得
		IExtensionPoint point = registry.getExtensionPoint(ClusterControlPlugin.getDefault().getBundle().getSymbolicName() + ".viewPlugin");

		for(IExtension extension: point.getExtensions()){
			for(IConfigurationElement element: extension.getConfigurationElements()){
				Integer priority = null;
				ViewPosition position = null;
				String viewId = null;
				
				// 要素名がview_pluginだった場合、ExtensionTypeの情報を取得
				if(element.getName().equals("cloudViewPlugin")){
					try {
						priority = Integer.parseInt(element.getAttribute("priority"));
						position = ViewPosition.valueOf(element.getAttribute("position"));
						viewId = element.getAttribute("view_id");
						
						assert priority != null: "priority is null.";
						assert position != null: "position is null.";
						assert viewId != null: "viewId is null.";
						
						views.add(new PluginView(priority, position, viewId));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		Collections.sort(views, (view1, view2)-> {
			int result;
			result = view1.getPosition().ordinal() - view2.getPosition().ordinal();
			
			if(result == 0){
				result = view1.getPriority() - view2.getPriority();
			}
			return result;
		});
		
		views.addAll(views);
	}
	
	public static List<PluginView> getPluginViews(){
		if (singleton == null) {
			singleton = new CloudViewPluginExtension();
		}
		return singleton.views;
	}
	
	public enum ViewPosition{
		left,
		top,
		right,
		middle,
		bottom;
	}
	
	public static class PluginView{
		private int priority;
		private ViewPosition position;
		private String id;
		
		public PluginView(int priority, ViewPosition position, String id){
			this.priority = priority;
			this.position = position;
			this.id = id;
		}
		
		public int getPriority() {
			return priority;
		}
		public void setPriority(int priority) {
			this.priority = priority;
		}
		public ViewPosition getPosition() {
			return position;
		}
		public void setPosition(ViewPosition position) {
			this.position = position;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}
}
