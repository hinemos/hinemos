/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;

import java.util.Arrays;
import java.util.List;

public class AccessMethodConstant {
	/** SSHで実行 */
	public static final int TYPE_SSH = 0;
	/** WinRMで実行 */
	public static final int TYPE_WINRM = 1;

	public static List<Integer> getTypeList() {
		return Arrays.asList(TYPE_SSH, TYPE_WINRM);
	}
}
