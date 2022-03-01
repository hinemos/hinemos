/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.ping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.clustercontrol.ping.util.PingProperties;

/**
 * Fping実行環境差分ロジック（RHEL）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class FPingUtils {
	
	private static Logger m_log = Logger.getLogger(FPingUtils.class);
	
	public boolean fping(
			HashSet<String> hosts, 
			int version, 
			int sentCount,
			int sentInterval,
			int timeout,
			int bytes,
			final ArrayList<String> result, 
			final ArrayList<String> error) {
		
		Process process = null ;//fpingプロセス
		int	m_exitValue = 0; //fpingコマンドの戻り値

		String fpingPath = PingProperties.getFpingPath();
		String cmd[] = getCommand(fpingPath, hosts, sentCount, sentInterval, timeout, bytes);

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
				
				result.addAll(inStreamReader.getResult());
				m_log.debug("isReachable() STDOUT :" + inStreamReader.getResult().toString());

				errStreamReader.join();
				error.addAll(errStreamReader.getResult());
				m_log.debug("isReachable() STDERR :" +errStreamReader.getResult().toString());

			}
		} catch (IOException e) {
			error.add(e.getMessage());
			m_log.warn("isReachable() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			return false;

		}
		catch (InterruptedException e) {
			error.add(e.getMessage());
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
			
			error.add(errMsg);

			//fpingの戻り値が!0 or !1ならば失敗を返す
			return false;
		}
	}
	
	public String[] getCommand(String fpingPath, HashSet<String> hosts, int sentCount, int sentInterval, int timeout, int bytes) {
		//コマンド実行する配列を初期化する。
		int length = 6 + hosts.size();
		String cmd[] = new String[length];

		cmd[0] = fpingPath;
		cmd[1] = "-C" + sentCount;
		cmd[2] = "-p" + sentInterval;
		cmd[3] = "-t" + timeout;
		cmd[4] = "-b" + bytes;
		cmd[5] = "-q" ;

		//コマンドを実行するために値を詰め替えます。
		Iterator<String> itr = hosts.iterator();
		int i = 0;
		while(itr.hasNext()) {
			cmd[i + 6] = itr.next();
			i++;
		}
		
		return cmd;
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
}