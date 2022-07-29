/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.util.RestBeanUtilBase;

public class RestAgentBeanUtil extends RestBeanUtilBase {

	public static void convertBean(Object srcBean, Object destBean) throws HinemosUnknown {
		//将来的に一部の処理をオーバーライドで変更できるように、本クラスを経由して共通処理を呼び出しとする
		try{
			new RestAgentBeanUtil().convertBeanRecursive(srcBean, destBean);
		} catch (InvalidSetting e) {
			// convertBeanRecursiveでは InvalidSettingは発生しない想定（RestBeanUtilとの互換性のためにthrowsがある）
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}
}
