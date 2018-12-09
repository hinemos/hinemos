/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.jobmanagement.HinemosUnknown_Exception;
import com.clustercontrol.ws.jobmanagement.IconFileDuplicate_Exception;
import com.clustercontrol.ws.jobmanagement.IconFileNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidSetting_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidUserPass_Exception;
import com.clustercontrol.ws.jobmanagement.JobMapEndpoint;
import com.clustercontrol.ws.jobmanagement.JobMapEndpointService;
import com.clustercontrol.ws.jobmanagement.JobmapIconImage;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 * 
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class JobMapEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( JobMapEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public JobMapEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static JobMapEndpointWrapper getWrapper(String managerName) {
		return new JobMapEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<JobMapEndpoint>> getJobMapEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(JobMapEndpointService.class, JobMapEndpoint.class);
	}
	
	public String echo(String str) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = (JobMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.echo(str);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("echo(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public String getVersion() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = (JobMapEndpoint) endpointSetting.getEndpoint();
				return endpoint.getVersion();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getVersion(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	public void addJobmapIconImage(JobmapIconImage jobmapIconImage)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, IconFileDuplicate_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addJobmapIconImage(jobmapIconImage);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addJobmapIconImage(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	public void modifyJobmapIconImage(JobmapIconImage jobmapIconImage)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, IconFileNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyJobmapIconImage(jobmapIconImage);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyJobmapIconImage(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	public void deleteJobmapIconImage(List<String> iconIdList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, IconFileNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteJobmapIconImage(iconIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteJobmapIconImage(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	public JobmapIconImage getJobmapIconImage(String iconId)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, IconFileNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobmapIconImage(iconId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobmapIconImage(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	public String getJobmapIconIdJobDefault()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobmapIconIdJobDefault();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobmapIconIdJobDefault(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	public String getJobmapIconIdJobnetDefault()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobmapIconIdJobnetDefault();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobmapIconIdJobnetDefault(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	public String getJobmapIconIdApprovalDefault()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobmapIconIdApprovalDefault();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobmapIconIdApprovalDefault(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	public String getJobmapIconIdMonitorDefault()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobmapIconIdMonitorDefault();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobmapIconIdMonitorDefault(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	public String getJobmapIconIdFileDefault()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<JobMapEndpoint> endpointSetting : getJobMapEndpoint(endpointUnit)) {
			try {
				JobMapEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJobmapIconIdFileDefault();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobmapIconIdFileDefault(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}
