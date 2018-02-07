/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ping.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.platform.ping.FpingCommand;

/**
 * アドレスが到達可能かどうか確認するクラスです。
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ReachAddressFping {

	private static Log m_log = LogFactory.getLog( ReachAddressFping.class );

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


		Process process = null ;//fpingプロセス
		int	m_exitValue = 0; //fpingコマンドの戻り値

		String fpingPath;
		if(version== 6){
			fpingPath = PingProperties.getFping6Path();
		}else{
			fpingPath = PingProperties.getFpingPath();
		}
		String cmd[] = FpingCommand.getCommand(fpingPath, hosts, m_sentCount, m_sentInterval, m_timeout, m_bytes);

		try {
			process = Runtime.getRuntime().exec(cmd);

			if(process != null){
				//標準出力、エラー出力読み取り開始
				StreamReader errStreamReader = new StreamReader(process.getErrorStream());
				errStreamReader.start();
				StreamReader inStreamReader = new StreamReader(process.getInputStream());
				inStreamReader.start();


				//				コマンド実行待機
				m_exitValue= process.waitFor();

				//標準出力取得

				inStreamReader.join();
				m_resultMsg = inStreamReader.getResult();
				m_log.debug("isReachable() STDOUT :" + inStreamReader.getResult().toString());

				errStreamReader.join();
				m_errMsg = errStreamReader.getResult();
				m_log.debug("isReachable() STDERR :" +errStreamReader.getResult().toString());


			}
		} catch (IOException e) {
			m_errMsg = new ArrayList<String>();
			m_errMsg.add(e.getMessage());
			m_log.warn("isReachable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			return false;

		}
		catch (InterruptedException e) {
			m_errMsg = new ArrayList<String>();
			m_errMsg.add(e.getMessage());
			m_log.warn("isReachable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			return false;
		}
		finally{
			if(process != null){
				process.destroy();
			}
		}
		if(m_exitValue == 0 || m_exitValue == 1){
			m_log.debug("exit value = " + m_exitValue);

			//fpingの戻り値が0ならば成功を返す
			//fpingは対象ノードがunreachableだと1をかえすので、1でも正常終了とする。
			return true;
		}else{
			String errMsg = "fping exit value is not 0 or 1. exit value = " + m_exitValue;
			m_log.info(errMsg);
			for(int j=0; j<cmd.length; j++) {
				m_log.info("cmd[" + j + "] = " + cmd[j]);
			}

			m_errMsg = new ArrayList<String>();
			m_errMsg.add(errMsg);

			//fpingの戻り値が!0 or !1ならば失敗を返す
			return false;
		}
	}


	/**
	 * 実行プロセスの標準、エラー出力読み取りスレッド
	 * コンストラクタで渡されたストリームを読み出し、文字列として格納する
	 */
	static class StreamReader extends Thread {

		private BufferedReader m_br;
		private ArrayList<String> m_ret;
		private InputStream m_ist;

		/**
		 * コンストラクタ
		 * @param ist 入力ストリーム
		 */
		public StreamReader(InputStream ist) {
			super();
			m_br = new BufferedReader(new InputStreamReader(ist));
			m_ist = ist;
			m_ret = new ArrayList<String>();
		}


		@Override
		public void run() {


			try {
				while(true){

					String outputString = m_br.readLine();

					if(outputString != null){
						m_ret.add(outputString);
					}
					else{
						break;
					}

				}

			} catch (IOException e) {
				m_log.warn("run() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
			try {

				/**
				 * コネクションクローズ
				 */
				m_ist.close();

			} catch (IOException e) {
				m_log.warn("isReachable() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

		}
		/**
		 * 読み出し結果のArrayList
		 * @return ストリームから読み出したStringを一行毎に入れたArrayList
		 */
		public ArrayList<String> getResult() {
			return m_ret;
		}
	}

	public  ArrayList<String> getM_errMsg() {
		return m_errMsg;
	}

	public ArrayList<String> getM_resultMsg() {
		return m_resultMsg;
	}
}
