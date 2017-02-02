package com.clustercontrol.util;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class ClientProxyManager {

	public static void init() {
		ProxySelector.setDefault(new ClientProxySelector());
		Authenticator.setDefault(new ClientAuthenticatorSelector());
	}
	
	static class ClientProxySelector extends ProxySelector {
		@Override
		public List<Proxy> select(URI uri) {
			return Collections.singletonList(EndpointManager.getProxy());
		}
		@Override
		public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
			// TODO Auto-generated method stub
		}
	}
	
	static class ClientAuthenticatorSelector extends Authenticator {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return EndpointManager.getAuthenticator();
		}
	}

}
