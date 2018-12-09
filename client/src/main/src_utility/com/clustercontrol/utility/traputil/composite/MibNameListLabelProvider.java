/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.composite;

import org.eclipse.jface.viewers.LabelProvider;

import com.clustercontrol.utility.traputil.bean.SnmpTrapMibMasterData;


/**
 * MIB名リストのラベルプロバイダ
 * 
 * @version 6.1.0
 * @since 2.4.0
 * 
 */
public class MibNameListLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		return ((SnmpTrapMibMasterData)element).getMib();
	}
}
