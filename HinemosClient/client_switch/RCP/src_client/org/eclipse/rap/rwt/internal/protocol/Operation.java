/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.rap.rwt.internal.protocol;

import java.util.Map;

// dummy class
public class Operation {

	public static class NotifyOperation extends Operation {

		public Map<String, JsonValue> getProperties() {
			// do nothing
			return null;
		}
		
	}
}
