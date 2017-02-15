/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.port.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;


/**
 * FTPサービスが動作しているかを確認するクラスです。
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class ReachAddressFTP extends ReachAddressProtocol {

	private static Log m_log = LogFactory.getLog(ReachAddressFTP.class);

	/**
	 * FTPサービスが動作しているかをテストします
	 * 
	 * @param addressText
	 * @return FTP監視ステータス
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

			bufferOrg.append("Monitoring the FTP Service of "
					+ address.getHostName() + "[" + address.getHostAddress()
					+ "]:" + m_portNo + ".\n\n");

			FTPClient client = new FTPClient();

			for (int i = 0; i < m_sentCount && retry; i++) {
				try {
					bufferOrg.append(HinemosTime.getDateString() + " Tried to Connect: ");
					client.setDefaultTimeout(m_timeout);

					start = HinemosTime.currentTimeMillis();
					client.connect(address, m_portNo);
					end = HinemosTime.currentTimeMillis();

					m_response = end - start;

					result = client.getReplyString();

					int reply = client.getReplyCode();

					if (FTPReply.isPositiveCompletion(reply)) {
						if (m_response > 0) {
							if (m_response < m_timeout) {
								result = result + ("\n" + "Response Time = " + m_response + "ms");
							} else {
								m_response = m_timeout;
								result = result + ("\n" + "Response Time = " + m_response + "ms");
							}
						} else {
							result = result + ("\n" + "Response Time < 1ms");
						}

						retry = false;
						isReachable = true;
					} else {
						retry = false;
						isReachable = false;
					}

				} catch (SocketException e){
					result = (e.getMessage() + "[SocketException]");
					retry = true;
					isReachable = false;
				} catch (IOException e) {
					result = (e.getMessage() + "[IOException]");
					retry = true;
					isReachable = false;
				} finally {
					bufferOrg.append(result + "\n");
					if (client.isConnected()) {
						try {
							client.disconnect();
						} catch (IOException e) {
							m_log.warn("isRunning(): "
									+ "socket disconnect failed: " + e.getMessage(), e);
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

			m_message = result + "(FTP/" + m_portNo + ")";
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
