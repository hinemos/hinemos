/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.agent.util;

public class CollectorId {
	public final int type;
	public final String id;

	public CollectorId(int type, String id) {
		this.type = type;
		this.id = id;
	}

	@Override
	public boolean equals(Object compare) {
		if (! (compare instanceof CollectorId)) {
			return false;
		}

		if (this.type != ((CollectorId)compare).type) {
			return false;
		}
		if (this.id == null || ((CollectorId)compare).id == null || ! this.id.equals(((CollectorId)compare).id)){
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "CollectorId [type = " + type + ", id = " + id + "]";
	}

	@Override
	public int hashCode() {
		int hashCode = 17;
		hashCode = 31 * hashCode + type;
		hashCode = id == null ? 31 * hashCode + 0 : 31 * hashCode + id.hashCode();
		return hashCode;
	}

}