/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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