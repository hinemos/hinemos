/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.util;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

import intel.management.wsman.ManagedInstance;
import intel.management.wsman.ManagedReference;
import intel.management.wsman.WsmanConnection;
import intel.management.wsman.WsmanException;
import intel.management.wsman.WsmanUtils;

public class WinRs {
	private static final String NAMESPACE_SHELL = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell";
	private static final String RESOURCE_URI_CMD = NAMESPACE_SHELL + "/cmd";
	
	private static Log m_log = LogFactory.getLog( WinRs.class ); 
	private String username;
	private String password;
	private String url;

	public WinRs(String host, int port, String protocol, String username, String password) {
		this.username = username;
		this.password = password;
		
		try {
			if (InetAddress.getByName(host) instanceof Inet6Address){
				if(!"https".equals(protocol)) {
					url = String.format("http://[%s]:%d/wsman", host, port);
				} else {
					url = String.format("https://[%s]:%d/wsman", host, port);
				}
			}
			else{
				if(!"https".equals(protocol)) {
					url = String.format("http://%s:%d/wsman", host, port);
				} else {
					url = String.format("https://%s:%d/wsman", host, port);
				}
			}
		} catch (UnknownHostException e) {
			m_log.warn("UnknownException " + e.getMessage(), e);
		}
	}

	public void cleanupCommand(String shellId, String commandId) throws WsmanException {
		WsmanConnection conn = createConnection();
		ManagedReference ref = conn.newReference(RESOURCE_URI_CMD);
		ref.addSelector("ShellId", shellId);
		
		ManagedInstance shellInst = ref.createMethod(NAMESPACE_SHELL, "Signal");
		shellInst.getBody().setAttribute("CommandId", commandId);
		shellInst.addProperty("Code", NAMESPACE_SHELL + "/signal/ctrl_c");
		
		ref.invoke(shellInst, NAMESPACE_SHELL + "/Signal");
	}

	public void closeShell(String shellId) throws WsmanException {
		WsmanConnection conn = createConnection();
		ManagedReference ref = conn.newReference(RESOURCE_URI_CMD);
		ref.addSelector("ShellId", shellId);
		ref.delete();
	}

	public WinRsCommandOutput getCommandOutput(String shellId, String commandId) throws WsmanException {
		WsmanConnection conn = createConnection();
		ManagedReference ref = conn.newReference(RESOURCE_URI_CMD);
		ref.addSelector("ShellId", shellId);
		
		ManagedInstance shellInst = ref.createMethod(NAMESPACE_SHELL, "Receive");
		Element property = shellInst.addProperty("DesiredStream", "stdout stderr");
		property.setAttribute("CommandId", commandId);
		ManagedInstance resp = ref.invoke(shellInst, NAMESPACE_SHELL + "/Receive");

		return getCommandOutputFromResponse(commandId, resp);
	}

	public String openShell() throws UnknownHostException, WsmanException {
		return openShell(1000, null);
	}
	
	public String openShell(int lifeTime, String workingDirectory) throws UnknownHostException, WsmanException {
		//lifetime: second
		WsmanConnection conn = createConnection();
		ManagedReference ref = conn.newReference(RESOURCE_URI_CMD);

		ManagedInstance shellInst = ref.createMethod(NAMESPACE_SHELL, "Shell");
		shellInst.addProperty("Lifetime", String.format("PT%dS", lifeTime));
		shellInst.addProperty("InputStreams", "stdin");
		shellInst.addProperty("OutputStreams", "stdout stderr");
		if (workingDirectory != null) {
			shellInst.addProperty("WorkingDirectory", workingDirectory);
		}

		ManagedInstance resp = ref.create(shellInst);
		NodeList nodeList = resp.getBody().getElementsByTagNameNS("http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", "Selector");
		Element shellIdElement = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element node = (Element)nodeList.item(i);
			if ("ShellId".equals(node.getAttribute("Name"))) {
				shellIdElement = node;
				break;
			}
		}
		if (shellIdElement == null) {
			return null;
		}
		
		return shellIdElement.getTextContent();
	}

	public String runCommand(String shellId, String command, String[] args) throws WsmanException {
		WsmanConnection conn = createConnection();
		ManagedReference ref = conn.newReference(RESOURCE_URI_CMD);
		ref.addSelector("ShellId", shellId);
		
		ManagedInstance shellInst = ref.createMethod(NAMESPACE_SHELL, "CommandLine");
		shellInst.addProperty("Command", command);
		for (String arg : args) {
			shellInst.addProperty("Arguments", arg);
		}
		
		ManagedInstance resp = ref.invoke(shellInst, NAMESPACE_SHELL + "/Command");
		
		Element commandIdNode = WsmanUtils.findChild(resp.getBody(), NAMESPACE_SHELL, "CommandId");
		return commandIdNode.getTextContent();
	}

	private WsmanConnection createConnection() {
		// コネクションと認証の設定
		WsmanConnection conn = WsmanConnection.createConnection(url);
		conn.setAuthenticationScheme("basic");
		conn.setUsername(username);
		conn.setUserpassword(password);
		conn.setTimeout(90000);
		
		boolean sslTrustall = HinemosPropertyCommon.infra_winrm_ssl_trustall.getBooleanValue();
		if(sslTrustall) {
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			
				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				}
			
				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				}
			};

			conn.setTrustManager(tm);
			conn.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
		} else {
			conn.setHostnameVerifier(HttpsSupport.getDefaultHostnameVerifier());
		}

		return conn;
	}
	
	private WinRsCommandOutput getCommandOutputFromResponse(String commandId,
			ManagedInstance resp) {
		
		StringBuilder stdout = new StringBuilder();
		StringBuilder stderr = new StringBuilder();
		long exitCode = 0;
		WinRsCommandState state = WinRsCommandState.Running;
		
		NodeList nodeList = resp.getBody().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element node = (Element)nodeList.item(i);
			if (!commandId.equals(node.getAttribute("CommandId"))) {
				continue;
			}

			if ("Stream".equals(node.getLocalName())) {
				if ("stdout".equals(node.getAttribute("Name"))) {
					stdout.append(new String(Base64.decodeBase64(node.getTextContent())));
				} else if ("stderr".equals(node.getAttribute("Name"))) {
					stderr.append(new String(Base64.decodeBase64(node.getTextContent())));
				}
			} else if ("CommandState".equals(node.getLocalName())) {
				if  (node.getAttribute("State").endsWith("Done")) {
					exitCode = Long.parseLong(node.getChildNodes().item(0).getTextContent());
					state = WinRsCommandState.Done;
				} else if (node.getAttribute("State").endsWith("Running")) {
					state = WinRsCommandState.Running;
				} else {
					state = WinRsCommandState.Pending;
				}
			}
		}
		
		return new WinRsCommandOutput(stdout.toString(), stderr.toString(), exitCode, state);
	}
}