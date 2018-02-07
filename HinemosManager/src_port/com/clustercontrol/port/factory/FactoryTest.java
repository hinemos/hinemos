/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.port.factory;

import java.net.MalformedURLException;

import com.clustercontrol.port.protocol.ReachAddressProtocol;

public class FactoryTest {

	public static void main(String args[]) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException, MalformedURLException {
		ReachAddressProtocol m_reachability = null;
		String protocolClassName = "com.clustercontrol.port.protocol.ReachAddressTCP";

		String[] address = { "172.20.68.22", "172.20.68.22", "172.20.68.2222",
		"172.20.68.222" };
		int[] port = { 22, 100, 22, 22 };
		int[] runCount = { 2, 3, 4, 7 };
		int[] runInterval = { 1000, 2000, 3000, 1000 };
		int[] timeout = { 1000, 2000, 3000, 1000 };

		Class<?> cls = Class.forName(protocolClassName);
		m_reachability = (ReachAddressProtocol)cls.newInstance();

		for (int i = 0; i < address.length; i++) {
			System.out.println("count = " + (i + 1));

			m_reachability.setPortNo(port[i]);
			m_reachability.setSentCount(runCount[i]);
			m_reachability.setSentInterval(runInterval[i]);
			m_reachability.setTimeout(timeout[i]);

			boolean b = m_reachability.isReachable(address[i], address[i]);
			System.out.println("Message: \n"    + m_reachability.getMessage());
			System.out.println("MessageOrg: \n" + m_reachability.getMessageOrg());
			System.out.println("isReachable: " + b);
			System.out.println("--------------------------------------------------------");
		}
	}

}
