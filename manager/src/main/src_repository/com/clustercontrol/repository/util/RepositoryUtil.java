/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.util.MessageConstant;

/**
 * リポジトリに関するUtilityクラス<br/>
 *
 *
 */
public class RepositoryUtil {

	private static Log log = LogFactory.getLog(RepositoryUtil.class);

	/**
	 * ノードの基本情報をハッシュとして返す
	 * @param nodeInfo
	 * @return
	 */
	public static Map<String, String> createNodeParameter(NodeInfo nodeInfo) {
		Map<String, String> param = null;

		if (nodeInfo != null) {
			param = new HashMap<String, String>();

			param.put(SystemParameterConstant.FACILITY_ID, nodeInfo.getFacilityId());
			param.put(SystemParameterConstant.FACILITY_NAME, nodeInfo.getFacilityName());

			param.put(SystemParameterConstant.IP_ADDRESS_VERSION, nodeInfo.getIpAddressVersion() == null ? null : nodeInfo.getIpAddressVersion().toString());
			param.put(SystemParameterConstant.IP_ADDRESS, nodeInfo.getAvailableIpAddress());
			param.put(SystemParameterConstant.IP_ADDRESS_V4, nodeInfo.getIpAddressV4());
			param.put(SystemParameterConstant.IP_ADDRESS_V6, nodeInfo.getIpAddressV6());

			param.put(SystemParameterConstant.NODE_NAME, nodeInfo.getNodeName());
			param.put(SystemParameterConstant.OS_NAME, nodeInfo.getOsName());
			param.put(SystemParameterConstant.OS_RELEASE, nodeInfo.getOsRelease());
			param.put(SystemParameterConstant.OS_VERSION, nodeInfo.getOsVersion());
			param.put(SystemParameterConstant.CHARSET, nodeInfo.getCharacterSet());

			param.put(SystemParameterConstant.AGENT_AWAKE_PORT, nodeInfo.getAgentAwakePort() == null ? null : nodeInfo.getAgentAwakePort().toString());

			param.put(SystemParameterConstant.JOB_PRIORITY, nodeInfo.getJobPriority() == null ? null : nodeInfo.getJobPriority().toString());
			param.put(SystemParameterConstant.JOB_MULTIPLICITY, nodeInfo.getJobMultiplicity() == null ? null : nodeInfo.getJobMultiplicity().toString());

			param.put(SystemParameterConstant.SNMP_PORT, nodeInfo.getSnmpPort() == null ? null : nodeInfo.getSnmpPort().toString());
			param.put(SystemParameterConstant.SNMP_COMMUNITY, nodeInfo.getSnmpCommunity());
			param.put(SystemParameterConstant.SNMP_VERSION, nodeInfo.getSnmpVersion() == null ? null : SnmpVersionConstant.typeToString(nodeInfo.getSnmpVersion()));
			param.put(SystemParameterConstant.SNMP_TIMEOUT, nodeInfo.getSnmpTimeout() == null ? null : nodeInfo.getSnmpTimeout().toString());
			param.put(SystemParameterConstant.SNMP_TRIES, nodeInfo.getSnmpRetryCount() == null ? null : nodeInfo.getSnmpRetryCount().toString());

			param.put(SystemParameterConstant.WBEM_PORT, nodeInfo.getWbemPort() == null ? null : nodeInfo.getWbemPort().toString());
			param.put(SystemParameterConstant.WBEM_PROTOCOL, nodeInfo.getWbemProtocol());
			param.put(SystemParameterConstant.WBEM_TIMEOUT, nodeInfo.getWbemTimeout() == null ? null : nodeInfo.getWbemTimeout().toString());
			param.put(SystemParameterConstant.WBEM_TRIES, nodeInfo.getWbemRetryCount() == null ? null : nodeInfo.getWbemRetryCount().toString());
			param.put(SystemParameterConstant.WBEM_USER, nodeInfo.getWbemUser());
			param.put(SystemParameterConstant.WBEM_PASSWORD, nodeInfo.getWbemUserPassword());

			param.put(SystemParameterConstant.WINRM_USER, nodeInfo.getWinrmUser());
			param.put(SystemParameterConstant.WINRM_PASSWORD, nodeInfo.getWinrmUserPassword());
			param.put(SystemParameterConstant.WINRM_VERSION, nodeInfo.getWinrmVersion());
			param.put(SystemParameterConstant.WINRM_PORT, nodeInfo.getWinrmPort() == null ? null : nodeInfo.getWinrmPort().toString());
			param.put(SystemParameterConstant.WINRM_PROTOCOL, nodeInfo.getWinrmProtocol());
			param.put(SystemParameterConstant.WINRM_TIMEOUT, nodeInfo.getWinrmTimeout() == null ? null : nodeInfo.getWinrmTimeout().toString());
			param.put(SystemParameterConstant.WINRM_TRIES, nodeInfo.getWinrmRetries() == null ? null : nodeInfo.getWinrmRetries().toString());

			param.put(SystemParameterConstant.IPMI_IP_ADDRESS, nodeInfo.getIpmiIpAddress());
			param.put(SystemParameterConstant.IPMI_PORT, nodeInfo.getIpmiPort() == null ? null : nodeInfo.getIpmiPort().toString());
			param.put(SystemParameterConstant.IPMI_TIMEOUT, nodeInfo.getIpmiTimeout() == null ? null : nodeInfo.getIpmiTimeout().toString());
			param.put(SystemParameterConstant.IPMI_TRIES, nodeInfo.getIpmiRetries() == null ? null : nodeInfo.getIpmiRetries().toString());
			param.put(SystemParameterConstant.IPMI_PROTOCOL, nodeInfo.getIpmiProtocol());
			param.put(SystemParameterConstant.IPMI_LEVEL, nodeInfo.getIpmiLevel());
			param.put(SystemParameterConstant.IPMI_USER, nodeInfo.getIpmiUser());
			param.put(SystemParameterConstant.IPMI_PASSWORD, nodeInfo.getIpmiUserPassword());

			param.put(SystemParameterConstant.SSH_USER, nodeInfo.getSshUser());
			param.put(SystemParameterConstant.SSH_USER_PASSWORD, nodeInfo.getSshUserPassword());
			param.put(SystemParameterConstant.SSH_PRIVATE_KEY_FILENAME, nodeInfo.getSshPrivateKeyFilepath());
			param.put(SystemParameterConstant.SSH_PRIVATE_KEY_PASSPHRASE, nodeInfo.getSshPrivateKeyPassphrase());
			param.put(SystemParameterConstant.SSH_PORT, nodeInfo.getSshPort() == null ? null : nodeInfo.getSshPort().toString());
			param.put(SystemParameterConstant.SSH_TIMEOUT, nodeInfo.getSshTimeout() == null ? null : nodeInfo.getSshTimeout().toString());

			param.put(SystemParameterConstant.CLOUD_SERVICE, nodeInfo.getCloudService());
			param.put(SystemParameterConstant.CLOUD_SCOPE, nodeInfo.getCloudScope());
			param.put(SystemParameterConstant.CLOUD_RESOURCE_TYPE, nodeInfo.getCloudResourceType());
			param.put(SystemParameterConstant.CLOUD_RESOURCE_ID, nodeInfo.getCloudResourceId());
			param.put(SystemParameterConstant.CLOUD_RESOURCE_NAME, nodeInfo.getCloudResourceName());
			param.put(SystemParameterConstant.CLOUD_LOCATION, nodeInfo.getCloudLocation());

			if (nodeInfo.getNodeVariableInfo() != null) {
				for (NodeVariableInfo info : nodeInfo.getNodeVariableInfo()) {
					if (info.getNodeVariableName() == null || "".equals(info.getNodeVariableName())) {
						if (log.isDebugEnabled()) log.debug("key is not valid. (key = " + info.getNodeVariableName() + ")");
						continue;
					}
					if (param.containsKey(info.getNodeVariableName())) {
						log.info("duplicated key (key = " + info.getNodeVariableName() + "). this parameter will be used. (value = " + param.get(info.getNodeVariableName()) + ")");
						continue;
					}
					if (log.isTraceEnabled()) log.trace("adding user parameter. (key = " + info.getNodeVariableName() + ", value = " + info.getNodeVariableValue() + ")");
					param.put(info.getNodeVariableName(), info.getNodeVariableValue());
				}
			}
		}

		return param;
	}

	/**
	 * IPV4を数値型に変換します。
	 * @param ary オクテットごとに区切られたint型のアドレス
	 * @return int
	 */
	public static int ipV4ToInt(String addr) {
		int[] ary = new int[4];
		String[] strAry = addr.split("\\.");

		for (int i = 0; i < 4; i++ ) {
			ary[i] = Integer.parseInt(strAry[i]);
		}

		int l = ary[0] << 24;
        l += ary[1] << 16;
        l += ary[2] << 8;
        l += ary[3];

        return l;
	}

	/**
	 * INT型をIPアドレス(IPv4)文字列へ変換します。
	 * @param i IPv4アドレスの数値
	 * @return String
	 */
	public static String intToIpV4(int i) {
		int b1 = (i >> 24) & 0xff;
		int b2 = (i >> 16) & 0xff;
		int b3 = (i >> 8) & 0xff;
		int b4 = i & 0xff;

		return b1 + "." + b2 + "." + b3 + "." + b4;
	}

	/**
	 * BigInteger型をIPアドレス(IPv6)文字列へ変換します。
	 * @param argInt IPv6アドレスの数値
	 * @return String
	 */
	public static String bigIntToIpV6(BigInteger argInt) {

		StringBuilder str = new StringBuilder();
		for (int i=15; i>=0; i--) {
			int shift = 8 * i;
			Integer n = 0xff;
			BigInteger num = argInt.shiftRight(shift).and(new BigInteger(n.toString()));
			int intNum = num.intValue();
			String s = Integer.toHexString(intNum);
			if (s.length() < 2) {
				s = "0" + s;
			}
			str.append(s);
			if (i > 0 && i < 15) {
				int f = i % 2;
				str.append(f == 0 ? ":" : "");
			}
		}
		return str.toString();
	}

	/**
	 * byte配列をBigInteger型へ変換します。
	 * @param ary バイト配列
	 * @return　BigInteger
	 */
	public static BigInteger byteToBigIntV6(byte[] ary) {

		BigInteger ret = new BigInteger(ary);
		log.debug("ary=" + ret.toString());

		return ret;
	}

	/**
	 * 指定された範囲のIPアドレスリストを取得します。
	 * @param strFrom 開始アドレス
	 * @param strTo 終了アドレス
	 * @param version IPアドレスのバージョン(4 or 6)
	 * @return
	 * @throws HinemosUnknown
	 * @throws UnknownHostException
	 */
	public static List<String> getIpList (String strFrom, String strTo, int version) throws HinemosUnknown, UnknownHostException {
		List<String> list = new ArrayList<String>();
		if (version == 4) {
			int from = ipV4ToInt(strFrom);
			int to = ipV4ToInt(strTo);
			if(from > to) {
				throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_RANGE_OF_IP_ADDRESSES.getMessage());
			}
			for (int i = from ; i <= to; i ++) {
				list.add(intToIpV4(i));
			}
		} else {
			BigInteger from = byteToBigIntV6(InetAddress.getByName(strFrom).getAddress());
			BigInteger to = byteToBigIntV6(InetAddress.getByName(strTo).getAddress());

			if(from.compareTo(to) > 0) {
				throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_RANGE_OF_IP_ADDRESSES.getMessage());
			} else if (from.compareTo(to) == 0) {
				list.add(bigIntToIpV6(from));
			} else {
				int i = 0;
				while(true) {
					list.add(bigIntToIpV6(from));
					from = from.add(BigInteger.ONE);
					i++;
					if (i > 256 || from.compareTo(to) >= 0) {
						break;
					}
				}
			}
		}
		return list;
	}
}
