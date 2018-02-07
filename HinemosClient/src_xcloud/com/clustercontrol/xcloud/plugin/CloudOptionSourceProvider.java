/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import com.clustercontrol.xcloud.extensions.ICloudOptionHandler;
import com.clustercontrol.xcloud.model.base.PropChangedEventNotifier;
import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.base.PropertyObserver;
import com.clustercontrol.xcloud.model.base.ValueObserver;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.model.cloud.ILocation;

public class CloudOptionSourceProvider extends AbstractSourceProvider {
	public final static String ActiveHinemosManager = "activeHinemosManager";
	public final static String ActiveCloudScope = "activeCloudScope";
	public final static String ActiveLocation = "activeLocation";
	public final static String LastCheckedHandler = "lastCheckedHandler";
	
	public static class OptionHandlerHolder {
		protected String commandId;
		protected ICloudOptionHandler optionHandler;
		
		public OptionHandlerHolder(String commandId, ICloudOptionHandler optionHandler) {
			this.commandId = commandId;
			this.optionHandler = optionHandler;
		}
		
		public String getCommandId() {
			return commandId;
		}
		public void setCommandId(String commandId) {
			this.commandId = commandId;
		}
		public ICloudOptionHandler getCloudOptionHandler() {
			return optionHandler;
		}
		public void setCloudOptionHandler(ICloudOptionHandler optionHandler) {
			this.optionHandler = optionHandler;
		}
	}
	
	/**
	 * プロパティ変更通知処理をカプセル化したクラス。
	 */
	protected final PropChangedEventNotifier notifier = new PropChangedEventNotifier();
	
	protected IHinemosManager activeHinemosManager;
	protected ICloudScope activeCloudScope;
	protected ILocation activeLocation;
	
	// この値は、テスト中に更新するので、eclipse の variable として扱わない！！
	protected OptionHandlerHolder lastCheckedHandler;
	
	public static final PropertyId<ValueObserver<IHinemosManager>> PROP_ActiveHinemosManager = new PropertyId<ValueObserver<IHinemosManager>>("activeHinemosManager"){};
	public static final PropertyId<ValueObserver<ICloudScope>> PROP_ActiveCloudScope = new PropertyId<ValueObserver<ICloudScope>>("activeCloudScope"){};
	public static final PropertyId<ValueObserver<ILocation>> PROP_ActiveLocation = new PropertyId<ValueObserver<ILocation>>("activeLocation"){};
	public static final PropertyId<ValueObserver<OptionHandlerHolder>> PROP_LastCheckedHandler = new PropertyId<ValueObserver<OptionHandlerHolder>>("lastCheckedHandler"){};
	
	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { ActiveCloudScope, ActiveLocation, ActiveHinemosManager };
	}

	@Override
	public Map<?, ?> getCurrentState() {
		Map<String, Object> map = new HashMap<>();
		map.put(ActiveCloudScope, activeCloudScope);
		map.put(ActiveLocation, activeLocation);
		map.put(ActiveHinemosManager, activeHinemosManager);
		return map;
	}

	public IHinemosManager getHinemosManager() {
		return this.activeHinemosManager;
	}
	public void setHinemosManager(IHinemosManager activeHinemosManager) {
		if (this.activeHinemosManager == activeHinemosManager)
			return;
		
		IHinemosManager oldLocation = this.activeHinemosManager;
		this.activeHinemosManager = activeHinemosManager;
		fireSourceChanged(ISources.WORKBENCH, ActiveHinemosManager, this.activeHinemosManager);
		notifier.fireValueChanged(this, PROP_ActiveHinemosManager, this.activeHinemosManager, oldLocation);
	}

	public ICloudScope getActiveCloudScope() {
		return this.activeCloudScope;
	}
	public void setActiveCloudScope(ICloudScope activeCloudScope) {
		if (this.activeCloudScope == activeCloudScope)
			return;
		
		ICloudScope oldCloudScope = this.activeCloudScope;
		this.activeCloudScope = activeCloudScope;
		fireSourceChanged(ISources.WORKBENCH, ActiveCloudScope, this.activeCloudScope);
		notifier.fireValueChanged(this, PROP_ActiveCloudScope, this.activeCloudScope, oldCloudScope);
	}

	public ILocation getActiveLocation() {
		return this.activeLocation;
	}
	public void setActiveLocation(ILocation activeLocation) {
		if (this.activeLocation == activeLocation)
			return;
		
		ILocation oldLocation = this.activeLocation;
		this.activeLocation = activeLocation;
		fireSourceChanged(ISources.WORKBENCH, ActiveLocation, this.activeLocation);
		notifier.fireValueChanged(this, PROP_ActiveLocation, this.activeLocation, oldLocation);
	}

	public OptionHandlerHolder getLastCheckedHandler() {
		return this.lastCheckedHandler;
	}

	public void setLastCheckedHandler(OptionHandlerHolder lastCheckedHandler) {
		if (this.lastCheckedHandler == lastCheckedHandler)
			return;
		
		OptionHandlerHolder oldActiveOptionHandler = this.lastCheckedHandler;
		this.lastCheckedHandler = lastCheckedHandler;
		notifier.fireValueChanged(this, PROP_LastCheckedHandler, this.lastCheckedHandler, oldActiveOptionHandler);
	}
	
	public <P, O extends PropertyObserver<P>> void addPropertyObserver(PropertyId<O> pid, O observer) {
		notifier.addPropertyObserver(pid, observer);
	}
	
	public <P, O extends PropertyObserver<P>> void removePropertyObserver(PropertyId<O> pid, O observer) {
		notifier.removePropertyObserver(pid, observer);
	}

	@Override
	public void dispose() {
	}
	
	public static IHinemosManager getActiveHinemosManagerToProvider() {
		ISourceProviderService sourceProviderService = (ISourceProviderService)PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		CloudOptionSourceProvider provider = (CloudOptionSourceProvider)sourceProviderService.getSourceProvider(ActiveHinemosManager);
		return provider.getHinemosManager();
	}
	public static void setActiveHinemosManagerToProvider(IHinemosManager activeHinemosManager) {
		ISourceProviderService sourceProviderService = (ISourceProviderService)PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		CloudOptionSourceProvider provider = (CloudOptionSourceProvider)sourceProviderService.getSourceProvider(ActiveHinemosManager);
		if (provider != null)
			provider.setHinemosManager(activeHinemosManager);
	}
	
	public static ICloudScope getActiveCloudScopeToProvider() {
		ISourceProviderService sourceProviderService = (ISourceProviderService)PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		CloudOptionSourceProvider provider = (CloudOptionSourceProvider)sourceProviderService.getSourceProvider(ActiveCloudScope);
		return provider.getActiveCloudScope();
	}
	public static void setActiveCloudScopeToProvider(ICloudScope activeCloudScope) {
		ISourceProviderService sourceProviderService = (ISourceProviderService)PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		CloudOptionSourceProvider provider = (CloudOptionSourceProvider)sourceProviderService.getSourceProvider(ActiveCloudScope);
		if (provider != null)
			provider.setActiveCloudScope(activeCloudScope);
	}
	
	public static ILocation getActiveLocationToProvider() {
		ISourceProviderService sourceProviderService = (ISourceProviderService)PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		CloudOptionSourceProvider provider = (CloudOptionSourceProvider)sourceProviderService.getSourceProvider(ActiveLocation);
		return provider.getActiveLocation();
	}
	public static void setActiveLocationToProvider(ILocation activeLocation) {
		ISourceProviderService sourceProviderService = (ISourceProviderService)PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		CloudOptionSourceProvider provider = (CloudOptionSourceProvider)sourceProviderService.getSourceProvider(ActiveLocation);
		if (provider != null)
			provider.setActiveLocation(activeLocation);
	}
	
	public static OptionHandlerHolder getActiveOptionHandlerToProvider() {
		ISourceProviderService sourceProviderService = (ISourceProviderService)PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		CloudOptionSourceProvider provider = (CloudOptionSourceProvider)sourceProviderService.getSourceProvider(ActiveCloudScope);
		return provider.getLastCheckedHandler();
	}
	public static void setActiveOptionHandlerToProvider(OptionHandlerHolder lastCheckedHandler) {
		ISourceProviderService sourceProviderService = (ISourceProviderService)PlatformUI.getWorkbench().getService(ISourceProviderService.class);
		CloudOptionSourceProvider provider = (CloudOptionSourceProvider)sourceProviderService.getSourceProvider(ActiveCloudScope);
		if (provider != null)
			provider.setLastCheckedHandler(lastCheckedHandler);
	}
}
