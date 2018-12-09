/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/*
 * Syslog パケット用のユーティリティクラス。
 */
public class SyslogPacketHelper {
	private static Logger m_log = Logger.getLogger(SyslogPacketHelper.class);
	// シスログヘッダのタイムスタンプのパターン
	public static final String timestampPattern = "MMM dd HH:mm:ss";
	
	// 分割した場合に付加するヘッダーのプライオリティ。
	public static final String priority = "<13>";
	
	// 文字セット。
	private Charset hdrCharset = Charset.forName("ascii");
	private Charset msgCharset = Charset.forName("utf-8");
	
	// ASCII 互換文字セット一覧。
	private static final Set<Charset> ACII_COMPATIBLE = new HashSet<>();
	
	static {
		try {
			ACII_COMPATIBLE.add(Charset.forName("ascii"));
			ACII_COMPATIBLE.add(Charset.forName("utf-8"));
			ACII_COMPATIBLE.add(Charset.forName("shift-jis"));
			ACII_COMPATIBLE.add(Charset.forName("MS932"));
		} catch(RuntimeException e) {
			Logger.getLogger(SyslogPacketHelper.class).warn(e.getMessage(), e);
		}
	}
	
	public SyslogPacketHelper(Charset hdrCharset, Charset msgCharset) {
		this.hdrCharset = hdrCharset;
		this.msgCharset = msgCharset;
	}
	
	public SyslogPacketHelper() {
	}
	
	/*
	 * Syslog パケットのヘッダとメッセージの境目のインデックスを取得。
	 */
	public int searchSyslogMessage(byte[] packet) throws IOException {
		if (ACII_COMPATIBLE.contains(hdrCharset)) {
			int index = -1;
			
			// プライオリティー
			for (int i = 0; i < packet.length; ++i) {
				byte b = packet[i];
				if (b == '>') {
					index = i;
					++index;
					break;
				}
			}
			
			if (index == -1) {
				return -1;
			}
			
			// タイムスタンプ
			// 日付が一桁で、十の位が空白になっていないパケットを考慮した解析
			if (packet.length > index + timestampPattern.length()) {
				if (packet[index + timestampPattern.length() - 1] == 32) {
					index = index + timestampPattern.length() - 1;
					++index;
					
					if (index >= packet.length) {
						return -1;
					}
				} else if (packet[index + timestampPattern.length()] == 32) {
					index = index + timestampPattern.length();
					++index;
					
					if (index >= packet.length) {
						return -1;
					}
				} else {
					return -1;
				}
			} else {
				return -1;
			}
			
			// ホスト名
			for (int i = index; i < packet.length; ++i) {
				byte b = packet[i];
				if (b == 32) {
					index = i;
					++index;
					break;
				}
			}
			
			return index;
		} else {
			try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(packet), hdrCharset)) {
				StringBuilder buf = new StringBuilder();
				
				// プライオリティー
				while (true) {
					int c = reader.read();
					if (c == '>') {
						buf.append((char)c);
						break;
					} else if (c == -1) {
						return -1;
					} else {
						buf.append((char)c);
					}
				}
				
				// タイムスタンプ
				for (int i = 0; i < timestampPattern.length() - 1; ++i) {
					int c = reader.read();
					if (c == -1) {
						return -1;
					} else {
						buf.append((char)c);
					}
				}
				
				// 日付が一桁で、十の位が空白になっていないパケットを考慮した解析
				{
					int c = reader.read();
					if (c == ' ') {
						buf.append((char)c);
					} else if (c == -1) {
						return -1;
					} else {
						buf.append((char)c);
						
						c = reader.read();
						if (c == ' ') {
							buf.append((char)c);
						} else {
							return -1;
						}
					}
				}
				
				// ホスト名
				while (true) {
					int c = reader.read();
					if (c == ' ') {
						buf.append((char)c);
						return buf.toString().getBytes(hdrCharset).length;
					} else if (c == -1) {
						return packet.length;
					} else {
						buf.append((char)c);
					}
				}
			}
		}
	}

	/*
	 * Syslog パケットをヘッダとメッセージに分解。
	 */
	public String[] splitSyslogMessage(byte[] packet) throws IOException {
		int index = searchSyslogMessage(packet);
		if (index != -1) {
			String headerStr = new String(packet, 0, index, hdrCharset);
			String messageStr = new String(packet, index, packet.length - index, msgCharset);
			return new String[]{headerStr, messageStr};
		} else {
			return null;
		}
	}

	/*
	 * バイト配列が syslog ヘッダーから開始しているかどうか？
	 */
	public boolean containsHeader(byte[] packet) {
		try {
			return searchSyslogMessage(packet) != -1;
		} catch (IOException e) {
			m_log.warn(e.getMessage(), e);
			return false;
		}
	}
	
	public Charset getHdrCharset() {
		return hdrCharset;
	}
	
	public Charset getMsgCharset() {
		return msgCharset;
	}
}