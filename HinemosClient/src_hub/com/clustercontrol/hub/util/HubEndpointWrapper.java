/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.hub.HinemosDbTimeout_Exception;
import com.clustercontrol.ws.hub.HinemosUnknown_Exception;
import com.clustercontrol.ws.hub.HubEndpoint;
import com.clustercontrol.ws.hub.HubEndpointService;
import com.clustercontrol.ws.hub.InvalidRole_Exception;
import com.clustercontrol.ws.hub.InvalidSetting_Exception;
import com.clustercontrol.ws.hub.InvalidUserPass_Exception;
import com.clustercontrol.ws.hub.LogFormat;
import com.clustercontrol.ws.hub.LogFormatDuplicate_Exception;
import com.clustercontrol.ws.hub.LogFormatKeyPatternDuplicate_Exception;
import com.clustercontrol.ws.hub.LogFormatNotFound_Exception;
import com.clustercontrol.ws.hub.LogFormatUsed_Exception;
import com.clustercontrol.ws.hub.LogTransferDuplicate_Exception;
import com.clustercontrol.ws.hub.LogTransferNotFound_Exception;
import com.clustercontrol.ws.hub.StringQueryInfo;
import com.clustercontrol.ws.hub.StringQueryResult;
import com.clustercontrol.ws.hub.TransferInfo;
import com.clustercontrol.ws.hub.TransferInfoDestTypeMst;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class HubEndpointWrapper {
	private EndpointUnit endpointUnit;

	public HubEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static HubEndpointWrapper getWrapper(String managerName) {
		return new HubEndpointWrapper(EndpointManager.getActive(managerName));
	}
	
	private static List<EndpointSetting<HubEndpoint>> getHubEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(HubEndpointService.class, HubEndpoint.class);
	}

	/**
	 * 
	 * @param formatId
	 * @return
	 * @throws InvalidUserPass_Exception 
	 * @throws InvalidRole_Exception 
	 * @throws HinemosUnknown_Exception 
	 * @throws InvalidRole_Exception 
	 */
	public LogFormat getLogFormat(String formatId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getLogFormat(formatId);
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("getLogFormat(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<String> getLogFormatIdList(String ownerRoleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getLogFormatIdList(ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("getLogFormatIdList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<LogFormat> getLogFormatList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getLogFormatList();
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("getLogFormatList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<LogFormat> getLogFormatListByOwnerRole(String ownerRoleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getLogFormatListByOwnerRole(ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("getLogCollectListByOwnerRole(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	
	public void addLogFormat(LogFormat format) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, LogFormatDuplicate_Exception, LogFormatKeyPatternDuplicate_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addLogFormat(format);
				return;
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("addLogFormat(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public void modifyLogFormat(LogFormat format) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, LogFormatKeyPatternDuplicate_Exception, LogFormatNotFound_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyLogFormat(format);
				return;
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("modifyLogFormat(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public void deleteLogFormat(List<String> formatIdList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, LogFormatNotFound_Exception, LogFormatUsed_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteLogFormat(formatIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("deleteLogFormat(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}


	public TransferInfo getTransferInfo(String transferId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getTransferInfo(transferId);
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("getLogTransfer(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<String> getTransferInfoIdList(String ownerRoleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getTransferInfoIdList(ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("getLogTransferIdList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<TransferInfo> getTransferInfoList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getTransferInfoList();
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("getLogTransferList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<TransferInfo> getTransferInfoListByOwnerRole(String ownerRoleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getTransferInfoListByOwnerRole(ownerRoleId);
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("getLogTransferListByOwnerRole(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	public void addTransferInfo(TransferInfo transfer) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, LogTransferDuplicate_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addTransferInfo(transfer);
				return;
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("addLogTransfer(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public void modifyTransferInfo(TransferInfo transfer) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, LogTransferNotFound_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyTransferInfo(transfer);
				return;
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("modifyLogTransfer(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public void deleteTransferInfo(List<String> transferIdList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, LogTransferNotFound_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteTransferInfo(transferIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("deleteLogTransfer(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * 
	 * @return
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public List<TransferInfoDestTypeMst> getTransferInfoDestTypeMstList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getTransferInfoDestTypeMstList();
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("getLogTransferDestTypeMstList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	/**
	 * @param query
	 * @return
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws HinemosDbTimeout_Exception
	 * @throws InvalidSetting_Exception
	 */
	public StringQueryResult queryCollectStringData(StringQueryInfo query)  throws InvalidUserPass_Exception, InvalidRole_Exception, HinemosUnknown_Exception, HinemosDbTimeout_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<HubEndpoint> endpointSetting : getHubEndpoint(endpointUnit)) {
			try {
				HubEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.queryCollectStringData(query);
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("queryCollectStringData(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}	
	
}
