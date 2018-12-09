/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.port.protocol;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;


/**
 * アドレスのポートに到達可能かどうか確認するクラスです。
 * 
 * @version 2.4.0
 * @since 2.4.0
 */
public class ReachAddressTCP extends ReachAddressProtocol {

	private static Log m_log = LogFactory.getLog(ReachAddressTCP.class);

	/**
	 * 指定されたアドレスが到達可能かどうかをテストします
	 * 
	 * @param addressText
	 * @return PORT監視ステータス
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

			StringBuffer bufferOrg = new StringBuffer(); // オリジナルメッセージ
			String result = "";

			// Reachability のチェック ICMP が使用される
			boolean retry = true;

			InetAddress address = InetAddress.getByName(addressText);

			bufferOrg.append("Monitoring the port of " + address.getHostName()
					+ "[" + address.getHostAddress() + "]:" + m_portNo
					+ ".\n\n");

			// ソケット
			Socket socket = null;

			for (int i = 0; i < m_sentCount && retry; i++) {
				try {
					// ソケットを生成
					socket = new Socket();
					InetSocketAddress isa = new InetSocketAddress(address,
							m_portNo);

					bufferOrg.append(HinemosTime.getDateString() + " Tried to Connect: ");
					start = HinemosTime.currentTimeMillis();
					socket.connect(isa, m_timeout);
					end = HinemosTime.currentTimeMillis();

					m_response = end - start;
					if (m_response > 0) {
						if (m_response < m_timeout) {
							result = ("Response Time = " + m_response + "ms");
						} else {
							m_response = m_timeout;
							result = ("Response Time = " + m_response + "ms");
						}
					} else {
						result = ("Response Time < 1ms");
					}
					retry = false;
					isReachable = true;
				} catch (BindException e) {
					result = (e.getMessage() + "[BindException]");
					retry = true;
					isReachable = false;
				} catch (ConnectException e) {
					result = (e.getMessage() + "[ConnectException]");
					retry = false;
					isReachable = false;
				} catch (NoRouteToHostException e) {
					result = (e.getMessage() + "[NoRouteToHostException]");
					retry = true;
					isReachable = false;
				} catch (PortUnreachableException e) {
					result = (e.getMessage() + "[PortUnreachableException]");
					retry = true;
					isReachable = false;
				} catch (IOException e) {
					result = (e.getMessage() + "[IOException]");
					retry = true;
					isReachable = false;
				} finally {
					bufferOrg.append(result + "\n");
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
							m_log.warn("isRunning(): " + "socket close failed: " + e.getMessage(), e);
						}
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

			m_message = result + "(TCP/" + m_portNo + ")";
			m_messageOrg = bufferOrg.toString();
			return isReachable;
		} catch (UnknownHostException e) {
			m_log.debug("isRunning(): "
					+ MessageConstant.MESSAGE_FAIL_TO_EXECUTE_TO_CONNECT.getMessage() + e.getMessage());

			m_message = MessageConstant.MESSAGE_FAIL_TO_EXECUTE_TO_CONNECT.getMessage() + " ("
					+ e.getMessage() + ")";

			return false;
		}
	}
}
