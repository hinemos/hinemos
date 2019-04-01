/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.utility.HinemosUnknown_Exception;
import com.clustercontrol.ws.utility.InvalidRole_Exception;
import com.clustercontrol.ws.utility.InvalidUserPass_Exception;
import com.clustercontrol.ws.utility.UtilityEndpoint;
import com.clustercontrol.ws.utility.UtilityEndpointService;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 * 
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class UtilityEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( UtilityEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public UtilityEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static UtilityEndpointWrapper getWrapper(String managerName) {
		m_log.info("managerName : " + managerName);
		return new UtilityEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<UtilityEndpoint>> getUtilityEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(UtilityEndpointService.class, UtilityEndpoint.class);
	}
	
	public String echo(String str) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<UtilityEndpoint> endpointSetting : getUtilityEndpoint(endpointUnit)) {
			try {
				UtilityEndpoint endpoint = (UtilityEndpoint) endpointSetting.getEndpoint();
				return endpoint.echo(str);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("echo(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}
