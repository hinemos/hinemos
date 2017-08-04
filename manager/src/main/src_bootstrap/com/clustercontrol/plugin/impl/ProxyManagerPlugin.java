/*
Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */

package com.clustercontrol.plugin.impl;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.api.HinemosPlugin;

public class ProxyManagerPlugin implements HinemosPlugin {
	
	public static final Log log = LogFactory.getLog(ProxyManagerPlugin.class);
	
	private static final Map<String, ProxyEntry> _hostProxyMap = new ConcurrentHashMap<String, ProxyEntry>();
	
	private static final String _proxyHost;
	private static final Integer _port;
	private static final String _user;
	private static final String _password;
	private static final List<String> _ignoreHostList;
	
	static {
		String proxyHost = null;
		Long port = null;
		String user = null;
		String password = null;
		String ignoreHostStr = null;
		List<String> ignoreHostList = new ArrayList<String>();
		
		try {
			proxyHost = HinemosPropertyUtil.getHinemosPropertyStr("http.proxy.host", null);
			port = HinemosPropertyUtil.getHinemosPropertyNum("http.proxy.port", null);
			user = HinemosPropertyUtil.getHinemosPropertyStr("http.proxy.user", null);
			password = HinemosPropertyUtil.getHinemosPropertyStr("http.proxy.password", null);
			ignoreHostStr = HinemosPropertyUtil.getHinemosPropertyStr("http.proxy.ignorehosts", null);
			
			if (proxyHost != null && port != null) {
				log.info("initializing http proxy : proxyHost = " + proxyHost + ", port = " + port);
				if (ignoreHostStr != null) {
					ignoreHostList = Arrays.asList(ignoreHostStr.split(","));
				}
			}
		} catch (Throwable t) {
			log.warn("invalid proxy configuration.", t);
		} finally {
			_proxyHost = proxyHost;
			_port = port == null ? null : port.intValue();
			_user = user;
			_password = password;
			_ignoreHostList = ignoreHostList;
		}
	}
	
	private static class ProxyEntry {
		public final String proxyHost;
		public final int port;
		public final String user;
		public final String password;
		public ProxyEntry(String proxyHost, int port, String user, String password) {
			this.proxyHost = proxyHost;
			this.port = port;
			this.user = user;
			this.password = password;
		}
	}
	
	private static class HinemosProxySelector extends ProxySelector {
		private final ProxySelector selector;
		
		public HinemosProxySelector(ProxySelector selector) {
			this.selector = selector;
		}
		
		@Override
		public List<Proxy> select(URI uri) {
			String host = uri.getHost();
			// IPv6
			// uri.getHost()=null, uri.toString()=socket://%5b2001:380:615:98:0:0:0:204%5d:22
			// IPv4
			// uri.getHost=127.0.0.1, uri.toString=socket://127.0.0.1:22
			log.debug("uri.getHost=" + uri.getHost() + ", uri.toString=" + uri.toString());

			if (host == null) {
				Matcher m = Pattern.compile(".*%5b(.*)%5d.*", Pattern.DOTALL).matcher(uri.toString());
				if (m.matches()) {
					host = m.group(1);
				}
				log.debug("matched host=" + host);
			}

			if (host != null) {
				ProxyEntry entry = _hostProxyMap.get(host);
				if (entry != null) {
					log.debug("Use HTTP proxy " + _proxyHost);
					Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(entry.proxyHost, entry.port));
					return Arrays.asList(proxy);
				}
			}
			for (String ignoreHost : _ignoreHostList) {
				if (ignoreHost.equals(uri.getHost())) {
					return selector.select(uri);
				}
			}

			if (_proxyHost == null || _port == null) {
				log.debug("No proxy");
				return selector.select(uri);
			}else{
				log.debug("Use HTTP proxy " + _proxyHost);
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(_proxyHost, _port));
				return Arrays.asList(proxy);
			}
		}

		@Override
		public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
			log.warn("connection failure : uri = " + uri, ioe);
		}
	}
	
	private static class HinemosAuthenticator extends Authenticator {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			ProxyEntry entry = _hostProxyMap.get(getRequestingURL().getHost());
			if (entry != null) {
				return new PasswordAuthentication(entry.user, entry.password.toCharArray());
			}
			for (String ignoreHost : _ignoreHostList) {
				if (ignoreHost.equals(getRequestingURL().getHost())) {
					return null;
				}
			}
			if (_user != null && _password != null) {
				return new PasswordAuthentication(_user, _password.toCharArray());
			} else {
				return null;
			}
		}
	}

	public static void addEntry(String proxyHost, int port, String user, String password, String... hosts) {
		for (String host : hosts) {
			log.debug("addEntry host=" + host + ", proxyHost=" + proxyHost);
			_hostProxyMap.put(host, new ProxyEntry(proxyHost, port, user, password));
		}
	}

	public static void removeEntry(String... hosts) {
		for (String host : hosts) {
			log.debug("removeEntry host=" + host);
			_hostProxyMap.remove(host);
		}
	}

	@Override
	public Set<String> getDependency() {
		return new HashSet<String>();
	}

	@Override
	public void create() { }

	@Override
	public void activate() {
		ProxySelector.setDefault(new HinemosProxySelector(ProxySelector.getDefault()));
		Authenticator.setDefault(new HinemosAuthenticator());
	}

	@Override
	public void deactivate() { }

	@Override
	public void destroy() { }

}
