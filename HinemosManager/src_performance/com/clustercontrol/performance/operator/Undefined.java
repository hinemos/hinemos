/*

Copyright (C) 2008 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

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
