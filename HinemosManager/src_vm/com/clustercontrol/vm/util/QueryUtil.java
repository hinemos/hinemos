/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.vm.util;

import java.util.List;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

public class QueryUtil {

	public static List<String> getVmProtocolMstDistinctProtocol() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<String> list
			= em.createNamedQuery("VmProtocolMstEntity.findDistinctProtocol", String.class)
			.getResultList();
			return list;
		}
	}
}
