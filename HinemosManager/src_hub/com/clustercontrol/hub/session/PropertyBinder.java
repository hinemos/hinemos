/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.session;

import java.util.Map;

import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.hub.model.CollectStringKeyInfo;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.notify.monitor.model.EventLogEntity;

public interface PropertyBinder {
	String bind(EventLogEntity event, String param) throws TransferException;
	String bind(JobSessionEntity job, String param) throws TransferException;
	String bind(CollectStringKeyInfo key, CollectStringData string, String param) throws TransferException;
	String bind(CollectKeyInfo key, CollectData numeric, String param) throws TransferException;
	
	Map<String, String> bind(EventLogEntity event, Map<String, String> params) throws TransferException;
	Map<String, String> bind(JobSessionEntity job, Map<String, String> params) throws TransferException;
	Map<String, String> bind(CollectStringKeyInfo key, CollectStringData strin, Map<String, String> params) throws TransferException;
	Map<String, String> bind(CollectKeyInfo key, CollectData numeric, Map<String, String> params) throws TransferException;
}