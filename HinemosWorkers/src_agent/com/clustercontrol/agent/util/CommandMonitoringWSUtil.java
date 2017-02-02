/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.agent.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.monitor.CommandExecuteDTO;
import com.clustercontrol.ws.monitor.CommandResultDTO;
import com.clustercontrol.ws.monitor.CommandResultDTO.InvalidLines;
import com.clustercontrol.ws.monitor.CommandResultDTO.Results;
import com.clustercontrol.ws.monitor.CommandVariableDTO;
import com.clustercontrol.ws.monitor.CommandVariableDTO.Variables.Entry;

/**
 * Utilities for Command Monitoring WS DTO
 *
 */
public class CommandMonitoringWSUtil {

	public static Map<String, String> getVariable(CommandExecuteDTO dto, String facilityId) {
		// Local Variable
		CommandVariableDTO variableDTO = null;
		Map<String, String> ret = new HashMap<String, String>();

		// Main
		variableDTO = getVariableDTO(dto, facilityId);
		if (variableDTO != null && variableDTO.getVariables() != null && variableDTO.getVariables().getEntry() != null) {
			for (CommandVariableDTO.Variables.Entry entry : variableDTO.getVariables().getEntry()) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}

		return ret;
	}

	public static CommandVariableDTO getVariableDTO(CommandExecuteDTO dto, String facilityId) {
		// Local Variable
		CommandVariableDTO ret = null;

		// Main
		if (dto.getVariables() != null) {
			for (CommandVariableDTO vdto : dto.getVariables()) {
				if (facilityId.equals(vdto.getFacilityId())) {
					ret = vdto;
					break;
				}
			}
		}

		return ret;
	}

	public static String toStringCommandExecuteDTO(CommandExecuteDTO dto) {
		// Local Variables
		String ret = null;
		String variablesStr = null;

		// MAIN
		if (dto != null) {
			if (dto.getVariables() != null) {
				for (CommandVariableDTO vdto : dto.getVariables()) {
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

	public static String toStringCommandVariableDTO(CommandVariableDTO dto) {
		// Local Variables
		String ret = null;
		String variableStr = null;

		// MAIN
		if (dto != null) {
			if (dto.getVariables() != null && dto.getVariables().getEntry() != null) {
				StringBuilder temp = new StringBuilder();
				for (Entry entry : dto.getVariables().getEntry()) {
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
	
	public static String toShortString(CommandResultDTO dto) {
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
					+ ", timeout = " + dto.isTimeout()
					+ ", exitCode = " + dto.getExitCode()
					+ ", collectDate = " + sdf.format(new Date(dto.getCollectDate()))
					+ ", executeDate = " + sdf.format(new Date(dto.getExecuteDate()))
					+ ", exitDate = " + sdf.format(new Date(dto.getExitDate()))
					+ "]";

		}
		
		return ret;
	}
	
	public static String toString(CommandResultDTO dto) {
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
					+ ", timeout = " + dto.isTimeout()
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

	public static String toString(Results dto) {
		// Local Variables
		String ret = null;
		// MAIN
		if (dto != null && dto.getEntry() != null) {
			StringBuilder temp = new StringBuilder();
			for (CommandResultDTO.Results.Entry entry : dto.getEntry()) {
				temp.append("[key = " + entry.getKey()
						+ ", value = " + entry.getValue()
						+ "]");
			}
			ret = temp.length() == 0 ? null: temp.toString();
		}
		return ret;
	}

	public static String toString(InvalidLines dto) {
		// Local Variables
		String ret = null;

		// MAIN
		if (dto != null && dto.getEntry() != null) {
			StringBuilder temp = new StringBuilder();
			for (CommandResultDTO.InvalidLines.Entry entry : dto.getEntry()) {
				temp.append("[key = " + entry.getKey()
						+ ", value = " + entry.getValue()
						+ "]");
			}
			ret = temp.length() == 0 ? null: temp.toString();
		}
		return ret;
	}

}
