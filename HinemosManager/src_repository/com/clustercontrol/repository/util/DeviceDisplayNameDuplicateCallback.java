/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.ExclusiveJpaTransactionCallback;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.util.apllog.AplLogger;

public class DeviceDisplayNameDuplicateCallback extends ExclusiveJpaTransactionCallback {
	
	private static final Log log = LogFactory.getLog( DeviceDisplayNameDuplicateCallback.class );

	private String facilityId = null;
	private StringBuffer duplicatedDeviceMsg = null;
	private Map<String, List<String>> duplicatedDeviceMap = new HashMap<>();

	public DeviceDisplayNameDuplicateCallback(String facilityId) {
		this.facilityId = facilityId;
		duplicatedDeviceMsg = new StringBuffer(String.format("Duplicate device display name values ​​entered. facilityId=%s :", facilityId));
	}

	public void addDuplicateDevice(NodeDeviceInfo device) {
		log.warn(String.format("Duplicate device display name values entered."
				+ " facilityId=%s : %s[deviceName=%s, deviceDisplayName=%s, deviceIndex=%s]",
				facilityId, device.getDeviceType(), device.getDeviceName(), device.getDeviceDisplayName(), device.getDeviceIndex()));
		List<String> devicelist = duplicatedDeviceMap.getOrDefault(device.getDeviceType(), new ArrayList<>());
		devicelist.add(device.getDeviceDisplayName());
		duplicatedDeviceMap.put(device.getDeviceType(), devicelist);
	}

	@Override
	public void postCommit() {
		duplicatedDeviceMsg.append(duplicatedDeviceMap.entrySet().stream()
				.map(entry -> "{devicetype=" + entry.getKey() + ":deviceDisplayName=[" + String.join(",", entry.getValue())+ "]}")
				.collect(Collectors.joining(", ")));
		AplLogger.put(InternalIdCommon.NODE_REG_SYS_001, new String[]{}, duplicatedDeviceMsg.toString());
	}

	@Override
	public boolean isDuplicate(JpaTransactionCallback callback) {
		if (!DeviceDisplayNameDuplicateCallback.class.isInstance(callback)) {
			return false;
		}
		DeviceDisplayNameDuplicateCallback ddc = (DeviceDisplayNameDuplicateCallback) callback;
		if (this.facilityId != null && !this.facilityId.equals(ddc.facilityId)) {
			return false;
		}
		return true;
	}
}
