/*

 Copyright (C) 2008 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 */

package com.clustercontrol.poller.impl;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sblim.wbem.cim.CIMDataType;
import org.sblim.wbem.cim.CIMDateTime;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.cim.UnsignedInt16;
import org.sblim.wbem.cim.UnsignedInt32;
import org.sblim.wbem.cim.UnsignedInt64;
import org.sblim.wbem.cim.UnsignedInt8;
import org.sblim.wbem.client.CIMClient;
import org.sblim.wbem.client.PasswordCredential;
import org.sblim.wbem.client.UserPrincipal;
import org.sblim.wbem.util.SessionProperties;

import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.poller.util.TableEntry;
import com.clustercontrol.poller.util.TableEntry.ErrorType;
import com.clustercontrol.util.HinemosTime;

/**
 * WBEMのポーリングを実行するクラス
 * 
 * @version 3.1.0
 * @since 3.1.0
 */
public class WbemPollerImpl {
	private static Log m_log = LogFactory.getLog(WbemPollerImpl.class);

	// デフォルトのポート番号
	private static final int DEFAULT_PORT = 5988;

	// デフォルトのリトライ回数
	private static final int DEFAULT_RETRIES = 3;

	// デフォルトのタイムアウト値(ms)
	private static final int DEFAULT_TIMEOUT = 3000;


	// IPアドレス（ポーリングの度に更新）
	private String m_ipAddress;

	// 収集のターゲットとなるCIMクラスとプロパティ（全てのポーリングが終了するまで値を保持）
	private String[] m_cimText;

	// CIMサーバアクセス用アドレス(例: http://xx.xx.xx.xx:5988)
	private String m_cimAgentAddress = null;

	// 名前空間(デフォルト: root/cimv2)
	private String m_nameSpace = "root/cimv2";

	// 結果を格納するHashMap（Key:CIMクラス.プロパティ名）
	private HashMap<String, CIMValue> m_retMap = new HashMap<String, CIMValue>();

	// クラス変数の初期化
	private void init(){

	}

	/**
	 * メインルーチン
	 * IPアドレスと　DataTableを受け取り、
	 * ポーリングした結果をDataTableに代入する
	 * 
	 * @param ipAddress IPアドレス
	 * @param port ポート番号
	 * @param protocol 通信プロトコル
	 * @param user 接続ユーザ
	 * @param password 接続パスワード
	 * @param nameSpace 名前空間
	 * @param retries １回のポーリングでのリトライ回数
	 * @param timeout ポーリングのタイムアウト
	 * @param cimList 対象CIMクラス、プロパティのリスト
	 * @param indexCheckFlg ポーリング結果のインデックスが揃っているかのチェック
	 */
	@SuppressWarnings("unchecked")
	public DataTable polling(
			String ipAddress,
			int port,
			String protocol,
			String user,
			String password,
			String nameSpace,
			int retries,
			int timeout,
			Set<String> cimList // cimクラスとプロパティ名のリスト
			) {
		// クラス変数を初期化
		init();

		if (port < 0){
			m_log.debug("set Port. " + port + " to " + DEFAULT_PORT);
			port = DEFAULT_PORT;
		}

		// デフォルト値の設定
		if(retries < 0){
			m_log.debug("set Retries. " + retries + " to " + DEFAULT_RETRIES);
			retries = DEFAULT_RETRIES;
		}

		if(timeout < 0){
			m_log.debug("set Timeout. " + timeout + " to " + DEFAULT_TIMEOUT);
			timeout = DEFAULT_TIMEOUT;
		}

		// ポーリングの結果を返すインスタンス
		DataTable dataTable = new DataTable();

		m_ipAddress = ipAddress;
		try{
			InetAddress address = InetAddress.getByName(ipAddress);
			if(address instanceof Inet6Address){
				m_ipAddress = "[" + m_ipAddress + "]";
			}
		} catch (UnknownHostException e) {
			m_log.warn("polling() ipAddress = " + ipAddress, e);
		}
		m_cimAgentAddress = protocol + "://" + m_ipAddress + ":" + port;

		if(nameSpace != null) {
			m_nameSpace = nameSpace;
		}

		// デバッグ出力
		if (m_log.isDebugEnabled()) {
			m_log.debug("polling() start : " + m_ipAddress.toString());
			m_log.debug("Port            : " + port);
			m_log.debug("Protocol        : " + protocol);
			m_log.debug("User            : " + user);
			m_log.debug("Password        : " + password);
			m_log.debug("Retries         : " + retries);
			m_log.debug("Timeout         : " + timeout);
			m_log.debug("URL             : " + m_cimAgentAddress);
		}

		long enumerationStart = HinemosTime.currentTimeMillis();

		CIMClient cimClient = null;

		try {

			// *****************************
			// 1. Create user credentials
			// *****************************
			UserPrincipal userPr = new UserPrincipal(user);
			PasswordCredential pwCred = new PasswordCredential(password.toCharArray());

			// *****************************
			// 2. Set NameSpace
			// - URL is set like: http(s)://<IP>:Port
			// - Namespace does not need to be specified in COPs if set in this constuctor
			// - There is no server authentication being done. Thus: No need for a truststore
			// *****************************
			CIMNameSpace ns = new CIMNameSpace(m_cimAgentAddress, m_nameSpace);

			// *****************************
			// 3. Create CIM Client
			// *****************************
			cimClient = new CIMClient(ns, userPr, pwCred);

			// *****************************
			// 4. Create Session Properties
			// *****************************
			SessionProperties properties = cimClient.getSessionProperties();
			if(properties == null){
				properties = new SessionProperties();
				cimClient.setSessionProperties(properties);
			}
			properties.setHttpTimeOut(timeout);

			m_cimText = new String[cimList.size()];


			// 問い合わせ用のHashMapを作成
			HashMap<String, ArrayList<String>> requestMap = new HashMap<String, ArrayList<String>>();
			String cimClass = "";
			String cimProperty = "";
			ArrayList<String> propertyList = null;

			int i = 0;
			for (String cimText : cimList) {

				m_cimText[i] = cimText;

				String[] targetValue = m_cimText[i].split("\\.");
				cimClass = targetValue[0];
				cimProperty = targetValue[1];

				propertyList = requestMap.get(cimClass);

				// 既に存在する場合は、プロパティ名を追加する
				if(propertyList != null && propertyList.size() != 0){
					propertyList.add(cimProperty);
				}
				// 存在しない場合は、新しく作成する
				else {
					propertyList = new ArrayList<String>();
					propertyList.add(cimProperty);
					requestMap.put(cimClass, propertyList);
				}

				i++;
			}


			CIMObjectPath cop = null;
			CIMInstance ci = null;
			CIMValue value = null;
			Enumeration<CIMInstance> enm = null;

			// 設定したリトライ回数分リトライする
			for(int j = 0; j < retries; j++) {
				boolean errorFlg = false;
				m_retMap = new HashMap<String, CIMValue>();
				try {
					for (Map.Entry<String, ArrayList<String>> cimClassEntry: requestMap.entrySet()) {
						cimClass = cimClassEntry.getKey();
						propertyList = cimClassEntry.getValue();

						m_log.debug("CIMClass : " + cimClassEntry.getKey());

						cop = new CIMObjectPath(cimClassEntry.getKey());
						enm = cimClient.enumInstances(cop, true);

						i = 0;
						while(enm.hasMoreElements()) {

							ci = enm.nextElement();

							for (String property : propertyList) {
								cimProperty = property;

								if(ci.getProperty(cimProperty) != null){

									value = ci.getProperty(cimProperty).getValue();

									// 下記のコードを挿入すると、5回に1回程度の頻度で、
									// CIMサーバが異常なデータを返却したときと同様の動作となる。
									// 例：
									// プロセス監視の収集結果の集計でArray index out of range: 0が発生する。
									// test code
									/*
									if(value.getType().getType() == CIMDataType.STRING_ARRAY){
										testCounter ++;
										if (testCounter > 10) {
											value = new CIMValue(new Vector<String>(),
													new CIMDataType(CIMDataType.STRING_ARRAY));
											testCounter = 0;
										}
									}
									 */
									// test code

									if (!checkCIMData(value)) {
										errorFlg = true;
										continue;
										/*
										 * 失敗しても、とりあえず最後まで進める。
										 * (一部だけおかしい、という可能性があるため。)
										 */
									}
									m_retMap.put(cimClass+"."+cimProperty+"."+i, value);

								}
							}
							if (errorFlg) {
								break;
							}
							i++;
						}
						if (errorFlg) {
							break;
						}
					}
				} catch(CIMException e){
					errorFlg = true;
					for (String property : propertyList) {
						dataTable.putValue(new TableEntry(getEntryKey(cimClass + "." + property + ".0"), HinemosTime.currentTimeMillis(), ErrorType.IO_ERROR, e));
					}
					m_log.warn("polling() warning :" + m_ipAddress.toString() +
							", cimClass=" + cimClass + ", ID=" + e.getID() + ", message=" + e.getMessage());
				} catch(Exception e) {
					errorFlg = true;
					for (String property : propertyList) {
						dataTable.putValue(new TableEntry(getEntryKey(cimClass + "." + property + ".0"), HinemosTime.currentTimeMillis(), ErrorType.IO_ERROR, e));
					}
					m_log.warn("polling() warning :" + m_ipAddress.toString() + ", " + cimClass + " unforeseen error. " + e.getMessage(), e);
				} finally {
					if (errorFlg) {
						// m_retMap = new HashMap<String, CIMValue>();
						/*
						 * リトライしても失敗した場合は、ある程度成功したm_retMapを、
						 * 格納フェーズに進める。
						 */
					} else {
						break;
					}
				}
			}
		}
		catch(RuntimeException e){
			m_log.warn("polling() warning :" + m_ipAddress.toString() + " unforeseen error. " + e.getMessage(), e);
		}
		finally {
			if(cimClient != null) {
				try {
					cimClient.close();
				}
				catch (Exception e) {
					m_log.warn("polling():" + m_ipAddress.toString() + " Session close failed", e);
				}
			}
		}

		long enumerationStop = HinemosTime.currentTimeMillis();

		// デバッグ出力
		if (m_log.isDebugEnabled()) {
			m_log.debug("polling() end : time : " + (enumerationStop - enumerationStart));
		}

		// ***************
		// 結果を格納 (格納フェーズ)
		// ***************
		try {
			// 結果に何も格納されていない場合は、何もせず終了
			if (m_retMap == null || m_retMap.size() == 0) {
				m_log.debug("wbemReceived() : " + m_ipAddress.toString() + " result is empty");
				return dataTable;
			}

			long time = HinemosTime.currentTimeMillis(); // 取得時刻

			for(Map.Entry<String, CIMValue> entry: m_retMap.entrySet()) {
				String cimString = entry.getKey();
				CIMValue value = entry.getValue();

				if(value.getType().getType() == CIMDataType.UINT8){
					long ret = ((UnsignedInt8)value.getValue()).longValue();
					dataTable.putValue(getEntryKey(cimString), time, ret);

					m_log.debug("polling() dataTable put : " +
							"entryKey : " + getEntryKey(cimString) +
							", time : " + time +
							", value : " + ret);
				}
				else if(value.getType().getType() == CIMDataType.UINT16){
					long ret = ((UnsignedInt16)value.getValue()).longValue();
					dataTable.putValue(getEntryKey(cimString), time, ret);

					m_log.debug("polling() dataTable put : " +
							"entryKey : " + getEntryKey(cimString) +
							", time : " + time +
							", value : " + ret);
				}
				else if(value.getType().getType() == CIMDataType.UINT32){
					long ret = ((UnsignedInt32)value.getValue()).longValue();
					dataTable.putValue(getEntryKey(cimString), time, ret);

					m_log.debug("polling() dataTable put : " +
							"entryKey : " + getEntryKey(cimString) +
							", time : " + time +
							", value : " + ret);
				}
				else if(value.getType().getType() == CIMDataType.UINT64) {
					BigInteger bigInt = ((UnsignedInt64)value.getValue()).bigIntValue();
					long ret = bigInt.longValue();
					dataTable.putValue(getEntryKey(cimString), time, ret);

					m_log.debug("polling() dataTable put : " +
							"entryKey : " + getEntryKey(cimString) +
							", time : " + time +
							", value : " + ret);
				}
				else if(value.getType().getType() == CIMDataType.STRING) {
					String ret = (String)value.getValue();
					dataTable.putValue(getEntryKey(cimString), time, ret);

					m_log.debug("polling() dataTable put : " +
							"entryKey : " + getEntryKey(cimString) +
							", time : " + time +
							", value : " + ret);

				}
				else if(value.getType().getType() == CIMDataType.STRING_ARRAY){

					Vector<String> ret = (Vector<String>)value.getValue();
					dataTable.putValue(getEntryKey(cimString), time, ret);

					m_log.debug("polling() dataTable put : " +
							"entryKey : " + getEntryKey(cimString) +
							", time : " + time +
							", value : " + ret);
				}
				else if(value.getType().getType() == CIMDataType.DATETIME){

					CIMDateTime sdt = (CIMDateTime)value.getValue();

					// CIMDateTimeからCalendar情報を取得
					Calendar cal = sdt.getCalendar();

					// Calendar情報から、ミリ秒の情報を取得したものを秒単位に変更
					long ret = cal.getTimeInMillis() / 1000;

					dataTable.putValue(getEntryKey(cimString), time, ret);

					m_log.debug("polling() dataTable put : " +
							"entryKey : " + getEntryKey(cimString) +
							", time : " + time +
							", value : " + ret);

				}
				else {
					m_log.debug("polling() data type is nothing");
				}

			}
		} catch (Exception e) { // 何か例外が生じた場合は収集を停止する
			m_log.warn("polling() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);

			// 収集したデータを全てクリアする
			dataTable.clear();
			for(Map.Entry<String, CIMValue> entry: m_retMap.entrySet()) {
				final String entryKey = getEntryKey(entry.getKey());
				dataTable.putValue(new TableEntry(entryKey, HinemosTime.currentTimeMillis(), ErrorType.IO_ERROR, e));
			}
		}
		return dataTable;
	}

	private boolean checkCIMData(CIMValue value) {
		if (value == null) {
			m_log.info("checkCIMData : value is null");
			return false;
		}
		if (value.getType() == null) {
			m_log.info("checkCIMData : value.getType is null, " +
					"refClassName=" + value.getType().getRefClassName() +
					"stringType=" + value.getType().getStringType() +
					"toString=" + value.getType().toString());
			return false;
		}
		int type = value.getType().getType();
		if (type == CIMDataType.UINT8) {
		} else if(type == CIMDataType.UINT16) {
		} else if(type == CIMDataType.UINT32) {
		} else if(type == CIMDataType.UINT64) {
		} else if(type == CIMDataType.STRING) {
		} else if(type == CIMDataType.STRING_ARRAY){
			@SuppressWarnings("unchecked")
			Vector<String> ret = (Vector<String>)value.getValue();
			if (ret.size() == 0) {
				m_log.info("checkCIMData : CIMValue has fault. : ip=" + m_ipAddress);
				return false;
			}
		}
		return true;
	}


	/**
	 * DataTableに格納するためのEntryKeyを返すメソッド
	 * 
	 * @param cimString CIMクラスとプロパティの文字列
	 */
	private String getEntryKey(String cimString){

		return PollerProtocolConstant.PROTOCOL_WBEM + "." + cimString;
	}

	/**
	 * 単体試験用
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// パラメータを設定
		WbemPollerImpl poller = new WbemPollerImpl();

		String ipAddress = args[0];
		int port = Integer.parseInt(args[1]);
		String protocol = args[2];
		String user = args[3];
		String password = args[4];
		String nameSpace = args[5];  // 設定不可
		int retries = Integer.parseInt(args[6]);
		int timeout = Integer.parseInt(args[7]);
		Set<String> cims = new HashSet<String>();

		for(int i=8; i<args.length; i++){
			cims.add(args[i]);
		}

		DataTable table = poller.polling(
				ipAddress,
				port,
				protocol,
				user,
				password,
				nameSpace,
				retries,
				timeout,
				cims);
		System.out.println(table);
	}
}
