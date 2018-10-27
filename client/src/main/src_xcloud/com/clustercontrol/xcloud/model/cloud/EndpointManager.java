/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointUnit.EndpointSetting;


public class EndpointManager implements IEndpointManager {
	
	private static final Log logger = LogFactory.getLog(EndpointManager.class);
	
	private String managerName;
	
	public EndpointManager(String managerName){
		this.managerName = managerName;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> T getEndpoint(final Class<T> clazz) {
		ClassLoader orgLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
			InvocationHandler h = new InvocationHandler() {
				List<EndpointSetting<T>> endpointSettings = getEndpointSetting(clazz);
	
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					ClassLoader orgLoader = Thread.currentThread().getContextClassLoader();
					try {
						Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
						// 以下の処理は、Hinemos クライアントから引用。
						// 以下の作りこみは、getEndpoint を クラウド クライアントから、エンドポイントを取得する統一的な方法にするため。
						// 基本的には、Hinemos クライアントで、エンドポイントを取得する方法と同じになるはず。
						WebServiceException wse = null;
						for (EndpointSetting<T> endpointSetting : endpointSettings) {
							try {
								Object endpoint = endpointSetting.getEndpoint();
								return method.invoke(endpoint, args);
							}
							catch (InvocationTargetException e) {
								if (!(e.getCause() instanceof WebServiceException)) {
									throw e.getCause();
								}
								
								logger.warn("calling " + method.getName(), e.getCause());
			
		//						com.clustercontrol.util.EndpointManager.changeEndpoint();
			
								wse = (WebServiceException)e.getCause();
							}
						}
						throw wse;
					} finally {
						Thread.currentThread().setContextClassLoader(orgLoader);
					}

				}
			};
			return (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, h);
		} finally {
			Thread.currentThread().setContextClassLoader(orgLoader);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<EndpointSetting<T>> getEndpointSetting(Class<T> clazz) {
		ClassLoader orgLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
			
			Class<? extends Service> serviceClass = null;
			
			//サービスクラスの名称がエンドポイントクラス名+Serviceであること前提
			//改善する場合は引数にServiceClassを追加する等
			String serviceClassName = clazz.getCanonicalName() + "Service";
			try {
				serviceClass = (Class<? extends Service>) clazz.getClassLoader().loadClass(serviceClassName);
			} catch (ClassNotFoundException e) {
				logger.debug(serviceClassName + " not found. " + e.getMessage());
			}
			
			List<EndpointSetting<T>> settings = null;
			if(serviceClass != null){
				settings = com.clustercontrol.util.EndpointManager.get(managerName).getEndpoint(serviceClass, clazz);
			}
			if(settings != null && settings.size() > 0){
				return settings;
			}
			throw new UnsupportedOperationException(); 
		} finally {
			Thread.currentThread().setContextClassLoader(orgLoader);
		}
	}
	
	@Override
	public String getAccountName() {
		return com.clustercontrol.util.EndpointManager.get(managerName).getUserId();
	}
}
