/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openapitools.client.model.AgtCustomMonitorInfoResponse;
import org.openapitools.client.model.AgtCustomMonitorVarsInfoResponse;
import org.openapitools.client.model.AgtCustomResultDTORequest;

import com.clustercontrol.util.HinemosTime;

/**
 * Utilities for Command Monitoring WS DTO
 *
 */
public class CommandMonitoringWSUtil {

	public static Map<String, String> getVariable(AgtCustomMonitorInfoResponse config, String facilityId) {
		// Local Variable
		AgtCustomMonitorVarsInfoResponse variableDTO = null;
		Map<String, String> ret = new HashMap<String, String>();

		// Main
		variableDTO = getVariableDTO(config, facilityId);
		if (variableDTO != null && variableDTO.getVariables() != null) {
			for (Entry<String, String> entry : variableDTO.getVariables().entrySet()) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}

		return ret;
	}

	public static AgtCustomMonitorVarsInfoResponse getVariableDTO(AgtCustomMonitorInfoResponse config, String facilityId) {
		// Local Variable
		AgtCustomMonitorVarsInfoResponse ret = null;

		// Main
		if (config.getVariables() != null) {
			for (AgtCustomMonitorVarsInfoResponse vdto : config.getVariables()) {
				if (facilityId.equals(vdto.getFacilityId())) {
					ret = vdto;
					break;
				}
			}
		}

		return ret;
	}

	public static String toStringCommandExecuteDTO(AgtCustomMonitorInfoResponse dto) {
		// Local Variables
		String ret = null;
		String variablesStr = null;

		// MAIN
		if (dto != null) {
			if (dto.getVariables() != null) {
				for (AgtCustomMonitorVarsInfoResponse vdto : dto.getVariables()) {
					if (variablesStr == null) {
						variablesStr = vdto.getFacilityId();
					} else {
						variablesStr += ", " + vdto.getFacilityId();
					}
				}
			}

			ret = "CommandConfig [monitorId = " + dto.getMonitorId()
					+ ", effectiveUser = " + dto.getEffectiveUser()
					+ ", command = " + dto.getCommand()
					+ ", timeout = " + dto.getTimeout()
					+ ", interval = " + dto.getInterval()
					+ ", calendar = " + (dto.getCalendar() == null ? null : dto.getCalendar().getCalendarId())
					+ ", variables = (" + variablesStr + ")"
					+ "]";
		}

		return ret;
	}

	public static String toStringCommandVariableDTO(AgtCustomMonitorVarsInfoResponse dto) {
		// Local Variables
		String ret = null;
		String variableStr = null;

		// MAIN
		if (dto != null) {
			if (dto.getVariables() != null) {
				StringBuilder temp = new StringBuilder();
				for (Entry<String, String> entry : dto.getVariables().entrySet()) {
					temp.append(temp.length() == 0 ? "" : ", ");
					temp.append("[key = " + entry.getKey() + ", value = " + entry.getValue() + "]");
				}
				variableStr = temp.length() == 0 ? null: temp.toString();
			}

			ret = "CommandVariableDTO [facilityId = " + dto.getFacilityId()
					+ "variables = (" + variableStr + ")"
					+ "]";
		}

		return ret;
	}
	
	public static String toShortString(AgtCustomResultDTORequest dto) {
		// Local Variables
		String ret = null;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		
		// MAIN
		if (dto != null) {
			ret = "CommandResultDTO [monitorId = " + dto.getMonitorId()
					+ ", facilityId = " + dto.getFacilityId()
					+ ", command = " + dto.getCommand()
					+ ", user = " + dto.getUser()
					+ ", timeout = " + dto.getTimeout()
					+ ", exitCode = " + dto.getExitCode()
					+ ", collectDate = " + sdf.format(new Date(dto.getCollectDate()))
					+ ", executeDate = " + sdf.format(new Date(dto.getExecuteDate()))
					+ ", exitDate = " + sdf.format(new Date(dto.getExitDate()))
					+ "]";

		}
		
		return ret;
	}
	
	public static String toString(AgtCustomResultDTORequest dto) {
		// Local Variables
		String ret = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		
		// MAIN
		if (dto != null) {
			ret = "CommandResultDTO [monitorId = " + dto.getMonitorId()
					+ ", facilityId = " + dto.getFacilityId()
					+ ", command = " + dto.getCommand()
					+ ", user = " + dto.getUser()
					+ ", timeout = " + dto.getTimeout()
					+ ", exitCode = " + dto.getExitCode()
					+ ", stdout = " + dto.getStdout()
					+ ", stderr = " + dto.getStderr()
					+ ", collectDate = " + sdf.format(new Date(dto.getCollectDate()))
					+ ", executeDate = " + sdf.format(new Date(dto.getExecuteDate()))
					+ ", exitDate = " + sdf.format(new Date(dto.getExitDate()))
					+ ", results = (" + toString(dto.getResults()) + ")"
					+ ", invalieLines = (" + toString(dto.getInvalidLines()) + ")"
					+ "]";

		}

		return ret;
	}

	public static String toString(Map<String, String> dto) {
		// Local Variables
		String ret = null;
		// MAIN
		if (dto != null) {
			StringBuilder temp = new StringBuilder();
			for (Entry<String, String> entry : dto.entrySet()) {
				temp.append("[key = " + entry.getKey()
						+ ", value = " + entry.getValue()
						+ "]");
			}
			ret = temp.length() == 0 ? null: temp.toString();
		}
		return ret;
	}

}
