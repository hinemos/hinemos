/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.port.protocol;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * DNSサービスが動作しているかを確認するクラスです。
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class ReachAddressDNS extends ReachAddressProtocol {

	private static Log m_log = LogFactory.getLog(ReachAddressDNS.class);

	/**
	 * DNSサービスが動作しているかをテストします
	 *
	 * @param addressText
	 * @return DNS監視ステータス
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.clustercontrol.port.protocol.ReachAddressProtocol#isRunning(java.
	 * lang.String)
	 */
	@Override
	protected boolean isRunning(String addressText) {

		m_message = "";
		m_messageOrg = "";
		m_response = -1;

		boolean isReachable = false;

		try {
			long start = 0; // 開始時間
			long end = 0; // 終了時間
			boolean retry = true; // リトライするか否か(true:する、false:しない)

			StringBuffer bufferOrg = new StringBuffer(); // オリジナルメッセージ
			String result = "";

			InetAddress address = InetAddress.getByName(addressText);
			String addressStr = address.getHostAddress();
			if(address instanceof Inet6Address){
				addressStr = "[" + addressStr + "]";
			}

			bufferOrg.append("Monitoring the DNS Service of "
					+ address.getHostName() + "[" + address.getHostAddress()
					+ "]:" + m_portNo + ".\n\n");

			Properties props = new Properties();
			props.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.dns.DnsContextFactory");
			props.put(Context.PROVIDER_URL, "dns://" + addressStr + ":" + m_portNo);
			props.put("com.sun.jndi.dns.timeout.initial", String.valueOf(m_timeout));
			props.put("com.sun.jndi.dns.timeout.retries", "1");

			InitialDirContext idctx = null;

			String hostname = HinemosPropertyCommon.monitor_port_protocol_dns.getStringValue();
			m_log.debug("The hostname from which to retrieve attributes is " + hostname);

			for (int i = 0; i < m_sentCount && retry; i++) {
				try {
					bufferOrg.append(HinemosTime.getDateString() + " Tried to Connect: ");

					start = HinemosTime.currentTimeMillis();

					idctx = new InitialDirContext(props);
					Attributes attrs = idctx.getAttributes(hostname);

					end = HinemosTime.currentTimeMillis();

					bufferOrg.append("\n");
					NamingEnumeration<? extends Attribute> allAttr = attrs.getAll();
					while (allAttr.hasMore()) {
						Attribute attr = allAttr.next();
						bufferOrg.append("Attribute: " + attr.getID() + "\n");
						NamingEnumeration<?> values = attr.getAll();
						while (values.hasMore())
							bufferOrg.append("Value: " + values.next() + "\n");
					}
					bufferOrg.append("\n");

					m_response = end - start;

					if (m_response > 0) {
						if (m_response < m_timeout) {
							result = result
									+ ("Response Time = " + m_response + "ms");
						} else {
							m_response = m_timeout;
							result = result
									+ ("Response Time = " + m_response + "ms");
						}
					} else {
						result = result + ("Response Time < 1ms");
					}

					retry = false;
					isReachable = true;

				} catch (NamingException e) {
					result = (e.getMessage() + "[NamingException]");
					retry = true;
					isReachable = false;
				} catch (Exception e) {
					result = (e.getMessage() + "[Exception]");
					retry = true;
					isReachable = false;
				} finally {
					bufferOrg.append(result + "\n");
					try {
						if (idctx != null) {
							idctx.close();
						}
					} catch (NamingException e) {
						m_log.warn("isRunning(): "
								+ "socket disconnect failed: " + e.getMessage(), e);
					}
				}

				if (i < m_sentCount - 1 && retry) {
					try {
						Thread.sleep(m_sentInterval);
					} catch (InterruptedException e) {
						break;
					}
				}
			}

			m_message = result + "(DNS/" + m_portNo + ")";
			m_messageOrg = bufferOrg.toString();
			return isReachable;
		} catch (UnknownHostException e) {
			m_log.debug("isRunning(): " + MessageConstant.MESSAGE_FAIL_TO_EXECUTE_TO_CONNECT.getMessage()
					+ e.getMessage());

			m_message = MessageConstant.MESSAGE_FAIL_TO_EXECUTE_TO_CONNECT.getMessage() + " ("
					+ e.getMessage() + ")";

			return false;
		}
	}
}
