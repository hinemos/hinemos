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

package com.clustercontrol.ping.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.ping.bean.PingRunCountConstant;
import com.clustercontrol.ping.bean.PingRunIntervalConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * アドレスが到達可能かどうか確認するクラスです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ReachAddress {

	private static Log m_log = LogFactory.getLog( ReachAddress.class );

	/** 送信回数 */
	private int m_sentCount = PingRunCountConstant.TYPE_COUNT_01;

	/** 送信間隔（ミリ秒） */
	private int m_sentInterval = PingRunIntervalConstant.TYPE_SEC_01;

	/** タイムアウト（ミリ秒） */
	private int m_timeout = 1000;

	/** メッセージ */
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/** パケット紛失率（%） */
	private int m_lost = 0;

	/** 応答平均時間（ミリ秒） */
	private long m_average = 0;

	// isReachableがスレッドセーフではないため、同期をとるために使用
	private static Object m_syncObj = new Object();

	/**
	 * コンストラクタ
	 */
	public ReachAddress(int sentNum, int sentInterval, int timeout){

		m_sentCount = sentNum;
		m_sentInterval = sentInterval;
		m_timeout = timeout;
	}

	/**
	 * アドレスを取得し、到達可能かどうかをテストします
	 * 
	 * @param info
	 * @return PING監視ステータス
	 */
	public boolean isReachable(String ipNetworkNumber, String nodeName) {

		String addressText = null;
		if(ipNetworkNumber != null && !"".equals(ipNetworkNumber)){
			addressText = ipNetworkNumber;
		}
		else if(nodeName != null && !"".equals(nodeName)){
			addressText = nodeName;
		}
		else{
			m_log.debug("isReachable(): " + MessageConstant.MESSAGE_NOT_REGISTER_IN_REPOSITORY.getMessage());
			m_message = MessageConstant.MESSAGE_NOT_REGISTER_IN_REPOSITORY.getMessage();
			m_messageOrg = null;
			return false;
		}

		boolean result = this.isReachable(addressText);
		return result;
	}

	/**
	 * 指定されたアドレスが到達可能かどうかをテストします
	 * 
	 * @param addressText
	 * @return PING監視ステータス
	 */
	private boolean isReachable(String addressText) {

		m_message = null;
		m_messageOrg = null;
		m_lost = 0;
		m_average = 0;

		try {
			long max = 0;
			long min = 0;
			long sum = 0;
			int num = 0;
			long start = 0;
			long end = 0;

			// オリジナルメッセージ
			StringBuffer buffer = new StringBuffer();

			InetAddress address = InetAddress.getByName(addressText);
			buffer.append("Pinging " + address.getHostName() + " [" + address.getHostAddress() + "].\n\n");

			int i = 0;
			for (; i < m_sentCount; i++) {

				// Reachability のチェック ICMP が使用される
				boolean isReachable;
				// isReachableがスレッドセーフではないため、同期
				synchronized (m_syncObj) {
					start = HinemosTime.currentTimeMillis();
					isReachable = address.isReachable(m_timeout);
					end = HinemosTime.currentTimeMillis();
				}

				long time = end - start;

				if (isReachable) {
					buffer.append("Reply from " + address.getHostAddress() + ": ");

					sum += time;

					if (i == 0) {
						max = time;
						min = time;
					} else {
						if (time > max) {
							max = time;
						} else if (time < min) {
							min = time;
						}
					}
					num++;

					if (time > 0) {
						buffer.append("time=" + time + "ms\n");
					} else {
						buffer.append("time<1ms\n");
					}
				} else {
					if (time >= m_timeout) {
						buffer.append("Request timed out.\n");
					} else {
						buffer.append("Reply from " + address.getHostAddress() + ": Destination net unreachable.\n");
						//                        num++;
					}
				}

				if(i < m_sentCount-1){
					try {
						Thread.sleep(m_sentInterval);
					} catch (InterruptedException e) {
						break;
					}
				}
			}

			buffer.append("\nPing statistics for " + address.getHostAddress() + ":\n");
			//パケット紛失率
			if (num == 0) {
				m_lost = 100;
			} else {
				m_lost = (i - num) * 100 / i;
			}

			//メッセージ
			m_message = "Packets: Sent = " + i +
					", Received = " + num +
					", Lost = " + (i-num) + " (" + m_lost + "% loss),";

			buffer.append("\t" + m_message + "\n");

			buffer.append("Approximate round trip times in milli-seconds:\n");

			// 応答平均時間（ミリ秒）
			if (num != 0) {
				m_average = sum / num;
			} else {
				m_average = 0;
			}

			buffer.append("\tMinimum = " + min
					+ "ms, Maximum = " + max
					+ "ms, Average = " + m_average + "ms\n");

			m_messageOrg = buffer.toString();
			return true;

		} catch (UnknownHostException e) {
			m_log.warn("isReachable() " + MessageConstant.MESSAGE_FAIL_TO_EXECUTE_PING.getMessage()
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);

			m_message = MessageConstant.MESSAGE_FAIL_TO_EXECUTE_PING.getMessage() + " (" + e.getMessage() + ")";
		} catch (IOException e) {
			m_log.warn("isReachable() " + MessageConstant.MESSAGE_FAIL_TO_EXECUTE_PING.getMessage()
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);

			m_message = MessageConstant.MESSAGE_FAIL_TO_EXECUTE_PING.getMessage() + " (" + e.getMessage() + ")";
		}
		return false;
	}

	/**
	 * @return メッセージを戻します。
	 */
	public String getMessage() {
		return m_message;
	}
	/**
	 * @return オリジナルを戻します。
	 */
	public String getMessageOrg() {
		return m_messageOrg;
	}
	/**
	 * @return パケット紛失率(％)を戻します。
	 */
	public int getLost() {
		return m_lost;
	}
	/**
	 * @return 応答時間平均（ミリ秒）を戻します。
	 */
	public long getAverage() {
		return m_average;
	}
}
