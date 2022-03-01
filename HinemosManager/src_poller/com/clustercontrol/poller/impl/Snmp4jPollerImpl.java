/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.poller.impl;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthHMAC128SHA224;
import org.snmp4j.security.AuthHMAC192SHA256;
import org.snmp4j.security.AuthHMAC256SHA384;
import org.snmp4j.security.AuthHMAC384SHA512;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.DefaultPDUFactory;

import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.nodemap.util.SearchConnectionProperties;
import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.poller.util.TableEntry;
import com.clustercontrol.poller.util.TableEntry.ErrorType;
import com.clustercontrol.process.factory.RunMonitorProcess;
import com.clustercontrol.repository.util.SearchDeviceProperties;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * snmp4jライブラリを用いて実装したsnmpポーリングクラス
 * 
 * v3のユーザ作成について、下記のページを参照してください。
 * https://access.redhat.com/documentation/ja-JP/Red_Hat_Enterprise_Linux/6/html/Deployment_Guide/sect-System_Monitoring_Tools-Net-SNMP-Configuring.html
 */
public class Snmp4jPollerImpl {

	private final static Log log = LogFactory.getLog(Snmp4jPollerImpl.class);

	private final List<String> processOidList;

	private final Integer maxRepetitions = HinemosPropertyCommon.monitor_poller_snmp_bulk_maxrepetitions.getIntegerValue();
	private final Integer nonRepeaters  = HinemosPropertyCommon.monitor_poller_snmp_bulk_nonrepeaters.getIntegerValue();
	private final boolean deleteLabel = HinemosPropertyCommon.monitor_resource_delete_label.getBooleanValue();
	
	private final int notV3SnmpPoolSize = HinemosPropertyCommon.monitor_poller_snmp_not_v3_snmp_pool_size.getIntegerValue();
	
	private List<Snmp> notV3SnmpPool = new ArrayList<Snmp>(notV3SnmpPoolSize);
	private int notV3SnmpPoolIndex = 0;
	
	private static Snmp4jPollerImpl instance = new Snmp4jPollerImpl();
	
	private String _encode="";
	
	private Snmp4jPollerImpl() {
		try {
			for (int i = 0; i < notV3SnmpPoolSize; i++) {
				notV3SnmpPool.add(createNotV3Snmp());
			}
		} catch (IOException e) {
			log.warn("IOException message=" + e.getMessage());
		}
		
		String oidName = ".1.3.6.1.2.1.25.4.2.1.2";
		String oidParam = ".1.3.6.1.2.1.25.4.2.1.5";
		String oidPath = ".1.3.6.1.2.1.25.4.2.1.4";
		processOidList = new ArrayList<String>();
		processOidList.add(oidName);
		processOidList.add(oidParam);
		processOidList.add(oidPath);
	}
	
	public static Snmp4jPollerImpl getInstance() {
		return instance;
	}
	
	/**
	 * SNMPでポーリングし、DataTableの形式で返す
	 *
	 * @param ipAddress IPアドレス
	 * @param port ポート番号
	 * @param version バージョン（0:SNMP V1 protocol, 1:SNMP V2 protocol, 3: SNMP V3 protocol）
	 * @param community コミュニティ
	 * @param retries １回のポーリングでのリトライ回数
	 * @param timeout ポーリングのタイムアウト
	 * @param oidSet 対象OIDのリスト
	 * @param securityLevel セキュリティレベル（v3）
	 * @param user ユーザ名（v3）
	 * @param authPassword 認証パスワード（v3）
	 * @param privPassword 暗号化パスワード（v3）
	 * @param authProtocol 認証プロトコル（v3）
	 * @param privProtocol 暗号化プロトコル（v3）
	 */
	public DataTable polling(
			String ipAddress,
			int port,
			int version,
			String community,
			int retries,
			int timeout,
			Set<String> oidSet,
			String securityLevel,
			String user,
			String authPassword,
			String privPassword,
			String authProtocol,
			String privProtocol) {

		if (log.isDebugEnabled()) {
			String[][] nameValues = {
					{"ipAddress", ipAddress},
					{"port", String.valueOf(port)},
					{"version", String.valueOf(version)},
					{"community", community},
					{"retries", String.valueOf(retries)},
					{"timeout", String.valueOf(timeout)},
					{"securityLevel", securityLevel},
					{"user", user},
					{"authPassword", authPassword},
					{"privPassword", privPassword},
					{"authProtocol", authProtocol},
					{"privProtocol", privProtocol}
			};
			if (log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				for (String[] nameValue : nameValues) {
					sb.append(nameValue[0]).append("=").append(nameValue[1]).append(", ");
				}
				int i = 0;
				for (final String oid : oidSet) {
					sb.append("oidList[").append(i).append("]=").append(oid).append(", ");
					i++;
				}
				log.debug(sb.toString());
			}
		}

		//retriesは本当が試行回数だが、hinemosで実行回数になっているため、それに1を減らす。
		if (--retries < 0) {
			retries =0;
		}
		
		DataTable dataTable = null;
		
		_encode=HinemosPropertyCommon.repository_device_encoding.getStringValue();
		log.debug("polling(): SNMP encoding charsets are: "+_encode);
		
		Snmp snmp = null;

		// 結果取得途中のタイムアウト時に不完全な結果返却を許すかどうか
		// true：許容 取得できた情報のみ返却
		// false：許容しない IOException
		boolean allowIncomplete = true;
		
		// 呼出元がプロセス監視の場合は、プロセス数が不正な値となるため不完全な結果返却を許容しない
		if (RunMonitorProcess.class.getName().equals(Thread.currentThread().getStackTrace()[2].getClassName())) {
			allowIncomplete = false;
		}
		
		try {
			if (version == SnmpConstants.version3) {
				snmp = createV3Snmp(securityLevel, user, authPassword,
						privPassword, authProtocol, privProtocol);
			} else {
				snmp = getNotV3SnmpFromPool();
			}

			oidSet = formalizeOidList(oidSet);
			DefaultPDUFactory factory = createPduFactory(oidSet, version);
			
			Target target = createTarget(ipAddress, port, version,
					community, retries, timeout, securityLevel, user);

			MultipleOidsUtils utils = new MultipleOidsUtils(snmp, factory);
			
			int maxRetry = HinemosPropertyCommon.monitor_poller_snmp_max_retry.getIntegerValue();
			boolean errorFlag = true;
			for (int i = 0; i < maxRetry; i++) {
				Collection<VariableBinding> vbs = utils.query(target, createColumnOidList(oidSet).toArray(new OID[0]), allowIncomplete); 
				DataTable dataTableNotChecked = createDataTable(vbs);
				
				// 最後まで到達
				if (isDataTableValid(oidSet, dataTableNotChecked)) {
					dataTable = dataTableNotChecked;
					errorFlag = false;
					break;
				}
			}
			if (errorFlag) {
				// 何らかのエラーがある場合、全OIDについて失敗した情報を格納する
				dataTable = new DataTable();
				for (final String oid : oidSet) {
					final String entryOid = getEntryKey(oid) +".0";
					dataTable.putValue(new TableEntry(entryOid, HinemosTime.currentTimeMillis(),
							ErrorType.RESPONSE_NOT_FOUND, new SnmpResponseError(MessageConstant.MESSAGE_COULD_NOT_GET_VALUE.getMessage())));
				}
				log.warn("reach max retry(" + maxRetry + ")");
			}
		} catch (IOException e) {
			// ポーリング処理自体が失敗した場合、全OIDについて失敗した情報を格納する
			dataTable = new DataTable();
			for (final String oid : oidSet) {
				final String entryOid = getEntryKey(oid) +".0";
				dataTable.putValue(new TableEntry(entryOid, HinemosTime.currentTimeMillis(), ErrorType.IO_ERROR, e));
			}
		} finally {
			if (version == SnmpConstants.version3 && snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					log.warn(e);
				}
			}
		}
		
		// ポーリングしたにもかかわらずデータが存在しない箇所については、データが存在しないエラーのデータを入れる
		for (final String oid : oidSet) {
			final String entryOid = getEntryKey(oid);
			if (dataTable.containStartWith(entryOid) == false) {
				dataTable.putValue(new TableEntry(entryOid+".0", HinemosTime.currentTimeMillis(), 
						ErrorType.RESPONSE_NOT_FOUND, new SnmpResponseError(MessageConstant.MESSAGE_RESPONSE_NOT_FOUND.getMessage(new String[]{oid}))));
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("polling() : built DataTable");
			log.debug(dataTable.toString());
		}
		return dataTable;
	}

	public synchronized Snmp getNotV3SnmpFromPool() {
		if (notV3SnmpPoolIndex >= notV3SnmpPoolSize) {
			notV3SnmpPoolIndex = 0;
		}
		
		return notV3SnmpPool.get(notV3SnmpPoolIndex++);
	}

	private static int convertSecurityLevelToInt(String securityLevel) {
		if (SnmpSecurityLevelConstant.AUTH_NOPRIV.equals(securityLevel)) {
			return SecurityLevel.AUTH_NOPRIV;
		} else if (SnmpSecurityLevelConstant.AUTH_PRIV.equals(securityLevel)) {
			return SecurityLevel.AUTH_PRIV;
		}

		return SecurityLevel.NOAUTH_NOPRIV;
	}

	private List<OID> createColumnOidList(Set<String> oidSet) {
		List<OID> columnOIDList = new ArrayList<OID>();
		for (String oid : oidSet) {
			columnOIDList.add(new OID(oid));
		}
		return columnOIDList;
	}

	private DataTable createDataTable(Collection<VariableBinding> vbs) {
		DataTable dataTable = new DataTable();
		
		long time = HinemosTime.currentTimeMillis();
		for (VariableBinding binding : vbs) {
			if (binding == null) {
				continue;
			}
			String oidString = "." + binding.getOid().toDottedString();
			dataTable.putValue(
					getEntryKey(oidString),
					time,
					getVariableValue(oidString,
							binding.getVariable()));
		}
		
		return dataTable;
	}

	private static Target createNotV3Target(String ipAddress, int port, int version,
			String community, int retries, int timeout) {
		CommunityTarget target = new CommunityTarget();
		target.setAddress(new UdpAddress(String.format("%s/%d", ipAddress, port)));
		target.setCommunity(new OctetString(community));
		target.setTimeout(timeout);
		target.setRetries(retries);
		target.setVersion(version);
		return target;
	}

	private DefaultPDUFactory createPduFactory(Set<String> oids, int version) {
		DefaultPDUFactory factory;
		
		// SNMPv1以外のプロセス監視の場合はBULK
		if (isProcessOidList(oids) && version != SnmpConstants.version1) {
			factory = new DefaultPDUFactory(PDU.GETBULK);
			factory.setMaxRepetitions(maxRepetitions);
			factory.setNonRepeaters(nonRepeaters);
		//　それ以外の場合はv4.1踏襲のGETNEXT
		}else{
			factory = new DefaultPDUFactory(PDU.GETNEXT);
		}
		return factory;
	}

	private Snmp createNotV3Snmp() throws IOException {
		Snmp snmp = new Snmp(new UdpTransportMappingImpl());
		snmp.listen();
		return snmp;
	}

	public Snmp createV3Snmp(String securityLevel, String user, String authPassword, 
			String privPassword, String authProtocol, String privProtocol) throws IOException {

		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		MessageDispatcher md = new MessageDispatcherImpl();
		md.addMessageProcessingModel(new MPv3(usm));
		Snmp snmp = new Snmp(md, new UdpTransportMappingImpl());

		OID authProtocolOid;
		switch (authProtocol) {
		case SnmpProtocolConstant.SHA:
			authProtocolOid = AuthSHA.ID;
			break;
		case SnmpProtocolConstant.SHA224:
			authProtocolOid = AuthHMAC128SHA224.ID;
			break;
		case SnmpProtocolConstant.SHA256:
			authProtocolOid = AuthHMAC192SHA256.ID;
			break;
		case SnmpProtocolConstant.SHA384:
			authProtocolOid = AuthHMAC256SHA384.ID;
			break;
		case SnmpProtocolConstant.SHA512:
			authProtocolOid = AuthHMAC384SHA512.ID;
			break;
		default:
			authProtocolOid = AuthMD5.ID;
			break;
		}

		OID privProtocolOid;
		switch (privProtocol) {
		case SnmpProtocolConstant.AES:
			privProtocolOid = PrivAES128.ID;
			break;
		case SnmpProtocolConstant.AES192:
			privProtocolOid = PrivAES192.ID;
			break;
		case SnmpProtocolConstant.AES256:
			privProtocolOid = PrivAES256.ID;
			break;
		default:
			privProtocolOid = PrivDES.ID;
			break;
		}

		UsmUser usmUser;
		OctetString securityName = new OctetString(user);
		if (convertSecurityLevelToInt(securityLevel) == SecurityLevel.NOAUTH_NOPRIV) {
			usmUser = new UsmUser(securityName, null, null, null, null);
		} else if (convertSecurityLevelToInt(securityLevel) == SecurityLevel.AUTH_NOPRIV) {
			usmUser = new UsmUser(securityName, authProtocolOid, new OctetString(authPassword),
					null, null);
		} else {
			usmUser = new UsmUser(securityName, authProtocolOid, new OctetString(authPassword),
					privProtocolOid, new OctetString(privPassword));
		}
		snmp.getUSM().addUser(securityName, usmUser);

		snmp.listen();
		return snmp;
	}

	public static Target createTarget(String ipAddress, int port,
			int version, String community, int retries, int timeout,
			String securityLevel, String user) {
		Target target = null;
		if (version == SnmpConstants.version3) {
			target = createV3Target(ipAddress, port, version, community, retries, timeout, securityLevel, user);
		} else {
			target = createNotV3Target(ipAddress, port, version, community, retries,
					timeout);
		}
		return target;
	}

	private static Target createV3Target(String ipAddress, int port, int version, String community, int retries, int timeout,
			String securityLevel, String user) {
		UserTarget target = new UserTarget();
		target.setAddress(new UdpAddress(String.format("%s/%d", ipAddress, port)));
		target.setTimeout(timeout);
		target.setRetries(retries);
		target.setVersion(version);
		target.setSecurityLevel(convertSecurityLevelToInt(securityLevel));
		target.setSecurityName(new OctetString(user));
		return target;
	}

	private Set<String> formalizeOidList(Set<String> oids) {
		Set<String> newOids = new HashSet<String>(oids.size());
		for (String oid : oids) {
			//snmp4jのgetbulkが、末尾が0であるOIDを対応していないため、
			//.XX.YY.0ようなOIDを.XX.YYに変換
			if (oid.endsWith(".0")) {
				oid = oid.substring(0, oid.length() - 2);
			}
			newOids.add(oid);
		}

		return newOids;
	}

	/**
	 * DataTableに格納するためのEntryKeyを返すメソッド
	 *
	 * @param oidString OID
	 */
	private String getEntryKey(String oidString){
		return PollerProtocolConstant.PROTOCOL_SNMP + "." + oidString;
	}

	private Serializable getVariableValue(String oidString, Variable variable) {
		switch (variable.getSyntax()) {
		case SMIConstants.SYNTAX_OCTET_STRING:
			OctetString octStr = (OctetString) variable;
			StringBuilder value = new StringBuilder();
			byte[] bytes = octStr.getValue();
			if ((oidString.startsWith(SearchDeviceProperties.getOidNicMacAddress()) ||
					oidString.startsWith(SearchConnectionProperties.DEFAULT_OID_ARP) ||
					oidString.startsWith(SearchConnectionProperties.DEFAULT_OID_FDB)) && bytes.length == 6)  {

				// 6 byteのbinaryのOctetString
				// 00:0A:1F:5F:30 という値として扱う
				for (byte b : bytes) {
					String part = String.format("%02x", b).toUpperCase();
					if (value.length() == 0) {
						value.append(part);
					} else {
						value.append(":" + part);
					}
				}
			} else {
				// WindowsのNIC名には0x00が含まれることがあるので除外する
				int length = bytes.length;
				for (int i = 0; i < bytes.length; i++) {
					if (bytes[i] == 0x00) {
						length = i;
						break;
					}
				}
				
				//repository.device.encodeが指定されているかチェック
				if(_encode.isEmpty()){
					//空の場合は、環境の文字コードでエンコード
					value.append(new String(bytes, 0, length));
				}else{
					//以下の処理でマッチする文字コードがない場合は、UTF-8でエンコード
					String targetEncode="UTF-8";
					String[]tryEncode=_encode.split(",");
					//ひとつしか指定されなかった場合、その文字コードでエンコード
					if(tryEncode.length==1){
						targetEncode=tryEncode[0];
					}else{
						//複数指定された場合は、マッチする文字コードを探し出す
						for(int i=0;i<tryEncode.length;i++){
							if(checkEncode(bytes,tryEncode[i])){
								targetEncode=tryEncode[i];
								break;
							}
						}
					}
					log.debug("getVariableValue(): Try encode with "+targetEncode);
					
					try {
						value.append(new String(bytes, 0, length,targetEncode));
					} catch (UnsupportedEncodingException e) {
						//存在しない文字コードが指定されていた場合、環境の文字コードでエンコード
						log.warn("getVariableValue(): "+targetEncode+" is not supported encode. Fall back to Default");
						value.append(new String(bytes, 0, length));
					}
					log.debug("getVariableValue(): encoded string is ["+value+"]");
				}

			}

			log.debug("SnmpPollerImpl deleteLabel=" + deleteLabel);

			// Windowsのファイルシステム名からラベルを削除する。
			// C:\ Label:ABC  Serial Number 80f3e65c
			// ↓
			// C:\
			String ret = value.toString();
			if (deleteLabel && oidString.startsWith(SearchDeviceProperties.getOidFilesystemName())) {
				ret = ret.replaceAll(HinemosPropertyCommon.monitor_resource_label_replace.getStringValue(), "");
			}

			return ret;

		case SMIConstants.SYNTAX_OBJECT_IDENTIFIER:
			return "."  + variable.toString();

		default:
			return variable.toLong();
		}
	}
	
	/**
	 * Octet Stringが指定された文字コードかどうかをチェックするメソッド
	 * @param src ポーリング結果のOctet String
	 * @param encode チェックしたい文字コード
	 * @return true 文字コードが一致した場合
	 *          false 一致しなかった場合
	 */
	private boolean checkEncode(byte[]src, String encode){
		
		//JISチェック
		for (byte b : src){
			if (b == 0x1B){
				//JISが指定されていた場合
				if(encode.equals("JIS")||encode.equals("ISO-2022-JP")){
					return true;
				}else{
					//JIS以外が指定されていた場合
					return false;
				}
			}
		}
		
		//JIS以外の場合
		try {
			byte[] tmp = new String(src, encode).getBytes(encode);
			return Arrays.equals(tmp, src);
		}
		catch(UnsupportedEncodingException e) {
			return false;
		}
	}
	
	private boolean isDataTableValid(Set<String> oids, DataTable dataTable) {
		//プロセス監視に関して、すべてoidの結果の数が同じであることをチェックする
		if (isProcessOidList(oids)) {
			return isDataTableValidForProcess(oids, dataTable);
		}

		return true;
	}

	private boolean isDataTableValidForProcess(Set<String> oidSet,
			DataTable dataTable) {
		
		int lastCount = -1;
		ArrayList<ArrayList<String>> pidListList = new ArrayList<>();
		
		// プロセス監視の3つのOIDについて、取得したデータ長及び、PIDがそろっているかを確認する
		for (String oid : oidSet) {
			ArrayList<String> pidList = new ArrayList<String>();
			
			oid = getEntryKey(oid);
			int count = 0;
			for (String dataTableOid : dataTable.keySet()) {
				if (dataTableOid.startsWith(oid)) {
					count++;
					
					// PID取得&設定
					String pid = dataTableOid.substring(dataTableOid.lastIndexOf("."));
					pidList.add(pid);
				}
			}

			//Listをソート
			Collections.sort(pidList);
			pidListList.add(pidList);

			//最初のOIDの個数をセット
			if (lastCount == -1) {
				lastCount = count;
				continue;
			}

			//各OIDの個数をチェック
			if (lastCount != count) {
				return false;
			}
		}
		//全PID Listがそろっているかをチェックする
		@SuppressWarnings("unchecked")
		ArrayList<String>[] pidListArray = new ArrayList[3];
		for(int i = 0; oidSet.size() > i; i++){
			pidListArray[i] = pidListList.get(i);
		}
		
		for(int i = 0; lastCount > i; i++){
			String pid = null;
			for(int j = 0; pidListArray.length > j; j++){
				//最初のPIDをセット
				if(pid == null){
					pid = pidListArray[j].get(i);
					continue;
				}
				
				if(!pid.equals(pidListArray[j].get(i))){
					log.warn("isDataTableValidForProcess don't match. pid = " + pid + " is not exit.");
					log.warn("isDataTableValidForProcess current list = " + pidListArray[j].toString());
					return false;
				}
				else{
					if(log.isDebugEnabled()){
						log.debug("isDataTableValidForProcess match. pid = " + pid + " is exit.");
					}
				}
			}
		}
		
		if(log.isDebugEnabled()){
			log.debug("isDataTableValidForProcess success.");
		}
		return true;
	}

	private boolean isProcessOidList(Set<String> oids) {
		return oids.size() == processOidList.size() && oids.containsAll(processOidList);
	}
}