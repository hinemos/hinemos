/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.SendSdmlMessageRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.SendQueue.SendableObject;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;

public class SdmlMessageSendableObject implements SendableObject {
	private static Log log = LogFactory.getLog(SdmlMessageSendableObject.class);
	private SendSdmlMessageRequest request;

	public SdmlMessageSendableObject(SendSdmlMessageRequest request){
		this.request = request;
	}

	public void sendMessage(String agentRequsetId) throws InvalidRole, InvalidUserPass, InvalidSetting, RestConnectFailed, HinemosUnknown{
		log.info("Sdml Send Message : monitorId =" + request.getOutputBasicInfo().getMonitorId() + ", message ="
				+ request.getOutputBasicInfo().getMessage());
		
		SendSdmlMessageRequest req = new SendSdmlMessageRequest();
		req.setAgentInfo(Agent.getAgentInfoRequest());
		req.setOutputBasicInfo(request.getOutputBasicInfo());
		SdmlAgentRestClientWrapper.agentSdmlSendSdmlMessage(req, agentRequsetId);
	}
}
