/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.operator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.poller.util.DataTable;

/**
 * 計算方法が未定義の場合
 */
public class Undefined extends Operator {
	private static Log m_log = LogFactory.getLog(Undefined.class);
	
	@Override
	public double calc(DataTable currentTable, DataTable previousTable, String deviceName) throws CollectedDataNotFoundException, InvalidValueException{
		m_log.warn("undefined");
		return Double.NaN;
	}
}
