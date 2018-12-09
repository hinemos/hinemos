/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceFeature;

import org.apache.log4j.Logger;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ObjectSharingService;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.ws.util.HttpAuthenticator;
import com.clustercontrol.ws.xcloud.CloudEndpointImpl;
import com.clustercontrol.ws.xcloud.IWebServiceBase;
import com.clustercontrol.ws.xcloud.Publisher;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.HinemosCredential;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.factory.monitors.BillingDetailMonitor;
import com.clustercontrol.xcloud.factory.monitors.CloudServiceBillingDetailRunMonitor;
import com.clustercontrol.xcloud.factory.monitors.CloudServiceBillingRunMonitor;
import com.clustercontrol.xcloud.factory.monitors.PlatformResourceMonitor;
import com.clustercontrol.xcloud.factory.monitors.PlatformServiceConditionMonitor;
import com.clustercontrol.xcloud.factory.monitors.PlatformServiceRunMonitor;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.registry.IObjMonitor;
import com.clustercontrol.xcloud.registry.ObjMonitor;
import com.clustercontrol.xcloud.registry.ObjRegistry;
import com.clustercontrol.xcloud.util.OptionClassLoader;

public class CloudManager {
	private static CloudManager singleton;
	
	public static synchronized CloudManager singleton() {
		if (singleton == null) {
			singleton = new CloudManager();
		}
		return singleton;
	}
	
	public static synchronized void release() {
		if (singleton != null) {
			singleton.close();
			singleton = null;
		}
	}
	
	public final static String Key_MainEndpoint = "main_endpoint";

	private Publisher publisher;
	private Map<String, OptionClassLoader> loaderMap = new LinkedHashMap<>();
	
	private CloudManager() {
		publisher = new Publisher();
		
		Publisher.setContextListener(new Publisher.ContextListener() {
			@Override
			public void onUpdateContext(WebServiceContext wsctx) {
				try {
					String accountName = HttpAuthenticator.getAccount(wsctx).split(":")[0];
					Session.current().setHinemosCredential(new HinemosCredential(accountName));
				} catch (InvalidUserPass e) {
					throw new InternalManagerError(e);
				}
			}
		});

		// Web サービスのリクエスト実行中に発生したエラーの最終処理を設定。
		publisher.setUncaughtExceptionHandler(new Publisher.UncaughtExceptionHandler() {
			@Override
			public CloudManagerException uncaughtException(Throwable e) {
				if (e instanceof CloudManagerException) {
					return (CloudManagerException)e;
				}
				else if (e instanceof PluginException) {
					return new CloudManagerException(e);
				}
				else {
					return null;
				}
			}
		});
	}

	public synchronized void publish(String postAddress, IWebServiceBase implementor, WebServiceFeature...features) {
		String address = HinemosPropertyCommon.ws_client_address.getStringValue() + postAddress;
		Logger.getLogger(this.getClass()).info("publish " + address);
		publisher.publish(address, implementor, features);
	}

	public IRepository getRepository() {
		return getObject(IRepository.class, Repository.class);
	}

	public IPlatforms getPlatforms() {
		return getObject(IPlatforms.class, Platforms.class);
	}

	public ICloudScopes getCloudScopes() {
		return getObject(ICloudScopes.class, CloudScopes.class);
	}

	public ILoginUsers getLoginUsers() {
		return getObject(ILoginUsers.class, LoginUsers.class);
	}

	public IInstances getInstances(CloudLoginUserEntity user, LocationEntity location) throws CloudManagerException {
		IInstances instances = getObject(IInstances.class, Instances.class);
		instances.setAccessInformation(user, location);
		return instances;
	}

	public IStorages getStorages(CloudLoginUserEntity user, LocationEntity location) throws CloudManagerException {
		IStorages storages = getObject(IStorages.class, Storages.class);
		storages.setAccessInformation(user, location);
		return storages;
	}

	public INetworks getNetworks(CloudLoginUserEntity user, LocationEntity location) throws CloudManagerException {
		INetworks networks = getObject(INetworks.class, Networks.class);
		networks.setAccessInformation(user, location);
		return networks;
	}
	
	public IBillings getBillings() {
		return getObject(IBillings.class, Billings.class);
	}

	public IObjMonitor getObjMonitor() {
		return getObject(IObjMonitor.class, ObjMonitor.class);
	}
	
	protected  <T, M extends T> T getObject(Class<T> interfaceClass, Class<M> objectClass) {
		T service = ObjRegistry.reg().get(interfaceClass);
		if (service == null) {
			synchronized (CloudManager.class) {
				service = ObjRegistry.reg().get(interfaceClass);
				if (service == null) {
					ObjRegistry.reg().put(interfaceClass, objectClass);
					service = ObjRegistry.reg().get(interfaceClass);
				}
			}
		}
		return service;
	}
	
	private ICloudOption getCloudOption(String platformId) {
		return ObjRegistry.reg().get(ICloudOption.class, platformId);
	}

	public <T extends ICloudOption> void addCloudOption(String platformId, T optionClazz) {
		if (ObjRegistry.reg().get(ICloudOption.class, platformId) != null)
			throw new InternalManagerError();
		
		optionClazz.start();
		
		ObjRegistry.reg().put(ICloudOption.class, platformId, optionClazz);
	}

	public void addCloudOption(String platformId, String optionClass, String dependOption, String optionLibDir) {
		try {
			File[] files = new File(optionLibDir).listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile() && pathname.getName().endsWith(".jar");
				}
			});
			
			if (files == null)
				throw new InternalManagerError();
			
			List<URL> list = new ArrayList<>();
			for (File f: files) {
				String path = f.getAbsolutePath();
				
				if (path.startsWith("/")) {
					path = "file:" + path;
				} else {
					path = "file:/" + path;
				}
				list.add(new URL(path));
			}

			addCloudOption(platformId, optionClass, dependOption, list.toArray(new URL[list.size()]));
		} catch (MalformedURLException e) {
			throw new InternalManagerError(e.getMessage(), e);
		}
	}
	
	public interface OptionCallable<T> {
		T call(ICloudOption option) throws CloudManagerException;
	}
	
	public interface OptionExecutor {
		void execute(ICloudOption option) throws CloudManagerException;
	}
	
	public <T> T optionCall(String platformId, OptionCallable<T> callable) throws CloudManagerException {
		ICloudOption cloudOption = getCloudOption(platformId);
		if (cloudOption == null) {
			String message = String.format("PlatformId(%s) is not related to CloudOption.", platformId);
			Logger.getLogger(this.getClass()).warn(message);
			throw new InternalManagerError(message);
		}
		OptionClassLoader loader = loaderMap.get(platformId);
		return optionCall(cloudOption, loader, callable);
	}
	
	protected <T> T optionCall(ICloudOption cloudOption, OptionClassLoader loader, OptionCallable<T> callable) throws CloudManagerException {
		ICloudOption prev = Session.current().get(ICloudOption.class);
		try {
			ClassLoader defaultLoader = null;
			if (loader != null) {
				defaultLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(loader);
			}
			
			try {
				Session.current().set(ICloudOption.class, cloudOption);
				return callable.call(cloudOption);
			} finally {
				if (loader != null) {
					Thread.currentThread().setContextClassLoader(defaultLoader);
				}
			}
		} finally {
			Session.current().set(ICloudOption.class, prev);
		}
	}

	public void optionExecute(String platformId, final OptionExecutor executor) throws CloudManagerException {
		optionCall(platformId, new OptionCallable<Object>() {
			@Override
			public Object call(ICloudOption option) throws CloudManagerException {
				executor.execute(option);
				return null;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public  void addCloudOption(String platformId, String optionClass, String dependOption, URL...urls) {
		try {
			ClassLoader base = dependOption == null ? CloudManager.class.getClassLoader(): loaderMap.get(dependOption);
			if (base == null)
				throw new InternalManagerError();

			// TODO Might cause duplicate problem. Should prevent loading same classes that parent already loaded
			OptionClassLoader loader = new OptionClassLoader(urls, base);

			ClassLoader defaultLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(loader);
			
			try {
				Class<? extends ICloudOption> optionClazz = (Class<? extends ICloudOption>)loader.loadClass(optionClass);

				ICloudOption option = optionClazz.newInstance();

				addCloudOption(platformId, option);
			} finally {
				Thread.currentThread().setContextClassLoader(defaultLoader);
			}
			
			loaderMap.put(platformId, loader);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new InternalManagerError(e.getMessage(), e);
		}
	}

	public void open() {
		// Webサービスの起動処理
		IWebServiceBase mainEndpoint = ObjRegistry.reg().get(IWebServiceBase.class, Key_MainEndpoint);
		// SEI を含んだ jar に、以下のコードを有効にして、CloudManagerWSFaetureFactory.java を jar に含めると(正確には、
		// CloudManagerWSFaetureFactory の import 文があると)、wsgen が失敗します。
		// おそらく java ランタイムの内部クラスを使用しているからだと思われます。
		// 本リリースでは、以下のコードは必要ないので、無効とします。
		// publish("/HinemosWS/CloudEndpoint", mainEndpoint == null ? new CloudEndpointImpl(): mainEndpoint, CloudManagerWSFaetureFactory.createWebServiceFeature());
		publish("/HinemosWS/CloudEndpoint", mainEndpoint == null ? new CloudEndpointImpl(): mainEndpoint);

		PlatformResourceMonitor.start();
		PlatformServiceConditionMonitor.start();
		BillingDetailMonitor.start();
		
		// クラウド監視を追加
		HinemosModuleConstant.addExtensionType(new HinemosModuleConstant.ExtensionType(PlatformServiceRunMonitor.monitorTypeId, PlatformServiceRunMonitor.monitorType, PlatformServiceRunMonitor.STRING_CLOUD_SERVICE_DONDITION));
		ObjectSharingService.objectRegistry().put(RunMonitor.class, PlatformServiceRunMonitor.monitorTypeId + "." + PlatformServiceRunMonitor.monitorType, PlatformServiceRunMonitor.class);

		// クラウド課金監視を追加
		HinemosModuleConstant.addExtensionType(new HinemosModuleConstant.ExtensionType(CloudServiceBillingRunMonitor.monitorTypeId, CloudServiceBillingRunMonitor.monitorType, CloudServiceBillingRunMonitor.STRING_CLOUD_SERVICE_BILLING));
		ObjectSharingService.objectRegistry().put(RunMonitor.class, CloudServiceBillingRunMonitor.monitorTypeId + "." + CloudServiceBillingRunMonitor.monitorType, CloudServiceBillingRunMonitor.class);

		// クラウド課金詳細監視を追加
		HinemosModuleConstant.addExtensionType(new HinemosModuleConstant.ExtensionType(CloudServiceBillingDetailRunMonitor.monitorTypeId, CloudServiceBillingDetailRunMonitor.monitorType, CloudServiceBillingDetailRunMonitor.STRING_CLOUD_SERVICE_BILLING_DETAIL));
		ObjectSharingService.objectRegistry().put(RunMonitor.class, CloudServiceBillingDetailRunMonitor.monitorTypeId + "." + CloudServiceBillingDetailRunMonitor.monitorType, CloudServiceBillingDetailRunMonitor.class);
	}

	public void close() {
		PlatformServiceConditionMonitor.stop();
		PlatformResourceMonitor.stop();

		publisher.close();
		
		List<ObjRegistry.Entry<ICloudOption>> options = ObjRegistry.reg().getAll(ICloudOption.class);
		for (ObjRegistry.Entry<ICloudOption> option: options) {
			option.getImplementor().stop();
			ObjRegistry.reg().remove(ICloudOption.class, option.getKey());
		}
		Iterator<OptionClassLoader> iter = loaderMap.values().iterator();
		while (iter.hasNext()) {
			try {
				iter.next().close();
			} catch (IOException e) {
			}
		}
	}
}
