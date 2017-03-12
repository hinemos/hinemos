/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.repository.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Node Search Utility
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class NodeSearchUtil {
	// ログ
	private static Log m_log = LogFactory.getLog( NodeSearchUtil.class );

	/**
	 * Generate default IP for node search/create dialog
	 */
	public static String generateDefaultIp( String def ){
		try{
			InetAddress addr = Inet4Address.getLocalHost();
			// NOTE: Only correct when netmask length is 24
			return addr.getHostAddress().replaceFirst("\\.\\d+$", "."); 
		} catch (UnknownHostException e) {
			m_log.debug( e );
		}
		return def;
	}

	/**
	 * Generate default IP for node search/create dialog
	 */
	public static String generateDefaultIp( String def, int hostAddress ){
		try{
			// Get IP
			InetAddress addr = Inet4Address.getLocalHost();

			// Get subnet mask length
			int prefixLength = -1;
			NetworkInterface ni = NetworkInterface.getByInetAddress(addr);
			for( InterfaceAddress iaddr : ni.getInterfaceAddresses() ){
				if( iaddr.getAddress() instanceof Inet4Address ){
					prefixLength = iaddr.getNetworkPrefixLength();
					break;
				}
			}
			if( -1 == prefixLength ){
				return def;
			}

			byte[] ipRaw = addr.getAddress();
			// For ipv4
			if( 4 == ipRaw.length ){
				int counter = prefixLength;
				for( int i=0; i<ipRaw.length; i++ ){
					if( counter < 8 ){
						byte mask = 0x00;
						for( int j=0; j<counter; j++ ){
							mask = (byte)(mask >> 1 | 0x80);
						}
						ipRaw[i] = (byte)(ipRaw[i] & mask);
					}
					counter -= 8;
				}

				// Re-format/round up hostAddress
				if( hostAddress < 0 ){
					hostAddress += Math.pow( 2, 32- prefixLength );
				}

				// Add host address part
				int part = 4;
				while( hostAddress > 0 && part > 0 ){
					ipRaw[part-1] += (hostAddress & 0xff);
					hostAddress >>= 8;
				}
				return Inet4Address.getByAddress(ipRaw).getHostAddress();
			}
			// TODO Support ipv6?
		} catch (UnknownHostException | SocketException e) {
			m_log.debug( e );
		}
		return def;
	}

}
