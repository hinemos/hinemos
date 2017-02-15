/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.snmptrap.bean;

import java.io.Serializable;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlType;

/**
 * VarBind 情報を内部形式にて保持する<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class SnmpVarBind implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum SyntaxType {
		Null,
		Counter32,
		Counter64,
		Gauge32,
		Int32,
		IPAddress,
		ObjectId,
		OctetString,
		Opaque,
		TimeTicks,
		UnsignedInt32
		};

	// object type
	private String name;
	// object type
	private SyntaxType type;
	// string raw data
	private byte[] object;
	
	// for cluster jax-ws
	public SnmpVarBind() { }
	
	public SnmpVarBind(String name, SyntaxType type, byte[] object) {
		this.name = name;
		this.type = type;
		this.object = object;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setType(SyntaxType type) {
		this.type = type;
	}
	
	public SyntaxType getType() {
		return type;
	}
	
	public void setObject(byte[] object) {
		this.object = object;
	}

	public byte[] getObject() {
		return object;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(object);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		SnmpVarBind other = (SnmpVarBind) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(object, other.object))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SnmpVarBind [name=" + name + ", type=" + type + ", object="
				+ Arrays.toString(object) + "]";
	}
}
