/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalInfoUtil {

	private static final Log m_log = LogFactory.getLog( LocalInfoUtil.class );

	public static List<String> getInternalIpAddressList() {
		ArrayList<String> ipAddressList = new ArrayList<String>();

		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			if (null != networkInterfaces) {
				while (networkInterfaces.hasMoreElements()) {
					NetworkInterface ni = networkInterfaces.nextElement();
					Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						InetAddress in = inetAddresses.nextElement();
						String hostAddress = Ipv6Util.expand(in.getHostAddress());
						if (hostAddress != null && !hostAddress.equals("127.0.0.1") &&
								!hostAddress.startsWith("0:0:0:0:0:0:0:1") &&
								!hostAddress.equals("::1")) {
							ipAddressList.add(hostAddress);
							m_log.debug("getInternalIpAddressList() Add IP Address : ipAddress=" + hostAddress);
						}
					}
				}
			}
		} catch (SocketException e) {
			m_log.warn("failed getting internal ip address.", e);
		}

		return ipAddressList;
	}
}
