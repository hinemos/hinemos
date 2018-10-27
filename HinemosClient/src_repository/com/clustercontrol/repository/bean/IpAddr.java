/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

public class IpAddr implements Comparable<IpAddr>{
	int version = 6;
	String ip = "";
	public IpAddr (String ip, int version) {
		if (version == 4) {
			this.version = 4;
		}
		this.ip = ip;
	}
	
	@Override
	public int compareTo(IpAddr o) {
		
		if (equals(o)) {
			return this.version - o.version;
		}
		if (this.version == 4 && o.version == 4) {
			long v1 = 0;
			for (String octet : this.ip.split("\\.")){
				v1 *= 256;
				try {
					int i = Integer.parseInt(octet);
					v1 += i;
				} catch (Exception e) {
				}
			}
			long v2 = 0;
			for (String octet : o.ip.split("\\.")){
				v2 *= 256;
				try {
					v2 += Integer.parseInt(octet);
				} catch (Exception e) {
				}
			}
			long diff = v1 - v2;
			if (diff > 0) {
				return 1;
			} else if (diff < 0) {
				return -1;
			} else {
				return 0;
			}
		}
		
		return this.ip.compareTo(o.ip);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IpAddr other = (IpAddr) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (version != other.version)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return ip;
	}
}