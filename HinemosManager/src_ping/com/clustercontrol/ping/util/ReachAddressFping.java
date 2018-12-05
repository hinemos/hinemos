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

import java.util.ArrayList;
import java.util.HashSet;

import com.clustercontrol.ping.util.PingProperties;
import com.clustercontrol.platform.ping.FPingUtils;
/**
 * アドレスが到達可能かどうか確認するクラスです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ReachAddressFping {

	/** 送信回数 */
	private int m_sentCount
	= PingProperties.getFpingCount() ;

	/** 送信間隔（ミリ秒） */
	private int m_sentInterval = PingProperties.getFpingInterval() ;

	/** タイムアウト（ミリ秒） */
	private int m_timeout = PingProperties.getFpingTimeout() ;

	/** 送信データサイズ(byte)*/
	private int m_bytes = PingProperties.getFpingBytes();

	/**実行結果のエラーメッセージ**/
	private ArrayList<String> m_errMsg;

	/**実行結果のメッセージ**/
	private ArrayList<String> m_resultMsg;


	/**
	 * コンストラクタ
	 */
	public ReachAddressFping(int sentNum, int sentInterval, int timeout){

		m_sentCount = sentNum;
		m_sentInterval = sentInterval; // sec  to msec
		m_timeout = timeout;

		PingProperties.getProperties();
	}

	/**
	 * アドレスを取得し、到達可能かどうかをテストします
	 * 
	 * @param info
	 * @return PING監視ステータス
	 */
	public boolean isReachable(HashSet<String> hosts, int version) {
		
		m_resultMsg = new ArrayList<String>();
		m_errMsg = new ArrayList<String>();
		
		return new FPingUtils().fping(
				hosts, version, m_sentCount, m_sentInterval, m_timeout, m_bytes, m_resultMsg, m_errMsg);
		
	}

	public  ArrayList<String> getM_errMsg() {
		return m_errMsg;
	}

	public ArrayList<String> getM_resultMsg() {
		return m_resultMsg;
	}
}
