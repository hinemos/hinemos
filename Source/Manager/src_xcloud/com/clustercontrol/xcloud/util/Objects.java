/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;


public class Objects {
	private Object[] objects;
	
	public Objects(Object... objects) {
		this.objects = objects;
	}
	
	public Object[] objects() {
		return objects;
	}

	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof Objects) {
			Objects props2 = (Objects)anObject;
			if (this.objects.length != props2.objects.length) {
				return false;
			}
			
			for (int i = 0; i < objects.length; i++) {
				if (!this.objects[i].equals(props2.objects[i])) {
					return false;
				}
			}
			
			return true;
		}
		return false;
	}
	
	public int hashCode() {
		int h = 0;
		for (int i = 0; i < objects.length; i++) {
			h = 31*h + objects[i].hashCode();
		}
		return h;
	}

	public String toString() {
		StringBuffer oneLine = new StringBuffer();
		for (int j = 0; j < objects.length; ++j) {
			if (j != 0) {
				oneLine.append('-');
			}
			else {
				oneLine.append('"');
			}
			
			oneLine.append(objects[j].toString());

			if (j == objects.length - 1) {
				oneLine.append('"');
			}
		}
		return oneLine.toString();
	}
}
