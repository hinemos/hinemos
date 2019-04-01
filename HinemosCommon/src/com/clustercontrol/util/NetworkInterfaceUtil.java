/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.IpAddressInfo;
import com.clustercontrol.bean.IpVersion;
import com.clustercontrol.bean.SubnetInfo;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;

/**
 * NetworkInterfaceに関する情報を扱うUtil<br>
 * <br>
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
public class NetworkInterfaceUtil {

	// ログ出力関連.
	/** ロガー */
	private static Log log = LogFactory.getLog(NetworkInterfaceUtil.class);

	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// IP計算関連.
	/** 1byteのbit数 */
	private static final int ONE_BYTE = 8;

	/** IPv4のCIDR最大bit数 */
	private static final int IPV4_MAX_BIT = 32;

	/** IPv6のプリフィックス最大bit数 */
	private static final int IPV6_MAX_BIT = 128;

	/** IPv4の最小アドレス */
	private static final String IPV4_MIN_ADDRESS = "0.0.0.0";

	/** IPv4の最大アドレス */
	private static final String IPV4_MAX_ADDRESS = "255.255.255.255";

	/** IPv6の最小アドレス */
	private static final String IPV6_MIN_ADDRESS = "0000:0000:0000:0000:0000:0000:0000:0000";

	/** IPv6の最大アドレス */
	private static final String IPV6_MAX_ADDRESS = "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF";

	/**
	 * 引数で指定したネットワークが上位のサブネットに包含されているかチェックする.
	 * 
	 * @param checkSubnet
	 *            チェック対象のサブネット
	 * @param upperSubnet
	 *            上位のサブネット
	 * @return true:上位の中に完全包含、false:違う範囲あり(バージョン違い含む).
	 * 
	 * @throws InvalidSetting
	 *             引数不正(呼び出し元でチェックしていない引数についてのみ).
	 * @throws HinemosUnknown
	 */
	public static boolean checkContainSubnet(IpAddressInfo checkSubnet, IpAddressInfo upperSubnet)
			throws InvalidSetting, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 引数nullチェック.
		if (checkSubnet == null || upperSubnet == null) {
			message = "the necessary argument('checkSubnet' or 'upperSubnet') is null.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// 引数サブネットに関する情報存在チェック.
		SubnetInfo checkSubnetInfo = checkSubnet.getSubnetInfo();
		SubnetInfo upperSubnetInfo = upperSubnet.getSubnetInfo();
		if (checkSubnetInfo == null || upperSubnetInfo == null) {
			message = "the argument('checkSubnet' or 'upperSubnet') need to have 'SubnetInfo'.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}
		Integer upperPrefixBitInt = upperSubnetInfo.getPrefixBit();
		if (upperPrefixBitInt == null) {
			message = "the argument 'upperSubnet' need to have 'prefixBit'.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// プリフィックス"0"は全範囲を対象とする.
		int upperPrefixBit = upperPrefixBitInt.intValue();
		if (upperPrefixBit == 0) {
			message = String.format(
					"the subnet is included in the upper subnet."
							+ " Because prefix is '0' so all IPs are within the range." + " subnet(upper)=[%s]",
					upperSubnet.getOriginIpAddress());
			log.debug(methodName + DELIMITER + message);
			return true;
		}

		// 引数バージョン有無チェック.
		if (checkSubnet.getVersion() == null || upperSubnet.getVersion() == null) {
			message = "the argument('checkSubnet' or 'upperSubnet') need to have version.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// バージョン異なる場合は範囲も異なる.
		if (!checkSubnet.getVersion().equals(upperSubnet.getVersion())) {
			message = String.format("the subnet has different range from the upper subnet."
					+ " Because each of IP-version are different." + " subnet(checked)=[%s], subnet(upper)=[%s]",
					checkSubnet.getOriginIpAddress(), upperSubnet.getOriginIpAddress());
			log.debug(methodName + DELIMITER + message);
			return false;
		}

		// 最小アドレスチェック.
		InetAddress checkMinAddress = checkSubnetInfo.getMinAddress();
		InetAddress upperMinAddress = upperSubnetInfo.getMinAddress();
		if (checkMinAddress == null || upperMinAddress == null) {
			message = "the argument ('checkSubnet' or 'upperSubnet') need to have 'minAddress'.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// 最小が上位よりも小さい→範囲外.
		boolean smallerMin = compareInetAddress(checkMinAddress, upperMinAddress, false);
		if (smallerMin) {
			message = String.format("the subnet has different range from the upper subnet."
					+ " Because the minimum-IP is smaller than upper." + " subnet(checked)=[%s], subnet(upper)=[%s]",
					checkSubnet.getOriginIpAddress(), upperSubnet.getOriginIpAddress());
			log.debug(methodName + DELIMITER + message);
			return false;
		}

		// 最大アドレスチェック.
		InetAddress checkMaxAddress = checkSubnetInfo.getMaxAddress();
		InetAddress upperMaxAddress = upperSubnetInfo.getMaxAddress();
		if (checkMaxAddress == null || upperMaxAddress == null) {
			message = "the argument ('checkSubnet' or 'upperSubnet') need to have 'maxAddress'.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// 最大が上位よりも大きい→範囲外.
		boolean largerMax = compareInetAddress(upperMaxAddress, checkMaxAddress, false);
		if (largerMax) {
			message = String.format("the subnet has different range from the upper subnet."
					+ " Because the maximum-IP is larger than upper." + " subnet(checked)=[%s], subnet(upper)=[%s]",
					checkSubnet.getOriginIpAddress(), upperSubnet.getOriginIpAddress());
			log.debug(methodName + DELIMITER + message);
			return false;
		}

		// 計算の結果包含されている.
		message = String.format(
				" the subnet is included in the upper subnet." + " subnet(checked)=[%s], subnet(upper)=[%s]",
				checkSubnet.getOriginIpAddress(), upperSubnet.getOriginIpAddress());
		log.debug(methodName + DELIMITER + message);
		return true;

	}

	/**
	 * 引数で指定したIPアドレスが指定のサブネットに含まれるかどうかチェックする.<br>
	 * 
	 * @param ipAddress
	 *            IPアドレス(IPv4/IPv6)
	 * @param subnet
	 *            サブネット(getIpAddressInfo()メソッドでプリフィックス指定して取得すること)
	 * @return true:含まれている 、false:含まれていない(IPバージョン違いもここ)
	 * @throws InvalidSetting
	 *             引数不正でチェック不可の場合.
	 * @throws HinemosUnknown
	 *             発生し得ない想定のエラー、ロジック誤り等の可能性あり.
	 */
	public static boolean checkContainSubnet(InetAddress ipAddress, IpAddressInfo subnet)
			throws InvalidSetting, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 引数空チェック.
		if (ipAddress == null || subnet == null) {
			message = "the necessary argument (ipAddress or subnet) is null.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// 引数チェック(サブネット).
		if (subnet.getSubnetInfo() == null) {
			message = "the 'subnet.getSubnetInfo()' is null.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		log.trace(methodName + DELIMITER
				+ String.format(
						"prepared to check to contain the ip-address in the subnet."
								+ " IP=[%s], subnet=[%s], subnetMin=[%s], subnetMax[%s]",
						ipAddress.getHostAddress(), subnet.getOriginIpAddress(),
						subnet.getSubnetInfo().getMinAddress().getHostAddress(),
						subnet.getSubnetInfo().getMaxAddress().getHostAddress()));

		// 引数チェック(プリフィックスbit数).
		Integer prefixBit = subnet.getSubnetInfo().getPrefixBit();
		if (prefixBit == null) {
			message = "the necessary argument ('subnet.getPrefixBit()') is null.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// プリフィックス"0"は全範囲を対象とする.
		if (prefixBit.intValue() == 0) {
			message = String.format("the IP address is within the subnet."
					+ " Because prefix is '0' so all IPs are within the range." + " IP=[%s], subnet=[%s]",
					ipAddress.getHostAddress(), subnet.getOriginIpAddress());
			log.debug(methodName + DELIMITER + message);
			return true;
		}

		// チェック対象IPのバージョンを特定
		IpVersion checkIpv = getIpVersion(ipAddress);

		// 引数チェック(サブネットのバージョン).
		if (subnet.getVersion() == null) {
			message = "the 'subnet.getVersion()' is null.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// バージョン違い.
		if (checkIpv != subnet.getVersion()) {
			return false;
		}

		// 引数チェック(サブネットの最小・最大IP).
		if (subnet.getSubnetInfo().getMinAddress() == null || subnet.getSubnetInfo().getMaxAddress() == null) {
			message = "the necessary argument ('minAddress or maxAddress()') is null.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// サブネットの最小IPよりも小さいか.
		boolean smallerMin = compareInetAddress(ipAddress, subnet.getSubnetInfo().getMinAddress(), false);
		if (smallerMin) {
			message = String.format("the IP address was calculated outside the subnet."
					+ " because it's smaller than minimum." + " IP=[%s], subnet=[%s]", ipAddress.getHostAddress(),
					subnet.getOriginIpAddress());
			log.debug(methodName + DELIMITER + message);
			return false;
		}

		// サブネットの最大IPよりも大きいか.
		boolean largerMax = compareInetAddress(subnet.getSubnetInfo().getMaxAddress(), ipAddress, false);
		if (largerMax) {
			message = String.format("the IP address was calculated outside the subnet."
					+ " because it's larger than maximum." + " IP=[%s], subnet=[%s]", ipAddress.getHostAddress(),
					subnet.getOriginIpAddress());
			log.debug(methodName + DELIMITER + message);
			return false;
		}

		// 最小～最大の間なので含まれていると判定.
		message = String.format("the IP address was calculated within the subnet." + " IP=[%s], subnet=[%s]",
				ipAddress.getHostAddress(), subnet.getOriginIpAddress());
		log.debug(methodName + DELIMITER + message);
		return true;

	}

	/**
	 * 引数で指定したIPアドレスの大小を比較する.<br>
	 * <br>
	 * 引数のIPのバージョンはそろえること.
	 * 
	 * @param smallerIp
	 *            比較した際に小さいと想定するIP.
	 * @param largerIp
	 *            比較した際に大きいと想定するIP.
	 * @param containEqual
	 *            true:比較結果等しい場合もtrueとしたい時に設定、false:比較結果等しい場合はfalseにしたい時に設定.
	 * 
	 * @return true:引数の想定通りの大小関係、false:引数と逆の大小関係
	 * 
	 * @throws InvalidSetting
	 *             引数不正(バージョン違いもここ).
	 * @throws HinemosUnknown
	 *             想定外.
	 * 
	 */
	public static boolean compareInetAddress(InetAddress smallerIp, InetAddress largerIp, boolean containEqual)
			throws InvalidSetting, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 引数チェック.
		if (smallerIp == null || largerIp == null) {
			message = "the necessary argument('smallerIp' or 'largerIp') is null.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// バージョンチェック.
		IpVersion smallerVersion = getIpVersion(smallerIp);
		IpVersion largerVersion = getIpVersion(largerIp);
		if (smallerVersion != largerVersion) {
			message = String.format("the argument('smallerIp' and 'largerIp') need to be same version."
					+ " smallerIp=[%s], largerIp=[%s]", smallerVersion.toString(), largerVersion.toString());
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		log.trace(methodName + DELIMITER
				+ String.format("prepared to compare InetAddress." + " smallerIp=[%s], largerIp=[%s]",
						smallerIp.getHostAddress(), largerIp.getHostAddress()));

		// バイナリを1byteずつ比較.
		List<Byte> rawSmallerAddress = BinaryUtil.arrayToList(smallerIp.getAddress());
		List<Byte> rawLargerAddress = BinaryUtil.arrayToList(largerIp.getAddress());
		for (int i = 0; i < rawSmallerAddress.size(); i++) {
			log.trace(methodName + DELIMITER
					+ String.format("prepared to compare InetAddress by a byte." + " index=%dByte", i + 1));
			Byte smallByte = rawSmallerAddress.get(i);
			int smallBytePlus = smallByte.intValue() & 0xFF; // int変換(0～255)
			Byte largeByte = rawLargerAddress.get(i);
			int largeBytePlus = largeByte.intValue() & 0xFF; // int変換(0～255)

			// 比較して想定通り.
			if (smallBytePlus < largeBytePlus) {
				log.trace(methodName + DELIMITER
						+ String.format("compared result is true." + " smallerBinary=%d, largerBinary=%d",
								smallBytePlus, largeBytePlus));
				return true;
			}

			// 比較して想定と逆.
			if (smallBytePlus > largeBytePlus) {
				log.trace(methodName + DELIMITER
						+ String.format("compared result is false." + " smallerBinary=%d, largerBinary=%d",
								smallBytePlus, largeBytePlus));
				return false;
			}
			// 等しい場合は次のバイナリを比較.
		}

		// 全バイナリが等しい.
		if (containEqual) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 引数で指定したIPアドレスのバージョンを取得.<br>
	 * <br>
	 * IPv6とIPv4が併用されている場合はIPv6を優先.<br>
	 * 
	 * @param ipAddress
	 *            IPアドレス(IPv4/IPv6)
	 * @return IpVersion定数
	 * @throws InvalidSetting
	 *             引数不正でチェック不可の場合.
	 * @throws HinemosUnknown
	 *             発生し得ない想定のエラー、ロジック誤り等の可能性あり.
	 */
	public static IpVersion getIpVersion(InetAddress ipAddress) throws InvalidSetting, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 引数空チェック.
		if (ipAddress == null) {
			message = "the necessary argument (ipAddress) is null.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// チェック対象IPのバージョンを特定
		// IPv6とIPv4が併用されている場合は、より細かく指定可能なIPv6に寄せて判定する.
		if (ipAddress instanceof Inet6Address) {
			return IpVersion.IPV6;
		} else if (ipAddress instanceof Inet4Address) {
			return IpVersion.IPV4;
		} else {
			message = String.format("failed to get version of IP from the 'ipAddress'(argument)." + " ipAddress=[%s]",
					ipAddress);
			log.warn(methodName + DELIMITER + message);
			throw new HinemosUnknown(message);
		}
	}

	/**
	 * IP文字列を分解してバージョン等の情報を取得する.<br>
	 * <br>
	 * 分解時に引数不正や文字列の形式不正が存在するかもチェックして不正の場合はExceptionをthrowする.<br>
	 * 
	 * @param originIpStr
	 *            IP文字列<br>
	 *            プリフィックス指定有無どちらでも可<br>
	 *            IPv4/CIDR(ex. 0.0.0.0/0 )<br>
	 *            IPv6/プリフィックス(ex. 0001::0008/64)
	 * @return IPアドレスに関する情報
	 * @throws InvalidSetting
	 *             引数不正
	 * @throws HinemosUnknown
	 *             発生し得ない想定.
	 * @see com.clustercontrol.bean.IpAddressInfo
	 */
	public static IpAddressInfo getIpAddressInfo(String originIpStr) throws InvalidSetting, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 引数空.
		if (originIpStr == null || originIpStr.isEmpty()) {
			message = "the necessary originIpStr is empty.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// 返却用のオブジェクト生成.
		IpAddressInfo returnInfo = new IpAddressInfo();
		SubnetInfo returnSubnetInfo = new SubnetInfo();
		returnInfo.setOriginIpAddress(originIpStr);

		// プリフィックス有無判定.
		boolean havePrefix = checkHavePrefix(originIpStr);
		returnInfo.setHavePrefix(havePrefix);

		// IPアドレスとプリフィックス部分を分解してセット.
		String onlyIpAddress = null;
		Integer prefixBit = null;
		if (havePrefix) {
			onlyIpAddress = getOnlyIpAddress(originIpStr);
			prefixBit = getIpPrefix(originIpStr);
		} else {
			onlyIpAddress = originIpStr;
			prefixBit = null;
		}

		// プリフィックスのみに分解できなかった.
		if (havePrefix && prefixBit == null) {
			message = String.format("failed to get 'prefixBit' from the 'originIpStr'(argument)." + " originIpStr=[%s]",
					originIpStr);
			log.warn(methodName + DELIMITER + message);
			throw new HinemosUnknown(message);
		}

		returnInfo.setOnlyIpAddress(onlyIpAddress);
		returnSubnetInfo.setPrefixBit(prefixBit);
		returnInfo.setSubnetInfo(returnSubnetInfo);

		// InetAddressの形式で取得・文字列の形式不正もここでチェック.
		InetAddress inetAddress = getInetAddress(returnInfo.getOnlyIpAddress());
		if (inetAddress == null) {
			message = String.format(
					"failed to get 'InetAddress' from the 'originIpStr'(argument)." + " originIpStr=[%s]", originIpStr);
			log.warn(methodName + DELIMITER + message);
			throw new HinemosUnknown(message);
		}

		returnInfo.setInetAddress(inetAddress);

		// バージョン判定.
		if (inetAddress instanceof Inet6Address) {
			returnInfo.setVersion(IpVersion.IPV6);
		} else if (inetAddress instanceof Inet4Address) {
			returnInfo.setVersion(IpVersion.IPV4);
		} else {
			// 想定外.
			message = String.format(
					"failed to get version of IP from the 'originIpStr'(argument)." + " originIpStr=[%s]", originIpStr);
			log.warn(methodName + DELIMITER + message);
			throw new HinemosUnknown(message);
		}

		// プリフィックス指定あり(サブネット)の場合に範囲内の最小アドレスと最大アドレスを算出する.
		if (havePrefix) {
			returnSubnetInfo = calculateSubnetInfo(prefixBit, inetAddress, returnInfo.getVersion());
			returnInfo.setSubnetInfo(returnSubnetInfo);
		}

		return returnInfo;
	}

	/**
	 * IPのプリフィックス指定有無を判定する.<br>
	 * 
	 * @param ipAddress
	 *            IPアドレス(プリフィックス指定有無どちらも可)
	 * @return true:プリフィックス有、false:プリフィックス無.
	 * @throws InvalidSetting
	 *             引数不正
	 */
	public static boolean checkHavePrefix(String ipAddressStr) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 引数不正.
		if (ipAddressStr == null || ipAddressStr.isEmpty()) {
			message = "the necessary 'ipAddressStr' is empty.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}
		// プリフィックス有無の判定.
		int indexSlash = ipAddressStr.indexOf("/");
		if (indexSlash > 0) {
			return true;
		}
		return false;
	}

	/**
	 * プリフィックス表記のIPからIPアドレスのみを取得.<br>
	 * <br>
	 * ※取得した文字列の妥当性についてはチェックしない(IPv4/IPv6の形式になっているか)
	 * 
	 * @param allIpAddress
	 *            IPv4/CIDR(ex. 0.0.0.0/0 ) IPv6/プリフィックス(ex. 0001::0008/64)
	 * @return IPアドレス(引数の例だとIPv4=0.0.0.0,IPv6=0001::0008)
	 * @throws InvalidSetting
	 *             引数不正
	 */
	public static String getOnlyIpAddress(String allIpAddress) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 引数空.
		if (allIpAddress == null || allIpAddress.isEmpty()) {
			message = "the necessary 'allIpAddress' is empty.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}
		// 引数に含まれている"/"がないもしくは位置不正.
		String ipAddress = allIpAddress;
		int indexSlash = ipAddress.indexOf("/");
		int length = ipAddress.length();
		if (indexSlash < 1 || indexSlash > length) {
			message = String.format("'/' in address isn't exist or is on a faulty position. address=[%s]",
					allIpAddress);
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}
		// "/"より後ろを取得.
		ipAddress = ipAddress.substring(0, indexSlash);

		return ipAddress;
	}

	/**
	 * プリフィックス表記のIPからプリフィックスを取得.<br>
	 * <br>
	 * ※取得したbit値の範囲の妥当性についてはチェックしない
	 * 
	 * @param allIpAddress
	 *            IPv4/CIDR(ex. 0.0.0.0/0 ) IPv6/プリフィックス(ex. 0001::0008/64)
	 * @return プリフィックスに該当するbit数(引数の例だとIPv4=0,IPv6=64)、
	 * @throws InvalidSetting
	 *             引数不正
	 */
	public static Integer getIpPrefix(String allIpAddress) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 引数空.
		if (allIpAddress == null || allIpAddress.isEmpty()) {
			message = "the necessary 'allIpAddress' is empty.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}
		// 引数に含まれている"/"がないもしくは位置不正.
		String prefixStr = allIpAddress;
		int indexSlash = prefixStr.indexOf("/");
		int length = prefixStr.length();
		if (indexSlash < 0 || indexSlash >= length) {
			message = String.format("'/' in address isn't exist or is on a faulty position. address=[%s]",
					allIpAddress);
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// "/"より後ろを取得.
		prefixStr = prefixStr.substring(indexSlash + 1, length);

		// "/"より後ろの文字列が数値変換不可.
		Integer prefixBit = null;
		int prefixPreInt = 0;
		try {
			prefixPreInt = Integer.parseInt(prefixStr);
		} catch (NumberFormatException e) {
			message = String.format("failed to change number from the string after '/'. address=[%s]", allIpAddress);
			log.warn(methodName + DELIMITER + message, e);
			throw new InvalidSetting(message);
		}

		// 数値に変換して返却.
		prefixBit = Integer.valueOf(prefixPreInt);
		return prefixBit;
	}

	/**
	 * IPの文字列をInetAddressに変換して返却する.<br>
	 * <br>
	 * 引数の文字列をInetAddressに変換する.<br>
	 * 
	 * @param ipAddress
	 *            IPアドレス(プリフィックス指定無)
	 * @return 表記のバージョン
	 * @throws InvalidSetting
	 *             引数不正
	 */
	public static InetAddress getInetAddress(String onlyIpAddress) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 引数不正.
		if (onlyIpAddress == null || onlyIpAddress.isEmpty()) {
			message = "the necessary 'onlyIpAddress' is empty.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// 変換(文字列の形式不正の場合もUnknownHostExceptionになる).
		InetAddress ipAddress = null;
		try {
			ipAddress = InetAddress.getByName(onlyIpAddress);
		} catch (UnknownHostException e) {
			message = String.format("failed to change 'InetAddress' from the string of IP. address=[%s]",
					onlyIpAddress);
			log.warn(methodName + DELIMITER + message, e);
			throw new InvalidSetting(message);
		}

		return ipAddress;
	}

	/**
	 * サブネット内の最小・最大IPアドレスを算出.<br>
	 * 
	 * @param prefixBit
	 *            プリフィックスに該当するbit数
	 * @param subnetAddress
	 *            ネットワークを表すIPアドレス.
	 * @param version
	 *            IPアドレスのバージョン.
	 * @return IPアドレスの最小値と最大値をセットして返却.
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 *             想定外.
	 */
	public static SubnetInfo calculateSubnetInfo(int prefixBit, InetAddress subnetAddress, IpVersion version)
			throws InvalidSetting, HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		SubnetInfo returnInfo = new SubnetInfo();
		InetAddress minAddress = null;
		InetAddress maxAddress = null;

		// プリフィックスの指定チェック.
		switch (version) {
		case IPV6:
			// IPv6の場合にプリフィックスが0～128bitの範囲内か.
			if (prefixBit < 0 || prefixBit > IPV6_MAX_BIT) {
				message = String.format(
						"On expressing IPv6, bit of prefix after '/' must be from 0 to 128." + " prefix=[%d]",
						prefixBit);
				log.warn(methodName + DELIMITER + message);
				throw new InvalidSetting(message);
			}
			// プリフィックスの範囲がOKだったので返却用にセット.
			returnInfo.setPrefixBit(Integer.valueOf(prefixBit));
			// プリフィックス"0"の場合は全範囲指定.
			if (prefixBit == 0) {
				try {
					minAddress = InetAddress.getByName(IPV6_MIN_ADDRESS);
					maxAddress = InetAddress.getByName(IPV6_MAX_ADDRESS);
				} catch (UnknownHostException e) {
					message = String.format("failed to change 'InetAddress' from the string. string=[%s] or [%s]",
							IPV6_MIN_ADDRESS, IPV6_MAX_ADDRESS);
					log.warn(methodName + DELIMITER + message, e);
					throw new HinemosUnknown(message);
				}
				returnInfo.setMinAddress(minAddress);
				returnInfo.setMaxAddress(maxAddress);
				return returnInfo;
			}
			break;
		case IPV4:
			// IPv4の場合にプリフィックス(CIDR)が0～32bitの範囲内か.
			if (prefixBit < 0 || prefixBit > IPV4_MAX_BIT) {
				message = String.format(
						"On expressing IPv4, bit of CIDR after '/' must be from 0 to 32." + " prefix=[%d]", prefixBit);
				log.warn(methodName + DELIMITER + message);
				throw new InvalidSetting(message);
			}
			// プリフィックスの範囲がOKだったので返却用にセット.
			returnInfo.setPrefixBit(Integer.valueOf(prefixBit));
			// プリフィックス"0"の場合は全範囲指定.
			if (prefixBit == 0) {
				try {
					minAddress = InetAddress.getByName(IPV4_MIN_ADDRESS);
					maxAddress = InetAddress.getByName(IPV4_MAX_ADDRESS);
				} catch (UnknownHostException e) {
					message = String.format("failed to change 'InetAddress' from the string. string=[%s] or [%s]",
							IPV4_MIN_ADDRESS, IPV4_MAX_ADDRESS);
					log.warn(methodName + DELIMITER + message, e);
					throw new HinemosUnknown(message);
				}
				returnInfo.setMinAddress(minAddress);
				returnInfo.setMaxAddress(maxAddress);
				return returnInfo;
			}
			break;
		default:
			// 想定外.
			message = "failed to get version of IP.";
			log.warn(methodName + DELIMITER + message);
			throw new HinemosUnknown(message);
		}

		// ネットワーク範囲を算出するのに必要なパラメータを計算・初期化.
		int prefixByte = prefixBit / ONE_BYTE; // プリフィックスに該当するbyte数
		int oddBit = prefixBit % ONE_BYTE; // プリフィックスに該当するバイナリの内、1byte未満の半端bit数.
		List<Byte> rawSubnetAddress = BinaryUtil.arrayToList(subnetAddress.getAddress()); // サブネットアドレスのバイナリ.
		List<Byte> rawMinAddress = new ArrayList<Byte>(); // ネットワーク内の最小アドレスのバイナリ.
		List<Byte> rawMaxAddress = new ArrayList<Byte>(); // ネットワーク内の最大アドレスのバイナリ.

		// プリフィックスに該当するバイナリは元のサブネットアドレスのバイナリのまま.
		if (prefixByte > 0) {
			for (int i = 0; i < prefixByte; i++) {
				Byte subnetByte = rawSubnetAddress.get(i);
				rawMinAddress.add(subnetByte);
				rawMaxAddress.add(subnetByte);
			}
		}

		// プリフィックスに該当するバイナリの内、1byte未満の半端bit分について、最小値と最大値を求める.
		if (oddBit > 0) {
			// プリフィックスのbit数からbit演算でネットワーク算出するための2進数生成.
			StringBuilder minArg = new StringBuilder();
			StringBuilder maxArg = new StringBuilder();
			for (int i = 0; i < ONE_BYTE; i++) {
				if (i < oddBit) {
					minArg.append(1);
					maxArg.append(0);
				} else {
					minArg.append(0);
					maxArg.append(1);
				}
			}
			int oddPrefixIntMin = Integer.parseInt(minArg.toString(), 2);
			int oddPrefixIntMax = Integer.parseInt(maxArg.toString(), 2);

			// ネットワークのバイナリをbit演算用に正のint変換.
			Byte netByte = rawSubnetAddress.get(prefixByte); // 半端byteに該当するサブネットアドレスのバイナリ.
			int netByteMinus = netByte.intValue(); // int変換(-128～127)
			int netBytePlus = netByteMinus & 0xFF; // int変換(0～255)

			// ネットワーク内の先頭IPのバイトをbit演算で算出.
			int minNetByteInt = netBytePlus & oddPrefixIntMin;
			if (log.isDebugEnabled()) {
				message = String.format(
						"calculated the start IP within the subnet." + " terminal binary=[%d](decimal), subnet=[%s/%d]",
						minNetByteInt, subnetAddress.getHostAddress(), prefixBit);
				log.debug(methodName + DELIMITER + message);
			}
			Byte minNetByte = Byte.valueOf((byte) minNetByteInt);
			rawMinAddress.add(minNetByte);

			// ネットワーク内の最終IPのバイトをbit演算で算出.
			int maxNetByteInt = netBytePlus | oddPrefixIntMax;
			if (log.isDebugEnabled()) {
				message = String.format(
						"calculated the end IP within the subnet." + " terminal binary=[%d](decimal), subnet=[%s/%d]",
						minNetByteInt, subnetAddress.getHostAddress(), prefixBit);
				log.debug(methodName + DELIMITER + message);
			}
			Byte maxNetByte = Byte.valueOf((byte) maxNetByteInt);
			rawMaxAddress.add(maxNetByte);
		}

		// プリフィックスに該当しないバイナリを連結.
		int notPrefixByte = 0;
		int maxByte = 0;

		switch (version) {
		case IPV6:
			maxByte = IPV6_MAX_BIT / ONE_BYTE;
			break;
		case IPV4:
			maxByte = IPV4_MAX_BIT / ONE_BYTE;
			break;
		default:
			// 想定外.
			message = "failed to get version of IP.";
			log.warn(methodName + DELIMITER + message);
			throw new HinemosUnknown(message);
		}

		if (oddBit <= 0) {
			notPrefixByte = maxByte - prefixByte;
		} else {
			notPrefixByte = maxByte - prefixByte - 1;
		}

		if (notPrefixByte > 0) {
			for (int i = 0; i < notPrefixByte; i++) {
				rawMinAddress.add(Byte.valueOf((byte)0));
				rawMaxAddress.add(Byte.valueOf((byte) 255));
			}
		}

		// 算出した最小と最大のIPアドレスをInetアドレスに変換して返却.
		try {
			minAddress = InetAddress.getByAddress(BinaryUtil.listToArray(rawMinAddress));
			maxAddress = InetAddress.getByAddress(BinaryUtil.listToArray(rawMaxAddress));
		} catch (UnknownHostException e) {
			message = String.format("failed to change 'InetAddress' from the binary. binary=[%s] or [%s]",
					BinaryUtil.listToString(rawMinAddress, 1), BinaryUtil.listToString(rawMaxAddress, 1));
			log.warn(methodName + DELIMITER + message, e);
			throw new HinemosUnknown(message);
		}

		returnInfo.setMinAddress(minAddress);
		returnInfo.setMaxAddress(maxAddress);
		log.trace(
				methodName + DELIMITER
						+ String.format("calcurated subnetInfo. subnet=[%s], minAddress=[%s], maxAddress=[%s]",
								subnetAddress.getHostAddress(), minAddress.getHostAddress(),
								maxAddress.getHostAddress()));
		return returnInfo;
	}

	/**
	 * MACアドレスの文字列比較.<br>
	 * <br>
	 * 区切り文字":"と"-"の違い、16進数大文字/小文字の違いを吸収してバイナリ値で比較する.<br>
	 * 
	 * @param macAddress1
	 *            比較対象のMACアドレス.
	 * @param macAddress2
	 *            比較対象のMACアドレス.
	 * @return true:引数のMACアドレスが等しい、false:等しくない.
	 * @throws InvalidSetting
	 *             引数が空文字・nullの場合(空同士・null同士もここ)
	 */
	public static boolean equalsMacAddresses(String macAddress1, String macAddress2) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 引数チェック.
		if (macAddress1 == null || macAddress1.isEmpty() || macAddress2 == null || macAddress2.isEmpty()) {
			message = "the necessary argument('macAddress1' or 'macAddress2') is null.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		List<Byte> macBinary1 = BinaryUtil.stringToList(macAddress1, 1, 1);
		if (macBinary1 == null || macBinary1.isEmpty() || macBinary1.size() != 6) {
			message = String.format("the argument 'macAddress1' is invalid. macAddress1=%s", macAddress1);
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		List<Byte> macBinary2 = BinaryUtil.stringToList(macAddress2, 1, 1);
		if (macBinary2 == null || macBinary2.isEmpty() || macBinary2.size() != 6) {
			message = String.format("the argument 'macAddress2' is invalid. macAddress2=%s", macAddress2);
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		return BinaryUtil.equals(macBinary1, macBinary2);
	}

}
