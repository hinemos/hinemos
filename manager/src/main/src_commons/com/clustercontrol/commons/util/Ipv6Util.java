/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Ipv6Util {
	private static Log m_log = LogFactory.getLog( Ipv6Util.class );

	/**
	 * 同一のIpv6アドレスであるか確認するため、表記を変換する。
	 * 例)
	 * fe80::250:56ff:fe9d:0
	 * fe80:0:0:0:250:56ff:fe9d:0
	 * fe80::250:56ff:fe9d:0%2
	 * ↓
	 * fe80:0:0:0:250:56ff:fe9d:0
	 * 
	 * @param ipv6
	 * @return
	 */
	public static String expand(String ipv6) {
		if (ipv6 == null) {
			return ipv6;
		}
		// IPv4だと何もしない。
		try{
			InetAddress address = InetAddress.getByName(ipv6);
			if (!(address instanceof Inet6Address)){
				return ipv6;
			}
		} catch (UnknownHostException e) {
			m_log.info("expandIpv6 (" + ipv6 + ")" + e.getMessage());
			return ipv6;
		}

		int endIndex = ipv6.indexOf("%");
		if (endIndex > 0) {
			ipv6 = ipv6.substring(0, endIndex);
		}
		try {
			ipv6 = InetAddress.getByName(ipv6).getHostAddress();
		} catch (UnknownHostException e) {
			m_log.info("expandIpv6 [" + ipv6 + "]" + e.getMessage());
		}
		return ipv6;
	}

	public static void main (String args[]) {
		System.out.println(Ipv6Util.expand("fe80::250:56ff:fe9d:0"));
		System.out.println(Ipv6Util.expand("fe80:0:0:0:250:56ff:fe9d:0"));
		System.out.println(Ipv6Util.expand("fe80::250:56ff:fe9d:0%2"));
		System.out.println(Ipv6Util.expand("fe80::250:56ff:fe9d:123456"));
		System.out.println(Ipv6Util.expand("192.168.1.2"));
	}
}
