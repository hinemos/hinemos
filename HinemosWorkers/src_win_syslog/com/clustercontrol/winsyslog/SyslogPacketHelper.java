/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.winsyslog;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;

/*
 * syslog パケット用のユーティリティクラス。
 */
public class SyslogPacketHelper {
	private static Logger logger = Logger.getLogger(SyslogPacketHelper.class);

	// シスログヘッダのタイムスタンプのパターン
	public static final String timestampPattern = "MMM dd HH:mm:ss";

	public static final int timestampLen = timestampPattern.length();

	// 分割した場合に付加するヘッダーのプライオリティ。
	public static final String priority = "<13>";
	
	// syslogヘッダー Charset ascii固定
	private static final Charset hdrCharset = Charset.forName("ascii");

	// syslogメッセージ Charset デフォルトutf-8
	private Charset msgCharset = Charset.forName("utf-8");
	
	//ヘッダ補完（送信時刻）向けのタイムゾーン デフォルト固定
	private static final TimeZone headerTimeZone  =TimeZone.getDefault ();
	
	/*
	 * syslog ヘッダを作成する。
	 */
	public static String createSyslogHeader(Date date, String host,TimeZone tz) {
		// シスログヘッダー用データフォーマット。
		SimpleDateFormat dateFormat = new SimpleDateFormat(timestampPattern, Locale.ENGLISH);
		dateFormat.setTimeZone(tz);
		
		String header =  String.format("%s%s %s ", priority, dateFormat.format(date), host);
		// 日付が一桁の場合の対処。
		if (header.charAt(priority.length() + 3 + 1) == '0') {
			header = header.substring(0, priority.length() + 3 + 1) + ' ' + header.substring(priority.length() + 3 + 1 + 1);
		}
		return header;
	}
	
	public SyslogPacketHelper( Charset msgCharset) {
		this.msgCharset = msgCharset;
	}
	
	public SyslogPacketHelper() {
	}
	
	/*
	 * syslog パケットのヘッダとメッセージの境目のインデックスを取得。
	 * 
	 * RFC3164に準拠（syslogヘッダーは asciiのcharsetがmust）して、ascii前提の処理とする。
	 * 
	 * ヘッダの形式は <PRI>DATE HOSTANME 形式のみに対応（他のパターンは無視）
	 * 
	 * 見つからない場合は -1 を返却
	 * 
	 */
	public int searchSyslogMessage(byte[] packet) throws IOException {
		int index = -1;
		
		// プライオリティー
		// <PRI> の 先頭'<'の存在チェック 
		// <PRI> の '>' まで読み進める 
		for (int i = 0; i < packet.length; ++i) {
			byte b = packet[i];
			if ( i == 0 && b != '<') {
				break;
			}
			if (b == '>') {
				index = i;
				++index;
				break;
			}
		}
		if (index == -1) {
			return -1;
		}
		
		// タイムスタンプチェック
		// "MMM dd HH:mm:ss "を想定した
		//	・スペース（0x20 = Deci32）と コロン（0x3A=Deci58）の存在チェック 
		// 		ただし dd については 1桁(ゼロサプレス)と2桁の両ケースを考慮
		//  ・dd HH mm ss 部分の数値チェック 
		if (packet.length > index + timestampLen) {
			int nextIndex =0;
			if (packet[index + timestampLen - 1] == 32  //末尾の スペース
			 && packet[index + timestampLen - 10] == 32 //dd後の スペース
			 && packet[index + timestampLen - 12] == 32 //dd前の スペース
			 && packet[index + timestampLen - 4] == 58 //mm後の コロン
			 && packet[index + timestampLen - 7] == 58 //HH後の コロン
			) {
				//dd 1桁パターン
				nextIndex = index + timestampLen-1;
			} else if (packet[index + timestampLen] == 32 //末尾の スペース
				 && packet[index + timestampLen - 9] == 32 //dd後の スペース
				 && packet[index + timestampLen - 12] == 32 //dd前の スペース
				 && packet[index + timestampLen - 3] == 58 //mm後の コロン
				 && packet[index + timestampLen - 6] == 58 //HH後の コロン
				) {
				//dd 2桁パターン
				nextIndex = index + timestampLen;
			} else {
				//スペースとコロンの位置がおかしいのでNG
				return -1;
			}
			//' dd HH:mm:ss '部分 向け数値チェック
			for (int i = index + 3; i < nextIndex ; ++i) {
				int lastLen = nextIndex -i;
				if( lastLen == 3 || lastLen == 6  || lastLen == 9  || lastLen == 12 ){// 区切りのスペースかコロンの箇所はパス
					continue;
				}
				if(48 <= packet[i] && packet[i] <= 57 ){//数値
					continue;
				}
				if(lastLen == 11 && packet[i] == 32){//dd の先頭スペースは許可
					continue;
				}
				
				//想定した文字以外ならNG
				return -1;
			}
			index = nextIndex;
			++index;
			if (index >= packet.length) {
				return -1;
			}
		} else {
			return -1;
		}
		
		// ホスト名チェック、以下の場合ＮＧとする
		// ・次のスペースが検出できない。
		// ・次のスペース検出時、スペース直前にコロン（メッセージのTAG部想定）がある
		// ・次のスペース検出までに ホスト名かIPアドレスにはありえない文字が入る
		//     半角英数 . / - _ :   以外なら 全てＮＧ
		// ・次のスペース検出時、間に1文字もない
		boolean existHostname = false;
		for (int i = index; i < packet.length; ++i) {
			if(65 <= packet[i] && packet[i] <= 90 ){
				//大文字 A-Z
				continue;
			}
			if(97 <= packet[i] && packet[i] <= 122 ){
				 //小文字 a-z
				continue;
			}
			if(48 <= packet[i] && packet[i] <= 57 ){
				 //数値
				continue;
			}
			if(45 <= packet[i] && packet[i] <= 47 ){
				 // . / - 
				continue;
			}
			if(packet[i] == 58 || packet[i] == 95 ){
				// : _ 
				continue;
			}
			if (packet[i] == 32) { //次のスペース検出
				if(packet[i-1] != 58 && i > index){
					//スペースの直前が コロンではなく、間に１文字以上ある
					index = i;
					++index;
					existHostname=true;
				}
				break;
			}
			//ホスト名かIPアドレスにありえない文字の場合はNG
			break;
		}
		if( existHostname == false ){
			return -1;
		}
		return index;
		
	}

	/*
	 * syslog パケットをヘッダとメッセージに分解。
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
			logger.warn(e.getMessage(), e);
			return false;
		}
	}
	
	/*
	 * syslog ヘッダを作成する。
	 */
	public byte[] createSyslogHeaderArray(Date date, String host) {
		return createSyslogHeader(date, host,headerTimeZone).getBytes(hdrCharset);
	}
	
	public Charset getHdrCharset() {
		return hdrCharset;
	}
	
	public Charset getMsgCharset() {
		return msgCharset;
	}
}