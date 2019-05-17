/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.util;

import java.util.List;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.ws.reporting.HinemosUnknown_Exception;
import com.clustercontrol.ws.reporting.InvalidRole_Exception;
import com.clustercontrol.ws.reporting.InvalidSetting_Exception;
import com.clustercontrol.ws.reporting.InvalidUserPass_Exception;
import com.clustercontrol.ws.reporting.NotifyNotFound_Exception;
import com.clustercontrol.ws.reporting.ReportingDuplicate_Exception;
import com.clustercontrol.ws.reporting.ReportingEndpoint;
import com.clustercontrol.ws.reporting.ReportingEndpointService;
import com.clustercontrol.ws.reporting.ReportingInfo;
import com.clustercontrol.ws.reporting.ReportingNotFound_Exception;
import com.clustercontrol.ws.reporting.TemplateSetInfo;

/**
 * Hinemosマネージャとの通信をするクラス。 HAのような複数マネージャ対応のため、このクラスを実装する。
 * 
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReportingEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog(ReportingEndpointWrapper.class);
	
	private EndpointUnit endpointUnit;
	
	public ReportingEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}
	
	public static ReportingEndpointWrapper getWrapper(String managerName) {
		return new ReportingEndpointWrapper(EndpointManager.get(managerName));
	}
	
	private static List<EndpointSetting<ReportingEndpoint>> getReportingEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(ReportingEndpointService.class, ReportingEndpoint.class);
	}
	
	public ReportingInfo getReportingInfo(String reportingId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, ReportingNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getReportingInfo(reportingId);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("getReportingInfo(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<ReportingInfo> getReportingList() 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			ReportingNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getReportingList();
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("getReportingList(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public boolean addReporting(ReportingInfo info) 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			ReportingNotFound_Exception, ReportingDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.addReporting(info);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("addReporting(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public boolean  modifyReporting(ReportingInfo info) 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			ReportingNotFound_Exception, NotifyNotFound_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.modifyReporting(info);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("modifyReporting(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public boolean deleteReporting(String reportingId) 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			ReportingNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.deleteReporting(reportingId);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("deleteReporting(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public void setReportingStatus(String reportingId, boolean validFlag) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, ReportingNotFound_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				endpoint.setReportingStatus(reportingId, validFlag);
				return;
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("setReportingStatus(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<String> createReportingFile(String reportingId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, ReportingNotFound_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.createReportingFile(reportingId);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("createReportingFile(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<String> createReportingFileWithParam(String reportingId, ReportingInfo reportingInfo) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, ReportingNotFound_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.createReportingFileWithParam(reportingId, reportingInfo);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("createReportingFileWithParam(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public DataHandler downloadReportingFile(String reportingId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, ReportingNotFound_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.downloadReportingFile(reportingId);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("downloadReportingFile(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<TemplateSetInfo> getTemplateSetInfoList(String ownerRoleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, ReportingNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getTemplateSetList(ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("getTemplateSetInfoList(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public TemplateSetInfo getTemplateSetInfo(String templateSetId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, ReportingNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getTemplateSetInfo(templateSetId);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("getTemplateSetInfoList(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<String> getTemplateIdList(String ownerRoleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getTemplateIdList(ownerRoleId);
			} catch (WebServiceException e) {
				//マルチマネージャ接続時にレポーティングが有効になってないマネージャの混在によりendpoint通信で異常が出る場合あり
				//警告ログを出力するかは呼出元に判断をゆだねる。
				wse = e;
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public boolean addTemplateSet(TemplateSetInfo info) 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			ReportingNotFound_Exception, ReportingDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.addTemplateSet(info);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("addTemplateSet(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public boolean  modifyTemplateSet(TemplateSetInfo info) 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			ReportingNotFound_Exception, NotifyNotFound_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.modifyTemplateSet(info);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("modifyTemplateSet(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public boolean deleteTemplateSet(String templateSetId) 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, 
			ReportingNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.deleteTemplateSet(templateSetId);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("deleteTemplateSet(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<String> getReportOutputTypeStrList() 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getReportOutputTypeStrList();
			} catch (WebServiceException e) {
				//マルチマネージャ接続時にレポーティングが有効になってないマネージャの混在によりendpoint通信で異常が出る場合あり
				//警告ログを出力するかは呼出元に判断をゆだねる。
				wse = e;
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public int outputStringToType(String typeStr) 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.outputStringToType(typeStr);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("outputStringToType(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public String outputTypeToString(int type) 
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.outputTypeToString(type);
			} catch (WebServiceException e) {
				wse = e;
				String errMessage = HinemosMessage.replace(e.getMessage());
				m_log.warn("outputTypeToString(), " + errMessage, e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public String getVersion() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<ReportingEndpoint> endpointSetting : getReportingEndpoint(endpointUnit)) {
			try {
				ReportingEndpoint endpoint = (ReportingEndpoint) endpointSetting.getEndpoint();
				return endpoint.getVersion();
			} catch (WebServiceException e) {
				//マルチマネージャ接続時にレポーティングが有効になってないマネージャの混在によりendpoint通信で異常が出る場合あり
				//警告ログを出力するかは呼出元に判断をゆだねる。
				wse = e;
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

}
