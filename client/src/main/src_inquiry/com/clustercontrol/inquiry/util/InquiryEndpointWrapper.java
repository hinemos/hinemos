/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.inquiry.util;

import java.util.List;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.inquiry.HinemosUnknown_Exception;
import com.clustercontrol.ws.inquiry.InquiryEndpoint;
import com.clustercontrol.ws.inquiry.InquiryEndpointService;
import com.clustercontrol.ws.inquiry.InquiryTarget;
import com.clustercontrol.ws.inquiry.InquiryTargetCreating_Exception;
import com.clustercontrol.ws.inquiry.InquiryTargetCommandNotFound_Exception;
import com.clustercontrol.ws.inquiry.InquiryTargetNotDownloadable_Exception;
import com.clustercontrol.ws.inquiry.InquiryTargetNotFound_Exception;
import com.clustercontrol.ws.inquiry.InvalidRole_Exception;
import com.clustercontrol.ws.inquiry.InvalidUserPass_Exception;
/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class InquiryEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( InquiryEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public InquiryEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static InquiryEndpointWrapper getWrapper(String managerName) {
		return new InquiryEndpointWrapper(EndpointManager.getActive(managerName));
	}

	private static List<EndpointSetting<InquiryEndpoint>> getInquiryEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(InquiryEndpointService.class, InquiryEndpoint.class);
	}

	public List<InquiryTarget> getInquiryTargetList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception  {
		WebServiceException wse = null;
		for (EndpointSetting<InquiryEndpoint> endpointSetting : getInquiryEndpoint(endpointUnit)) {
			try {
				InquiryEndpoint endpoint = endpointSetting.getEndpoint();
				List<InquiryTarget> list = endpoint.getInquiryTargetList();
				return list;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getInquiryContentList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public InquiryTarget getInquiryTarget(String targetId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InquiryTargetNotFound_Exception  {
		WebServiceException wse = null;
		for (EndpointSetting<InquiryEndpoint> endpointSetting : getInquiryEndpoint(endpointUnit)) {
			try {
				InquiryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getInquiryTarget(targetId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getInquiryContentList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void prepare(String target) throws HinemosUnknown_Exception, InvalidRole_Exception, com.clustercontrol.ws.inquiry.InvalidUserPass_Exception, InquiryTargetCreating_Exception, InquiryTargetNotFound_Exception, InquiryTargetCommandNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InquiryEndpoint> endpointSetting : getInquiryEndpoint(endpointUnit)) {
			try {
				InquiryEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.prepare(target);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("prepare(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public DataHandler download(String id) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InquiryTargetNotDownloadable_Exception, InquiryTargetNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InquiryEndpoint> endpointSetting : getInquiryEndpoint(endpointUnit)) {
			try {
				InquiryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.download(id);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("download(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}