/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.vm.util;

import java.util.List;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

public class QueryUtil {

	public static List<String> getVmProtocolMstDistinctProtocol() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<String> list
		= em.createNamedQuery("VmProtocolMstEntity.findDistinctProtocol", String.class)
		.getResultList();
		return list;
	}
}
