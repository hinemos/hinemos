/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.ws.xcloud;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.Invoker;
import javax.xml.ws.spi.Provider;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.plugin.impl.WebServicePlugin;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.ws.xcloud.security.HinemosAccessRight;
import com.clustercontrol.ws.xcloud.security.HinemosAccessRights;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.validation.ValidationUtil;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

public class Publisher implements AutoCloseable {

	public interface MethodChecker {
		void check(Method method, Object... args) throws PluginException;
	}

	public interface ContextListener {
		void onUpdateContext(WebServiceContext wsctx);
	}

	public interface UncaughtExceptionHandler {
		PluginException uncaughtException(Throwable e);
	}

	private class CloudInvoker extends Invoker {
		private IWebServiceBase implementor;
		private Field resourceField;
		private Endpoint endpoint;
		private WebServiceContext wsctx;

		public CloudInvoker(IWebServiceBase implementor) {
			assert implementor != null;
			for (Field f : implementor.getClass().getDeclaredFields()) {
				if (f.getAnnotation(Resource.class) != null) {
					this.resourceField = f;
					break;
				}
			}
			this.implementor = implementor;
		}

		@Override
		public void inject(WebServiceContext wsctx)
				throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			this.wsctx = wsctx;

			if (resourceField != null) {
				boolean flag = resourceField.isAccessible();
				resourceField.setAccessible(true);
				try {
					resourceField.set(implementor, wsctx);
				} finally {
					resourceField.setAccessible(flag);
				}
			}
		}

		@Override
		public Object invoke(Method method, Object... args)
				throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			Logger logger = Logger.getLogger(Publisher.class);
			logger.debug("calling " + method.getName());

			ClassLoader defaultLoader = Thread.currentThread().getContextClassLoader();
			try (SessionScope sessionScope = SessionScope.open()) {
				logger.debug("Calling " + method.getName());

				Thread.currentThread().setContextClassLoader(implementor.getClass().getClassLoader());

				long before = System.currentTimeMillis();

				context.set(wsctx);
				if (contextListener != null)
					contextListener.onUpdateContext(wsctx);

				HinemosAccessRight accessRight = method.getAnnotation(HinemosAccessRight.class);
				if (accessRight != null) {
					for (SystemPrivilegeMode systemPrivilege : accessRight.right()) {
						CloudUtil.authCheck(wsctx, new SystemPrivilegeInfo(accessRight.roleName(), systemPrivilege));
					}
				} else {
					HinemosAccessRights ars = method.getAnnotation(HinemosAccessRights.class);
					if (ars != null) {
						List<SystemPrivilegeInfo> spis = new ArrayList<SystemPrivilegeInfo>();
						for (HinemosAccessRight ar : ars.value()) {
							for (SystemPrivilegeMode systemPrivilege : ar.right()) {
								CloudUtil.authCheck(wsctx, new SystemPrivilegeInfo(ar.roleName(), systemPrivilege));
							}
						}
						if (!spis.isEmpty()) {
							CloudUtil.authCheck(wsctx, spis.toArray(new SystemPrivilegeInfo[0]));
						}
					}
				}

				ValidationUtil.getMethodValidator().validate(method, args);

				List<MethodChecker> checkerList = new ArrayList<>(checkers);
				for (MethodChecker checker : checkerList) {
					checker.check(method, args);
				}

				Object returnValue = method.invoke(implementor, args);

				context.set(null);

				long after = System.currentTimeMillis();

				StringBuilder sb = new StringBuilder();
				sb.append("Called ").append(method.getName()).append(" : ").append("elapsedTime=")
						.append(after - before).append("ms");
				if (args != null) {
					for (Object arg : args) {
						sb.append(", arg=").append(arg);
					}
				}
				logger.info(sb.toString());

				return returnValue;
			} catch (InvocationTargetException e) {
				if (e.getTargetException() instanceof CloudManagerException) {
					CloudManagerException cloudException = (CloudManagerException)e.getTargetException();
					if (!ErrorCode.UNEXPECTED.match(cloudException) && !ErrorCode.HINEMOS_MANAGER_ERROR.match(cloudException) && cloudException.getCause() == null) {
						if (logger.isDebugEnabled()) {
							logger.warn(HinemosMessage.replace(cloudException.getMessage()), e);
						} else {
							logger.warn(HinemosMessage.replace(cloudException.getMessage()));
						}
					} else {
						logger.warn(HinemosMessage.replace(cloudException.getMessage()), e);
					}
				} else {
					logger.warn(HinemosMessage.replace(e.getTargetException().getMessage()), e);
				}
				throw e;
			} catch (IllegalAccessException | IllegalArgumentException e) {
				logger.warn(e.getMessage(), e);
				throw e;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);

				if (handler != null) {
					PluginException fault = handler.uncaughtException(e);
					if (fault != null) {
						throw new InvocationTargetException(fault);
					}
				}

				if (e instanceof InternalManagerError) {
					throw (InternalManagerError) e;
				} else {
					throw new InternalManagerError(e);
				}
			} finally {
				Thread.currentThread().setContextClassLoader(defaultLoader);
			}
		}

		@SuppressWarnings("unused")
		public Endpoint getEndpoint() {
			return endpoint;
		}

		public void setEndpoint(Endpoint endpoint) {
			this.endpoint = endpoint;
		}

		public IWebServiceBase getImplementor() {
			return implementor;
		}
	};

	private List<CloudInvoker> endpoints = Collections.synchronizedList(new ArrayList<CloudInvoker>());
	private List<MethodChecker> checkers = Collections.synchronizedList(new ArrayList<MethodChecker>());
	private UncaughtExceptionHandler handler;

	private static ThreadLocal<WebServiceContext> context = new ThreadLocal<WebServiceContext>() {
		protected WebServiceContext initialValue() {
			return null;
		}
	};

	private static ContextListener contextListener;

	public static WebServiceContext getCurrentContext() {
		return context.get();
	}

	public Publisher() {
	}

	@SuppressWarnings("unchecked")
	public void publish(String address, IWebServiceBase implementor, WebServiceFeature... features) {
		URL url = null;
		try {
			url = new URL(address);
		} catch (Exception e) {
			throw new InternalManagerError(e);
		}

		if ("https".equals(url.getProtocol())) {
			Map<String, HttpsServer> httpsServerMap = null;
			{
				Field field = null;
				Boolean accesible = null;
				try {
					field = WebServicePlugin.class.getDeclaredField("httpsServerMap");

					accesible = field.isAccessible();
					field.setAccessible(true);
					httpsServerMap = (Map<String, HttpsServer>) field.get(null);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				} finally {
					if (accesible != null) {
						field.setAccessible(accesible);
					}
				}
			}

			ThreadPoolExecutor _threadPool = null;
			{
				Field field = null;
				Boolean accesible = null;
				try {
					field = WebServicePlugin.class.getDeclaredField("_threadPool");

					accesible = field.isAccessible();
					field.setAccessible(true);
					_threadPool = (ThreadPoolExecutor) field.get(null);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				} finally {
					if (accesible != null) {
						field.setAccessible(accesible);
					}
				}
			}

			CloudInvoker invoker = new CloudInvoker(implementor);
			implementor.start();
			Endpoint e = Provider.provider().createEndpoint(null, implementor.getClass(), invoker, features);
			e.setExecutor(_threadPool);

			String urlPrefix = "https://" + url.getHost() + ":" + url.getPort();

			HttpsServer server = httpsServerMap.get(urlPrefix);
			if (server == null) {
				try {
					// HTTPS
					// Serverの作成（HTTPSサーバの開始は、後で一括して行うため、ここではインスタンスの生成のみに留める
					// HinemosPropertyCommon;
					// HinemosPropertyDefault;

					String protocol = HinemosPropertyCommon.ws_https_protocol.getStringValue();
					String keystorePath = HinemosPropertyDefault.ws_https_keystore_path.getStringValue();
					String keystorePassword = HinemosPropertyCommon.ws_https_keystore_password.getStringValue();
					String keystoreType = HinemosPropertyCommon.ws_https_keystore_type.getStringValue();
					SSLContext ssl = SSLContext.getInstance(protocol);
					KeyManagerFactory keyFactory = KeyManagerFactory
							.getInstance(KeyManagerFactory.getDefaultAlgorithm());
					KeyStore store = KeyStore.getInstance(keystoreType);
					try (FileInputStream fi = new FileInputStream(keystorePath)) {
						store.load(fi, keystorePassword.toCharArray());
					}
					keyFactory.init(store, keystorePassword.toCharArray());
					TrustManagerFactory trustFactory = TrustManagerFactory
							.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					trustFactory.init(store);
					ssl.init(keyFactory.getKeyManagers(), trustFactory.getTrustManagers(), new SecureRandom());
					HttpsConfigurator configurator = new HttpsConfigurator(ssl);

					// 新規にHTTPSSeverを作って、Hashmapに登録する
					server = HttpsServer.create(new InetSocketAddress(url.getHost(), url.getPort()), 0);
					server.setHttpsConfigurator(configurator);
					httpsServerMap.put(urlPrefix, server);
				} catch (Exception e1) {
					throw new InternalManagerError(e1);
				}
			}

			e.publish(server.createContext(url.getPath()));
			invoker.setEndpoint(e);
			endpoints.add(invoker);
		} else {
			CloudInvoker invoker = new CloudInvoker(implementor);
			implementor.start();
			Endpoint e = Provider.provider().createEndpoint(null, implementor.getClass(), invoker, features);
			e.publish(address);
			invoker.setEndpoint(e);
			endpoints.add(invoker);
		}
	}

	@Override
	public void close() {
		synchronized (endpoints) {
			for (CloudInvoker ep : endpoints) {
				try {
					// ep.getEndpoint().stop();
				} catch (Exception e) {
					Logger logger = Logger.getLogger(Publisher.class);
					logger.error(e.getMessage(), e);
				}
				try {
					ep.getImplementor().stop();
				} catch (Exception e) {
					Logger logger = Logger.getLogger(Publisher.class);
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public void addChecker(MethodChecker checker) {
		checkers.add(checker);
	}

	public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
		this.handler = handler;
	}

	public synchronized static void setContextListener(ContextListener contextListener) {
		Publisher.contextListener = contextListener;
	}
}
