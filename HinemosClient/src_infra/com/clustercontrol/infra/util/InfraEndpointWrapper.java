/*

Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.util;

import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.infra.AccessInfo;
import com.clustercontrol.ws.infra.FacilityNotFound_Exception;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.IOException_Exception;
import com.clustercontrol.ws.infra.InfraCheckResult;
import com.clustercontrol.ws.infra.InfraEndpoint;
import com.clustercontrol.ws.infra.InfraEndpointService;
import com.clustercontrol.ws.infra.InfraFileBeingUsed_Exception;
import com.clustercontrol.ws.infra.InfraFileInfo;
import com.clustercontrol.ws.infra.InfraFileNotFound_Exception;
import com.clustercontrol.ws.infra.InfraFileTooLarge_Exception;
import com.clustercontrol.ws.infra.InfraManagementDuplicate_Exception;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InfraModuleNotFound_Exception;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidSetting_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.ModuleResult;
import com.clustercontrol.ws.infra.NotifyDuplicate_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;
import com.clustercontrol.ws.infra.SessionNotFound_Exception;
/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class InfraEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( InfraEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public InfraEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static InfraEndpointWrapper getWrapper(String managerName) {
		return new InfraEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<InfraEndpoint>> getInfraEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(InfraEndpointService.class, InfraEndpoint.class);
	}

	public List<InfraManagementInfo> getInfraManagementList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				List<InfraManagementInfo> list = endpoint.getInfraManagementList();
				return list;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getInfraManagementList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public InfraManagementInfo getInfraManagement(String managementId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyNotFound_Exception, InfraManagementNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				InfraManagementInfo info = endpoint.getInfraManagement(managementId);
				return info;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getInfraManagement(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<InfraManagementInfo> getInfraManagementListByOwnerRole(String ownerRoleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = (InfraEndpoint) endpointSetting.getEndpoint();
				List<InfraManagementInfo> list = endpoint.getInfraManagementListByOwnerRole(ownerRoleId);
				return list;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getInfraManagementList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void addInfraManagement(InfraManagementInfo info)	throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, NotifyDuplicate_Exception, InfraManagementDuplicate_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addInfraManagement(info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addInfraManagement(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyInfraManagement(InfraManagementInfo info) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, NotifyDuplicate_Exception, NotifyNotFound_Exception, InfraManagementNotFound_Exception, InfraManagementDuplicate_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyInfraManagement(info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyInfraManagement(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteInfraManagement(List<String> managementIds) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, NotifyNotFound_Exception, InfraManagementNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = (InfraEndpoint) endpointSetting.getEndpoint();
				endpoint.deleteInfraManagement(managementIds);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteInfraManagement(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<InfraCheckResult> getCheckResultList(String managementId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getCheckResultList(managementId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCheckResults(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public String createSession(String managementId, List<String> moduleIdList, List<AccessInfo> accessList) throws HinemosUnknown_Exception, InfraManagementNotFound_Exception, InfraModuleNotFound_Exception, InvalidRole_Exception, InvalidUserPass_Exception, FacilityNotFound_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		String ret = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				ret = endpoint.createSession(managementId,  moduleIdList, accessList);
				return ret;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("createCheckSession(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean deleteSession(String sessionId) throws HinemosUnknown_Exception, InfraManagementNotFound_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		boolean ret = false;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				ret = endpoint.deleteSession(sessionId);
				return ret;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("createCheckSession(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public ModuleResult checkInfraModule(String sessionId, boolean verbose) throws HinemosUnknown_Exception, InfraManagementNotFound_Exception, InfraModuleNotFound_Exception, InvalidRole_Exception, InvalidUserPass_Exception, SessionNotFound_Exception {
		WebServiceException wse = null;
		ModuleResult ret = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				ret = endpoint.checkInfraModule(sessionId, verbose);
				return ret;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("checkInfraModule(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public ModuleResult runInfraModule(String sessionId) throws HinemosUnknown_Exception, InfraManagementNotFound_Exception, InfraModuleNotFound_Exception, InvalidRole_Exception, InvalidUserPass_Exception, SessionNotFound_Exception{
		WebServiceException wse = null;
		ModuleResult ret = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				ret = endpoint.runInfraModule(sessionId);
				return ret;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("runInfraModule(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void addInfraFile(InfraFileInfo info, String filePath) throws HinemosUnknown_Exception, InfraFileTooLarge_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InfraManagementDuplicate_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addInfraFile(info, new DataHandler(new FileDataSource(filePath)));
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addInfraFile(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public void modifyInfraFile(InfraFileInfo info, String filePath) throws HinemosUnknown_Exception, InfraFileTooLarge_Exception, InvalidRole_Exception, InvalidUserPass_Exception	{
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				if (filePath != null) {
					endpoint.modifyInfraFile(info, new DataHandler(new FileDataSource(filePath)));
				} else {
					endpoint.modifyInfraFile(info, null);
				}
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyInfraFile(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<InfraFileInfo> getInfraFileList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				List<InfraFileInfo> list = endpoint.getInfraFileList();
				return list;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getInfraFileList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<InfraFileInfo> getInfraFileListByOwnerRoleId(String ownerRoleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				List<InfraFileInfo> list = endpoint.getInfraFileListByOwnerRoleId(ownerRoleId);
				return list;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getInfraFileListByOwnerRoleId(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteInfraFileList(List<String> fileIdList) throws HinemosUnknown_Exception, InfraFileNotFound_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InfraFileBeingUsed_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteInfraFileList(fileIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteInfraFileList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public DataHandler downloadInfraFile(String fileId, String fileName) throws HinemosUnknown_Exception, InfraFileNotFound_Exception, InvalidRole_Exception, InvalidSetting_Exception, InvalidUserPass_Exception, IOException_Exception  {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.downloadInfraFile(fileId, fileName);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("downloadInfraFile(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public void deleteDownloadedInfraFile(String fileName) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteDownloadedInfraFile(fileName);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("downloadInfraFile(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public int getFileSizeLimit() throws HinemosUnknown_Exception, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {

		WebServiceException wse = null;

		for (EndpointSetting<InfraEndpoint> endpointSetting : getInfraEndpoint(endpointUnit)) {
			try {
				InfraEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getInfraMaxFileSize();

			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getFileSizeLimit(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
}