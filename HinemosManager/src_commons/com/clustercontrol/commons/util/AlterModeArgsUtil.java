/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlterModeArgsUtil {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(AlterModeArgsUtil.class);

	private static final boolean isCloudServiceMode;

	static {
		/* クラウドサービスモードのシステムプロパティ確認 */
		String str = System.getProperty("ascloudservice");
		if (m_log.isDebugEnabled()) {
			m_log.debug("Initializer : Read SystemProperty \"ascloudservice\" is " + str);
		}
		isCloudServiceMode = new Boolean(str);
	}

	public static boolean isCloudServiceMode() {
		return isCloudServiceMode;
	}

}
