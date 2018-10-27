/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cidr {
	public static final Pattern cidrPattern = Pattern.compile("^(\\d{1,3}+)\\.(\\d{1,3}+)\\.(\\d{1,3}+)\\.(\\d{1,3}+)/(\\d{1,2}+)$");
	public static final Pattern ipPattern = Pattern.compile("^(\\d{1,3}+)\\.(\\d{1,3}+)\\.(\\d{1,3}+)\\.(\\d{1,3}+)$");

	private int network;
	private int mask;

	public Cidr(String cidr) throws UnknownHostException {
		Matcher m = cidrPattern.matcher(cidr);
		if (!m.matches()) 
			throw new UnknownHostException("cider is of illegal format");
		network = ofIpaddress(new byte[]{toByte(m.group(1)), toByte(m.group(2)), toByte(m.group(3)), toByte(m.group(4))});
		mask = ofMask(Byte.parseByte(m.group(5)));
	}
	
	private static int ofIpaddress(Inet4Address ipaddress) {
		return ofIpaddress(ipaddress.getAddress());
	}

	private static int ofIpaddress(byte[] bytes) {
        int address = bytes[3] & 0xFF;
        address |= ((bytes[2] << 8) & 0xFF00);
        address |= ((bytes[1] << 16) & 0xFF0000);
        address |= ((bytes[0] << 24) & 0xFF000000);
		return address;
	}
	
	private static int ofMask(byte mask) {
		byte[] masks = new byte[4];
		for (int i = 0; i < 4; ++i) {
			int check = (mask - 8 * (i + 1));
			masks[i] = (byte)(check > 0 ? 0xFF: makeBitPattern(8 + check));
			if (check < 0) {
				break;
			}
		}
		return ofIpaddress(masks);
	}
	
	private static byte toByte(String value) {
		int intValue = Integer.parseInt(value);
		if (intValue > 127) {
			return (byte)(intValue - 256);
		}
		return (byte)intValue;
	}

	private static byte makeBitPattern(int check) {
		byte result = 0;
		for (int i = 0; i < Math.min(check, 8); ++i) {
			result |= (1 << (8 - i - 1));
		}
		return result;
	}

	public boolean matches(Inet4Address addr) {
		return (ofIpaddress(addr) & mask) == network;
	}

	public boolean matches(String addr) /* throws UnknownHostException */ {
		Matcher m = ipPattern.matcher(addr);
		if (!m.matches()) 
			return false;
//			throw new UnknownHostException("addr is of illegal format");
		return (ofIpaddress(new byte[]{toByte(m.group(1)), toByte(m.group(2)), toByte(m.group(3)), toByte(m.group(4))}) & mask) == network;
	}

	public static boolean matches(String cider, String addr) throws UnknownHostException {
		return new Cidr(cider).matches(addr);
	}

	public static List<String> matches(String cider, List<String> addrs) throws UnknownHostException {
		Cidr c = new Cidr(cider);
		
		List<String> matched = new ArrayList<>();
		for (String addr: addrs) {
			if (c.matches(addr))
				matched.add(addr);
		}
		return matched;
	}
}
